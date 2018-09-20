package org.sn.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;

import com.mobiocean.service.SyncIntentService;
import com.mobiocean.ui.Home;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.AanwlaService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RestApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyCustomReceiver extends BroadcastReceiver {

    private final String cryptoParam = "U_EMAIL_PASSWORD";
    private final String Enc_Key = "abcdefghijklmnopqrstuvwxyz";
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Bundle bundle = intent.getExtras();
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        if(bundle!=null){
            String appId = bundle.getString("appId");
            String reqFrom = bundle.getString("reqFrom");
            String broadCaseId = bundle.getString("broadCaseId");
            if(appId!=null && reqFrom!=null && broadCaseId!=null && !appId.isEmpty() && !reqFrom.isEmpty() && !broadCaseId.isEmpty()){
                appId = decrypt(appId, Enc_Key, true);
                reqFrom = decrypt(reqFrom, Enc_Key, true);
                broadCaseId = decrypt(broadCaseId, Enc_Key, true);
                if(appId!=null && reqFrom!=null && broadCaseId!=null && !appId.isEmpty() && !reqFrom.isEmpty() && !broadCaseId.isEmpty()){
                    verifyLoginId(appId,reqFrom,broadCaseId);
                }
            }
        }
    }

    private void verifyLoginId(final String appId, String contactName, final String broadcastId) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(MobiApplication.CONTACT_SERVER).addConverterFactory(GsonConverterFactory.create()).build();
        AanwlaService webInterface = retrofit.create(AanwlaService.class);
        Call<Integer> call = webInterface.verifyUserInfo(CallHelper.Ds.structPC.iStudId, appId, contactName);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response != null) {
                    Integer result = response.body();
                    if (result != null && result > 0) {
                        CallHelper.Ds.structPC.iStudId = appId;
                        editor.putString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
                        editor.commit();
                        if (!SyncIntentService.serviceStarted) {
                            context.startService(new Intent(context, SyncIntentService.class));
                        }
                        DeBug.ShowLog("TestingApp", "demoDeviceAdmin  activated");
                        Intent intentLaunch = new Intent(context, Home.class);
                        intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        CallHelper.isAppForground = true;
                        context.startActivity(intentLaunch);
                        new InstallStatus().execute();
                        sendBroadcast(broadcastId, true);
                    }
                } else {
                    sendBroadcast(broadcastId, false);
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                sendBroadcast(broadcastId, false);
            }
        });
    }

    private void sendBroadcast(String broadcastId, boolean isActive) {
        Intent i = new Intent();
        i.setAction(broadcastId);
        i.putExtra("isActive", isActive);
        context.sendBroadcast(i);
    }

    private class InstallStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RestApiCall mRestApiCall = new RestApiCall();
            {
                JSONObject json = new JSONObject();
                try {
                    json.put("AndroidAppId", CallHelper.Ds.structPC.iStudId);
                    json.put("isUninstalled", 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String stpassword = mRestApiCall.setAppInstallationStatus(json);
            }
            return null;
        }
    }

    /**
     * Function to encrypt
     */
    private String encrypt(String plainText, String encryptionKey, boolean isMD5) {
        try {
            if (isMD5)
                encryptionKey = encodeMD5(encryptionKey);
            encryptionKey = encryptionKey.substring(0, 16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(cryptoParam.getBytes("UTF-8")));
            byte[] encVal = cipher.doFinal(plainText.getBytes());
            return Base64.encodeToString(encVal, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Function to decrypt
     */
    private String decrypt(String cipherText, String encryptionKey, boolean isMD5) {
        try {
            if (isMD5)
                encryptionKey = encodeMD5(encryptionKey);
            encryptionKey = encryptionKey.substring(0, 16);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(cryptoParam.getBytes("UTF-8")));
            byte[] decodedValue = Base64.decode(cipherText, Base64.DEFAULT);
            byte[] decValue = cipher.doFinal(decodedValue);
            return new String(decValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Function to encode given string to MD5
     */
    private String encodeMD5(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}