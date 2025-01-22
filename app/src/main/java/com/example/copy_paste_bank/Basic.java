package com.example.copy_paste_bank;

import static android.widget.GridLayout.spec;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.copy_paste_bank.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public class Basic {
    private static Context mContex;
    public static boolean isDow = true;
    public static boolean isUp = false;

    public Basic(Context mContex) {
        this.mContex = mContex;
    }

    public int getPixelSiz(int id) {
        return mContex.getResources().getDimensionPixelSize(id);
    }

    public float getFloatSiz(int id) {
        DisplayMetrics metrics = new DisplayMetrics();
        float scaledDensity = mContex.getResources().getDisplayMetrics().scaledDensity;
        return getPixelSiz(id) / scaledDensity;
    }

//    public void keyboardEvent(ConstraintLayout mConstrain, View elm, int opt) {
//        // Para eventos al mostrar o ocultar el teclado
//        mConstrain.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                // on below line we are creating a variable for rect
//                Rect rect = new Rect();
//                View contain = StartVar.mRootView;
//                // on below line getting frame for our relative layout.
//                contain.getWindowVisibleDisplayFrame(rect);
//                // on below line getting screen height for relative layout.
//                int screenHeight = contain.getRootView().getHeight();
//                // on below line getting keypad height.
//                int keypadHeight = screenHeight - rect.bottom;
//                if (keypadHeight > screenHeight * 0.15) {
//                    isDow = false;
//                    isUp = true;
//                    //Toast.makeText(MainActivity.this, "Keyboard is +", Toast.LENGTH_LONG).show();
//                }
//                else {
//                    isDow = true;
//                    isUp = false;
//                    //Toast.makeText(mContex, "Keyboard is -", Toast.LENGTH_LONG).show();
//
//                    if (opt == 0 && elm != null) {
//                        //Toast.makeText(mContex, "Aqui hayyyyyyyy?  " , Toast.LENGTH_LONG).show();
//                        elm.clearFocus();
//                    }
//                }
//            }
//        });
//    }

//    public void steAllKeyEvent(ConstraintLayout mConstrain, List<EditText> mInputList) {
//        for (int i = 0; i < mInputList.size(); i++) {
//            // Para eventos al mostrar o ocultar el teclado
//            keyboardEvent(mConstrain, mInputList.get(i), 0); //opt = 0 is clear elm focus
//            //-------------------------------------------------------------------------------------
//        }
//    }

    public void setAllfocusEvent(View elm, List<EditText> mInputList) {
        for (int i = 0; i < mInputList.size(); i++) {
            mInputList.get(i).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    //Toast.makeText(mContext, "Siz is "+b, Toast.LENGTH_LONG).show();
                    if (b) {
                        elm.setVisibility(View.INVISIBLE);
                    } else {
                        elm.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    public static Float parseFloat(String value){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.##");
        format.setDecimalFormatSymbols(symbols);

        try {
            return format.parse(value).floatValue();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return (float)0.00;
    }

    public static String setDecimal(Float value){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("0.##");
        format.setDecimalFormatSymbols(symbols);

        try {
            return format.parse(Float.toString(value)).toString();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return "0.00";
    }




    public static Float floatFormat(String value) {
        String mValue = value.replaceAll("([^.;^0-9]+)", "");
        mValue = mValue.replaceAll("^.$", "0.00");

        return mValue.isEmpty() ? (float)0 : Float.parseFloat(mValue);
    }



    public static String setMask(String value, String sing) {
        value = value.replaceAll("([^.;^0-9]+)", "");
        value = value.replaceAll("(^\\.$)", "0.");
        return value + " " + sing;
    }

    public static String nameProcessor(String value){
        String text = value.replaceAll("([^\\s;^0-9a-zA-Z]+)", "");
        text = text.replaceAll("(\\s{2,})", " ");
        text = text.replaceAll("(^\\s)|(\\s$)", "");
        return text;
    }

    public static String inputProcessor(String value){
        return value.replaceAll("([;,\"<>]+)", "");
    }

    public static void msg(String msg)
    {
        TextView text = new TextView(mContex);
        // Se ajustan los parametros del Texto ----------------------------------
        text.setText(msg);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setGravity(Gravity.CENTER);
        text.setWidth(R.dimen.spinner_w1);
        text.setMaxLines(1);
        text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.text_color1));
        text.setBackgroundColor(ContextCompat.getColor(text.getContext(), R.color.text_background2));
        text.setPadding(10,5,10,5);

        CardView cardView = new CardView(mContex);
        cardView.setLayoutParams(new GridLayout.LayoutParams(spec(140), spec(150)));
        cardView.addView(text);
        cardView.setRadius(10f);

        Toast mToast = new Toast(mContex);
        mToast.setView(cardView);
        mToast.show();
    }
    public static void checkClt(){

    }

    public static int bitL(int val, int rota) {
        return val << rota;
    }

    public static int bitR(int val, int rota) {
        return (val >> rota) & 1;
    }

    public static int toHex(String value) {
        return Integer.decode(value);
    }

    public static List<Integer> getBits(int n) {
        List<Integer> list = new ArrayList<>();
        while (true) {
            if(n < 32){
                list.add(bitL(0x1, n));
                break;
            }
            else{
                list.add(0);
                n -= 32;
            }
        }
        return list;
    }

    public static List<Integer> getBits(String text) {
        String[] sList = text.split("'");
        int n = 0;
        List<Integer> list = new ArrayList<>();
        for (String val: sList){
            list.add(Integer.decode(val));
        }
        return list;
    }

    public static String saveBits(List<Integer> list) {
        String text = "";
        for (Integer val: list){
            text = String.format("%x",val);
           if(!text.startsWith("0x")){
               text = "0x"+text;
           }
           //msg(text);
        }
        return text;
    }
    public static String saveNewBit(int r){
        String bit = "";
        for(int i = 0 ; i < r; i += 32) {
            if(r < 32) {
                bit = String.format("0x%x", Basic.bitL(0x1, r))+"'";
            }
            else{
                r -= 32;
            }
        }
        return bit;
    }
}
