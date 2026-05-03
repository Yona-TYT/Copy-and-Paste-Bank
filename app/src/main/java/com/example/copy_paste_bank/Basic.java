package com.example.copy_paste_bank;

import static android.widget.GridLayout.spec;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Objects;

public class Basic {
    @SuppressLint("StaticFieldLeak")
    private static Context mContex;
    private static String oldMsg = "";
    private static long lastShowTime = 0;

    public Basic(Context mContex) {
        Basic.mContex = mContex;
        if (mContex == null){
            Basic.mContex = AppContextProvider.getAppContext();
        }
    }

    public int getPixelSiz(int id) {
        return mContex.getResources().getDimensionPixelSize(id);
    }

    public float getFloatSiz(int id) {
        DisplayMetrics metrics = new DisplayMetrics();
        float scaledDensity = mContex.getResources().getDisplayMetrics().scaledDensity;
        return getPixelSiz(id) / scaledDensity;
    }

    public static String setFormatAlternate(String value, boolean langEs){
        value = value.replaceAll("([^\\d.,-])","");
        if (value.isEmpty()){
            value = "0";
        }
        if(langEs) {
            return setFormatterInternal(Double.parseDouble(value), new Locale("es", "VE"));
        }
        else{
            return setFormatterInternal(Double.parseDouble(value), Locale.US);
        }
    }

    public static String setFormatAlternate(Double value, boolean langEs) {
        if (langEs) {
            return setFormatterEs(value);
        }
        else {
            return setFormatterEn(value);
        }
    }

    public static String setFormatterEs(String value){
        value = value.replaceAll("([^\\d.,-])","");
        if (value.isEmpty()){
            value = "0";
        }
        return setFormatterInternal(Double.parseDouble(value), new Locale("es", "VE"));
    }

    public static String setFormatterEn(Double value) {
        return setFormatterInternal(value, Locale.US);           // 1,234.56
    }

    public static String setFormatterEs(Double value) {
        return setFormatterInternal(value, new Locale("es", "VE"));
    }
    public static String setFormatterInternal(Double value, Locale locale){
        if (value == null) return "";
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        df.applyPattern("#,##0.00");
        return df.format(value);
    }

    public static Double getDouble(String value, boolean isEs) throws ParseException {
        Locale locale = isEs ? new Locale("es", "VE") : Locale.US;
        return getDouble(value, locale);
    }

    public static Double getDouble(String value, Locale locale) throws ParseException {

        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        df.applyPattern("###,##0.00");
        return Objects.requireNonNull(df.parse(value)).doubleValue();
    }

    public static Double notFormatter(String value) throws ParseException {
        value = value.replaceAll("([^\\d.,-])","");
        if (value.isEmpty()){
            value = "0,00";
        }
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("ES"));
        DecimalFormat formatter = (DecimalFormat) nf;
        formatter.applyPattern("###,##0.00");
        return Objects.requireNonNull(formatter.parse(value)).doubleValue();
    }

    public static String nameProcessor(String value){
        String text = value.replaceAll("([^\\s0-9a-zA-Z]+)", "");
        text = text.replaceAll("(\\s{2,})", " ");
        text = text.replaceAll("(^\\s)|(\\s$)", "");
        return text;
    }

    public static String inputProcessor(String value){
        return value.replaceAll("([;,\"<>]+)", "");
    }

    public static void msg(String msg){
        msgInternal(msg, false);
    }

    public static void msg(String msg, boolean isClipboard){
        msgInternal(msg, isClipboard);
    }

    public static void msgInternal(String msg, boolean isClipboard)
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            long currentTime = System.currentTimeMillis();
            long fiveSecondsAgo = currentTime - 3000;  // 5s en ms

            if (oldMsg.equals(msg) && lastShowTime > fiveSecondsAgo) {
                return;  // No mostrar
            }
            // Actualiza el mensaje anterior y el tiempo de muestra
            oldMsg = msg;
            lastShowTime = currentTime;

            TextView text = new TextView(mContex);
            // Se ajustan los parametros del Texto ----------------------------------
            text.setText(msg);
            text.setTypeface(Typeface.DEFAULT_BOLD);
            text.setGravity(Gravity.CENTER);
            text.setWidth(R.dimen.spinner_w1);
            text.setMaxLines(1);
            text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.text_color1));
            text.setBackgroundColor(ContextCompat.getColor(text.getContext(), R.color.text_background2));
            text.setPadding(10, 5, 10, 5);

            CardView cardView = new CardView(mContex);
            cardView.setLayoutParams(new GridLayout.LayoutParams(spec(140), spec(150)));
            cardView.addView(text);
            cardView.setRadius(10f);

            Toast mToast = new Toast(mContex);
            mToast.setView(cardView);
            mToast.setDuration(Toast.LENGTH_LONG);
            mToast.show();

            //Copiar al portapapeles
            if(isClipboard){
                ClipboardManager clipboard = (ClipboardManager) mContex.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Texto Extraído", msg);
                clipboard.setPrimaryClip(clip);
            }
        });
    }

    public static String parseMoneyValue(String value, String groupingSeparator, String currencySymbol) {
        return value.replace(groupingSeparator, "").replace(currencySymbol, "");
    }

    public static boolean isLollipopAndAbove() {
        return true;
    }

    /**
     * Configura un EditText (o CurrencyEditText) en modo solo lectura
     * @param editText El EditText a configurar
     * @param readOnly true = solo lectura, false = editable
     */
    public static void setReadOnly(EditText editText, boolean readOnly) {
        if (editText == null) return;

        // Configuración de interactividad
        editText.setFocusable(!readOnly);
        editText.setFocusableInTouchMode(!readOnly);
        editText.setCursorVisible(!readOnly);
        editText.setClickable(!readOnly);
        editText.setEnabled(!readOnly);

        if (readOnly) {
            // Guardamos el fondo original solo la primera vez
            if (editText.getTag() == null) {
                editText.setTag(editText.getBackground());   // Guardamos en el Tag
            }
            editText.setBackground(null);                    // Modo solo lectura limpio
        } else {
            // Restauramos el fondo original
            Drawable originalBackground = (Drawable) editText.getTag();

            if (originalBackground != null) {
                editText.setBackground(originalBackground);
            } else {
                // Fallback seguro: restaurar fondo del tema
                editText.setBackgroundTintList(null);
                if (editText.getBackground() != null) {
                    editText.getBackground().clearColorFilter();
                }
            }
        }
    }
}