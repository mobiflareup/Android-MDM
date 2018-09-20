package org.conveyance.configuration;

import com.mobiocean.BuildConfig;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.CallHelper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          22-09-16         Initial creation
 ****************************************************************************/

public class RHelper {

    public String SERVER_URL = MobiApplication.CONTACT_SERVER;

    public String dateTime() {
//        Calendar c = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
//        String strDate = sdf.format(c.getTime());
//        return strDate;
        return CallHelper.GetTimeWithDate();
    }

    public RConveyanceAPI getInterface(){
        RConveyanceAPI rConveyanceAPI = null;
        try{
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (BuildConfig.DEBUG)
                builder.addInterceptor(loggingInterceptor);
            OkHttpClient okHttpClient = builder.connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            rConveyanceAPI = retrofit.create(RConveyanceAPI.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return rConveyanceAPI;
    }

}
