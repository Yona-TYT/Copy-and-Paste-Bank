package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
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

    String[] mResList = DataExtracts.mResList;

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

        mButt1.setOnClickListener(this);
        mButt2.setOnClickListener(this);
        mButt3.setOnClickListener(this);
        mButt4.setOnClickListener(this);
        mButt5.setOnClickListener(this);
        mButt6.setOnClickListener(this);
        mButt7.setOnClickListener(this);

        new Basic(this);

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

        if (itemId == R.id.butt1){

            //Se limpian los valores residuales
            mResList[0] = "";
            mResList[1] = "";
            mResList[2] = "";
            mResList[3] = "";
            mResList[4] = "";
            mResList[5] = "";
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

            mText1.setText(mResList[0]);
            mText2.setText(mResList[1]);
            mText3.setText(mResList[2]);
            mText4.setText(mResList[3]+" "+mResList[5]);
            mText5.setText(mResList[4]);

            if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && mResList[4].isEmpty()){
                Basic.msg("No se encontraron DATOS!");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mResList[4]);

                //clipboard.setPrimaryClip(clipData);
                //Basic.msg("Pegado y copiado al portapapeles.");
            }
        }
    }
}