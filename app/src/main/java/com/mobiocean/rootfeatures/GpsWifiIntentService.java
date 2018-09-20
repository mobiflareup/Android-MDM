package com.mobiocean.rootfeatures;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mobiocean.util.DeBug;

import java.io.DataOutputStream;

/**
 * Created by SIVA on 05-05-17.
 */

public class GpsWifiIntentService extends IntentService {
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;
    boolean checkBlockGps = false;

    public GpsWifiIntentService() {
        super("GPSONSERVICE");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        settings = getSharedPreferences(PREFS_NAME, this.MODE_PRIVATE);
        editor = settings.edit();
        int type = intent.getIntExtra("PUTFEATURETYPE", 0);
        if (type == 1) {
            checkBlockGps = settings.getBoolean(RootConstants.ISBLOCKGPS, false);
            if (checkBlockGps && RootConstants.checkRootMethod())
                GpsOnInRoot();
        }
    }

    private void GpsOnInRoot() {

        String[] cmds = {"cd /system/bin", "settings put secure location_providers_allowed +gps"};
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
            DeBug.ShowLog("GPSONWORKING", "ok");
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
}
