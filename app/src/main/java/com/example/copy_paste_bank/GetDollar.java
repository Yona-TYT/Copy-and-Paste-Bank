package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
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

    public static List<Float> mDollar = Arrays.asList((float)0, (float)0, (float)0);


    static List<String> mUrl = Arrays.asList("https://pydolarve.org/api/v1/dollar?page=bcv", "https://pydolarve.org/api/v1/dollar?page=enparalelovzla", "https://pydolarve.org/api/v1/dollar?page=criptodolar");
    static List<String> mkey = Arrays.asList("usd", "enparalelovzla", "promedio");

    public GetDollar(Context mContext, FragmentActivity mActivity, Spinner mSpinner, TextView mTextView) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mSpinner = mSpinner;
        this.mTextView = mTextView;
    }

    public static float getPrice(int idx){
        return GetDollar.mDollar.get(idx);
    }

    public static void urlRun() throws IOException {

        int idx = 0;
        Request request = new Request.Builder()
                .url(mUrl.get(idx))
                .build();
        setRequest( request, idx);

        idx = 1;
        request = new Request.Builder()
                .url(mUrl.get(idx))
                .build();
        setRequest( request, idx);

        idx = 2;
        request = new Request.Builder()
                .url(mUrl.get(idx))
                .build();
        setRequest( request, idx);
    }

    public static void setRequest(Request request, int idx){

        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                try {
                    //JSONObject json = new JSONObject(myResponse);
                    JSONObject json = new JSONObject(myResponse);
                    Iterator<String> mKeysA = json.keys();
                    for (; mKeysA.hasNext(); ) {
                        String mObjA = mKeysA.next();
                        JSONObject newJson = json.getJSONObject(mObjA);
                        Iterator<String> mKeysB = newJson.keys();

                        for (; mKeysB.hasNext(); ) {
                            String mObjB = mKeysB.next();
                            if (mObjB.equals(mkey.get(idx))) {
                                String price = newJson.getJSONObject(mObjB).get("price").toString();
                                GetDollar.mDollar.set(idx, Float.parseFloat(price));
                                //Basic.msg("--- " + newJson.getJSONObject(mObjB).get("price"));
                                //mTextView.setText(Basic.setFormatter(price)+" Bs");
                            }
                        }
                    }
                    mActivity.runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            if(idx == (mUrl.size()-1)) {
                                List<String> mSpinL1 = Arrays.asList("BCV", "Paralelo", "Promedio");
                                mTextView.setText(Basic.setFormatter(GetDollar.mDollar.get(idx)) + " Bs");
                                for (int i = 0; i < mSpinL1.size(); i++) {
                                    String tx = mSpinL1.get(i) + " " + Basic.setFormatter(GetDollar.mDollar.get(idx));
                                    //mSpinL1.set(i, tx);
                                }
                                SelecAdapter adapt1 = new SelecAdapter(mContext, mSpinL1);
                                mSpinner.setAdapter(adapt1);
                            }
                        }
                    });

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
