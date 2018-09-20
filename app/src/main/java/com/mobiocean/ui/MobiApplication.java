package com.mobiocean.ui;

import android.app.Application;

import com.mobiocean.BuildConfig;
import com.mobiocean.util.AanwlaService;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MobiApplication extends Application {

    public static final String CONTACT_SERVER = "http://admin.mobiocean.com/";
   // public static final String CONTACT_SERVER = "http://192.168.0.101/MobiOcean.MDM/";

    private AanwlaService aanwlaService;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG)
                builder.addInterceptor(loggingInterceptor);
            OkHttpClient okHttpClient = builder.connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(CONTACT_SERVER)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            aanwlaService = retrofit.create(AanwlaService.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public AanwlaService getContactService() {
        return aanwlaService;
    }

    public void clearApplicationData() {
        File cacheDirectory = getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }

        return deletedAll;
    }
}