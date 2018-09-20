package com.mobiocean.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mobiocean.rootfeatures.DisableQuickSettingsService;
import com.mobiocean.service.AppBlock;
import com.mobiocean.service.BatteryWifiInfoService;
import com.mobiocean.service.CallDetectService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;

import org.sn.util.Constants;

/**
 * Test HandSet Model    = s01
 * ADT Package Version   = 21.0.1.201212060302
 * Eclipse Platform      = 4.2.1.v20120814
 * Date					= October 17,2013
 * Functionality			= To get the time Elapsed Time (Between TurnOn and TurnOff the Android Phone and Start Services
 * On BootReceiver)
 * Android version		= 2.3.6 [Gingerbread (API level 10)]
 */
public class BootReceiver extends BroadcastReceiver {

    public static long timediff;
    public static boolean bStartedAfterBoot = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        try {
            DeBug.ShowToast(context, "On Boot  : 1");
            SharedPreferences settings = context.getSharedPreferences(CallDetectService.PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            DeBug.ShowLog("On Boot", "Received action: " + intent.getAction());
            if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN") || intent.getAction().equals("android.intent.action.QUICKBOOT_POWEROFF")) {
                DeBug.ShowToast(context, "On Boot  : 2");
                //Remove on time change....
                if (!CallHelper.Ds.structPC.bDateChangedToDefault) {
                    editor.putInt("structPC.AppHrMin", CallHelper.Ds.structPC.AppHrMin);
                    editor.putInt("structPC.bWeekDay", CallHelper.Ds.structPC.bWeekDay);
                    editor.putLong("currentTimeMillis", System.currentTimeMillis());
                    editor.putInt("lSchoolSchedule.iCurrentDate", CallHelper.Ds.lSchoolSchedule.iCurrentDate);
                    editor.putInt("lSchoolSchedule.iCurrentMonth", CallHelper.Ds.lSchoolSchedule.iCurrentMonth);
                    editor.putInt("lSchoolSchedule.iCurrentYear", CallHelper.Ds.lSchoolSchedule.iCurrentYear);
                    editor.commit();
                }
            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                DeBug.ShowToast(context, "On Boot  : 3");
                DeBug.ShowToast(context, "On Boot  : 4");
                SharedPreferences settings1 = context.getSharedPreferences(CallDetectService.PREFS_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = settings1.edit();
                DeBug.ShowLog("TIMER", "onFinish");
                long cTime = System.currentTimeMillis();
                timediff = cTime - settings1.getLong("currentTimeMillis", cTime);
                timediff = timediff / 60000;
                DeBug.ShowToast(context, "On Boot  : " + timediff);
                context.startService(new Intent(context, CallDetectService.class));
                context.startService(new Intent(context, AppBlock.class));
                context.startService(new Intent(context, BatteryWifiInfoService.class)); //SIVA
                if (!Constants.isMyServiceRunning(context, DisableQuickSettingsService.class)) {
                    context.startService(new Intent(context, DisableQuickSettingsService.class)); //SIVA
                }
                bStartedAfterBoot = true;
            }
        } catch (Exception e) {
            DeBug.ShowLog("On Boot", "Received Exception: " + e.getMessage().toString());
            e.printStackTrace();
        }
    }
}