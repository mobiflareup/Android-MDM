package com.mobiocean.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.mobiocean.BuildConfig;

/**
 * Test HandSet Model     = s01
 * ADT Pakage Version     = 21.0.1.201212060302
 * Eclipse Platform       = 4.2.1.v20120814
 * Date					 = October 17,2013
 * Functionality			 = To ON or OFF the Logs and Toast in Gingerbox
 * Android version		 = 2.3.6 [Gingerbread (API level 10)]
 */

public class DeBug {
    public static void ShowToast(Context context, String message) {
        if (BuildConfig.DEBUG)
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } catch (Exception ignore) {

            }
    }

    public static void ShowLog(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message);
    }

    public static void ShowLogD(String TAG, String message) {
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
    }
}
