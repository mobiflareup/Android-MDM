package com.mobiocean.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.AudioRecord;
import android.net.ConnectivityManager;
import android.net.VpnService;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.mobiocean.mobidb.ApplicationInfoDB;
import com.mobiocean.receiver.DemoDeviceAdminReceiver;
import com.mobiocean.rootfeatures.RootConstants;
import com.mobiocean.thread.DeviceAdminThread;
import com.mobiocean.ui.Lock;
import com.mobiocean.ui.eFeatureSetting;
import com.mobiocean.ui.ePhoneMode;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.eFeatureControl;

import org.conveyance.configuration.RSharedData;
import org.conveyance.services.RGetTowerLocationService;
import org.sn.activities.GpsOffActivity;
import org.sn.activities.StartVpnActivity;
import org.sn.beans.SensorListBean;
import org.sn.database.SensorListTable;
import org.sn.services.MobiVpnService;
import org.sn.util.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.mobiocean.util.Constant.MIN_LUT;

public class AppBlock extends Service {
    //Log Tag Filters
    protected static String Tag = "Task";
    protected static String Tag1 = "GingerBox";
    //Know the Package name of any particular Application
    protected String packageName;
    //Know the Class name of any particular Package
    protected String ClassName;
    //To check WiFi
    protected WifiManager WManager;
    //Handler for the 1 second thread
    protected Handler handler;
    //Handler to start the above every 1 second
    protected Handler handlerForRunnable;
    //To Check Bluetooth
    protected BluetoothAdapter mBluetoothAdapter;
    //To Check Mobile Data Connectivity Assume ENABLED
    protected Object mobileDataEnabled;
    //Audio Manager to check media activtiy either for Music(AD) or Radio(RD)
    protected static AudioManager audioManager1;
    //Gain focus from the current audio
    protected OnAudioFocusChangeListener afChangeListener;
    //WeekDay
    protected static int iWeekDay;
    protected ActivityManager am;
    protected int Hr;
    protected int Min;

    protected static DevicePolicyManager devicePolicyManager;
    protected static ComponentName demoDeviceAdmin;
    public static boolean isOutgoing = false;
    protected static final int ACTIVATION_REQUEST = 47; // identifies our request id

    protected String CurrentappInSchoolMode = "";

    //DeviceAdmin flag
    public static boolean DeviceAdminThread_Started = false;

    public static HashMap<Integer, Integer> usedTimeCounter = new HashMap<Integer, Integer>();
    public static HashMap<Integer, String> lastUsedTime = new HashMap<Integer, String>();

    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    boolean isCurrentlyBlockedInTime = false;
    ApplicationInfoDB mApplicationInfoDB;
    public static boolean isGpsAlertOn = false;
    private boolean micMuted = false;
    private AudioManager audioManagerMic;
    private AudioRecord audioRecordMic;
    int recordMicCount = 0;
    private int scanCounter = 0;

    Context context;
    String BSSID;
    String networkSSID = "";
    String networkPass = "";
    WifiManager wifiManager;
    WifiConfiguration conf;
    List<SensorListBean> sensorBeans = new ArrayList<>();
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        try {
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
            wakeLock.acquire();
            WManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);

            mApplicationInfoDB = ApplicationInfoDB.getInstance(this);
            handlerForRunnable = new Handler();
            context = getBaseContext();
            handler = new Handler() {

                private void checkApps() {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        if (WindowChangeDetectingService.packageName != null && !WindowChangeDetectingService.packageName.isEmpty())
                            packageName = WindowChangeDetectingService.packageName;
                        if (WindowChangeDetectingService.ClassName != null && !WindowChangeDetectingService.ClassName.isEmpty())
                            ClassName = WindowChangeDetectingService.ClassName;
                    }
                    DeBug.ShowLog("NarayananABP", "PackageName : ClassName " + packageName + " : " + ClassName);
                    DeBug.ShowLog(Tag1, "group " + mApplicationInfoDB.getAppGroup(packageName) + " index " + mApplicationInfoDB.getAppIndex(packageName) + " size " + CallHelper.Ds.structACC.listGroupIds.size());

                    if (!packageName.contains("mobiocean"))
                        for (int group = 0; group < CallHelper.Ds.structACC.listGroupIds.size(); group++) {
                            if ((CallHelper.Ds.structACC.lAllowedTime[iWeekDay][group][Hr] & MIN_LUT[Min]) != 0) {
                                DeBug.ShowLog("NarayananAB", "APPBLOCK CHECK " + group);
                                if (CallHelper.Ds.structACC.bPresentlyStopped[iWeekDay][group]) {
                                    DeBug.ShowLog(Tag1, "GM Not ALLowed  " + packageName);
                                    if (mApplicationInfoDB.checkIsinGroup(Integer.parseInt(CallHelper.Ds.structACC.listGroupIds.get(group)), packageName)) {
                                        DeBug.ShowLog(Tag1, "GM Not ALLowed  " + packageName);
                                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                                        startMain.addCategory(Intent.CATEGORY_HOME);
                                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(startMain);
                                        DeBug.ShowToast(AppBlock.this, "You Are In Jail . . .GM\n" + packageName);
                                    }
                                } else {
                                    if (CallHelper.Ds.structACC.bTimeDurationCtrl[iWeekDay][group] == 0) //for Game Block
                                    {
                                        CallHelper.Ds.structACC.bPresentlyStopped[iWeekDay][group] = false;
                                        CallHelper.Ds.structACC.bPresentlyOn[group] = 3;
                                    } else {
                                        isCurrentlyBlockedInTime = false;
                                        if ((CallHelper.Ds.structACC.bTimeDurationCtrl[iWeekDay][group] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME && (CallHelper.Ds.structACC.bTimeDurationCtrl[iWeekDay][group] & eFeatureSetting.RESTRICTED_IN_DURATION) != eFeatureSetting.RESTRICTED_IN_DURATION) {
                                            if ((CallHelper.Ds.structACC.lAllowedTime[iWeekDay][group][Hr] & MIN_LUT[Min]) != 0) {
                                                isCurrentlyBlockedInTime = true;
                                                if (mApplicationInfoDB.checkIsinGroup(Integer.parseInt(CallHelper.Ds.structACC.listGroupIds.get(group)), packageName)) {
                                                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                                                    startMain.addCategory(Intent.CATEGORY_HOME);
                                                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(startMain);
                                                    DeBug.ShowToast(AppBlock.this, "You Are In Jail . . .GM \n" + packageName);
                                                    DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_CLOCK_TIME .  GM\n" + CallHelper.Ds.structPC.AppHrMin);
                                                }
                                            }
                                            DeBug.ShowLog(Tag1, "" + packageName + "   HR " + Hr + " Min " + Min);

                                        }

                                        if (!isCurrentlyBlockedInTime)
                                            if ((CallHelper.Ds.structACC.bTimeDurationCtrl[iWeekDay][group] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                                                if (mApplicationInfoDB.checkIsinGroup(Integer.parseInt(CallHelper.Ds.structACC.listGroupIds.get(group)), packageName)) {
                                                    CallHelper.Ds.structACC.wUsedDuration[iWeekDay][group]++;
                                                    CallHelper.Ds.structACC.bPresentlyOn[group] = 1;
                                                    DeBug.ShowLog("NarayananAB", "RESTRICTED_IN_DURATION . .  AB\n" + CallHelper.Ds.structACC.wUsedDuration[iWeekDay][group]);
                                                }
                                                if (CallHelper.Ds.structACC.wUsedDuration[iWeekDay][group] / 60 >= CallHelper.Ds.structACC.wTotalDuration[iWeekDay][group]) {
                                                    // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                                    //1. when the activity is being stopped then it saves the current duration,
                                                    //2. if the duration has expired then stops the activity and save the current duration.
                                                    CallHelper.Ds.structACC.bPresentlyStopped[iWeekDay][group] = true;
                                                    CallHelper.Ds.structACC.bPresentlyOn[group] = 3;
                                                    DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_DURATION . .  GM\n" + CallHelper.Ds.structACC.wUsedDuration[iWeekDay][group]);
                                                }
                                            }
                                    }
                                }
                            } else {
                                DeBug.ShowLog("NarayananAB", "APPBLOCK OPEN " + group);
                            }
                        }
                }

                private void blockNewInstalledApps() {
                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_NEW_APP][Hr] & MIN_LUT[Min]) != 0) {
                        DeBug.ShowLog("NarayananIB", "APPINSTALLBLOCK CHECK");
                        if (packageName.contains("installer")) {
                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                            startMain.addCategory(Intent.CATEGORY_HOME);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(startMain);
                            DeBug.ShowToast(AppBlock.this, "You Are In Jail . . .GM \n" + packageName);
                            DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_CLOCK_TIME .  GM\n" + CallHelper.Ds.structPC.AppHrMin);
                        }
                    } else {
                        DeBug.ShowLog("NarayananIB", "APPINSTALLBLOCK OPEN");
                    }
                }

                private void blockPlayStore() {
                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]) {
                        if (packageName.equalsIgnoreCase("com.android.vending")) {
                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                            startMain.addCategory(Intent.CATEGORY_HOME);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(startMain);

                            DeBug.ShowToast(AppBlock.this, "You Are In Jail . . .GM \n" + packageName);
                            DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_CLOCK_TIME .  GM\n" + CallHelper.Ds.structPC.AppHrMin);
                            DeBug.ShowLog("features", " WF bPresentlyStopped is TRUE. EndTime 0 " + CallHelper.Ds.structFCC.wEndTime[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]);
                        }
                    } else {
                        DeBug.ShowLog("features", "Day " + iWeekDay + " AppHrMin " + CallHelper.Ds.structPC.AppHrMin);

                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] == 0) {
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_PLAYSTORE] = 3;
                        } else {
                            isCurrentlyBlockedInTime = false;
                            DeBug.ShowLogD("features", "WF lAllowedTime" + Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE][Hr]));
                            if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {

                                if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE][Hr] & MIN_LUT[Min]) != 0) {
                                    isCurrentlyBlockedInTime = true;
                                    if (packageName.equalsIgnoreCase("com.android.vending")) {
                                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                                        startMain.addCategory(Intent.CATEGORY_HOME);
                                        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(startMain);
                                        DeBug.ShowToast(AppBlock.this, "You Are In Jail . . .GM \n" + packageName);
                                        DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_CLOCK_TIME .  GM\n" + CallHelper.Ds.structPC.AppHrMin);
                                        DeBug.ShowToast(AppBlock.this, "WF RESTRICTED_IN_CLOCK_TIME . WF . 1\n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]);
                                    }
                                }
                                DeBug.ShowLog(Tag1, "" + packageName + "   HR " + Hr + " Min " + Min);

                            }

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                                    if ((packageName.equalsIgnoreCase("com.android.vending")) && !CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]) {
                                        CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]++;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_PLAYSTORE] = 1;
                                        DeBug.ShowLog("features", "WF RESTRICTED_IN_DURATION " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]);

                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.
                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_PLAYSTORE] = 3;
                                        DeBug.ShowLog("features", "WF RESTRICTED_IN_DURATION . Used  Total.   " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]);
                                        DeBug.ShowToast(AppBlock.this, "WF RESTRICTED_IN_DURATION . WF . \n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE]);
                                    }
                                }
                        }
                    }
                }

                //Narayanan GPS BUZZER
                public void checkGps() {
                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGPS_BUZZER]) {
                        if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGPS_BUZZER] & eFeatureSetting.RESTRICTED_IN_DURATION_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_DURATION_CLOCK_TIME) {
                            if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iGPS_BUZZER][Hr] & MIN_LUT[Min]) != 0) {
                                LocationManager manager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
                                if (manager != null) {
                                    boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                    if (!statusOfGPS & !isGpsAlertOn) {
                                        isGpsAlertOn = true;
                                        Intent i = new Intent(getApplicationContext(), GpsOffActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        DeBug.ShowLog("NarayananGPS", " Check GPS bPresentlyStopped is TRUE. EndTime 0 " + CallHelper.Ds.structFCC.wEndTime[iWeekDay][eFeatureControl.iGPS_BUZZER]);
                                    }
                                }
                            }
                        } else if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGPS_BUZZER] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                            LocationManager manager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
                            if (manager != null) {
                                boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                if (!statusOfGPS & !isGpsAlertOn) {
                                    isGpsAlertOn = true;
                                    Intent i = new Intent(getApplicationContext(), GpsOffActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                    DeBug.ShowLog("NarayananGPS", " Check GPS bPresentlyStopped is TRUE. EndTime 0 " + CallHelper.Ds.structFCC.wEndTime[iWeekDay][eFeatureControl.iGPS_BUZZER]);
                                }
                            }
                        }
                    } else {
                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGPS_BUZZER] == 0) {
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGPS_BUZZER] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iGPS_BUZZER] = 3;
                        } else {
                            isCurrentlyBlockedInTime = false;
                            if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGPS_BUZZER] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGPS_BUZZER] & eFeatureSetting.RESTRICTED_IN_DURATION) != eFeatureSetting.RESTRICTED_IN_DURATION) {
                                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iGPS_BUZZER][Hr] & MIN_LUT[Min]) != 0) {
                                        isCurrentlyBlockedInTime = true;
                                        LocationManager manager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
                                        if (manager != null) {
                                            boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                            if (!statusOfGPS & !isGpsAlertOn) {
                                                isGpsAlertOn = true;
                                                Intent i = new Intent(getApplicationContext(), GpsOffActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(i);
                                                DeBug.ShowLog("NarayananGPS", "Check GPS RESTRICTED_IN_CLOCK_TIME \n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGPS_BUZZER] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGPS_BUZZER]);
                                            }
                                        }
                                    }
                                }

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGPS_BUZZER] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                                    LocationManager manager = (LocationManager) getBaseContext().getSystemService(Context.LOCATION_SERVICE);
                                    if (manager != null) {
                                        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                        if (!statusOfGPS) {
                                            CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGPS_BUZZER]++;
                                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iGPS_BUZZER] = 1;
                                            DeBug.ShowLog("NarayananGPS", "WF RESTRICTED_IN_DURATION " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGPS_BUZZER] + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGPS_BUZZER]);
                                        }
                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGPS_BUZZER] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGPS_BUZZER]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.
                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGPS_BUZZER] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iGPS_BUZZER] = 3;
                                        DeBug.ShowLog("NarayananGPS", "Check GPS RESTRICTED_IN_DURATION . Used  Total.   " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGPS_BUZZER] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGPS_BUZZER]);
                                    }
                                }
                        }
                    }
                }

                //Narayanan BLOCK RECORDING
                public void checkMic() {
                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_VOICE_RECORD][Hr] & MIN_LUT[Min]) != 0) {
                        audioManagerMic = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
                        audioManagerMic.setMode(AudioManager.MODE_IN_CALL);
                        audioManagerMic.setMicrophoneMute(true);
                        micMuted = true;
                        try {
                            if (recordMicCount == 0) {
                                audioRecordMic = new AudioRecord(1, 11025, 16, 2, 5000);
                                audioRecordMic.startRecording();
                            } else if (recordMicCount == 5) {
                                if (audioRecordMic != null) {
                                    audioRecordMic.stop();
                                    audioRecordMic.release();
                                    audioRecordMic = null;
                                }
                                audioRecordMic = new AudioRecord(1, 11025, 16, 2, 5000);
                                audioRecordMic.startRecording();
                                recordMicCount = 0;
                            }
                            recordMicCount++;
                            DeBug.ShowLog("NarayananCM", "Count mic : " + recordMicCount);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            recordMicCount = 0;
                        }
                        editor.putBoolean("AppBlock.micMuted", micMuted);
                        editor.commit();
                    } else {
                        if (settings.getBoolean("AppBlock.micMuted", micMuted)) {
                            try {
                                if (audioManagerMic != null) {
                                    audioRecordMic.stop();
                                    audioRecordMic.release();
                                    audioRecordMic = null;
                                    recordMicCount = 0;
                                }
                            } catch (Exception e) {
                                recordMicCount = 0;
                                audioRecordMic = null;
                            }
                            micMuted = false;
                            editor.putBoolean("AppBlock.micMuted", micMuted);
                            editor.commit();
                        }
                    }
                }


                //To Check Camera (CM)
                private void checkCamera() {
                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_CAMERA][Hr] & MIN_LUT[Min]) != 0) {
                        if (!isCameraDisabled()) {
                            isCurrentlyBlockedInTime = true;
                            DeBug.ShowLog(Tag1, "" + packageName);
                            if (isCameraRunning()) {
                                Intent startMain = new Intent(Intent.ACTION_MAIN);
                                startMain.addCategory(Intent.CATEGORY_HOME);
                                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(startMain);
                            }
                            disableCamera();
                            DeBug.ShowLog("Camera", "RESTRICTED_IN_CLOCK_TIME . . .CM \n" + packageName + CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_CAMERA]);
                        }
                    } else {
                        if (isCameraDisabled())
                            enableCamera();
                    }
                }

                private boolean isCameraRunning() {
                    boolean result = false;
                    Camera camera;
                    try {
                        camera = Camera.open();
                        camera.release();
                        result = false;
                    } catch (Exception e) {
                        result = true;
                    }
                    return result;
                }

                protected void disableCamera() {
                    DeBug.ShowLog("TusharCamera", "camera disabled  = " + devicePolicyManager.getCameraDisabled(demoDeviceAdmin));

                    if (!devicePolicyManager.getCameraDisabled(demoDeviceAdmin)) {
                        devicePolicyManager.setCameraDisabled(demoDeviceAdmin, true);
                    }
                }

                protected void enableCamera() {
                    DeBug.ShowLog("Camera", "camera disabled  = " + devicePolicyManager.getCameraDisabled(demoDeviceAdmin));

                    if (devicePolicyManager.getCameraDisabled(demoDeviceAdmin)) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        devicePolicyManager.setCameraDisabled(demoDeviceAdmin, false);
                        DeBug.ShowLog("Camera", "camera disabled  = " + devicePolicyManager.getCameraDisabled(demoDeviceAdmin));
                    }
                }

                boolean isCameraDisabled() {
                    return devicePolicyManager.getCameraDisabled(demoDeviceAdmin);
                }

                private void checkWifiHotSopt() {
                    DeBug.ShowLog("features", " WF bPresentlyStopped" + CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]);
                    DeBug.ShowLogD("features", "WF lAllowedTime" + Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT][Hr]));

                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]) {
                        if (isWifiHotSpotOn()) {
                            stopWiFiHotSopt();
//						WManager.setWifiEnabled(true);
                            DeBug.ShowLog("features", " WF bPresentlyStopped is TRUE. EndTime 0 " + CallHelper.Ds.structFCC.wEndTime[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]);
                        }
                    } else {
                        DeBug.ShowLog("features", "Day " + iWeekDay + " AppHrMin " + CallHelper.Ds.structPC.AppHrMin);

                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] == 0) {
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_HOTSPOT] = 3;
                        } else {
                            isCurrentlyBlockedInTime = false;
                            DeBug.ShowLogD("features", "WF lAllowedTime" + Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT][Hr]));

                            //NARAYANAN

//						if((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME)
//						{
//
//							if((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT][Hr] & MIN_LUT[Min]) != 0)
//							{
//								isCurrentlyBlockedInTime = true;
//								if(isWifiHotSpotOn())
//								{
//									stopWiFiHotSopt();
//									WManager.setWifiEnabled(true);
//									DeBug.ShowToast(AppBlock.this, "WF RESTRICTED_IN_CLOCK_TIME . WF . 1\n"+CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]/60 +" "+  CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]);
//								}
//							}
//							DeBug.ShowLog(Tag1,""+ packageName +"   HR "+Hr +" Min "+Min);
//
//						}

                            //NARAYANAN

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                                    if ((isWifiHotSpotOn()) && !CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]) {
                                        CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]++;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_HOTSPOT] = 1;
                                        DeBug.ShowLog("WiFi_HS", "WF RESTRICTED_IN_DURATION " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]);

                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.
                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_HOTSPOT] = 3;
                                        DeBug.ShowLog("WiFi_HS", "WF RESTRICTED_IN_DURATION . Used  Total.   " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]);
                                        DeBug.ShowToast(AppBlock.this, "WF RESTRICTED_IN_DURATION . WF . \n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT]);
                                    }
                                }
                        }
                    }
                }

                private void stopWiFiHotSopt() {
                    try {

                        Method[] wmMethods = WManager.getClass().getDeclaredMethods();

                        for (Method method : wmMethods) {
                            if (method.getName().equals("setWifiApEnabled")) {
                                try {
                                    method.invoke(WManager, null, false);
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                public boolean isWifiHotSpotOn() {

                    try {
                        Method method = WManager.getClass().getDeclaredMethod("isWifiApEnabled");
                        method.setAccessible(true);
                        return (Boolean) method.invoke(WManager);
                    } catch (Throwable ignored) {
                    }
                    return false;
                }


                // To Check WiFi (WF) (Hardware m
                private void checkWifi() {
                    DeBug.ShowLog("wifi", " WF bPresentlyStopped" + CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_WIFI]);
                    DeBug.ShowLogD("wifi", "WF lAllowedTime" + Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_WIFI][Hr]));

                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_WIFI]) {

                        if (WManager.isWifiEnabled()) {
                            WManager.setWifiEnabled(false);
                            DeBug.ShowLog("features", " WF bPresentlyStopped is TRUE. EndTime 0 " + CallHelper.Ds.structFCC.wEndTime[iWeekDay][eFeatureControl.iBLOCK_WIFI]);
                        }
                    } else {
                        DeBug.ShowLog("features", "Day " + iWeekDay + " AppHrMin " + CallHelper.Ds.structPC.AppHrMin);

                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_WIFI] == 0) {
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_WIFI] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_WIFI] = 3;
                        } else {
                            isCurrentlyBlockedInTime = false;
                            DeBug.ShowLogD("features", "WF lAllowedTime" + Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_WIFI][Hr]));

                            //NARAYANAN

//						if((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_WIFI] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME)
//						{
//
//							if((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_WIFI][Hr] & MIN_LUT[Min]) != 0)
//							{
//								isCurrentlyBlockedInTime = true;
//								if(WManager.isWifiEnabled())
//								{
//									WManager.setWifiEnabled(false);
//									DeBug.ShowToast(AppBlock.this, "WF RESTRICTED_IN_CLOCK_TIME . WF . 1\n"+CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]/60 +" "+  CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]);
//								}
//							}
//							DeBug.ShowLog(Tag1,""+ packageName +"   HR "+Hr +" Min "+Min);
//
//						}

                            //NARAYANAN

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_WIFI] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                                    if ((WManager.isWifiEnabled()) && !CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_WIFI]) {
                                        CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]++;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_WIFI] = 1;
                                        DeBug.ShowLog("wifi", "WF RESTRICTED_IN_DURATION " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI] + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]);

                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.
                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_WIFI] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_WIFI] = 3;
                                        DeBug.ShowLog("wifi", "WF RESTRICTED_IN_DURATION . Used  Total.   " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]);
                                        DeBug.ShowToast(AppBlock.this, "WF RESTRICTED_IN_DURATION . WF . \n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]);
                                    }
                                }
                        }
                    }
                }

                private void setMobileDataEnabled(boolean enabled) {
                    try {

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            final ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            final Class conmanClass = Class.forName(conman.getClass().getName());
                            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
                            iConnectivityManagerField.setAccessible(true);
                            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
                            final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                            final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                            setMobileDataEnabledMethod.setAccessible(true);
                            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
                        } else {
                            try {
                                TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                Method methodSet = Class.forName(tm.getClass().getName()).getDeclaredMethod("setDataEnabled", Boolean.TYPE);
                                methodSet.invoke(tm, true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

          /*      if (RootConstants.checkRootMethod()) {  //SIVA mobile data enable in root device only
                    try {
                        Runtime.getRuntime().exec("su -c svc data enable");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/
                }

                //To Check Mobile Data Connectivity (MC)
            /*private void checkDataConnectivity()
            {
				DeBug.ShowLog("DataConnection", "Dataconnection "+"1");

				if(CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION])
				{
					DeBug.ShowLog("DataConnection", "Dataconnection "+"2");
					ConnectivityManager dataManager;
					dataManager  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
					Method dataMtd;
					try {
						dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
						dataMtd.setAccessible(true);
						mobileDataEnabled=dataMtd.invoke(dataManager, false);
						DeBug.ShowLog("AppBlock", "bTimeDurationCtrl blocked ");
					} catch (NoSuchMethodException e) {

						e.printStackTrace();
					} catch (IllegalArgumentException e) {

						e.printStackTrace();
					} catch (IllegalAccessException e) {

						e.printStackTrace();
					} catch (InvocationTargetException e) {

						e.printStackTrace();
					}
				}else
				{
					isCurrentlyBlockedInTime = false;
					if(CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iDATA_CONNECTION]==0) //Fot DataConnectivity Block  
					{
						DeBug.ShowLog("DataConnection", "Dataconnection "+"3");
						CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION] = false;
						CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iDATA_CONNECTION] = 3;
					}
					else
					{
						DeBug.ShowLog("DataConnection", "Dataconnection "+"4");
						if((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iDATA_CONNECTION] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME)
						{
							DeBug.ShowLog("DataConnection", "Dataconnection "+"5");
							isCurrentlyBlockedInTime = true;
							if((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iDATA_CONNECTION][Hr] & MIN_LUT[Min]) != 0)
							{
								DeBug.ShowLog("DataConnection", "Dataconnection "+"6");
								//	if (mobileDataEnabled != null)	
								{
									ConnectivityManager dataManager;
									dataManager  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
									Method dataMtd;
									try {
										dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
										dataMtd.setAccessible(true);
										mobileDataEnabled=dataMtd.invoke(dataManager, false);   
										DeBug.ShowToast(getApplicationContext(), ""+mobileDataEnabled);
										DeBug.ShowLog("AppBlock", "bTimeDurationCtrl blocked ");
									} catch (NoSuchMethodException e) {

										e.printStackTrace();
									} catch (IllegalArgumentException e) {

										e.printStackTrace();
									} catch (IllegalAccessException e) {

										e.printStackTrace();
									} catch (InvocationTargetException e) {

										e.printStackTrace();
									}
									DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_CLOCK_TIME . DC 1 \n" + CallHelper.Ds.structPC.AppHrMin  + ' ' + CallHelper.Ds.structFCC.wStartTime[iWeekDay][eFeatureControl.iDATA_CONNECTION]);
								}
							}
							DeBug.ShowLog(Tag1,""+ packageName +"   HR "+Hr +" Min "+Min);

						}

						if(!isCurrentlyBlockedInTime)
							if((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iDATA_CONNECTION] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION)
							{
								if( ! CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION])		            	 
								{
									CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]++;
									CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iDATA_CONNECTION] = 1;
								}
								if(CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]/60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION])
								{	 
									// check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
									//1. when the activity is being stopped then it saves the current duration,
									//2. if the duration has expired then stops the activity and save the current duration.
									CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION] = true;
									CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iDATA_CONNECTION] = 3;
									DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_DURATION . . DC 3\n"+CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]);
								}
							}
					}


				}
			}*/
                private void checkDataConnectivity() {
                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION]) {
                        setMobileDataEnabled(false);
                    } else {
                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iDATA_CONNECTION] == 0) {
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iDATA_CONNECTION] = 3;
                        } else {
                            isCurrentlyBlockedInTime = false;
                        /*if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iDATA_CONNECTION] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {
                            if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iDATA_CONNECTION][Hr] & MIN_LUT[Min]) != 0) {
                                isCurrentlyBlockedInTime = true;
                                setMobileDataEnabled(false);
                            }

                        }*/

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iDATA_CONNECTION] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {

                                    if (!CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION]) {
                                        CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]++;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iDATA_CONNECTION] = 1;
                                        DeBug.ShowLogD("DATA", "USED TIME . . DATA\n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]);
                                        DeBug.ShowLog("data", "data RESTRICTED_IN_DURATION . Used  Total.   " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]);
                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.
                                        setMobileDataEnabled(false);
                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iDATA_CONNECTION] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iDATA_CONNECTION] = 3;
                                        DeBug.ShowToast(AppBlock.this, "DATA . . \n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iDATA_CONNECTION]);
                                    }

                                }
                        }
                    }
                }

                //Check For the Bluetooth (BT) (Hardware level implementation)
                private void checkBluetooth() {
                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_BLUE]) {
                        if (mBluetoothAdapter.isEnabled())
                            mBluetoothAdapter.disable();
                    } else {
                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_BLUE] == 0) //For Bluetooth Block
                        {
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_BLUE] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_BLUE] = 3;
                        } else {
                            isCurrentlyBlockedInTime = false;
                            if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_BLUE] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {
                                if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_BLUE][Hr] & MIN_LUT[Min]) != 0) {
                                    if (!((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_BLUE] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION)) {
                                        isCurrentlyBlockedInTime = true;
                                        if (mBluetoothAdapter.isEnabled()) {
                                            mBluetoothAdapter.disable();
                                            DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_CLOCK_TIME .  BT\n" + CallHelper.Ds.structPC.AppHrMin);
                                        }
                                    }
                                }
                                DeBug.ShowLog(Tag1, "" + packageName + "   HR " + Hr + " Min " + Min);

                            }

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_BLUE] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {

                                    if ((mBluetoothAdapter.isEnabled()) && !CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_BLUE]) {
                                        CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_BLUE]++;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_BLUE] = 1;
                                        DeBug.ShowLogD("BT_TIME", "USED TIME . . BT\n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_BLUE]);
                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_BLUE] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_BLUE]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.
                                        mBluetoothAdapter.disable();
                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_BLUE] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_BLUE] = 3;
                                        DeBug.ShowToast(AppBlock.this, "RESTRICTED_IN_DURATION . . BT\n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_BLUE]);
                                    }

                                }
                        }
                    }
                }

                private boolean canLogApp() {
                    if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iAPP_LOG]) {
                        DeBug.ShowLog("NarayananAL", "Presently stopped : true");
                        if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iAPP_LOG][Hr] & MIN_LUT[Min]) != 0) {
                            DeBug.ShowLog("NarayananAL", "Presently stopped : true and sending log");
                            Applog();
                        }
                    } else {
                        DeBug.ShowLog("NarayananAL", "Presently stopped : false");
                        if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iAPP_LOG] == 0) //For Bluetooth Block
                        {
                            DeBug.ShowLog("NarayananAL", "Allowed WIth Td : true");
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iAPP_LOG] = false;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iAPP_LOG] = 3;
                        } else {
                            DeBug.ShowLog("NarayananAL", "Allowed WIth Td : false");
                            isCurrentlyBlockedInTime = false;
                            if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iAPP_LOG] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {
                                DeBug.ShowLog("NarayananAL", "Restricted with CLOCK");
                                if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iAPP_LOG][Hr] & MIN_LUT[Min]) != 0) {
                                    isCurrentlyBlockedInTime = true;
                                    {
                                        DeBug.ShowLog("NarayananAL", "Sending applogs");
                                        Applog();
                                    }
                                }
                                DeBug.ShowLog(Tag1, "" + packageName + "   HR " + Hr + " Min " + Min);

                            }

                            if (!isCurrentlyBlockedInTime)
                                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iAPP_LOG] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {

                                    if (!CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iAPP_LOG]) {
                                        CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iAPP_LOG]++;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iAPP_LOG] = 1;
                                    }
                                    if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iAPP_LOG] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iAPP_LOG]) {
                                        // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                                        //1. when the activity is being stopped then it saves the current duration,
                                        //2. if the duration has expired then stops the activity and save the current duration.

                                        CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iAPP_LOG] = true;
                                        CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iAPP_LOG] = 3;
                                    }

                                }
                        }
                    }

                    return true;
                }

                private void Applog() {
                    int index = mApplicationInfoDB.getAppIndex(packageName);
                    if (index != -1) {
                        int usedTime = 0;
                        if (usedTimeCounter.get(index) != null)
                            usedTime = usedTimeCounter.get(index);

                        usedTimeCounter.put(index, usedTime + 1);
                        lastUsedTime.put(index, CallHelper.GetTimeWithDate());
                        DeBug.ShowLog("NarayananAL", "Receiving app logs pkg : " + packageName + ", index :" + index);
                    }
                }

                private void BlockAll() {
                    try {
                        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                        boolean locked = km.inKeyguardRestrictedInputMode();
                        if (!locked && (!packageName.equals(getPackageName()) || !Lock.screenOn)) {
                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                            startMain.addCategory(Intent.CATEGORY_HOME);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(startMain);
                            Lock.screenOn = true;
                            if (!settings.getBoolean("unlockedSOS", true)) {
                                Intent mIntent = new Intent(AppBlock.this, Lock.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mIntent.putExtra("isForLogin", (int) 0);
                                startActivity(mIntent);
                            } else if (!settings.getBoolean("unlockedSound", true)) {
                                Intent mIntent = new Intent(AppBlock.this, Lock.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mIntent.putExtra("isForLogin", (int) 2);
                                startActivity(mIntent);
                            } else if (!settings.getBoolean("unlocked", true)) {
                                Intent mIntent = new Intent(AppBlock.this, Lock.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mIntent.putExtra("isForLogin", (int) 1);
                                startActivity(mIntent);
                            } else {
                                Lock.screenOn = false;
                            }
                        } else if (locked) {
                            if (!settings.getBoolean("unlockedSound", true) && !settings.getBoolean("unlockedSoundOn", false)) {
                                Intent mIntent = new Intent(AppBlock.this, Lock.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mIntent.putExtra("isForLogin", (int) 2);
                                startActivity(mIntent);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void changeWifi() {
                    try {
                        DeBug.ShowLog("ConnectWifiThread", "inside Wifi Change.");
                        if (sensorBeans.size() > 0) {
                            if (connectWIFI()) {
                                return;
                            }
                            if (!TextUtils.isEmpty(BSSID) && !TextUtils.isEmpty(getBSSID(context)) && getBSSID(context).equalsIgnoreCase(BSSID)) {
                                return;
                            }
                        } else {
                            DeBug.ShowLog("ConnectWifiThread", "sensorBeans is Empty");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public String getBSSID(Context context) {

                    String stBSSID = null;
                    try {
                        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        stBSSID = wifiInfo.getBSSID();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return stBSSID;

                }

                private boolean connectWIFI() {
                    boolean connected = false;
                    try {
                        scanWifi();
                        if (checkWIFIProfileAvilable()) {
                            if (checkWifiavilable()) {
                                if (!checkConnectedToDesiredWifi()) {
                                    DeBug.ShowLog("ConnectWifiThread", "Wifi Change detected.");
                                    connected = connectToWIFI();
                                    context.startService(new Intent(context, SyncIntentService.class).putExtra("sensor", true));
                                    DeBug.ShowLog("ConnectWifiThread", "Wifi Change detected new Profile Added.");
                                } else {
                                    connected = true;
                                    for (SensorListBean sensorBean : sensorBeans) {
                                        if (networkSSID.equals(sensorBean.SSID)) {
                                            if (CallHelper.oldSensor == "" || !CallHelper.oldSensor.equals(String.valueOf(sensorBean.ProfileId))) {
                                                context.startService(new Intent(context, SyncIntentService.class).putExtra("sensor", true));
                                                DeBug.ShowLog("ConnectWifiThread", "Wifi Change detected new Profile Added.");
                                                CallHelper.oldSensor = "" + sensorBean.ProfileId;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            creatWifiProfile();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return connected;
                }

                protected void creatWifiProfile() {
                    try {
                        conf = new WifiConfiguration();
                        conf.SSID = "\"" + networkSSID + "\"";
                        conf.preSharedKey = "\"" + networkPass + "\"";
                        wifiManager.addNetwork(conf);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                protected boolean checkWIFIProfileAvilable() {
                    boolean connected = false;
                    try {
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        if (list != null)
                            for (WifiConfiguration i : list) {
                                if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                                    connected = true;
                                    break;
                                }
                            }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return connected;
                }

                private boolean checkWifiavilable() {
                    boolean isNetworkAvailble = false;
                    try {
                        List<ScanResult> l = wifiManager.getScanResults();
                        for (ScanResult r : l) {
                            if (r.SSID.equalsIgnoreCase(networkSSID))
                                isNetworkAvailble = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return isNetworkAvailble;
                }

                protected boolean checkConnectedToDesiredWifi() {
                    boolean isNetworkAvailble = false;
                    try {
                        WifiInfo wi = wifiManager.getConnectionInfo();
                        if (wi != null) {
                            if (wi.getIpAddress() != 0) {
                                DeBug.ShowLog("ConnectWifiThread", wi.getSSID());
                                if (wi.getSSID().equals("\"" + networkSSID + "\"")) {
                                    isNetworkAvailble = true;
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return isNetworkAvailble;
                }

                private void checkOpenNetwork() {
                    try {
                        WifiInfo wi = wifiManager.getConnectionInfo();
                        if (wi != null) {
                            if (wi.getIpAddress() != 0) {
                                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                for (WifiConfiguration i : list) {
                                    if (i.SSID != null && i.SSID.equals(wi.getSSID())) {
                                        if (i.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                                            return;
                                        }
                                        if (i.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) ||
                                                i.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                                            return;
                                        }
                                        if (i.wepKeys[0] == null) {
                                            wifiManager.disconnect();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                private boolean connectToWIFI() {
                    boolean connected = false;
                    try {
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        for (WifiConfiguration i : list) {
                            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                                wifiManager.disconnect();
                                wifiManager.enableNetwork(i.networkId, true);
                                wifiManager.reconnect();
                                connected = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return connected;
                }

                protected void scanWifi() {
                    try {
                        DeBug.ShowLog("ConnectWifiThread", "Scanning Wifi.");
                        List<WifiDetails> ourWifi = new ArrayList<>();
                        if (wifiManager.startScan()) {
                            List<ScanResult> wifiScanResultList = wifiManager.getScanResults();
                            for (ScanResult result : wifiScanResultList) {
                                if (sensorBeans.size() > 0) {
                                    for (SensorListBean sensorBean : sensorBeans) {
                                        if (sensorBean.SSID.equals(result.SSID)) {
                                            int signalLevel = result.level;
                                            ourWifi.add(new WifiDetails(result, sensorBean));
                                            DeBug.ShowLog("ConnectWifiThread", "WiFi Name:" + result.SSID + " Strength:" + signalLevel);
                                        }
                                    }
                                }
                            }
                            if (ourWifi.size() > 0) {
                                int i = -100;
                                for (WifiDetails res : ourWifi) {
                                    if (i < res.getScanResult().level) {
                                        i = res.getScanResult().level;
                                        networkSSID = res.getSensorBean().SSID;
                                        networkPass = res.getSensorBean().Password;
                                        CallHelper.profileSensor = "" + res.getSensorBean().ProfileId;
                                    }
                                }
                                editor.putString("CallHelper.profileSensor", CallHelper.profileSensor);
                                editor.commit();
                                DeBug.ShowLog("ConnectWifiThread", "WiFi Name:" + networkSSID + " Password:" + networkPass + " Length:" + i + " Profile Id:" + CallHelper.profileSensor);
                            } else {
                                resetSensor();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                private void resetSensor() {
                    String profileId = settings.getString("structCCC.stProfileId", CallHelper.Ds.structCCC.stProfileId);
                    String sensorProfile = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
                    if (profileId != null && !profileId.isEmpty() && sensorProfile != null && !sensorProfile.isEmpty() && !sensorProfile.equals(profileId)) {
                        CallHelper.profileSensor = profileId;
                        if (profileId == "0") {
                            CallHelper.Ds.structCCC.isProfileEnabled = 0;
                            editor.putInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
                        } else {
                            CallHelper.Ds.structCCC.isProfileEnabled = 1;
                            editor.putInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
                        }
                        editor.putString("CallHelper.profileSensor", CallHelper.profileSensor);
                        editor.commit();
                        context.startService(new Intent(context, SyncIntentService.class).putExtra("sensor", true));
                    }
                }

                int count = 0;

                @SuppressWarnings("deprecation")
                @Override
                public void handleMessage(Message msg) {
                    try {
                        super.handleMessage(msg);
                        boolean isAccessibilitySettingsOn = true;
                        boolean isappHasUsageAccess = true;
                        if (devicePolicyManager.isAdminActive(demoDeviceAdmin)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                if (!isAccessibilitySettingsOn(AppBlock.this)) {
                                    try {
                                        isAccessibilitySettingsOn = false;
                                        if (count == 0) {
                                            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            AppBlock.this.startActivity(intent);
                                        }
                                        count++;
                                        if (count == 300)
                                            count = 0;
                                    } catch (Exception ignore) {
                                    }
                                } else if (!isAccessibilitySettingsOn)
                                    count = 0;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                                if (isAccessibilitySettingsOn) {
                                    if (!appHasUsageAccess(AppBlock.this)) {
                                        isappHasUsageAccess = false;
                                        if (count == 0) {

                                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            AppBlock.this.startActivity(intent);
                                        }
                                        count++;
                                        if (count == 4)
                                            count = 0;
                                    } else
                                        count = 0;
                                }
                        }

				/*
                 * code block to ensure that OneMinuteTimerService is running if not to start the same.
				 */
                        {
                            if (!Constants.isMyServiceRunning(context, OneMinuteTimerService.class)) {
                                startService(new Intent(getBaseContext(), OneMinuteTimerService.class));
                            }
                            if (settings.getBoolean("Conveyance.isStarted", false) && !Constants.isMyServiceRunning(context, OneMinuteService.class)) {
                                startService(new Intent(getBaseContext(), OneMinuteService.class));
                            }
                            RSharedData sharedData = new RSharedData(context);
                            if (sharedData.getStatus() && !Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
                                startService(new Intent(getBaseContext(), RGetTowerLocationService.class));
                            }
                        }

                        long Starttime = System.currentTimeMillis();
                        //	boolean bRadioOn = false;
                        iWeekDay = CallHelper.Ds.structPC.bWeekDay;

                        Hr = CallHelper.Ds.structPC.AppHrMin / 100;
                        Min = CallHelper.Ds.structPC.AppHrMin % 100;

                        DeBug.ShowLog("App_time", "Hr " + Hr + " Min " + Min + " Day " + iWeekDay);

                        try {
                            am = (ActivityManager) getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
                            packageName = getTopActivty(isappHasUsageAccess).toLowerCase();
                            ClassName = am.getRunningTasks(1).get(0).topActivity.getClassName().toLowerCase();
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                                if (WindowChangeDetectingService.packageName != null && !WindowChangeDetectingService.packageName.isEmpty())
                                    packageName = WindowChangeDetectingService.packageName;
                                if (WindowChangeDetectingService.ClassName != null && !WindowChangeDetectingService.ClassName.isEmpty())
                                    ClassName = WindowChangeDetectingService.ClassName;
                            }
                            //	packageName = am.getRunningAppProcesses().get(0).processName;
                            audioManager1 = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DeBug.ShowLog("PKG", " P " + packageName + "  IN PKG list " + ClassName);
                        final List<RunningServiceInfo> recentTasks = am.getRunningServices(Integer.MAX_VALUE);

//                    for (int i = 0; i < recentTasks.size(); i++) {
//                        if (recentTasks.get(i).process.toString().toUpperCase().contains("FM")) {
//                            //	bRadioOn=true;
//                            DeBug.ShowLog("Executed app", "name " + recentTasks.get(i).process.toString() + " Active time: " + recentTasks.get(i).activeSince + " last time: " + recentTasks.get(i).lastActivityTime);
//                            break;
//                        }
//                    }
                        if (!CallHelper.Ds.structPC.bTimeExpired)
                            if (devicePolicyManager.isAdminActive(demoDeviceAdmin))

                                if (ClassName != null && (ClassName.contains("com.android.settings") || ClassName.contains("com.android.packageinstaller"))
                                        && !DeviceAdminThread_Started) {
                            /*Intent intent= new Intent(AppBlock.this,Abc.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);*/
                                    Thread deviceAdminThread = new Thread(new DeviceAdminThread(AppBlock.this));
                                    deviceAdminThread.setPriority(Thread.MAX_PRIORITY - 1);
                                    deviceAdminThread.start();
                                    DeBug.ShowLog("PKG", " TP " + deviceAdminThread.getPriority());
                                }
                        //DeBug.ShowToast(getApplicationContext(), "Radio is on "+bRadioOn);

                        int isEnable = settings.getInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
                        boolean blockActive = true;
                        if (isEnable != 1)
                            blockActive = false;

                        scanCounter++;
                        DeBug.ShowLog("ConnectWifiThread", "wifi scan count " + scanCounter);
                        if (!CallHelper.Ds.structPC.bTimeExpired && scanCounter > 45) {
                            if (!SyncIntentService.serviceStarted) {
                                scanCounter = 0;
                                if (WManager != null && WManager.isWifiEnabled()) {
                                    networkSSID = "";
                                    networkPass = "";
                                    context = getBaseContext();
                                    BSSID = getBSSID(context);
                                    wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                    SensorListTable sensorDb = new SensorListTable(context);
                                    sensorBeans = sensorDb.getSensors();
                                    if (sensorBeans != null && sensorBeans.size() > 0) {
                                        changeWifi();
                                    }
                                } else {
                                    resetSensor();
                                }
                            }
                        }

                        if (blockActive && !CallHelper.Ds.structPC.bTimeExpired) {
                            if ((CallHelper.Ds.structPC.bMode == ePhoneMode.PARENT_RESTRICTED) && !CallHelper.Ds.structPC.bDateChangedToDefault) {

                                if (settings.getBoolean("isLockEnabled", false)) {
                                    BlockAll();
                                }

                                if (CallHelper.Ds.structPC.bSimChanged) {
                                    if (packageName != null && !packageName.contains("mobiocean")) {

                                        if (!CurrentappInSchoolMode.contains(packageName)) {
                                            Intent startMain = new Intent(Intent.ACTION_MAIN);
                                            startMain.addCategory(Intent.CATEGORY_HOME);
                                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            //startActivity(startMain);
                                            CurrentappInSchoolMode = packageName.toLowerCase();
                                            DeBug.ShowToast(AppBlock.this, "SIM Changed . . . \n" + packageName);

                                            return;
                                        }
                                    }
                                }

                                DeBug.ShowLog("DataConnection", "INIT " + Long.toBinaryString(CallHelper.Ds.structCCC.wFeatureControlWord[0]));

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.APP_REPSOTORY) == eFeatureControl.APP_REPSOTORY) {
                                    checkApps();
                                }

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.GPS_BUZZER) == eFeatureControl.GPS_BUZZER) {
                                    checkGps();
                                }

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_VOICE_RECORD) == eFeatureControl.BLOCK_VOICE_RECORD) {
                                    checkMic();
                                    micMuted = true;
                                } else {
                                    if (settings.getBoolean("AppBlock.micMuted", micMuted)) {
                                        try {
                                            if (audioManagerMic != null) {
                                                audioRecordMic.stop();
                                                audioRecordMic.release();
                                                audioRecordMic = null;
                                                count = 0;
                                            }
                                        } catch (Exception e) {
                                            count = 0;
                                            audioRecordMic = null;
                                        }
                                        micMuted = false;
                                        editor.putBoolean("AppBlock.micMuted", micMuted);
                                        editor.commit();
                                        audioManagerMic = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
                                        audioManagerMic.setMode(AudioManager.MODE_NORMAL);
                                        if (audioManagerMic.isMicrophoneMute()) {
                                            audioManagerMic.setMicrophoneMute(false);
                                        }
                                    }
                                }

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_OPEN_WIFI) == eFeatureControl.BLOCK_OPEN_WIFI)
                                    checkOpenNetwork();

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_WIFI) == eFeatureControl.BLOCK_WIFI) {

                                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_WIFI][Hr] & MIN_LUT[Min]) != 0) {
                                        DeBug.ShowLog("NarayananWifi", "WIFI CHECK");
                                        checkWifi();              //4
                                    } else {
                                        DeBug.ShowLog("NarayananWifi", "WIFI OPEN");
                                    }
                                }

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_HOTSPOT) == eFeatureControl.BLOCK_HOTSPOT) {

                                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_HOTSPOT][Hr] & MIN_LUT[Min]) != 0) {
                                        DeBug.ShowLog("NarayananWH", "WIFI_HOTSPOT CHECK");
                                        checkWifiHotSopt();
                                    } else {
                                        DeBug.ShowLog("NarayananWH", "WIFI_HOTSPOT OPEN");
                                    }
                                }
                                if (!settings.getBoolean(RootConstants.ISBLOCKMOBILEDATA, false)) {
                                    DeBug.ShowLog("DataConnection", "Dataconnection " + ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.DATA_CONNECTION) == eFeatureControl.DATA_CONNECTION));
                                    if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.DATA_CONNECTION) == eFeatureControl.DATA_CONNECTION) {

                                        if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iDATA_CONNECTION][Hr] & MIN_LUT[Min]) != 0) {
                                            DeBug.ShowLog("NarayananDC", "DATA_CONNECTIVITY CHECK");
                                            checkDataConnectivity();  //5
                                        } else {
                                            DeBug.ShowLog("NarayananDC", "DATA_CONNECTIVITY OPEN");
                                        }
                                    }
                                } else {
                                    if (RootConstants.checkRootMethod()) {
                                        if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.DATA_CONNECTION) == eFeatureControl.DATA_CONNECTION) {
                                            DeBug.ShowLog("DATACONECTION", "NONE");
                                        } else {
                                            try {
                                                Runtime.getRuntime().exec("su -c svc data enable");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_BLUE) == eFeatureControl.BLOCK_BLUE) {

                                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_BLUE][Hr] & MIN_LUT[Min]) != 0) {
                                        DeBug.ShowLog("NarayananBT", "BLUETOOTH CHECK");
                                        checkBluetooth();
                                    } else {
                                        DeBug.ShowLog("NarayananBT", "BLUETOOTH OPEN");
                                    }
                                }
                                //	checkMedia(bRadioOn);     //1

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_NEW_APP) == eFeatureControl.BLOCK_NEW_APP)
                                    blockNewInstalledApps();  //31

                                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_PLAYSTORE) == eFeatureControl.BLOCK_PLAYSTORE) {
                                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_PLAYSTORE][Hr] & MIN_LUT[Min]) != 0) {
                                        DeBug.ShowLog("NarayananPS", "PLAYSTORE CHECK");
                                        blockPlayStore();  //31
                                    } else {
                                        DeBug.ShowLog("NarayananPS", "PLAYSTORE OPEN");
                                    }
                                }

                                //VPN Code tested and working.. hardcoded. starts
                                boolean vpnIsEnabled = settings.getBoolean("VpnIsEnabled", false);
                                if (vpnIsEnabled) {
                                    Intent vpnIntent = VpnService.prepare(getApplicationContext());
                                    if (vpnIntent == null) {
                                        if (!Constants.isMyServiceRunning(context, MobiVpnService.class)) {
                                            Intent i = new Intent(context, MobiVpnService.class);
                                            startService(i);
                                        }
                                    } else {
                                        if (!StartVpnActivity.screenOn) {
                                            Intent startVpn = new Intent(context, StartVpnActivity.class);
                                            startVpn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            context.startActivity(startVpn);
                                        }
                                    }
                                } else {
                                    if (Constants.isMyServiceRunning(context, MobiVpnService.class)) {
                                        if (MobiVpnService.mThread != null) {
                                            MobiVpnService.mThread.interrupt();
                                        }
                                        Intent i = new Intent(context, MobiVpnService.class);
                                        stopService(i);
                                    }
                                }
                                //VPN Code tested and working.. hardcoded. ends

                                if (devicePolicyManager.isAdminActive(demoDeviceAdmin))
                                    if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_CAMERA) == eFeatureControl.BLOCK_CAMERA) {
                                        if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_CAMERA][Hr] & MIN_LUT[Min]) != 0) {
                                            DeBug.ShowLog("NarayananCam", "CAMERA CHECK");
                                            checkCamera();              //1
                                        } else if (isCameraDisabled()) {
                                            DeBug.ShowLog("NarayananCam", "CAMERA OPEN");
                                            enableCamera();
                                        }
                                    } else if (isCameraDisabled()) {
                                        DeBug.ShowLog("NarayananCam", "CAMERA OPEN");
                                        enableCamera();
                                    }


                                if (packageName != null && !packageName.contains("mobiocean"))
                                    if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.APP_LOG) == eFeatureControl.APP_LOG) {
                                        canLogApp();
                                    }

                            } else if ((CallHelper.Ds.structPC.bMode == ePhoneMode.SCHOOL_RESTRICTED) || CallHelper.Ds.structPC.bDateChangedToDefault) {
                                if (packageName != null && !packageName.contains("mobiocean")) {

                                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                                    startMain.addCategory(Intent.CATEGORY_HOME);
                                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(startMain);

                                    CurrentappInSchoolMode = packageName;

                                    if (!CallHelper.Ds.structPC.bDateChangedToDefault)
                                        DeBug.ShowToast(AppBlock.this, "In SCHOOL_RESTRICTED MOde . . . \n" + packageName);
                                    else
                                        DeBug.ShowToast(AppBlock.this, "Wrong Time . . . \n" + CallHelper.Ds.structPC.AppHrMin);
                                }
                            }

                            if (CallHelper.Ds.lSchoolSchedule.StartWIFI) {
                                if (!WManager.isWifiEnabled())
                                    WManager.setWifiEnabled(true);

                                if (mBluetoothAdapter.isEnabled())
                                    mBluetoothAdapter.disable();
                            }
                        } else

                        {
                            if (devicePolicyManager.isAdminActive(demoDeviceAdmin) && isCameraDisabled()) {
                                DeBug.ShowLog(Tag1, "" + packageName);
                                enableCamera();
                            }
                        }

                        DeBug.ShowLog("APPBLOCK", "Time/Sec in MS " + (System.currentTimeMillis() - Starttime) + "      " + CallHelper.Ds.structNCC.bTimeDurationCtrl[4][2]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            ;

            Runnable obj = new Runnable() {
                @Override
                public void run() {
                    mApplicationInfoDB = ApplicationInfoDB.getInstance(AppBlock.this);
                    handler.sendEmptyMessage(0);
                    handlerForRunnable.postDelayed(this, 1000);
                }
            };

            handlerForRunnable.post(obj);
        } catch (Exception e) {
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(getBaseContext(), AppBlock.class));
    }

    public void onTaskRemoved(Intent rootIntent) {
        DeBug.ShowToast(AppBlock.this, "AppBlockService onTaskRemoved ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public String getTopActivty(boolean isappHasUsageAccess) {
        String topPackageName = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isappHasUsageAccess) {
            UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            // We get usage stats for the last 10 seconds
            List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);
            // Sort the stats by the last time used
            if (stats != null) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : stats) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (mySortedMap != null && !mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    DeBug.ShowLog("TopPackageName", topPackageName);
                }
            }
        } else {
            topPackageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        }

        return topPackageName;
    }

    private boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = "com.mobiocean/com.mobiocean.service.WindowChangeDetectingService";
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            DeBug.ShowLog("CurrentActivity", "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Exception e) {
            DeBug.ShowLog("CurrentActivity", "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            try {
                DeBug.ShowLog("CurrentActivity", "***ACCESSIBILIY IS ENABLED*** -----------------");
                String settingValue = Settings.Secure.getString(
                        mContext.getApplicationContext().getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                if (settingValue != null) {
                    TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                    splitter.setString(settingValue);
                    while (splitter.hasNext()) {
                        String accessabilityService = splitter.next();

                        DeBug.ShowLog("CurrentActivity", "-------------- > accessabilityService :: " + accessabilityService);
                        if (accessabilityService.equalsIgnoreCase(service)) {
                            DeBug.ShowLog("CurrentActivity", "We've found the correct setting - accessibility is switched on!");
                            accessibilityFound = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DeBug.ShowLog("CurrentActivity", "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }

    @SuppressLint("NewApi")
    private boolean appHasUsageAccess(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)

            try {
                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
                return (mode == AppOpsManager.MODE_ALLOWED);

            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        else
            return true;

    }

    public class WifiDetails {

        ScanResult scanResult = null;
        SensorListBean sensorBean = null;

        WifiDetails(ScanResult scanResult, SensorListBean sensorBean) {
            this.scanResult = scanResult;
            this.sensorBean = sensorBean;
        }

        public ScanResult getScanResult() {
            return scanResult;
        }

        public SensorListBean getSensorBean() {
            return sensorBean;
        }

    }
}