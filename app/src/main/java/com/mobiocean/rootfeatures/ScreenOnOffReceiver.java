package com.mobiocean.rootfeatures;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobiocean.util.DeBug;

import org.sn.util.Constants;

/**
 * Created by SIVA on 04-10-17.
 */

public class ScreenOnOffReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            DeBug.ShowLog("QUICKSETTINGS", "SCREEN_ON");
            // some code
            if (!Constants.isMyServiceRunning(context, DisableQuickSettingsService.class)) {
                DeBug.ShowLog("QUICKSETTINGS", "ON_SATRT");
                context.startService(new Intent(context, DisableQuickSettingsService.class));
            }
        }
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            DeBug.ShowLog("QUICKSETTINGS", "SCREEN_OFF");
            // some code
            if (Constants.isMyServiceRunning(context, DisableQuickSettingsService.class)) {
                DeBug.ShowLog("QUICKSETTINGS", "ON_STOP");
                context.stopService(new Intent(context, DisableQuickSettingsService.class));
            }
        }
    }
}