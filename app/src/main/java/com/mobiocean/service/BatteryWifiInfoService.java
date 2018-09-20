package com.mobiocean.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.mobiocean.rootfeatures.RootConstants;
import com.mobiocean.ui.Connectivity;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.NetworkUtil;
import com.mobiocean.util.RestApiCall;

import org.conveyance.configuration.RConstant;
import org.json.JSONException;
import org.json.JSONObject;
import org.sn.location.LocationDetails;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by SIVA on 08-09-17.
 */

public class BatteryWifiInfoService extends Service {
    private PowerManager.WakeLock wakeLock;
    PowerManager pm;
    Handler handler;
    private Runnable runnable;
    static Context ctx;
    //BAttery info
    String technology = "";
    int voltage = 0;
    int temperature = 0;
    String batteryHealth = "";
    String batterystatus = "";
    String value = "";
    RestApiCall restApiCall;

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
            DeBug.ShowLog("BATTERYY", "START");
            pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
            wakeLock.acquire();
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    DeBug.ShowLog("BATTERYY", "RUNNING");
                    if (org.sn.location.NetworkUtil.NetworkStatus.NO_NET != org.sn.location.NetworkUtil.getConnectivityStatus(ctx)) {
                        batteryInformation();
                    }
                    handler.postDelayed(this, RootConstants.FIVAMINUTES);
                }
            };
            handler.postDelayed(runnable, RootConstants.FIVAMINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        DeBug.ShowLog("BATTERYY", "STOP");
        super.onDestroy();
        try {
            if (handler != null && runnable != null)
                handler.removeCallbacks(runnable);
            if (wakeLock != null && wakeLock.isHeld())
                wakeLock.release();
            startService(new Intent(ctx, BatteryWifiInfoService.class));
            DeBug.ShowLog("BATTERYY", "AGAINSTART");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void batteryInformation() {
        try {

            String CurrentTime = "" + System.currentTimeMillis();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Long.parseLong(CurrentTime));
            DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
            String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

            Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int percent = (level * 100) / scale;

            technology = batteryIntent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
            int health = batteryIntent.getIntExtra("health", 0);
            int status = batteryIntent.getIntExtra("status", 0);
            voltage = batteryIntent.getIntExtra("voltage", 0);
            temperature = batteryIntent.getIntExtra("temperature", 0);

            batteryHealth = getHealthString(health);
            batterystatus = getStatusString(status);
            value = String.valueOf(percent) + "%";
            Toast.makeText(ctx, "Battery Percentage" + value, Toast.LENGTH_SHORT).show();


            JSONObject jsonBatteryInfo = new JSONObject();
            try {
                jsonBatteryInfo.put("AppId", CallHelper.Ds.structPC.iStudId);
                jsonBatteryInfo.put("Voltage", voltage);
                jsonBatteryInfo.put("Temperature", temperature);
                jsonBatteryInfo.put("BatteryPercent", value);
                jsonBatteryInfo.put("BatteryStatus", batterystatus);
                jsonBatteryInfo.put("BatteryHealth", batteryHealth);
                jsonBatteryInfo.put("Technology", technology);
                jsonBatteryInfo.put("LogDateTime", SMSTimeStamp);
                new SendBatteryInfo().execute(jsonBatteryInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SendBatteryInfo extends AsyncTask<JSONObject, Void, String> {

        @Override
        protected String doInBackground(JSONObject... params) {

            restApiCall = new RestApiCall();
            return restApiCall.uploadBatteryInfo(params[0]);
        }
    }

    private String getHealthString(int health) {
        String healthString = "Unknown";
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                healthString = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                healthString = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                healthString = "Over Voltage";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                healthString = "Over Heat";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                healthString = "Failure";
                break;
        }
        return healthString;
    }

    private String getStatusString(int status) {
        String statusString = "Unknown";
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                statusString = "Charging";
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                statusString = "Discharging";
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                statusString = "Full";
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                statusString = "Not Charging";
                break;
        }
        return statusString;
    }

    public static void getNetworkInfo() {    // wifi connectivity
        boolean ConnectedWifi;
        boolean ConnectedMobile;
        String connectivityName = "";
        String connectivityType = "";
        boolean Roaming;
        boolean IsConnectedToProvisioningNetwork;
        String NetworkTypeInfo = "";
        String roaming = "";
        String isconnectedtoprovisioningnetwork = "";
        NetworkInfo.DetailedState State;
        String status = "";
        String connectedWifi = "";
        String connectednetwork = "";


        NetworkInfo info = Connectivity.getNetworkInfo(ctx);

        if (info != null && !info.equals("")) {
            ConnectedWifi = Connectivity.isConnectedWifi(ctx);
            ConnectedMobile = Connectivity.isConnectedMobile(ctx);
            Roaming = info.isRoaming();
            IsConnectedToProvisioningNetwork = info.isConnectedOrConnecting();
            State = Connectivity.connectivityInfo(ctx).getDetailedState();
            status = NetworkUtil.getConnectivityStatusString(ctx);
            NetworkTypeInfo = info.getTypeName();
            connectivityName = info.getExtraInfo();
            connectivityType = Connectivity.connectivityInfo(ctx).getTypeName();

            if (TextUtils.isEmpty(connectivityName)) {
                connectivityName = "";
            } else {
                connectivityName = connectivityName.replaceAll("[^a-zA-Z]", "");
            }

            if (ConnectedWifi == true) {
                connectedWifi = "Yes";
            } else {
                connectedWifi = "No";
            }

            if (ConnectedMobile == true) {
                connectednetwork = "Yes";
            } else {
                connectednetwork = "No";
            }

            if (Roaming == true) {
                roaming = "Yes";
            } else {
                roaming = "No";
            }

            if (IsConnectedToProvisioningNetwork == true) {
                isconnectedtoprovisioningnetwork = "Yes";
            } else {
                isconnectedtoprovisioningnetwork = "No";
            }

            if (TextUtils.isEmpty(connectivityType)) {
                connectivityType = "";
            } else {
                connectivityType = Connectivity.connectivityInfo(ctx).getTypeName();
            }

        } else {

        }

    }
}
