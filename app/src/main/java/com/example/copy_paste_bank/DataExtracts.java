package com.example.copy_paste_bank;

import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DataExtracts {
    private static final List<String> mTypeList = Arrays.asList("v","j","g","p","e","r","c" );
    private static final List<String> mAreaList = Arrays.asList("0426", "0416", "0414", "0412", "0424", "0422");
    private static final List<String> mBankList = Arrays.asList(
            "0102;BDV DE_VENEZUELA;VNZ VENZ VNZL VENEZUELA;Venezuela",
            "0156;100%;100%_BANCO;100%Banco",
            "0172;BANCAMIGA;|;Bancamiga",
            "0114;BANCARIBE;|;Bancaribe",
            "0171;ACTIVO;|;Bco. Activo",
            "0175;BDT BICENTENARIO DE_LOS_TRABAJADORES;TRABAJADORES;BDT",
            "0166;AGRICOLA;BA BAV;BAgricola",
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

    private static GlobalData glData = GlobalData.getInstance(AppContextProvider.getAppContext());

    public DataExtracts(){
    }

    public static void startinProcess(String clipText){

        //Elimina partes no necesarias de numeros de cuentas (Antento a ESTO)
       clipText = clipText.replaceAll("(?<!\\d)(01\\d{2})(?:[- _]?\\d){16}(?!\\d)", "$1");
       clipText = clipText.replaceAll("\\b(01\\d{2})(?=.{1,16}\\b)\\*{3,}\\d+\\b", "$1");


        clipText = clipText.replaceAll("\\b(04\\d{2})[-./\\s](?:(\\d{3})[-./\\s](\\d{2})[-./\\s](\\d{2})|(\\d{2})[-./\\s](\\d{2})[-./\\s](\\d{3})|(\\d{3})[-./\\s](\\d{4}))\\b", "$1$2$3$4$5$6$7$8$9");

        //GlobalData.dataDbg[0] = clipText;



        // Busca números que inicien con al menos dos ceros (00) y que tengan en total 4 o más dígitos
        clipText = clipText.replaceAll("\\b00+\\d{2,}\\b", "");

        //--------------------------------------
        //Elimina formatos de fechas
        clipText = clipText.replaceAll("(?:\\s|^)(\\[\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{1,2}])(?:\\s|$)", "");

        clipText = clipText.replaceAll("(\\d{2})[\\-/:]\\d{2}[\\-/:]\\d{2,4}", "");
        clipText = clipText.replaceAll("(\\d{1,2})(:)(\\d{1,2})(:)(\\d{1,2})(pm|am|p\\.m\\.|a\\.m\\.|p\\.\\sm\\.)", "");
        clipText = clipText.replaceAll("(\\d{1,2})(:)(\\d{1,2})(\\s)(pm|am|p\\.m\\.|a\\.m\\.|p\\.\\sm\\.)", "");

        //Elimina formatos no utiles
        clipText = clipText.replaceAll("(?<!\\d)(\\d{1,2})/(\\d{1,2})(?!\\d)", "");



        clipText = clipText.replaceAll("\\$", "bs");

        clipText = clipText.replaceAll("[.\\h,_\\-](?=\\d{3}(?:[.\\h,_\\- ]|\\b))", "");

        GlobalData.dataDbg[0] = clipText;

        clipText = processString(clipText);


        //---------------------------------------------------------------------------------------------

        String copyTx = clipText.replaceAll("(\\b04\\d*)[.\\s,_\\-](?=\\d)", "$1");



        copyTx = copyTx.replaceAll("^580?", "0");

        String[] strRes = validatePhoneNumber(copyTx);

        glData.setDateList(0, strRes[1]+strRes[2]); //Telf + Area
        glData.setDateList(1,  strRes[1]);


        List<String> txList = new ArrayList<>();
        txList.add( strRes[1]+strRes[2] );

        copyTx = strRes[0];


        //----------------------------------------------------------------------------------------------------


        String regRif = "(?<![a-z])[-_./\\s]{0,3}(\\d{6,9})_(\\d)";

        for (int i = 0; i < mTypeList.size(); i++) {
            String s = mTypeList.get(i);

            // Se construye el String de la RegEx de forma dinámica con la palabra actual
            String regex = "(?<=^|_|\\s)(" + Pattern.quote(s) + ")[-_./\\s]{0,3}(\\d{6,9})";

            // OPTIMIZACIÓN: Compilar el patrón dentro del bucle es la forma correcta de procesarlo
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(copyTx);

            // Realiza el reemplazo equivalente al replaceAll pero de forma más veloz
            if(i == 1) {
                copyTx = matcher.replaceAll(i + "#" + s + "$2");
                //Unificamos los digitos del rif
                copyTx = copyTx.replaceAll(regRif, "$1$2");            }
            else {
                copyTx = matcher.replaceAll(i + "#" + s + "$2");
            }
        }
        copyTx = copyTx.replaceAll("\\s+",  "_");
         //GlobalData.dataDbg[0] = copyTx;

//----------------------------------------------------------------------------------------------------


        String mSpl = "_+";
        String[] txAll = copyTx.split(mSpl);

        //GlobalData.dataDbg[0] = copyTx;//Arrays.toString(txNum);

        //-----------------------------------------------------------------------------------
        String[] txNum = copyTx.replaceAll("([^0-9_.,#])","").split(mSpl);

        //GlobalData.dataDbg[0] = Arrays.toString(txNum);


        Integer[] resID = validateID(txNum);
        //Basic.msg("-Id>? "+idx);

        //if(idx >(-1)) {
            String mType = mTypeList.get(resID[0]); //validateType(clipText);
            String mId = String.valueOf(resID[1]);
            txList.add(mId);
            glData.setDateList(2, mType.toUpperCase()+mId);
            //txNum[idx] = "";    //Clear the cedula number
        //}

        Object[] namRes = validateBankCode(txAll);
        int idx = (int)namRes[0];
        //Basic.msg("->? "+idx);

       // GlobalData.dataDbg[0] = copyTx;


        if(idx >(-1)) {
            glData.setDateList(3, (String)namRes[1]);
            glData.setDateList(5, (String)namRes[2]);
            txList.add( ((String) namRes[1]) );
        }
        else {
            String[] bankCode = validateBankName(txList, copyTx);
            if(!bankCode[0].isEmpty()){
                glData.setDateList(3, bankCode[0]);
                glData.setDateList(5, bankCode[1]);
            }
        }
        if(glData.getDate(4).isEmpty()){

            glData.setDateList(4, validateMonto(txList, copyTx));
            glData.setDateList(6,  glData.getDate(4).replaceAll("\\.",""));
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

        rawTx = rawTx.replaceAll("\\+58(?:\\s*0|\\s+)?", "0");

       // GlobalData.dataDbg[0] = rawTx;

        //rawTx = rawTx.replaceAll("(\\+580)|(\\+58\\s)|(\\+58)", "0");

        rawTx = rawTx.replaceAll("\\n", "_|_");

        rawTx = rawTx.replaceAll("(\\s+\\n|\\n\\s+)", " ");
        rawTx = rawTx.replaceAll("\\n", " ");
        rawTx = rawTx.replaceAll("(\\.\\s)|(\\s+)", " ");

        //Elimina posibles "o" en numeros -----------------------------------------------
        Pattern patt = Pattern.compile("(o[0-9]{3})");
        Matcher m = patt.matcher(rawTx);

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

        //GlobalData.dataDbg[0] = rawTx;

        //Espacio entre numeros de telefono -----------------------------
        patt = Pattern.compile("((^|[\\s_])(\\d{4})([\\s\\-/])([\\d\\s]{7,9}([\\s_]|$)))");
        m = patt.matcher(rawTx);
        if (m.find()) {
            String gr = m.group(1);
            assert gr != null;
            //Basic.msg("-> "+gr);
            String grCopy = gr.replaceAll("[\\s_\\-/]+", "");
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
        //GlobalData.dataDbg[0] = rawTx;
        //Basic.msg("-> "+text);
        return rawTx;
    }

    private static String[] validatePhoneNumber(String rawTx) {
        String[] res = {rawTx, "", ""};

        //GlobalData.dataDbg[0] = rawTx;
        boolean b = true;
        for (int i = 0; i < mAreaList.size(); i++) {
            String s = mAreaList.get(i);

            // Grupo 1: (" + Pattern.quote(s) + ") -> Captura el código de área de la lista
            // Separador: (?:|\\D{1,3})            -> Tolera los símbolos intermedios (no los captura)
            // Grupo 2: (\\d{7})                   -> Captura exactamente los 7 dígitos finales
            String regex = "(" + Pattern.quote(s) + ")(?:|\\D{1,3})(\\d{7})(?!\\d)";

            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(rawTx);

            if (m.find()) {
                String part1 = m.group(1);  // Ej: "0414"
                String part2 = m.group(2); // Ej: "3829688"
                if (b) {
                    res[1] = part1;
                    res[2] = part2;

                    b = false;
                }

                rawTx = rawTx.replaceAll((part1 + part2), "");

            }
        }

        // 5. Guardamos el texto final procesado
        res[0] = rawTx;

        return res;
    }

    private static boolean ValidCodeArea(String value){
        value = value.replaceAll("^580?", "0");
        for (String s : mAreaList){
            //Basic.msg("-> "+text.substring(1));
            if(value.startsWith(s)) {
                //Basic.msg("-> "+text);
                String tlf = value.replaceFirst(s, "");
                if(tlf.length() == 7) {
                    glData.setDateList(0, value);
                    glData.setDateList(1,  tlf);
                    return true;
                }
            }
        }
        return false;
    }

    private static Integer[] validateID(String[] list) {

        List<Integer[]> listRes = new ArrayList<>();

        // Eliminamos ^ y $, y hacemos el prefijo totalmente opcional
        Pattern patron1 = Pattern.compile("(?<=^|\\D)(?:(\\d)#)?(\\d{6,9})(?!\\d)");
     //   GlobalData.dataDbg[0] = Arrays.toString(list);


        for (String text : list) {
            Integer[] res = {0, 0};

            Matcher matcher = patron1.matcher(text);

            // Cambiamos .matches() por .find() para buscar dentro de la cadena
            if (matcher.find()) {
                String parte1 = matcher.group(1); // Si el texto es "25681650", parte1 será NULL
                String parte2 = matcher.group(2); // Siempre tendrá el número largo "25681650"

                if (parte1 != null) {
                    res[0] = Integer.parseInt(parte1);
                } // Si es null, res[0] se queda en 0 por defecto de forma segura

                if (parte2 != null) {
                    res[1] = Integer.parseInt(parte2);
                }

                listRes.add(res);

                // Tu evaluación inteligente sigue funcionando:
                // Si tenía la parte del '#', parte1 no es null y rompe el ciclo.
                if (parte1 != null) {
                    listRes.clear();
                    listRes.add(res);
                    break;
                }
            }
        }
        if (!listRes.isEmpty()) {
            Integer[] res = listRes.get(0);
            if (res[0] >= mTypeList.size()) {
                res[0] = 0;
            }
            return res;
        }
        else {
            return (new Integer[]{0, 0});
        }
    }

    private static Object[] validateBankCode(String[] list) {

       //GlobalData.dataDbg[0] = Arrays.toString(list);
        Pattern p = Pattern.compile("(?<=^|\\D)(01\\d{2})(?=$|\\D)");
        for (int i = 0; i< list.length ; i++){
            String text = list[i];

            Matcher m = p.matcher(text);
            if (m.find()) {
              String gr = m.group(1);
                for(String mCode : mBankList){
                    String code = mCode.split(";")[0];
                    if(code.equals(gr)){
                        glData.setDateList(3, code);
                        return new Object[]{i,code,mCode.split(";")[3]};
                    }
                }
            }
        }
        return new Object[]{-1,"",""};
    }

    private static String[] validateBankName(List<String> numList, String rawTx) {
            for (String newTx : numList) {
                rawTx = rawTx.replace(newTx, "");
            }
            for (String newTx : mTypeList) {
                rawTx = rawTx.replaceAll("(^" + Pattern.quote(newTx) + "$)", "");
            }

            rawTx = rawTx.replaceAll("[\\s|_-]+","_");


            rawTx = rawTx.replaceAll("(^_)|(_$)","");

        //Basic.msg("->? "+text);
            for (String newTx : mBankList) {
                String[] strList = newTx.split(";");
                String pattern = "(?<=^|_)" + Pattern.quote(rawTx) + "(?=$|_)";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(strList[1].toLowerCase());
                if (m.find()) {
                    return new String[]{strList[0], strList[3]};
                }
                for (String ttx : strList[1].toLowerCase().split(" ")) {
                    if(rawTx.contains(ttx)){
                        //mDebug[0] = ttx;
                        return new String[]{strList[0], strList[3]};
                    }
                }
            }
            for (String newTx : mBankList) {
                String[] strList = newTx.split(";");
                String pattern = "(?<=^|_)" + Pattern.quote(rawTx) + "(?=$|_)";
                Pattern p = Pattern.compile(pattern);
                Matcher m = p.matcher(strList[2].toLowerCase());
                if (m.find()) {
                    return new String[]{strList[0], strList[3]};
                }
                for (String ttx : strList[2].toLowerCase().split(" ")) {
                    if(rawTx.contains(ttx)){
                        //mDebug[0] = ttx;
                        return new String[]{strList[0], strList[3]};
                    }
                }
            }

        return new String[]{"",""};
    }

    private static String validateMonto(List<String> numList, String rawTx) {

        String[] mony = {"monto_(bs.)","monto_bs","bs","bs.","bolos","bsf","bolivares","monto", "dolar", "verdes", "dolares"};

        rawTx = rawTx.replaceAll("([\\n\\s])", "_");

        for(String s : mony){
            rawTx = rawTx.replaceAll("[^a-z\\d]"+s+"[^a-z\\d]", "_bs_");
        }

        //GlobalData.dataDbg[0] = rawTx;

        rawTx = rawTx.replaceAll("\\d#[a-z]", "");

        Pattern patt;
        Matcher matc;

        // 1. Escapar y unir los números con el operador OR (|)
        String alternancias = numList.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        // 2. Crear el patrón con límites de palabra (\b)
        patt = Pattern.compile("(?<=^|_)(" + alternancias + ")(?=$|_)");
        matc = patt.matcher(rawTx);

        // 3. Eliminar los valores y limpiar espacios dobles sobrantes
        rawTx = matc.replaceAll("").replaceAll("\\s+", " ").trim();

        rawTx = rawTx.replaceAll("([^0-9,.bs_|])", "");
        rawTx = rawTx.replaceAll("((^|_)[bs](_|$))", "");

        rawTx = rawTx.replaceAll("_+", "_");

        // 2. Mejor regex: captura números completos (con o sin separadores)
        patt = Pattern.compile("((?:^|_)\\d+(?:[._]\\d+)*(?:,\\d+)?(?:_|$))");
        matc = patt.matcher(rawTx);

        if (matc.find()) {
            String gr = matc.group(1);
            String grCopy = gr.replaceAll("\\D", ""); // solo dígitos

            // Si está en la lista negra, eliminarlo completamente
            boolean b = false;
            for (String newTx : numList) {
                if (newTx.equals(grCopy)) {
                    rawTx = rawTx.replaceFirst(Pattern.quote(gr), "");
                    b = true;
                    break;
                }
            }

            //GlobalData.dataDbg[0] = rawTx;

            if (!b) {
                // Limpiar separadores y normalizar
                String clan = gr.replaceAll("(^_+)|(_+$)", ""); // quita _ al inicio/fin
                clan = clan.replaceAll("_", ".");           // _ → .
                clan = clan.replaceAll("^0+(\\d)", "$1");   // quita ceros a la izquierda (mejor que tu regex)

                if (!clan.isEmpty()) {
                    rawTx = rawTx.replaceFirst(Pattern.quote(gr), clan);
                }
            }
        }

        //GlobalData.dataDbg[0] = rawTx;

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
        // ==================== PROCESAR BS ====================

        // Caso 1: bs_ al inicio (bs_9450, bs_9.450, etc)
        patt = Pattern.compile("bs[_ ]*([\\d,.]+)");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);

            //GlobalData.dataDbg[0] = gr ;

            return limpiarNumero(gr);
        }

        // Caso 2: _bs al final (9450_bs, 9.450_bs, etc)
        patt = Pattern.compile("([\\d,.]+)[_ ]*bs");
        matc = patt.matcher(rawTx);
        if (matc.find()) {
            String gr = matc.group(1);

           // GlobalData.dataDbg[0] = limpiarNumero(gr) ;

            return limpiarNumero(gr);
        }
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
                newTx = newTx.replaceAll("([a-z]{2,})\\s+([a-z]{2,})", "$1_$2");
               // GlobalData.dataDbg[0] = Arrays.toString(rawTx.split("\\n"));

                // ==================== CONVERTIR FORMATO EN → ES ====================
                Pattern patt = Pattern.compile("([0-9,]+(?:\\.[0-9]{1,2})?)$");
                Matcher matc = patt.matcher(newTx);

                if (matc.find()) {
                    String numero = matc.group(1);

                    // Si parece formato inglés (tiene coma como separador de miles y punto decimal)
                    if (numero.contains(",") && numero.contains(".")) {

                        // 1. Quitar todas las comas (separadores de miles)
                        String limpio = numero.replaceAll(",", "");

                        // 2. Cambiar el punto decimal por coma
                        limpio = limpio.replace(".", ",");

                        // 3. Añadir separadores de miles con punto (opcional pero recomendado)
                        limpio = agregarSeparadoresMilesES(limpio);

                        newTx = newTx.replace(numero, limpio);
                    }
                }

                // Basic.msg("?-> " + newTx);
                tx += newTx.replaceAll("[\\s-.]+", "") + " ";
            }
            if(res){
                //Basic.msg("-> "+tx);
                return tx;
            }
        }
        return rawTx;
    }

    private static String agregarSeparadoresMilesES(String numero) {
        // numero debe venir como "1000000,55" o "1234" o "1234,56"

        String[] partes = numero.split(",");
        String entero = partes[0];
        String decimal = partes.length > 1 ? "," + partes[1] : "";

        // Añadir puntos cada 3 dígitos de derecha a izquierda
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = entero.length() - 1; i >= 0; i--) {
            sb.append(entero.charAt(i));
            count++;
            if (count % 3 == 0 && i > 0) {
                sb.append(".");
            }
        }
        return sb.reverse().toString() + decimal;
    }

    private static String limpiarNumero(String num) {
        if (num == null || num.isEmpty()) return "";

        num = num.replaceAll("[^\\d,.]", "");           // solo números, punto y coma
        num = num.replaceAll("^0+(\\d)", "$1");         // quitar ceros a la izquierda
        num = num.replaceAll("^,", "0,");               // ,123 → 0,123
        num = num.replaceAll("[,.]+$", "");             // quitar separadores al final

        return num;
    }
}
