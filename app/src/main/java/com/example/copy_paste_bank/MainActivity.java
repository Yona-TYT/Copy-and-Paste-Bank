package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.example.copy_paste_bank.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private static final int STORAGE_PERMISSION_CODE = 23;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private boolean mPermiss = false;
    private boolean mCamPermiss = false;

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
    private List<String> mSpinL1 = Arrays.asList("BCV", "Promedio", "Paralelo", "Valor Personalizado");
    private int currSel1 = 0;

    private Switch mSw1;
    private boolean isConv = false;

    public String[] mResList = DataExtracts.mResList;
    public String[] mDebug = DataExtracts.mDebug;
    private String mConver = "";

    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> selectPictureLauncher;
    private Uri photoUri;
    private TextRecognizer recognizer;

    private Launcher mLaunch;

    private ActivityMainBinding binding;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize ML Kit Text Recognizer
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        mButt1 = findViewById(R.id.butt1);
        mButt2 = findViewById(R.id.butt2);
        mButt4 = findViewById(R.id.butt4);
        mButt5 = findViewById(R.id.butt5);
        mButt6 = findViewById(R.id.butt6);
        mButt7 = findViewById(R.id.butt7);
        mButt8 = findViewById(R.id.butt8);
        mButt9 = findViewById(R.id.buttRe);
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
        mButt8.setOnLongClickListener(this);

        mInput2.setOnClickListener(this);
        mSw1.setOnClickListener(this);

        new Basic(this);
        new FilesManager(this);

        mSw1.setChecked(false);

        GetDollar mGet = new GetDollar(getApplicationContext(), MainActivity.this, mSpin1 , mInput1);
        try {
            GetDollar.urlRun();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mLaunch = new Launcher(this.getActivityResultRegistry(), this.getApplicationContext(), new Launcher.OnCapture() {
            @Override
            public void invoke(Uri uri) {
                try {
                    InputStream stream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    processImage(bitmap);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Launcher clazz = mLaunch;
        getLifecycle().addObserver(clazz);

        mBtnImg1.setOnClickListener(v -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mLaunch.launchPicker();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        mBtnImg1.setOnLongClickListener(v -> {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mLaunch.launchCamera();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        });

        //Para la lista de Monitores de dolar ------------------------------------------------------
        SelecAdapter adapt1 = new SelecAdapter(this, mSpinL1);
        mSpin1.setAdapter(adapt1);
        //mSpin1.setSelection(currSel1); //Set La Moneda como default
        mSpin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == GetDollar.mDollar.size()-1){
                    GetDollar.mDollar.set(i, GetDollar.mDollar.get(currSel1));
                    mInput1.setText(Basic.setFormatter(GetDollar.mDollar.get(i).toString()));

                    mInput1.setVisibility(View.VISIBLE);
                    mDollView.setVisibility(View.INVISIBLE);

                }
                else {
                    mDollView.setText(Basic.setFormatter(GetDollar.mDollar.get(i).toString())+" Bs");

                    mInput1.setVisibility(View.INVISIBLE);
                    mDollView.setVisibility(View.VISIBLE);
                }
                currSel1 = i;
                mSw1.setChecked(false);
                isConv = false;
                mInput2.setText(formatNumber(mResList[4], false));   //Monto

                View spinnerSel = mSpin1.getSelectedView();
                if(spinnerSel != null) {
                    TextView mView = spinnerSel.findViewWithTag(i);
                    mView.setText(mSpinL1.get(i));
                    //Basic.msg(""+mSpin1.getSelectedView().findViewWithTag(i));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
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
                GetDollar.mDollar.set(GetDollar.mDollar.size()-1, (float)mInput1.getNumericValue());
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
                    DataExtracts.mResList[4] = getInputValue();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognizer.close();
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
        //DataExtracts.mResList[4] = getInputValue();

        if (itemId == R.id.input2) {
            if(isConv) {
                isConv = false;
                mSw1.setChecked(false);
            }
        }

        if (itemId == R.id.switch1) {
            isConv = !isConv;
            if(GetDollar.getPrice(currSel1) <= 0){
                isConv = false;
                mSw1.setChecked(false);
                Basic.msg("Error obteniendo precio del DOLAR");
                return;
            }
            if(!isConv) {
                mInput2.setText(formatNumber(mResList[4], false));   //Monto

                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mResList[4]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Monto en Bolivares");
            }
            else {

                mInput2.clearFocus();

                if(mResList[4].isEmpty()){
                    mSw1.setChecked(false);
                    isConv = false;
                    Basic.msg("El monto esta Vacio!.");
                }
                else {
                    float value;
                    try {
                        value = Basic.notFormatter(mResList[4]);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    value *= GetDollar.getPrice(currSel1);
                    mConver = Basic.setFormatter(Float.toString(value));
                    mInput2.setText(mConver);   //Monto

                    ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mConver);
                    clipboard.setPrimaryClip(clipData);
                    Basic.msg("Monto convertido a: "+ mSpinL1.get(currSel1));
                }
            }
        }

        //Boton que recarga el precio dolar
        if (itemId == R.id.buttRe) {
            GetDollar mGet = new GetDollar(getApplicationContext(), MainActivity.this, mSpin1 , mDollView);
            try {
                GetDollar.urlRun();
            } catch (IOException e) {
                throw new RuntimeException(e);
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
            if(getInputValue().isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", (isConv? mConver : getInputValue()));
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Monto Copiado al portapapeles.");
            }
        }

        //Todos los datos
        if (itemId == R.id.butt7) {
            if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && (isConv? mConver : getInputValue()).isEmpty()){
                Basic.msg("Los campos estan VACIOS!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + (isConv? mConver : getInputValue()));
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Los datos se han copiado al portapapeles.");
            }
        }

        //Debug
        if (itemId == R.id.butt8) {
            ClipData clipData = ClipData.newPlainText("Clip Data", mText5.getText());
            clipboard.setPrimaryClip(clipData);
            Basic.msg("Debug Copiado al portapapeles.");
        }

        //Boton Pegar
        if (itemId == R.id.butt1){
            ClipData clip =  clipboard.getPrimaryClip();
            String text = "";
            if(clip != null && clip.getItemCount() > 0){
                text = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).getText().toString().toLowerCase();
            }
            else{
                Basic.msg("No se encontraron DATOS!");
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
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[1]);
                clipboard.setPrimaryClip(clipData);
                Basic.msg("Telf Sin Area copiado al portapapeles.");
            }
        }

        //Cedula sin Indicador de Tipo
        if (itemId == R.id.butt4) {
            if(mResList[2].isEmpty()){
                Basic.msg("Este Campo esta VACIO!:");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[2].replaceAll("\\D",""));
                clipboard.setPrimaryClip(clipData);
                Basic.msg("ID sin TIPO copiado al portapapeles.");
            }
        }

//        //Codigo Banco
//        if (itemId == R.id.butt5) {
//            if(mResList[3].isEmpty()){
//                Basic.msg("Este Campo esta VACIO!:");
//            }
//            else {
//                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[3]);
//                clipboard.setPrimaryClip(clipData);
//                Basic.msg("Codg. Banco Copiado al portapapeles.");
//            }
//        }

//        //Monto
//        if (itemId == R.id.butt6) {
//            if(getInputValue().isEmpty()){
//                Basic.msg("Este Campo esta VACIO!:");
//            }
//            else {
//                ClipData clipData = ClipData.newPlainText("Clip Data", (isConv? mConver : getInputValue()));
//                clipboard.setPrimaryClip(clipData);
//                Basic.msg("Monto Copiado al portapapeles.");
//            }
//        }

        //Debug
        if (itemId == R.id.butt8) {
            ClipData clipData = ClipData.newPlainText("Clip Data", mText5.getText());
            clipboard.setPrimaryClip(clipData);
            Basic.msg("Debug Copiado al portapapeles.");
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
        //--------------------------------------

        DataExtracts.startinProcess(text);

        mText1.setText(formatPhone(mResList[0]));              //Telf + Area
        mText3.setText(formatNumber(mResList[2], true));    // Cedula
        mText4.setText(mResList[3]+" "+mResList[5]);           // Codig Banco
        mInput2.setText(formatNumber(mResList[4], false));   //Monto
        mText5.setText( mDebug[0]);                              // Debug
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
    public String formatPhone(String str) {
        if(str.isEmpty()){
            return "";
        }
        return str.substring(0, 4)+"-"+str.substring(4);
    }

    private String getInputValue(){
        if(isConv){
            return mResList[4];
        }
        else {
            return Basic.setFormatter(Double.toString(mInput2.getNumericValue()));
        }
    }
}

