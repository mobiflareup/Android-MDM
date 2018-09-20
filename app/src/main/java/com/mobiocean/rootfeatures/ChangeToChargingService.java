package com.mobiocean.rootfeatures;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.mobiocean.util.DeBug;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by SIVA on 09-05-17.
 */

public class ChangeToChargingService extends Service {
    Handler handler;
    Runnable r;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            settings = getSharedPreferences(PREFS_NAME, this.MODE_PRIVATE);
            editor = settings.edit();

            handler = new Handler();
            r = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    //if (settings.getInt(RootConstants.ISCHARGING, 0) == 0) // 0 mean is charging
                    superUsbBlock();
                    DeBug.ShowLog("CHARGING", "RUNNING");
                   // Toast.makeText(ChangeToChargingService.this, "STOP MTP", Toast.LENGTH_SHORT).show();
                    handler.postDelayed(r, 10000);
                }
            };
            handler.postDelayed(r, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        try {
            DeBug.ShowLog("CHARGING","DESTROY1");
            handler.removeCallbacks(r);
            stopSelf();
            DeBug.ShowLog("CHARGING","DESTROY");
        } catch (Exception e) {
            DeBug.ShowLog("CHARGING", ""+e.getMessage());
            e.printStackTrace();
        }
        super.onDestroy();

    }

    private void superUsbBlock() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            DataInputStream inputStream = new DataInputStream(process.getInputStream());

            outputStream.writeBytes("setprop persist.sys.usb.config adb" + "\n"); //setprop persist.sys.usb.config mtp,ptp,adb
            outputStream.flush(); //setprop persist.sys.usb.config adb

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();

            //editor.putInt(RootConstants.ISCHARGING, 0);
            //editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
