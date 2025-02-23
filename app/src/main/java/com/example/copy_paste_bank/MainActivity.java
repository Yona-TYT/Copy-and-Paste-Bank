package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mButt1;
    private Button mButt2;
    private Button mButt3;
    private Button mButt4;
    private Button mButt5;
    private Button mButt6;
    private Button mButt7;

    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private TextView mText4;
    private TextView mText5;

    private TextView mText6; //Test only

    private TextView mText7;

    private Spinner mSpin1;
    private List<String> mSpinL1 = Arrays.asList("BCV", "Paralelo", "Promedio");
    private int currSel1 = 0;

    private Switch mSw1;
    private boolean isConv = false;

    public String[] mResList = DataExtracts.mResList;
    public String[] mDebug = DataExtracts.mDebug;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mButt1 = findViewById(R.id.butt1);
        mButt2 = findViewById(R.id.butt2);
        mButt3 = findViewById(R.id.butt3);
        mButt4 = findViewById(R.id.butt4);
        mButt5 = findViewById(R.id.butt5);
        mButt6 = findViewById(R.id.butt6);
        mButt7 = findViewById(R.id.butt7);

        mText1 = findViewById(R.id.text1);
        mText2 = findViewById(R.id.text2);
        mText3 = findViewById(R.id.text3);
        mText4 = findViewById(R.id.text4);
        mText5 = findViewById(R.id.text5);
        mText6 = findViewById(R.id.text6); //Test only
        mText7 =  findViewById(R.id.dollarView);

        mSpin1 = findViewById(R.id.spin1);

        mSw1 = findViewById(R.id.switch1);

        mButt1.setOnClickListener(this);
        mButt2.setOnClickListener(this);
        mButt3.setOnClickListener(this);
        mButt4.setOnClickListener(this);
        mButt5.setOnClickListener(this);
        mButt6.setOnClickListener(this);
        mButt7.setOnClickListener(this);
        mSw1.setOnClickListener(this);

        new Basic(this);

        mSw1.setChecked(false);

        GetDollar mGet = new GetDollar(getApplicationContext(), MainActivity.this, mSpin1 , mText7);
        try {
            GetDollar.urlRun();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //Para la lista de Monjitores de dolar ------------------------------------------------------
        SelecAdapter adapt1 = new SelecAdapter(this, mSpinL1);
        mSpin1.setAdapter(adapt1);
        //mSpin1.setSelection(currSel1); //Set La Moneda como default
        mSpin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currSel1 = i;
                mSw1.setChecked(false);
                isConv = false;
                mText5.setText(formatNumber(mResList[4], false));   //Monto
                mText7.setText(Basic.setFormatter(GetDollar.mDollar.get(i))+" Bs");

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        int itemId = view.getId();

        if (itemId == R.id.switch1) {
            isConv = !isConv;

            if(GetDollar.getPrice(currSel1) <= 0){
                isConv = false;
                mSw1.setChecked(false);
                Basic.msg("Error obteniendo precio del DOLAR");
                return;
            }

            if(!isConv) {
                mText5.setText(formatNumber(mResList[4], false));   //Monto

                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mResList[4]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Monto en Bolivares");
            }

            if(mResList[4].isEmpty()){
                mSw1.setChecked(false);
                isConv = false;
                Basic.msg("El monto esta Vacio!.");
            }
            else {
                if(isConv) {
                    float value;
                    try {
                        value = Basic.notFormatter(mResList[4]);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    value *= GetDollar.getPrice(currSel1);
                    mText5.setText(Basic.setFormatter(value));   //Monto

                    ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + Basic.setFormatter(value));
                    clipboard.setPrimaryClip(clipData);
                    Basic.msg("Monto convertido a: "+ mSpinL1.get(currSel1));
                }
            }

        }

        //Telefono completo
        if (itemId == R.id.butt2) {
            if(mResList[0].isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Telf + Area copiado al portapapeles.");
            }
        }

        //Telefono Sin Area
        if (itemId == R.id.butt3) {
            if(mResList[1].isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[1]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Telf Sin Area copiado al portapapeles.");
            }
        }

        //Cedula
        if (itemId == R.id.butt4) {
            if(mResList[2].isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[2]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("ID copiado al portapapeles.");
            }
        }

        //Codigo Banco
        if (itemId == R.id.butt5) {
            if(mResList[3].isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[3]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Codg. Banco Copiado al portapapeles.");
            }
        }

        //Monto
        if (itemId == R.id.butt6) {
            if(mResList[4].isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[4]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Monto Copiado al portapapeles.");
            }
        }

        //Todos los datos
        if (itemId == R.id.butt7) {
            if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && mResList[4].isEmpty()){
                Basic.msg("Los campos estan VACIOS!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mResList[4]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Los datos se han copiado al portapapeles.");
            }
        }

        //Boton Pegar
        if (itemId == R.id.butt1){

            //Se limpian los valores residuales
            mResList[0] = "";
            mResList[1] = "";
            mResList[2] = "";
            mResList[3] = "";
            mResList[4] = "";
            mResList[5] = "";
            mDebug[0] = "";

            mText1.setText("");     // Telf + Area
            mText2.setText("");     // Telf
            mText3.setText("");     // Cedula
            mText4.setText("");     // Codig Banco
            mText5.setText("");     // Monto
            mText6.setText("");
            //--------------------------------------
            ClipData clip =  clipboard.getPrimaryClip();

            String text = "";

            if(clip != null && clip.getItemCount() > 0){
                text = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).getText().toString().toLowerCase();
            }
            else{
                Basic.msg("No se encontraron DATOS!");
                return;
            }

            DataExtracts.startinProcess(text);

            mText1.setText(formatPhone(mResList[0]));              //Telf + Area
            mText2.setText(mResList[1]);                           // Telf
            mText3.setText(formatNumber(mResList[2], true));    // Cedula
            mText4.setText(mResList[3]+" "+mResList[5]);           // Codig Banco
            mText5.setText(formatNumber(mResList[4], false));   //Monto
            mText6.setText(mDebug[0]);
            if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && mResList[4].isEmpty()){
                Basic.msg("No se encontraron DATOS!");
            }
            else {
                //Se apaga el sw
                isConv = false;
                mSw1.setChecked(false);

                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mResList[4]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Pegado y copiado al portapapeles.");

            }
        }
    }
    public String formatNumber(String str, boolean id) {
        if(str.isEmpty()){
            return "";
        }
        String type = "";

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("ES"));
        DecimalFormat formatter = (DecimalFormat) nf;

        if(id) {
            type += str.charAt(0);
            str = str.replaceAll("\\D", "");
            if(str.startsWith("0")){
                return type+str;
            }
            formatter.applyPattern("###,###.##");
        }
        else {
            formatter.applyPattern("###,##0.00");
        }
        str = str.replaceAll("\\.", "");
        str = str.replaceAll(",", ".");

        return  type+formatter.format(Float.parseFloat(str));
    }
    public String formatPhone(String str) {
        if(str.isEmpty()){
            return "";
        }
        return str.substring(0, 4)+"-"+str.substring(4);
    }
}

