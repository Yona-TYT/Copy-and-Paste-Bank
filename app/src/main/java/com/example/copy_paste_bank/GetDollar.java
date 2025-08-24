package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetDollar {

    private static Context mContext;
    private static FragmentActivity mActivity;
    private static Spinner mSpinner;
    private static TextView mTextView;

    public static List<Float> mDollar = Arrays.asList((float)0, (float)0, (float)0, (float)0);
    public static List<String> mDate = Arrays.asList("", "", "", "");

    private static List<String> mUrlA = Arrays.asList("https://pydolarve.org/api/v1/dollar?page=bcv", "https://pydolarve.org/api/v1/dollar?page=criptodolar", "https://pydolarve.org/api/v1/dollar?page=enparalelovzla", "");
    private static List<String> mkeyA = Arrays.asList("usd", "promedio", "enparalelovzla", "");
    private static List<String> mObjA = Arrays.asList("price", "last_update", "", "");

    private static List<String> mUrlB = Arrays.asList("https://ve.dolarapi.com/v1/dolares/oficial", "https://pydolarve.org/api/v1/dollar?page=criptodolar", "https://ve.dolarapi.com/v1/dolares/paralelo", "");
    private static List<String> mkeyB = Arrays.asList("promedio", "promedio", "promedio", "");
    private static List<String> mObjB = Arrays.asList("promedio", "fechaActualizacion", "", "");

    //Mapa de arrays
    public static HashMap<String, List<List<String>>> arrayMap = new HashMap<>();;
    private static List<String> mIds = Arrays.asList("id1", "id2");

    private static int myTry = 0;

    public GetDollar(Context mContext, FragmentActivity mActivity, Spinner mSpinner, TextView mTextView) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mSpinner = mSpinner;
        this.mTextView = mTextView;

        arrayMap.clear(); //Limpia el mapa

        List<List<String>> buildList0 = new ArrayList<>();
        buildList0.add(mUrlA);
        buildList0.add(mkeyA);
        buildList0.add(mObjA);
        arrayMap.put(mIds.get(0), buildList0);

        List<List<String>> buildList1 = new ArrayList<>();
        buildList1.add(mUrlB);
        buildList1.add(mkeyB);
        buildList1.add(mObjB);
        arrayMap.put(mIds.get(1), buildList1);

    }
    public static float getPrice(int idx){
        return GetDollar.mDollar.get(idx);
    }

    public static void urlRun() throws IOException {
        int idx = 0;
        List<List<String>> i = arrayMap.get(mIds.get(myTry));
        if(i != null) {
            Request request = new Request.Builder()
                    .url(i.get(0).get(idx))
                    .build();
            setRequest(request, idx, myTry);

            idx = 1;
            request = new Request.Builder()
                    .url(i.get(0).get(idx))
                    .build();
            setRequest(request, idx, myTry);

            idx = 2;
            request = new Request.Builder()
                    .url(i.get(0).get(idx))
                    .build();
            setRequest(request, idx, myTry);

            /* Este valor es local
            idx = 3;
            request = new Request.Builder()
                    .url(mUrl.get(idx))
                    .build();
            setRequest( request, idxm, myTry);

             */
        }
    }

    public static void setRequest(Request request, int idx, int tries){

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                mActivity.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        if(idx == (mUrlA.size()-2)) {
                            if(tries == 0) {
                                myTry = 1;
                                try {
                                    urlRun();
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                                Basic.msg("Reintentando CONEXION!.");

                            }
                            else {
                                myTry = 0;
                                Basic.msg("Error de CONEXION!");
                            }
                        }
                    }
                });
                call.cancel();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();
                List<List<String>> i = arrayMap.get(mIds.get(tries));
                try {
                    //JSONObject json = new JSONObject(myResponse);
                    JSONObject json = new JSONObject(myResponse);
                    Iterator<String> mKeysA = json.keys();

                    if(tries == 0) {
                        for (; mKeysA.hasNext(); ) {
                            String mObjA = mKeysA.next();
                            JSONObject newJson = json.getJSONObject(mObjA);
                            Iterator<String> mKeysB = newJson.keys();

                            for (; mKeysB.hasNext(); ) {
                                String mObjB = mKeysB.next();
                                if (mObjB.equals(mkeyA.get(idx))) {
                                    String price = newJson.getJSONObject(mObjB).get("price").toString();
                                    GetDollar.mDollar.set(idx, Float.parseFloat(price));

                                    GetDollar.mDate.set(idx, newJson.getJSONObject(mObjB).get("last_update").toString());
                                    //Basic.msg("--- " + newJson.getJSONObject(mObjB).get("price"));
                                    //mTextView.setText(Basic.setFormatter(price)+" Bs");
                                }
                            }
                        }
                    }
                    else if(i != null){
                        for (; mKeysA.hasNext(); ) {
                            String mObjA = mKeysA.next();
                            //String price = json.getJSONObject(mObjA).get("price").toString();

                            if (mObjA.equals(i.get(2).get(0))) {
                                String price = json.get(i.get(2).get(0)).toString();
                                Log.d("PhotoPicker", " --------------Aqui Hay URL?------------------------: " + mObjA+" - "+price);
                                GetDollar.mDollar.set(idx, Float.parseFloat(price));
                            }
                            if (mObjA.equals(i.get(2).get(1))) {
                                String date = json.get(i.get(2).get(1)).toString();
                                Log.d("PhotoPicker", " --------------Aqui Hay URL?------------------------: " + mObjA+" - date");
                                GetDollar.mDate.set(idx, date);
                            }
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                mActivity.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        if(idx == (mUrlA.size()-1)) {
                            mTextView.setText(Basic.setFormatter(GetDollar.mDollar.get(idx).toString()) + " Bs");
                        }
                        List<String> mSpinL1 = Arrays.asList("BCV", "Promedio", "Paralelo", "Valor Personalizado");
                        for (int i = 0; i < mSpinL1.size(); i++) {
                            String tx = mSpinL1.get(i) + (i == mSpinL1.size()-1 ? "" : " "+Basic.setFormatter(GetDollar.mDollar.get(i).toString())+" Bs");
                            mSpinL1.set(i, tx);
                        }
                        SelecAdapter adapt1 = new SelecAdapter(mContext, mSpinL1);
                        mSpinner.setAdapter(adapt1);
                    }
                });
            }
        });
    }
}
