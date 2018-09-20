package com.mobiocean.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;
import com.browser.gingerbox.StringCryptor;
import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.database.DatabaseHandler_Call;
import com.mobiocean.database.DatabaseHandler_SMS;
import com.mobiocean.mobidb.ApplicationInfoDB;
import com.mobiocean.mobidb.ApplicationInfoStruct;
import com.mobiocean.receiver.DemoDeviceAdminReceiver;
import com.mobiocean.service.AppBlock;
import com.mobiocean.service.CallDetectService;
import com.mobiocean.service.OneMinuteTimerService;
import com.mobiocean.service.SyncIntentService;
import com.mobiocean.service.UploadService;
import com.mobiocean.ui.eFeatureSetting;

import org.conveyance.configuration.RSharedData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sn.activities.SOSAutoAnswerActivity;
import org.sn.database.ContactListTable;
import org.sn.database.WebListTable;
import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;
import org.sn.services.DownloadSecuredIntentService;
import org.sn.util.SharedData;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.mobiocean.util.Constant.COUNTRY_CODE;
import static com.mobiocean.util.Constant.MIN_LUT;

public class CallHelper {
    public static String profileSensor = "";
    public static String oldSensor = "";
    public static DataStructure Ds = new DataStructure();
    public static long sosOpened = 0L;
    public static boolean canOpenGPS = true;
    public static boolean[] runTime;
    private static int workingIndex;
    private static String SmsNumber;
    private static String Body;
    private static String SMSTimeStamp;

    public static int iWeekDay;

    private static boolean abortmessage = false;
    private static boolean settingSMS = false;

    static Intent prevIntent;

    private static PendingIntent pi;
    private static int SMSCounter = 0;
    private static boolean bOutGoingCallOn;

    private static long IncomingStartTime;
    private static long OutGoingStartTime;
    private static long RingingStartTime;
    private static long EndTime;
    boolean bStateChangedFromRinging = false;
    boolean bStateChangedFromOffHook = false;

    public static SharedPreferences settings = null;
    public static SharedPreferences.Editor editor = null;
    public static final String PREFS_NAME = "MyPrefsFile";

    private static final String BLOCKED_WEBSITES_PREFS_NAME = "blockedWebsitesPref";
    private static SharedPreferences blockedWebsitesPreferences;
    private static SharedPreferences.Editor blockedWebsitesPreferenceEditor;

    private static File file1 = null;
    protected static String filepath = "";
    static boolean ButtonPressedForAttendance = false;
    protected static boolean AttendanceFilePresent = false;
    private static SharedPreferences store_data;//for First run check & for saving recording strings
    private static String store_DataForEmployee = "mysp";
    private static SharedPreferences.Editor editor_Sumer;

    // Shared Preference file for Nimboli AppLogs	#GS
    protected static final String Nimboli_ApplicationLog_PREFS_NAME = "Nimboli_App_Log";
    //	public static SharedPreferences AppLog_SharedPref;
    //	public static SharedPreferences.Editor AppLog_Editor;

    private static int allowedDuration = 1440;

    MyPhoneStateListener phoneListener = new MyPhoneStateListener();
    protected static byte[] PASSWORD = new byte[]{0x20, 0x32, 0x34, 0x47, (byte) 0x84, 0x33, 0x58};

    private static String Incoming = "NarayananIC";
    private static String Outgoing = "MOBI_OC";

    private static String incommingNumber;
    private static Context ctx;
    private TelephonyManager tm;
    private ITelephony telephonyService;

    private IncomingReceiver incomingReceiver;
    private MessageReceiver messageReceiver;

    private static String Url;
    private static boolean Exception;

    private static int iselectedscholslot;

    private static String Parent_no = "";

    private static TelephonyManager mtelephonyManager;
    private static GsmCellLocation GCL;


    private DatabaseHandler_Call Call_db;
    protected DatabaseHandler_SMS SMS_db;


    public static byte bSMSrequestCounter = 0;
    static Calendar cal = Calendar.getInstance();

    protected static long lGPSTimeInMilies = 0;

    public static boolean intilizationComplited = false;

    protected static long EarthRadius = 6378137;

    private static byte GeofencerequestedGPS = 0;

    public static boolean locationIsUploading = false;

    public static String loc_freq = "";
    public static String fre_Updated_on = "";
    public static int GoogleCount = 0;
    public static int GPSCount = 0;
    public static int iGPSHealthReportCount = 0;
    protected static String stAirplaneMode_ONtime = "";
    protected static String stAirplaneMode_OFFtime = "";


    public static int CurrentLocSlote = -1;

    public static int SMSsentCount = 0;
    public static int InternetsentCount = 0;

    public static int TimeChangedCounter = 0;

    public static int gpsOnOff = 0;

    protected static boolean updateInfoDownloading = false;

    public static boolean isAppForground = false;
    protected static boolean isrefreshedbuttonpressed = false;

    protected static LocationManager locationManager = null;

    public static Location licationFromCallhelper;
    protected static double latitudeForDealer;
    protected static double longitudeForDealer;

    protected static boolean KeepFeatchingLocations = true;


    public static boolean SimChangeSentToServer = true;

    public static boolean InfoUploading = false;

    public static boolean SendingSMS = false;

    public static Context uploadContext;

    /******************************************************
     * BroadcastReceiver for detecting Incoming Calls
     * *****************************************************/

    public static void initSharedPreferences(Context context) {
        if (settings == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
            else
                settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
        }
    }

    private static AlarmManager mAlarmManager = null;

    public static boolean checkForUpdate = true;

    public static AlarmManager getAlarmanager(Context context) {
        if (mAlarmManager == null)
            mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        return mAlarmManager;
    }

    private class IncomingReceiver extends BroadcastReceiver {

        //		final String TAG = IncomingReceiver.class.getSimpleName();
        final String TAG = "NarayananIC";
        boolean bStateChangedFromRinging = false;

        String state = null;

        public void onReceive(final Context context, Intent intent) {

            ctx = context;
            Bundle extras = intent.getExtras();
            state = extras.getString(TelephonyManager.EXTRA_STATE);
            DeBug.ShowLog(Outgoing, TelephonyManager.EXTRA_STATE);

            if (state != null) {
                if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    DeBug.ShowLog(Outgoing, state);
                    DeBug.ShowLog(Outgoing, TelephonyManager.EXTRA_STATE);
                    if (bStateChangedFromRinging) {
                        bStateChangedFromOffHook = true;
                        bStateChangedFromRinging = false;
                    }

                    cal.set(Ds.lSchoolSchedule.iCurrentYear, Ds.lSchoolSchedule.iCurrentMonth,
                            Ds.lSchoolSchedule.iCurrentDate, Ds.structPC.AppHrMin / 100, Ds.structPC.AppHrMin % 100);
                    return;
                } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

                    AppBlock.isOutgoing = false;

                    if (System.currentTimeMillis() - CallHelper.sosOpened < 300000) {
                        if (settings != null && settings.getBoolean("sosAuto", CallHelper.Ds.structFCC.IsSosAutoAnswer)) {
                            answerCall(context);
                        }
                    }

                    DeBug.ShowLog(TAG, "EXTRA_STATE_RINGING ");
                    DeBug.ShowLog(TAG, TelephonyManager.EXTRA_STATE);
                    bStateChangedFromRinging = true;

                    //set time to current time for call log
                    cal.set(Ds.lSchoolSchedule.iCurrentYear, Ds.lSchoolSchedule.iCurrentMonth,
                            Ds.lSchoolSchedule.iCurrentDate, Ds.structPC.AppHrMin / 100, Ds.structPC.AppHrMin % 100);

                    int allowednoIndex;
                    boolean allowednoFound = false;
                    iWeekDay = Ds.structPC.bWeekDay;
                    if (!Ds.structPC.bTimeExpired) {
                        if (Ds.structPC.bMode == ePhoneMode.PARENT_RESTRICTED) {
                            incommingNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                            for (allowednoIndex = 0; allowednoIndex < Ds.structDNCC.bTotalNumbers; allowednoIndex++)
                                if (incommingNumber != null && (Ds.structDNCC.stDefunctNumber[allowednoIndex] != null) && (incommingNumber.indexOf(Ds.structDNCC.stDefunctNumber[allowednoIndex]) != -1)) {
                                    allowednoFound = true;
                                    break;
                                }
                            if (!allowednoFound) {
                                try {
                                    tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                                    Class c = Class.forName(tm.getClass().getName());
                                    Method m = c.getDeclaredMethod("getITelephony");
                                    m.setAccessible(true);
                                    telephonyService = (ITelephony) m.invoke(tm);
                                    if (!settings.getBoolean("unlocked", true)) {
                                        telephonyService.endCall();
                                    }
                                    boolean ExtNoMatch = true;
                                    if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.ALLOWED_NUMBERS_CALL) == eFeatureControl.ALLOWED_NUMBERS_CALL) {
                                        ContactListTable table = new ContactListTable(context);

                                        String Number = incommingNumber.replace(" ", "");
                                        StringBuffer bb = new StringBuffer(Number);

                                        if (Number.startsWith("091"))
                                            bb.delete(0, 3);
                                        else if (Number.startsWith("+91"))
                                            bb.delete(0, 3);
                                        else if (Number.startsWith("0"))
                                            bb.delete(0, 1);
                                        else if (Number.startsWith("+"))
                                            bb.delete(0, 1);

                                        ExtNoMatch = table.canBlockIncomingCall(bb.toString());
                                    }
                                    if (!ExtNoMatch) {
                                        int Hr = Ds.structPC.AppHrMin / 100;
                                        int Min = Ds.structPC.AppHrMin % 100;
                                        DeBug.ShowLog(Incoming, "Entered Incoming call drop " + Hr + ":" + Min + " res" + ((Ds.structNCC.lAllowedTime[iWeekDay][1][Hr] & MIN_LUT[Min]) != 0));
                                        DeBug.ShowLog(Incoming, "Entered Incoming call drop " + Hr + ":" + Min + " res" + ((Ds.structFCC.lAllowedTime[iWeekDay][19][Hr] & MIN_LUT[Min]) != 0));
                                        if ((Ds.structNCC.lAllowedTime[iWeekDay][0][Hr] & MIN_LUT[Min]) != 0) {
                                            telephonyService = (ITelephony) m.invoke(tm);
                                            boolean temp = telephonyService.endCall();
                                            DeBug.ShowLog(Incoming, "Ext number NOT Allowed");
                                        }
                                        if ((Ds.structFCC.lAllowedTime[iWeekDay][19][Hr] & MIN_LUT[Min]) != 0) {
                                            telephonyService = (ITelephony) m.invoke(tm);
                                            boolean temp = telephonyService.endCall();
                                            DeBug.ShowLog(Incoming, "Ext number NOT Allowed");
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
                    AppBlock.isOutgoing = true;
                    if (!TextUtils.isEmpty(Ds.structPC.iStudId))
                        if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.CALL_HIST) == eFeatureControl.CALL_HIST) {
                            iWeekDay = Ds.structPC.bWeekDay;
                            int Hr = Ds.structPC.AppHrMin / 100;
                            int Min = Ds.structPC.AppHrMin % 100;
                            Call_db = new DatabaseHandler_Call(context);
                            if ((Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iCALL_HIST][Hr] & MIN_LUT[Min]) != 0) {
                                DeBug.ShowLog("CallLog", "6 ");
                                new Thread(new Runnable() {
                                    @SuppressLint("NewApi")
                                    @Override
                                    public void run() {
                                        DeBug.ShowLog("CallLog", "7 ");
                                        try {
                                            Thread.sleep(500);
                                            getCallDetails(ctx, cal);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        }
                    return;
                }

                try {
                    if (!Ds.structPC.bTimeExpired)
                        if (Ds.structPC.bMode == ePhoneMode.PARENT_RESTRICTED && !Ds.structPC.bDateChangedToDefault)
                            if (Ds.structNCC.bTimeDurationCtrl[iWeekDay][1] != 0 || Ds.structNCC.bTimeDurationCtrl[iWeekDay][0] != 0)
                                CallStateListener(state);
                } catch (java.lang.Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void answerCall(final Context context) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
                            buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(
                                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
                            context.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
                            Intent headSetUnPluggedintent = new Intent(Intent.ACTION_HEADSET_PLUG);
                            headSetUnPluggedintent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
                            headSetUnPluggedintent.putExtra("state", 0);
                            headSetUnPluggedintent.putExtra("name", "Headset");
                            try {
                                context.sendOrderedBroadcast(headSetUnPluggedintent, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Intent i = new Intent(context, SOSAutoAnswerActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            context.startActivity(i);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }).start();
        }

    }

    ;

    private boolean checkForNumberAllowedAndTimeForSMS(Context context, String Number) {

        boolean isCurrentlyBlockedInTime = false;
        boolean isNumberAllowed = false;
        if (Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS]) {
            isNumberAllowed = false;
        } else {
            DeBug.ShowLog("features", "Day " + iWeekDay + " AppHrMin " + Ds.structPC.AppHrMin);

            if (Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] == 0) {
                Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] = false;
                Ds.structFCC.bPresentlyOn[eFeatureControl.iALLOWED_NUMBERS_SMS] = 3;
            } else {
                int Hr = Ds.structPC.AppHrMin / 100;
                int Min = Ds.structPC.AppHrMin % 100;
                isCurrentlyBlockedInTime = false;
                DeBug.ShowLogD("features", "WF lAllowedTime" + Long.toHexString(Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS][Hr]));
                if ((Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {

                    if ((Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS][Hr] & MIN_LUT[Min]) != 0) {
                        isCurrentlyBlockedInTime = true;
                        isNumberAllowed = true;
                    }
                    //	DeBug.ShowLog(Tag1,""+ packageName +"   HR "+Hr +" Min "+Min);

                }

                if (!isCurrentlyBlockedInTime)
                    if ((Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                        if (!Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS]) {
                            Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS]++;
                            Ds.structFCC.bPresentlyOn[eFeatureControl.iALLOWED_NUMBERS_SMS] = 1;
                            DeBug.ShowLog("features", "WF RESTRICTED_IN_DURATION " + Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] + " " + Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS]);
                            isNumberAllowed = true;
                        }
                        if (Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] / 60 >= Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS]) {
                            // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                            //1. when the activity is being stopped then it saves the current duration,
                            //2. if the duration has expired then stops the activity and save the current duration.
                            Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] = true;
                            Ds.structFCC.bPresentlyOn[eFeatureControl.iALLOWED_NUMBERS_SMS] = 3;
                            DeBug.ShowLog("features", "WF RESTRICTED_IN_DURATION . Used  Total.   " + Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS] / 60 + " " + Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iALLOWED_NUMBERS_SMS]);
                        }
                    }
            }
        }
        if (isNumberAllowed) {
            ContactListTable blockedNumbersDB = new ContactListTable(context);

            StringBuffer bb = new StringBuffer(Number);

            if (Number.startsWith("091"))
                bb.delete(0, 3);
            else if (Number.startsWith("+91"))
                bb.delete(0, 3);
            else if (Number.startsWith("0"))
                bb.delete(0, 0);

            isNumberAllowed = blockedNumbersDB.checkNumber(bb.toString(), 2);
            //blockedNumbersDB
        } else
            isNumberAllowed = true;

        return isNumberAllowed;

    }

    /**
     * Give the difference between two timings                   #GS
     */

    public static int TimeDiff(int EndTime, int AppHrMin) {
        int diff = 0;
        for (; EndTime > AppHrMin; ) {
            AppHrMin++;
            diff++;
            if (AppHrMin % 100 == 60) {
                AppHrMin = AppHrMin + 40;
            }
        }
        return diff;
    }

    /******************************************************
     * Function to Listen call States
     * *****************************************************/
    public void CallStateListener(String State) {

        iWeekDay = Ds.structPC.bWeekDay;
        DeBug.ShowLog(Outgoing, "PhoneStateListener state " + State);
        DeBug.ShowLog(Incoming, "PhoneStateListener state " + State);

        if (State.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
            //tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
            /*		    	Ds.structMCC.bStartIncomingCounter = false;
	    	Ds.structMCC.bStartOutgoingCounter = false;
	    	Ds.structMCC.iOutgoingCounter=0;*/

            //bStateChangedFromRinging=false;
            EndTime = System.currentTimeMillis();
            DeBug.ShowLog(Outgoing, "EndTime " + EndTime);

            if (bStateChangedFromRinging) {
                bStateChangedFromRinging = false;
                if (bStateChangedFromOffHook) {
                    DeBug.ShowLog(Incoming, "IncomingCounter " + ((EndTime - IncomingStartTime) / 1000));
                    bStateChangedFromOffHook = false;
                    // calculate and add the duration to the total duration
                    //temp_time= (short) (Ds.structNCC.wUsedDuration[iWeekDay][0]+((EndTime-IncomingStartTime)/1000));
                    //if(Ds.structNCC.bTimeDurationCtrl[iWeekDay][0]-temp_time>0)
                    Ds.structNCC.wUsedDuration[iWeekDay][0] += (short) ((EndTime - IncomingStartTime) / 1000);
                    if ((Ds.structNCC.wTotalDuration[iWeekDay][0] * 60 - Ds.structNCC.wUsedDuration[iWeekDay][0]) <= 6)
                        Ds.structNCC.wUsedDuration[iWeekDay][0] = (short) (Ds.structNCC.wTotalDuration[iWeekDay][0] * 60);

                    //if(Ds.structNCC.wUsedDuration[iWeekDay][0]<6)
                    //Ds.structNCC.wUsedDuration[iWeekDay][0]=0;
                    Ds.structNCC.bIncomingCallOn = 2;
                }
            } else {
                if (bOutGoingCallOn) {
                    DeBug.ShowLog(Outgoing, "OutgoingCounter " + ((EndTime - OutGoingStartTime)));
                    //subtracting 6 every time for compensate outgoing ringer time
                    if ((((EndTime - OutGoingStartTime) / 1000) - 6) > 0)
                        Ds.structNCC.wUsedDuration[iWeekDay][1] += (short) ((EndTime - OutGoingStartTime) / 1000) - 6;

                    if ((Ds.structNCC.wTotalDuration[iWeekDay][1] * 60 - Ds.structNCC.wUsedDuration[iWeekDay][1]) <= 6)
                        Ds.structNCC.wUsedDuration[iWeekDay][1] = (short) (Ds.structNCC.wTotalDuration[iWeekDay][1] * 60);

                    Ds.structNCC.bOutgoingCallOn = 2;
                    DeBug.ShowLog(Outgoing, "CALL_STATE_IDLE wUsedDuration " + Ds.structNCC.wUsedDuration[iWeekDay][1]);
                    bOutGoingCallOn = false;
                }
            }
            DeBug.ShowLog(Outgoing, "Ds.structNCC.wUsedDuration " + (Ds.structNCC.wUsedDuration[iWeekDay][1]));

			/*			//remove the Mute and set the Mute flag=false
			if( Ds.structNCC.bMuted )
			{
				AppBlockService.audioManager1.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
				Ds.structNCC.bMuted = false;
			}
			 */            // check if the AlarmManager is on then Cancel the AlarmManager
            if (Ds.structNCC.bAlarmManager[iWeekDay][0]) {
                CallDetectService.alarmManager = getAlarmanager(CallDetectService.callDetectService);
                CallDetectService.alarmManager.cancel(pi);
                Ds.structNCC.bAlarmManager[iWeekDay][0] = false;
                DeBug.ShowLog(Outgoing, "Alarm Stoped incoming");
            } else if (Ds.structNCC.bAlarmManager[iWeekDay][1]) {
                CallDetectService.alarmManager = getAlarmanager(CallDetectService.callDetectService);

                CallDetectService.alarmManager.cancel(pi);
                Ds.structNCC.bAlarmManager[iWeekDay][1] = false;
                DeBug.ShowLog(Outgoing, "Alarm Stoped outgoing ");
            }
            //IncomingStartTime = 0;
            // OutGoingStartTime = 0;
        } else if (State.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            if (bStateChangedFromRinging) {
                IncomingStartTime = System.currentTimeMillis();
                //	allowedDuration=(int) (allowedDuration-((IncomingStartTime-RingingStartTime)/1000));
                {
                    CallDetectService.alarmManager = getAlarmanager(CallDetectService.callDetectService);

                    CallDetectService.alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + allowedDuration * 1000, pi);
                    Ds.structNCC.bAlarmManager[iWeekDay][0] = true;
                    DeBug.ShowLog(Incoming, "Alarm Started InComingCall " + Ds.structPC.AppHrMin + "  " + allowedDuration);
                }//Ds.structNCC.bMuted= true;

                bStateChangedFromOffHook = true;
                Ds.structNCC.bIncomingCallOn = 1;
                //Ds.structMCC.bStartIncomingCounter = true;
                DeBug.ShowLog(Incoming, "incoming OFFHOOK " + bStateChangedFromRinging + " InComingStartTime " + IncomingStartTime);
            } else {
                //Ds.structMCC.bStartOutgoingCounter = true;
                OutGoingStartTime = System.currentTimeMillis();
                IncomingStartTime = 0;
                Ds.structNCC.bOutgoingCallOn = 1;
                DeBug.ShowLog(Outgoing, "Outgoing OFFHOOK " + " OutGoingStartTime " + OutGoingStartTime);
                DeBug.ShowLog(Outgoing, "OFFHOOK wUsedDuration " + Ds.structNCC.wUsedDuration[iWeekDay][1]);
            }
        } else if (State.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
            RingingStartTime = System.currentTimeMillis();
            //Ds.structMCC.bStartIncomingCounter=true;
            bStateChangedFromRinging = true;
            //IncomingStartTime = System.currentTimeMillis();
            OutGoingStartTime = 0;
			/*try {
			telephonyService.endCall();
		} catch (RemoteException e) {

			e.printStackTrace();
		}*/
            //Ds.structMCC.bStartIncomingCounter=true;
            DeBug.ShowLog(Incoming, "RINGING");
        }


    }

    public class MyPhoneStateListener extends PhoneStateListener {
        boolean bStateChangedFromRinging = false;
        boolean bStateChangedFromOffHook = false;

        // IncomingStartTime = 0;
        long OutGoingStartTime = 0;

        long EndTime;

        public void onCallStateChanged(int state, String incomingNumber) {
            iWeekDay = Ds.structPC.bWeekDay;
            DeBug.ShowLog(Outgoing, "PhoneStateListener state " + state);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    //tm.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
				/*		    	Ds.structMCC.bStartIncomingCounter = false;
		    	Ds.structMCC.bStartOutgoingCounter = false;
		    	Ds.structMCC.iOutgoingCounter=0;*/

                    //bStateChangedFromRinging=false;
                    EndTime = System.currentTimeMillis();
                    DeBug.ShowLog(Outgoing, "EndTime " + EndTime);

                    if (bStateChangedFromRinging) {
                        bStateChangedFromRinging = false;
                        if (bStateChangedFromOffHook) {
                            DeBug.ShowLog(Outgoing, "IncomingCounter " + ((EndTime - IncomingStartTime) / 1000));
                            bStateChangedFromOffHook = false;
                            // calculate and add the duration to the total duration
                            Ds.structNCC.wUsedDuration[iWeekDay][0] += (short) ((EndTime - IncomingStartTime) / 1000);
                            Ds.structNCC.bIncomingCallOn = 2;
                        }
                    } else {
                        if (bOutGoingCallOn) {
                            DeBug.ShowLog(Outgoing, "OutgoingCounter " + ((EndTime - OutGoingStartTime)));


                            //subtracting 6 every time for compensate outgoing ringer time
                            Ds.structNCC.wUsedDuration[iWeekDay][1] += (short) ((EndTime - OutGoingStartTime) / 1000) - 6;
                            Ds.structNCC.bOutgoingCallOn = 2;
                            DeBug.ShowLog(Outgoing, "CALL_STATE_IDLE wUsedDuration " + Ds.structNCC.wUsedDuration[iWeekDay][1]);
                            bOutGoingCallOn = false;
                        }
                    }
                    DeBug.ShowLog(Outgoing, "Ds.structNCC.wUsedDuration " + (Ds.structNCC.wUsedDuration[iWeekDay][1]));

				/*				//remove the Mute and set the Mute flag=false
				if( Ds.structNCC.bMuted )
				{
					AppBlockService.audioManager1.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
					Ds.structNCC.bMuted = false;
				}
				 */            // check if the AlarmManager is on then Cancel the AlarmManager
                    if (Ds.structNCC.bAlarmManager[iWeekDay][0]) {
                        CallDetectService.alarmManager.cancel(pi);
                        Ds.structNCC.bAlarmManager[iWeekDay][0] = false;
                        DeBug.ShowLog(Outgoing, "Alarm Stoped incoming");
                    } else if (Ds.structNCC.bAlarmManager[iWeekDay][1]) {
                        CallDetectService.alarmManager.cancel(pi);
                        Ds.structNCC.bAlarmManager[iWeekDay][1] = false;
                        DeBug.ShowLog(Outgoing, "Alarm Stoped outgoing ");
                    }
                    //IncomingStartTime = 0;
                    // OutGoingStartTime = 0;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (bStateChangedFromRinging) {
                        IncomingStartTime = System.currentTimeMillis();
                        bStateChangedFromOffHook = true;
                        Ds.structNCC.bIncomingCallOn = 1;
                        //Ds.structMCC.bStartIncomingCounter = true;
                        DeBug.ShowLog(Outgoing, "incoming OFFHOOK " + bStateChangedFromRinging + " InComingStartTime " + IncomingStartTime);
                    } else {
                        //Ds.structMCC.bStartOutgoingCounter = true;
                        OutGoingStartTime = System.currentTimeMillis();
                        IncomingStartTime = 0;
                        Ds.structNCC.bOutgoingCallOn = 1;
                        DeBug.ShowLog(Outgoing, "Outgoing OFFHOOK " + " OutGoingStartTime " + OutGoingStartTime);
                        DeBug.ShowLog(Outgoing, "OFFHOOK wUsedDuration " + Ds.structNCC.wUsedDuration[iWeekDay][1]);


                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    //Ds.structMCC.bStartIncomingCounter=true;
                    bStateChangedFromRinging = true;
                    //IncomingStartTime = System.currentTimeMillis();
                    OutGoingStartTime = 0;
				/*try {
				telephonyService.endCall();
			} catch (RemoteException e) {

				e.printStackTrace();
			}*/
                    //Ds.structMCC.bStartIncomingCounter=true;
                    DeBug.ShowLog(Outgoing, "RINGING");
                    break;
                default:
                    break;
            }
        }
    }


    /******************************************************
     * BroadcastReceiver for Incoming SMSs
     * *****************************************************/
    protected class MessageReceiver extends BroadcastReceiver {
        private SharedPreferences MessageReceivergetter;
        private SharedPreferences.Editor MessageReceivereditor;
        // All available column names in SMS table
        // [_id, thread_id, address,
        // person, date, protocol, read,
        // status, type, reply_path_present,
        // subject, body, service_center,
        // locked, error_code, seen]

        protected static final String SMS_EXTRA_NAME = "pdus";
        protected static final String SMS_URI = "content://sms";

        protected static final String ADDRESS = "address";
        protected static final String PERSON = "person";
        protected static final String DATE = "date";
        protected static final String READ = "read";
        protected static final String STATUS = "status";
        protected static final String TYPE = "type";
        protected static final String BODY = "body";
        protected static final String SEEN = "seen";

        protected static final int MESSAGE_TYPE_INBOX = 1;
        protected static final int MESSAGE_TYPE_SENT = 2;

        protected static final int MESSAGE_IS_NOT_READ = 0;
        protected static final int MESSAGE_IS_READ = 1;

        protected static final int MESSAGE_IS_NOT_SEEN = 0;
        protected static final int MESSAGE_IS_SEEN = 1;

        // Change the password here or give a user possibility to change it

        public void onReceive(final Context context, Intent intent) {
            SMS_db = new DatabaseHandler_SMS(context);
            MessageReceivergetter = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            MessageReceivereditor = MessageReceivergetter.edit();
            String tag = "INFO";
            //	int categories = 0;
            //	abortBroadcast();
            //	this.hashCode();
            DeBug.ShowLog(tag, "" + this.hashCode());

            if (Ds.msgrcvrhashcode != this.hashCode()) {
                context.unregisterReceiver(this);
                return;
            }
            //abortmessage=false;
            settingSMS = false;

			/* if(prevIntent!=null){

				 if(prevIntent.equals(intent))
				 return;
			 }
			 prevIntent=intent;*/
            Bundle bundle = intent.getExtras();
            DeBug.ShowToast(context, "msg recived ");
            if (bundle != null) {
                // do you manipulation on String then if you can abort.

            }
            Object messages[] = (Object[]) bundle.get("pdus");
            SmsMessage smsMessage[] = new SmsMessage[messages.length];
            ContentResolver contentResolver = context.getContentResolver();

            for (int n = 0; n < messages.length; n++) {
                smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
            }
            int allowednoIndex = 1;
            boolean allowednoFound = false;
            boolean ExtNoMatch = false;
            boolean bSpecialNoFound = false;
            int bNoIndex = 1;
            char cNoChar = 0;
            //abortmessage = true;


            //First check for the Signature
            Body = smsMessage[0].getMessageBody();
            SmsNumber = smsMessage[0].getOriginatingAddress();
            final long SMSTime = smsMessage[0].getTimestampMillis();
            Body = Body.trim();
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(SMSTime);
            SMSTimeStamp = formatter.format(calendar.getTime()).toString();
            DeBug.ShowToast(ctx, "SMS Time " + SMSTimeStamp);

            iWeekDay = Ds.structPC.bWeekDay;
			/*To check No. has a Non Digit char
			 * and less than 8 chars.
			 * */


            //for special Non-Digit Nos
            for (bNoIndex = 1; bNoIndex < SmsNumber.length(); bNoIndex++) {
                cNoChar = SmsNumber.charAt(bNoIndex);
                if (!Character.isDigit(cNoChar)) {
                    bSpecialNoFound = true;
                    break;
                }

            }

            // Special No, if it is not a full 10 digit No
            if (SmsNumber.length() < 8)
                bSpecialNoFound = true;

            // Ds.structPC.bFactoryControlByte:0 for checking that can parent change setting directly ?
            if (((Ds.structPC.bFactoryControlByte >> 0) & 1) == 1)
                for (allowednoIndex = 1; allowednoIndex < Ds.structDNCC.bTotalNumbers; allowednoIndex++)
                    if ((Ds.structDNCC.stDefunctNumber[allowednoIndex] != null) && (smsMessage[0].getOriginatingAddress().indexOf(Ds.structDNCC.stDefunctNumber[allowednoIndex]) != -1)) {
                        allowednoFound = true;
                        break;
                    }

            if (smsMessage[0].getOriginatingAddress().toUpperCase().indexOf(Ds.structDNCC.stDefunctNumber[0]) != -1) {
                DeBug.ShowLogD("SMS", "Factory no " + SmsNumber);
                allowednoIndex = 0;
            }
            //			  Toast toasta = DeBug.ShowToast(context,"Index "+allowednoIndex+" number "+smsMessage[0].getOriginatingAddress() + "ANF"+ allowednoFound);

            if ((allowednoIndex == 0) || (allowednoFound && !bSpecialNoFound && !Ds.structPC.bTimeExpired) && (SmsNumber.toLowerCase().contains("ginger") || SmsNumber.toLowerCase().contains("mobocn")) || SmsNumber.toLowerCase().contains("mobocn")) {

                if (Body.contains("MObiocean Registration"))
                    try {
                        Pattern pattern = Pattern.compile("[0-9]+");
                        final Matcher matcher = pattern.matcher(Body);
                        if (matcher.find()) {
                            final String str = matcher.group(0);

                            editor.putString("getOtp", str);
                            editor.commit();
                            DeBug.ShowLog("CheckSms", "CheckSms1 " + str);
                            Intent intent1 = new Intent("getOtp");
                            intent1.putExtra("otp", str);
                            // You can also include some extra data.

						/*intent1.putExtra("phyLocation", mPointsInfoStruct.getPointName());
						intent1.putExtra("lat", (double) Double.parseDouble(mPointsInfoStruct.getPointLat()));
						intent1.putExtra("lon", (double) Double.parseDouble(mPointsInfoStruct.getPointLong()));
						intent1.putExtra("time",  mPointsInfoStruct.getPointTimeRemaning());
						intent1.putExtra("dropAddress",  mPointsInfoStruct.getPointTimeRemaning());
						intent1.putExtra("time",  mPointsInfoStruct.getPointTimeRemaning());*/
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                            return;
                        }
                    } catch (Throwable e) {

                    }
                //DeBug.ShowToast(context, "You Are In Jail \n");


				/*if(allowednoIndex ==0) //that means the message is encrypted
				{

					try {
						Body = Body.substring(0, workingIndex)+StringCryptor.decrypt(Body.substring(workingIndex), Ds.structPC.stEDPassword);
						}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}*/

                // first try to remove the spaces if there are any while finding the characters of the Signature.
                // In this manner, we are able to look for the Signature with the conditions; it may not be there at all,
                // it may be there partially, it may have spaces in between.  if the Signature is not found then
                // just set the flag, Then try to look for the Password.
                // If the signature is there then okay, but the password has to be there.



				/*while((bodyLength > (workingIndex)) && matchIndex <5)
				{
					cLocalMD = Body.charAt(workingIndex);
					if((cLocalMD == ' ') || (cLocalMD == Ds.structPC.stSign[matchIndex]))
					{
						workingIndex++;
						if((cLocalMD == Ds.structPC.stSign[matchIndex]))
							matchIndex++;
					}
					else
						break;
				}*/

                ///////////////////////////////////////////////////////////////////////////////
                decodeMessage(context, Body);
                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                //The message is normal message from defunct numbers and it will get inserted in the inbox
            } //if(allowednoFound)
            else //that means the SMS is not from Defunct no.
            {
                //DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3\n 000 "+Ds.structNCC.bTimeDurationCtrl[iWeekDay][2]+"    "+abortmessage);
                DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3\n 000 " + Ds.structNCC.bTimeDurationCtrl[iWeekDay][2] + "    " + settingSMS);


                if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.INCOMING_SMS) == eFeatureControl.INCOMING_SMS) {
                    int Hr = Ds.structPC.AppHrMin / 100;
                    int Min = Ds.structPC.AppHrMin % 100;
                    if ((Ds.structNCC.lAllowedTime[iWeekDay][2][Hr] & MIN_LUT[Min]) != 0) {
//						abortBroadcast();
                        deleteSMS(context);
                        //abortmessage = true;
                        settingSMS = true;
                    }
                }

                if (Ds.structPC.bMode == ePhoneMode.PARENT_RESTRICTED && !bSpecialNoFound && !Ds.structPC.bTimeExpired)
                    if (((Ds.structNCC.bPhoneFeatureCtrl & ePhoneFeatureCtrl.CALL_NO_SMS) != ePhoneFeatureCtrl.CALL_NO_SMS) && !Ds.structNCC.bPresentlyStopped[iWeekDay][2]) {
                        ExtNoMatch = true;

						/*
						 CL0 number resricted without limits
						 CL1 number  are not restricted but in limits
						 CL8 number restrcted but in limits
//						 		number  are not resrcted without limits
						 */

                        //DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3\n 111 "+Ds.structNCC.bTimeDurationCtrl[iWeekDay][2]+"    "+abortmessage);
                        DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3\n 111 " + Ds.structNCC.bTimeDurationCtrl[iWeekDay][2] + "    " + settingSMS);

                        boolean ExtTDCCallNotAllowed = false;

                        if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.ALLOWED_NUMBERS_SMS) == eFeatureControl.ALLOWED_NUMBERS_SMS)
                            ExtNoMatch = checkForNumberAllowedAndTimeForSMS(context, SmsNumber);
						/*if((Ds.structNCC.bTimeDurationCtrl[iWeekDay][2] & eFeatureSetting.RESTRICTED_IN_NUMBERS) == eFeatureSetting.RESTRICTED_IN_NUMBERS ) //check for allowed no.
							{
								//    	   check for the allowed no;
								for(int i=0; i<Ds.structNCC.bTotalNumbers; i++)
									if ((Ds.structNCC.stEXT_ALWD_Numbers[i] !=null) && (incommingNumber.indexOf(Ds.structNCC.stEXT_ALWD_Numbers[i]) !=-1))
									{
										ExtNoMatch = true;
										break;
									}*/
                        if (!ExtNoMatch) {
                            //					    	        	DeBug.ShowToast(context,incommingNumber);
//							abortBroadcast();
                            deleteSMS(context);
                            //abortmessage = true;
                            settingSMS = true;
                            SMSCounter++;
                            ExtTDCCallNotAllowed = true;
                            DeBug.ShowLog(Outgoing, "Ext number NOT Allowed");
                        }
                        //}

                        //Regulate Incomming SMS
                        if (ExtNoMatch)
                            if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.INCOMING_SMS) == eFeatureControl.INCOMING_SMS)
                                if (Ds.structNCC.bTimeDurationCtrl[iWeekDay][2] != 0) //for SMSs
                                {


                                    if ((Ds.structNCC.bTimeDurationCtrl[iWeekDay][2] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {

                                        int Hr = Ds.structPC.AppHrMin / 100;
                                        int Min = Ds.structPC.AppHrMin % 100;
                                        if ((Ds.structNCC.lAllowedTime[iWeekDay][2][Hr] & MIN_LUT[Min]) != 0) {
//											abortBroadcast();
                                            deleteSMS(context);
                                            //abortmessage = true;
                                            settingSMS = true;
                                        }

                                    }
                                    DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3  3 22222 \n" + Ds.structNCC.wUsedDuration[iWeekDay][2] + " / / / " + settingSMS);


                                    //	DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3  3 3333  \n"+Ds.structNCC.wUsedDuration[iWeekDay][2] + " / / / "+abortmessage);

                                    //if(!abortmessage)
                                    if (!settingSMS)
                                        if (((Ds.structNCC.bTimeDurationCtrl[iWeekDay][2] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) && !ExtTDCCallNotAllowed) {
                                            Ds.structNCC.wUsedDuration[iWeekDay][2]++;
                                            //DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3   2  \n"+Ds.structNCC.wUsedDuration[iWeekDay][2] +  " TD  "+ Ds.structNCC.wTotalDuration[iWeekDay][2] +"     / / / /   "+abortmessage);

                                            DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3   2  \n" + Ds.structNCC.wUsedDuration[iWeekDay][2] + " TD  " + Ds.structNCC.wTotalDuration[iWeekDay][2] + "     / / / /   " + settingSMS);

                                            if (Ds.structNCC.wUsedDuration[iWeekDay][2] > Ds.structNCC.wTotalDuration[iWeekDay][2]) {
                                                Ds.structNCC.bPresentlyStopped[iWeekDay][2] = true;
                                                MessageReceivereditor.putBoolean("structNCC.bPresentlyStopped" + iWeekDay + "" + 2, Ds.structNCC.bPresentlyStopped[iWeekDay][2]);
                                                MessageReceivereditor.commit();

                                                abortBroadcast();
                                                //	abortmessage = true;
                                                settingSMS = true;
                                                SMSCounter++;
                                                //	DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3  3  \n"+Ds.structNCC.wUsedDuration[iWeekDay][2] + " / / / "+abortmessage);
                                                DeBug.ShowToast(context, "RESTRICTED_IN_DURATION . SMS3  3  \n" + Ds.structNCC.wUsedDuration[iWeekDay][2] + " / / / " + settingSMS);

                                            }
                                        }

                                } else {/*

							for(allowednoIndex=0;allowednoIndex<Ds.structNCC.bTotalNumbers ; allowednoIndex++)
								if((Ds.structNCC.stEXT_ALWD_Numbers[allowednoIndex]!=null) && (smsMessage[0].getOriginatingAddress().indexOf(Ds.structNCC.stEXT_ALWD_Numbers[allowednoIndex])!=-1))
								{
									ExtNoMatch = true
									break;
								}
							DeBug.ShowToast(context,"IndexSMS "+allowednoIndex+"no "+smsMessage[0].getOriginatingAddress());
							if(ExtNoMatch)
							{
								ExtNoMatch = false;
								// try to see if signature is there and if it is there then abort it.
							DeBug.ShowToast(context,"Ext Allowed no Found ");
							} //if(allowednoFound)
							else
							{
								abortBroadcast();
								abortmessage = true;
								SMSCounter++;
							}
								 */
                                }

                    }  //if (((Ds.structNCC.bPhoneFeatureCtrl & ePhoneFeatureCtrl.CALL_NO_SMS
                    else {
                        //	abortBroadcast();
                        //	abortmessage = true;
                        SMSCounter++;
                    }

                /**Part of B2B  **/
                //Store inComing SMSLogs in database
                if (!TextUtils.isEmpty(Ds.structPC.iStudId))
                    if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SMS_HIST) == eFeatureControl.SMS_HIST) {
                        boolean canLogSMS = false;
                        Call_db = new DatabaseHandler_Call(context);
                        iWeekDay = Ds.structPC.bWeekDay;
                        int Hr = Ds.structPC.AppHrMin / 100;
                        int Min = Ds.structPC.AppHrMin % 100;
                        if ((Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iSMS_HIST][Hr] & MIN_LUT[Min]) != 0)
                            canLogSMS = true;
                        if (canLogSMS)
                            new Thread(new Runnable() {
                                @SuppressLint("NewApi")
                                @Override
                                public void run() {
                                    try {
                                        int responce = -1;
                                        String From = "";
                                        String To = "";
                                        int isIncoming = 0;
                                        From = SmsNumber;
                                        isIncoming = 1;
                                        LocationBean location = new LocationDetails(context).getLocation();
                                        RestApiCall mRestApiCall = new RestApiCall();
                                        JSONObject json = new JSONObject();
                                        try {
                                            json.put("Lat", location.Lat);
                                            json.put("Long", location.Longt);
                                            json.put("startDateTime", SMSTimeStamp);
                                            json.put("IsIncoming", isIncoming);
                                            json.put("AppId", Ds.structPC.iStudId);
                                            json.put("From", From);
                                            json.put("To", To);
                                            json.put("MsgText", Body);
                                            json.put("cellId", location.CellId);
                                            json.put("locationAreaCode", location.LAC);
                                            json.put("mobileCountryCode", location.MCC);
                                            json.put("mobileNetworkCode", location.MNC);
                                            json.put("LogDateTime", GetTimeWithDate());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        responce = Integer.parseInt(mRestApiCall.SMSLogs(json));
                                        DeBug.ShowLog("CallDB", "type :" + 1 + " Number: " + SmsNumber + "\nregistered at: " + SMSTimeStamp + " SMS " + Body);
                                        if (responce < 0) {
                                            Struct_SMS mStruct_SMS = new Struct_SMS("Ram", SmsNumber, SMSTime, Body, 1, "" + location.Lat, location.Longt);
                                            mStruct_SMS.setCellId(location.CellId);
                                            mStruct_SMS.setLAC(location.LAC);
                                            mStruct_SMS.setMCC(location.MCC);
                                            mStruct_SMS.setMNC(location.MNC);
                                            mStruct_SMS.setLogDateTime(GetTimeWithDate());
                                            SMS_db.addSMS(mStruct_SMS);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                    }

            } ////that means the SMS is not from Defunct no.
            DeBug.ShowToast(context, "Received SMS: " + smsMessage[0].getOriginatingAddress() + "\nBody: " + smsMessage[0].getMessageBody());

            if (settingSMS) {
                abortBroadcast();
                settingSMS = false;
            } else if (abortmessage) {
                Body = Body.replaceAll("'", "''");
                if (!(Ds.sms_numberlist.contains(SmsNumber))) {
                    Ds.id++;
                    Ds.no_tableid = Ds.id;
                    Ds.sms_numberlist.add(0, SmsNumber);
                    Ds.sms_numberFlaglist.add(0, 1);
                } else {
                    int p = Ds.sms_numberlist.indexOf(SmsNumber);
                    Ds.sms_numberlist.remove(p);
                    Ds.sms_numberlist.add(0, SmsNumber);

                    int unread = Ds.sms_numberFlaglist.get(p);
                    Ds.sms_numberFlaglist.remove(p);
                    Ds.sms_numberFlaglist.add(0, ++unread);
                }
                MessageReceivereditor.putInt("Ds.sms_numberlist_size", Ds.sms_numberlist.size());
                for (int i = 0; i < Ds.sms_numberlist.size(); i++) {
                    MessageReceivereditor.putString("Ds.sms_numberlist" + i, Ds.sms_numberlist.get(i));
                    MessageReceivereditor.putInt("Ds.sms_numberFlaglist" + i, Ds.sms_numberFlaglist.get(i));
                }
                MessageReceivereditor.commit();

                abortmessage = false;
                abortBroadcast();
            }
        }

        public void deleteSMS(Context context) {

            Uri inboxUri = Uri.parse("content://sms/inbox");
            int count = 0;
            Cursor c = context.getContentResolver().query(inboxUri, null, null, null, null);
            if (c != null) {
                while (c.moveToNext()) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            String pid = c.getString(0);
                            String uri = "content://sms/" + pid;
                            count = context.getContentResolver().delete(Uri.parse(uri), null, null);
                            DeBug.ShowLog("log>>>", "affected rows " + count);
                        } else {
                            abortBroadcast();
                        }
                    } catch (Exception ignore) {
                    }
                }
                c.close();
            }
        }

    }

    ;//MessageReceiver BroadcastReceiver


    /******************************************************
     * Function to Decode Incoming SMSs from Server
     * *****************************************************/
    public static void decodeMessage(Context context, String Body) {
        if (Body == null)
            return;
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        store_data = context.getSharedPreferences(store_DataForEmployee, Context.MODE_PRIVATE);
        editor_Sumer = store_data.edit();


        try {
            blockedWebsitesPreferences = context.getSharedPreferences(BLOCKED_WEBSITES_PREFS_NAME, Context.MODE_PRIVATE);
            blockedWebsitesPreferenceEditor = blockedWebsitesPreferences.edit();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }

        int phoneNoIndex;
        int bodyLength = 0;
        int signValue = 0;
        String workingString = null;
        char cLocalMD;
        int matchIndex = 0;

        bodyLength = Body.length();
        workingIndex = 0;


        /****Create Gingerbox folder having Attendance folder inside it(For Attendance GBox set as SL1)****/
        filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, "Gingerbox");
        file1 = new File(file.getAbsolutePath(), "Attendance");

        if (!file.exists()) {
            file.mkdir();
            file1.mkdir();
        }

        while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
            workingIndex++;

        if (bodyLength > (workingIndex + 10)) //&& (matchIndex ==5)
        {
            workingString = Body.substring(workingIndex, workingIndex + 11);
            //Toast toast7 = DeBug.ShowToast(context,"WIndex "+workingIndex +" bodyLength "+bodyLength +"Password= "+Ds.structPC.stPassword , Toast.LENGTH_LONG);toast7.show();
            DeBug.ShowLog("TESTSMS", "Pass " + workingString);
            if (workingString.equalsIgnoreCase(Ds.structPC.stPassword)) {
                workingIndex = workingIndex + 11;
				/*if(allowednoIndex ==0) //that means the message is encrypted
				{

				try
				{
					Body= Body.substring(0, workingIndex+1)+StringCryptor.decrypt(Body.substring(workingIndex+1), Ds.structPC.stEDPassword);
				}
				catch (Exception e) {

					e.printStackTrace();
				}
				System.out.println(Body);
			}*/
                while ((workingIndex + 2) <= bodyLength) {
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    workingString = Body.substring(workingIndex, workingIndex + 2).toUpperCase();
                    signValue = (byte) (workingString.charAt(0));
                    signValue = (int) (signValue * 256 + (byte) (workingString.charAt(1)));
                    workingIndex = workingIndex + 2;
                    DeBug.ShowLog("TESTMSG", "Sig " + workingString + " At " + (workingIndex - 2));
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                    workingIndex = decodeSettings(signValue, 0, bodyLength, context, Body, workingIndex);

                }
                settingSMS = true;
            } else {
                //if (workingString.equalsIgnoreCase("mesg")) //that means it is a protected message
                {
                    DeBug.ShowToast(context, "There is NO Password ");
                    //putSmsToDatabase( contentResolver, smsMessage[0]);
                    //AndroidCalls.mydb.execSQL("INSERT INTO sms_inbox VALUES('" + Body + "', '" + SmsNumber + "', '" + SMSTimeStamp + "');");
                }
                //else
                {
                    //abortmessage = true;
                    SMSCounter++;
                }

            }
        }

    }

    private static int decodeSettings(int signValue, int allowednoIndex, int bodyLength, Context context, String Body, int workingIndex) {

        int phoneNoIndex = 0;
        String workingString = null;
        String Number = null;
        byte featureValue = 0;
        char cLocalMD = 0;
        int matchIndex = 0;
        int categories;
        String IndexInMsg;

        switch (signValue) {
            case 0x4344:  //CD Child ID CD 1234567890
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));


                    {
                        mtelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
                        String id = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                        Ds.structPC.iStudId = id;
                        DeBug.ShowLog("TESTMSG", "CD " + Ds.structPC.iStudId);

                    }
                    workingIndex = workingIndex + phoneNoIndex - 1;

                    editor.putString("structPC.iStudId", Ds.structPC.iStudId);

                    editor.commit();
                }
                break;
            case 0x5343:  //SC SIM change SC 1234567890
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));
                    if (phoneNoIndex == 1)
                        Ds.structDNCC.stDefunctNumber[1] = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            mtelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);

                            Ds.structPC.stPhoneNo = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                            DeBug.ShowLog("TESTMSG", "SC " + Ds.structPC.stPhoneNo);
                            try {
                                Ds.structPC.stSIMSerialno = mtelephonyManager.getSimSerialNumber();
                                DeBug.ShowLog("TESTMSG", "SC " + Ds.structPC.stSIMSerialno);
                            } catch (java.lang.Exception e) {
                                Ds.structPC.stSIMSerialno = "";
                                DeBug.ShowLog("TESTMSG", "SC " + Ds.structPC.stSIMSerialno);
                                e.printStackTrace();
                            }
                            Ds.structPC.bSimChanged = false;
                        }
                        workingIndex = workingIndex + phoneNoIndex - 1;
                    }

                    editor.putString("structPC.stPhoneNo", Ds.structPC.stPhoneNo);
                    editor.putString("structPC.stSIMSerialno", Ds.structPC.stSIMSerialno);
                    editor.putBoolean("structPC.bSimChanged", Ds.structPC.bSimChanged);
                    editor.commit();
                }
                break;
            case 0x5254: //for "RT" "Request Time"  RT 12-30-13 5 5 5PM ////mm-dd-yy hr min secPM/AM

                String currentdate = Body.substring(workingIndex, workingIndex + 8);

                workingIndex = workingIndex + 9;

                DeBug.ShowLog("TESTMSG", "RT " + currentdate);
                int[] hrmin = new int[2];
                int hhmm = 0;
                while (bodyLength > (workingIndex) && Character.isDigit(Body.charAt(workingIndex)) && hhmm < 2) {

                    IndexInMsg = "";

                    while (bodyLength > (workingIndex) && Character.isDigit(Body.charAt(workingIndex))) {
                        cLocalMD = Body.charAt(workingIndex);

                        IndexInMsg = IndexInMsg + cLocalMD;

                        workingIndex++;
                    }
                    int index = 0;
                    if (Character.isDigit(cLocalMD)) {
                        index = Integer.parseInt(IndexInMsg);

                        hrmin[hhmm] = index;
                        DeBug.ShowLog("TESTMSG", "RT 2 " + hrmin[hhmm] + "  " + hhmm);
                        hhmm++;
                    }

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                }
                String ampm = Body.substring(Body.length() - 2);
                if (ampm.equalsIgnoreCase("PM")) {
                    if (hrmin[0] != 12)
                        hrmin[0] = hrmin[0] + 12;
                } else if (ampm.equalsIgnoreCase("AM")) {
                    if (hrmin[0] == 12)
                        hrmin[0] = 0;
                }

                Ds.lSchoolSchedule.iCurrentDate = Integer.parseInt(currentdate.substring(0, 2));
                Ds.structPC.iCurrentDate = Ds.lSchoolSchedule.iCurrentDate;
                Ds.lSchoolSchedule.iCurrentMonth = Integer.parseInt(currentdate.substring(3, 5)) - 1;
                Ds.structPC.iCurrentMonth = Ds.lSchoolSchedule.iCurrentMonth;
                Ds.lSchoolSchedule.iCurrentYear = Integer.parseInt(currentdate.substring(6, 8)) + 2000;
                Ds.structPC.iCurrentYear = Ds.lSchoolSchedule.iCurrentYear;
                Ds.lSchoolSchedule.CurrentDate.set(Ds.lSchoolSchedule.iCurrentYear, Ds.lSchoolSchedule.iCurrentMonth, Ds.lSchoolSchedule.iCurrentDate);
                CallHelper.Ds.structPC.bWeekDay = (byte) (Ds.lSchoolSchedule.CurrentDate.get(Calendar.DAY_OF_WEEK) - 1);
                Ds.structPC.AppHrMin = hrmin[0] * 100 + hrmin[1];
                try {
                    OneMinuteTimerService.tempAppHrMin = Ds.structPC.AppHrMin;
                } catch (java.lang.Exception e1) {
                    e1.printStackTrace();
                }
                Ds.structPC.bDateChangedToDefault = false;
                int weekDay = (byte) (Ds.lSchoolSchedule.CurrentDate.get(Calendar.DAY_OF_WEEK) - 1);
                if (weekDay != settings.getInt("structPC.bWeekDay", Ds.structPC.bWeekDay)) {
                    if (weekDay == 0) {
                        for (int day = 0; day < 7; day++) {
                            for (int i1 = 0; i1 < Ds.structFCC.bTotalFeatures; i1++) {
                                Ds.structFCC.wUsedDuration[day][i1] = 0;
                                Ds.structFCC.bPresentlyStopped[day][i1] = false;
                                editor.putInt("structFCC.wUsedDuration" + day + "" + i1, Ds.structFCC.wUsedDuration[day][i1]);
                                editor.putBoolean("structFCC.bPresentlyStopped" + day + "" + i1, Ds.structFCC.bPresentlyStopped[day][i1]);

                            }
                            for (int i1 = 0; i1 < 3; i1++) {
                                Ds.structNCC.wUsedDuration[day][i1] = 0;
                                Ds.structNCC.bPresentlyStopped[day][i1] = false;
                                editor.putInt("structNCC.wUsedDuration" + day + "" + i1, Ds.structNCC.wUsedDuration[day][i1]);
                                editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + i1, Ds.structNCC.bPresentlyStopped[day][i1]);
                            }
                        }
                    }
                }
                Ds.structPC.bWeekDay = (byte) (Ds.lSchoolSchedule.CurrentDate.get(Calendar.DAY_OF_WEEK) - 1);
                editor.putInt("structPC.AppHrMin", Ds.structPC.AppHrMin);
                editor.putInt("lSchoolSchedule.iCurrentDate", Ds.lSchoolSchedule.iCurrentDate);
                editor.putInt("lSchoolSchedule.iCurrentMonth", Ds.lSchoolSchedule.iCurrentMonth);
                editor.putInt("lSchoolSchedule.iCurrentYear", Ds.lSchoolSchedule.iCurrentYear);
                editor.putInt("structPC.bWeekDay", Ds.structPC.bWeekDay);
                editor.commit();
                DeBug.ShowLog("TESTMSG", "RT 3 " + Ds.structPC.AppHrMin + " " + Ds.lSchoolSchedule.iCurrentYear + " " + Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH) + " " + Ds.lSchoolSchedule.CurrentDate.get(Calendar.DATE));

                workingIndex = Body.length();
                settingSMS = true;
                break;
            case 0x5355://for SU school code update

                phoneNoIndex = 0;
                do {
                    if (bodyLength > (workingIndex + phoneNoIndex))
                        cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                    else {
                        phoneNoIndex++;
                        break;
                    }
                    phoneNoIndex++;
                } while (cLocalMD != ' ' || bodyLength < (workingIndex + phoneNoIndex));
                workingString = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                Ds.lSchoolSchedule.stSchoolCode = workingString;
                editor.putString("lSchoolSchedule.stSchoolCode", Ds.lSchoolSchedule.stSchoolCode);
                editor.commit();
                workingIndex = workingIndex + phoneNoIndex;
                DeBug.ShowLog("TESTMSG", ":" + workingString + ":");
                settingSMS = true;
                break;

            case 0x5557://for UW update wifi

                int indxx = 0;
                String uwCred[] = new String[3];
                while ((bodyLength > workingIndex) && indxx < 4) {
                    int ind = 0;
                    ind = Body.indexOf(" ", workingIndex);
                    if (ind < 0)
                        ind = bodyLength;
                    uwCred[indxx] = Body.substring(workingIndex, ind);
                    indxx++;
                    workingIndex = ind + 1;
                }

                if (indxx == 3) {
                    Ds.lSchoolSchedule.stBSSID = uwCred[0];
                    Ds.lSchoolSchedule.stSSID = uwCred[1];
                    Ds.lSchoolSchedule.stPass = uwCred[2];

                    editor.putString("lSchoolSchedule.stBSSID", uwCred[0]);
                    editor.putString("lSchoolSchedule.stSSID", uwCred[1]);
                    editor.putString("lSchoolSchedule.stPass", uwCred[2]);

                    editor.commit();
                }
                DeBug.ShowLog("TESTMSG", ":" + workingString + ":");
                settingSMS = true;
                break;

            case 0x5353://for SS school mode

                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    int featureIndex = (byte) ((byte) cLocalMD - (byte) 48);
                    if (featureIndex == 1) {
                        context.startService(new Intent(context, DownloadSecuredIntentService.class));
                    }
                    workingIndex++;
                }
                while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                    workingIndex++;
                settingSMS = true;

                break;
            case 0x5357://for SW school mode

                String ss = null;

                ss = Body.substring(workingIndex, workingIndex + 2);
                int dd = Integer.parseInt(ss);
                DeBug.ShowLog("TESTMSG", "SW  dd " + dd);
                ss = Body.substring(workingIndex + 2, workingIndex + 4);
                int mm = Integer.parseInt(ss) - 1;
                DeBug.ShowLog("TESTMSG", "SW  mm " + mm);
                ss = Body.substring(workingIndex + 4, workingIndex + 6);
                int yy = Integer.parseInt(ss) + 2000;
                DeBug.ShowLog("TESTMSG", "SW  yy " + yy);
                Calendar date = Calendar.getInstance();
                date.set(yy, mm, dd);
                DeBug.ShowLog("TESTMSG", "SW  yy " + yy);

                for (iselectedscholslot = 0; iselectedscholslot < Ds.lSchoolSchedule.bTotalSchoolSchedule; iselectedscholslot++) {
                    DeBug.ShowLog("TESTMSG", "SW  loop " + " iStartDate " + Ds.lSchoolSchedule.iStartDate[iselectedscholslot]);

                    if (Ds.lSchoolSchedule.iStartDate[iselectedscholslot] == dd && Ds.lSchoolSchedule.iStartMonth[iselectedscholslot] == mm && Ds.lSchoolSchedule.iStartYear[iselectedscholslot] == yy) {
                        DeBug.ShowLog("TESTMSG", "SW  yy" + ss + " lSchoolSchedule.length " + Ds.lSchoolSchedule.iStartDate[iselectedscholslot]);
                        break;
                    }
                }
                DeBug.ShowLog("TESTMSG", "SW  k 1 " + iselectedscholslot);
                if (iselectedscholslot >= Ds.lSchoolSchedule.bTotalSchoolSchedule)
                    for (iselectedscholslot = 0; iselectedscholslot < Ds.lSchoolSchedule.bTotalSchoolSchedule; iselectedscholslot++) {
                        if (Ds.lSchoolSchedule.iStartDate[iselectedscholslot] == 0) {
                            break;
                        }
                    }
                DeBug.ShowLog("TESTMSG", "SW  k 2 " + iselectedscholslot);
                Ds.lSchoolSchedule.StartDate[iselectedscholslot].set(yy, mm, dd);
                Ds.lSchoolSchedule.iStartDate[iselectedscholslot] = dd;
                Ds.lSchoolSchedule.iStartMonth[iselectedscholslot] = mm;
                Ds.lSchoolSchedule.iStartYear[iselectedscholslot] = yy;
                DeBug.ShowLog("TESTMSG", "SW  k 2 " + iselectedscholslot + " iStartDate " + Ds.lSchoolSchedule.iStartDate[iselectedscholslot]);

                DeBug.ShowLog("TESTMSG", "SW  k 3 " + iselectedscholslot);
                workingIndex = workingIndex + 6;
                while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                    workingIndex++;
                settingSMS = true;

                break;
            case 0x4557://for EW school mode
                //	Ds.lSchoolSchedule[1].;
                ss = Body.substring(workingIndex, workingIndex + 2);
                dd = Integer.parseInt(ss);
                DeBug.ShowLog("TESTMSG", "EW  dd" + dd);
                ss = Body.substring(workingIndex + 2, workingIndex + 4);
                mm = Integer.parseInt(ss) - 1;
                DeBug.ShowLog("TESTMSG", "EW  mm" + mm);
                ss = Body.substring(workingIndex + 4, workingIndex + 6);
                yy = Integer.parseInt(ss) + 2000;
                DeBug.ShowLog("TESTMSG", "EW  yy" + yy);
                DeBug.ShowLog("TESTMSG", "EW  k 2 " + iselectedscholslot);
                Ds.lSchoolSchedule.iCurrentSchoolSchedule = -1;
                Ds.lSchoolSchedule.EndDate[iselectedscholslot].set(yy, mm, dd);
                Ds.lSchoolSchedule.iEndDate[iselectedscholslot] = dd;
                Ds.lSchoolSchedule.iEndMonth[iselectedscholslot] = mm;
                Ds.lSchoolSchedule.iEndYear[iselectedscholslot] = yy;
                DeBug.ShowLog("TESTMSG", "EW  k 3 " + iselectedscholslot + " iStartDate " + Ds.lSchoolSchedule.iEndDate[iselectedscholslot]);

                workingIndex = workingIndex + 6;
                while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                    workingIndex++;
                workingIndex = updateSchoolTimeDurationCtrl(Body, context, workingIndex);

                editor.putInt("lSchoolSchedule.iStartDate" + iselectedscholslot, Ds.lSchoolSchedule.iStartDate[iselectedscholslot]);
                editor.putInt("lSchoolSchedule.iStartMonth" + iselectedscholslot, Ds.lSchoolSchedule.iStartMonth[iselectedscholslot]);
                editor.putInt("lSchoolSchedule.iStartYear" + iselectedscholslot, Ds.lSchoolSchedule.iStartYear[iselectedscholslot]);

                editor.putInt("lSchoolSchedule.iEndDate" + iselectedscholslot, Ds.lSchoolSchedule.iEndDate[iselectedscholslot]);
                editor.putInt("lSchoolSchedule.iEndMonth" + iselectedscholslot, Ds.lSchoolSchedule.iEndMonth[iselectedscholslot]);
                editor.putInt("lSchoolSchedule.iEndYear" + iselectedscholslot, Ds.lSchoolSchedule.iEndYear[iselectedscholslot]);

                editor.commit();
                settingSMS = true;

                break;
            case 0x4155: //for "AU" "Add Blocked URL"  AU www.asd.cm www.sad.co.in

                while ((bodyLength > (workingIndex)) && Character.isLowerCase(Body.charAt(workingIndex))) {
                    phoneNoIndex = 0;
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (cLocalMD != ' ');

                    workingString = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                    String encryp = null;
                    int data = blockedWebsitesPreferences.getInt("stBlockedWebsitesName_Size", 0);
                    Ds.structFCC.stBlockedWebsitesName.clear();
                    for (int i = 0; i < data; i++) {
                        String WebSite = blockedWebsitesPreferences.getString("" + i, "");
                        DeBug.ShowLog("EN", "" + WebSite);
                        String decyptedWebsites;
                        try {
                            decyptedWebsites = StringCryptor.decrypt(WebSite, StringCryptor.encryptionKey);
                            DeBug.ShowLog("EN", "" + decyptedWebsites);
                            Ds.structFCC.stBlockedWebsitesName.add(decyptedWebsites);
                        } catch (java.lang.Exception e) {
                            e.printStackTrace();
                        }

                    }

                    if (!Ds.structFCC.stBlockedWebsitesName.contains(workingString)) {
                        DeBug.ShowLog("TESTMSG", "AU " + workingString);
                        Ds.structFCC.stBlockedWebsitesName.add(workingString);
                        int size = Ds.structFCC.stBlockedWebsitesName.size();
                        try {
                            encryp = StringCryptor.encrypt(workingString, StringCryptor.encryptionKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        blockedWebsitesPreferenceEditor.putInt("stBlockedWebsitesName_Size", size);
                        blockedWebsitesPreferenceEditor.putString("" + (size - 1), encryp);

                    }

                    workingIndex = workingIndex + phoneNoIndex - 1;

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                }

                blockedWebsitesPreferenceEditor.commit();
                settingSMS = true;

                break;
            case 0x5255: //for "RU" "Remove Blocked Url"  RU www.asd.cm www.sad.co.in

                String encryp = null;
                while ((bodyLength > (workingIndex)) && Character.isLowerCase(Body.charAt(workingIndex))) {
                    phoneNoIndex = 0;
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (cLocalMD != ' ');

                    workingString = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                    workingIndex = workingIndex + phoneNoIndex - 1;
                    DeBug.ShowLog("TESTMSG", "RU " + workingString);
                    int data = blockedWebsitesPreferences.getInt("stBlockedWebsitesName_Size", 0);
                    Ds.structFCC.stBlockedWebsitesName.clear();
                    for (int i = 0; i < data; i++) {
                        String WebSite = blockedWebsitesPreferences.getString("" + i, "");
                        DeBug.ShowLog("EN", "" + WebSite);
                        String decyptedWebsites;
                        try {
                            decyptedWebsites = StringCryptor.decrypt(WebSite, StringCryptor.encryptionKey);
                            DeBug.ShowLog("EN", "" + decyptedWebsites);
                            Ds.structFCC.stBlockedWebsitesName.add(decyptedWebsites);
                        } catch (java.lang.Exception e) {
                            e.printStackTrace();
                        }

                    }

                    if (Ds.structFCC.stBlockedWebsitesName.contains(workingString))
                        Ds.structFCC.stBlockedWebsitesName.remove(workingString);

                    blockedWebsitesPreferenceEditor.putInt("stBlockedWebsitesName_Size", Ds.structFCC.stBlockedWebsitesName.size());
                    for (int i = 0; i < Ds.structFCC.stBlockedWebsitesName.size(); i++) {
                        try {
                            encryp = StringCryptor.encrypt(Ds.structFCC.stBlockedWebsitesName.get(i), StringCryptor.encryptionKey);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        blockedWebsitesPreferenceEditor.putString("" + i, encryp);
                    }
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                }
                blockedWebsitesPreferenceEditor.commit();
                settingSMS = true;

                break;

            case 0x4141: //"AA"  "Add always allowed App "   AA 1 45 456

                ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);

                int size = settings.getInt("Ds.structACC.listGroupIds.size", 0);
                Ds.structACC.listGroupIds = new ArrayList<String>();//read From shearedpref
                for (int i = 0; i < size; i++) {
                    Ds.structACC.listGroupIds.add(settings.getString("Ds.structACC.listGroupIds" + i, ""));
                }


                int Groupid = -1;
                while (bodyLength > (workingIndex) && Character.isDigit(Body.charAt(workingIndex))) {

                    IndexInMsg = "";

                    while (bodyLength > (workingIndex) && Character.isDigit(Body.charAt(workingIndex))) {
                        cLocalMD = Body.charAt(workingIndex);

                        IndexInMsg = IndexInMsg + cLocalMD;
                        //System.out.println("\nIndex "+IndexInMsg);
                        workingIndex++;
                    }
                    int index = 0;
                    if (Character.isDigit(cLocalMD)) {
                        if (Groupid == -1) {
                            Groupid = Integer.parseInt(IndexInMsg);
                            if (!Ds.structACC.listGroupIds.contains("" + Groupid)) {
                                Ds.structACC.listGroupIds.add("" + Groupid);
                                editor.putString("Ds.structACC.listGroupIds" + (Ds.structACC.listGroupIds.size() - 1), "" + Groupid);
                                editor.putInt("Ds.structACC.listGroupIds.size", Ds.structACC.listGroupIds.size());
                                editor.commit();

                                Ds.inItApplicationStruct(context,
                                        Ds.structACC.listGroupIds.size(),
                                        Ds.structACC.listGroupIds);

                                for (int day = 0; day < 7; day++)
                                    for (int i = 0; i < Ds.structACC.bTotalFeatures; i++) {
                                        Ds.structACC.wUsedDuration[day][i] = (short) settings.getInt("structACC.wUsedDuration" + day + "" + i, Ds.structACC.wUsedDuration[day][i]);
                                        Ds.structACC.wTotalDuration[day][i] = (short) settings.getInt("structACC.wTotalDuration" + day + "" + i, Ds.structACC.wTotalDuration[day][i]);
                                        Ds.structACC.wEndTime[day][i] = (short) settings.getInt("structACC.wEndTime" + day + "" + i, Ds.structACC.wEndTime[day][i]);
                                        Ds.structACC.wStartTime[day][i] = (short) settings.getInt("structACC.wStartTime" + day + "" + i, Ds.structACC.wStartTime[day][i]);
                                        Ds.structACC.bPresentlyStopped[day][i] = settings.getBoolean("structACC.bPresentlyStopped" + day + "" + i, Ds.structACC.bPresentlyStopped[day][i]);
                                        Ds.structACC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structACC.bTimeDurationCtrl" + day + "" + i, Ds.structACC.bTimeDurationCtrl[day][i]);
                                        //	DeBug.ShowLog(TAG," r "+"structFCC.wStartTime"+day+""+i );
                                        for (int j = 0; j < 24; j++) {
                                            Ds.structACC.lAllowedTime[day][i][j] = settings.getLong("structACC.lAllowedTime" + day + "" + i + "" + j, 0L);
//										if(day==4 && i==0 && j==16)	;
                                            //		DeBug.ShowLog(TAG," r "+Long.toHexString( Ds.structFCC.lAllowedTime[day][i][j]));
                                        }


                                    }
                            }
                        } else {
                            index = Integer.parseInt(IndexInMsg);
                            DeBug.ShowLog("TESTMSG", "AA " + index);
                            //updateindatabase
                            //	if(mApplicationInfoDB.checkIsinGroup(Groupid, index))
                            mApplicationInfoDB.updateGroup(index, Groupid);
                        }
                    }

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                }
                Ds.structPC.bMode = ePhoneMode.PARENT_RESTRICTED;
                editor.putInt("structPC.bMode", Ds.structPC.bMode);
                editor.commit();

                ArrayList<ApplicationInfoStruct> list = mApplicationInfoDB.getData();

                DeBug.ShowLogD("ALLLIST", list.toString());
			/*for(int i=0;i<list.size();i++)
			{
				 Ds.structACC.packageList.addApplicationListToGroup(i, mApplicationInfoDB.getDataByGroup(i));
			}*/
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x4E46: //for "NF"  "Update father no."
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        //NZ 1234GP     workingIndex = 3+4 =7
                        //  012345
                        //012345678
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));
                    if (phoneNoIndex == 1)
                        Ds.structDNCC.stDefunctNumber[1] = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            Ds.structDNCC.stDefunctNumber[1] = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                            DeBug.ShowLog("TESTMSG", "NF " + Body.substring(workingIndex, workingIndex + phoneNoIndex - 1));
                        }
                        workingIndex = workingIndex + phoneNoIndex - 1;
                    }
                    try {

                        encryp = StringCryptor.encrypt(Ds.structDNCC.stDefunctNumber[1], StringCryptor.encryptionKey);
                        editor.putString("structDNCC.stDefunctNumber" + 1, encryp);
                        //DeBug.ShowLog("EN",""+encryp );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editor.commit();
                }

                //									Ds.fUPdateDefuncNo();
                //						Toast toasta = DeBug.ShowToast(context,"Father" +Ds.structDNCC.stDefunctNumber[1], Toast.LENGTH_LONG);toasta.show();
                settingSMS = true;

                break;
            case 0x4E4D: //for NM  "Update mother no."
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));
                    if (phoneNoIndex == 1)
                        Ds.structDNCC.stDefunctNumber[2] = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            Ds.structDNCC.stDefunctNumber[2] = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                            DeBug.ShowLog("TESTMSG", "NM " + Body.substring(workingIndex, workingIndex + phoneNoIndex - 1));
                        }
                        workingIndex = workingIndex + phoneNoIndex - 1;
                    }
                    try {
                        encryp = StringCryptor.encrypt(Ds.structDNCC.stDefunctNumber[2], StringCryptor.encryptionKey);
                        editor.putString("structDNCC.stDefunctNumber" + 2, encryp);
                        //DeBug.ShowLog("EN",""+encryp );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editor.commit();
                }
                //									Ds.fUPdateDefuncNo();
                //					Toast toast8 = DeBug.ShowToast(context,"Mother" +Ds.structDNCC.stDefunctNumber[2], Toast.LENGTH_LONG);toast8.show();
                settingSMS = true;

                break;
            case 0x4E5A: //for "NZ"  "Update factory no."
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (cLocalMD != ' ');
                    if (phoneNoIndex == 1)
                        Ds.structDNCC.stDefunctNumber[0] = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            Ds.structDNCC.stDefunctNumber[0] = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                            DeBug.ShowLog("TESTMSG", "NF " + Body.substring(workingIndex, workingIndex + phoneNoIndex - 1));

                        }
                        workingIndex = workingIndex + phoneNoIndex - 1;
                    }
                    try {
                        encryp = StringCryptor.encrypt(Ds.structDNCC.stDefunctNumber[0], StringCryptor.encryptionKey);
                        editor.putString("structDNCC.stDefunctNumber" + 0, encryp);
                        //DeBug.ShowLog("EN",""+encryp );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editor.commit();
                }
                settingSMS = true;

                break;

            case 0x5750: //for "WP"  " For Factory Reset"

                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    switch (cLocalMD) {
                        case '0':
                            ContentResolver cr = context.getContentResolver();
                            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
                            while (cur.moveToNext()) {
                                try {
                                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                                    System.out.println("The uri is " + uri.toString());
                                    cr.delete(uri, null, null);
                                } catch (Exception e) {
                                    System.out.println(e.getStackTrace());
                                }
                            }
                            break;
                        case '1':
                            if (workingIndex < bodyLength) {
                                cLocalMD = Body.charAt(workingIndex);
                                if (cLocalMD != ' ')
                                    workingIndex++;
                                switch (cLocalMD) {
                                    case ' ':
                                        // SMS wiping
                                        Uri inboxUri = Uri.parse("content://sms/inbox");
                                        int count = 0;
                                        Cursor c = context.getContentResolver().query(inboxUri, null, null, null, null);
                                        if (c != null) {
                                            while (c.moveToNext()) {
                                                try {
                                                    // Delete the SMS
                                                    String pid = c.getString(0); // Get id;
                                                    String uri = "content://sms/" + pid;
                                                    count = context.getContentResolver().delete(Uri.parse(uri),
                                                            null, null);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            c.close();
                                        }
                                        break;
                                    case '0':
                                        try {
                                            String[] code = Body.substring(workingIndex).trim().split(" ");
                                            if (code.length > 0) {
                                                workingIndex += code[0].length() + 1;
                                                String lockPin = StringCryptor.decrypt(code[0].trim(), StringCryptor.encryptionKey);
                                                editor.putString("LockPin", lockPin);
                                                editor.commit();
                                            }
                                        } catch (Exception e) {
                                            String lockPin = "8520";
                                            editor.putString("LockPin", lockPin);
                                            editor.commit();
                                        }
                                        break;
                                    case '1':
                                        editor.putBoolean("CallHelper.AppUpdate", true);
                                        editor.commit();
                                        break;
                                    case '3':
                                        if (CallDetectService.devicePolicyManager == null)
                                            CallDetectService.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                        if (CallDetectService.devicePolicyManager.getCameraDisabled(CallDetectService.demoDeviceAdmin))
                                            CallDetectService.devicePolicyManager.setCameraDisabled(CallDetectService.demoDeviceAdmin, false);
                                        CallDetectService.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                        CallDetectService.devicePolicyManager.removeActiveAdmin(CallDetectService.demoDeviceAdmin);
                                    case '2':
                                        CallHelper.Ds.structCCC.isProfileEnabled = 0;
                                        CallHelper.Ds.structCCC.wFeatureControlWord[0] = 0L;
                                        editor.putLong("structCCC.wFeatureControlWord" + 0, 0);
                                        editor.putInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
                                        editor.commit();
                                        break;
                                }
                            } else {
                                // SMS wiping
                                Uri inboxUri = Uri.parse("content://sms/inbox");
                                int count = 0;
                                Cursor c = context.getContentResolver().query(inboxUri, null, null, null, null);
                                if (c != null) {
                                    while (c.moveToNext()) {
                                        try {
                                            // Delete the SMS
                                            String pid = c.getString(0); // Get id;
                                            String uri = "content://sms/" + pid;
                                            count = context.getContentResolver().delete(Uri.parse(uri),
                                                    null, null);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    c.close();
                                }
                            }
                            break;
                        case '2':
                            editor.putBoolean("VpnIsEnabled", false);
                            editor.commit();
                            break;
                        case '3':
                            if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.FACT_RESET) == eFeatureControl.FACT_RESET)
                                if (CallDetectService.devicePolicyManager == null)
                                    CallDetectService.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                            CallDetectService.devicePolicyManager.wipeData(0);
                            break;
                        case '4':
                            if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.REMOTE_LOC) == eFeatureControl.REMOTE_LOC) {
                                if (CallDetectService.devicePolicyManager == null)
                                    CallDetectService.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                CallDetectService.devicePolicyManager.lockNow();
                                editor.putBoolean("unlocked", false);
                                editor.putBoolean("isLockEnabled", true);
                                editor.commit();
                            }
                            break;
                        case '5':
                            wipingSdcard();
                            break;
                        case '6':
                            if ((Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.REMOTE_TRIGGER) == eFeatureControl.REMOTE_TRIGGER) {
                                if (CallDetectService.devicePolicyManager == null)
                                    CallDetectService.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                                CallDetectService.devicePolicyManager.lockNow();
                                editor.putBoolean("unlockedSound", false);
                                editor.putBoolean("unlockedSoundOn", false);
                                editor.putBoolean("isLockEnabled", true);
                                editor.commit();
                            }
                            break;
                        case '7':
                            context.startService(new Intent(context, SyncIntentService.class));
                            break;
                        case '8':
                            context.startService(new Intent(context, SyncIntentService.class).putExtra("upload_data", true));
                            break;
                        case '9':
                            LocationDetails locationDetails = new LocationDetails(context);
                            locationDetails.timerLocation();
                            break;
                    }
                }
                settingSMS = true;

                break;

            case 0x4E59: //for "NY"  "Update stPullMsgNo"
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (cLocalMD != ' ');
                    if (phoneNoIndex == 1)
                        Ds.structDNCC.stPullMsgNo = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            Ds.structDNCC.stPullMsgNo = COUNTRY_CODE + Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                            DeBug.ShowLog("TESTMSG", "NY " + Body.substring(workingIndex, workingIndex + phoneNoIndex - 1));

                        }
                        workingIndex = workingIndex + phoneNoIndex - 1;
                    }
                    try {
                        encryp = StringCryptor.encrypt(Ds.structDNCC.stPullMsgNo, StringCryptor.encryptionKey);
                        editor.putString("structDNCC.stPullMsgNo", encryp);
                        //DeBug.ShowLog("EN",""+encryp );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    editor.commit();
                }
                settingSMS = true;

                break;

            case 0x414E: //for "AN" Add extra Allowed no.

                while (workingIndex < bodyLength && Character.isDigit(Body.charAt(workingIndex))) {
                    Number = null;
                    phoneNoIndex = 0;
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));

                    if (phoneNoIndex == 1)
                        Number = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            Number = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);
                            DeBug.ShowLog("TESTMSG", "AN " + Number);
                        }

                    }
                    int i = 0;
                    for (; i < Ds.structNCC.bTotalNumbers; i++) {
                        if (Ds.structNCC.stEXT_ALWD_Numbers[i] == null) {
                            Ds.structNCC.stEXT_ALWD_Numbers[i] = Number;
                            editor.putString("structNCC.stEXT_ALWD_Numbers" + i, Ds.structNCC.stEXT_ALWD_Numbers[i]);
                            break;
                        }
                    }
                    {

                        for (int Day = 0; Day < 7; Day++)
                            for (int callfeatere = 0; callfeatere < 2; callfeatere++) {

                                Ds.structNCC.bTimeDurationCtrl[Day][callfeatere] = (byte) (Ds.structNCC.bTimeDurationCtrl[Day][callfeatere] | eFeatureSetting.RESTRICTED_IN_NUMBERS);
                                editor.putInt("structNCC.bTimeDurationCtrl" + Day + "" + callfeatere, Ds.structNCC.bTimeDurationCtrl[Day][callfeatere]);
                                DeBug.ShowLog("TESTMSG", "AN bTimeDurationCtrl" + Day + " " + callfeatere + "  " + Ds.structNCC.bTimeDurationCtrl[Day][callfeatere]);
                            }
                    }
                    workingIndex = workingIndex + phoneNoIndex - 1;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                }
                editor.commit();
                settingSMS = true;

                break;
            case 0x524E: //for "RN"  Remove extra Allowed no.
                int i = 0;
                while (workingIndex < bodyLength && Character.isDigit(Body.charAt(workingIndex))) {
                    Number = null;
                    phoneNoIndex = 0;
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));

                    if (phoneNoIndex == 1)
                        Number = null;
                    else {
                        if (phoneNoIndex <= 12) {
                            Number = Body.substring(workingIndex, workingIndex + phoneNoIndex - 1);

                            DeBug.ShowLog("TESTMSG", "RN " + Number);
                        }
                        //workingIndex = workingIndex + phoneNoIndex-1;
                    }
                    i = 0;
                    for (; i < Ds.structNCC.bTotalNumbers; i++) {
                        if (Ds.structNCC.stEXT_ALWD_Numbers[i] != null && Ds.structNCC.stEXT_ALWD_Numbers[i].equalsIgnoreCase(Number)) {
                            Ds.structNCC.stEXT_ALWD_Numbers[i] = null;
                            editor.putString("structNCC.stEXT_ALWD_Numbers" + i, Ds.structNCC.stEXT_ALWD_Numbers[i]);
                            break;
                        }
                    }
                    workingIndex = workingIndex + phoneNoIndex - 1;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                }
                i = 0;
                for (; i < Ds.structNCC.bTotalNumbers; i++) {
                    if (Ds.structNCC.stEXT_ALWD_Numbers[i] != null) {

                        break;
                    }
                }
                DeBug.ShowLog("TESTMSG", "RN i= " + i);
                if (i >= Ds.structNCC.bTotalNumbers)
                    for (int Day = 0; Day < 7; Day++)
                        for (int callfeatere = 0; callfeatere < 2; callfeatere++) {
                            Ds.structNCC.bTimeDurationCtrl[Day][callfeatere] = (byte) (Ds.structNCC.bTimeDurationCtrl[Day][callfeatere] & ~(1 << 3));
                            editor.putInt("structNCC.bTimeDurationCtrl" + Day + "" + callfeatere, Ds.structNCC.bTimeDurationCtrl[Day][callfeatere]);
                            DeBug.ShowLog("TESTMSG", "RN bTimeDurationCtrl = " + Ds.structNCC.bTimeDurationCtrl[Day][callfeatere]);
                        }
                editor.commit();
                settingSMS = true;

                break;
            case 0x4D44: //for "MD"
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    int featureIndex = 0;
                    workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);

                    if (featureValue == 0) {
                        Ds.structPC.bMode = 0x00;
                        for (int day = 0; day < 7; day++) {
                            for (featureIndex = 0; featureIndex < 3; featureIndex++) {
                                Ds.structNCC.wTotalDuration[day][featureIndex] = (short) 1440;
                                Ds.structNCC.bTimeDurationCtrl[day][featureIndex] = (byte) (Ds.structNCC.bTimeDurationCtrl[day][featureIndex] & ~(3 << 1));
                                Ds.structNCC.wUsedDuration[day][featureIndex] = 0;
                                Ds.structNCC.bPresentlyStopped[day][featureIndex] = false;
                                for (int j = 0; j < 24; j++) {
                                    Ds.structNCC.lAllowedTime[day][featureIndex][j] = 0x0FFFFFFFFFFFFFFFL;
                                    editor.putLong("structNCC.lAllowedTime" + day + "" + featureIndex + "" + j, Ds.structNCC.lAllowedTime[day][featureIndex][j]);
                                }
                                editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + featureIndex, Ds.structNCC.bPresentlyStopped[day][featureIndex]);
                                editor.putInt("structNCC.wTotalDuration" + day + "" + featureIndex, Ds.structNCC.wTotalDuration[day][featureIndex]);
                                editor.putInt("structNCC.wUsedDuration" + day + "" + featureIndex, Ds.structNCC.wUsedDuration[day][featureIndex]);
                                editor.putInt("structNCC.bTimeDurationCtrl" + day + "" + featureIndex, Ds.structNCC.bTimeDurationCtrl[day][featureIndex]);
                            }
                            for (featureIndex = 0; featureIndex < Ds.structFCC.bTotalFeatures; featureIndex++) {
                                Ds.structFCC.wTotalDuration[day][featureIndex] = (short) 1440;
                                Ds.structFCC.bTimeDurationCtrl[day][featureIndex] = 0;
                                Ds.structFCC.wUsedDuration[day][featureIndex] = 0;
                                Ds.structFCC.bPresentlyStopped[day][featureIndex] = false;
                                for (int j = 0; j < 24; j++) {
                                    Ds.structFCC.lAllowedTime[day][featureIndex][j] = 0x0FFFFFFFFFFFFFFFL;
                                    editor.putLong("structFCC.lAllowedTime" + day + "" + featureIndex + "" + j, Ds.structFCC.lAllowedTime[day][featureIndex][j]);
                                }
                                editor.putBoolean("structFCC.bPresentlyStopped" + day + "" + featureIndex, Ds.structFCC.bPresentlyStopped[day][featureIndex]);
                                editor.putInt("structFCC.wTotalDuration" + day + "" + featureIndex, Ds.structFCC.wTotalDuration[day][featureIndex]);
                                editor.putInt("structFCC.wUsedDuration" + day + "" + featureIndex, Ds.structFCC.wUsedDuration[day][featureIndex]);
                                editor.putInt("structFCC.bTimeDurationCtrl" + day + "" + featureIndex, Ds.structFCC.bTimeDurationCtrl[day][featureIndex]);

                            }
                        }

                    }
                    editor.putInt("structPC.bMode", Ds.structPC.bMode);
                    editor.commit();
                }
                settingSMS = true;

                break;
			/*
		    CL0 number restricted without limits
		    CL2, 4, number  are not restricted but in limits
		    CL1,3,5 number restrcted but in limits
//		    		number  are not resrcted without limits
			 */
            case 0x4349: //CI clock time for the Incoming calls
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in CI SMS "+workingIndex);

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    //CL? TS1200 TE1210 TD30 TC?? (no of SMS)
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 1, (int) 0, context, featureValue, workingIndex);
                        //updateTimeDurationCtrl(String Body, int ifNotFeature, int featureIndex , Context context)
                    }

                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x434F: //CO clock time for the Outgoing calls
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in CO SMS "+workingIndex);

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    //CL? TS1200 TE1210 TD30 TC?? (no of SMS)
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 1, (int) 1, context, featureValue, workingIndex);
                        //updateTimeDurationCtrl(String Body, int ifNotFeature, int featureIndex , Context context)
                    }

                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x4353: //CS clock time for the SMS
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in CS SMS "+workingIndex);

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    //CL? TS1200 TE1210 TD30 TC?? (no of SMS)
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 1, (int) 2, context, featureValue, workingIndex);
                        //updateTimeDurationCtrl(String Body, int ifNotFeature, int featureIndex , Context context)
                    }

                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x474D: //for "GM" feature timings
                /**"GBox set as GM1 4 D3 TS0000 TE2345 TD60"
                 * settings for camera (4:feature ID)*/
                categories = CAT.GAME.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                    int nxtSpaceIndex = Body.indexOf(" ", workingIndex);

                    String fId = Body.substring(workingIndex, nxtSpaceIndex);

                    workingIndex = nxtSpaceIndex + 1;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);

                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D') {
                            int ifNotFeature = 0;
                            int featureIndex = Integer.parseInt(fId);
                            if (Integer.parseInt(fId) == eFeatureControl.iINCOMING_CALL) {
                                ifNotFeature = 1;
                                featureIndex = 0;
                            } else if (Integer.parseInt(fId) == eFeatureControl.iOUTGOING_CALL) {
                                ifNotFeature = 1;
                                featureIndex = 1;
                            } else if (Integer.parseInt(fId) == eFeatureControl.iINCOMING_SMS) {
                                ifNotFeature = 1;
                                featureIndex = 2;
                            }
                            workingIndex = updateTimeDurationCtrl(Body, (int) ifNotFeature, featureIndex, context, featureValue, workingIndex);
						/*						else
						workingIndex = updateTimeDurationCtrl( Body, (int)0, Integer.parseInt(fId),context,featureValue,workingIndex);
						 */
                        }

                    }
                }
                //									Toast toastd = DeBug.ShowToast(context,Temp+"GM" +Ds.structFCC.bTimeDurationCtrl1, Toast.LENGTH_LONG);toastd.show();
                settingSMS = true;

                break;

            case 0x4144: //for "AD" Music
                categories = CAT.MEDIA.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in AD "+workingIndex);

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    //AD? TS1200 TE1210 TD30GM
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 0, (int) categories, context, featureValue, workingIndex);
                    }
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;

            case 0x5244: //for "RD" Radio
                categories = CAT.RADIO.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;

                //DeBug.ShowToast(context, "Entered in RD "+workingIndex);

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;

                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    //AD? TS1200 TE1210 TD30GM
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 0, (int) categories, context, featureValue, workingIndex);
                    }
                }
                //										Toast toaste = DeBug.ShowToast(context,Temp+"RD" +Ds.structFCC.bTimeDurationCtrl3, Toast.LENGTH_LONG);toaste.show();
                settingSMS = true;

                break;
            case 0x434D: //for "CM" Camera
                categories = CAT.CAMERA.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 0, (int) categories, context, featureValue, workingIndex);
                    }
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x5746: //for "WF" WIFI
                categories = CAT.WIFI.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in WF SMS "+workingIndex);

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 0, (int) categories, context, featureValue, workingIndex);
                    }
                    //DeBug.ShowToast(context, " WF SMS Rcvd "+Ds.structFCC.bPresentlyStopped[1][categories]);
                }
                //Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x4D43: //for "MC" Mobile Connectivity
                categories = CAT.DATACONNECTIVITY.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 0, (int) categories, context, featureValue, workingIndex);
                    }
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x4254: //for "BT" Bluetooth
                categories = CAT.BLUETOOTH.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //	DeBug.ShowToast(context, "Entered in BT "+workingIndex);
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')
                            workingIndex = updateTimeDurationCtrl(Body, (int) 0, (int) categories, context, featureValue, workingIndex);
                    }
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);toastc.show();
                settingSMS = true;

                break;
            case 0x4341: //for "CA" Chat APP
                /**"GBox set as CA1 3 D3 TS0000 TE2345 CA1 2 D3 TS0000 TE2345"
                 * settings for apps with group id 2 and 3*/
                categories = CAT.CHAT.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in BT "+workingIndex);
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;

                    int nxtSpaceIndex = Body.indexOf(" ", workingIndex);

                    String fId = Body.substring(workingIndex, nxtSpaceIndex);

                    int size1 = settings.getInt("Ds.structACC.listGroupIds.size", 0);
                    Ds.structACC.listGroupIds = new ArrayList<String>();//read From shearedpref
                    for (int i1 = 0; i1 < size1; i1++) {
                        Ds.structACC.listGroupIds.add(settings.getString("Ds.structACC.listGroupIds" + i1, ""));
                    }

                    workingIndex = nxtSpaceIndex + 1;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        int GrpId = Ds.structACC.listGroupIds.indexOf(fId);
                        if (GrpId > -1) {
                            cLocalMD = Body.charAt(workingIndex);
                            if (cLocalMD == 'D')
                                if (featureValue > -1)
                                    workingIndex = updateTimeDurationCtrl(Body, (int) 2, GrpId, context, featureValue, workingIndex);
                        } else {
                            String temp1 = Body.substring(workingIndex, Body.length());
                            DeBug.ShowLogD("NarayananCH", temp1);
                            int temp = temp1.indexOf("CA1");
                            if (temp > 0) {
                                workingIndex += temp;
                            } else {
                                workingIndex = Body.length();
                            }
                        }
                    }
                }
                //								DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);
                settingSMS = true;

                break;

            case 0x4357: //for "CW" WebSite
                //categories=CAT.WEB.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                String[] modules = Body.split("CW1");
                List<String> tempList = new ArrayList<String>();
                if (modules.length > 0) {
                    int length = modules.length;
                    if (modules[0].trim().equalsIgnoreCase("GBox set as")) {
                        for (int ii = 1; ii < length; ii++) {
                            tempList.add(modules[ii].trim());
                        }
                    }
                    if (tempList.size() > 0) {
                        for (String s : tempList) {
                            decodeCw(s);
                        }
                    }
                }
                settingSMS = true;
                break;

            case 0x5753: //for "WS" WebSite
                //categories=CAT.WEB.intValue();
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                //DeBug.ShowToast(context, "Entered in WS "+workingIndex);
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D') ;
                        //		updateTimeDurationCtrl( Body, (int)0, (int)categories,context,featureValue);
                    }
                }
                //								DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2, Toast.LENGTH_LONG);
                settingSMS = true;

                break;


            case 0x4750: //for "GP"
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;

                if (Body.contains("GBox set as")) {
                    String removestr = Body.substring(12);
                    DeBug.ShowLog("removestr", removestr);
                    try {
                        editor.putString("CallHelper.runTime", removestr);
                        runTime = decodeGp8(removestr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (featureValue == 1) {
                        Ds.structLGC.wRequestGPSLocn = 1;
                        //if(Ds.structLGC.wRequestGPSLocn ==0)
                        //{
                        //Ds.structLGC.wRequestGPSLocn = 1;//Ds.structLGC.wRequestGPSLocn |(	1 << 0);
                        //	Ds.structLGC.bNoOfRequestGPSLocn++;
                        //}
                        Ds.structLGC.bNetwork = true;
                    }
                    if (featureValue == 8 || featureValue == 0) {
                        if (featureValue == 8) {
                            Ds.structPC.bFactoryControlByte = (byte) (Ds.structPC.bFactoryControlByte | featureValue);
                            for (int j = 0; j < Ds.structLGC.bTotalLocationSchedule; j++) {
                                Ds.structLGC.wEndTime[j] = 0;
                                Ds.structLGC.wStartTime[j] = 0;
                                Ds.structLGC.wLocFreq[j] = 0;
                            }
                            workingIndex = updateLocationTimeCtrl(Body, context, workingIndex);
                            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(System.currentTimeMillis());
                            String TimeStamp = formatter.format(calendar.getTime()).toString();
                            fre_Updated_on = TimeStamp;
                            editor.putString(" fre_Updated_on", fre_Updated_on);

                            for (int kk = 0; kk < Ds.structLGC.bTotalLocationSchedule; kk++) {
                                if (Ds.structLGC.wEndTime[kk] == 0)
                                    break;
                            }

                        } else
                            Ds.structPC.bFactoryControlByte = (byte) (Ds.structPC.bFactoryControlByte & ~(1 << 3));

                        editor.putInt("structPC.bFactoryControlByte", Ds.structPC.bFactoryControlByte);

                    }
                    if (featureValue == 3 || featureValue == 2) {
                        if (featureValue == 2)
                            Ds.structPC.bFactoryControlByte = (byte) (Ds.structPC.bFactoryControlByte | featureValue);
                        else
                            Ds.structPC.bFactoryControlByte = (byte) (Ds.structPC.bFactoryControlByte & ~(1 << 1));

                        editor.putInt("structPC.bFactoryControlByte", Ds.structPC.bFactoryControlByte);
                    }
                    editor.putInt("structLGC.bNoOfRequestGPSLocn", Ds.structLGC.bNoOfRequestGPSLocn);
                    editor.putInt("structLGC.wRequestGPSLocn", Ds.structLGC.wRequestGPSLocn);

                }
                editor.commit();
                //DeBug.ShowToast(context,"GP Ds.structLGC.bCallHelperGPSLocn= " +Ds.structLGC.wRequestGPSLocn);
                settingSMS = true;

                break;
            case 0x4746: //for "GF" GBox set as GF TS0500 TE1000 TG28.4316859 77.0578855 5 TS1000 TE2359 TG30.3781788 76.7766974 30
                //									 DeBug.ShowToast(context,"PW allowednoIndex " +allowednoIndex);
                if (allowednoIndex == 0) {
                    workingIndex = updateGeoFenceTimeCtrl(Body, context, workingIndex);
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x474B: //for "GK" Google Key GK asdow_1h
                //									 DeBug.ShowToast(context,"PW allowednoIndex " +allowednoIndex);
                if (allowednoIndex == 0) {
                    String Key = "";
                    while (workingIndex < bodyLength && Body.charAt(workingIndex) != ' ') {
                        Key = Key + Body.charAt(workingIndex++);
                    }
                    Ds.structCCC.stGoogleAPIKey[0] = Key;
                    Ds.structCCC.stGoogleAPIKey[1] = Key;

                    DeBug.ShowLog("TESTMSG", "GK :" + Ds.structCCC.stGoogleAPIKey + ":");

                    editor.putString("structCCC.stGoogleAPIKey" + "0", Ds.structCCC.stGoogleAPIKey[0]);
                    editor.putString("structCCC.stGoogleAPIKey" + "1", Ds.structCCC.stGoogleAPIKey[1]);
                    editor.commit();

                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;

            case 0x4C4B: //for "LK"				#GS

                DeBug.ShowLog("TESTMSG", "Inside LK");
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    String Key = "";

                    while (workingIndex < bodyLength && Body.charAt(workingIndex) != ' ') {
                        Key = Key + Body.charAt(workingIndex++);
                    }


                    Ds.structCCC.stGoogleAPIKey[featureValue] = Key;

                    DeBug.ShowLog("NOLOC", "LK :  " + Key);

                    editor.putString("structCCC.stGoogleAPIKey" + featureValue, Ds.structCCC.stGoogleAPIKey[featureValue]);

                }
                editor.commit();
                //DeBug.ShowToast(context,"GP Ds.structLGC.bCallHelperGPSLocn= " +Ds.structLGC.wRequestGPSLocn);
                settingSMS = true;

                break;

            case 0x5341: //for "SA" RunApp
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (allowednoIndex == 0) {

                    if (Character.isDigit(cLocalMD)) {
                        workingIndex++;
                        featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                        DeBug.ShowLog("TESTMSG", "SA " + featureValue);
                        if (featureValue == 1)
                            Ds.structPC.bRunApp = true;
                        else
                            Ds.structPC.bRunApp = false;

                        editor.putBoolean("structPC.bRunApp", Ds.structPC.bRunApp);
                        editor.commit();
                    }
                }
                //								 DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;

            case 0x4946: //for "IF" Get IF and send Info to the server    #GS
                DeBug.ShowLog("IPMSG", "IF");
                if (allowednoIndex == 0) {
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    Calendar calendar = Calendar.getInstance();
                    DeBug.ShowLog("IPMSG", "IF  " + allowednoIndex);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    //	String TimeStamp=formatter.format(calendar.getTime()).toString();

                    calendar.setTimeInMillis(Ds.lSchoolSchedule.CurrentDate.getTimeInMillis());
//				int date1 = calendar.get(Calendar.DATE);
//				int month = calendar.get(Calendar.MONTH)+1;
//				int year = calendar.get(Calendar.YEAR);
                    //calendar.set(Calendar.HOUR_OF_DAY, OneMinuteTimerReceiver.tempAppHrMin/100);
                    //calendar.set(Calendar.MINUTE, OneMinuteTimerReceiver.tempAppHrMin%100);
                    String TimeStamp = GetTimeWithDate();

                    String infoToBeSent = "" + TimeStamp + " :: " + Ds.structCCC.wFeatureControlWord[0] + " :: "
                            + Ds.structPC.bTimeExpired;

                    DeBug.ShowLog("IPMSG", "" + infoToBeSent);

				/*Struct_Send_SMSInfo.oListSMSInfo.add(new Struct_Send_SMSInfo(7 , TimeStamp, "",""+infoToBeSent, 0.0 ,0.0, 0,System.currentTimeMillis()));
					 if(!Struct_Send_SMSInfo.thread_SendSMS.isAlive())
					 {
						 DeBug.ShowLog("THREAD","a l i v e Work "+"Thread is starting ");
						 Thread_SendSMS.Init(context,(byte)0);
						 Struct_Send_SMSInfo.startSMSSendThread();
					 }*/
                    try {
                        DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                        String TimeStamp1 = GetTimeWithDate();
                        if (SMS_db.recordExist(TimeStamp1, 7) == 0) {
                            SMS_db.addSMS(new Struct_Send_SMSInfo(7, TimeStamp1, "", "" + infoToBeSent, 0.0, 0.0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                            Intent msgIntent = new Intent(context, UploadService.class);
                            Bundle b = new Bundle();
                            msgIntent.putExtra("UploadStatus", 0);
                            msgIntent.putExtras(b);
                            context.startService(msgIntent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //								 DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;


            case 0x564E: //for "VN" Get VN(Version Number) and send Info to the server    #GS               // VN0   -  Upload App Version
                // VN1   -  Upload remaining Log Files
                // VN2   -  Create and send file(Manual File upload)
                DeBug.ShowLog("IPMSG", "VN");
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (featureValue == 0) {
                        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                        Calendar calendar = Calendar.getInstance();
                        DeBug.ShowLog("IPMSG", "VN  " + allowednoIndex);
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        //	String TimeStamp=formatter.format(calendar.getTime()).toString();

                        calendar.setTimeInMillis(Ds.lSchoolSchedule.CurrentDate.getTimeInMillis());
                        int date1 = calendar.get(Calendar.DATE);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int year = calendar.get(Calendar.YEAR);
                        //calendar.set(Calendar.HOUR_OF_DAY, OneMinuteTimerReceiver.tempAppHrMin/100);
                        //calendar.set(Calendar.MINUTE, OneMinuteTimerReceiver.tempAppHrMin%100);
                        String TimeStamp = GetTimeWithDate();

                        String versionName = "No version found";
                        try {
                            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                        } catch (NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        String infoToBeSent = "" + TimeStamp + " :: " + versionName;

                        DeBug.ShowLog("IPMSG", "vn :   " + infoToBeSent);

					/*Struct_Send_SMSInfo.oListSMSInfo.add(new Struct_Send_SMSInfo(10 , TimeStamp, "",""+infoToBeSent, 0.0 ,0.0, 0,System.currentTimeMillis()));
						 if(!Struct_Send_SMSInfo.thread_SendSMS.isAlive())
						 {
							 DeBug.ShowLog("THREAD","a l i v e Work "+"Thread is starting ");
							 Thread_SendSMS.Init(context,(byte)0);
							 Struct_Send_SMSInfo.startSMSSendThread();
						 }*/
                        try {
                            DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                            String TimeStamp1 = GetTimeWithDate();
                            if (SMS_db.recordExist(TimeStamp1, 10) == 0) {
                                SMS_db.addSMS(new Struct_Send_SMSInfo(10, TimeStamp1, "", "" + infoToBeSent, 0.0, 0.0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                                Intent msgIntent = new Intent(context, UploadService.class);
                                Bundle b = new Bundle();
                                msgIntent.putExtra("UploadStatus", 0);
                                msgIntent.putExtras(b);
                                context.startService(msgIntent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (featureValue == 1) {
//					LKONagarNigam.uploadSubordinateLogFile();

                    } else if (featureValue == 2) {
//					LKONagarNigam.CheckLogFile();
                    }
                }

                //								 DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;


            case 0x4543: //for "EC" Realese date change EC 30 02 2013
                if (allowednoIndex == 0) {
                    if (bodyLength >= (workingIndex + 10)) {
                        Ds.structPC.iExpiryDate = Integer.parseInt(Body.substring(workingIndex, workingIndex + 2));
                        Ds.structPC.iExpiryMonth = Integer.parseInt(Body.substring(workingIndex + 3, workingIndex + 5)) - 1;
                        Ds.structPC.iExpiryYear = Integer.parseInt(Body.substring(workingIndex + 6, workingIndex + 10));
                        Ds.structPC.ExpiryDate.set(Ds.structPC.iExpiryYear, Ds.structPC.iExpiryMonth, Ds.structPC.iExpiryDate);

                        //DeBug.ShowToast(context,"Year " +Ds.structPC.ExpiryDate.MONTH);
                        DeBug.ShowLog("TESTMSG", "EC " + Ds.structPC.iExpiryMonth);


                        editor.putInt("structPC.iExpiryDate", Ds.structPC.iExpiryDate);
                        editor.putInt("structPC.iExpiryMonth", Ds.structPC.iExpiryMonth);
                        editor.putInt("structPC.iExpiryYear", Ds.structPC.iExpiryYear);

                        workingIndex = workingIndex + 10;
                        Ds.structPC.bExpiryDateUpdated = true;
                        editor.putBoolean("structPC.bExpiryDateUpdated", Ds.structPC.bExpiryDateUpdated);

                        editor.commit();
                    }
                }
                //DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x4546: //for "EF" Expirydate of Featurs EC<0---F> <f1f2f3f4> DDMMYYYY
                if (allowednoIndex == 0) {
                    char cFeatureRegisteIndex = Body.charAt(workingIndex++);
                    //workingIndex++;
                    DeBug.ShowLog("TESTMSG", "EC RIndex " + cFeatureRegisteIndex);
                    Calendar exp = Calendar.getInstance();
                    int end = Body.indexOf(" ", workingIndex + 1);
                    if (end == -1)
                        end = Body.length();

                    String data = Body.substring(workingIndex + 1, end);
                    DeBug.ShowLog("TESTMSG", "EC RIndex " + data);
                    String[] Enabled = data.split(",");

                    Ds.structCCC.wFeatureControlWord[0] = 0;
                    for (int k = 0; k < Enabled.length; k++) {
                        Ds.structCCC.wFeatureControlWord[0] = (int) (Ds.structCCC.wFeatureControlWord[0] | (1 << Integer.parseInt("" + Enabled[k]) - 1));//server starting with
                        DeBug.ShowLog("TESTMSG", "EC STATUS " + Long.toBinaryString(Ds.structCCC.wFeatureControlWord[0]));
                    }
                    editor.putLong("structCCC.wFeatureControlWord" + 0, Ds.structCCC.wFeatureControlWord[0]);
                    workingIndex = end;
				/* while((workingIndex+1)<bodyLength)
					 {

						 byte EXPDateStarting    = (byte) ( Body.indexOf(",", workingIndex)+1);
						 int iExpiryDate = Integer.parseInt(Body.substring(EXPDateStarting, EXPDateStarting + 2));
						 int iExpiryMonth= Integer.parseInt(Body.substring(EXPDateStarting+2, EXPDateStarting + 4))-1;
						 int iExpiryYear = Integer.parseInt(Body.substring(EXPDateStarting+4, EXPDateStarting + 8));
						 exp.set(iExpiryYear,iExpiryMonth,iExpiryDate);

						 char temp;
						 workingIndex++;
						 while(Body.charAt(workingIndex)!=',')
						 {
							 temp = Body.charAt(workingIndex++);
							 Ds.structCCC.lExpDatesOfFeatures[0][Byte.parseByte(""+temp)]=exp.getTimeInMillis();
							 Ds.structCCC.wFeatureControlWord[0]=(short) (Ds.structCCC.wFeatureControlWord[0] | (1<<Byte.parseByte(""+temp)));
							 DeBug.ShowLog("TESTMSG","EC STATUS "+Integer.toBinaryString(Ds.structCCC.wFeatureControlWord[0]));
							 editor.putLong("structCCC.wFeatureControlWord"+0, Ds.structCCC.wFeatureControlWord[0]);
							 editor.putLong("structCCC.lExpDatesOfFeatures"+0+""+Byte.parseByte(""+temp), Ds.structCCC.lExpDatesOfFeatures[0][Byte.parseByte(""+temp)]);
						 }
						// workingIndex = EXPDateStarting+8;
					 }*/

                }
                editor.commit();
                //								DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x4643: //for "FC" Expirydate of Featurs EC<0---F> 123456 30 02 2013
                if (allowednoIndex == 0) {
                    char cFeatureRegisteIndex = Body.charAt(workingIndex++);
                    //workingIndex++;
                    DeBug.ShowLog("TESTMSG", "EC RIndex " + cFeatureRegisteIndex);

                    while ((workingIndex + 1) < bodyLength && Body.charAt(++workingIndex) == ':') {
                        byte EXPDateStarting = (byte) (Body.indexOf(",", workingIndex) + 1);
                        int iExpiryDate = Integer.parseInt(Body.substring(EXPDateStarting, EXPDateStarting + 1));

                        char temp;
                        workingIndex++;
                        while (Body.charAt(workingIndex) != ',') {
                            temp = Body.charAt(workingIndex++);
                            if (iExpiryDate == 0) {
                                Ds.structCCC.wFeatureControlWord[0] = (short) (Ds.structCCC.wFeatureControlWord[0] & ~(1 << Byte.parseByte("" + temp)));
                                Ds.structCCC.lExpDatesOfFeatures[0][Byte.parseByte("" + temp)] = 0;
                                editor.putLong("structCCC.lExpDatesOfFeatures" + 0 + "" + Byte.parseByte("" + temp), Ds.structCCC.lExpDatesOfFeatures[0][Byte.parseByte("" + temp)]);
                            } else
                                Ds.structCCC.wFeatureControlWord[0] = (short) (Ds.structCCC.wFeatureControlWord[0] | (1 << Byte.parseByte("" + temp)));
                            DeBug.ShowLog("TESTMSG", "FC STATUS " + Long.toBinaryString(Ds.structCCC.wFeatureControlWord[0]));


                        }
                        workingIndex = EXPDateStarting + 1;
                    }
                    editor.putLong("structCCC.wFeatureControlWord" + 0, Ds.structCCC.wFeatureControlWord[0]);

                }
                editor.commit();
                //DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;

            case 0x5057: //for "PW" Password  //To cahnge "GBox set as"
                //DeBug.ShowToast(context,"PW allowednoIndex " +allowednoIndex);
                if (allowednoIndex == 0) {
                    int index = Body.indexOf(";");
                    if (index != -1) {
                        Ds.structPC.stPassword = Body.substring(workingIndex, index);
                        DeBug.ShowLog("TESTMSG", "PW :" + Ds.structPC.stPassword + ":");

                        workingIndex = index;

                        editor.putString("structPC.stPassword", Ds.structPC.stPassword);
                        editor.commit();
                    }
                }
                //								 DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x5050: //for "PP" Parent Password
                //									DeBug.ShowToast(context,"PW allowednoIndex " +allowednoIndex);

                allowednoIndex = 0;
                if (allowednoIndex == 0) {

                    phoneNoIndex = 0;
				/*do
				{
					if(bodyLength > (workingIndex + phoneNoIndex))
						cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
					else
					{
						phoneNoIndex++;
						break;
					}
					phoneNoIndex++;
				}while(cLocalMD!=' ');*/
                    DeBug.ShowLog("TESTMSG", "PP  " + workingIndex);
                    byte index = 0;
                    //workingIndex++;
                    while (workingIndex < bodyLength && Body.charAt(workingIndex) != ' ') {
                        DeBug.ShowLog("TESTMSG", "WI  " + workingIndex);

                        String stPass = "";
                        int endIndex = Body.indexOf(",", workingIndex);
                        if (endIndex > 1) {
                            stPass = Body.substring(workingIndex, endIndex);
                            workingIndex = endIndex + 1;
                            Ds.structPC.stParentPassword[index] = stPass;
                            editor.putString("structPC.stParentPassword" + index, Ds.structPC.stParentPassword[index]);
                            DeBug.ShowLog("TESTMSG", "PP  " + Ds.structPC.stParentPassword[index]);
                            index++;
                        } else {
                            endIndex = Body.indexOf(" ", workingIndex);
                            if (endIndex > 1) {
                                stPass = Body.substring(workingIndex, endIndex);
                                workingIndex = endIndex + 1;
                                Ds.structPC.stParentPassword[index] = stPass;
                                editor.putString("structPC.stParentPassword" + index, Ds.structPC.stParentPassword[index]);
                                DeBug.ShowLog("TESTMSG", "PP  " + Ds.structPC.stParentPassword[index]);
                                index++;
                                break;
                            } else {
                                stPass = Body.substring(workingIndex, bodyLength);
                                workingIndex = bodyLength;
                                Ds.structPC.stParentPassword[index] = stPass;
                                editor.putString("structPC.stParentPassword" + index, Ds.structPC.stParentPassword[index]);
                                DeBug.ShowLog("TESTMSG", "PP  " + Ds.structPC.stParentPassword[index]);
                                index++;
                                break;
                            }
                        }
                    }
                    editor.commit();

                }
                //
                //							DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x4544: //for "ED" Encryption Password change ED 1234567890123456
                //									 DeBug.ShowToast(context,"PW allowednoIndex " +allowednoIndex);

                if (allowednoIndex == 0) {

                    if (bodyLength >= (workingIndex + 16)) {

                        Ds.structPC.stEDPassword = Body.substring(workingIndex, workingIndex + 16);
                        //	DeBug.ShowToast(context,"PW " +Ds.structPC.stEDPassword);
                        DeBug.ShowLog("TESTMSG", "SA " + Ds.structPC.stEDPassword);
                        workingIndex = workingIndex + 16;

                        editor.putString("structPC.stEDPassword", Ds.structPC.stEDPassword);
                        editor.commit();
                    }
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x494C: //for "IL" to check app is running
                //									Toast toastc = DeBug.ShowToast(context,"PW allowednoIndex " +allowednoIndex);

                if (allowednoIndex == 0) {
                    for (int j = 1; j < Ds.structDNCC.bTotalNumbers; j++)
                        if (!Ds.structDNCC.stDefunctNumber[j].equals("0000000000")) {
                            Parent_no = Ds.structDNCC.stDefunctNumber[j];
                            break;
                        }

                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    Calendar calendar = Calendar.getInstance();

                    calendar.setTimeInMillis(System.currentTimeMillis());
                    //	String TimeStamp=formatter.format(calendar.getTime()).toString();

                    calendar.setTimeInMillis(Ds.lSchoolSchedule.CurrentDate.getTimeInMillis());
//				int date1 = calendar.get(Calendar.DATE);
//				int month = calendar.get(Calendar.MONTH)+1;
//				int year = calendar.get(Calendar.YEAR);
                    //calendar.set(Calendar.HOUR_OF_DAY, OneMinuteTimerReceiver.tempAppHrMin/100);
                    //calendar.set(Calendar.MINUTE, OneMinuteTimerReceiver.tempAppHrMin%100);
//				String TimeStamp=converttoDoubleDigit(date1)+"-"+converttoDoubleDigit(month)+"-"+year+" "
//						+converttoDoubleDigit(OneMinuteTimerReceiver.tempAppHrMin/100)+":"
//						+converttoDoubleDigit(OneMinuteTimerReceiver.tempAppHrMin%100);

				/*Struct_Send_SMSInfo.oListSMSInfo.add(new Struct_Send_SMSInfo(1 , TimeStamp, "","", 0.0 ,0.0, 0,System.currentTimeMillis()));
					 if(!Struct_Send_SMSInfo.thread_SendSMS.isAlive())
					 {
						 DeBug.ShowLog("THREAD","a l i v e Work "+"Thread is starting ");
						 Thread_SendSMS.Init(context,(byte)0);
						 Struct_Send_SMSInfo.startSMSSendThread();
					 }*/
                    try {
                        DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                        String TimeStamp1 = GetTimeWithDate();
                        if (SMS_db.recordExist(TimeStamp1, 1) == 0) {
                            SMS_db.addSMS(new Struct_Send_SMSInfo(1, TimeStamp1, "", "", 0.0, 0.0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                            Intent msgIntent = new Intent(context, UploadService.class);
                            Bundle b = new Bundle();
                            msgIntent.putExtra("UploadStatus", 0);
                            msgIntent.putExtras(b);
                            context.startService(msgIntent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                //							DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                settingSMS = true;

                break;
            case 0x4642: //for "FB" factory byte
                phoneNoIndex = 0;
                if (allowednoIndex == 0) {
                    do {
                        if (bodyLength > (workingIndex + phoneNoIndex))
                            cLocalMD = Body.charAt(workingIndex + phoneNoIndex);
                        else {
                            phoneNoIndex++;
                            break;
                        }
                        phoneNoIndex++;
                    } while (Character.isDigit(cLocalMD));
                    Ds.structPC.bFactoryControlByte = (byte) Integer.parseInt(Body.substring(workingIndex, workingIndex + phoneNoIndex - 1));
                    workingIndex = workingIndex + phoneNoIndex - 1;
                    DeBug.ShowLog("TESTMSG", "FB " + Ds.structPC.bFactoryControlByte);
                    editor.putInt("structPC.bFactoryControlByte", Ds.structPC.bFactoryControlByte);
                    editor.commit();
                }
                //								Toast toastc = DeBug.ShowToast(context,Temp+"AD" +Ds.structFCC.bTimeDurationCtrl2);
                break;
            case 0x4E41: //for "NA"
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    workingIndex++;
                    while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                        workingIndex++;
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    if (bodyLength > (workingIndex)) {
                        cLocalMD = Body.charAt(workingIndex);
                        if (cLocalMD == 'D')

                            workingIndex = updateTimeDurationCtrl(Body, (int) 1, (int) 4, context, featureValue, workingIndex);
                    }

                }
                settingSMS = true;

                break;
            case 0x5349: //for "SI"

                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                else
                    break;
                if (Character.isDigit(cLocalMD)) {
                    featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                    workingIndex++;

                    SI(featureValue);

                }
                settingSMS = true;

                break;

            case 0x534F: //for "SO"
                workingIndex++;
                featureValue = (byte) ((byte) cLocalMD - (byte) 48);
                //SO(featureValue);
                try {
                    new Thread(new Runnable() {
                        public void run() {
                            RestApiCall mRestApiCall = new RestApiCall();
                            JSONObject json = new JSONObject();
                            try {
                                json.put("AndroidAppId", CallHelper.Ds.structPC.iStudId);
                                json.put("isUninstalled", 1);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mRestApiCall.setAppInstallationStatus(json);
                        }
                    }).start();
                } catch (Exception ignore) {
                }
                DevicePolicyManager devicePolicyManager;
                devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName demoDeviceAdmin = new ComponentName(context, DemoDeviceAdminReceiver.class);
                if (devicePolicyManager.getCameraDisabled(demoDeviceAdmin))
                    devicePolicyManager.setCameraDisabled(demoDeviceAdmin, false);
                CallDetectService.devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                CallDetectService.devicePolicyManager.removeActiveAdmin(CallDetectService.demoDeviceAdmin);
                try {
                    try {
                        editor.clear();
                        editor.commit();
                        new RSharedData(context).clearData();
                        new SharedData(context).clearData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        File cacheDirectory = context.getCacheDir();
                        File applicationDirectory = new File(cacheDirectory.getParent());
                        if (applicationDirectory.exists()) {
                            String[] fileNames = applicationDirectory.list();
                            for (String fileName : fileNames) {
                                if (!fileName.equals("lib")) {
                                    deleteFile(new File(applicationDirectory, fileName));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        String packageName = context.getPackageName();
                        Runtime runtime = Runtime.getRuntime();
                        runtime.exec("pm clear " + packageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                settingSMS = true;

                break;

            default:
                workingIndex = bodyLength;
                break;

        } //for switch (signValue)
        return workingIndex;

    }

    public static boolean deleteFile(File file) {
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else {
                deletedAll = file.delete();
            }
        }
        return deletedAll;
    }

    public static String converttoDoubleDigit(int x) {
        String doubledigit = "";
        if (x < 10)
            doubledigit = "0" + x;
        else
            doubledigit = "" + x;

        return doubledigit;

    }

    public static boolean blockUrl(String URL, Context context) {
        DataStructure.runInTime = new HashMap<String, boolean[][]>();
        DataStructure.totalApplication = new ArrayList<String>();
        initSharedPreferences(context);
        CallHelper.decodeMessage(context, settings.getString("webCode", ""));
        boolean sf = true;
        int Hr = Ds.structPC.AppHrMin / 100;
        int Min = Ds.structPC.AppHrMin % 100;
        WebListTable websiteDB = new WebListTable(ctx);
        {
            String profileId = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
            int prof = Integer.parseInt(profileId);
            int totMin = (Hr * 60) + Min;
            DeBug.ShowLog("NarayananWB", "WEBBLOCK OPEN " + totMin);
            DeBug.ShowLog("NarayananWB", "WEBBLOCK OPEN " + CallHelper.Ds.structPC.bWeekDay);
            if (DataStructure.totalApplication.size() > 0) {
                for (String cat : DataStructure.totalApplication) {
                    boolean[][] blockTime = DataStructure.runInTime.get(cat);
                    if (blockTime != null) {
                        if (blockTime[CallHelper.Ds.structPC.bWeekDay][totMin]) {
                            DeBug.ShowLog("NarayananWB", "WEBBLOCK CHECK");
                            int categ = Integer.parseInt(cat);
                            if (sf)
                                sf = websiteDB.isUrlAllowed(URL, categ, prof);
                        } else {
                            DeBug.ShowLog("NarayananWB", "WEBBLOCK OPEN");
                        }
                    }
                }
            }
        }
        int isWebBlack = settings.getInt("webCode.IsWebBlack", CallHelper.Ds.structFCC.IsWebBlack);
        if (isWebBlack != 1) {
            sf = !sf;
        }
        return sf;
    }

    public static void decodeCw(String code) {
        String id = code.substring(0, code.indexOf(" D")).trim();
        boolean[][] tempRun = decodeTime(code.substring(code.indexOf(" D")).trim());
        DataStructure.runInTime.put(id, tempRun);
        DataStructure.totalApplication.add(id);
    }


    //Added To DECODE timeSettings
    public static boolean[] decodeGp8(String code) {
        boolean[] tempRun = new boolean[1440];
        boolean isStarted = false;
        boolean isEnded = false;
        int startTime = 0;
        int endTime = 0;
        int gapTime = 0;
        for (int j = 0; j < 1440; j++) {
            tempRun[j] = false;
        }
        for (int j = 0; j < 1440; j += 15) {
            tempRun[j] = true;
        }
        for (int workIndex = 0; workIndex < code.length(); workIndex++) {
            char CWcode = code.charAt(workIndex);
            switch (CWcode) {
                case 'T':
                    CWcode = code.charAt(++workIndex);
                    switch (CWcode) {
                        case 'S':
                            int s1 = Character.getNumericValue(code.charAt(++workIndex));
                            s1 = (s1 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            int s2 = Character.getNumericValue(code.charAt(++workIndex));
                            s2 = (s2 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            startTime = (s1 * 60) + s2;
                            isStarted = true;
                            break;
                        case 'E':
                            int e1 = Character.getNumericValue(code.charAt(++workIndex));
                            e1 = (e1 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            int e2 = Character.getNumericValue(code.charAt(++workIndex));
                            e2 = (e2 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            endTime = (e1 * 60) + e2;
                            isEnded = true;
                            break;
                        case 'F':
                            workIndex++;
                            while (workIndex < code.length() && code.charAt(workIndex) != ' ') {
                                gapTime = (gapTime * 10) + Character.getNumericValue(code.charAt(workIndex));
                                workIndex++;
                            }
                            if (isStarted && isEnded) {
                                for (int j = startTime; j < endTime; j++) {
                                    tempRun[j] = false;
                                }
                                int tempTime = (int) Math.ceil((endTime - startTime) / gapTime);
                                if (!(tempTime > 0)) {
                                    tempTime = 1;
                                }
                                for (int i = startTime; i < endTime; i = i + tempTime) {
                                    tempRun[i] = true;
                                    DeBug.ShowLog("NSTime", "" + startTime + " : " + endTime + " : " + tempTime + " : " + i);
                                }
                                isStarted = false;
                                isEnded = false;
                                gapTime = 0;
                            }
                            break;
                    }
                    break;
            }
        }
        return tempRun;
    }

    public static boolean[][] decodeTime(String code) {
        boolean[][] tempRun = new boolean[8][1441];
        boolean isStarted = false;
        int selDate = 10;
        int startTime = 0;
        int endTime = 0;
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 1440; j++) {
                tempRun[i][j] = false;
            }
        }
        for (int workIndex = 0; workIndex < code.length(); workIndex++) {
            char CWcode = code.charAt(workIndex);
            switch (CWcode) {
                case 'D':
                    int date = Character.getNumericValue(code.charAt(++workIndex));
                    if (date >= 0 && date <= 7)
                        selDate = date;
                    break;
                case 'T':
                    CWcode = code.charAt(++workIndex);
                    switch (CWcode) {
                        case 'S':
                            int s1 = Character.getNumericValue(code.charAt(++workIndex));
                            s1 = (s1 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            int s2 = Character.getNumericValue(code.charAt(++workIndex));
                            s2 = (s2 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            startTime = (s1 * 60) + s2;
                            isStarted = true;
                            break;
                        case 'E':
                            int e1 = Character.getNumericValue(code.charAt(++workIndex));
                            e1 = (e1 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            int e2 = Character.getNumericValue(code.charAt(++workIndex));
                            e2 = (e2 * 10) + Character.getNumericValue(code.charAt(++workIndex));
                            endTime = (e1 * 60) + e2;
                            if (isStarted) {
                                for (int i = startTime; i < endTime; i++) {
                                    tempRun[selDate][i] = true;
                                }
                                isStarted = false;
                            }
                            break;
                        case 'D':
                            workIndex++;
                            while (workIndex < code.length() && code.charAt(workIndex) != ' ') {
                                workIndex++;
                            }
                            break;
                    }
                    break;
            }
        }
        return tempRun;
    }

    /******************************************************
     * Function to Decode Timing setting in Incoming SMSs from Server
     * *****************************************************/
    protected static int updateTimeDurationCtrl(String Body, int ifNotFeature, int featureIndex, Context context, int featureValue, int workingIndex) {
        if (featureIndex > -1) {

            //String SMS=null;
            int bodyLength = Body.length();
            int indexIncrease;
            int wValue = 0;
            int wStartValue = 0;
            int weekSetFlag = 0;
            int weekSetValue[] = new int[7];
            boolean startTimeSet = false;
            char cLocalMD = ' ';

            int StartTime[] = new int[5];
            int EndTime[] = new int[5];
            int Duration = 0;
            int daycount = 0;
            byte control = 0;
            boolean bInClockTime = false;
            boolean bInTimeDuration = false;
            boolean bInTSClockTime = false;
            boolean bInSMSCount = false;
            long tempAllowedTime[][] = new long[7][24];
            byte changedDays[] = new byte[7];
            boolean from_allowed_time_setting = false;

            for (int day = 0; day < 7; day++)
                for (int hr = 0; hr < 24; hr++)
                    tempAllowedTime[day][hr] = 0L;
            //CL? W?????? TS1200 TE1210 TD30 TC?? (no of SMS)
            // if W is present then w? applies to days given by ???? and for th rest fo the days the setting is unchanged
            // if W not present then the settings applies to all 7 days.
            // W????, ???? means 1-Mon, 2-Tue, 7-Sun

            // if only GM0 is sent
            // if GM0 BT0 W1345 is sent
            // if GM0 D1347 BT0 is sent
            if (featureValue == 0)
                control = 0;

            while (bodyLength > (workingIndex)) {
                while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                    workingIndex++;
                if (bodyLength > (workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                if (cLocalMD == 'D') {

                    int maxSlot = 0;
                    Duration = 1440;
                    bInClockTime = false;
                    bInTimeDuration = false;
                    bInTSClockTime = false;
                    bInSMSCount = false;
                    control = 0;
                    // when enters here that time the weekSetFlag is reset
                    // if W is given then set the days and weekSetFlag
                    // weekSetFlag gets re-set if there is another W
                    weekSetFlag = 0;
                    do {
                        if (bodyLength > (++workingIndex))
                            cLocalMD = Body.charAt(workingIndex);
                        else
                            break;
                        wValue = (byte) ((byte) cLocalMD - (byte) 48);
                        if (wValue > 0 && wValue <= 7) {
                            weekSetValue[weekSetFlag] = wValue % 7; //SMS has 1-7 as Mon-Sun whereas the code has 0-6 as Sun-Sat
                            weekSetFlag++;
                        }
                    } while (Character.isDigit(cLocalMD));
                    while (bodyLength > (workingIndex)) {
                        while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                            workingIndex++;
                        if (bodyLength > (workingIndex))
                            cLocalMD = Body.charAt(workingIndex);

                        if (cLocalMD == 'T') {
                            //					DeBug.ShowToast(context, "Found T "+workingIndex, Toast.LENGTH_LONG);
                            DeBug.ShowLog(Outgoing, "Found T " + featureIndex);

                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            indexIncrease = 0;
                            switch (cLocalMD) {
                                case 'S':  //Start time
                                    //							DeBug.ShowToast(context, "Found S "+workingIndex, Toast.LENGTH_LONG);
                                    bInTSClockTime = true;
                                    do {
                                        indexIncrease++;
                                        if (bodyLength > (++workingIndex))
                                            cLocalMD = Body.charAt(workingIndex);
                                        else
                                            break;
                                    } while (Character.isDigit(cLocalMD));

                                    if (indexIncrease > 1) {
                                        wStartValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                                        if (wStartValue <= 2400) {
                                            startTimeSet = true;
                                        } else
                                            workingIndex = bodyLength;
                                    }
                                    DeBug.ShowLog(Outgoing, " TS " + wStartValue);
                                    //DeBug.ShowToast(context, "TS " +" wValue " +wStartValue);
                                    break;
                                case 'E':
                                    //							DeBug.ShowToast(context, "Found E "+workingIndex, Toast.LENGTH_LONG);
                                    bInTSClockTime = false;
                                    do {
                                        indexIncrease++;
                                        if (bodyLength > (++workingIndex))
                                            cLocalMD = Body.charAt(workingIndex);
                                        else
                                            break;
                                    } while (Character.isDigit(cLocalMD));
                                    if (indexIncrease > 1) {
                                        wValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                                        if (wValue <= 2400) {

                                            if (startTimeSet) {

                                                bInClockTime = true;
                                                StartTime[maxSlot] = wStartValue;
                                                EndTime[maxSlot] = wValue;
                                                maxSlot++;
                                                if (featureValue != 0)
                                                    control = (byte) (control | 1 | eFeatureSetting.RESTRICTED_IN_CLOCK_TIME);
                                                else
                                                    control = 0;
                                            }
                                        } else
                                            workingIndex = bodyLength;
                                    }
                                    DeBug.ShowLog(Outgoing, " TE " + wValue);
                                    //DeBug.ShowToast(context, "TE " +" wValue " +wValue);
                                    break;
                                case 'D':
                                    //							DeBug.ShowToast(context, "Found D "+workingIndex, Toast.LENGTH_LONG);

                                    do {
                                        DeBug.ShowLog("STATUS", "Found D " + workingIndex);
                                        indexIncrease++;
                                        if (bodyLength > (++workingIndex))
                                            cLocalMD = Body.charAt(workingIndex);
                                        else
                                            break;
                                    } while (Character.isDigit(cLocalMD));
                                    if (indexIncrease > 1) {
                                        wValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                                        if (wValue <= 1440) {
                                            bInTimeDuration = true;
                                            Duration = wValue;
                                            if (featureValue != 0)
                                                control = (byte) (control | 1 | eFeatureSetting.RESTRICTED_IN_DURATION);
                                            else
                                                control = 0;
                                        } else
                                            workingIndex = bodyLength;
                                    }
                                    DeBug.ShowLog(Outgoing, " TD " + wValue);
                                    //DeBug.ShowToast(context, "TD " +" wValue " +wValue);
                                    break;
                                case 'C':
                                    //							DeBug.ShowToast(context, "Found C "+workingIndex, Toast.LENGTH_LONG);

                                    do {
                                        DeBug.ShowLog("STATUS", "Found C " + workingIndex);
                                        indexIncrease++;
                                        if (bodyLength > (++workingIndex))
                                            cLocalMD = Body.charAt(workingIndex);
                                        else
                                            break;
                                    } while (Character.isDigit(cLocalMD));
                                    if (indexIncrease > 1) {
                                        wValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                                        if (wValue <= 1000) {
                                            Duration = wValue;
                                            bInSMSCount = true;

                                            if (featureValue != 0)
                                                control = (byte) (control | 1 | eFeatureSetting.RESTRICTED_IN_DURATION);
                                            else
                                                control = 0;

                                        } else
                                            workingIndex = bodyLength;
                                    }
                                    DeBug.ShowLog(Outgoing, " TC " + wValue);
                                    //DeBug.ShowToast(context, "TC " +" wValue " +wValue);
                                    break;
                                default:
                                    workingIndex = bodyLength;
                                    if (weekSetFlag > 0)
                                        for (int day = 0; day < weekSetFlag; day++) {
                                            if (ifNotFeature == 0)
                                                Ds.structFCC.bTimeDurationCtrl[weekSetValue[day]][featureIndex] = (byte) (0);
                                            else if (ifNotFeature == 1) {
                                                Ds.structNCC.bTimeDurationCtrl[weekSetValue[day]][featureIndex] = (byte) (0);
                                                //		Ds.structNCC.bTimeDurationCtrl[weekSetValue[day]][1] = (byte)(0);
                                            } else
                                                Ds.structACC.bTimeDurationCtrl[weekSetValue[day]][featureIndex] = (byte) (0);
                                        }
                                    else
                                        for (int day = 0; day < 7; day++) {
                                            if (ifNotFeature == 0)
                                                Ds.structFCC.bTimeDurationCtrl[day][featureIndex] = (byte) (0);
                                            else if (ifNotFeature == 1) {
                                                Ds.structNCC.bTimeDurationCtrl[day][featureIndex] = (byte) (0);
                                                //	Ds.structNCC.bTimeDurationCtrl[day][1] = (byte)(0);
                                            } else
                                                Ds.structACC.bTimeDurationCtrl[day][featureIndex] = (byte) (0);
                                        }
                                    break;
                            }//switch
                        }//if "T"
                        else
                            break;

                    }//while inside if('D')

                    //save the values here if -------
                    //NAx D1234       ->extra given no. applicable for calls/SMS or not x->0:not applicable
                    //                                                                  x->1:applicable
                    int day;
                    if (ifNotFeature == 1 && featureIndex == 4) {

                        for (int i = 0; i < weekSetFlag; i++) {
                            day = weekSetValue[i];
                            for (int j = 0; j < 3; j++) {
                                if (featureValue == 0)
                                    Ds.structNCC.bTimeDurationCtrl[day][j] = (byte) (Ds.structNCC.bTimeDurationCtrl[day][j] | eFeatureSetting.RESTRICTED_IN_NUMBERS);
                                if (featureValue == 1)
                                    Ds.structNCC.bTimeDurationCtrl[day][j] = (byte) ((Ds.structNCC.bTimeDurationCtrl[day][j]) & ~(1 << 3));
                                editor.putInt("structNCC.bTimeDurationCtrl" + day + "" + j, Ds.structNCC.bTimeDurationCtrl[day][j]);

                            }
                        }
                    }

                    if ((bInTimeDuration || bInClockTime || bInSMSCount || featureValue == 0)) {
                        int x = 0;
                        long lSetAllowedBytes = 0L;
                        if (!bInTimeDuration && bInClockTime) {
                            Duration = 1440;
                        } else if (!bInClockTime && bInTimeDuration) {
                            lSetAllowedBytes = 0x0FFFFFFFFFFFFFFFL;
                        }
                        if (featureValue == 0) {
                            lSetAllowedBytes = 0x0FFFFFFFFFFFFFFFL;
                            Duration = 1440;
                        }
                        bInTimeDuration = false;
                        bInClockTime = false;
                        bInSMSCount = false;
                        long temp = 0L;
                        if (ifNotFeature == 0) {
                            long time = System.currentTimeMillis();
                            //DeBug.ShowToast(context, "Out of while in clock " +" "+StartTime + " "+EndTime,Toast.LENGTH_LONG);
                            for (int i = 0; i < weekSetFlag; i++) {
                                from_allowed_time_setting = true;
                                day = weekSetValue[i];
                                for (x = 0; x < 24; x++) {
                                    Ds.structFCC.lAllowedTime[day][featureIndex][x] = lSetAllowedBytes;
                                }

                                if (featureValue != 0)
                                    for (int slot = 0; slot < maxSlot; slot++) {
                                        DeBug.ShowLogD("TESTMSG", "1 StartTime " + slot + " " + StartTime[slot]);
                                        DeBug.ShowLogD("TESTMSG", "2 EndTime   " + slot + " " + EndTime[slot]);
                                        int ST = StartTime[slot];
                                        int ET = EndTime[slot];
                                        changedDays[day]++;
									/*if((ET/100-ST/100)>1)
								{

									for(int l=ST%100;l<60;l++)
										Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[l];
									ST=ST+100;
									for(;(ST/100)<(ET/100);)
									{
									    Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=0x0FFFFFFFFFFFFFFFL;
									    ST=ST+100;
									}
									for(int l=0;l<ET%100;l++)
										Ds.structFCC.lAllowedTime[day][featureIndex][ET/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ET/100]|MIN_LUT[l];

								}
								else*/
                                        for (; ST < ET; ) {
                                            tempAllowedTime[day][ST / 100] = tempAllowedTime[day][ST / 100] | MIN_LUT[ST % 100];
                                            //	Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[ST%100];
                                            ST++;
                                            if (ST % 100 == 60) {
                                                ST = ST + 40;
                                                //int HrDiff = (EndTime[i1]/100)-(StartTime[i1]/100);
                                            }
                                        }

                                    }

                                Ds.structFCC.wTotalDuration[day][featureIndex] = (short) Duration;
                                Ds.structFCC.bTimeDurationCtrl[day][featureIndex] = control;
                                Ds.structFCC.wUsedDuration[day][featureIndex] = 0;
                                Ds.structFCC.bPresentlyStopped[day][featureIndex] = false;

                                DeBug.ShowLog("TESTMSG", "3 StartTime " + StartTime);
                                DeBug.ShowLog("TESTMSG", "3 EndTime   " + EndTime);
                                DeBug.ShowLog("TESTMSG", "3 IsBlackList  " + Duration);
                                DeBug.ShowLog("TESTMSG", "3 control   " + control);

                                for (int hr = 0; hr < 24; hr++) {
                                    editor.putLong("structFCC.lAllowedTime" + day + "" + featureIndex + "" + hr, Ds.structFCC.lAllowedTime[day][featureIndex][hr]);
                                    //	DeBug.ShowLogD("TESTMSG","structFCC.lAllowedTime"+day+""+featureIndex+""+hr+" "+Long.toHexString(tempAllowedTime[day][hr]));
                                }
                                editor.putBoolean("structFCC.bPresentlyStopped" + day + "" + featureIndex, Ds.structFCC.bPresentlyStopped[day][featureIndex]);
                                editor.putInt("structFCC.wTotalDuration" + day + "" + featureIndex, Ds.structFCC.wTotalDuration[day][featureIndex]);
                                editor.putInt("structFCC.wUsedDuration" + day + "" + featureIndex, Ds.structFCC.wUsedDuration[day][featureIndex]);
                                editor.putInt("structFCC.bTimeDurationCtrl" + day + "" + featureIndex, Ds.structFCC.bTimeDurationCtrl[day][featureIndex]);

                            }

                            DeBug.ShowLogD("TESTMSG", " time required " + (System.currentTimeMillis() - time));

                        } else if (ifNotFeature == 1) {
                            if ((Ds.structNCC.bTimeDurationCtrl[1][1] & eFeatureSetting.RESTRICTED_IN_NUMBERS) == eFeatureSetting.RESTRICTED_IN_NUMBERS)
                                control = (byte) (control | eFeatureSetting.RESTRICTED_IN_NUMBERS);

                            long time = System.currentTimeMillis();
                            //DeBug.ShowToast(context, "Out of while in clock " +" "+StartTime + " "+EndTime,Toast.LENGTH_LONG);
                            for (int i = 0; i < weekSetFlag; i++) {
                                from_allowed_time_setting = true;
                                day = weekSetValue[i];
                                for (x = 0; x < 24; x++) {
                                    Ds.structNCC.lAllowedTime[day][featureIndex][x] = lSetAllowedBytes;
                                }

                                if (featureValue != 0)
                                    for (int slot = 0; slot < maxSlot; slot++) {
                                        DeBug.ShowLogD("TESTMSG", "4 StartTime " + slot + " " + StartTime[slot]);
                                        DeBug.ShowLogD("TESTMSG", "4 EndTime   " + slot + " " + EndTime[slot]);
                                        int ST = StartTime[slot];
                                        int ET = EndTime[slot];
                                        changedDays[day]++;
									/*if((ET/100-ST/100)>1)
								{

									for(int l=ST%100;l<60;l++)
										Ds.structNCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structNCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[l];
									ST=ST+100;
									for(;(ST/100)<(ET/100);)
									{
									    Ds.structNCC.lAllowedTime[day][featureIndex][ST/100]=0x0FFFFFFFFFFFFFFFL;
									    ST=ST+100;
									}
									for(int l=0;l<ET%100;l++)
										Ds.structNCC.lAllowedTime[day][featureIndex][ET/100]=Ds.structNCC.lAllowedTime[day][featureIndex][ET/100]|MIN_LUT[l];

								}
								else*/
                                        for (; ST < ET; ) {
                                            tempAllowedTime[day][ST / 100] = tempAllowedTime[day][ST / 100] | MIN_LUT[ST % 100];
                                            //	Ds.structNCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structNCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[ST%100];
                                            ST++;
                                            if (ST % 100 == 60) {
                                                ST = ST + 40;
                                                //int HrDiff = (EndTime[i1]/100)-(StartTime[i1]/100);
                                            }
                                        }

                                    }

                                Ds.structNCC.wTotalDuration[day][featureIndex] = (short) Duration;
                                Ds.structNCC.bTimeDurationCtrl[day][featureIndex] = control;
                                Ds.structNCC.wUsedDuration[day][featureIndex] = 0;
                                Ds.structNCC.bPresentlyStopped[day][featureIndex] = false;

                                DeBug.ShowLog("TESTMSG", "5 StartTime " + StartTime);
                                DeBug.ShowLog("TESTMSG", "5 EndTime   " + EndTime);
                                DeBug.ShowLog("TESTMSG", "5 IsBlackList  " + Duration);
                                DeBug.ShowLog("TESTMSG", "5 control   " + control);
                                DeBug.ShowLog("TESTMSG", "5 Control Value   " + Ds.structCCC.wFeatureControlWord[0]);

                                for (int hr = 0; hr < 24; hr++) {
                                    editor.putLong("structNCC.lAllowedTime" + day + "" + featureIndex + "" + hr, Ds.structNCC.lAllowedTime[day][featureIndex][hr]);
                                    //	DeBug.ShowLogD("TESTMSG","structFCC.lAllowedTime"+day+""+featureIndex+""+hr+" "+Long.toHexString(tempAllowedTime[day][hr]));
                                }
                                editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + featureIndex, Ds.structNCC.bPresentlyStopped[day][featureIndex]);
                                editor.putInt("structNCC.wTotalDuration" + day + "" + featureIndex, Ds.structNCC.wTotalDuration[day][featureIndex]);
                                editor.putInt("structNCC.wUsedDuration" + day + "" + featureIndex, Ds.structNCC.wUsedDuration[day][featureIndex]);
                                editor.putInt("structNCC.bTimeDurationCtrl" + day + "" + featureIndex, Ds.structNCC.bTimeDurationCtrl[day][featureIndex]);

                            }

                            DeBug.ShowLogD("TESTMSG", " time required " + (System.currentTimeMillis() - time));


                        } else {

                            long time = System.currentTimeMillis();
                            //DeBug.ShowToast(context, "Out of while in clock " +" "+StartTime + " "+EndTime,Toast.LENGTH_LONG);
                            for (int i = 0; i < weekSetFlag; i++) {
                                from_allowed_time_setting = true;
                                day = weekSetValue[i];
                                for (x = 0; x < 24; x++) {
                                    if (day > -1 && featureIndex > -1 && x > -1)
                                        Ds.structACC.lAllowedTime[day][featureIndex][x] = lSetAllowedBytes;
                                }

                                if (featureValue != 0)
                                    for (int slot = 0; slot < maxSlot; slot++) {
                                        DeBug.ShowLogD("TESTMSG", "1 StartTime " + slot + " " + StartTime[slot]);
                                        DeBug.ShowLogD("TESTMSG", "2 EndTime   " + slot + " " + EndTime[slot]);
                                        int ST = StartTime[slot];
                                        int ET = EndTime[slot];
                                        changedDays[day]++;
									/*if((ET/100-ST/100)>1)
								{

									for(int l=ST%100;l<60;l++)
										Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[l];
									ST=ST+100;
									for(;(ST/100)<(ET/100);)
									{
									    Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=0x0FFFFFFFFFFFFFFFL;
									    ST=ST+100;
									}
									for(int l=0;l<ET%100;l++)
										Ds.structFCC.lAllowedTime[day][featureIndex][ET/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ET/100]|MIN_LUT[l];

								}
								else*/
                                        for (; ST < ET; ) {
                                            tempAllowedTime[day][ST / 100] = tempAllowedTime[day][ST / 100] | MIN_LUT[ST % 100];
                                            //	Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[ST%100];
                                            ST++;
                                            if (ST % 100 == 60) {
                                                ST = ST + 40;
                                                //int HrDiff = (EndTime[i1]/100)-(StartTime[i1]/100);
                                            }
                                        }

                                    }

                                try {

                                    Ds.structACC.wTotalDuration[day][featureIndex] = (short) Duration;
                                    Ds.structACC.bTimeDurationCtrl[day][featureIndex] = control;
                                    Ds.structACC.wUsedDuration[day][featureIndex] = 0;
                                    Ds.structACC.bPresentlyStopped[day][featureIndex] = false;
                                    DeBug.ShowLog("TESTMSG", "3 StartTime " + StartTime);
                                    DeBug.ShowLog("TESTMSG", "3 EndTime   " + EndTime);
                                    DeBug.ShowLog("TESTMSG", "3 IsBlackList  " + Duration);
                                    DeBug.ShowLog("TESTMSG", "3 control   " + control);

                                    for (int hr = 0; hr < 24; hr++) {
                                        editor.putLong("structACC.lAllowedTime" + day + "" + featureIndex + "" + hr, Ds.structACC.lAllowedTime[day][featureIndex][hr]);
                                        DeBug.ShowLogD("TESTMSG", "structACC.lAllowedTime" + day + "" + featureIndex + "" + hr + " " + Long.toHexString(tempAllowedTime[day][hr]));
                                    }
                                    editor.putBoolean("structACC.bPresentlyStopped" + day + "" + featureIndex, Ds.structACC.bPresentlyStopped[day][featureIndex]);
                                    editor.putInt("structACC.wTotalDuration" + day + "" + featureIndex, Ds.structACC.wTotalDuration[day][featureIndex]);
                                    editor.putInt("structACC.wUsedDuration" + day + "" + featureIndex, Ds.structACC.wUsedDuration[day][featureIndex]);
                                    editor.putInt("structACC.bTimeDurationCtrl" + day + "" + featureIndex, Ds.structACC.bTimeDurationCtrl[day][featureIndex]);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }

                            DeBug.ShowLogD("TESTMSG", " time required " + (System.currentTimeMillis() - time));


                        }

                        //DeBug.ShowToast(context, "before of While " +"  "+StartTime + "  "+EndTime + "\ncontrol ");
                        Ds.structPC.bMode = ePhoneMode.PARENT_RESTRICTED;

                    }
                    editor.putInt("structPC.bMode", Ds.structPC.bMode);
                    //call URL here if ------


                }//if "D"
                else
                    break;

            }//while

            if (from_allowed_time_setting) {
                if (ifNotFeature == 0) {
                    for (int day = 0; day < 7; day++) {
                        if (changedDays[day] != 0 && featureValue != 0) {
                            for (int i = 0; i < 24; i++) {
                                Ds.structFCC.lAllowedTime[day][featureIndex][i] = tempAllowedTime[day][i];
                                editor.putLong("structFCC.lAllowedTime" + day + "" + featureIndex + "" + i, Ds.structFCC.lAllowedTime[day][featureIndex][i]);
                                DeBug.ShowLogD("TESTMSG", "2 structFCC.lAllowedTime" + day + "" + featureIndex + "" + i + " " + Long.toHexString(Ds.structFCC.lAllowedTime[day][featureIndex][i]));
                            }
                            if (changedDays[day] > 1) {
                                Ds.structFCC.wTotalDuration[day][featureIndex] = (short) 1440;
                                Ds.structFCC.bTimeDurationCtrl[day][featureIndex] = (byte) (Ds.structFCC.bTimeDurationCtrl[day][featureIndex] & ~(1 << 1));
                            }
                        }
                        DeBug.ShowLog("TESTMSG", "6 control   " + Ds.structFCC.bTimeDurationCtrl[day][featureIndex]);
                    }
                } else if (ifNotFeature == 1) {

                    for (int day = 0; day < 7; day++) {
                        if (changedDays[day] != 0 && featureValue != 0) {
                            for (int i = 0; i < 24; i++) {
                                Ds.structNCC.lAllowedTime[day][featureIndex][i] = tempAllowedTime[day][i];
                                editor.putLong("structNCC.lAllowedTime" + day + "" + featureIndex + "" + i, Ds.structNCC.lAllowedTime[day][featureIndex][i]);
                                DeBug.ShowLogD("TESTMSG", "2 structNCC.lAllowedTime" + day + "" + featureIndex + "" + i + " " + Long.toHexString(Ds.structNCC.lAllowedTime[day][featureIndex][i]));
                            }
                            if (changedDays[day] > 1) {
                                Ds.structNCC.wTotalDuration[day][featureIndex] = (short) 1440;
                                Ds.structNCC.bTimeDurationCtrl[day][featureIndex] = (byte) (Ds.structNCC.bTimeDurationCtrl[day][featureIndex] & ~(1 << 1));
                            }
                        }
                        DeBug.ShowLog("TESTMSG", "6 control   " + Ds.structNCC.bTimeDurationCtrl[day][featureIndex]);
                    }

                } else {

                    for (int day = 0; day < 7; day++) {
                        if (changedDays[day] != 0 && featureValue != 0) {
                            for (int i = 0; i < 24; i++) {
                                Ds.structACC.lAllowedTime[day][featureIndex][i] = tempAllowedTime[day][i];
                                editor.putLong("structACC.lAllowedTime" + day + "" + featureIndex + "" + i, Ds.structACC.lAllowedTime[day][featureIndex][i]);
                                DeBug.ShowLogD("TESTMSG", "2 structACC.lAllowedTime" + day + "" + featureIndex + "" + i + " " + Long.toHexString(Ds.structACC.lAllowedTime[day][featureIndex][i]));
                            }
                            if (changedDays[day] > 1) {
                                Ds.structACC.wTotalDuration[day][featureIndex] = (short) 1440;
                                Ds.structACC.bTimeDurationCtrl[day][featureIndex] = (byte) (Ds.structACC.bTimeDurationCtrl[day][featureIndex] & ~(1 << 1));
                            }
                        }
                        DeBug.ShowLog("TESTMSG", "6 control   " + Ds.structACC.bTimeDurationCtrl[day][featureIndex]);
                    }

                }

            }

            editor.commit();
            return workingIndex;
        }
        return workingIndex;
    }

    /**********************************************************************
     * Function to Decode School Timing setting in Incoming SMSs from Server
     * *********************************************************************/
    protected static int updateSchoolTimeDurationCtrl(String Body, Context context, int workingIndex) {

        //String SMS=null;
        int bodyLength = Body.length();
        int indexIncrease;
        int wValue = 0;
        int wStartValue = 0;
        int weekSetFlag = 0;
        int weekSetValue[] = new int[7];
        boolean startTimeSet = false;
        char cLocalMD = ' ';

        int StartTime[] = new int[5];
        int EndTime[] = new int[5];
        long tempAllowedTime[] = new long[24];
        byte changedDays[] = new byte[7];
        boolean from_allowed_time_setting = false;


        for (int hr = 0; hr < 24; hr++)
            tempAllowedTime[hr] = 0L;
        int maxSlot = 0;
        while (bodyLength > (workingIndex)) {
            while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                workingIndex++;
            if (bodyLength > (workingIndex))
                cLocalMD = Body.charAt(workingIndex);

            if (cLocalMD == 'T') {

                if (bodyLength > (++workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                indexIncrease = 0;
                switch (cLocalMD) {
                    case 'S':  //Start time
                        //							DeBug.ShowToast(context, "Found S "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));

                        if (indexIncrease > 1) {
                            wStartValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wStartValue <= 2400) {
                                startTimeSet = true;
                            } else
                                workingIndex = bodyLength;
                        }
                        DeBug.ShowLog("TESTMSG", " TS " + wStartValue);
                        //DeBug.ShowToast(context, "TS " +" wValue " +wStartValue);
                        break;
                    case 'E':
                        //							DeBug.ShowToast(context, "Found E "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));
                        if (indexIncrease > 1) {
                            wValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wValue <= 2400) {

                                if (startTimeSet) {

                                    StartTime[maxSlot] = wStartValue;
                                    EndTime[maxSlot] = wValue;
                                    maxSlot++;
                                }
                            } else
                                workingIndex = bodyLength;
                        }
                        DeBug.ShowLog("TESTMSG", " TE " + wValue);
                        //DeBug.ShowToast(context, "TE " +" wValue " +wValue);
                        break;
                    default:
                        workingIndex = bodyLength;
                        break;
                }//switch
            }//if "T"
            else
                break;

        }//while inside if('D')

        int day = 0;
        int x = 0;
        long temp = 0L;
        long time = System.currentTimeMillis();
        //DeBug.ShowToast(context, "Out of while in clock " +" "+StartTime + " "+EndTime,Toast.LENGTH_LONG);

        for (int slot = 0; slot < maxSlot; slot++) {
            DeBug.ShowLog("TESTMSG", "1 StartTime " + slot + " " + StartTime[slot]);
            DeBug.ShowLog("TESTMSG", "1 EndTime   " + slot + " " + EndTime[slot]);
            int ST = StartTime[slot];
            int ET = EndTime[slot];
            for (; ST < ET; ) {
                tempAllowedTime[ST / 100] = tempAllowedTime[ST / 100] | MIN_LUT[ST % 100];
                //	Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]=Ds.structFCC.lAllowedTime[day][featureIndex][ST/100]|MIN_LUT[ST%100];
                ST++;
                if (ST % 100 == 60) {
                    ST = ST + 40;
                    //int HrDiff = (EndTime[i1]/100)-(StartTime[i1]/100);
                }
            }
            DeBug.ShowLog("TESTMSG", "2 StartTime " + StartTime[slot]);
            DeBug.ShowLog("TESTMSG", "2 EndTime   " + EndTime[slot]);
        }

        Ds.lSchoolSchedule.wStartTime[iselectedscholslot] = (short) StartTime[0];
        Ds.lSchoolSchedule.wEndTime[iselectedscholslot] = (short) EndTime[maxSlot - 1];

        editor.putInt("lSchoolSchedule.wStartTime" + iselectedscholslot, Ds.lSchoolSchedule.wStartTime[iselectedscholslot]);
        editor.putInt("lSchoolSchedule.wEndTime" + iselectedscholslot, Ds.lSchoolSchedule.wEndTime[iselectedscholslot]);

        DeBug.ShowLog("TESTMSG", " time required " + (System.currentTimeMillis() - time));

        for (int i = 0; i < 24; i++) {
            Ds.lSchoolSchedule.lAllowedTime[iselectedscholslot][i] = tempAllowedTime[i];
            editor.putLong("lSchoolSchedule.lAllowedTime" + iselectedscholslot + "" + i, Ds.lSchoolSchedule.lAllowedTime[iselectedscholslot][i]);
            DeBug.ShowLog("TESTMSG", "2 Ds.lSchoolSchedule.lAllowedTime " + iselectedscholslot + " " + i + " " + Long.toHexString(Ds.lSchoolSchedule.lAllowedTime[iselectedscholslot][i]));
        }
        editor.commit();
        return workingIndex;
    }


    /*************************************************************************
     * Function to Decode  Location Timing setting in Incoming SMSs from Server
     * *********************************************************************/
    protected static int updateLocationTimeCtrl(String Body, Context context, int workingIndex) {

        //String SMS=null;
        int bodyLength = Body.length();
        int indexIncrease;
        int wValue = 0;
        int wStartValue = 0;
        int wFreqValue = 0;
        boolean startTimeSet = false;
        boolean EndTimeSet = false;
        char cLocalMD = ' ';
        int StartTime[] = new int[15];
        int EndTime[] = new int[15];
        int FreqTime[] = new int[15];

        int maxSlot = 0;

        while (bodyLength > (workingIndex)) {
            while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                workingIndex++;
            if (bodyLength > (workingIndex))
                cLocalMD = Body.charAt(workingIndex);

            if (cLocalMD == 'T') {

                if (bodyLength > (++workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                indexIncrease = 0;

                switch (cLocalMD) {
                    case 'S':  //Start time
                        //							DeBug.ShowToast(context, "Found S "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));

                        if (indexIncrease > 1) {
                            wStartValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wStartValue <= 2400) {
                                startTimeSet = true;
                            } else
                                workingIndex = bodyLength;
                        }
                        DeBug.ShowLog("TESTMSG", " TS " + wStartValue);
                        //DeBug.ShowToast(context, "TS " +" wValue " +wStartValue);
                        break;
                    case 'E':
                        //							DeBug.ShowToast(context, "Found E "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));
                        if (indexIncrease > 1) {
                            wValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wValue <= 2400) {

                                if (startTimeSet) {

                                    StartTime[maxSlot] = wStartValue;
                                    EndTime[maxSlot] = wValue;
                                    EndTimeSet = true;
                                }
                            } else
                                workingIndex = bodyLength;
                        }
                        DeBug.ShowLog("TESTMSG", " TE " + wValue);
                        //DeBug.ShowToast(context, "TE " +" wValue " +wValue);
                        break;
                    case 'F':
                        //							DeBug.ShowToast(context, "Found E "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));

                        if (indexIncrease > 1) {
                            wFreqValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wFreqValue > 0) {

                                if (EndTimeSet) {

                                    StartTime[maxSlot] = wStartValue;
                                    EndTime[maxSlot] = wValue;
                                    FreqTime[maxSlot] = wFreqValue;
                                    DeBug.ShowLog("TESTMSG", maxSlot + " TF " + FreqTime[maxSlot]);
                                    maxSlot++;
                                }
                            } else
                                workingIndex = bodyLength;
                        }

                        //DeBug.ShowToast(context, "TE " +" wValue " +wValue);
                        break;

                    default:
                        workingIndex = bodyLength;
                        break;
                }//switch
            }//if "T"
            else
                break;

        }//while(bodyLength > (workingIndex))

        long time = System.currentTimeMillis();
        //DeBug.ShowToast(context, "Out of while in clock " +" "+StartTime + " "+EndTime,Toast.LENGTH_LONG);

        for (int slot = 0; slot < maxSlot; slot++) {

            int ST = StartTime[slot];
            int ET = EndTime[slot];
            int FT = FreqTime[slot];
            Ds.structLGC.wStartTime[slot] = (short) ST;
            Ds.structLGC.wEndTime[slot] = (short) ET;
            int freq = TimeDiff(ET, ST);
            FT = freq / FT;
            Ds.structLGC.wLocFreq[slot] = (short) FT;

            DeBug.ShowLog("TESTMSG", "2 StartTime " + ST);
            DeBug.ShowLog("TESTMSG", "2 EndTime   " + ET);
            DeBug.ShowLog("TESTMSG", "2 EndTime   " + FT);
            editor.putInt("structLGC.wStartTime" + slot, Ds.structLGC.wStartTime[slot]);
            editor.putInt("structLGC.wEndTime" + slot, Ds.structLGC.wEndTime[slot]);
            editor.putInt("structLGC.wLocFreq" + slot, Ds.structLGC.wLocFreq[slot]);

        }
        for (int slot = maxSlot; slot < Ds.structLGC.bTotalLocationSchedule; slot++) {
            editor.putInt("structLGC.wStartTime" + slot, 0);
            editor.putInt("structLGC.wEndTime" + slot, 0);
            editor.putInt("structLGC.wLocFreq" + slot, 0);
        }
        DeBug.ShowLog("TESTMSG", " time required " + (System.currentTimeMillis() - time));

        editor.commit();

        for (int kk = 0; kk < Ds.structLGC.bTotalLocationSchedule; kk++) {
            short et = (short) settings.getInt("structLGC.wEndTime" + kk, Ds.structLGC.wEndTime[kk]);
            short st = (short) settings.getInt("structLGC.wStartTime" + kk, Ds.structLGC.wStartTime[kk]);
            short ft = (short) settings.getInt("structLGC.wLocFreq" + kk, Ds.structLGC.wLocFreq[kk]);
            DeBug.ShowLogD("LOC", "after update   Start " + st + " End " + et + " ft " + ft);

        }
        return workingIndex;
    }


    /*************************************************************************
     * Function to Decode  GeoFence Timing setting in Incoming SMSs from Server
     * *********************************************************************/
    protected static int updateGeoFenceTimeCtrl(String Body, Context context, int workingIndex) {

        //String SMS=null;
        int bodyLength = Body.length();
        int indexIncrease;
        int wValue = 0;
        int wStartValue = 0;
        int wFreqValue = 0;
        boolean startTimeSet = false;
        boolean EndTimeSet = false;
        char cLocalMD = ' ';
        int StartTime[] = new int[Ds.structLGC.bTotalLocationSchedule];
        int EndTime[] = new int[Ds.structLGC.bTotalLocationSchedule];
        int FreqTime[] = new int[Ds.structLGC.bTotalLocationSchedule];

        double dLatitude[] = new double[Ds.structLGC.bTotalLocationSchedule];
        double dLongitude[] = new double[Ds.structLGC.bTotalLocationSchedule];
        double dRadius[] = new double[Ds.structLGC.bTotalLocationSchedule];

        int maxSlot = 0;
        boolean bfromG = false;
        while (bodyLength > (workingIndex)) {
            while ((bodyLength > (workingIndex)) && Body.charAt(workingIndex) == ' ')
                workingIndex++;
            if (bodyLength > (workingIndex))
                cLocalMD = Body.charAt(workingIndex);

            if (cLocalMD == 'T') {

                if (bodyLength > (++workingIndex))
                    cLocalMD = Body.charAt(workingIndex);
                indexIncrease = 0;

                switch (cLocalMD) {
                    case 'S':  //Start time
                        //							DeBug.ShowToast(context, "Found S "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));

                        if (indexIncrease > 1) {
                            wStartValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wStartValue <= 2400) {
                                startTimeSet = true;
                            } else
                                workingIndex = bodyLength;
                        }
                        bfromG = false;
                        DeBug.ShowLog("TESTMSG", " TS " + wStartValue);
                        //DeBug.ShowToast(context, "TS " +" wValue " +wStartValue);
                        break;
                    case 'E':
                        //							DeBug.ShowToast(context, "Found E "+workingIndex, Toast.LENGTH_LONG);
                        do {
                            indexIncrease++;
                            if (bodyLength > (++workingIndex))
                                cLocalMD = Body.charAt(workingIndex);
                            else
                                break;
                        } while (Character.isDigit(cLocalMD));

                        if (indexIncrease > 1) {
                            wValue = Integer.parseInt(Body.substring(workingIndex - indexIncrease + 1, workingIndex));
                            if (wValue <= 2400) {

                                if (startTimeSet) {

                                    StartTime[maxSlot] = wStartValue;
                                    EndTime[maxSlot] = wValue;
                                    EndTimeSet = true;
                                    maxSlot++;
                                }
                            } else
                                workingIndex = bodyLength;
                        }
                        bfromG = false;
                        DeBug.ShowLog("TESTMSG", " TE " + wValue);
                        //DeBug.ShowToast(context, "TE " +" wValue " +wValue);
                        break;
                    case 'G':
                        //DeBug.ShowToast(context, "Found E "+workingIndex, Toast.LENGTH_LONG);
                        workingIndex++;
                        int SpaceIndex = Body.indexOf(" ", workingIndex);
                        dLatitude[maxSlot - 1] = Double.parseDouble(Body.substring(workingIndex, SpaceIndex));
                        workingIndex = SpaceIndex + 1;
                        DeBug.ShowLog("TESTMSG", " TG " + dLatitude[maxSlot - 1]);

                        SpaceIndex = Body.indexOf(" ", workingIndex);
                        dLongitude[maxSlot - 1] = Double.parseDouble(Body.substring(workingIndex, SpaceIndex));
                        workingIndex = SpaceIndex + 1;
                        DeBug.ShowLog("TESTMSG", " TG " + dLongitude[maxSlot - 1]);


                        SpaceIndex = Body.indexOf(" ", workingIndex);
                        if (SpaceIndex == -1)
                            SpaceIndex = bodyLength;

                        dRadius[maxSlot - 1] = Double.parseDouble(Body.substring(workingIndex, SpaceIndex));
                        workingIndex = SpaceIndex + 1;
                        DeBug.ShowLog("TESTMSG", " TG " + dRadius[maxSlot - 1]);
                        bfromG = true;
                        break;

                    default:
                        workingIndex = bodyLength;
                        break;
                }//switch
            }//if "T"
            else
                break;
            if (maxSlot >= Ds.structLGC.bTotalLocationSchedule && bfromG) {
                workingIndex = bodyLength;
                break;
            }
        }//while(bodyLength > (workingIndex))


        //DeBug.ShowToast(context, "Out of while in clock " +" "+StartTime + " "+EndTime,Toast.LENGTH_LONG);

        for (int slot = 0; slot < maxSlot; slot++) {

            int ST = StartTime[slot];
            int ET = EndTime[slot];

            Ds.structLGC.wStartGeoFenceTime[slot] = (short) ST;
            Ds.structLGC.wEndGeoFenceTime[slot] = (short) ET;
            Ds.structLGC.dGeoFenceLatitude[slot] = dLatitude[slot];
            Ds.structLGC.dGeoFenceLongitude[slot] = dLongitude[slot];
            Ds.structLGC.dGeoFenceRadius[slot] = dRadius[slot] * 1000;

            DeBug.ShowLog("TESTMSG", "2 StartTime " + ST);
            DeBug.ShowLog("TESTMSG", "2 EndTime   " + ET);
            DeBug.ShowLog("TESTMSG", "2 radius    " + Ds.structLGC.dGeoFenceRadius[slot]);
            editor.putInt("structLGC.wStartGeoFenceTime" + slot, Ds.structLGC.wStartGeoFenceTime[slot]);
            editor.putInt("structLGC.wEndGeoFenceTime" + slot, Ds.structLGC.wEndGeoFenceTime[slot]);
            editor.putString("structLGC.dGeoFenceLatitude" + slot, "" + Ds.structLGC.dGeoFenceLatitude[slot]);
            editor.putString("structLGC.dGeoFenceLongitude" + slot, "" + Ds.structLGC.dGeoFenceLongitude[slot]);
            editor.putString("structLGC.dGeoFenceRadius" + slot, "" + Ds.structLGC.dGeoFenceRadius[slot]);
        }
        editor.commit();

        long time = System.currentTimeMillis();
        DeBug.ShowLog("TESTMSG", " time required " + (System.currentTimeMillis() - time));


        return workingIndex;
    }

    /*************************************************************************
     * Function to Send info to Server:
     * try through Internet
     * if failed try through SMS
     * if faild save info to try after some time
     * *********************************************************************/
    public static int sendInfoToServer(final Context ctx, final Struct_Send_SMSInfo smsinfo, final int SMSsend) {
        int InfoType = smsinfo.getInfoType();
        String Time = smsinfo.getTime();
        String AppName = smsinfo.getAppName();
        String Info2 = smsinfo.getInfo2();
        double Lat = smsinfo.getLat();
        double Lon = smsinfo.getLon();
        int index = smsinfo.getIndex();
        long CurrentTime = smsinfo.getSave_Time();
        long LastSMSSentTime = smsinfo.getLast_Sms_Time();
        String CellId = smsinfo.getCellId();
        String LAC = smsinfo.getLAC();
        String MCC = smsinfo.getMCC();
        String MNC = smsinfo.getMNC();

        for (int i = 1; i < Ds.structDNCC.bTotalNumbers; i++)
            if (!Ds.structDNCC.stDefunctNumber[i].equals("0000000000")) {
                Parent_no = Ds.structDNCC.stDefunctNumber[i];
                break;
            }
        int InfosentBy_Internet_SMS = 0;
        Exception = false;
        final String TAG = "callURLSMS";
        Url = null;
        int responce = 0;
        //	WebserviceCall com = new WebserviceCall();
        DeBug.ShowLog("SMSSENT", " sending info  " + Time);

        switch (InfoType) {
            case 2://For GL

                RestApiCall mRestApiCall1 = new RestApiCall();
                JSONObject json1 = new JSONObject();
                try {

                    json1.put("AppId", Ds.structPC.iStudId);
                    json1.put("Latitude", Lat);
                    json1.put("Longitude", Lon);
                    json1.put("LogDateTime", Time);
                    json1.put("SrvcCalledBy", "INTERNET");
                    json1.put("LocReq", index);
                    json1.put("LocSource", Info2 + "+" + smsinfo.Id);
                    json1.put("IsWithInGeoFence", Integer.parseInt(AppName));
                    json1.put("CellId", CellId);
                    json1.put("locationAreaCode", LAC);
                    json1.put("mobileCountryCode", MCC);
                    json1.put("mobileNetworkCode", MNC);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                responce = Integer.parseInt(mRestApiCall1.StuLoc(json1));


			/*			responce=com.SendStudLoc(Ds.structPC.iStudId, index, ""+Lon, ""+Lat, Time,Info2,"INTERNET",Integer.parseInt(AppName));
			 */
                break;

            case 3://for AA

                JSONObject json2 = new JSONObject();

                try {
				/*json2.put("SimNo",Ds.structPC.stSIMSerialno);*/
                    json2.put("SimNo", Ds.structPC.stSIMSerialno);
                    json2.put("AndroidAppId", Ds.structPC.iStudId);
                    json2.put("AppIdx", "" + index);
                    json2.put("AppName", AppName);
                    json2.put("Seq", "L");

                    responce = genarateJSON(AppName, index, Time, 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (responce != -1) {
                    ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(ctx);
                    mApplicationInfoDB.updateAppSyncStatusByNameandIndex(AppName, index);
                }

			/*			responce=com.UploadAppData( Ds.structPC.stSIMSerialno, ""+index, AppName, "L");
			 */
                break;
            case 4://for RA

                RestApiCall mRestApiCall10 = new RestApiCall();
                JSONObject json10 = new JSONObject();
                try {
                    json10.put("AndroidAppId", Ds.structPC.iStudId);
                    json10.put("appIdx", "" + index);
                    responce = genarateJSON(AppName, index, Time, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (responce != -1) {
                    ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(ctx);
                    mApplicationInfoDB.updateAppSyncStatusByNameandIndex(AppName, index);
                }
                //responce = Integer.parseInt(mRestApiCall10.setAppListUninstallStatus(context,json10));

			/*			responce=com.UninstallAppInfo(Ds.structPC.iStudId, ""+index);
			 */
                break;
            case 6://for Sim Change Info to Webserver For Log and Mobile Restart   #GS

                //		Struct_Send_SMSInfo	mStruct_Send_SMSInfo1 =  getCellInfo(context);

                RestApiCall mRestApiCall4 = new RestApiCall();
                JSONObject json4 = new JSONObject();
                try {
                    json4.put("AppId", Ds.structPC.iStudId);
                    json4.put("Lat", Lat);
                    json4.put("Long", Lon);
                    json4.put("dateTime", Time);
                    json4.put("uploadedByAndroidAppId", Ds.structPC.iStudId);
                    json4.put("Remarks", Info2);
                    json4.put("functionalityId", 10);
                    json4.put("cellId", CellId);
                    json4.put("locationAreaCode", LAC);
                    json4.put("mobileCountryCode", MCC);
                    json4.put("mobileNetworkCode", MNC);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responce = Integer.parseInt(mRestApiCall4.BtnPress_XMLFormat(ctx, json4));

			/*			responce=com.setImageAttendenceStatus(Ds.structPC.iStudId,Time,"0","0",10,""+Info2,Ds.structPC.iStudId);
			 */
                DeBug.ShowLog("LMMSG", "Sending Mobile On/Off via webservice..." + responce);
                break;
            case 601:
                mRestApiCall4 = new RestApiCall();
                json4 = new JSONObject();
                try {
                    json4.put("AppId", Ds.structPC.iStudId);
                    json4.put("Lat", Lat);
                    json4.put("Long", Lon);
                    json4.put("dateTime", Time);
                    json4.put("uploadedByAndroidAppId", Ds.structPC.iStudId);
                    json4.put("Remarks", AppName + " " + smsinfo.Id);
                    json4.put("functionalityId", index);
                    json4.put("cellId", CellId);
                    json4.put("locationAreaCode", LAC);
                    json4.put("mobileCountryCode", MCC);
                    json4.put("mobileNetworkCode", MNC);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responce = Integer.parseInt(mRestApiCall4.BtnPress_XMLFormat(ctx, json4));

			/*			responce=com.setImageAttendenceStatus(Ds.structPC.iStudId,Time,"0","0",10,""+Info2,Ds.structPC.iStudId);
			 */
                DeBug.ShowLog("LMMSG", "Sending Mobile On/Off via webservice..." + responce);
                break;
            case 10:    //  VN - Version Number generated by Nimboli     #GS
                DeBug.ShowLog("LMMSG", "Sending version Number via webservice..." + "" + Info2);
                RestApiCall mRestApiCall8 = new RestApiCall();
                JSONObject json8 = new JSONObject();
                try {
                    json8.put("AppId", Ds.structPC.iStudId);
                    json8.put("dateTime", Time);
                    json8.put("ClientVersionMDM", Info2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responce = Integer.parseInt(mRestApiCall8.VersionNo(json8));
			/*			responce=com.SendVersionNumber(Ds.structPC.iStudId, ""+Time, ""+Info2);
			 */
                break;
        }
        if (responce == -1) {
            DeBug.ShowLog(TAG, "responce of  sendInfoToServer  ClientProtocolException " + InfoType + " = " + (responce - 48));

            if (SMSsend == 1) {
                Exception = true;
                InfosentBy_Internet_SMS = 1;
            }
            return -1;
        } else if (responce == 1 || responce == 0) {

            Struct_Send_SMSInfo.SMSsent = 2;
            if (InfoType == 2) {
                //if(Thread_SendSMS.databasecount ==0)
				/*	if(index != 0 )
					ApplicationLog.StartService(context,"GPNetSms","Internet",0);
				else
					ApplicationLog.StartService(context,"GLNetSms","Internet",5);	*/

                InternetsentCount++;
            } else if (InfoType == 6) {
                Struct_Send_SMSInfo.SMSsent = 2;
            }
            return 1;
        }
        return InfosentBy_Internet_SMS;

    }

    private static int genarateJSON(String stAppName, int AppIndex, String Time, int isInstall) {
        int allAppsUploaded = -1;
        JSONObject json = new JSONObject();
        JSONArray appArray = new JSONArray();

        {
            JSONObject jsonApp = new JSONObject();
            try {
                stAppName = URLEncoder.encode(stAppName, "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }


            stAppName = stAppName.replace("%C2%A0", "");
            try {
                stAppName = URLDecoder.decode(stAppName, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                jsonApp.put("ChatApp", "" + stAppName.trim());
                jsonApp.put("AppIndx", "" + AppIndex);
                jsonApp.put("IsInstalled", isInstall);
                jsonApp.put("LogDateTime", Time);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            appArray.put(jsonApp);
        }
        try {
            json.put("AppId", Ds.structPC.iStudId);
            json.put("chatAppLst", appArray);
            DeBug.ShowLog("AppList", "" + appArray);
            RestApiCall mRestApiCall = new RestApiCall();
            allAppsUploaded = Integer.parseInt(mRestApiCall.SetAppList(json));
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return allAppsUploaded;

    }

    public static void SI(int onoff) //1=audio in is OFF  0 =  audio in is ON
    {
        //DeBug.ShowToast(context,"SI " +onoff);

        if (onoff == 1) {
            Ds.structPC.bMicroPhone = true;
            CallDetectService.audioManager1.setMicrophoneMute(true);
        } else if (onoff == 0) {
            Ds.structPC.bMicroPhone = false;
            CallDetectService.audioManager1.setMicrophoneMute(false);
        }
        CallDetectService.callDetectService.editor.putBoolean("structPC.bMicroPhone", Ds.structPC.bMicroPhone);

    }

    public static void SO(int onoff) //1=audio out is OFF  0 =  audio out is ON
    {
        //DeBug.ShowToast(context,"SO " +onoff);
        if (onoff == 0) {

            Ds.structPC.bSpeaker = false;
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_RING, false);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_ALARM, false);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_DTMF, false);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_MUSIC, false);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        } else if (onoff == 1) {
            Ds.structPC.bSpeaker = true;
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_RING, true);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_ALARM, true);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_DTMF, true);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_MUSIC, true);
            CallDetectService.audioManager1.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
        CallDetectService.callDetectService.editor.putBoolean("structPC.bSpeaker", Ds.structPC.bSpeaker);
    }

    public CallHelper(Context ctxa) {
        ctx = ctxa;


        //		UAD = new UpLoadAppsData(context);
        //		CPR = new Check_Parent_Register();

        //callStateListener = new CallStateListener();
        incomingReceiver = new IncomingReceiver();
        messageReceiver = new MessageReceiver();
        Ds.msgrcvrhashcode = messageReceiver.hashCode();
        initSharedPreferences(ctxa);
    }


    public static int getET(int feature, int Day, int ST) {
        boolean Break = false;
        int hr = 0;
        int min = 0;

        for (hr = ST / 100; hr < 24; hr++) {
            min = 0;
            if (hr == ST / 100)
                min = ST % 100;
            if ((Ds.structNCC.lAllowedTime[Day][feature][hr] != 0x0FFFFFFFFFFFFFFFL))
                for (; min < 60; min++) {
                    // 	DeBug.ShowLogD("TESTMSG","hula . . ."+" "+hr+" "+Long.toHexString(structFCC.lAllowedTime[Day][feature][hr]&MIN_LUT[min]));
                    if ((Ds.structNCC.lAllowedTime[Day][feature][hr] & MIN_LUT[min]) == 0) {
                        //   	DeBug.ShowLogD("TESTMSG","hula . . .1"+" "+hr+" "+Long.toHexString( Ds.structFCC.lAllowedTime[Day][feature][hr]&MIN_LUT[min]));
                        Break = true;
                        break;
                    }
                }

            if (Break)
                break;
        }
        if (Break)
            return (hr * 100 + min);
        else
            return (23 * 100 + 60);
    }

    public static int getST(int feature, int Day, int ST) {
        boolean Break = false;
        int hr = 0;
        int min = 0;

        for (hr = ST / 100; hr < 24; hr++) {
            min = 0;
            if (hr == ST / 100)
                min = ST % 100;
            if ((Ds.structNCC.lAllowedTime[Day][feature][hr] != 0x0FFFFFFFFFFFFFFFL))
                for (; min < 60; min++) {
                    // 	DeBug.ShowLogD("TESTMSG","hula . . ."+" "+hr+" "+Long.toHexString(structFCC.lAllowedTime[Day][feature][hr]&MIN_LUT[min]));
                    if ((Ds.structNCC.lAllowedTime[Day][feature][hr] & MIN_LUT[min]) != 0) {
                        //   	DeBug.ShowLogD("TESTMSG","hula . . .1"+" "+hr+" "+Long.toHexString( Ds.structFCC.lAllowedTime[Day][feature][hr]&MIN_LUT[min]));
                        Break = true;
                        break;
                    }
                }

            if (Break)
                break;
        }
        if (Break)
            return (hr * 100 + min);
        else
            return (23 * 100 + 60);
    }

    public static int timeDiff(int TimeA, int TimeB) {
        int diff = 0;
        if (TimeB > TimeA) {
            while (TimeA < TimeB) {
                TimeA++;
                diff++;
                if ((TimeA % 100) == 60)
                    TimeA = TimeA + 40;
            }

        } else if (TimeA > TimeB)
            while (TimeB < TimeA) {
                TimeB++;
                diff++;
                if ((TimeB % 100) == 60)
                    TimeB = TimeB + 40;
            }

        return diff;
    }

    /*******TO get Date time from APP hour *********/      // #GS
    public static String GetTimeWithDate() {
        int date1 = CallHelper.Ds.structPC.iCurrentDate;
        int month = CallHelper.Ds.structPC.iCurrentMonth + 1;
        int year = CallHelper.Ds.structPC.iCurrentYear;
        String TimeStamp = converttoDoubleDigit(date1) + "-" + converttoDoubleDigit(month) + "-" + year + " "
                + converttoDoubleDigit(OneMinuteTimerService.tempAppHrMin / 100) + ":"
                + converttoDoubleDigit(OneMinuteTimerService.tempAppHrMin % 100);
        try {
            Date date = new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(TimeStamp);
            TimeStamp = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(date);
        } catch (Exception ignore) {
        }
        return TimeStamp;
    }

    /*******TO get Date time from APP hour *********/      // #GS
    public static int GetSystymasTempAppHrMin() {
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        int Hour = calendar.get(Calendar.HOUR_OF_DAY);
        int Min = calendar.get(Calendar.MINUTE);
        //calendar.set(Calendar.HOUR_OF_DAY, OneMinuteTimerReceiver.tempAppHrMin/100);
        //calendar.set(Calendar.MINUTE, OneMinuteTimerReceiver.tempAppHrMin%100);
        int TimeStamp = (Hour * 100) + Min;

        return TimeStamp;
    }

    /*************************************************************************
     * Function to get Call details:
     * send to server through Internet
     * if failed save to database to try again
     * *********************************************************************/
    @SuppressWarnings("null")
    private void getCallDetails(Context context, Calendar cal) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        else
            settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        long dialed;
        String stTypeofCall = null;
        String columns[] = new String[]
                {
                        CallLog.Calls._ID,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                        CallLog.Calls.DURATION,
                        CallLog.Calls.TYPE};

        Cursor c = context.getContentResolver().query(Uri.parse("content://call_log/calls"), columns, null, null, CallLog.Calls.DATE + " desc"); //last record first

        c.moveToFirst();

        if ((settings.getLong("lastcallTime", 0L) == c.getLong(2)))
            return;

        editor.putLong("lastcallTime", c.getLong(2));
        editor.commit();
        dialed = cal.getTimeInMillis();
        switch (c.getInt(4)) {
            case 1:
                stTypeofCall = "InComing";
                break;
            case 2:
                stTypeofCall = "OutGoing";
                break;
            case 3:
                stTypeofCall = "Missed";
                break;
            default:
                break;
        }

        DeBug.ShowLog(Incoming, "" + c.getLong(2));

        String From = "";
        String To = "";
        int isIncoming = c.getInt(4);
        if (c.getInt(4) == 2) {
            isIncoming = 0;
            To = c.getString(1);
            From = "";
        } else {
            isIncoming = 1;
            From = c.getString(1);
            To = "";
        }

        String TimeStamp = formatter.format(cal.getTime()).toString();
        int responce = -1;

        Struct_Send_SMSInfo mStruct_Send_SMSInfo = new Struct_Send_SMSInfo();

        //	if((( Ds.structPC.bFactoryControlByte & 2) == 2 ))

        //	getCellIdforregular(context,0);


        //	WebserviceCall WSC= new  WebserviceCall();
        double lat = Ds.structLGC.dLatitude;
        double lon = Ds.structLGC.dLongitude;

        if (lat == 0 && lon == 0) {
            mStruct_Send_SMSInfo = getCellInfo(context);
        } else {
            mStruct_Send_SMSInfo.setCellId("0");
            mStruct_Send_SMSInfo.setMCC("0");
            mStruct_Send_SMSInfo.setMNC("0");
            mStruct_Send_SMSInfo.setLAC("0");
        }

        RestApiCall mRestApiCall = new RestApiCall();
        JSONObject json = new JSONObject();
        try {
            json.put("Lat", lat);
            json.put("Long", lon);
            json.put("startDateTime", "" + TimeStamp);
            json.put("IsIncoming", (int) isIncoming);
            json.put("AppId", Ds.structPC.iStudId);
            json.put("From", From);
            json.put("To", To);
            json.put("duration", c.getInt(3));
            json.put("cellId", mStruct_Send_SMSInfo.getCellId());
            json.put("locationAreaCode", mStruct_Send_SMSInfo.getLAC());
            json.put("mobileCountryCode", mStruct_Send_SMSInfo.getMCC());
            json.put("mobileNetworkCode", mStruct_Send_SMSInfo.getMNC());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            responce = Integer.parseInt(mRestApiCall.CallLogs(json));
            DeBug.ShowLog("CallLog", "7 " + json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
		/*		responce=WSC.SendCallLog(Ds.structPC.iStudId,(int) isIncoming, From, To, ""+TimeStamp, c.getInt(3),""+Ds.structLGC.dLatitude,""+Ds.structLGC.dLongitude);
		 */
        DeBug.ShowLog("CallDB", "type :" + stTypeofCall + " Number: " + c.getString(1) + "\nregistered at: " + new Date(dialed).toString() + " Location " + Ds.structLGC.dLatitude);

        if (responce < 0) {
            Struct_Contact mStruct_Contact = new Struct_Contact("Ram", c.getString(1), dialed, c.getInt(3), c.getInt(4), "" + Ds.structLGC.dLatitude, "" + Ds.structLGC.dLongitude);
            mStruct_Contact.setCellId(mStruct_Send_SMSInfo.getCellId());
            mStruct_Contact.setLAC(mStruct_Send_SMSInfo.getLAC());
            mStruct_Contact.setMCC(mStruct_Send_SMSInfo.getMCC());
            mStruct_Contact.setMNC(mStruct_Send_SMSInfo.getMNC());
            Call_db.addContact(mStruct_Contact);
        }
    }

    public static String[] getNames(JSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator iterator = jo.keys();
        String[] names = new String[length];
        int i = 0;
        while (iterator.hasNext()) {
            names[i] = (String) iterator.next();
            i += 1;
        }
        return names;
    }

    /******************************************************
     * Function for checking location is withen Geofence or Not
     * *****************************************************/
    public static int getInsides(double Latitude, double Longitude, double geofenceradius) {
        double refLatitude = Ds.structLGC.dCurrentGeoFenceLatitude;
        double refLongitude = Ds.structLGC.dCurrentGeoFenceLongitude;

		/* int dn    = 0;
			 double de = geofenceradius;

			 //Coordinate offsets in radians
			 double dLat = dn/EarthRadius;  // = 0
			 double dLon = de/(EarthRadius*Math.cos(Math.PI*savedLatitude/180));

			 //OffsetPosition, decimal degrees
			 double latO = savedLatitude + (dLat * 180/Math.PI);
			 double lonO = savedLongitude + (dLon * 180/Math.PI);

			 DeBug.ShowLog("GEOFENCE", ""+latO +"   "+lonO);
			 double radius= (lonO - savedLongitude);
			 System.out.println(""+radius);

			double p = (Latitude-savedLatitude)*(Latitude-savedLatitude) + (Longitude-savedLongitude)*(Longitude-savedLongitude);
			System.out.println("jg  "+p);*/
        int in_out = 0;

        double R = 6371;
        double lat1 = Math.PI * refLatitude / 180;
        double lat2 = Math.PI * Latitude / 180;

        double lon1 = Math.PI * refLongitude / 180;
        double lon2 = Math.PI * Longitude / 180;

        double Dlat = lat1 - lat2;
        double Dlon = lon1 - lon2;

        double a = Math.sin(Dlat / 2) * Math.sin(Dlat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(Dlon / 2) * Math.sin(Dlon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double d = c * R * 1000;

		/*if( (p < radius*radius) && (Ds.structLGC.bUserGeogenceStatus != 1))
			{
				 DeBug.ShowLog("GEOFENCE", ""+"IN");
				 in_out=1;
				 Ds.structLGC.bUserGeogenceStatus=1;
			}
			else if( (p > radius*radius) && (Ds.structLGC.bUserGeogenceStatus != 0))
			{
				 DeBug.ShowLog("GEOFENCE", ""+"OUT");
				 in_out=0;
				 Ds.structLGC.bUserGeogenceStatus=0;
			}
			else
			{
				in_out = -1;
				 DeBug.ShowLog("GEOFENCE", ""+"ON");
			}*/
        DeBug.ShowLog("GEOFENCE", "" + " geostatus " + Ds.structLGC.bUserGeogenceStatus + " refLatitude " + refLatitude + " radious " + geofenceradius);

        if ((d < geofenceradius) && (Ds.structLGC.bUserGeogenceStatus != 6)) {
            DeBug.ShowLog("GEOFENCE", "" + "IN");
            in_out = 6;
            Ds.structLGC.bUserGeogenceStatus = 6;
        } else if ((d > geofenceradius) && (Ds.structLGC.bUserGeogenceStatus != 9)) {
            DeBug.ShowLog("GEOFENCE", "" + "OUT");
            in_out = 9;
            Ds.structLGC.bUserGeogenceStatus = 9;
        } else {
            in_out = -1;
            DeBug.ShowLog("GEOFENCE", "" + "ON");
        }
        return in_out;
    }

    public static LocationManager getlocationManager(Context context) {
        if (locationManager == null)
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return locationManager;
    }

    /******************************************************
     * Function to register recievers Call/ SMS
     * *****************************************************/
    public void start() {
        IntentFilter filter = new IntentFilter("android.intent.action.PHONE_STATE");
        ctx.registerReceiver(incomingReceiver, filter);

        IntentFilter filter1 = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter1.setPriority(2147483647);
        ctx.registerReceiver(messageReceiver, filter1);

        DeBug.ShowLog("INFO", "" + messageReceiver.hashCode());
    }

    /******************************************************
     * Function to unregister recievers Call/ SMS
     * *****************************************************/
    protected void stop() {
        //tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
        ctx.unregisterReceiver(incomingReceiver);
        ctx.unregisterReceiver(messageReceiver);
    }

    public static Struct_Send_SMSInfo getCellInfo(final Context context) {
        mtelephonyManager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        GCL = (GsmCellLocation) mtelephonyManager.getCellLocation();
        Struct_Send_SMSInfo mStruct_Send_SMSInfo = new Struct_Send_SMSInfo();

        if (mtelephonyManager != null && GCL != null) {
            String networkOperator = mtelephonyManager.getNetworkOperator();
            if (TextUtils.isEmpty(networkOperator) || networkOperator.length() < 4) {
                mStruct_Send_SMSInfo.setCellId("" + 0);
                mStruct_Send_SMSInfo.setLAC("" + 0);
                mStruct_Send_SMSInfo.setMCC("" + 0);
                mStruct_Send_SMSInfo.setMNC("" + 0);
            } else {
                String mobileCountryCode = "0";
                String mobileNetworkCode = "0";
                try {
                    mobileCountryCode = networkOperator.substring(0, 3);
                    mobileNetworkCode = networkOperator.substring(3);
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }

                mStruct_Send_SMSInfo.setCellId("" + GCL.getCid());
                mStruct_Send_SMSInfo.setLAC("" + GCL.getLac());
                mStruct_Send_SMSInfo.setMCC(mobileCountryCode);
                mStruct_Send_SMSInfo.setMNC(mobileNetworkCode);
            }
        } else {
            mStruct_Send_SMSInfo.setCellId("" + 0);
            mStruct_Send_SMSInfo.setLAC("" + 0);
            mStruct_Send_SMSInfo.setMCC("" + 0);
            mStruct_Send_SMSInfo.setMNC("" + 0);
        }
        return mStruct_Send_SMSInfo;
    }

    public static String converInLocalFormate(String time) {
        String date = "";
        try {
            //String strCurrentDate = "Wed, 18 Apr 2012 07:55:29 +0000";//2015-12-19 12:18:39//19-Jan-2015 05:48 PM
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date newDate;
            newDate = format.parse(time);
            format = new SimpleDateFormat("dd-MMM-yyyy hh:mm aaa");
            //format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.format(newDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;

    }

    public static void wipingSdcard() {
        File deleteMatchingFile = new File(Environment.getExternalStorageDirectory().toString());

        File deleteMatchingFile1 = new File(Environment.getDataDirectory().toString());

        DeBug.ShowLog("", "  " + deleteMatchingFile + "  " + deleteMatchingFile1);
        try {
            File[] filenames = deleteMatchingFile.listFiles();
            if (filenames != null && filenames.length > 0) {
                for (File tempFile : filenames) {
                    if (tempFile.isDirectory()) {
                        wipeDirectory(tempFile.toString());
                        tempFile.delete();
                    } else {
                        tempFile.delete();
                    }
                }
            } else {
                deleteMatchingFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void wipeDirectory(String name) {
        File directoryFile = new File(name);
        File[] filenames = directoryFile.listFiles();
        if (filenames != null && filenames.length > 0) {
            for (File tempFile : filenames) {
                if (tempFile.isDirectory()) {
                    wipeDirectory(tempFile.toString());
                    tempFile.delete();
                } else {
                    tempFile.delete();
                }
            }
        } else {
            directoryFile.delete();
        }
    }

    public static int createFolder(String Path) {
        int result = -1;
        String SDCardLocation = Environment.getExternalStorageDirectory().getAbsolutePath();

        String path_names[] = Path.split("/");

        String PathToBeCteated = SDCardLocation + "/";
        int lastFolderIndex = path_names.length - 1;
        for (int i = 0; i < path_names.length; i++) {
            PathToBeCteated = PathToBeCteated + path_names[i] + "/";
            File NewFolderCreator = new File(PathToBeCteated);


            boolean fileexist = false;
            if (!NewFolderCreator.exists()) {
                if (NewFolderCreator.mkdirs()) {
                    fileexist = true;
                    //result++;
                }
            } else {
                fileexist = true;
            }

            if (lastFolderIndex == i && fileexist) {
                result = 1;
            }
        }
		/*String Path_Nimboli = SDCardLocation + "/Nimboli/";
		String Path_AppLog  = SDCardLocation + "/Nimboli/AppLog/";

		File NewFolderCreator=new File(Path_Nimboli);
		if(!NewFolderCreator.exists())
		{
			if(NewFolderCreator.mkdirs())
				result=1;

			NewFolderCreator=new File(Path_AppLog);
			if(NewFolderCreator.mkdirs())
				result=2;
		}
		else
		{
			NewFolderCreator=new File(Path_AppLog);
			if(NewFolderCreator.mkdirs())
				result=2;
		}*/

        return result;

    }

    public static ArrayList<InsertIntoContactSync> getContactList(Context context) {

        ArrayList<String> listPhoneNumberForServer = new ArrayList<String>();

        String phoneNumber = null;
        //String email = null;
        ArrayList<InsertIntoContactSync> listInsertIntoContactSync = new ArrayList<InsertIntoContactSync>();
        ArrayList<String> listPhoneNumber = new ArrayList<String>();

        String CurrentTime = "" + System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(CurrentTime));
        DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
        String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        StringBuffer output = new StringBuffer();

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null, null, null, null);

        // Loop for every contact in the phone
        int j = 0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                try {
                    String email = null;

                    String contact_id = cursor.getString(cursor.getColumnIndex(_ID));
                    String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));

                    int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));

                    if (hasPhoneNumber > 0) {

                        if (name.equalsIgnoreCase("mahendra"))
                            output.append("\n First Name:" + name);

                        // Query and loop for every phone number of the contact
                        Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[]{contact_id}, null);
                        if (phoneCursor.moveToNext()) {
                            phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                            String lookUp = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                            output.append("\n Phone number:" + phoneNumber);

                            if (listPhoneNumber.contains(phoneNumber)) {
                                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookUp);
                                DeBug.ShowLog("ContactSync", phoneNumber + " " + name + " " + contentResolver.delete(uri, null, null));
                                continue;
                            } else {
                                listPhoneNumber.add(phoneNumber);
                            }
                        }

                        phoneCursor.close();

                        // Query and loop for every email of the contact
                        Cursor emailCursor = contentResolver.query(EmailCONTENT_URI, null, EmailCONTACT_ID + " = ?", new String[]{contact_id}, null);

                        while (emailCursor.moveToNext()) {

                            email = emailCursor.getString(emailCursor.getColumnIndex(DATA));

                            output.append("\nEmail Id:" + email);

                        }

                        emailCursor.close();
                    }

                    output.append("\n");

                    phoneNumber = phoneNumber.replace(" ", "");

                    if (phoneNumber.contains("9579543434"))
                        DeBug.ShowLogD("AginAndAgain", "" + phoneNumber);

                    StringBuffer bb = new StringBuffer(phoneNumber);

                    if (phoneNumber.startsWith("091"))
                        bb.delete(0, 3);
                    else if (phoneNumber.startsWith("+91"))
                        bb.delete(0, 3);
                    else if (phoneNumber.startsWith("0"))
                        bb.delete(0, 1);
                    else if (phoneNumber.startsWith("+"))
                        bb.delete(0, 1);


                    if (phoneNumber.contains("9579543434"))
                        DeBug.ShowLogD("AginAndAgain", "" + bb.toString());

                    //		WebserviceCall WSC = new WebserviceCall();


                    if (!listPhoneNumberForServer.contains(bb.toString())) {
                        listPhoneNumberForServer.add(bb.toString());
                        InsertIntoContactSync mInsertIntoContactSync = new InsertIntoContactSync(name, bb.toString(), email, SMSTimeStamp);
                        mInsertIntoContactSync.setMessangerId("" + 0);
                        listInsertIntoContactSync.add(mInsertIntoContactSync);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return listInsertIntoContactSync;
    }


    public static List<Sms.SMS> getAllSms(Context context) {
        Sms sms = new Sms();
        Sms.SMS objSms = sms.newSMS();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = context.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = sms.newSMS();
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(c.getString(c.getColumnIndexOrThrow("date"))));
                DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
                objSms.setStartDateTime(SMSTimeStamp);
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setIsIncoming(1);
                } else {
                    objSms.setIsIncoming(0);
                }
                sms.addToList(objSms);
                c.moveToNext();
            }
        }
        // else {
        // throw new RuntimeException("You have no SMS");
        // }
        c.close();

        return sms.getSmsList();
    }

}
