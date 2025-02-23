package com.example.copy_paste_bank;

import android.annotation.SuppressLint;
import android.content.Context;
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
    private static int mSelec;
    private static TextView mTextView;

    private static float mDollar = 0;


    static List<String> mUrl = Arrays.asList("https://pydolarve.org/api/v1/dollar?page=bcv", "https://pydolarve.org/api/v1/dollar?page=enparalelovzla");
    static List<String> mkey = Arrays.asList("usd", "enparalelovzla");

    public GetDollar(Context applicationContext, FragmentActivity mActivity, int mSelec, TextView mTextView) {
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.mSelec = mSelec;
        this.mTextView = mTextView;
    }

    public static float getPrice(){
        return GetDollar.mDollar;
    }

    public static void urlRun() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(mUrl.get(mSelec))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                mActivity.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
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
                                    if (mObjB.equals(mkey.get(mSelec))) {
                                        String price = newJson.getJSONObject(mObjB).get("price").toString();
                                        GetDollar.mDollar = Float.parseFloat(price);
                                        //Basic.msg("--- " + newJson.getJSONObject(mObjB).get("price"));
                                        mTextView.setText(Basic.setFormatter(price)+" Bs");
                                    }
                                }
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
