package com.mobiocean.rootfeatures;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.mobiocean.util.DeBug;

/**
 * Created by SIVA on 02-05-17.
 */

public class GpsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            // Make an action or refresh an already managed state.
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            DeBug.ShowLog("GPSCHECK", "ok");
            if (!statusOfGPS && RootConstants.checkRootMethod()) {
                context.startService(new Intent(context, GpsWifiIntentService.class).putExtra("PUTFEATURETYPE",1));
            }
        }
    }

}
