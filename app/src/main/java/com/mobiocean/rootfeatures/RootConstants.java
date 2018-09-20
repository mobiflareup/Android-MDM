package com.mobiocean.rootfeatures;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.mobiocean.util.DeBug;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by SIVA on 05-05-17.
 */

public class RootConstants {
    public static final String ISBLOCKGPS = "blockGps";
    public static final String ISBLOCKUSB = "blockUsb";
    public static final String ISBLOCKNOTIFICATION = "blockNotification";
    public static final String ISBLOCKMOBILEDATA = "checkmobiledata";
    public static final String ISCHARGING = "ischarging";
    public static final String ISROOTDEVICE = "checkRootDevice";
    public static final String ISBLOCKQUICKSETTINGS = "blockQuickSettings";
    public static final long FIVAMINUTES = 300000;

    public static boolean checkRootMethod() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                DeBug.ShowLog("CHECKROOT", "true");
                return true;
            }
        }
        return false;
    }

    public static boolean checkRootMethod1() {
        boolean m1 = false;
        try {
            String buildTags = android.os.Build.TAGS;
            m1 = buildTags != null && buildTags.contains("test-keys");
            DeBug.ShowLog("CHECKROOT1", "" + m1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return m1;
    }

    public static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) {
                DeBug.ShowLog("CHECKROOT3", "true");
                return true;
            }
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static void notificationPermission(Context context) {
        try {

            String enabledAppList = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
            boolean temp = enabledAppList.contains("com.mobiocean");
            if (!temp) {
                context.startActivity(new Intent(
                        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }

        } catch (Exception ignore) {

        }
    }

}
