package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private Button mButt1;
    private Button mButt2;
    private Button mButt4;
    private Button mButt5;
    private Button mButt6;
    private Button mButt7;
    private Button mButt8;
    private Button mButt9;

    private ImageButton mBtnImg1;

    private TextView mText1;
    private TextView mText3;
    private TextView mText4;
    private TextView mText5; //Test only

    private TextView mDollView;

    private CurrencyEditText mInput1;
    private CurrencyEditText mInput2;

    private Spinner mSpin1;
    //private List<String> mSpinL1 = Arrays.asList("BCV", "Promedio", "Paralelo", "Valor Personalizado");
    private int currSel1 = 0;

    private Switch mSw1;
    private boolean isConv = false;
    private boolean isEsFormat = true;

    public String[] mResList;

    public Double glMonto = 0.0;
    public Double glTasa = 0.0;

    public String[] mDebug = GlobalData.dataDbg;
    private String mConver = "";

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private TextRecognizer recognizer;

    private Launcher mLaunch;

    private GlobalData glData = GlobalData.getInstance(AppContextProvider.getAppContext());

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize ML Kit Text Recognizer
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        mButt1 = findViewById(R.id.buttRe);
        mButt2 = findViewById(R.id.butt2);
        mButt4 = findViewById(R.id.butt4);
        mButt5 = findViewById(R.id.butt5);
        mButt6 = findViewById(R.id.butt6);
        mButt7 = findViewById(R.id.butt7);
        mButt8 = findViewById(R.id.butt8);
        mButt9 = findViewById(R.id.butt1);
        mBtnImg1 = findViewById(R.id.buttimag1);

        mText1 = findViewById(R.id.text1);
        mText3 = findViewById(R.id.text3);
        mText4 = findViewById(R.id.text4);
        mText5 = findViewById(R.id.text6);//Test only

        mDollView =  findViewById(R.id.dollarView);
        mInput1 = findViewById(R.id.input1);
        mInput2 = findViewById(R.id.input2);

        mSpin1 = findViewById(R.id.spin1);

        mSw1 = findViewById(R.id.switch1);

        mButt1.setOnClickListener(this);
        mButt2.setOnClickListener(this);
        mButt4.setOnClickListener(this);
        mButt5.setOnClickListener(this);
        mButt6.setOnClickListener(this);
        mButt7.setOnClickListener(this);
        mButt8.setOnClickListener(this);
        mButt9.setOnClickListener(this);
        mBtnImg1.setOnClickListener(this);
        //mBtnImg1.setOnLongClickListener(this);

        mButt2.setOnLongClickListener(this);
        mButt4.setOnLongClickListener(this);
        mButt5.setOnLongClickListener(this);
        mButt6.setOnLongClickListener(this);
        mButt8.setOnLongClickListener(this);

        mInput2.setOnClickListener(this);

        //Debug
        mText5.setMovementMethod(new ScrollingMovementMethod());

        new FilesManager(this);

        Msg.init(this);

        if (savedInstanceState != null) {
            glTasa = savedInstanceState.getDouble("tasa");
            glMonto = savedInstanceState.getDouble("monto");
            glData.setIsEsFormat(savedInstanceState.getBoolean("isEs"));
            glData.setIsConv( savedInstanceState.getBoolean("isConv"));
            mResList = savedInstanceState.getStringArray("mResList");
            glData.setDateList(mResList);
        }

        try {
            refresh(true);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        mLaunch = new Launcher(this.getActivityResultRegistry(), this.getApplicationContext(), new Launcher.OnCapture() {
            @Override
            public void invoke(List<Uri> uris) {
                try {
                    InputStream stream = getContentResolver().openInputStream(uris.get(0));
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    processImage(bitmap);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getLifecycle().addObserver(mLaunch);

        // Adjunta al botón para el picker
        mLaunch.attachToViewPicker(mBtnImg1, false, false);

        // Adjunta al botón para la camara
        mLaunch.attachToViewCam(mBtnImg1, true);

        //Para la lista de Monitores de dolar ------------------------------------------------------
        SelecAdapter adapt1 = new SelecAdapter(this, glData.getSpinTasa());
        mSpin1.setAdapter(adapt1);
        //int currSele = glData.getOptTasa();
        //mSpin1.setSelection(currSele); //Set La Moneda como
        mSpin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == GetDollar.mDollar.size()-1){
                    GetDollar.mDollar.set(i, GetDollar.mDollar.get(glData.getOptTasa()));
                    double mDollar = GetDollar.mDollar.get(i);
                    if(mDollar > 0){
                        mInput1.setText(Basic.setFormatAlternate(mDollar, isEsFormat));
                    }
                    else {
                        mInput1.setText(Basic.setFormatAlternate(glTasa, isEsFormat));

                    }

                    mInput1.setVisibility(View.VISIBLE);
                    mDollView.setVisibility(View.INVISIBLE);

                }
                else {
                    mDollView.setText(Basic.setFormatAlternate(GetDollar.mDollar.get(i), isEsFormat)+" Bs");

                    mInput1.setVisibility(View.INVISIBLE);
                    mDollView.setVisibility(View.VISIBLE);
                }
                glData.setOptTasa(i);
                mSw1.setChecked(false);
                glData.setIsConv(false);
                mInput2.setText(Basic.setFormatAlternate(glMonto, isEsFormat));   //Monto

                View spinnerSel = mSpin1.getSelectedView();
                if(spinnerSel != null) {
                    TextView mView = spinnerSel.findViewWithTag(i);
                    mView.setText(glData.getSpinTasa().get(i));
                    //Msg.m(""+mSpin1.getSelectedView().findViewWithTag(i));
                }
                currSel1 = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSw1.setOnCheckedChangeListener((buttonView, isChecked) -> {

            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

            mInput2.setCurrencySymbol("");

            isConv = glData.getIsConv();

            isConv = !isConv;
            if (GetDollar.getPrice(glData.getOptTasa()) > 0) {
                if (!isConv) {
                    mInput2.setText(Basic.setFormatAlternate(glMonto, isEsFormat));   //Monto

                    ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3] + "\n" + glMonto);
                    clipboard.setPrimaryClip(clipData);
                    Msg.m("Monto en Bolivares");
                }
                else {
                    mInput2.clearFocus();

                    if (glMonto < 1) {
                        mSw1.setChecked(false);
                        isConv = false;
                        Msg.m("El monto esta Vacio!.");
                    } else {

                        mInput2.setCurrencySymbol("Bs");

                        double value = glMonto;
                        //                    try {
                        //                        value = Basic.notFormatter(glMonto);
                        //                    } catch (ParseException e) {
                        //                        throw new RuntimeException(e);
                        //                    }
                        value *= GetDollar.getPrice(glData.getOptTasa());
                        mConver = Basic.setFormatAlternate(value, isEsFormat);
                        mInput2.setText(Basic.setFormatAlternate(value, isEsFormat));   //Monto

                        ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3] + "\n" + mConver);
                        clipboard.setPrimaryClip(clipData);
                        Msg.m("Monto convertido a: " + glData.getSpinTasa().get(glData.getOptTasa()));
                    }
                }
            }
        else {
            isConv = false;
            mSw1.setChecked(false);
            Msg.m("Error obteniendo precio del DOLAR");
        }

        glData.setIsConv(isConv);
        });


        mInput1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                glTasa = mInput1.getNumericValue();
                GetDollar.mDollar.set(GetDollar.mDollar.size()-1, mInput1.getNumericValue());
            }
        });

        mInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if(mInput2.hasFocus()) {
                    glData.setDateList(4, getInputValue());
                    glMonto = mInput2.getNumericValue();
                }
            }
        });

        mInput2.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(isConv) {
                    isConv = false;
                    mSw1.setChecked(false);
                    mInput2.setCurrencySymbol("");
                    mInput2.setText(Basic.setFormatAlternate(mInput2.getNumericValue(), glData.getIsEsFormat()));
                }
                return false;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Guardamos Todos los Valores
        outState.putDouble("tasa", glTasa);
        outState.putDouble("monto", glMonto);
        outState.putBoolean("isEs", isEsFormat);
        outState.putBoolean("isConv", glData.getIsConv());
        outState.putStringArray("mResList", glData.getDateList());
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            refresh(false);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognizer.close();
    }

    @SuppressLint("SetTextI18n")
    private void refresh(boolean create) throws ParseException {

        mResList = glData.getDateList();
        glMonto = detectNumberFormat(mResList[4]);
        isEsFormat = glData.getIsEsFormat();
        isConv = glData.getIsConv();

        // Input Tasa
       // mInput1.setText(Basic.setFormatAlternate(glTasa, isEsFormat));

        if(create) {
            GetDollar mGet = new GetDollar(AppContextProvider.getAppContext(), MainActivity.this, mSpin1, mInput1, glTasa, isConv);
            try {
                GetDollar.urlRun();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Selector de Covertor

       //mSw1.setChecked(isConv);

        mText1.setText(formatPhone(mResList[0]));              //Telf + Area
        mText3.setText(formatNumber(mResList[2], true));    // Cedula
        mText4.setText(mResList[3]+(mResList[3].isEmpty()?"":" ")+mResList[5]);           // Codig Banco


        Double value1 = mInput1.getNumericValue();
        Double value2 = mInput2.getNumericValue();
        // 2. Cambiar el icono dinámicamente
        if (isEsFormat) {

            mInput1.setLocale("ES");
            mInput2.setLocale("ES");

            mInput1.setText(Basic.setFormatterEs(value1));
            mInput2.setText(Basic.setFormatterEs(value2));

        } else {
            mInput1.setLocale("EN");
            mInput2.setLocale("EN");

            mInput1.setText(Basic.setFormatterEn(value1));
            mInput2.setText(Basic.setFormatterEn(value2));

        }

        invalidateOptionsMenu();

        if(glData.getSendValue() > 0){
            glMonto = glData.getSendValue();
            mInput2.setText(Basic.setFormatAlternate(glMonto, isEsFormat));

            isConv = false;
            mSw1.setChecked(false);

            glData.setSendValue(0.0);
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.format, menu);
        getMenuInflater().inflate(R.menu.calc, menu);
        getMenuInflater().inflate(R.menu.history, menu);

        for(int i = 0; i < menu.size(); i++){
            MenuItem item = menu.getItem(i);
            SpannableString spannabl = new SpannableString(item.getTitle().toString());
            spannabl.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.black)),0 ,spannabl.length(),0);
            item.setTitle(spannabl);

            if(item.getItemId() == R.id.format){
                if (isEsFormat) {
                    item.setIcon(R.drawable.es_format);
                } else {
                    item.setIcon(R.drawable.en_format);
                }
            }
        }
        //test.setBackgroundColor(ContextCompat.getColor(test.getContext(), R.color.purple_500));
        return true;
    }

    @SuppressLint({"SetWorldReadable", "SetTextI18n"})
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item){
        int itemId = item.getItemId();

        if (itemId == R.id.calc) {
            Intent mIntent = new Intent(this, CalcActivity.class);
            startActivity(mIntent);
        }
        if (itemId == R.id.history) {
            //Intent mIntent = new Intent(this, CalcActivity.class);
           //startActivity(mIntent);
            Msg.m("No implementado!");
        }

        if (itemId == R.id.format) {
            //Intent mIntent = new Intent(this, CalcActivity.class);
            //startActivity(mIntent);

            // 1. Alternar el estado
            isEsFormat = !isEsFormat;
            glData.setIsEsFormat(isEsFormat);

            Double value1 = mInput1.getNumericValue();
            Double value2 = mInput2.getNumericValue();

            // 2. Cambiar el icono dinámicamente
            if (isEsFormat) {
                item.setIcon(R.drawable.es_format);
                mInput1.setLocale("ES");
                mInput2.setLocale("ES");

                mInput1.setText(Basic.setFormatterEs(value1));
                mInput2.setText(Basic.setFormatterEs(value2));
                mDollView.setText(Basic.setFormatterEs(GetDollar.mDollar.get(currSel1)) +" Bs");

                Msg.m("Formato Numerico ES");

            } else {
                item.setIcon(R.drawable.en_format);
                mInput1.setLocale("EN");
                mInput2.setLocale("EN");

                mInput1.setText(Basic.setFormatterEn(value1));
                mInput2.setText(Basic.setFormatterEn(value2));
                mDollView.setText(Basic.setFormatterEn(GetDollar.mDollar.get(currSel1)) +" Bs");

                Msg.m("Formato Numerico EN");
            }
        }

        return true;
    }

    private void processImage(Bitmap bitmap) {
        if(bitmap == null){
            return;
        }

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        Task<Text> mTask = recognizer.process(image);
        mTask.addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                //mText6.setText(text.getText().toLowerCase());
                actionTrigger(clipboard, text.getText().toLowerCase());
            }
        });

        mTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mText5.setText(e.getMessage());
            }
        });
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View view) {

        int itemId = view.getId();

        //Quita al focus a los inputs ------------
        //Find the currently focused view
        if(itemId != mInput1.getId()){
            mInput1.clearFocus();
        }
        if(itemId != mInput2.getId()){
            mInput2.clearFocus();
        }
        //----------------------------------------

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //Test
        //DataExtracts.glMonto = getInputValue();

        //Boton que recarga el precio dolar
        if (itemId == R.id.buttRe) {
            GetDollar mGet = new GetDollar(getApplicationContext(), MainActivity.this, mSpin1 , mDollView, glTasa, false);
            try {
                GetDollar.urlRun();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (itemId == R.id.input2) {
            if(isConv) {
                isConv = false;
                mSw1.setChecked(false);
            }
        }

        //Telefono completo
        if (itemId == R.id.butt2) {
            if(mResList[0].isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0]);
                clipboard.setPrimaryClip(clipData);
                Msg.m("Telf + Area copiado al portapapeles.");
            }
        }

        //Cedula
        if (itemId == R.id.butt4) {
            if(mResList[2].isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[2]);
                clipboard.setPrimaryClip(clipData);
                Msg.m("ID copiado al portapapeles.");
            }
        }

        //Codigo Banco
        if (itemId == R.id.butt5) {
            if(mResList[3].isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[3]);
                clipboard.setPrimaryClip(clipData);
                Msg.m("Codg. Banco Copiado al portapapeles.");
            }
        }

        //Monto
        if (itemId == R.id.butt6) {
            if(getInputValue().isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", (isConv? mConver : getInputValue()));
                clipboard.setPrimaryClip(clipData);
                Msg.m("Monto Copiado al portapapeles.");
            }
        }

        //Todos los datos
        if (itemId == R.id.butt7) {
            if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && (isConv? mConver : getInputValue()).isEmpty()){
                Msg.m("Los campos estan VACIOS!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + (isConv? mConver : getInputValue()));
                clipboard.setPrimaryClip(clipData);
                Msg.m("Los datos se han copiado al portapapeles.");
            }
        }

        //Debug
        if (itemId == R.id.butt8) {
            ClipData clipData = ClipData.newPlainText("Clip Data", mText5.getText());
            clipboard.setPrimaryClip(clipData);
            Msg.m("Debug Copiado al portapapeles.");
        }

        //Boton Pegar
        if (itemId == R.id.butt1){
            ClipData clip =  clipboard.getPrimaryClip();
            String text = "";
            if(clip != null && clip.getItemCount() > 0){
                text = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).getText().toString().toLowerCase();
            }
            else{
                Msg.m("No se encontraron DATOS!");
                return;
            }
            actionTrigger(clipboard, text);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        int itemId = view.getId();

        //Quita al focus a los inputs ------------
        //Find the currently focused view
        if(itemId != mInput1.getId()){
            mInput1.clearFocus();
        }
        if(itemId != mInput2.getId()){
            mInput2.clearFocus();
        }
        //----------------------------------------

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //Telefono Sin Area
        if (itemId == R.id.butt2) {
            if(mResList[1].isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[1]);
                clipboard.setPrimaryClip(clipData);
                Msg.m("Telf Sin Area copiado al portapapeles.");
            }
        }

        //Cedula sin Indicador de Tipo
        if (itemId == R.id.butt4) {
            if(mResList[2].isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[2].replaceAll("\\D",""));
                clipboard.setPrimaryClip(clipData);
                Msg.m("ID sin TIPO copiado al portapapeles.");
            }
        }

        //Codigo Banco
        if (itemId == R.id.butt5) {
            if(mResList[3].isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", (mResList[3]+" "+mResList[5]));
                clipboard.setPrimaryClip(clipData);
                Msg.m("Nombre y Codg. Banco Copiado al portapapeles.");
            }
        }

        //Monto
        if (itemId == R.id.butt6) {
            if(getInputValue().isEmpty()){
                Msg.m("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", (isConv? (mConver+mInput2.getCurrencySymbol()): getInputValue()));
                clipboard.setPrimaryClip(clipData);
                Msg.m("Monto y Simbolo Copiado al portapapeles.");
            }
        }

        //Debug
        if (itemId == R.id.butt8) {
            ClipData clipData = ClipData.newPlainText("Clip Data", mText5.getText());
            clipboard.setPrimaryClip(clipData);
            Msg.m("Debug Copiado al portapapeles.");
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    public void actionTrigger(ClipboardManager clipboard, String text){
        //Se limpian los valores residuales
        mResList[0] = "";
        mResList[1] = "";
        mResList[2] = "";
        mResList[3] = "";
        mResList[4] = "";
        mResList[5] = "";
        mDebug[0] = "";

        mText1.setText("");     // Telf + Area
        mText3.setText("");     // Cedula
        mText4.setText("");     // Codig Banco
        mInput2.setText("");     // Monto
        mText5.setText("");

        glMonto = 0.0;
        //--------------------------------------

        DataExtracts.startinProcess(text);

        try {
            glMonto = detectNumberFormat(mResList[4]);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        mText1.setText(formatPhone(mResList[0]));              //Telf + Area
        mText3.setText(formatNumber(mResList[2], true));    // Cedula
        mText4.setText(mResList[3]+" "+mResList[5]);           // Codig Banco
        mInput2.setText(Basic.setFormatAlternate(glMonto, isEsFormat));   //Monto
        mText5.setText( mDebug[0]);                              // Debug
        if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && glMonto < 1){
            Msg.m("No se encontraron DATOS!");
        }
        else {
            //Se apaga el sw
            isConv = false;
            mSw1.setChecked(false);

            if(mText5.getVisibility() == View.INVISIBLE) {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + glMonto);
                clipboard.setPrimaryClip(clipData);
                Msg.m("Pegado y copiado al portapapeles.");
            }
        }
    }
    public String formatNumber(String str, boolean id) {
        if(str.isEmpty()){
            return id ? "" : "0,00";
        }
        String type = "";

        NumberFormat nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("ES"));
        DecimalFormat formatter = (DecimalFormat) nf;

        if(id) {
            type += str.charAt(0);
            str = str.replaceAll("\\D", "");

            int mSiz = str.length();
            List<String> mResult = new ArrayList<>();
            String mResEnd = "";
            for(int a = mSiz, b = mSiz-3 ; b >= 0 ; a-=3, b-=3){
                mResult.add(str.substring(b,a));
                mResEnd = str.substring(0,b);
            }

            Collections.reverse(mResult);
            StringBuilder complete = new StringBuilder();
            for(String s : mResult){
                complete.append(".").append(s);
            }
            return type+"-"+(mResEnd+complete).replaceAll("^\\D+","");
        }
        else {
            formatter.applyPattern("###,##0.00");
        }
        str = str.replaceAll("\\.", "");
        str = str.replaceAll(",", ".");

        return formatter.format(Float.parseFloat(str));
    }

    public static Double detectNumberFormat(String text) throws ParseException {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        String clean = text.trim();

        // Permite números sin separadores, con separadores de miles, y decimales
        // Patrón más flexible

        // 1. Formato ES: punto = miles, coma = decimal  (1.234,56 o 1234,56 o 1234)
        if (Pattern.matches("^[+-]?[0-9]+(?:\\.[0-9]{3})*(?:,[0-9]+)?$", clean)) {
            return Basic.getDouble(clean, true);   // true = ES
        }

        // 2. Formato EN: coma = miles, punto = decimal  (1,234.56 o 1234.56 o 1234)
        if (Pattern.matches("^[+-]?[0-9]+(?:,[0-9]{3})*(?:\\.[0-9]+)?$", clean)) {
            return Basic.getDouble(clean, false);  // false = EN
        }

        // 3. Último intento: número simple sin separadores (9450, 123.45, etc)
        if (Pattern.matches("^[+-]?[0-9]+(?:[.,][0-9]+)?$", clean)) {
            // Intentamos detectar si usa punto o coma como decimal
            if (clean.contains(",")) {
                return Basic.getDouble(clean, true);   // probablemente formato español
            } else {
                return Basic.getDouble(clean, false);  // formato inglés o sin decimal
            }
        }

        return 0.0;
    }


    public String formatPhone(String str) {
        if(str.isEmpty()){
            return "";
        }
        return str.substring(0, 4)+"-"+str.substring(4);
    }

    private String getInputValue(){
        if(isConv){
            return Basic.setFormatAlternate(glMonto, isEsFormat);
        }
        else {
            return Basic.setFormatAlternate(mInput2.getNumericValue(), isEsFormat);
        }
    }
}

