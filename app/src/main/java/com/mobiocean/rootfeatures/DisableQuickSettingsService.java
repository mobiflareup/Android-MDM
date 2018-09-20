package com.mobiocean.rootfeatures;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.mobiocean.util.DeBug;

import java.lang.reflect.Method;

/**
 * Created by SIVA on 03-10-17.
 */

public class DisableQuickSettingsService extends Service {
    Handler collapseNotificationHandler = null;
    Runnable runnable = null;
    private PowerManager.WakeLock wakeLock;
    PowerManager pm;
    static Context ctx;
    boolean checkBlockNoti = false;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        ctx = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
            wakeLock.acquire();
            settings = getSharedPreferences(PREFS_NAME, this.MODE_PRIVATE);
            editor = settings.edit();
            checkBlockNoti = settings.getBoolean(RootConstants.ISBLOCKQUICKSETTINGS, false);
            if (checkBlockNoti && pm.isScreenOn()) {

                DeBug.ShowLog("QUICKSETTINGS", "START_DISABLE");
                collapseNow();
                //collapseQuickSettings();
            } else {
                stopSelf(startId);
                DeBug.ShowLog("QUICKSETTINGS", "STOPSELF");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void collapseNow() {

        try {
            // Initialize 'collapseNotificationHandler'
            if (collapseNotificationHandler == null) {
                collapseNotificationHandler = new Handler();
            }

            // If window focus has been lost && activity is not in a paused state
            // Its a valid check because showing of notification panel
            // steals the focus from current activity's window, but does not
            // 'pause' the activity
            runnable = new Runnable() {
                @Override
                public void run() {
                    collapseQuickSettings();
                    // Check if the window focus has been returned
                    // If it hasn't been returned, post this Runnable again
                    // Currently, the delay is 100 ms. You can change this
                    // value to suit your needs.
                    collapseNotificationHandler.postDelayed(this, 100L);
                }
            };
            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(runnable, 300L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void collapseQuickSettings() {
        // Use reflection to trigger a method from 'StatusBarManager'

        Object statusBarService = getSystemService("statusbar");
        Class<?> statusBarManager = null;

        try {
            statusBarManager = Class.forName("android.app.StatusBarManager");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Method collapseStatusBar = null;

        try {
            // Prior to API 17, the method to call is 'collapse()'
            // API 17 onwards, the method to call is `collapsePanels()`

            if (Build.VERSION.SDK_INT > 16) {
                collapseStatusBar = statusBarManager.getMethod("collapsePanels");
            } else {
                collapseStatusBar = statusBarManager.getMethod("collapse");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        collapseStatusBar.setAccessible(true);
        DeBug.ShowLog("QUICKSETTINGS", "RUNNING");
        try {
            collapseStatusBar.invoke(statusBarService);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (collapseNotificationHandler != null && runnable != null)
                collapseNotificationHandler.removeCallbacks(runnable);
            if (wakeLock != null && wakeLock.isHeld())
                wakeLock.release();

            DeBug.ShowLog("QUICKSETTINGS", "DESTROY");
         /*   checkBlockNoti = settings.getBoolean(RootConstants.ISBLOCKQUICKSETTINGS, false);
            if (checkBlockNoti) { // check is enable
                startService(new Intent(ctx, DisableQuickSettingsService.class));
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
