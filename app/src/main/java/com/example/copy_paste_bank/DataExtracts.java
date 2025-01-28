package com.example.copy_paste_bank;

import android.content.ClipData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataExtracts {
    private static final List<String> mTypeList = Arrays.asList("v","j","g","p","e","r","c" );
    private static final List<String> mAreaList = Arrays.asList("0426", "0416", "0414", "0412", "0424");
    private static final List<String> mBankList = Arrays.asList(
            "0102;BDV VENEZUELA DE_VENEZUELA;VNZ;Venezuela",
            "0156;100%;100%_BANCO;100%Banco",
            "0172;BANCAMIGA;_;Bancamiga",
            "0114;BANCARIBE;_;Bancaribe",
            "0171;ACTIVO;_;Bco. Activo",
            "0166;AGRICOLA;BA BAV;BAgricola",
            "0175;BDT BICENTENARIO;TRABAJADORES;BDT",
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
            "0177;BANFANB BANFAN;FUERZA ARMADA;Banfanb",
            "0146;BANGENTE;_;Bangente",
            "0174;BANPLUS;BAN+ BPLUS;BanPlus",
            "0108;BBVA PROVINCIAL;BP;Provincial",
            "0157;DELSUR;_;Bco. DelSur",
            "0169;MI_BANCO MIBANCO;R4;R4",
            "0178;N58;_;N58"
    );

    /*
    0 = Numero de Telefono + Codg. Area
    1 = Numero de Telefono Sin Codig. Area
    2 = Cedula de Identidad
    3 = Codg. Banco
    4 = Monto con . y ,
    5 = Nombre Banco
    6 = Monto solo con ,
    */
    public static String[] mResList = {"","","","","","",""};
    public static String[] mDebug = {""};

    public DataExtracts(){
    }

    public static void startinProcess(String clipText){
        //--------------------------------------

        clipText = processString(clipText);

        String mSpl = " ";
        String[] txAll = clipText.split(mSpl);
        String[] txNum = clipText.replaceAll("([^0-9\\s])","").split(mSpl);

        List<String> txList = new ArrayList<>();
        int idx = validatePhoneNumber(txNum);
        if(idx >(-1)) {
            txList.add(txNum[idx]);
            txNum[idx] = "";    //Clear the phone number
        }

        idx = validateID(txNum);
        if(idx >(-1)) {
            String mType = validateType(clipText);
            txList.add(txNum[idx]);
            mResList[2] = mType.toUpperCase()+txNum[idx].replaceAll("([^0-9])","");
            txNum[idx] = "";    //Clear the cedula number
        }

        Object[] namRes = validateBankCode(txNum);
        idx = (int)namRes[0];
        if(idx >(-1)) {
            mResList[3] = (String)namRes[1];
            mResList[5] = (String)namRes[2];

            txList.add(txNum[idx]);
            txNum[idx] = "";    //Clear the bank number
        }
        else {
            String[] bankCode = validateBankName(txList, txAll);
            if(!bankCode[0].isEmpty()){
                mResList[3] = bankCode[0];
                mResList[5] = bankCode[1];
            }
        }
        if(mResList[4].isEmpty()){
            mResList[4] = validateMonto(txList, clipText);
            mResList[6] = mResList[4].replaceAll("\\.","");
        }
    }

    private static String processString(String rawTx){
        //text = text.replaceAll("\\*","");
        rawTx = rawTx.replaceAll(":","");
        rawTx = rawTx.replaceAll("é","e");
        rawTx = rawTx.replaceAll("ó","o");
        rawTx = rawTx.replaceAll("í","i");

        rawTx = rawTx.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");
        rawTx = rawTx.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");

        Pattern patt = Pattern.compile("(\\n)");
        Matcher m = patt.matcher(rawTx);

        if(m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String copyTx = gr.replaceAll("\\n"," |_");
            rawTx = rawTx.replaceAll(gr, copyTx);

        }

        rawTx = rawTx.replaceAll("(\\s+\\n|\\n\\s+)", " ");
        rawTx = rawTx.replaceAll("\\n", " ");
        rawTx = rawTx.replaceAll("\\s+", " ");

        //Elimina posibles "o" en numeros -----------------------------------------------
        patt = Pattern.compile("(o[0-9]{3})");
        m = patt.matcher(rawTx);

        if(m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            for (String newTx : mAreaList){
                String copyTx = newTx.replaceFirst("0","o");
                if(copyTx.equals(gr)) {
                    rawTx = rawTx.replaceFirst(gr, newTx);
                }
            }
        }
        //------------------------------------------------------------------------------



        //Elimina caracteres inutiles --------------------------------------------------
        patt = Pattern.compile("(\\w,\\s)");
        m = patt.matcher(rawTx);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll(",", "");
            rawTx = rawTx.replaceFirst(gr, grCopy);
            m = patt.matcher(rawTx);
        }
        patt = Pattern.compile("(\\w\\.\\s)");
        m = patt.matcher(rawTx);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\.", "");
            rawTx = rawTx.replaceFirst(gr, grCopy);
            m = patt.matcher(rawTx);
        }

        patt = Pattern.compile("([a-z]\\s-\\s[a-z])");
        m = patt.matcher(rawTx);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll(" - ", " ");
            rawTx = rawTx.replaceFirst(gr, grCopy);
            m = patt.matcher(rawTx);
        }

        patt = Pattern.compile("([a-z][.,=][a-z])");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("[.,=]", "_");
            rawTx = rawTx.replaceAll(gr, grCopy);
        }

        patt = Pattern.compile("(([a-z][.,=][0-9])|([0-9][.,=][a-z]))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("[.,=]", "_");
            rawTx = rawTx.replaceAll(gr, grCopy);
        }
        //--------------------------------------------------------------------------------

        patt = Pattern.compile("([a-z]{2,})(\\s)([a-z]{2,})");
        m = patt.matcher(rawTx);
        while (m.find()) {
            String gr = m.group(1)+"_"+m.group(3);
            rawTx = rawTx.replaceFirst("([a-z]{2,})(\\s)([a-z]{2,})", gr);
            m = patt.matcher(rawTx);
        }

        //Espacio entre numeros de telefono -----------------------------
        patt = Pattern.compile("((^|\\s)([0-9]{4})(\\s)([0-9]{7}(\\s|$)))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            assert gr != null;
            //Basic.msg("-> "+gr);
            //String //grCopy = gr.replaceAll("(^\\s|$\\s)", " ");
            String grCopy = gr.replaceAll("\\s+", "");
            for (String newTx : mAreaList) {
                //Basic.msg("-> "+grCopy);
                if (grCopy.startsWith(newTx)) {
                    //Basic.msg("-> "+grCopy);
                    rawTx = rawTx.replaceAll(gr, " "+grCopy+" ") ;
                    break;
                }
            }
        }
        //----------------------------------------------------------------

        //Espacio entre montos ---------------------------------------
        patt = Pattern.compile("((^|\\s|[a-z])(\\d{1,3}(([^\\n\\w])\\d{3}){1,3})(\\s|$))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\s", "");
            rawTx = rawTx.replace(gr, " "+grCopy+" ");
        }
        //--------------------------------------------------------------

        //Espacio entre cifras ---------------------------------------
        patt = Pattern.compile("((^|\\s|[a-z])(\\d{1,3}(([^\\n\\w])\\d{3}){1,3})(\\s|$))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\s", "");
            rawTx = rawTx.replace(gr, " "+grCopy+" ");
        }
        //----------------------------------------------------------------

        for(String mtype : mTypeList) {
            //Basic.msg("-> "+newTx);
            patt = Pattern.compile("([^a-z]"+mtype+"[^a-z])");
            m = patt.matcher(rawTx);
            if(m.find()){
                String gr = m.group(1);
                //Basic.msg("-> "+gr);
                assert gr != null;
                String grCopy = gr.replaceAll(mtype, " "+mtype);
                rawTx = rawTx.replaceAll(gr, grCopy);
                break;
            }
        }
        //Basic.msg("-> "+text);
        return rawTx;
    }

    private static int validatePhoneNumber(String[] list) {
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

    private static boolean ValidCodeArea(String value){
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

    private static int validateID(String[] list) {
        for (int i = 0; i< list.length ; i++){
            String text = list[i];
            //Basic.msg("-> "+text);
            text = text.replaceAll("\\W", "");
            if(text.length() > 5 && text.length() < 10){
                return i;
            }
        }
        return -1;
    }

    private static String validateType(String text){
        text = text.replaceAll("[a-z]{2,}","");
        text = text.replaceAll("[^\\d[a-z]\\s]","");
        String mType = "v";

        Pattern p = Pattern.compile("([a-z][0-9]{6,10})");
        for(String newTx : text.split(" ")) {
            Matcher m = p.matcher(newTx);
            if (m.find()) {
                for(String mtype : mTypeList) {
                    if(newTx.startsWith(mtype)) {
                        return mtype;
                    }
                }
            }
        }
        return mType;
    }

    private static Object[] validateBankCode(String[] list) {
        for (int i = 0; i< list.length ; i++){
            String text = list[i];
            //Basic.msg("-> "+text);

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

    private static String[] validateBankName(List<String> numList, String[] list) {
        for (String s : list) {
            String text = s;
            for (String newTx : numList) {
                text = text.replace(newTx, "");
            }
            for (String newTx : mTypeList) {
                text = text.replaceAll("(^" + Pattern.quote(newTx) + "$)", "");
            }
            if (text.isEmpty()) {
                continue;
            }

            for (String newTx : mBankList) {
                String[] strList = newTx.split(";");
                String pattern = "\\b" + Pattern.quote(text) + "\\b";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(strList[1].toLowerCase());
                if (m.find()) {
                    return new String[]{strList[0], strList[3]};
                }
                for (String ttx : text.split("_")) {
                    pattern = "\\b" + Pattern.quote(ttx) + "\\b";
                    p = Pattern.compile(pattern);
                    m = p.matcher(strList[1].toLowerCase());
                    if (m.find()) {
                        return new String[]{strList[0], strList[3]};
                    }
                }
            }
            for (String newTx : mBankList) {
                String[] strList = newTx.split(";");
                String pattern = "\\b" + Pattern.quote(text) + "\\b";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(strList[2].toLowerCase());
                if (m.find()) {
                    return new String[]{strList[0], strList[3]};
                }
                for (String ttx : text.split("_")) {
                    pattern = "\\b" + Pattern.quote(ttx) + "\\b";
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

    private static String validateMonto(List<String> numList, String rawTx) {

        String[] mony = {"monto_bs","bs","bs.","bolos","bsf","bolivares","monto"};

        rawTx = rawTx.replaceAll("([\\n\\s])", "_");
        for(String newTx:mony){
            rawTx = rawTx.replaceAll(newTx, "_bs_");
        }

        rawTx = rawTx.replaceAll("([^0-9,.bs_|])", "");
        rawTx = rawTx.replaceAll("((^|_)[bs](_|$))", "");

        //Basic.msg("-> "+rawTx);

        //mDebug[0] = rawTx;

        for(String newTx : numList){
            rawTx = rawTx.replace(newTx, "");
        }

        rawTx = rawTx.replaceAll("(_)+", "_");

        Pattern patt = Pattern.compile("((^|_)\\d{1,3}([._]\\d{3})*(,\\d+)?(_|$))");
        Matcher matc = patt.matcher(rawTx);
        if(matc.find()){
            String gr = matc.group(1);
            assert gr != null;
            String grCopy = gr.replaceAll("\\D", "");
            for(String newTx : numList){
                //Basic.msg("-> "+newTx);
                if(newTx.equals(grCopy)){
                    rawTx = rawTx.replaceFirst(gr, "");
                    gr = "";
                    break;
                }
            }
            grCopy = gr.replaceAll("(^_+)|(_+$)", "");
            grCopy = grCopy.replaceAll("_", ".");
            grCopy = grCopy.replaceAll("(^)0+", "");

            if(!gr.isEmpty()) {
                rawTx = rawTx.replaceFirst(gr, grCopy);
            }
            //Basic.msg("-> "+grCopy);
        }
        rawTx = rawTx.replaceAll("bs", "_bs_");

        //Basic.msg("-> "+rawTx);

        patt = Pattern.compile("(bs_+[0-9]|[0-9]_+bs)");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);
            assert gr != null;
            String grCopy = gr.replaceAll("^_|_$", "");
            grCopy = grCopy.replaceAll("_{2,}", "_");
            rawTx = rawTx.replace(gr, grCopy);
        }

        //Basic.msg("-> "+rawTx);

        // Procesan con simbolo BS
        patt = Pattern.compile("(bs_[\\d,.]+(\\w|$))");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            gr = gr.replaceAll("[^\\d,.]", "");
            gr = gr.replaceAll("(^)0+", "");
            gr = gr.replaceAll("(^),", "0,");
            gr = gr.replaceAll("[,.]+$|^[,.]+", "");
            return gr;
        }

        //Basic.msg("-> "+rawTx);

        patt = Pattern.compile("((^|\\w)[\\d,.]+_bs)");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            gr = gr.replaceAll("[^\\d,.]", "");
            gr = gr.replaceAll("(^)0+", "");
            gr = gr.replaceAll("(^),", "0,");
            gr = gr.replaceAll("[,.]+$|^[,.]+", "");
            return gr;
        }
        //---------------------------------------------------------------
        //Basic.msg("-> "+rawTx);

        rawTx = rawTx.replaceAll("(\\|)+", "_");

        rawTx = rawTx.replaceAll("[bs]","");
        for(String newTx : rawTx.split("_")){
            newTx = newTx.replaceAll("[,.]+$|^[,.]+", "");
            newTx = newTx.replaceAll("(^)0+", "");
            if(!newTx.isEmpty()){
                return newTx;
            }
        }
        return "";
    }
}
