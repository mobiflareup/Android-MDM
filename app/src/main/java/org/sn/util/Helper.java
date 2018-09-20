package org.sn.util;

import com.mobiocean.BuildConfig;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.AanwlaService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Narayanan
 */

public class Helper {

    public String SERVER_URL = MobiApplication.CONTACT_SERVER;

    public AanwlaService getInterface(){
        AanwlaService rConveyanceAPI = null;
        try{
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG)
                builder.addInterceptor(loggingInterceptor);
            OkHttpClient okHttpClient = builder.build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            rConveyanceAPI = retrofit.create(AanwlaService.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return rConveyanceAPI;
    }

}
