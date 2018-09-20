package org.sn.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.service.UploadFileToserverIntentService;
import com.mobiocean.service.UploadService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.Struct_Send_SMSInfo;

import org.conveyance.services.RUploadDetailsService;
import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;
import org.sn.location.NetworkUtil;
import org.sn.securedstorage.AddToSecureIntentService;
import org.sn.services.UpdateConveyanceService;
import org.sn.util.SharedData;

import java.net.InetAddress;

/**
 * @author Narayanan
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    private String TimeStamp;
    private DatabaseHabdler_SMSSent SMS_db;
    private SharedData sharedData;

    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkUtil.NetworkStatus obj = NetworkUtil.getConnectivityStatus(context);
        TimeStamp = CallHelper.GetTimeWithDate();
        SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
        sharedData = new SharedData(context);
        LocationDetails locationDetails = new LocationDetails(context);
        LocationBean locationBean = locationDetails.getLocation();
        String locSrc = "Google";
        if (locationBean.IsGPS)
            locSrc = "GPS";
        double lat = 0.0;
        double lon = 0.0;
        if (locationBean.IsGPS) {
            try {
                lat = Double.parseDouble(locationBean.Lat);
                lon = Double.parseDouble(locationBean.Longt);
            } catch (Exception ignore) {
            }
        }

        if (obj != NetworkUtil.NetworkStatus.NO_NET) {
            String type = "No Net";
            if (obj == NetworkUtil.NetworkStatus.MOBILE) {
                type = "Mobile";
            } else if (obj == NetworkUtil.NetworkStatus.WIFI) {
                type = "Wifi";
                WifiManager WManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = WManager.getConnectionInfo();
                type += "-" + wifiInfo.getSSID();
            } else if (obj == NetworkUtil.NetworkStatus.OTHERS) {
                type = "Others";
            }
            if(!sharedData.canSetCheckNetworkStatus() || !sharedData.canCheckNetworkStatus()) {
                isInternetAvailable(locSrc, lat, lon, locationBean, type, context);
                sharedData.setCheckNetworkStatus(true);
            }
        } else {
            if(!sharedData.canSetCheckNetworkStatus() || sharedData.canCheckNetworkStatus()) {
                SMS_db.addSMS(new Struct_Send_SMSInfo(601, TimeStamp, "OFF", "" + locSrc, lat, lon, 15, System.currentTimeMillis(), locationBean.CellId, locationBean.LAC, locationBean.MCC, locationBean.MNC));
                sharedData.setCheckNetworkStatus(false);
            }
        }
        sharedData.setSetCheckNetworkStatus(true);
    }

    private void isInternetAvailable(final String locSrc, final double lat, final double lon, final LocationBean locationBean, final String remarks, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String rem = remarks;
                try {
                    InetAddress ipAddr = InetAddress.getByName("mobiocean.com");
                    boolean status = !ipAddr.equals("");
                    rem += "-Internet";
                    DeBug.ShowLog("InternetChange", "Network change status -> " + status);
                } catch (Exception e) {
                    e.printStackTrace();
                    rem += "-No Internet";
                } finally {
                    SMS_db.addSMS(new Struct_Send_SMSInfo(601, TimeStamp, rem, "" + locSrc, lat, lon, 15, System.currentTimeMillis(), locationBean.CellId, locationBean.LAC, locationBean.MCC, locationBean.MNC));
                    //Upload Offline SOS
                    Intent msgIntent = new Intent(context, UploadService.class);
                    Bundle b = new Bundle();
                    msgIntent.putExtra("UploadStatus", 0);
                    msgIntent.putExtras(b);
                    context.startService(msgIntent);
                    //Upload Offline SOS Files
                    context.startService(new Intent(context, UploadFileToserverIntentService.class));
                    context.startService(new Intent(context, UpdateConveyanceService.class));
                    context.startService(new Intent(context, AddToSecureIntentService.class));
                    Intent update = new Intent(context, RUploadDetailsService.class);
                    context.startService(update);
                }
            }
        }).start();
    }

}