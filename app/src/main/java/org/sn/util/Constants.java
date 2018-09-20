package org.sn.util;

import android.app.ActivityManager;
import android.content.Context;

import com.mobiocean.BuildConfig;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.AanwlaService;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Narayanan
 */

public class Constants {
    public static String FTP_HOST = "admin.mobiocean.com";
    public static String FTP_USER = "MobiAndroid";
    public static String FTP_PASS = "Ocean@Ginger123";
    public static String FILE_PASSWORD = "MoBiP30";
    public static HashMap<ServiceCallback, Object> serviceResponse = new HashMap<>();

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static AanwlaService getService(){
        AanwlaService aanwlaService = null;
        try {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG)
                builder.addInterceptor(loggingInterceptor);
            OkHttpClient okHttpClient = builder.build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(MobiApplication.CONTACT_SERVER)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            aanwlaService = retrofit.create(AanwlaService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return aanwlaService;
    }
}
