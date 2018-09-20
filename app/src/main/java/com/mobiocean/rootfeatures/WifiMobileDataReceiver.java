package com.mobiocean.rootfeatures;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

/**
 * Created by SIVA on 10-05-17.
 */

public class WifiMobileDataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE ,
                WifiManager.WIFI_STATE_UNKNOWN);

        switch(extraWifiState){
            case WifiManager.WIFI_STATE_DISABLED:
                Toast.makeText(context, "WIFI STATE DISABLED", Toast.LENGTH_SHORT).show();
                //context.startService(new Intent(context, GpsWifiIntentService.class).putExtra("PUTFEATURETYPE",2));
            /*    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(true);*/
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                Toast.makeText(context, "WIFI STATE DISABLING", Toast.LENGTH_SHORT).show();
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                Toast.makeText(context, "WIFI STATE ENABLED", Toast.LENGTH_SHORT).show();
              /*  WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                wifi.setWifiEnabled(true);*/
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                Toast.makeText(context, "WIFI STATE ENABLING", Toast.LENGTH_SHORT).show();
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                Toast.makeText(context, "WIFI STATE UNKNOWN", Toast.LENGTH_SHORT).show();
                break;
        }

        try {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                    Toast.makeText(context, "NETWORK SUCCESS", Toast.LENGTH_SHORT).show();

                } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                    Toast.makeText(context, "NETWORK FAILED", Toast.LENGTH_SHORT).show();
                    //if(RootConstants.checkRootMethod())
                    //{
                    //    context.startService(new Intent(context, GpsWifiIntentService.class).putExtra("PUTFEATURETYPE",2));
                        //setMobileConnectionEnabled();
                    //}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMobileConnectionEnabled() {
        try {
            Runtime.getRuntime().exec("su -c svc data enable");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setMobileConnectionDisable() {

        try {
            Runtime.getRuntime().exec("su -c svc data disable");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
