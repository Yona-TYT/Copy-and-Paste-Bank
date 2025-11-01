package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurrencyInputWatcher implements TextWatcher {
    private final EditText editText;
    private final String currencySymbol;
    private final Locale locale;
    private final int maxNumberOfDecimalPlaces;

    private boolean hasDecimalPoint;
    private int expectedCursorPos;
    private final DecimalFormat wholeNumberDecimalFormat;
    private final DecimalFormat fractionDecimalFormat;
    final DecimalFormatSymbols decimalFormatSymbols;

    private static final String FRACTION_FORMAT_PATTERN_PREFIX = "#,##0.";

    public CurrencyInputWatcher(EditText editText, String currencySymbol, Locale locale, int maxNumberOfDecimalPlaces) {
        this.editText = editText;
        this.currencySymbol = currencySymbol;
        this.locale = locale;
        this.maxNumberOfDecimalPlaces = maxNumberOfDecimalPlaces;

        if (maxNumberOfDecimalPlaces < 1) {
            throw new IllegalArgumentException("Maximum number of Decimal Digits must be a positive integer");
        }

        this.wholeNumberDecimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.wholeNumberDecimalFormat.applyPattern("#,##0");

        this.fractionDecimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        this.decimalFormatSymbols = this.wholeNumberDecimalFormat.getDecimalFormatSymbols();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        this.fractionDecimalFormat.setDecimalSeparatorAlwaysShown(true);
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        this.hasDecimalPoint = s.toString().contains(String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()));
        String newInputString = s.toString();
        if(newInputString.length() > this.currencySymbol.length()) {
            Pattern patt = Pattern.compile("(\\D$)");
            Matcher m = patt.matcher(newInputString);
            if(m.find()){
                this.hasDecimalPoint = true;
            }
        }

        // Calcula posición esperada del cursor
        int numStart = this.currencySymbol.length();
        if (start == numStart && before == (s.length() - numStart - count)) {
            this.expectedCursorPos = numStart + count;
        } else {
            this.expectedCursorPos = start + count;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void afterTextChanged(Editable s) {

        String newInputString = s.toString();
        if(!newInputString.startsWith(this.currencySymbol)) {
            newInputString = newInputString.replaceAll("([^.,\\d])","");
        }

        if(newInputString.length() > this.currencySymbol.length()) {
            Pattern patt = Pattern.compile("(\\D$)");
            Matcher m = patt.matcher(newInputString);
            if (m.find()) {
                newInputString = newInputString.replaceAll("\\D$", String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()));
            }
            else {
                patt = Pattern.compile("(\\d\\D$)");
                m = patt.matcher(newInputString);
                if (m.find()) {
                    newInputString = newInputString.replaceAll("\\D$", String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()));
                }
            }
        }

        boolean isParsableString;
        try {
            this.fractionDecimalFormat.parse(newInputString);
            isParsableString = true;
        } catch (ParseException e) {
            isParsableString = false;
        }

        if (newInputString.length() < this.currencySymbol.length() && !isParsableString) {

            this.editText.setText(this.currencySymbol);
            this.editText.setSelection(this.currencySymbol.length());
            return;
        }

        if (newInputString.equals(this.currencySymbol)) {
            this.editText.setSelection(this.currencySymbol.length());
            return;
        }

        this.editText.removeTextChangedListener(this);
        int startLength = this.editText.getText().length();
        int selectionEndIndex = this.editText.getSelectionEnd();  // NUEVO: Obtén el end de la selección anterior
        try {

            String numberWithoutGroupingSeparator = parseMoneyValue(newInputString, String.valueOf(this.decimalFormatSymbols.getGroupingSeparator()), this.currencySymbol);
            if (numberWithoutGroupingSeparator.equals(String.valueOf(this.decimalFormatSymbols.getDecimalSeparator()))) {
                numberWithoutGroupingSeparator = "0" + numberWithoutGroupingSeparator;
            }

            numberWithoutGroupingSeparator = truncateNumberToMaxDecimalDigits(numberWithoutGroupingSeparator, this.maxNumberOfDecimalPlaces, this.decimalFormatSymbols.getDecimalSeparator());

            Number parsedNumber = this.fractionDecimalFormat.parse(numberWithoutGroupingSeparator);
            int selectionStartIndex = this.editText.getSelectionStart();
            if (this.hasDecimalPoint) {
                this.fractionDecimalFormat.applyPattern(FRACTION_FORMAT_PATTERN_PREFIX + getFormatSequenceAfterDecimalSeparator(numberWithoutGroupingSeparator));
                this.editText.setText(this.currencySymbol + this.fractionDecimalFormat.format(parsedNumber));
            }
            else {
                this.editText.setText(this.currencySymbol + this.wholeNumberDecimalFormat.format(parsedNumber));
            }


            int endLength = this.editText.getText().length();
            int selection = expectedCursorPos + (endLength - startLength);
            if (selectionEndIndex == startLength) {
                // FIXED: Difiere al siguiente ciclo para timing de render
                this.editText.post(() -> this.editText.setSelection(endLength));
            } else if (selection > 0 && selection <= endLength) {
                this.editText.setSelection(selection);
            } else {
                this.editText.setSelection(endLength - 1);
            }

            //Basic.msg( "clamp triggered? selection=" + selection + " > endLen=" + endLength, true);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        this.editText.addTextChangedListener(this);
    }

    private static String parseMoneyValue(String inputString, String groupingSeparator, String currencySymbol) {
        return inputString.replace(groupingSeparator, "").replace(currencySymbol, "");
    }

    private static String truncateNumberToMaxDecimalDigits(String numberString, int maxDecimalDigits, char decimalSeparator) {
        int decimalSeparatorIndex = numberString.indexOf(decimalSeparator);
        if (decimalSeparatorIndex == -1) {
            return numberString;
        }
        int decimalDigits = numberString.length() - decimalSeparatorIndex - 1;
        if (decimalDigits <= maxDecimalDigits) {
            return numberString;
        }
        return numberString.substring(0, decimalSeparatorIndex + maxDecimalDigits + 1);
    }

    private String getFormatSequenceAfterDecimalSeparator(String number) {
        int noOfCharactersAfterDecimalPoint = number.length() - number.indexOf(this.decimalFormatSymbols.getDecimalSeparator()) - 1;
        return "0".repeat(Math.min(noOfCharactersAfterDecimalPoint, this.maxNumberOfDecimalPlaces));
    }
}
