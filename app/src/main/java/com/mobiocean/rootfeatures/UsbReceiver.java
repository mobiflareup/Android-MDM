package com.mobiocean.rootfeatures;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.mobiocean.service.OneMinuteService;
import com.mobiocean.util.DeBug;

import org.sn.util.Constants;

/**
 * Created by SIVA on 01-05-17.
 */

public class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        String path = intent.getDataString();

        if (intent.getAction() == Intent.ACTION_POWER_CONNECTED) {
            Toast.makeText(context, "USB_POWER", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED) {
            Toast.makeText(context, "USB_POWER_DISCONNECT", Toast.LENGTH_SHORT).show();
            try {
                if (Constants.isMyServiceRunning(context, ChangeToChargingService.class)) {
                    context.stopService(new Intent(context, ChangeToChargingService.class));
                    DeBug.ShowLog("CHARGING", "USB_POWER_DISCONNECT");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
