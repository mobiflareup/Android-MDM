package com.mobiocean.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.mobiocean.database.DatabaseHandler_SMS;
import com.mobiocean.receiver.DemoDeviceAdminReceiver;
import com.mobiocean.receiver.OneMinuteTimerReceiver;
import com.mobiocean.rootfeatures.ScreenOnOffReceiver;
import com.mobiocean.ui.SOSReceiver;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.StringCryptor;
import com.mobiocean.util.Struct_SMS;
import com.mobiocean.util.eFeatureControl;

import org.json.JSONException;
import org.json.JSONObject;
import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.mobiocean.util.Constant.MIN_LUT;

public class CallDetectService extends Service {

    public static CallHelper callHelper;
    public static CallDetectService callDetectService;
    public static PackageManager packageManager;

    public static AlarmManager alarmManager;
    public static PendingIntent pendingintent;
    BroadcastReceiver mReceiver;
    BroadcastReceiver mReceiver1;

    public static final String PREFS_NAME = "MyPrefsFile";
    public static SharedPreferences settings;
    public static SharedPreferences.Editor editor;

    public static TelephonyManager mtelephonyManager;
    public static String stSIMSerialno = null;
    public static String Country = null;

    public static AudioManager audioManager1;

    public static boolean SMSsent[] = new boolean[5];
    public static String SIM_CHANGED;
    public static String TIME_CHANGED;

    public static DevicePolicyManager devicePolicyManager;
    public static ComponentName demoDeviceAdmin;

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            callDetectService = this;
            for (int i = 0; i < 2; i++) {
                SMSsent[i] = true;
            }

            long time = System.currentTimeMillis();
            final String TAG = "CallDetectService";
            callDetectService = this;
            audioManager1 = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            callHelper = new CallHelper(this);
            callHelper.start();
            packageManager = getPackageManager();
            devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            mtelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction("android.intent.action.PHONE_STATE");
            mReceiver = new SOSReceiver();
            registerReceiver(mReceiver, filter);

            try {
                IntentFilter filter1 = new IntentFilter(Intent.ACTION_SCREEN_ON);
                filter1.addAction(Intent.ACTION_SCREEN_OFF);
                filter1.addAction("android.intent.action.PHONE_STATE");
                mReceiver1 = new ScreenOnOffReceiver();
                registerReceiver(mReceiver1, filter1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (settings.getBoolean("PREFS_NAME_TEST", true)) {
                try {
                    stSIMSerialno = mtelephonyManager.getSimSerialNumber();
                    CallHelper.Ds.structPC.stSIMSerialno = stSIMSerialno;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (int day = 0; day < 7; day++) {
                    for (int i = 0; i < 3; i++) {
                        editor.putInt("structNCC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structNCC.bTimeDurationCtrl[day][i]);
                        editor.putInt("structNCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structNCC.wUsedDuration[day][i]);
                        editor.putInt("structNCC.wTotalDuration" + day + "" + i, CallHelper.Ds.structNCC.wTotalDuration[day][i]);
                        editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structNCC.bPresentlyStopped[day][i]);
                        for (int j = 0; j < 24; j++) {
                            editor.putLong("structNCC.lAllowedTime" + day + "" + i + "" + j, CallHelper.Ds.structNCC.lAllowedTime[day][i][j]);
                            if (day == 4 && i == 0 && j == 16)
                                DeBug.ShowLog(TAG, " r " + Long.toBinaryString(CallHelper.Ds.structNCC.lAllowedTime[day][i][j]));
                        }
                    }

                }

                //store abort SMS no. in SharedPreference
                editor.putInt("Ds.sms_numberlist_size", CallHelper.Ds.sms_numberlist.size());
                for (int i = 0; i < CallHelper.Ds.sms_numberlist.size(); i++) {
                    editor.putString("Ds.sms_numberlist" + i, CallHelper.Ds.sms_numberlist.get(i));
                    editor.putInt("Ds.sms_numberFlaglist" + i, CallHelper.Ds.sms_numberFlaglist.get(i));
                }

                //store Default phone (DNCC) no. in SharedPreference
                String encryp = null;
                for (int i = 0; i < CallHelper.Ds.structDNCC.bTotalNumbers; i++) {
                    try {
                        String Number = CallHelper.Ds.structDNCC.stDefunctNumber[i];
                        encryp = StringCryptor.encrypt(Number, StringCryptor.encryptionKey);
                        editor.putString("structDNCC.stDefunctNumber" + i, encryp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                //store Pull_SMS phone no. in SharedPreference
                try {
                    String Number = CallHelper.Ds.structDNCC.stPullMsgNo;
                    encryp = StringCryptor.encrypt(Number, StringCryptor.encryptionKey);
                    editor.putString("structDNCC.stPullMsgNo", encryp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //store Extra allowed no. in SharedPreference
                for (int i = 0; i < CallHelper.Ds.structNCC.bTotalNumbers; i++) {
                    editor.putString("structNCC.stEXT_ALWD_Numbers" + i, CallHelper.Ds.structNCC.stEXT_ALWD_Numbers[i]);
                }

                editor.putInt("structPC.bWeekDay", CallHelper.Ds.structPC.bWeekDay);
                editor.putInt("structPC.AppHrMin", CallHelper.Ds.structPC.AppHrMin);
                editor.putInt("structPC.bMode", CallHelper.Ds.structPC.bMode);
                editor.putInt("structPC.iExpiryDate", CallHelper.Ds.structPC.iExpiryDate);
                editor.putInt("structPC.iExpiryMonth", CallHelper.Ds.structPC.iExpiryMonth);
                editor.putInt("structPC.iExpiryYear", CallHelper.Ds.structPC.iExpiryYear);
                editor.putInt("structPC.iCurrentDate", CallHelper.Ds.structPC.iCurrentDate);
                editor.putInt("structPC.iCurrentMonth", CallHelper.Ds.structPC.iCurrentMonth);
                editor.putInt("structPC.iCurrentYear", CallHelper.Ds.structPC.iCurrentYear);
                editor.putInt("structPC.bFactoryControlByte", CallHelper.Ds.structPC.bFactoryControlByte);
                editor.putInt("structPC.bTotalApps", CallHelper.Ds.structPC.bTotalApps);
                editor.putString("structPC.stSIMSerialno", CallHelper.Ds.structPC.stSIMSerialno);
                editor.putString("structPC.stPassword", CallHelper.Ds.structPC.stPassword);
                editor.putString("structPC.stEDPassword", CallHelper.Ds.structPC.stEDPassword);
                editor.putString("structPC.Country", CallHelper.Ds.structPC.Country);
                editor.putBoolean("structPC.bRunApp", CallHelper.Ds.structPC.bRunApp);
                editor.putBoolean("structPC.bTimeExpired", CallHelper.Ds.structPC.bTimeExpired);

                editor.putInt("structNCC.bPhoneFeatureCtrl", CallHelper.Ds.structNCC.bPhoneFeatureCtrl);

                editor.putInt("structLGC.bfeatureValue", CallHelper.Ds.structLGC.bfeatureValue);
                editor.putInt("structLGC.bNoOfRequestGPSLocn", CallHelper.Ds.structLGC.bNoOfRequestGPSLocn);
                editor.putInt("structLGC.wRequestGPSLocn", CallHelper.Ds.structLGC.wRequestGPSLocn);
                editor.putInt("structLGC.bTimesCount", CallHelper.Ds.structLGC.bTimesCount);
                editor.putBoolean("structLGC.bNetwork", CallHelper.Ds.structLGC.bNetwork);

                editor.putString("lSchoolSchedule.stSchoolCode", CallHelper.Ds.lSchoolSchedule.stSchoolCode);

                editor.putBoolean("PREFS_NAME_TEST", false);
                editor.commit();

                String SMS = "GBox set as LK0 eweqweqddqqqwdqqdqwdd-0212asda";

                CallHelper.decodeMessage(callDetectService, SMS);

                //		initGeoFence();
                //		addGeofance(""+CallHelper.Ds.structLGC.dGeoFenceLatitude, ""+CallHelper.Ds.structLGC.dLongitude, ""+CallHelper.Ds.structLGC.dGeoFenceRadius);


                /******************************************************
                 * Starts Alarm Manager for as one minute Timer
                 * *****************************************************/
                DeBug.ShowLog("MobiTimer", "Started");
                if (!OneMinuteTimerReceiver.bOneMinuteTimerStarted) {
                    Intent intent1 = new Intent(callDetectService, OneMinuteTimerReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243, intent1, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60 * 1000, pendingIntent);
                    ComponentName receiver = new ComponentName(getApplicationContext(), OneMinuteTimerReceiver.class);
                    PackageManager pm = getApplicationContext().getPackageManager();
                    pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }

////			alarmManager=(AlarmManager) CallHelper.getAlarmanager(callDetectService);
//			alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//			Intent intentTimer = new Intent(this, OneMinuteTimerReceiver.class);
//			pendingintent = PendingIntent.getBroadcast(getApplicationContext(), 0, intentTimer, 0);
//			if(!OneMinuteTimerReceiver.bOneMinuteTimerStarted)
////				alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 1000 * 60 , pendingintent);
//			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * 60 , pendingintent);

                /**Part of B2B  **/
                /*******************************************************
                 * SMS ContentObserver to get the info of sent SMSs
                 * *****************************************************/
                {
                    final YourObserver yourObserve = new YourObserver(new Handler());
                    ContentResolver contentResolver = getContentResolver();
                    contentResolver.registerContentObserver(Uri.parse("content://sms"), true, yourObserve);
                    DeBug.ShowLog("CallDB", "OutgoingSMSReceiverService 0");
                }

            } else //if values are stored.
            {
                {
                    CallHelper.Ds.structPC.Country = settings.getString("structPC.Country", CallHelper.Ds.structPC.Country);
                    DeBug.ShowToast(CallDetectService.callDetectService, "" + CallHelper.Ds.structPC.Country);
                }

                //set Geofenceset booblean off
                //CallHelper.Ds.structLGC.bGeofenceActive = -1;

                final Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        {
                            //	if(!OneMinuteTimerReceiver.bOneMinuteTimerStarted)

                            /******************************************************
                             * ReStarts Alarm Manager for as one minute Timer
                             * SMS ContentObserver to get the info of sent SMSs
                             * *****************************************************/
                            {
                                alarmManager = (AlarmManager) CallHelper.getAlarmanager(callDetectService);
                                try {
                                    alarmManager.cancel(pendingintent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                DeBug.ShowLog("MobiTimer", "Started");

                                Intent intent1 = new Intent(callDetectService, OneMinuteTimerReceiver.class);
                                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243, intent1, 0);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 60 * 1000, pendingIntent);
                                ComponentName receiver = new ComponentName(getApplicationContext(), OneMinuteTimerReceiver.class);
                                PackageManager pm = getApplicationContext().getPackageManager();
                                pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                                /**Part of B2B  **/
                                //SMS ContentObserver to get the info of sent SMSs, and store into database
                                {
                                    final YourObserver yourObserve = new YourObserver(new Handler());
                                    ContentResolver contentResolver = getContentResolver();
                                    contentResolver.registerContentObserver(Uri.parse("content://sms"), true, yourObserve);
                                    DeBug.ShowLog("CallDB", "OutgoingSMSReceiverService 0");
                                }

                            }
                            OneMinuteTimerService.appresarted = true;

                            CallHelper.Ds.structPC.bMicroPhone = settings.getBoolean("structPC.bMicroPhone", CallHelper.Ds.structPC.bMicroPhone);
                            CallHelper.Ds.structPC.bSpeaker = settings.getBoolean("structPC.bSpeaker", CallHelper.Ds.structPC.bSpeaker);
                            if (CallHelper.Ds.structPC.bMicroPhone)
                                CallHelper.SI(1);
                            else
                                CallHelper.SI(0);

                            if (CallHelper.Ds.structPC.bSpeaker)
                                CallHelper.SO(1);
                            else
                                CallHelper.SO(0);

                        }
                    }
                }, 10);
                //initGeoFence();
                //onUnregisterByPendingIntentClicked();
            }

            DeBug.ShowLog("TIME", " " + (System.currentTimeMillis() - time));

            try {
                CallHelper.intilizationComplited = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            UpdateService.cancelRepeat(this);
            UpdateService.scheduleRepeat(this);
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    /******************************************************
     * Function for sending SMS for SIM change
     *****************************************************/
    public static void sendSMS(final String SMS, final int SIM_Time) {
        if((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SEND_SMS) == eFeatureControl.SEND_SMS)
        {
            SMSsent[SIM_Time] = false;
            String SENT = "SMS_SENT";
            Intent sentIntent = new Intent(SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(callDetectService, 0, sentIntent, 0);
            callDetectService.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    DeBug.ShowLog("SMSSENT", " on recieve " + SMS + " " + SIM_Time + "  " + " " + arg1.getIntExtra("SMS_ID", 11111));
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            DeBug.ShowToast(callDetectService, "SMS sent " + SIM_Time);
                            DeBug.ShowLog("SMSSENT", " OK " + SMS + " " + SIM_Time);
                            SMSsent[SIM_Time] = true;
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            DeBug.ShowLog("SMSSENT", " Fail " + SMS + " " + SIM_Time);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            DeBug.ShowLog("SMSSENT", " Fail " + SMS + " " + SIM_Time);
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            DeBug.ShowLog("SMSSENT", " Fail " + SMS + " " + SIM_Time);
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            DeBug.ShowLog("SMSSENT", " Fail " + SMS + " " + SIM_Time);
                            break;
                    }
                }
            }, new IntentFilter(SENT));

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(CallHelper.Ds.structDNCC.stPullMsgNo, null, SMS, sentPI, null);
        }
    }


    /******************************************************
     * Function for sending SMSs
     *****************************************************/
    public static void sendSMSs(final String SMS) {
        if((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SEND_SMS) == eFeatureControl.SEND_SMS) {
            String SENT = "SMS_SENT";
            Intent sentIntent = new Intent(SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(callDetectService, 0, sentIntent, 0);
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(CallHelper.Ds.structDNCC.stPullMsgNo, null, SMS, sentPI, null);
        }
    }

    @Override
    public void onDestroy() {
        try {
            alarmManager = CallHelper.getAlarmanager(callDetectService);
            alarmManager.cancel(pendingintent);
            OneMinuteTimerService.servicesrestart = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
        startService(new Intent(getBaseContext(), CallDetectService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onTaskRemoved(Intent rootIntent) {
        DeBug.ShowToast(getApplicationContext(), "onTaskRemoved Calldetect Service");
        startService(new Intent(getBaseContext(), CallDetectService.class));
    }

    /******************************************************
     * ContentObserver for getting the sent SMS from device
     * Send this info to server
     * if faild store in DataBase
     *****************************************************/
    class YourObserver extends ContentObserver {
        DatabaseHandler_SMS SMS_db;

        public YourObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            final int iWeekDay = CallHelper.Ds.structPC.bWeekDay;
            if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId))
                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SMS_HIST) == eFeatureControl.SMS_HIST) {
                    boolean canLogSMS = false;
                    int Hr = CallHelper.Ds.structPC.AppHrMin / 100;
                    int Min = CallHelper.Ds.structPC.AppHrMin % 100;
                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iSMS_HIST][Hr] & MIN_LUT[Min]) != 0)
                        canLogSMS = true;
                    if (canLogSMS)
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    int responce = -1;
                                    String[] reqCols = new String[]{"_id", "address", "body", "date"};
                                    Cursor c = getContentResolver().query(Uri.parse("content://sms/sent"), reqCols, null, null, null);
                                    long tempTime = settings.getLong("CallDetectService.smsLastTime",0L);
                                    if (c != null && c.moveToFirst() && c.getLong(3) > tempTime) {
                                        editor.putLong("CallDetectService.smsLastTime",c.getLong(3));
                                        editor.commit();
                                        String From = "";
                                        String To = c.getString(1);
                                        int isIncoming = 0;
                                        LocationBean location = new LocationDetails(getBaseContext()).getLocation();
                                        RestApiCall mRestApiCall = new RestApiCall();
                                        JSONObject json = new JSONObject();
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTimeInMillis(c.getLong(3));
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                                        String strDate = sdf.format(cal.getTime());
                                        try {
                                            json.put("Lat", location.Lat);
                                            json.put("Long", location.Longt);
                                            json.put("startDateTime", strDate);
                                            json.put("IsIncoming", isIncoming);
                                            json.put("AppId", CallHelper.Ds.structPC.iStudId);
                                            json.put("From", From);
                                            json.put("To", To);
                                            json.put("MsgText", c.getString(2));
                                            json.put("cellId", location.CellId);
                                            json.put("locationAreaCode", location.LAC);
                                            json.put("mobileCountryCode", location.MCC);
                                            json.put("mobileNetworkCode", location.MNC);
                                            json.put("LogDateTime", CallHelper.GetTimeWithDate());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        responce = Integer.parseInt(mRestApiCall.SMSLogs(json));
                                        DeBug.ShowLog("CallDB", "type :" + 1 + " Number: " + To + "\nregistered at: " + strDate + " SMS " + c.getString(2));
                                        if (responce < 0) {
                                            Struct_SMS mStruct_SMS = new Struct_SMS("Ram", To, c.getLong(3), c.getString(2), 0, "" + location.Lat, location.Longt);
                                            mStruct_SMS.setCellId(location.CellId);
                                            mStruct_SMS.setLAC(location.LAC);
                                            mStruct_SMS.setMCC(location.MCC);
                                            mStruct_SMS.setMNC(location.MNC);
                                            mStruct_SMS.setLogDateTime(CallHelper.GetTimeWithDate());
                                            SMS_db.addSMS(mStruct_SMS);
                                        }
                                        c.close();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                }

        }
    }

}