package com.mobiocean.rootfeatures;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.mobiocean.util.DeBug;

import org.sn.util.Constants;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationFileTransferBlockService extends NotificationListenerService {

    Context context;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;
    boolean checkBlockNotification = false;
    boolean checkBlockUsb = false;
    boolean checkCharging = false;

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String pack = sbn.getPackageName();
            String text = "";
            String title = "";

            checkBlockNotification = settings.getBoolean(RootConstants.ISBLOCKNOTIFICATION, false);
            checkBlockUsb = settings.getBoolean(RootConstants.ISBLOCKUSB, false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    Bundle extras = sbn.getNotification().extras;
                    title = extras.getString("android.title");
                    text = extras.getCharSequence("android.text").toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                if(title.toUpperCase().contains("USB for".toUpperCase()) || title.toUpperCase().contains("media".toUpperCase())) {
                    if (RootConstants.checkRootMethod() && checkBlockUsb)
                    superUsbBlock();
                }
            }

            try {
                DeBug.ShowLog("Package", pack + "    ID" + sbn.getId());
                DeBug.ShowLog("Title", title);
                DeBug.ShowLog("Text", text);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //cancelAllNotifications(); without id based use this method, clear all notification
            //superUsbUnBlock();


            if(title.toUpperCase().contains("USB for".toUpperCase())  || title.toUpperCase().contains("media".toUpperCase())) {
                if (title.toUpperCase().contains("charg".toUpperCase())) {
                    checkCharging = true;
                    DeBug.ShowLog("CHARGING", "TRUE");
                } else {
                    checkCharging = false;
                    DeBug.ShowLog("CHARGING", "FALSE");
                    //editor.putInt(RootConstants.ISCHARGING, 1);
                    // editor.commit();
                }
                if (RootConstants.checkRootMethod() && checkBlockUsb && !checkCharging) {
                    DeBug.ShowLog("CHARGING", "STARTSERVICE");
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        superUsbBlock();
                    } else {
                        if (!Constants.isMyServiceRunning(context, ChangeToChargingService.class))
                            startService(new Intent(this, ChangeToChargingService.class));
                    }
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            {
                if (checkBlockNotification)
                    cancelNotification(sbn.getPackageName(), sbn.getTag(), sbn.getId());
            } else
            {
                if (checkBlockNotification)
                    cancelNotification(sbn.getKey());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        //Log.i("Msg", "Notification was removed");
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void superUsbMtp() {

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            DataInputStream inputStream = new DataInputStream(process.getInputStream());

            outputStream.writeBytes("setprop persist.sys.usb.config mtp" + "\n"); //setprop persist.sys.usb.config mtp,ptp,adb            outputStream.flush(); //setprop persist.sys.usb.config adb

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void superUsbPtp() {

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            DataInputStream inputStream = new DataInputStream(process.getInputStream());

            outputStream.writeBytes("setprop persist.sys.usb.config ptp" + "\n"); //setprop persist.sys.usb.config mtp,ptp,adb            outputStream.flush(); //setprop persist.sys.usb.config adb

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void superUsbUnBlock() {

        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            DataInputStream inputStream = new DataInputStream(process.getInputStream());

            outputStream.writeBytes("setprop persist.sys.usb.config mtp,ptp,adb" + "\n"); //setprop persist.sys.usb.config mtp,ptp,adb            outputStream.flush(); //setprop persist.sys.usb.config adb

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void check() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo 0 > /sys/bus/usb/drivers/usb/usb1/bConfigurationValue",});
            p = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo 1 > /sys/bus/usb/drivers/usb/usb1/bConfigurationValue",});

            Process p1 = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo 0 > /sys/bus/usb/drivers/usb/usb1/authorized",});
            p1 = Runtime.getRuntime().exec(new String[]{"su", "-c", "echo 1 > /sys/bus/usb/drivers/usb/usb1/authorized",});

   /*         try {
                ProcessBuilder pb = new ProcessBuilder(new String[]{"su", "-c","setprop adb"});
                pb.redirectErrorStream(true).start();

            } catch (Exception e) {
                e.printStackTrace();
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}