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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    private final List<String> mTypeList = Arrays.asList("v","j","g","p","e","r","c" );

    private final List<String> mAreaList = Arrays.asList("0426", "0416", "0414", "0412", "0424");


    private final List<String> mBankList = Arrays.asList(
            "0102;BDV VENEZUELA DE_VENEZUELA;VNZ;Venezuela",
            "0156;100%;100%_BANCO;100%Banco",
            "0172;BANCAMIGA;_;Bancamiga",
            "0114;BANCARIBE;_;Bancaribe",
            "0171;ACTIVO;_;Bco. Activo",
            "0166;AGRICOLA;BA;BAgricola",
            "0175;BDT BICENTENARIO;_;BDT",
            "0128;CARONI;_;Bco. Caroni",
            "0163;DEL_TESORO TESORO;BT;BDTesoro",
            "0115;EXTERIOR;BE;BExterior",
            "0151;FONDO_COMUN;BFC;BFC",
            "0173;INTERNACIONAL DE_DESARROLLO DESARROLLO;BID;B.I.D",
            "0105;MERCANTIL;BM;Mercantil",
            "0191;BNC NACIONAL_DE_CREDITO;_;BNC",
            "0138;BANCO_PLAZA;PLAZA;Bco. Plaza",
            "0137;SOFITASA;_;Sofitasa",
            "0104;BVDC VENEZOLANO_DE_CREDITO VENEZOLANO;_;BVDC",
            "0168;BANCRECER;BC;Bancrecer",
            "0134;BANESCO;_;Banesco",
            "0177;BANFANB;_;Banfanb",
            "0146;BANGENTE;_;Bangente",
            "0174;BANPLUS;BAN+ BPLUS;BanPlus",
            "0108;BBVA PROVINCIAL;BP;Provincial",
            "0157;DELSUR;_;Bco. DelSur",
            "0169;MI_BANCO MIBANCO;R4;R4",
            "0178;N58;_;N58"
    );

    private final String[] mResList = {"","","","",""};


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

            //--------------------------------------
            ClipData clip =  clipboard.getPrimaryClip();

            String text = "";

            if(clip != null && clip.getItemCount() > 0){
                text = Objects.requireNonNull(clipboard.getPrimaryClip()).getItemAt(0).getText().toString().toLowerCase();
            }
            text = text.replaceAll("\\n\\s+", " ");

            text = processString(text);

            text = text.replaceAll("\\n", " ");
            text = text.replaceAll("\\s+", " ");

            String mSpl = " ";

            String[] txAll = text.split(mSpl);

            String[] txNum = text.replaceAll("([^0-9\\s])","").split(mSpl);

            List<String> txList = new ArrayList<>();
            int idx = validatePhoneNumber(txNum);
            if(idx >(-1)) {
                txList.add(txNum[idx]);
                //result += txNum[idx].replaceAll("(^0{2,})","0")+"\n";
                txNum[idx] = "";    //Clear the phone number
            }

            idx = validateID(txNum);
            if(idx >(-1)) {

                String mType = validateType(text);
                txList.add(txNum[idx]);

                mResList[2] = mType.toUpperCase()+txNum[idx].replaceAll("([^0-9])","");

                //result += mType.toUpperCase()+txNum[idx].replaceAll("([^0-9])","")+"\n";
                txNum[idx] = "";    //Clear the phone number
            }

            Object[] namRes = validateBankCode(txNum);
            idx = (int)namRes[0];
            String bcoName = "";
            if(idx >(-1)) {
                mResList[3] = (String)namRes[1];
                bcoName = (String)namRes[2];

                txList.add(txNum[idx]);
                //result += txNum[idx].replaceAll("([^0-9])","");
                txNum[idx] = "";    //Clear the phone number
            }
            else {
                String[] bankCode = validateBankName(txList, txAll);
                if(!bankCode[0].isEmpty()){
                    mResList[3] = bankCode[0];
                    bcoName = bankCode[1];
                }
            }
            if(mResList[4].isEmpty()){
                mResList[4] = validateMonto(txList, text);
            }

            mText1.setText(mResList[0]);
            mText2.setText(mResList[1]);
            mText3.setText(mResList[2]);
            mText4.setText(mResList[3]+" "+bcoName);
            mText5.setText(mResList[4]);

            if(mResList[0].isEmpty() && mResList[2].isEmpty() && mResList[3].isEmpty() && mResList[4].isEmpty()){
                Basic.msg("No se encontraron DATOS!");
            }
            else {
                ClipData clipData = ClipData.newPlainText("Clip Data", mResList[0] + "\n" + mResList[2] + "\n" + mResList[3]+ "\n" + mResList[4]);

                clipboard.setPrimaryClip(clipData);
                Basic.msg("Pegado y copiado al portapapeles.");
            }
        }
    }

    private String processString(String text){
        text = text.replaceAll("\\*","");
        text = text.replaceAll(":","");
        text = text.replaceAll("é","e");
        text = text.replaceAll("ó","o");
        text = text.replaceAll("í","i");
        text = text.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");
        text = text.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");

        mText6.setText(text);

        //Elimina posibles "o" en numeros -----------------------------------------------
        Pattern patt = Pattern.compile("(o[0-9]{3})");
        Matcher m = patt.matcher(text);

        if(m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            for (String newTx : mAreaList){
                String copyTx = newTx.replaceFirst("0","o");
                if(copyTx.equals(gr)) {
                    text = text.replaceFirst(gr, newTx);
                }
            }
        }
        //------------------------------------------------------------------------------

        //Elimina caracteres inutiles --------------------------------------------------
        patt = Pattern.compile("(\\w,\\s)");
        m = patt.matcher(text);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll(",", "");
            text = text.replaceFirst(gr, grCopy);
            m = patt.matcher(text);
        }
        patt = Pattern.compile("(\\w\\.\\s)");
        m = patt.matcher(text);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\.", "");
            text = text.replaceFirst(gr, grCopy);
            m = patt.matcher(text);
        }

        patt = Pattern.compile("([a-z]\\s-\\s[a-z])");
        m = patt.matcher(text);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll(" - ", " ");
            text = text.replaceFirst(gr, grCopy);
            m = patt.matcher(text);
        }

        patt = Pattern.compile("([a-z]\\.[a-z])");
        m = patt.matcher(text);
        if (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\.", "_");
            text = text.replaceFirst(gr, grCopy);
        }
        //--------------------------------------------------------------------------------

        patt = Pattern.compile("([a-z]{2,})(\\s)([a-z]{2,})");
        m = patt.matcher(text);

        while (m.find()) {
            String gr = m.group(1)+"_"+m.group(3);
            text = text.replaceFirst("([a-z]{2,})(\\s)([a-z]{2,})", gr);
            m = patt.matcher(text);
        }

        //Espacio entre numeros de telefono -----------------------------
        patt = Pattern.compile("([0-9]{4})(\\s)([0-9]{7}\\s)");
        m = patt.matcher(text);
        while (m.find()) {
            String gr = m.group(1)+m.group(3);
            text = text.replaceFirst("([0-9]{4})(\\s)([0-9]{7})", gr);
            m = patt.matcher(text);
        }
        //----------------------------------------------------------------

        patt = Pattern.compile("(\\s[0-9]{4})(\\s)([0-9]{7})");
        m = patt.matcher(text);
        while (m.find()) {
            String gr = m.group(1)+m.group(3);
            text = text.replaceFirst("([0-9]{4})(\\s)([0-9]{7})", gr);
            m = patt.matcher(text);
        }

        patt = Pattern.compile("(\\d{1,3}(\\s\\d{1,3}){2,3})");
        m = patt.matcher(text);
        if (m.find()) {
            String gr = m.group(1);
            assert gr != null;
            String grCopy = gr.replaceAll("\\s", "");
            text = text.replace(gr, grCopy);
        }
        return text;
    }

    private int validatePhoneNumber(String[] list) {
        for (int i = 0; i< list.length ; i++){
            String text = list[i];
            text = text.replaceAll("(^0{2,})", "0");
            //Basic.msg("-> "+text);

            if(ValidCodeArea(text)){
                return i;
            }
        }
        return -1;
    }

    private boolean ValidCodeArea(String value){
        for (String text : mAreaList){
            //Basic.msg("-> "+value);
            if(value.startsWith(text)) {
                //Basic.msg("-> "+text);
                String tlf = value.replaceFirst(text, "");
                if(tlf.length() == 7) {
                    mResList[0] = value;
                    mResList[1] = tlf;
                    return true;
                }
            }
        }
        return false;
    }

    private int validateID(String[] list) {
        for (int i = 0; i< list.length ; i++){
            String text = list[i];
            text = text.replaceAll("\\W", "");
            if(text.length() > 5 && text.length() < 10){
                return i;
            }
        }
        return -1;
    }

    private String validateType(String text){
        String mType = "v";
        Pattern p = Pattern.compile("(([^0-9a-z])([0-9,.]{6,12})|([a-z])([0-9,.]{6,12}))");
        Matcher m = p.matcher(text);
        if (m.find()) {
            String gr = m.group(1);
            assert gr != null;
            for(String mtype : mTypeList) {
                String newTx = (mtype+gr).replaceFirst("[a-z]{2,}", mtype);
                //Basic.msg("-> "+newTx);
                String pattern = "\\b"+newTx+"\\b";
                p = Pattern.compile(pattern);
                m = p.matcher(text);
                if(m.find()){
                    //Basic.msg("-> "+mtype+gr);
                    return mtype;
                }
            }
        }
        return mType;
    }

    private Object[] validateBankCode(String[] list) {
        for (int i = 0; i< list.length ; i++){
            String text = list[i];
            text = text.replaceAll("\\W", "");
            if(text.length() == 4 && text.startsWith("01")){
                for(String mCode : mBankList){
                    String code = mCode.split(";")[0];
                    if(code.equals(text)){
                        mResList[3] = code;
                        return new Object[]{i,code,mCode.split(";")[3]};
                    }
                }
            }
        }
        return new Object[]{-1,"",""};
    }

    private String[] validateBankName(List<String> numList, String[] list) {
        for (String s : list) {
            String text = s;
            for (String newTx : numList) {
                text = text.replace(newTx, "");
            }
            for (String newTx : mTypeList) {
                text = text.replaceAll("(^" + newTx + "$)", "");
            }
            if (text.isEmpty()) {
                continue;
            }

            for (String newTx : mBankList) {
                String[] strList = newTx.split(";");

                String pattern = "\\b" + text + "\\b";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(strList[1].toLowerCase());
                if (m.find()) {
                    return new String[]{strList[0], strList[3]};
                }
                for (String ttx : text.split("_")) {
                    pattern = "\\b" + ttx + "\\b";
                    p = Pattern.compile(pattern);
                    m = p.matcher(strList[1].toLowerCase());
                    if (m.find()) {
                        return new String[]{strList[0], strList[3]};
                    }
                }
            }
            for (String newTx : mBankList) {
                String[] strList = newTx.split(";");

                String pattern = "\\b" + text + "\\b";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(strList[2].toLowerCase());
                if (m.find()) {
                    return new String[]{strList[0], strList[3]};
                }
                for (String ttx : text.split("_")) {
                    pattern = "\\b" + ttx + "\\b";
                    p = Pattern.compile(pattern);
                    m = p.matcher(strList[2].toLowerCase());
                    if (m.find()) {
                        return new String[]{strList[0], strList[3]};
                    }
                }
            }
        }
        return new String[]{"",""};
    }

    private String validateMonto(List<String> numList, String rawTx) {
        String[] mony = {"monto_bs","bs","bolos","bsf","bolivares","monto"};
        rawTx = rawTx.replaceAll("([\\n\\s])", "_");
        for(String newTx:mony){
            rawTx = rawTx.replaceAll(newTx, "bs");
        }

        rawTx = rawTx.replaceAll("([^0-9,.bs_])", "");
        rawTx = rawTx.replaceAll("(\\Dbs)|(bs\\D)", "bs");

        for(String newTx : numList){
            rawTx = rawTx.replace(newTx, "");
        }

        Pattern patt = Pattern.compile("((^|_)\\d{1,3}(.\\d{3})*(,\\d+)?(_|$))");
        Matcher matc = patt.matcher(rawTx);
        if(matc.find()){
            String gr = matc.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            gr = gr.replaceAll("_", "");
            String grCopy = gr.replaceAll("\\D", "");

            for(String newTx : numList){
                if(newTx.equals(grCopy)){
                    rawTx = rawTx.replaceFirst(gr, "");
                }
            }
            //mResList[4] = gr.replaceAll("[\\s\\n]", "");
            //rawTx = rawTx.replaceFirst(gr, "");
        }

        patt = Pattern.compile("(bs_+[0-9]|[0-9]_+bs)");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);
            assert gr != null;
            String grCopy = gr.replaceAll("_", "");
            rawTx = rawTx.replace(gr, grCopy);
            //mText6.setText(gr);
        }

        patt = Pattern.compile("(bs[\\d,.]+(\\w|$))");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            return gr.replaceAll("[^\\d,.]", "");
        }

        patt = Pattern.compile("((^|\\w)[\\d,.]+bs)");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            return gr.replaceAll("[^\\d,.]", "");
        }

        rawTx = rawTx.replaceAll("[bs]","");
        for(String newTx : rawTx.split("_")){
            newTx = newTx.replaceAll("^[.,]$","");
            if(!newTx.isEmpty()){
                return newTx;
            }
        }
        return "";
    }
}