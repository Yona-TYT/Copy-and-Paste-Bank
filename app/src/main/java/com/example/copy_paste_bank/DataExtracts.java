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
            "0102;BDV DE_VENEZUELA;VNZ VNZL VENEZUELA;Venezuela",
            "0156;100%;100%_BANCO;100%Banco",
            "0172;BANCAMIGA;|;Bancamiga",
            "0114;BANCARIBE;|;Bancaribe",
            "0171;ACTIVO;|;Bco. Activo",
            "0166;AGRICOLA;BA BAV;BAgricola",
            "0175;BDT BICENTENARIO;TRABAJADORES;BDT",
            "0128;CARONI;|;Bco. Caroni",
            "0163;DEL_TESORO;BT TESORO;BDTesoro",
            "0115;EXTERIOR;BE;BExterior",
            "0151;FONDO_COMUN;BFC FONDO;BFC",
            "0173;DE_DESARROLLO;BID DESARROLLO INTERNACIONAL;B.I.D",
            "0105;MERCANTIL;BM;Mercantil",
            "0191;BNC NACIONAL_DE_CREDITO;|;BNC",
            "0138;BANCO_PLAZA;PLAZA;Bco. Plaza",
            "0137;SOFITASA;|;Sofitasa",
            "0104;BVDC VENEZOLANO_DE_CREDITO;VENEZOLANO;BVDC",
            "0168;BANCRECER;BC;Bancrecer",
            "0134;BANESCO;|;Banesco",
            "0177;BANFANB BANFAN;FUERZA ARMADA;Banfanb",
            "0146;BANGENTE;|;Bangente",
            "0174;BANPLUS;BAN+ BPLUS;BanPlus",
            "0108;BBVA PROVINCIAL;BP;Provincial",
            "0157;DELSUR;|;Bco. DelSur",
            "0169;MIBANCO;MI_BANCO R4;R4",
            "0178;|;N58;N58"
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

        //Elimina formatos de fechas
        clipText = clipText.replaceAll("(\\d{2})[\\-/:]\\d{2}[\\-/:]\\d{2,4}", "");
        clipText = clipText.replaceAll("(\\d{1,2})(:)(\\d{1,2})(:)(\\d{1,2})(pm|am|p\\.m\\.|a\\.m\\.|p\\.\\sm\\.)", "");
        clipText = clipText.replaceAll("(\\d{1,2})(:)(\\d{1,2})(\\s)(pm|am|p\\.m\\.|a\\.m\\.|p\\.\\sm\\.)", "");

        //Elimina formatos no utiles
        clipText = clipText.replaceAll("(\\d{1,2})(/)(\\d{1,2})", "");



        clipText = clipText.replaceAll("\\$", "bs");


        //mDebug[0] = clipText;

        clipText = processString(clipText);

        String mSpl = " ";
        String copyTx = clipText.replaceAll("((_\\|_)+)|(\\|+)", " ");
        String[] txAll = copyTx.split(mSpl);
        String[] txNum = copyTx.replaceAll("([^0-9\\s.,])","").split(mSpl);

        for (String tx : txNum){
           // Basic.msg(txNum[8]);
        }

        List<String> txList = new ArrayList<>();
        int idx = validatePhoneNumber(txNum);
        if(idx >(-1)) {
            txList.add(txNum[idx].replaceAll("\\D",""));
            txNum[idx] = "";    //Clear the phone number
        }

        idx = validateID(txNum);
        //Basic.msg("-Id>? "+idx);

        if(idx >(-1)) {
            String mType = validateType(clipText);
            txList.add(txNum[idx].replaceAll("\\D",""));
            mResList[2] = mType.toUpperCase()+txNum[idx].replaceAll("([^0-9])","");
            txNum[idx] = "";    //Clear the cedula number
        }

        Object[] namRes = validateBankCode(txAll);
        idx = (int)namRes[0];
        //Basic.msg("->? "+idx);

        if(idx >(-1)) {
            mResList[3] = (String)namRes[1];
            mResList[5] = (String)namRes[2];

            txList.add(txNum[idx].replaceAll("\\D",""));
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
        rawTx = getSimple(rawTx);

        //Basic.msg(rawTx);

        //text = text.replaceAll("\\*","");
        rawTx = rawTx.replaceAll(":","");
        rawTx = rawTx.replaceAll("é","e");
        rawTx = rawTx.replaceAll("ó","o");
        rawTx = rawTx.replaceAll("í","i");

        rawTx = rawTx.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");
        //rawTx = rawTx.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");

        Pattern patt = Pattern.compile("(\\n)");
        Matcher m = patt.matcher(rawTx);

        if(m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String copyTx = gr.replaceAll("\\n","_|_");
            rawTx = rawTx.replaceAll(gr, copyTx);

        }

        rawTx = rawTx.replaceAll("(\\s+\\n|\\n\\s+)", " ");
        rawTx = rawTx.replaceAll("\\n", " ");
        rawTx = rawTx.replaceAll("(\\.\\s)|(\\s+)", " ");

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

        //mDebug[0] = rawTx;

        //Espacio entre numeros de telefono -----------------------------
        patt = Pattern.compile("((^|[\\s_])(\\d{4})([\\s\\-])([\\d\\s]{7,9}([\\s_]|$)))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            assert gr != null;
            //Basic.msg("-> "+gr);
            String grCopy = gr.replaceAll("[\\s_\\-]+", "");
            for (String newTx : mAreaList) {
                //Basic.msg("-> "+grCopy);
                if (grCopy.startsWith(newTx) && grCopy.length()==11) {
                    //Basic.msg("-> "+grCopy);
                    rawTx = rawTx.replaceAll(gr, " "+grCopy+" ") ;
                    break;
                }
            }
        }
        //----------------------------------------------------------------

        patt = Pattern.compile("((\\w{2,})(-+)(\\w{2,}))");
        m = patt.matcher(rawTx);
        while (m.find()) {
            String gr = m.group(1);
            //Basic.msg("??-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("-", "_");
            rawTx = rawTx.replaceFirst(gr, grCopy);
            m = patt.matcher(rawTx);
        }

        //Espacio entre montos ---------------------------------------
        patt = Pattern.compile("((^|[\\sa-z_])(\\d{1,3}(\\s\\d{3}){1,3})([\\s_.,]|$))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\s", "");
            rawTx = rawTx.replace(gr, grCopy);
        }
        //--------------------------------------------------------------


//        //Espacio entre cifras ---------------------------------------
//        patt = Pattern.compile("((^|\\s|[a-z])(\\d{1,3}(([^\\n\\w])\\d{3}){1,3})(\\s|$))");
//        m = patt.matcher(rawTx);
//        if (m.find()) {
//            String gr = m.group(1);
//            //Basic.msg("-> "+gr);
//            assert gr != null;
//            String grCopy = gr.replaceAll("\\s", "");
//            rawTx = rawTx.replace(gr, " "+grCopy+" ");
//        }
//        //----------------------------------------------------------------

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
        //mDebug[0] = rawTx;
        //Basic.msg("-> "+text);
        return rawTx;
    }

    private static int validatePhoneNumber(String[] list) {
        for (int i = 0; i< list.length ; i++){
            String text = list[i];
            text = text.replaceAll("(^0{2,})", "0");
            text = text.replaceAll("\\D", "");

            //Basic.msg("-> "+text);
            if(ValidCodeArea(text)){
                return i;
            }
        }
        return -1;
    }

    private static boolean ValidCodeArea(String value){
        for (String text : mAreaList){
            //Basic.msg("-> "+text.substring(1));
            if(value.startsWith(text)) {
                //Basic.msg("-> "+text);
                String tlf = value.replaceFirst(text, "");
                if(tlf.length() == 7) {
                    mResList[0] = value;
                    mResList[1] = tlf;
                    return true;
                }
            }
            String copyText = "58"+text.substring(1);
            if(value.startsWith(copyText)) {
                //Basic.msg("-> "+text);
                String tlf = value.replaceFirst(copyText, "");
                if(tlf.length() == 7) {
                    mResList[0] = text+tlf;
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
            text = text.replaceAll("([.,]\\d{1,2}$)", "");
            text = text.replaceAll("\\D", "");
            //Basic.msg("id>"+text);

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

            text = text.replaceAll("\\D", "");
            if(text.isEmpty()){
                continue;
            }
            //Basic.msg("-> "+text);
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
            text = text.replaceAll("(^_)|(_$)","");
            //Basic.msg("->? "+text);
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

        String[] mony = {"monto_(bs.)","monto_bs","bs","bs.","bolos","bsf","bolivares","monto", "dolar", "verdes", "dolares"};

        rawTx = rawTx.replaceAll("([\\n\\s])", "_");
        //mDebug[0] = rawTx;

        for(String newTx:mony){
            rawTx = rawTx.replaceAll("[^a-z\\d]"+newTx+"[^a-z\\d]", "_bs_");
        }

        for (String txTest : rawTx.split("_")){
            String txCopy = txTest.replaceAll("\\D", "");
            for(String newTx : numList){
                //Basic.msg("newTx-"+txCopy);
                if(newTx.equals(txCopy)){
                    rawTx = rawTx.replace(txTest, "");
                    break;
                }
            }
        }

        rawTx = rawTx.replaceAll("([^0-9,.bs_|])", "");
        rawTx = rawTx.replaceAll("((^|_)[bs](_|$))", "");

        //mDebug[0] = rawTx;

        rawTx = rawTx.replaceAll("(_)+", "_");

        //Basic.msg("-> "+rawTx);

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



        patt = Pattern.compile("((\\d+)(\\.)(\\d{1,2})_)");
        matc = patt.matcher(rawTx);
        if(matc.find()) {
            String gr = matc.group(1);
            //Basic.msg("-> "+gr);
            assert gr != null;
            String grCopy = gr.replaceAll("\\.", ",");
            rawTx = rawTx.replaceFirst(gr, grCopy);
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
        return "0.00";
    }
    private static String getSimple(String rawTx) {
        int count = rawTx.replaceAll("[^\\n]", "").length();
        if(count == 2 || count == 3) {
            boolean res = true;
            String tx = "";
            for(String newTx : rawTx.split("\\n")){
                int siz = newTx.length();
                if(siz < 2 || siz > 16 ){
                    //Basic.msg(""+count);
                    res = false;
                    break;
                }
                Pattern patt = Pattern.compile("(([a-z]{2,})(\\s+)([a-z]{2,}))");
                Matcher matc = patt.matcher(newTx);
                while (matc.find()) {
                    String gr = matc.group(1);
                    //Basic.msg("-> "+gr);
                    assert gr != null;
                    String grCopy = gr.replaceAll("\\s", "_");
                    newTx = newTx.replaceFirst(gr, grCopy);
                    matc = patt.matcher(newTx);
                }

                patt = Pattern.compile("((\\d+)(\\.)(\\d{1,2})$)");
                matc = patt.matcher(newTx);
                if(matc.find()) {
                    String testPhone = newTx.replaceAll("\\D","");
                    if(testPhone.length() != 11) {
                        String gr = matc.group(1);
                        //Basic.msg("-> "+gr);
                        assert gr != null;
                        String grCopy = gr.replaceAll("\\.", ",");
                        newTx = newTx.replaceFirst(gr, grCopy);
                    }
                }
                //---------------------------------------------------------------
                //Basic.msg("?-> "+newTx);
                tx += newTx.replaceAll("[\\s-.]+","")+" ";
            }
            if(res){
                //Basic.msg("-> "+tx);
                return tx;
            }
        }
        return rawTx;
    }
}
