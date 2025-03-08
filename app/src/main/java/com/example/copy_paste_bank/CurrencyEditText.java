package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import android.graphics.Rect;


public class CurrencyEditText extends AppCompatEditText {
    private String currencySymbolPrefix = null;
    private CurrencyInputWatcher textWatcher;
    private Locale locale = Locale.forLanguageTag("ES");//locale; //Esto es un experimentoooooo!!!!!!!1//Locale.getDefault();
    private int maxDP;

    @SuppressLint("PrivateResource")
    public CurrencyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        boolean useCurrencySymbolAsHint = false;
        setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        setKeyListener(DigitsKeyListener.getInstance("0123456789.,"));

        String localeTag = null;
        String prefix;

        int[] styleable = R.styleable.CurrencyEditText;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, styleable, 0, 0);
        try {
            prefix = a.getString(R.styleable.CurrencyEditText_currencySymbol);
            if (prefix == null) prefix = "";
            localeTag = a.getString(androidx.appcompat.R.styleable.AppCompatTextView_textLocale);
            useCurrencySymbolAsHint = a.getBoolean(com.google.android.material.R.styleable.TextInputLayout_hintTextColor, false);
            maxDP = a.getInt(R.styleable.CurrencyEditText_maxNumberOfDecimalDigits, 2);
        } finally {
            a.recycle();
        }

        currencySymbolPrefix = prefix.isEmpty() ? "" : prefix + " ";
        if (useCurrencySymbolAsHint) setHint(currencySymbolPrefix);
        if (Basic.isLollipopAndAbove() && localeTag != null && !localeTag.isEmpty()) locale = getLocaleFromTag(localeTag);
        textWatcher = new CurrencyInputWatcher(this, currencySymbolPrefix, locale, maxDP);
        addTextChangedListener(textWatcher);
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
        super.setText(text, type);
        if (getText() != null) setSelection(getText().length());
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            removeTextChangedListener(textWatcher);
            addTextChangedListener(textWatcher);
            if (getText().toString().isEmpty()) setText(currencySymbolPrefix);
        } else {
            removeTextChangedListener(textWatcher);
            if (getText().toString().equals(currencySymbolPrefix)) setText("");
        }
    }

    @Override
    public void onSelectionChanged(int selStart, int selEnd) {
        if (currencySymbolPrefix == null) return;
        int symbolLength = currencySymbolPrefix.length();
        if (selEnd < symbolLength && getText().toString().length() >= symbolLength) {
            setSelection(symbolLength);
        } else {
            super.onSelectionChanged(selStart, selEnd);
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