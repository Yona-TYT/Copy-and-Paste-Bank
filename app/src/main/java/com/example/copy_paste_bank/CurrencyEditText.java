package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.graphics.Rect;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;


public class CurrencyEditText extends AppCompatEditText {
    private String currencySymbolPrefix = null;
    private CurrencyInputWatcher textWatcher;
    private Locale locale = Locale.forLanguageTag("ES");//locale; //Esto es un experimentoooooo!!!!!!!1//Locale.getDefault();
    private int maxDP;
    private boolean isTouch = false;
    private Context mContex;
    private GestureDetector gestureDetector;
    private List<Integer> viewIdsToHide = new ArrayList<>();  // NUEVO: IDs de views a ocultar
    private boolean keepFocusOnKeyboardClose;

    @SuppressLint({"PrivateResource", "DiscouragedApi"})
    public CurrencyEditText(Context mContext, AttributeSet attrs) {
        super(mContext, attrs);

        this.mContex = mContext;

        boolean useCurrencySymbolAsHint = false;
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));

        String localeTag = null;
        String prefix;

        int[] styleable = R.styleable.CurrencyEditText;
        TypedArray a = mContext.getTheme().obtainStyledAttributes(attrs, styleable, 0, 0);
        try {
            prefix = a.getString(R.styleable.CurrencyEditText_currencySymbol);
            if (prefix == null) prefix = "";
            localeTag = a.getString(androidx.appcompat.R.styleable.AppCompatTextView_textLocale);
            useCurrencySymbolAsHint = a.getBoolean(com.google.android.material.R.styleable.TextInputLayout_hintTextColor, false);
            maxDP = a.getInt(R.styleable.CurrencyEditText_maxNumberOfDecimalDigits, 2);

            String viewsToHideStr = a.getString(R.styleable.CurrencyEditText_viewsToHideIds);
            List<Integer> viewIdsToHide = new ArrayList<>();
            if (viewsToHideStr != null && !viewsToHideStr.trim().isEmpty()) {
                String[] idNames = viewsToHideStr.split(",");
                for (String idName : idNames) {
                    idName = idName.trim();
                    if (!idName.isEmpty()) {
                        int id = getResources().getIdentifier(idName, "id", getContext().getPackageName());
                        if (id != 0) {  // ID válido
                            viewIdsToHide.add(id);
                        } else {
                            Log.w("CurrencyEditText", "ID no encontrado: " + idName);  // Warning si inválido
                        }
                    }
                }
            }
            this.viewIdsToHide = viewIdsToHide;

            this.keepFocusOnKeyboardClose = a.getBoolean(R.styleable.CurrencyEditText_keepFocusOnKeyboardClose, false);

        } finally {
            a.recycle();
        }
        currencySymbolPrefix = prefix.isEmpty() ? "" : prefix + " ";
        if (useCurrencySymbolAsHint) setHint(currencySymbolPrefix);
        if (Basic.isLollipopAndAbove() && localeTag != null && !localeTag.isEmpty()) locale = getLocaleFromTag(localeTag);
        textWatcher = new CurrencyInputWatcher(this, currencySymbolPrefix, locale, maxDP);
        addTextChangedListener(textWatcher);

        // NUEVO: Inicializa detector para double-tap
        gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                String text = getText().toString();
                if (text.startsWith(currencySymbolPrefix) && !text.equals(currencySymbolPrefix)) {
                    int start = currencySymbolPrefix.length();
                    int end = text.length();
                    setSelection(start, end); // Selecciona solo dígitos
                    return true;
                }
                return false;
            }
        });
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        invalidateTextWatcher();
    }

    public void setLocale(String localeTag) {
        locale = Locale.forLanguageTag(localeTag);
        invalidateTextWatcher();
    }

    public void setCurrencySymbol(String currencySymbol, boolean useCurrencySymbolAsHint) {
        currencySymbolPrefix = currencySymbol + " ";
        if (useCurrencySymbolAsHint) setHint(currencySymbolPrefix);
        invalidateTextWatcher();
    }

    public void setMaxNumberOfDecimalDigits(int maxDP) {
        this.maxDP = maxDP;
        invalidateTextWatcher();
    }

    private void invalidateTextWatcher() {
        removeTextChangedListener(textWatcher);
        textWatcher = new CurrencyInputWatcher(this, currencySymbolPrefix, locale, maxDP);
        addTextChangedListener(textWatcher);
    }

    public double getNumericValue() {
        return parseMoneyValueWithLocale(
                locale,
                getText().toString(),
                textWatcher.decimalFormatSymbols.getGroupingSeparator() + "",
                currencySymbolPrefix
        ).doubleValue();
    }

    public BigDecimal getNumericValueBigDecimal() {
        return new BigDecimal(
                parseMoneyValueWithLocale(
                        locale,
                        getText().toString(),
                        textWatcher.decimalFormatSymbols.getGroupingSeparator() + "",
                        currencySymbolPrefix
                ).toString()
        );
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        isTouch = false;
        super.setText(text, type);
        if (getText() != null) setSelection(getText().length());
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        String currentText = getText().toString();
        if (focused) {
            if (currentText.isEmpty()) {
                setText(currencySymbolPrefix);
            }
            // Auto-seleccionar todo el texto numérico (después del símbolo)
            post(() -> {
                String text = getText().toString(); // NUEVO: Obtén texto actual para verificación
                if (text.startsWith(currencySymbolPrefix) && !text.equals(currencySymbolPrefix)) { // NUEVO: Asegura prefixed y no solo símbolo
                    int start = currencySymbolPrefix.length();
                    int end = text.length();
                    setSelection(start, end); // Selecciona solo dígitos (sin if, ya que verificamos start < end implícitamente)
                } else {
                    int start = currencySymbolPrefix != null ? currencySymbolPrefix.length() : 0;
                    setSelection(start); // Cursor después del símbolo si vacío o solo símbolo
                }
            });

            setupKeyboardListener();
            View parent = getParent() instanceof View ? (View) getParent() : null;
            List<View> views = new ArrayList<>();
            if (parent != null && !viewIdsToHide.isEmpty()) {
                for (int id : viewIdsToHide) {
                    View view = parent.findViewById(id);
                    if (view != null) {
                        views.add(view);
                    }
                }
            }
            addViewsToHide(views);  // Tu método existente

        } else {
            if (currentText.equals(currencySymbolPrefix)) {
                setText("");
            }
            // Ocultar teclado
            InputMethodManager imm = (InputMethodManager) mContex.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }

            // FIXED: Fuerza pérdida de foco y oculta cursor/selección
            clearFocus();  // Pierde foco explícitamente
            setSelection(getSelectionStart());

            // Tu cleanup de listener y toggleViewsVisibility(false); sin cambios
            if (keyboardListener != null) {
                getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
                keyboardListener = null;
            }
            toggleViewsVisibility(false);
            isKeyboardVisible = false;
            viewsToHide.clear();
        }
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        if (currencySymbolPrefix == null){
            return;
        }

        int textLength = getText().toString().length();  // NUEVO: Obtén longitud actual
        int symbolLength = currencySymbolPrefix.length();
        if (textLength >= symbolLength) {
            // Ajusta para excluir símbolo
            if (selStart < symbolLength) {
                selStart = symbolLength;
            }
            if (selEnd < symbolLength) {
                selEnd = symbolLength;
            }
            if (selStart > selEnd) {
                selEnd = selStart;
            }
            // NUEVO: Clamp a rango válido [0, textLength] para evitar IndexOutOfBounds
            selStart = Math.min(selStart, textLength);
            selEnd = Math.min(selEnd, textLength);
            setSelection(selStart, selEnd);
        }
        else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isTouch = !isTouch;
        }

        // NUEVO: Delega al detector para manejar double-tap
        gestureDetector.onTouchEvent(event);

        return super.onTouchEvent(event);
    }

    private boolean isBackspaceHeld = false;  // NUEVO: Flag para estado de hold
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            if (event.getRepeatCount() == 0) {
                // Inicio del long-press: Remueve watcher para permitir continua
                removeTextChangedListener(textWatcher);
                isBackspaceHeld = true;
            }
            // Delega a super para borrar (incluye repeats)
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DEL && isBackspaceHeld) {
            // Al soltar: Reagrega watcher para formatear el resultado final
            addTextChangedListener(textWatcher);
            isBackspaceHeld = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private List<View> viewsToHide = new ArrayList<>();  // Lista de views a ocultar al abrir teclado
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;  // Listener para detectar teclado
    private boolean isKeyboardVisible = false;  // Estado del teclado

    // Método público para agregar views a ocultar
    public void addViewsToHide(List<View> views) {
        this.viewsToHide.addAll(views);
    }

    // Método privado para ocultar/restaurar views
    private void toggleViewsVisibility(boolean hide) {
        for (View view : viewsToHide) {
            if (view != null) {
                view.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        }
    }

    // Método para detectar visibilidad del teclado
    private void setupKeyboardListener() {
        if (keyboardListener == null) {
            keyboardListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    getRootView().getWindowVisibleDisplayFrame(r);
                    int screenHeight = getRootView().getHeight();
                    int keypadHeight = screenHeight - r.bottom;

                    boolean keyboardShown = keypadHeight > screenHeight * 0.15;  // Umbral ~15% para detectar teclado
                    if (keyboardShown != isKeyboardVisible) {
                        isKeyboardVisible = keyboardShown;
                        toggleViewsVisibility(keyboardShown);  // Oculta si visible, muestra si no

                        if (!keyboardShown) {  // FIXED: Al cerrar teclado
                            if (keepFocusOnKeyboardClose) {
                                // Mantén foco si attr=true
                                post(() -> requestFocus());
                            } else {
                                // FIXED: Fuerza pérdida de foco si attr=false (oculta cursor)
                                post(() -> {
                                    clearFocus();
                                    setSelection(getSelectionStart());
                                });
                            }
                        }
                    }
                }
            };
            getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
        }
    }

    private static Locale getLocaleFromTag(String localeTag) {
        String[] parts = localeTag.split("-");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        } else if (parts.length == 3) {
            return new Locale(parts[0], parts[1], parts[2]);
        } else {
            throw new IllegalArgumentException("Invalid locale tag: " + localeTag);
        }
    }

    private static BigDecimal parseMoneyValueWithLocale(Locale locale, String value, String groupingSeparator, String currencySymbolPrefix) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        symbols.setGroupingSeparator(groupingSeparator.charAt(0));
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(symbols);
        format.setParseBigDecimal(true);
        try {
            return (BigDecimal) format.parse(value.replace(currencySymbolPrefix, ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
