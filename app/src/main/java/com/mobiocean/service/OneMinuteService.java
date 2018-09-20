package com.mobiocean.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import org.conveyance.configuration.RConstant;
import org.conveyance.database.RStartStopLocation;
import org.sn.location.LocationDetails;

public class OneMinuteService extends Service {

    private Context context;
    private Runnable runnable;
    public RStartStopLocation locationTableAccess;
    private SharedPreferences settings;
    private final String PREFS_NAME = "MyPrefsFile";
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    public static boolean isPermitted = false;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        context = this;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
        wakeLock.acquire();
        locationTableAccess = new RStartStopLocation(context);
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                {
                    LocationDetails locationDetails = new LocationDetails(context);
                    locationDetails.conveyance(2);
                }
                if (settings.getBoolean("Conveyance.isStarted", false)) {
                    handler.postDelayed(this, RConstant.DEFAULT_DELAY);
                } else {
                    if (handler != null && runnable != null)
                        handler.removeCallbacks(runnable);
                    if (wakeLock != null && wakeLock.isHeld())
                        wakeLock.release();
                    stopSelf(startId);
                }
            }
        };
        handler.postDelayed(runnable, RConstant.DEFAULT_DELAY);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null)
            handler.removeCallbacks(runnable);
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
        if (settings.getBoolean("Conveyance.isStarted", false)) {
            getBaseContext().startService(new Intent(getBaseContext(), OneMinuteService.class));
        }
    }

}
