package org.conveyance.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import org.conveyance.configuration.RConstant;
import org.conveyance.configuration.RHelper;
import org.conveyance.configuration.RSharedData;
import org.conveyance.database.RStartStopLocation;
import org.sn.location.LocationDetails;

public class RGetTowerLocationService extends Service {

    private Context context;
    private Handler handler;
    private Runnable runnable;
    public RStartStopLocation locationTableAccess;
    private RSharedData settings;
    private RHelper helper = new RHelper();
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        context = this;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
        wakeLock.acquire();
        locationTableAccess = new RStartStopLocation(context);
        settings = new RSharedData(context);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                {
                    LocationDetails locationDetails = new LocationDetails(context);
                    locationDetails.travelAllowance();
                }
                if (settings.getStatus()) {
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
        if (settings.getStatus()) {
            Intent restartMe = new Intent(context, RGetTowerLocationService.class);
            startService(restartMe);
        }
    }
}