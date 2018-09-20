package com.mobiocean.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.mobiocean.R;
import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.database.DatabaseHandler_Call;
import com.mobiocean.database.DatabaseHandler_SMS;
import com.mobiocean.mobidb.ApplicationInfoDB;
import com.mobiocean.mobidb.LogInfoDatabaseHandler;
import com.mobiocean.mobidb.LogInfoStruct;
import com.mobiocean.receiver.BootReceiver;
import com.mobiocean.ui.eFeatureSetting;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.Constant;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.StringCryptor;
import com.mobiocean.util.Struct_Contact;
import com.mobiocean.util.Struct_SMS;
import com.mobiocean.util.Struct_Send_SMSInfo;
import com.mobiocean.util.eFeatureControl;

import org.conveyance.configuration.RSharedData;
import org.conveyance.services.RGetTowerLocationService;
import org.json.JSONException;
import org.json.JSONObject;
import org.sn.location.LocationDetails;
import org.sn.location.NetworkUtil;
import org.sn.util.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import static com.mobiocean.util.CallHelper.iWeekDay;
import static com.mobiocean.util.Constant.APP_CODE;
import static com.mobiocean.util.Constant.COUNTRY_CODE;
import static com.mobiocean.util.Constant.COUNTRY_INDEX;
import static com.mobiocean.util.Constant.GCM_SERVER_URL;
import static com.mobiocean.util.Constant.MIN_LUT;

public class OneMinuteTimerService extends Service {

    public static boolean bOneMinuteTimerStarted = false;
    public static boolean timerRunning = false;

    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    protected static long oldtimeinmilies = 0;

    public static int tempAppHrMin = 0;
    protected static Context context;
    public static boolean servicesrestart = false;
    public static boolean appresarted = true;
    protected static int ServiceStartCounter;

    DatabaseHandler_Call Call_db;
    DatabaseHandler_SMS SMS_db;
    protected static int responce = 0;
    private PowerManager.WakeLock wakeLock;

    static int x = 0;

    private Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DoNotSleep");
        wakeLock.acquire();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 60000);
                checkServicerRunning(context);
                try {
                    CallHelper.locationIsUploading = false;
                    timerRunning = true;
                    settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    editor = settings.edit();
                    bOneMinuteTimerStarted = true;
                    CallHelper.Ds.structCCC.wFeatureControlWord[0] = (long) settings.getLong("structCCC.wFeatureControlWord" + 0, CallHelper.Ds.structCCC.wFeatureControlWord[0]);
                    long time = System.currentTimeMillis();
                    /******************************************************************************
                     *  Read from sheredprefarance in Services Restarts:
                     * *****************************************************************************/
                    if (appresarted) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                readfromsharedpref(context);
                            }
                        }).start();
                    }
                    DeBug.ShowLog("SettingMSG", "INIT " + Long.toBinaryString(CallHelper.Ds.structCCC.wFeatureControlWord[0]));
                    /******************************************************************************
                     *  If Services restarted more than 30 times send server request for Reset Time:
                     * *****************************************************************************/
                    if (ServiceStartCounter >= 15) {
                        ServiceStartCounter = 0;
                        editor.putInt("ServiceStartCounter", ServiceStartCounter);
                        onServiceStart(context);
                    } else {
                        ServiceStartCounter = settings.getInt("ServiceStartCounter", ServiceStartCounter);
                        ServiceStartCounter++;
                        editor.putInt("ServiceStartCounter", ServiceStartCounter);
                    }
                    editor.commit();
                    CallHelper.GPSCount = settings.getInt("CallHelper.GPSCount", CallHelper.GPSCount);
                    CallHelper.GoogleCount = settings.getInt("CallHelper.GoogleCount", CallHelper.GoogleCount);
                    CallHelper.CurrentLocSlote = settings.getInt("CallHelper.CurrentLocSlote", CallHelper.CurrentLocSlote);
                    CallHelper.Ds.structLGC.bTimesCount = (byte) settings.getInt("structLGC.bTimesCount", CallHelper.Ds.structLGC.bTimesCount);
                    CallHelper.Ds.structPC.bPhoneNoUploded = settings.getBoolean("structPC.bPhoneNoUploded", CallHelper.Ds.structPC.bPhoneNoUploded);
                    CallHelper.Ds.structPC.TotalApps = settings.getInt("structPC.TotalApps", CallHelper.Ds.structPC.TotalApps);

                    /******************************************************************************
                     *  Maintain AppHrMin
                     * *****************************************************************************/
                    maintainOurTimer(context);

                    /******************************************************************************
                     *  if tempAppHrMin value greater that 2400 means next day started:
                     * *****************************************************************************/
                    if (tempAppHrMin >= 2400)
                        nextDayStarted(context);

                    /******************************************************************************
                     *  save data while (call in on/appblock is active):
                     * *****************************************************************************/
                    saveDataToShearedprefarenceWhileCallisGoingOn(context);


                    Calendar dtNow = Calendar.getInstance();
                    //dtNow.setToNow();
                    //DeBug.ShowToast(context, "Current date "+dtNow.get(Calendar.DATE));

                    /******************************************************************************
                     *  ckeck if app expired or not:
                     * *****************************************************************************/
                    //	if(oneMinTimeExp)
                    if (dtNow.after(CallHelper.Ds.structPC.ExpiryDate)) {
                        //stopService(intentCallDetectService);

                        //stopService(intentAppBlock);
                        DeBug.ShowToast(context, "Demo Time Expired    " + CallHelper.Ds.structPC.bTimeExpired);

                        if (!CallHelper.Ds.structPC.bTimeExpired) {
                            CallHelper.Ds.structPC.bTimeExpired = true;
                            editor.putBoolean("structPC.bTimeExpired", CallHelper.Ds.structPC.bTimeExpired);
                            DeBug.ShowLog("structPC.bTimeExpired", "" + CallHelper.Ds.structPC.bTimeExpired);
                        }
                    } else {

                        if (CallHelper.Ds.structPC.bTimeExpired) {
                            CallHelper.Ds.structPC.bTimeExpired = false;
                            editor.putBoolean("structPC.bTimeExpired", CallHelper.Ds.structPC.bTimeExpired);
                            DeBug.ShowToast(context, "Time not Expired     " + CallHelper.Ds.structPC.bTimeExpired);
                            DeBug.ShowLog("structPC.bTimeExpired", "" + CallHelper.Ds.structPC.bTimeExpired);
                        }


                        /******************************************************************************
                         *  send AL SMS:
                         * *****************************************************************************/
                        sendALSms(context);

                        if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context))
                            CallAndSMSLogsToServer(context);

                        /******************************************************************************
                         *  call Geofence check fun:
                         *  call location tracking function to get location:
                         * *****************************************************************************/
                        if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId))
                            if (CallHelper.TimeChangedCounter == 0)
                                try {

                                } catch (Exception e) {
                                    DeBug.ShowLogD("EXP", " showCurrentLocation not called CallDetectService killed by OS :(" + e.getMessage());
                                    context.startService(new Intent(context, CallDetectService.class));
                                    context.startService(new Intent(context, AppBlock.class));
                                    e.printStackTrace();
                                }

                        if (CallHelper.TimeChangedCounter > 0)
                            CallHelper.TimeChangedCounter--;
                    }

                    /******************************************************************************
                     *  If SIM changes Start Activity to show properMessage:
                     * *****************************************************************************/
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            onMobileRestarts(context);
                        }
                    }).start();

                    retriveAppGroupInfo(context);

                    /******************************************************
                     * Send version number of the application    	#GS
                     * ****************************************************/
                    if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId))
                        sendVersionInfo(context);

                    DeBug.ShowLog("CRASH", "tempAppHrMin   " + tempAppHrMin);

                    DeBug.ShowLog("CRASH", "GetSystymasTempAppHrMin    " + CallHelper.GetSystymasTempAppHrMin());

                    DeBug.ShowLog("CRASH", "CallHelper.Ds.structLGC.bGPSRequestTimer   " + (tempAppHrMin + CallHelper.Ds.structLGC.bGPSRequestTimer));
                    CallHelper.Ds.lSchoolSchedule.iCurrentDate = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DAY_OF_MONTH);
                    CallHelper.Ds.lSchoolSchedule.iCurrentMonth = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH);
                    CallHelper.Ds.lSchoolSchedule.iCurrentYear = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.YEAR);
                    oldtimeinmilies = System.currentTimeMillis();

                    if (!servicesrestart && !CallHelper.Ds.structPC.bDateChangedToDefault) {
                        DeBug.ShowToast(context, "AppHrMin before saving " + tempAppHrMin);
                        editor.putInt("structPC.AppHrMin", tempAppHrMin);
                        editor.putInt("structPC.bWeekDay", CallHelper.Ds.structPC.bWeekDay);
                        editor.putInt("lSchoolSchedule.iCurrentDate", CallHelper.Ds.lSchoolSchedule.iCurrentDate);
                        editor.putInt("lSchoolSchedule.iCurrentMonth", CallHelper.Ds.lSchoolSchedule.iCurrentMonth);
                        editor.putInt("lSchoolSchedule.iCurrentYear", CallHelper.Ds.lSchoolSchedule.iCurrentYear);
                    } else
                        servicesrestart = false;

                    editor.putInt("lSchoolSchedule.iCurrentSchoolSchedule", CallHelper.Ds.lSchoolSchedule.iCurrentSchoolSchedule);

                    if (CallHelper.Ds.structLGC.bTimesCount >= CallHelper.Ds.structLGC.bGPSRequestTimer)
                        editor.putInt("structLGC.bTimesCount", 0);
                    else
                        editor.putInt("structLGC.bTimesCount", CallHelper.Ds.structLGC.bTimesCount);

                    editor.commit();

                    DeBug.ShowLogD("EXP", CallHelper.Ds.structPC.bWeekDay + " OneMinuteTimerReceiver Total Time for " + CallHelper.Ds.structLGC.bTimesCount + " is " + (System.currentTimeMillis() - time) + " AppHr " + tempAppHrMin);

                    int Apphrmmi = settings.getInt("structPC.AppHrMin", 0);
                    DeBug.ShowLog("EXP", "CallHelper.Ds.lSchoolSchedule.StartWIFI " + CallHelper.Ds.lSchoolSchedule.StartWIFI);
                    DeBug.ShowToast(context, "AppHrMin after saving " + Apphrmmi);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (CallHelper.Ds.structLGC.bTimesCount >= CallHelper.Ds.structLGC.bGPSRequestTimer)
                    CallHelper.Ds.structLGC.bTimesCount = 0;

                if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId)) {
                    if (CallHelper.Ds.structACC.packageList.isEmpty()) {
                        //	CallHelper.Ds.structFCC.packageList.readFromSharedPreferences(settings);
                        //ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);
                        //ArrayList<Integer> list = mApplicationInfoDB.getGroups();

                        int size = settings.getInt("Ds.structACC.listGroupIds.size", 0);
                        CallHelper.Ds.structACC.listGroupIds = new ArrayList<String>();//read From shearedpref
                        for (int i = 0; i < size; i++) {
                            CallHelper.Ds.structACC.listGroupIds.add(settings.getString("Ds.structACC.listGroupIds" + i, ""));
                        }

                        CallHelper.Ds.inItApplicationStruct(context,
                                CallHelper.Ds.structACC.listGroupIds.size(),
                                CallHelper.Ds.structACC.listGroupIds);

				/*for(int i=0;i<list.size();i++)
                {
					CallHelper.Ds.structACC.packageList.addApplicationListToGroup(i, mApplicationInfoDB.getDataByGroup(i));
				}*/
                    }

                    getTimeSettingsFromPreferance(context);
                }


                saveAndsendAppLog(context);
                timerRunning = false;
            }
        };
        handler.post(runnable);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Send Log Report    #GS
     **/

    private void checkServicerRunning(Context context) {
        if (!Constants.isMyServiceRunning(context, CallDetectService.class)) {
            servicesrestart = true;
            startService(new Intent(context, CallDetectService.class));
        }
        if (!Constants.isMyServiceRunning(context, AppBlock.class)) {
            startService(new Intent(context, AppBlock.class));
        }
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        if (settings.getBoolean("Conveyance.isStarted", false) && !Constants.isMyServiceRunning(context, OneMinuteService.class)) {
            startService(new Intent(getBaseContext(), OneMinuteService.class));
        }
        RSharedData sharedData = new RSharedData(context);
        if (sharedData.getStatus() && !Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
            startService(new Intent(getBaseContext(), RGetTowerLocationService.class));
        }
    }

    /******************************************************************************
     *  Maintain AppHrMin
     * *****************************************************************************/
    private void maintainOurTimer(Context context) {
        incrementTimer(context);
   /*     CallHelper.Ds.structPC.AppHrMin = tempAppHrMin;

        boolean canTakeLocation = false;
        boolean canTrackGeoFence = false;
        boolean canAlertLocation = false;
        *//******************************************************************************
         * GeoFence code:
         * assign current tracking freq. default freq-1
         * *****************************************************************************//*
        if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.GEO_FENCE) == eFeatureControl.GEO_FENCE)
            canTrackGeoFence = getGeoFence(context);

        *//******************************************************************************
         * Location Alert code:
         * enable/disable Alert
         * assign current Alert freq. default freq-1
         * *****************************************************************************//*
        if (!canTrackGeoFence && (CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SET_LOCATION_ALERT) == eFeatureControl.SET_LOCATION_ALERT)
            canAlertLocation = getLocationAlert();

        *//******************************************************************************
         * Location Tracking code:
         * enable/disable tracking
         * assign current tracking freq.
         * *****************************************************************************//*
        if (!canTrackGeoFence && !canAlertLocation && (CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.lOCATE_DEVICE) == eFeatureControl.lOCATE_DEVICE)
            canTakeLocation = getLocationSettings();

        if (canTrackGeoFence || canTakeLocation || canAlertLocation) {
            {
                LocationDetails locationDetails = new LocationDetails(getBaseContext());
                locationDetails.timerLocation();
            }

            CallHelper.Ds.structPC.bFactoryControlByte = (byte) (CallHelper.Ds.structPC.bFactoryControlByte | 8);
            if (canTrackGeoFence && !canTakeLocation) {
                CallHelper.Ds.structLGC.bGPSRequestTimer = (byte) 15;
                editor.putInt("structLGC.bGPSRequestTimer", CallHelper.Ds.structLGC.bGPSRequestTimer);

            }
        } else
            CallHelper.Ds.structPC.bFactoryControlByte = (byte) (CallHelper.Ds.structPC.bFactoryControlByte & ~(1 << 3));

        editor.putInt("structPC.bFactoryControlByte", CallHelper.Ds.structPC.bFactoryControlByte);


        *//******************************************************************************
         * Send data to servers if it failed to send at first time:
         * *****************************************************************************//*
        if (CallHelper.Ds.structLGC.bTimesCount % 3 == 0)
            retrySendDataToServer(context);

        DeBug.ShowLog("NarayananTimer", "final AppHrMin  " + tempAppHrMin);*/

    }

    //Increment time in format of HHmm
    private void incrementTimer(Context context) {
        tempAppHrMin++;


        if ((tempAppHrMin % 100) == 60)
            tempAppHrMin = tempAppHrMin + 40;

        if (tempAppHrMin >= 2400) {
            nextDayStarted(context);
        }
    }

    public String converttoDoubleDigit(int x) {
        String doubledigit = "";
        if (x < 10)
            doubledigit = "0" + x;
        else
            doubledigit = "" + x;

        return doubledigit;

    }

    private void readfromsharedpref(Context context) {

        CallHelper.SMSsentCount = settings.getInt("CallHelper.SMSsentCount", CallHelper.SMSsentCount);
        CallHelper.InternetsentCount = settings.getInt("CallHelper.InternetsentCount", CallHelper.InternetsentCount);


        appresarted = false;
        //Restore AppHrMin After Restart/Reboot
        {
            CallHelper.fre_Updated_on = settings.getString("CallHelper.fre_Updated_on", CallHelper.fre_Updated_on);

            ServiceStartCounter = settings.getInt("ServiceStartCounter", ServiceStartCounter);
            ServiceStartCounter++;
            editor.putInt("ServiceStartCounter", ServiceStartCounter);
            //			DeBug.ShowLog("EXP", "           ServiceStartCounter   "+ServiceStartCounter +"   tempaphrmin "+tempAppHrMin);
            //			tempAppHrMin=settings.getInt("structPC.AppHrMin", CallHelper.Ds.structPC.AppHrMin);
            //			DeBug.ShowLog("EXP", "           ServiceStartCounter   "+ServiceStartCounter +"   tempaphrmin "+tempAppHrMin);
            CallHelper.Ds.structPC.bMode = (byte) settings.getInt("structPC.bMode", CallHelper.Ds.structPC.bMode);
            CallHelper.Ds.structPC.AppHrMin = settings.getInt("structPC.AppHrMin", CallHelper.Ds.structPC.AppHrMin);


            CallHelper.Ds.structPC.bWeekDay = (byte) settings.getInt("structPC.bWeekDay", CallHelper.Ds.structPC.bWeekDay);
            DeBug.ShowLog("CallDetectservice", "hr " + CallHelper.Ds.structPC.AppHrMin + " DAy " + CallHelper.Ds.structPC.bWeekDay);
            DeBug.ShowToast(OneMinuteTimerService.context, "AppHrMin reading from Service " + CallHelper.Ds.structPC.AppHrMin + " day  " + CallHelper.Ds.structPC.bWeekDay);

            CallHelper.Ds.structPC.iCurrentDate = settings.getInt("structPC.iCurrentDate", CallHelper.Ds.structPC.iCurrentDate);


            DeBug.ShowToast(OneMinuteTimerService.context, "AppHrMin   " + CallHelper.Ds.structPC.AppHrMin + "iCurrentDate   " + CallHelper.Ds.structPC.iCurrentDate);

            CallHelper.Ds.structPC.iCurrentMonth = settings.getInt("structPC.iCurrentMonth", CallHelper.Ds.structPC.iCurrentMonth);
            CallHelper.Ds.structPC.iCurrentYear = settings.getInt("structPC.iCurrentYear", CallHelper.Ds.structPC.iCurrentYear);
            // #GS			11-11-2014 12:37
            CallHelper.Ds.structPC.stMobile_SwitchOFF_dateTime = CallHelper.Ds.structPC.iCurrentDate + "-" + (CallHelper.Ds.structPC.iCurrentMonth + 1) +
                    "-" + CallHelper.Ds.structPC.iCurrentYear + " " + converttoDoubleDigit(CallHelper.Ds.structPC.AppHrMin / 100) + ":"
                    + converttoDoubleDigit(CallHelper.Ds.structPC.AppHrMin % 100);


            CallHelper.GPSCount = settings.getInt("CallHelper.GPSCount", CallHelper.GPSCount);
            CallHelper.GoogleCount = settings.getInt("CallHelper.GoogleCount", CallHelper.GoogleCount);
            CallHelper.iGPSHealthReportCount = settings.getInt("CallHelper.iGPSHealthReportCount", CallHelper.iGPSHealthReportCount);


            CallHelper.Ds.structPC.CurrentDate.set(CallHelper.Ds.structPC.iCurrentYear, CallHelper.Ds.structPC.iCurrentMonth, CallHelper.Ds.structPC.iCurrentDate);

            //get saved Current Date
            CallHelper.Ds.lSchoolSchedule.iCurrentDate = settings.getInt("lSchoolSchedule.iCurrentDate", CallHelper.Ds.lSchoolSchedule.iCurrentDate);
            CallHelper.Ds.lSchoolSchedule.iCurrentMonth = settings.getInt("lSchoolSchedule.iCurrentMonth", CallHelper.Ds.lSchoolSchedule.iCurrentMonth);
            CallHelper.Ds.lSchoolSchedule.iCurrentYear = settings.getInt("lSchoolSchedule.iCurrentYear", CallHelper.Ds.lSchoolSchedule.iCurrentYear);
            CallHelper.Ds.lSchoolSchedule.CurrentDate.set(CallHelper.Ds.lSchoolSchedule.iCurrentYear, CallHelper.Ds.lSchoolSchedule.iCurrentMonth, CallHelper.Ds.lSchoolSchedule.iCurrentDate);
            CallHelper.Ds.lSchoolSchedule.iCurrentSchoolSchedule = settings.getInt("lSchoolSchedule.iCurrentSchoolSchedule", CallHelper.Ds.lSchoolSchedule.iCurrentSchoolSchedule);
            CallHelper.Ds.structPC.stPhoneNo = settings.getString("structPC.stPhoneNo", CallHelper.Ds.structPC.stPhoneNo);
            CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);

            CallHelper.Ds.lSchoolSchedule.stSchoolCode = settings.getString("lSchoolSchedule.stSchoolCode", CallHelper.Ds.lSchoolSchedule.stSchoolCode);
            CallHelper.Ds.structLGC.bGPSRequestTimer = (byte) settings.getInt("structLGC.bGPSRequestTimer", CallHelper.Ds.structLGC.bGPSRequestTimer);
            CallHelper.Ds.structPC.bSimChanged = settings.getBoolean("structPC.bSimChanged", CallHelper.Ds.structPC.bSimChanged);
            CallHelper.SimChangeSentToServer = settings.getBoolean("CallHelper.SimChangeSentToServer", CallHelper.SimChangeSentToServer);

            /**
             * Check if Phone is restarted then Send info to Webserver
             *
             * **/


            if (BootReceiver.timediff < 0) {

                try {

                    {
                        try {
                            DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(OneMinuteTimerService.context);
                            String TimeStamp = CallHelper.GetTimeWithDate();
                            if (SMS_db.recordExist(TimeStamp, 5) == 0) {
                                SMS_db.addSMS(new Struct_Send_SMSInfo(5, "", "", "", 0.0, 0.0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                                Intent msgIntent = new Intent(OneMinuteTimerService.context, UploadService.class);
                                Bundle b = new Bundle();
                                msgIntent.putExtra("UploadStatus", 0);
                                msgIntent.putExtras(b);
                                OneMinuteTimerService.context.startService(msgIntent);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                CallHelper.Ds.structPC.bDateChangedToDefault = true;
            } else {
                int hr = (int) (BootReceiver.timediff / 60);

                int min = (int) (BootReceiver.timediff - (hr * 60));
                DeBug.ShowToast(OneMinuteTimerService.context, "hr " + hr + "min " + min);

                for (int i = 0; i < min; i++) {
                    CallHelper.Ds.structPC.AppHrMin++;
                    if (CallHelper.Ds.structPC.AppHrMin % 100 == 60)
                        CallHelper.Ds.structPC.AppHrMin = CallHelper.Ds.structPC.AppHrMin + 40;
                    if (CallHelper.Ds.structPC.AppHrMin >= 2400) {
                        CallHelper.Ds.structPC.AppHrMin = 0;
                        CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.DATE, true);
                        if (CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DATE) == 1) {
                            CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.MONTH, true);
                            if (CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH) == 0)
                                CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.YEAR, true);
                        }

                        CallHelper.Ds.structPC.bWeekDay++;
                        if (CallHelper.Ds.structPC.bWeekDay > 6)
                            CallHelper.Ds.structPC.bWeekDay = 0;
                        CallHelper.Ds.lSchoolSchedule.bSchoolHoliday = false;
                        CallHelper.Ds.lSchoolSchedule.iCurrentSchoolSchedule = -1;
                        CallHelper.Ds.lSchoolSchedule.iCurrentDate = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DATE);
                        CallHelper.Ds.lSchoolSchedule.iCurrentMonth = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH);
                        CallHelper.Ds.lSchoolSchedule.iCurrentYear = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.YEAR);

                        if (CallHelper.Ds.structPC.bWeekDay == 0) {
                            for (int day = 0; day < 7; day++) {
                                for (int i1 = 0; i1 < CallHelper.Ds.structFCC.bTotalFeatures; i1++) {
                                    CallHelper.Ds.structFCC.wUsedDuration[day][i1] = 0;
                                    CallHelper.Ds.structFCC.bPresentlyStopped[day][i1] = false;
                                    editor.putInt("structFCC.wUsedDuration" + day + "" + i1, CallHelper.Ds.structFCC.wUsedDuration[day][i1]);
                                    editor.putBoolean("structFCC.bPresentlyStopped" + day + "" + i1, CallHelper.Ds.structFCC.bPresentlyStopped[day][i1]);

                                }
                                for (int i1 = 0; i1 < 3; i1++) {
                                    CallHelper.Ds.structNCC.wUsedDuration[day][i1] = 0;
                                    CallHelper.Ds.structNCC.bPresentlyStopped[day][i1] = false;
                                    editor.putInt("structNCC.wUsedDuration" + day + "" + i1, CallHelper.Ds.structNCC.wUsedDuration[day][i1]);
                                    editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + i1, CallHelper.Ds.structNCC.bPresentlyStopped[day][i1]);
                                }
                            }
                        }
                    }
                }

                for (int i = 0; i < hr; i++) {
                    CallHelper.Ds.structPC.AppHrMin = CallHelper.Ds.structPC.AppHrMin + 100;
                    if (CallHelper.Ds.structPC.AppHrMin >= 2400) {
                        CallHelper.Ds.structPC.AppHrMin = CallHelper.Ds.structPC.AppHrMin % 100;

                        CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.DATE, true);
                        if (CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DATE) == 1) {
                            CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.MONTH, true);
                            if (CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH) == 0)
                                CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.YEAR, true);
                        }

                        CallHelper.Ds.structPC.bWeekDay++;
                        if (CallHelper.Ds.structPC.bWeekDay > 6)
                            CallHelper.Ds.structPC.bWeekDay = 0;
                        CallHelper.Ds.lSchoolSchedule.iCurrentSchoolSchedule = -1;
                        CallHelper.Ds.lSchoolSchedule.bSchoolHoliday = false;
                        CallHelper.Ds.lSchoolSchedule.iCurrentDate = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DATE);
                        CallHelper.Ds.lSchoolSchedule.iCurrentMonth = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH);
                        CallHelper.Ds.lSchoolSchedule.iCurrentYear = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.YEAR);

                        if (CallHelper.Ds.structPC.bWeekDay == 0) {
                            for (int day = 0; day < 7; day++) {
                                for (int i1 = 0; i1 < CallHelper.Ds.structFCC.bTotalFeatures; i1++) {
                                    CallHelper.Ds.structFCC.wUsedDuration[day][i1] = 0;
                                    CallHelper.Ds.structFCC.bPresentlyStopped[day][i1] = false;
                                    editor.putInt("structFCC.wUsedDuration" + day + "" + i1, CallHelper.Ds.structFCC.wUsedDuration[day][i1]);
                                    editor.putBoolean("structFCC.bPresentlyStopped" + day + "" + i1, CallHelper.Ds.structFCC.bPresentlyStopped[day][i1]);

                                }
                                for (int i1 = 0; i1 < 3; i1++) {
                                    CallHelper.Ds.structNCC.wUsedDuration[day][i1] = 0;
                                    CallHelper.Ds.structNCC.bPresentlyStopped[day][i1] = false;
                                    editor.putInt("structNCC.wUsedDuration" + day + "" + i1, CallHelper.Ds.structNCC.wUsedDuration[day][i1]);
                                    editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + i1, CallHelper.Ds.structNCC.bPresentlyStopped[day][i1]);
                                }
                            }
                        }
                    }
                }
                BootReceiver.timediff = 0;
                DeBug.ShowLog("CallDetectservice", "AppHrMin " + CallHelper.Ds.structPC.AppHrMin);
                DeBug.ShowLog("CallDetectservice", "AppHrMin " + CallHelper.Ds.lSchoolSchedule.iCurrentDate);
                editor.putBoolean("lSchoolSchedule.bSchoolHoliday", CallHelper.Ds.lSchoolSchedule.bSchoolHoliday);
                editor.commit();

            }
        }
        if (tempAppHrMin == 0)
            tempAppHrMin = CallHelper.Ds.structPC.AppHrMin;

        //load location slots
        for (int kk = 0; kk < CallHelper.Ds.structLGC.bTotalLocationSchedule; kk++) {
            CallHelper.Ds.structLGC.wEndTime[kk] = (short) settings.getInt("structLGC.wEndTime" + kk, CallHelper.Ds.structLGC.wEndTime[kk]);
            CallHelper.Ds.structLGC.wStartTime[kk] = (short) settings.getInt("structLGC.wStartTime" + kk, CallHelper.Ds.structLGC.wStartTime[kk]);
            CallHelper.Ds.structLGC.wLocFreq[kk] = (short) settings.getInt("structLGC.wLocFreq" + kk, CallHelper.Ds.structLGC.wLocFreq[kk]);
            DeBug.ShowLogD("LOC", "after restart    Start " + CallHelper.Ds.structLGC.wStartTime[kk]
                    + " End " + CallHelper.Ds.structLGC.wEndTime[kk] + " ft " + CallHelper.Ds.structLGC.wLocFreq[kk]);
        }

        //load timecontrol setting
        for (int day = 0; day < 7; day++) {
            for (int i = 0; i < 3; i++) {
                CallHelper.Ds.structNCC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structNCC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structNCC.bTimeDurationCtrl[day][i]);
                CallHelper.Ds.structNCC.wUsedDuration[day][i] = (short) settings.getInt("structNCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structNCC.wUsedDuration[day][i]);
                CallHelper.Ds.structNCC.wTotalDuration[day][i] = (short) settings.getInt("structNCC.wTotalDuration" + day + "" + i, CallHelper.Ds.structNCC.wTotalDuration[day][i]);
                CallHelper.Ds.structNCC.bPresentlyStopped[day][i] = settings.getBoolean("structNCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structNCC.bPresentlyStopped[day][i]);
                for (int j = 0; j < 24; j++) {
                    CallHelper.Ds.structNCC.lAllowedTime[day][i][j] = settings.getLong("structNCC.lAllowedTime" + day + "" + i + "" + j, 0L);
                    if (day == 4 && i == 0 && j == 16)
                        ;//	DeBug.ShowLog("calldetectservice"," r "+Long.toHexString(CallHelper.Ds.structNCC.lAllowedTime[day][i][j]));
                }

            }
            for (int i = 0; i < CallHelper.Ds.structFCC.bTotalFeatures; i++) {
                CallHelper.Ds.structFCC.wUsedDuration[day][i] = (short) settings.getInt("structFCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structFCC.wUsedDuration[day][i]);
                CallHelper.Ds.structFCC.wTotalDuration[day][i] = (short) settings.getInt("structFCC.wTotalDuration" + day + "" + i, CallHelper.Ds.structFCC.wTotalDuration[day][i]);
                CallHelper.Ds.structFCC.wEndTime[day][i] = (short) settings.getInt("structFCC.wEndTime" + day + "" + i, CallHelper.Ds.structFCC.wEndTime[day][i]);
                CallHelper.Ds.structFCC.wStartTime[day][i] = (short) settings.getInt("structFCC.wStartTime" + day + "" + i, CallHelper.Ds.structFCC.wStartTime[day][i]);
                CallHelper.Ds.structFCC.bPresentlyStopped[day][i] = settings.getBoolean("structFCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structFCC.bPresentlyStopped[day][i]);
                CallHelper.Ds.structFCC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structFCC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structFCC.bTimeDurationCtrl[day][i]);
                //	DeBug.ShowLog(TAG," r "+"structFCC.wStartTime"+day+""+i );
                for (int j = 0; j < 24; j++) {
                    CallHelper.Ds.structFCC.lAllowedTime[day][i][j] = settings.getLong("structFCC.lAllowedTime" + day + "" + i + "" + j, 0L);
                    if (day == 4 && i == 0 && j == 16) ;
                    //		DeBug.ShowLog(TAG," r "+Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[day][i][j]));
                }

            }
        }


        CallHelper.Ds.sms_numberlist.clear();
        CallHelper.Ds.sms_numberFlaglist.clear();
        int list_size = settings.getInt("Ds.sms_numberlist_size", CallHelper.Ds.sms_numberlist.size());
        for (int i = 0; i < list_size; i++) {
            CallHelper.Ds.sms_numberlist.add(settings.getString("Ds.sms_numberlist" + i, null));
            CallHelper.Ds.sms_numberFlaglist.add(settings.getInt("Ds.sms_numberFlaglist" + i, 0));
        }

        //load all defunt nos.
        String Number = null;
        String decrypt = null;
        for (int i = 0; i < CallHelper.Ds.structDNCC.bTotalNumbers; i++) {
            try {
                Number = settings.getString("structDNCC.stDefunctNumber" + i, CallHelper.Ds.structDNCC.stDefunctNumber[i]);
                decrypt = StringCryptor.decrypt(Number, StringCryptor.encryptionKey);
                CallHelper.Ds.structDNCC.stDefunctNumber[i] = decrypt;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //load pullSMS no.
        try {
            Number = settings.getString("structDNCC.stPullMsgNo", CallHelper.Ds.structDNCC.stPullMsgNo);
            decrypt = StringCryptor.decrypt(Number, StringCryptor.encryptionKey);
            CallHelper.Ds.structDNCC.stPullMsgNo = decrypt;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < CallHelper.Ds.structNCC.bTotalNumbers; i++) {
            CallHelper.Ds.structNCC.stEXT_ALWD_Numbers[i] = settings.getString("structNCC.stEXT_ALWD_Numbers" + i, CallHelper.Ds.structNCC.stEXT_ALWD_Numbers[i]);
        }

        {
            Constant.COUNTRY_CODE = settings.getString("COUNTRY_CODE", COUNTRY_CODE);
            Constant.GCM_SERVER_URL = settings.getString("GCM_SERVER_URL", GCM_SERVER_URL);
            Constant.COUNTRY_INDEX = settings.getInt("COUNTRY_INDEX", COUNTRY_INDEX);
        }

        CallHelper.Ds.structPC.bExpiryDateUpdated = settings.getBoolean("structPC.bExpiryDateUpdated", CallHelper.Ds.structPC.bExpiryDateUpdated);

        CallHelper.Ds.structPC.bFactoryControlByte = (byte) settings.getInt("structPC.bFactoryControlByte", CallHelper.Ds.structPC.bFactoryControlByte);

        CallHelper.Ds.structPC.iExpiryDate = settings.getInt("structPC.iExpiryDate", CallHelper.Ds.structPC.iExpiryDate);
        CallHelper.Ds.structPC.iExpiryMonth = settings.getInt("structPC.iExpiryMonth", CallHelper.Ds.structPC.iExpiryMonth);
        CallHelper.Ds.structPC.iExpiryYear = settings.getInt("structPC.iExpiryYear", CallHelper.Ds.structPC.iExpiryYear);
        CallHelper.Ds.structPC.ParentRegistered = settings.getInt("structPC.ParentRegistered", CallHelper.Ds.structPC.ParentRegistered);
        CallHelper.Ds.structPC.bNo_of_App_Uploaded = settings.getInt("structPC.bNo_of_App_Uploaded", CallHelper.Ds.structPC.bNo_of_App_Uploaded);

        CallHelper.Ds.structPC.ExpiryDate.set(CallHelper.Ds.structPC.iExpiryYear, CallHelper.Ds.structPC.iExpiryMonth, CallHelper.Ds.structPC.iExpiryDate);

        CallHelper.Ds.structPC.stPassword = settings.getString("structPC.stPassword", CallHelper.Ds.structPC.stPassword);
        CallHelper.Ds.structPC.stEDPassword = settings.getString("structPC.stEDPassword", CallHelper.Ds.structPC.stEDPassword);
        CallHelper.Ds.structPC.stSIMSerialno = settings.getString("structPC.stSIMSerialno", CallHelper.Ds.structPC.stSIMSerialno);
        CallHelper.Ds.structPC.bTimeExpired = settings.getBoolean("structPC.bTimeExpired", CallHelper.Ds.structPC.bTimeExpired);
        CallHelper.Ds.structPC.bRunApp = settings.getBoolean("structPC.bRunApp", CallHelper.Ds.structPC.bRunApp);
        for (int i = 0; i < CallHelper.Ds.structPC.bMaxPassWordAllowed; i++)
            CallHelper.Ds.structPC.stParentPassword[i] = settings.getString("structPC.stParentPassword" + i, CallHelper.Ds.structPC.stParentPassword[i]);

        CallHelper.Ds.structNCC.bPhoneFeatureCtrl = (byte) settings.getInt("structNCC.bPhoneFeatureCtrl", CallHelper.Ds.structNCC.bPhoneFeatureCtrl);


        CallHelper.Ds.structLGC.bfeatureValue = (byte) settings.getInt("structLGC.bfeatureValue", CallHelper.Ds.structLGC.bfeatureValue);
        CallHelper.Ds.structLGC.bNoOfRequestGPSLocn = (byte) settings.getInt("structLGC.bNoOfRequestGPSLocn", CallHelper.Ds.structLGC.bNoOfRequestGPSLocn);
        CallHelper.Ds.structLGC.bTimesCount = (byte) settings.getInt("structLGC.bTimesCount", CallHelper.Ds.structLGC.bTimesCount);
        CallHelper.Ds.structLGC.wRequestGPSLocn = (short) settings.getInt("structLGC.wRequestGPSLocn", CallHelper.Ds.structLGC.wRequestGPSLocn);
        CallHelper.Ds.structLGC.bNetwork = settings.getBoolean("structLGC.bNetwork", CallHelper.Ds.structLGC.bNetwork);
        CallHelper.Ds.structCCC.lLastSentSMSTime = settings.getLong("structCCC.lLastSentSMSTime", CallHelper.Ds.structCCC.lLastSentSMSTime);

        for (int i = 0; i < CallHelper.Ds.structLGC.bTotalLocationSchedule; i++) {
            CallHelper.Ds.structLGC.wStartGeoFenceTime[i] = (short) settings.getInt("structLGC.wStartGeoFenceTime" + i, CallHelper.Ds.structLGC.wStartGeoFenceTime[i]);
            CallHelper.Ds.structLGC.wEndGeoFenceTime[i] = (short) settings.getInt("structLGC.wEndGeoFenceTime" + i, CallHelper.Ds.structLGC.wEndGeoFenceTime[i]);
            CallHelper.Ds.structLGC.dGeoFenceLatitude[i] = Double.parseDouble(settings.getString("structLGC.dGeoFenceLatitude" + i, "" + CallHelper.Ds.structLGC.dGeoFenceLatitude[i]));
            CallHelper.Ds.structLGC.dGeoFenceLongitude[i] = Double.parseDouble(settings.getString("structLGC.dGeoFenceLongitude" + i, "" + CallHelper.Ds.structLGC.dGeoFenceLongitude[i]));
            CallHelper.Ds.structLGC.dGeoFenceRadius[i] = Double.parseDouble(settings.getString("structLGC.dGeoFenceRadius" + i, "" + CallHelper.Ds.structLGC.dGeoFenceRadius[i]));
        }

        CallHelper.Ds.structLGC.dCurrentGeoFenceLatitude = Double.parseDouble(settings.getString("structLGC.dCurrentGeoFenceLatitude", "" + CallHelper.Ds.structLGC.dCurrentGeoFenceLatitude));
        CallHelper.Ds.structLGC.dCurrentGeoFenceLongitude = Double.parseDouble(settings.getString("structLGC.dCurrentGeoFenceLongitude", "" + CallHelper.Ds.structLGC.dCurrentGeoFenceLongitude));
        CallHelper.Ds.structLGC.dCurrentGeoFenceRadius = Double.parseDouble(settings.getString("structLGC.dCurrentGeoFenceRadius", "" + CallHelper.Ds.structLGC.dCurrentGeoFenceRadius));
        CallHelper.Ds.structLGC.bGeofenceActive = settings.getInt("structLGC.bGeofenceActive", CallHelper.Ds.structLGC.bGeofenceActive);


        CallHelper.Ds.structCCC.wFeatureControlWord[0] = (long) settings.getLong("structCCC.wFeatureControlWord" + 0, CallHelper.Ds.structCCC.wFeatureControlWord[0]);

        // 0 - regular key     , 1 - attendance key
        CallHelper.Ds.structCCC.stGoogleAPIKey[0] = settings.getString("structCCC.stGoogleAPIKey0", CallHelper.Ds.structCCC.stGoogleAPIKey[0]);
        CallHelper.Ds.structCCC.stGoogleAPIKey[1] = settings.getString("structCCC.stGoogleAPIKey1", CallHelper.Ds.structCCC.stGoogleAPIKey[1]);

        for (int i = 0; i < CallHelper.Ds.structCCC.bTotalFeatures; i++) {
            CallHelper.Ds.structCCC.lExpDatesOfFeatures[0][i] = settings.getLong("structCCC.lExpDatesOfFeatures" + 0 + "" + i, CallHelper.Ds.structCCC.lExpDatesOfFeatures[0][i]);
        }
        CallHelper.Ds.structPC.bDeviceAdminEnabled = settings.getBoolean("structPC.bDeviceAdminEnabled", CallHelper.Ds.structPC.bDeviceAdminEnabled);

        DeBug.ShowToast(OneMinuteTimerService.context, "ParentRegistered " + CallHelper.Ds.structPC.ParentRegistered);
        DeBug.ShowLog("CallDetectservice", "ParentRegistered " + CallHelper.Ds.structPC.ParentRegistered + " ED " + CallHelper.Ds.structPC.iExpiryDate);

        //com.gingerbox.ipmsg.net.Packets.Connected=settings.getBoolean("Packets.Connected", false);

        //SIM change Notification
        try {
            //String Parent_no="";
            for (int i = 1; i < CallHelper.Ds.structDNCC.bTotalNumbers; i++)
                if (!CallHelper.Ds.structDNCC.stDefunctNumber[i].equals("0000000000")) {
                    //	Parent_no= CallHelper.Ds.structDNCC.stDefunctNumber[i];
                    break;
                }

            String stNewSim = null;

            //if(CallDetectService.mtelephonyManager.getSimSerialNumber()!=null)
            CallDetectService.mtelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            stNewSim = CallDetectService.mtelephonyManager.getSimSerialNumber();
            DeBug.ShowToast(OneMinuteTimerService.context, "SIM changed" + CallHelper.Ds.structPC.stSIMSerialno + "  " + stNewSim);
            DeBug.ShowLog("NSIM", "stNewSim 1 ::   " + stNewSim);

            DeBug.ShowLog("NSIM", "CallHelper.SimChangeSentToServer  " + "" + CallHelper.SimChangeSentToServer +
                    "    structPC.bSimChanged  " + CallHelper.Ds.structPC.bSimChanged);
            if (!TextUtils.isEmpty(stNewSim) && !CallHelper.Ds.structPC.stSIMSerialno.equals(stNewSim)) {
                CallHelper.Ds.structPC.bSimChanged = true;
                String stNewSimSerialNumber = CallDetectService.mtelephonyManager.getSimSerialNumber();
                CallDetectService.SIM_CHANGED = CallHelper.Ds.structPC.stPassword + " GC" + APP_CODE + " " + "SC" + " " + CallHelper.Ds.structPC.iStudId + " " + stNewSimSerialNumber;
                DeBug.ShowLog("NSIM", "stNewSimSerialNumber 1::   " + stNewSimSerialNumber);
                editor.putBoolean("structPC.bSimChanged", CallHelper.Ds.structPC.bSimChanged);
                editor.commit();
                CallDetectService.sendSMS(CallDetectService.SIM_CHANGED, 0); //0 for SIM changed
            } else {
                CallHelper.Ds.structPC.bSimChanged = false;
                editor.putBoolean("structPC.bSimChanged", CallHelper.Ds.structPC.bSimChanged);
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CallAndSMSLogsToServer(final Context context) {
        {
            new Thread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    try {
                        Calendar cal = Calendar.getInstance();
                        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                        //Upload CallLogs
                        {
                            Call_db = new DatabaseHandler_Call(context);
                            List<Struct_Contact> struct_Contacts = Call_db.getAllContacts();
                            DeBug.ShowLog("CallDB", "Reading after update..");
                            for (Struct_Contact cn : struct_Contacts) {
                                responce = 0;
                                cal.setTimeInMillis(cn.getDate());
                                String TimeStamp = formatter.format(cal.getTime()).toString();
                                String From = "";
                                String To = "";
                                int isIncoming = cn.getCallType();
                                double lat = Double.parseDouble(cn.get_slat());
                                double lon = Double.parseDouble(cn.get_slon());
                                if (cn.getCallType() == 2) {
                                    isIncoming = 0;
                                    To = cn.getPhoneNumber();
                                    From = "";
                                } else {
                                    isIncoming = 1;
                                    From = cn.getPhoneNumber();
                                    To = "";
                                }
                                //	getCellIdforregular(context,0);
                                RestApiCall mRestApiCall = new RestApiCall();
                                JSONObject json = new JSONObject();
                                try {

                                    json.put("Lat", lat);
                                    json.put("Long", lon);
                                    json.put("startDateTime", "" + TimeStamp);
                                    json.put("IsIncoming", (int) isIncoming);
                                    json.put("AppId", CallHelper.Ds.structPC.iStudId);
                                    json.put("From", From);
                                    json.put("To", To);
                                    json.put("duration", cn.getDuration());
                                    json.put("cellId", cn.getCellId());
                                    json.put("locationAreaCode", cn.getLAC());
                                    json.put("mobileCountryCode", cn.getMCC());
                                    json.put("mobileNetworkCode", cn.getMNC());

                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                responce = Integer.parseInt(mRestApiCall.CallLogs(json));
                                String log = "Id: " + cn.getID() + " ,Name: " + cn.getName() + " ,Phone: " + cn.getPhoneNumber();
                                String log2 = "Date: " + TimeStamp + " ,IsBlackList: " + cn.getDuration() + " ,Type: " + cn.getCallType();
                                DeBug.ShowLog("CallDB", log + "\n" + log2);
                                if (responce == 1)
                                    Call_db.deleteContact(cn);
                            }
                        }

                        //Upload SMSLogs
                        {
                            SMS_db = new DatabaseHandler_SMS(context);
                            List<Struct_SMS> SMSs = SMS_db.getAllSMS();
                            DeBug.ShowLog("CallDB", "Reading after update..");
                            for (Struct_SMS cn : SMSs) {
                                responce = 0;
                                cal.setTimeInMillis(cn.getDate());
                                String TimeStamp = formatter.format(cal.getTime()).toString();
                                String From = "";
                                String To = "";
                                int isIncoming = 0;
                                double lat = Double.parseDouble(cn.get_slat());
                                double lon = Double.parseDouble(cn.get_slon());
                                if (cn.getSMSType() == 0) {
                                    To = cn.getPhoneNumber();
                                } else {
                                    From = cn.getPhoneNumber();
                                    isIncoming = 1;
                                }
                                RestApiCall mRestApiCall = new RestApiCall();
                                JSONObject json = new JSONObject();
                                try {
                                    json.put("Lat", lat);
                                    json.put("Long", lon);
                                    json.put("startDateTime", "" + TimeStamp);
                                    json.put("IsIncoming", isIncoming);
                                    json.put("AppId", CallHelper.Ds.structPC.iStudId);
                                    json.put("From", From);
                                    json.put("To", To);
                                    json.put("MsgText", cn.getSMSBody());
                                    json.put("cellId", cn.getCellId());
                                    json.put("locationAreaCode", cn.getLAC());
                                    json.put("mobileCountryCode", cn.getMCC());
                                    json.put("mobileNetworkCode", cn.getMNC());
                                    json.put("LogDateTime", cn.getLogDateTime());
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                responce = Integer.parseInt(mRestApiCall.SMSLogs(json));
                                String log = "Id: " + cn.getID() + " ,Name: " + cn.getName() + " ,Phone: " + cn.getPhoneNumber();
                                String log2 = "Date: " + cn.getDate() + " ,IsBlackList: " + cn.getSMSBody() + " ,Type: " + cn.getSMSType()
                                        + " ,lat: " + cn.get_slat() + " ,lat: " + cn.get_slon();
                                DeBug.ShowLog("CallDB", log + "\n" + log2);
                                if (responce == 1)
                                    SMS_db.deleteSMS(cn);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }


    private void sendALSms(Context context) {

        int month = CallHelper.Ds.lSchoolSchedule.iCurrentMonth + 1;
        int date = CallHelper.Ds.lSchoolSchedule.iCurrentDate;

        float hourMinute = 10000 / month;//(this will give a number between 10000 and 834).
        hourMinute = hourMinute % date; //(this will a number between 0 and 31)
        if (hourMinute > 19)
            hourMinute = hourMinute / 2;

        hourMinute = hourMinute * 100;
        int ihourMinute = (int) hourMinute;

        if (ihourMinute == tempAppHrMin) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            DeBug.ShowLog("EXP", "OneMinuteTimerReceiver Total Time 1111111 " + Calendar.HOUR_OF_DAY + "    " + Calendar.MINUTE);
            {
                try {
                    DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                    String TimeStamp1 = CallHelper.GetTimeWithDate();
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
        }

    }

    private void nextDayStarted(Context context) {

        tempAppHrMin = 0;

        CallHelper.Ds.lSchoolSchedule.iCurrentSchoolSchedule = -1;
        CallHelper.Ds.lSchoolSchedule.bSchoolHoliday = false;

        CallHelper.Ds.structPC.CurrentDate.roll(Calendar.DATE, true);
        if (CallHelper.Ds.structPC.CurrentDate.get(Calendar.DATE) == 1) {
            CallHelper.Ds.structPC.CurrentDate.roll(Calendar.MONTH, true);
            if (CallHelper.Ds.structPC.CurrentDate.get(Calendar.MONTH) == 0)
                CallHelper.Ds.structPC.CurrentDate.roll(Calendar.YEAR, true);
        }

        CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.DATE, true);
        if (CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DATE) == 1) {
            CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.MONTH, true);
            if (CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH) == 0)
                CallHelper.Ds.lSchoolSchedule.CurrentDate.roll(Calendar.YEAR, true);
        }

        CallHelper.Ds.structPC.bWeekDay++;
        if (CallHelper.Ds.structPC.bWeekDay > 6)
            CallHelper.Ds.structPC.bWeekDay = 0;

        CallHelper.Ds.structPC.iCurrentDate = CallHelper.Ds.structPC.CurrentDate.get(Calendar.DATE);
        CallHelper.Ds.structPC.iCurrentMonth = CallHelper.Ds.structPC.CurrentDate.get(Calendar.MONTH);
        CallHelper.Ds.structPC.iCurrentYear = CallHelper.Ds.structPC.CurrentDate.get(Calendar.YEAR);


        editor.putInt("structPC.iCurrentDate", CallHelper.Ds.structPC.iCurrentDate);
        editor.putInt("structPC.iCurrentMonth", CallHelper.Ds.structPC.iCurrentMonth);
        editor.putInt("structPC.iCurrentYear", CallHelper.Ds.structPC.iCurrentYear);
        editor.putInt("structPC.bWeekDay", CallHelper.Ds.structPC.bWeekDay);
        editor.putBoolean("lSchoolSchedule.bSchoolHoliday", CallHelper.Ds.lSchoolSchedule.bSchoolHoliday);

        CallHelper.Ds.lSchoolSchedule.iCurrentDate = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.DAY_OF_MONTH);
        CallHelper.Ds.lSchoolSchedule.iCurrentMonth = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.MONTH);
        CallHelper.Ds.lSchoolSchedule.iCurrentYear = CallHelper.Ds.lSchoolSchedule.CurrentDate.get(Calendar.YEAR);

        editor.putInt("lSchoolSchedule.iCurrentDate", CallHelper.Ds.lSchoolSchedule.iCurrentDate);
        editor.putInt("lSchoolSchedule.iCurrentMonth", CallHelper.Ds.lSchoolSchedule.iCurrentMonth);
        editor.putInt("lSchoolSchedule.iCurrentYear", CallHelper.Ds.lSchoolSchedule.iCurrentYear);


        DeBug.ShowLog("features", "AppHrMin >= 2400  AndroidCalls.Day" + CallHelper.Ds.structPC.bWeekDay);

        if (CallHelper.Ds.structPC.bWeekDay == 0) {
            for (int day = 0; day < 7; day++) {
                for (int i = 0; i < CallHelper.Ds.structFCC.bTotalFeatures; i++) {
                    CallHelper.Ds.structFCC.wUsedDuration[day][i] = 0;
                    CallHelper.Ds.structFCC.bPresentlyStopped[day][i] = false;
                    editor.putInt("structFCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structFCC.wUsedDuration[day][i]);
                    editor.putBoolean("structFCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structFCC.bPresentlyStopped[day][i]);

                }
                for (int i = 0; i < 3; i++) {
                    CallHelper.Ds.structNCC.wUsedDuration[day][i] = 0;
                    CallHelper.Ds.structNCC.bPresentlyStopped[day][i] = false;
                    editor.putInt("structNCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structNCC.wUsedDuration[day][i]);
                    editor.putBoolean("structNCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structNCC.bPresentlyStopped[day][i]);
                }
            }
        }

        context.startService(new Intent(context, SyncIntentService.class).putExtra("upload_data", true));

    }

    private void onServiceStart(final Context context) {

        ServiceStartCounter = 0;
        editor.putInt("ServiceStartCounter", ServiceStartCounter);
        editor.commit();

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    RestApiCall mRestApiCall3 = new RestApiCall();
                    JSONObject json3 = new JSONObject();
                    try {
                        json3.put("StuMob", "1");
                        json3.put("countryId", 1);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    String p = mRestApiCall3.RT(json3);

                    if (p.equals("-1")) {
                        CallDetectService.TIME_CHANGED = CallHelper.Ds.structPC.stPassword + " GC" + APP_CODE + " " + "RT" + " " + COUNTRY_INDEX + " " + CallHelper.Ds.structPC.stPhoneNo;

                        {
                            try {
                                DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                                String TimeStamp = CallHelper.GetTimeWithDate();
                                if (SMS_db.recordExist(TimeStamp, 5) == 0) {
                                    SMS_db.addSMS(new Struct_Send_SMSInfo(5, "", "", "", 0.0, 0.0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));

                                    Intent msgIntent = new Intent(context, UploadService.class);
                                    Bundle b = new Bundle();
                                    msgIntent.putExtra("UploadStatus", 0);
                                    msgIntent.putExtras(b);
                                    context.startService(msgIntent);
                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    } else {
                        CallHelper.decodeMessage(OneMinuteTimerService.context, p);
                    }
                } catch (Exception e) {
                    System.out.println("" + e.getMessage());
                }
            }

        }).start();

    }

    private void onMobileRestarts(final Context context) {
        try {

            String stNewSim = null;
            TelephonyManager mtelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //if(CallDetectService.mtelephonyManager.getSimSerialNumber()!=null)
            stNewSim = mtelephonyManager.getSimSerialNumber();
            DeBug.ShowLog("NSIM", "stNewSim 2 ::   " + stNewSim);

            String stNew = settings.getString("stNewSimSerialNumber", "");

            DeBug.ShowLog("NSIM", "CallHelper.SimChangeSentToServer  " + "" + CallHelper.SimChangeSentToServer +
                    "    structPC.bSimChanged  " + CallHelper.Ds.structPC.bSimChanged);

            if (stNewSim != null && !stNewSim.equals("") && !stNew.equals(stNewSim))
                if (!CallHelper.Ds.structPC.stSIMSerialno.equals(stNewSim)) {
                    CallHelper.Ds.structPC.bSimChanged = true;

                    String stNewSimSerialNumber = mtelephonyManager.getSimSerialNumber();
                    editor.putString("stNewSimSerialNumber", stNewSimSerialNumber);
                    CallDetectService.SIM_CHANGED = CallHelper.Ds.structPC.stPassword + " GC" + APP_CODE + " " + "SC" + " " + CallHelper.Ds.structPC.iStudId + " " + stNewSimSerialNumber;
                    DeBug.ShowLog("NSIM", "stNewSimSerialNumber 2::   " + stNewSimSerialNumber);

                    CallHelper.SimChangeSentToServer = false;
                    editor.putBoolean("CallHelper.SimChangeSentToServer", CallHelper.SimChangeSentToServer);
                    editor.putBoolean("structPC.bSimChanged", CallHelper.Ds.structPC.bSimChanged);
                    editor.commit();
                    CallDetectService.sendSMS(CallDetectService.SIM_CHANGED, 0); //0 for SIM changed
                } else if (!stNew.equals(CallHelper.Ds.structPC.stSIMSerialno)) {
                    String stNewSimSerialNumber = mtelephonyManager.getSimSerialNumber();
                    editor.putString("stNewSimSerialNumber", stNewSimSerialNumber);
                    CallHelper.SimChangeSentToServer = false;
                    editor.putBoolean("CallHelper.SimChangeSentToServer", CallHelper.SimChangeSentToServer);
                    editor.commit();
                } else {
                    CallHelper.Ds.structPC.bSimChanged = false;
                    editor.putBoolean("structPC.bSimChanged", CallHelper.Ds.structPC.bSimChanged);
                    editor.commit();
                }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (CallHelper.Ds.structPC.bSimChanged)        // Mobile Switch ON/OFF          #GS
        {
            CallHelper.Ds.structPC.bSimChanged = false;
            editor.putBoolean("structPC.bSimChanged", CallHelper.Ds.structPC.bSimChanged);
            editor.commit();
            String stNewSim = "";
            CallDetectService.mtelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            //if(CallDetectService.mtelephonyManager.getSimSerialNumber()!=null)
            stNewSim = CallDetectService.mtelephonyManager.getSimSerialNumber();
            DeBug.ShowToast(context, "SIM changed" + CallHelper.Ds.structPC.stSIMSerialno + "  " + stNewSim);

            if (CallHelper.Ds.structPC.stSIMSerialno.equals(stNewSim)) {
                CallHelper.Ds.structPC.bSimChanged = false;
            }


            try {
                if (CallHelper.Ds.structLGC.bTimesCount > 1) {
                    if (CallHelper.Ds.structACC.listGroupIds.isEmpty()) {
                        DeBug.ShowLogD("EXP", " CallHelper.Ds.structACC.listGroupIds    " + "   getting Settings again");
                        //	CallHelper.Ds.structFCC.packageList.readFromSharedPreferences(settings);
                        ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);
                        ArrayList<Integer> list = mApplicationInfoDB.getGroups();

                        int size = settings.getInt("Ds.structACC.listGroupIds.size", 0);
                        CallHelper.Ds.structACC.listGroupIds = new ArrayList<String>();//read From shearedpref
                        for (int i = 0; i < size; i++) {
                            CallHelper.Ds.structACC.listGroupIds.add(settings.getString("Ds.structACC.listGroupIds" + i, ""));
                        }

                        CallHelper.Ds.inItApplicationStruct(context,
                                CallHelper.Ds.structACC.listGroupIds.size(),
                                CallHelper.Ds.structACC.listGroupIds);

                        //load timecontrol setting
                        for (int day = 0; day < 7; day++) {

                            for (int i = 0; i < CallHelper.Ds.structACC.bTotalFeatures; i++) {
                                CallHelper.Ds.structACC.wUsedDuration[day][i] = (short) settings.getInt("structACC.wUsedDuration" + day + "" + i, CallHelper.Ds.structACC.wUsedDuration[day][i]);
                                CallHelper.Ds.structACC.wTotalDuration[day][i] = (short) settings.getInt("structACC.wTotalDuration" + day + "" + i, CallHelper.Ds.structACC.wTotalDuration[day][i]);
                                CallHelper.Ds.structACC.wEndTime[day][i] = (short) settings.getInt("structACC.wEndTime" + day + "" + i, CallHelper.Ds.structACC.wEndTime[day][i]);
                                CallHelper.Ds.structACC.wStartTime[day][i] = (short) settings.getInt("structACC.wStartTime" + day + "" + i, CallHelper.Ds.structACC.wStartTime[day][i]);
                                CallHelper.Ds.structACC.bPresentlyStopped[day][i] = settings.getBoolean("structACC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structACC.bPresentlyStopped[day][i]);
                                CallHelper.Ds.structACC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structACC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structACC.bTimeDurationCtrl[day][i]);
                                //	DeBug.ShowLog(TAG," r "+"structFCC.wStartTime"+day+""+i );
                                for (int j = 0; j < 24; j++) {
                                    CallHelper.Ds.structACC.lAllowedTime[day][i][j] = settings.getLong("structACC.lAllowedTime" + day + "" + i + "" + j, 0L);
                                    if (day == 4 && i == 0 && j == 16) ;
                                    //		DeBug.ShowLog(TAG," r "+Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[day][i][j]));
                                }

                            }

                        }
                    }
                }
            } catch (Exception e1) {
                DeBug.ShowLogD("EXP", " CallDetectService killed by OS :(" + e1.getMessage());
                e1.printStackTrace();
            }
        }
        {
            DeBug.ShowLog("NSIM", "CallHelper.SimChangeSentToServer  " + "" + CallHelper.SimChangeSentToServer +
                    "    structPC.bSimChanged  " + CallHelper.Ds.structPC.bSimChanged);
            DeBug.ShowLog("NSIM", "stMobile_SwitchOFF_dateTime    " + CallHelper.Ds.structPC.stMobile_SwitchOFF_dateTime);

            CallHelper.SimChangeSentToServer = settings.getBoolean("CallHelper.SimChangeSentToServer", false);

            if (CallHelper.Ds.structPC.iStudId != "")
                if (!CallHelper.SimChangeSentToServer) {

                    BootReceiver.bStartedAfterBoot = false;
                    CallHelper.SimChangeSentToServer = true;
                    editor.putBoolean("CallHelper.SimChangeSentToServer", CallHelper.SimChangeSentToServer);
                    editor.commit();

                    String stNewSim = "";
                    stNewSim = CallDetectService.mtelephonyManager.getSimSerialNumber();

                    {

                        try {
                            DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                            String TimeStamp1 = CallHelper.GetTimeWithDate();
                            if (SMS_db.recordExist(TimeStamp1, 6) == 0) {
                                SMS_db.addSMS(new Struct_Send_SMSInfo(6, "" + TimeStamp1, "", "" + stNewSim + "$" + CallHelper.Ds.structPC.stMobile_SwitchOFF_dateTime, 0, 0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                                Intent msgIntent = new Intent(context, UploadService.class);
                                Bundle b = new Bundle();
                                msgIntent.putExtra("UploadStatus", 0);
                                msgIntent.putExtras(b);
                                context.startService(msgIntent);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                } else if (BootReceiver.bStartedAfterBoot) {
                    {

                        try {
                            BootReceiver.bStartedAfterBoot = false;
                            DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                            String TimeStamp1 = CallHelper.GetTimeWithDate();
                            String stNewSim = "";
                            stNewSim = CallDetectService.mtelephonyManager.getSimSerialNumber();
                            if (SMS_db.recordExist(TimeStamp1, 6) == 0) {
                                SMS_db.addSMS(new Struct_Send_SMSInfo(6, "" + TimeStamp1, "", "" + stNewSim + "$" + CallHelper.Ds.structPC.stMobile_SwitchOFF_dateTime, 0, 0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                                Intent msgIntent = new Intent(context, UploadService.class);
                                Bundle b = new Bundle();
                                msgIntent.putExtra("UploadStatus", 0);
                                msgIntent.putExtras(b);
                                context.startService(msgIntent);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }

                }
        }
    }

    private boolean getLocationSettings() {
        int Hr = CallHelper.Ds.structPC.AppHrMin / 100;
        int Min = CallHelper.Ds.structPC.AppHrMin % 100;
        iWeekDay = CallHelper.Ds.structPC.bWeekDay;
        boolean canTakeLocation = false;
        int mintue = (Hr * 60) + Min;
        if (CallHelper.runTime == null) {
            String tempTime = settings.getString("CallHelper.runTime", null);
            if (tempTime != null && !tempTime.isEmpty())
                CallHelper.runTime = CallHelper.decodeGp8(tempTime);
            else
                CallHelper.runTime = CallHelper.decodeGp8("GP8 TS0000 TE2359 TF96");
        }
        if (mintue < 1440 && CallHelper.runTime[mintue] && (CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.ilOCATE_DEVICE][Hr] & MIN_LUT[Min]) != 0) {
            canTakeLocation = true;
        }
        return canTakeLocation;

    }

    private boolean getGeoFence(Context context) {

        boolean isCurrentlyBlockedInTime = false;
        boolean canDetectGeofence = false;
        int iWeekDay = CallHelper.Ds.structPC.bWeekDay;

        if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGEO_FENCE]) {
            canDetectGeofence = false;
        } else {
            DeBug.ShowLog("features", "Day " + iWeekDay + " AppHrMin " + CallHelper.Ds.structPC.AppHrMin);

            if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGEO_FENCE] == 0) {
                CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGEO_FENCE] = false;
                CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iGEO_FENCE] = 3;
            } else {
                int Hr = CallHelper.Ds.structPC.AppHrMin / 100;
                int Min = CallHelper.Ds.structPC.AppHrMin % 100;
                isCurrentlyBlockedInTime = false;
                DeBug.ShowLogD("features", "WF lAllowedTime" + Long.toHexString(CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iGEO_FENCE][Hr]));
                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGEO_FENCE] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {

                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iGEO_FENCE][Hr] & MIN_LUT[Min]) != 0) {
                        isCurrentlyBlockedInTime = true;

                        canDetectGeofence = true;
                    }
                    //	DeBug.ShowLog(Tag1,""+ packageName +"   HR "+Hr +" Min "+Min);

                }

                if (!isCurrentlyBlockedInTime)
                    if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iGEO_FENCE] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                        if (!CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGEO_FENCE]) {
                            CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGEO_FENCE]++;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iGEO_FENCE] = 1;
                            DeBug.ShowLog("features", "WF RESTRICTED_IN_DURATION " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGEO_FENCE] + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGEO_FENCE]);
                            canDetectGeofence = true;
                        }
                        if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGEO_FENCE] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGEO_FENCE]) {
                            // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                            //1. when the activity is being stopped then it saves the current duration,
                            //2. if the duration has expired then stops the activity and save the current duration.
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iGEO_FENCE] = true;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iGEO_FENCE] = 3;
                            DeBug.ShowLog("features", "WF RESTRICTED_IN_DURATION . Used  Total.   " + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGEO_FENCE] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGEO_FENCE]);
                            DeBug.ShowToast(context, "WF RESTRICTED_IN_DURATION . WF . \n" + CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iGEO_FENCE] / 60 + " " + CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iGEO_FENCE]);
                        }
                    }
            }
        }
        return canDetectGeofence;
    }

    private boolean getLocationAlert() {
        boolean canDetectGeofence = false;
        int iWeekDay = CallHelper.Ds.structPC.bWeekDay;
        int Hr = CallHelper.Ds.structPC.AppHrMin / 100;
        int Min = CallHelper.Ds.structPC.AppHrMin % 100;
        if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iSET_LOCATION_ALERT] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {
            if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iSET_LOCATION_ALERT][Hr] & MIN_LUT[Min]) != 0) {
                canDetectGeofence = true;
            }
        }
        return canDetectGeofence;
    }

    private void retrySendDataToServer(final Context context) {
        DeBug.ShowLog("COUNTER", "uploading " + CallHelper.locationIsUploading + " Struct_Send_SMSInfo isEmpty() "
                + Struct_Send_SMSInfo.oListSMSInfo.isEmpty() + " SMSsent " + Struct_Send_SMSInfo.SMSsent);

        if (!CallHelper.locationIsUploading)
            if (Struct_Send_SMSInfo.SMSsent != 1) {
                Intent msgIntent = new Intent(context, UploadService.class);
                Bundle b = new Bundle();
                msgIntent.putExtra("UploadStatus", 1);
                msgIntent.putExtras(b);
                context.startService(msgIntent);
            }


        Intent intent = new Intent(context, UpLoadDocsIntentService.class);
        Bundle sendData0 = new Bundle();
        sendData0.putInt("FeatereValue", -1);
        intent.putExtras(sendData0);
        context.startService(intent);

        context.startService(new Intent(context, UploadFileToserverIntentService.class));

    }

    private void sendVersionInfo(final Context context) {

        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        CallHelper.Ds.structCCC.stAppVer = settings.getString("structCCC.stAppVer", "");
        DeBug.ShowLog("Version", "1  " + CallHelper.Ds.structCCC.stAppVer);

        if (!CallHelper.Ds.structCCC.stAppVer.contains(versionName)) {
            editor.putString("structCCC.stAppVer", "" + versionName);
            editor.commit();

            try {
                DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                String TimeStamp = CallHelper.GetTimeWithDate();
                if (SMS_db.recordExist(TimeStamp, 10) == 0) {
                    SMS_db.addSMS(new Struct_Send_SMSInfo(10, "" + TimeStamp, "", "" + "" + versionName
                            , 0, 0, 0, System.currentTimeMillis(), "0", "0", "0", "0"));


                    Intent msgIntent = new Intent(context, UploadService.class);
                    Bundle b = new Bundle();
                    msgIntent.putExtra("UploadStatus", 0);
                    msgIntent.putExtras(b);
                    context.startService(msgIntent);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            DeBug.ShowLog("Version", "2  " + versionName);
        }
        versionName = settings.getString("structCCC.stAppVer", "");
        DeBug.ShowLog("Version", "3  " + versionName);

    }

    private void retriveAppGroupInfo(Context context) {

        try {
            {
                if (CallHelper.Ds.structACC.listGroupIds.isEmpty()) {
                    DeBug.ShowLogD("EXP", " CallHelper.Ds.structACC.listGroupIds empty   " + "   getting Settings again");
                    //	CallHelper.Ds.structFCC.packageList.readFromSharedPreferences(settings);
                    //	ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);
                    //ArrayList<Integer> list = mApplicationInfoDB.getGroups();

                    int size = settings.getInt("Ds.structACC.listGroupIds.size", 0);
                    CallHelper.Ds.structACC.listGroupIds = new ArrayList<String>();//read From shearedpref
                    for (int i = 0; i < size; i++) {
                        CallHelper.Ds.structACC.listGroupIds.add(settings.getString("Ds.structACC.listGroupIds" + i, ""));
                    }

                    CallHelper.Ds.inItApplicationStruct(context,
                            CallHelper.Ds.structACC.listGroupIds.size(),
                            CallHelper.Ds.structACC.listGroupIds);

                    //load timecontrol setting
                    for (int day = 0; day < 7; day++) {
                        for (int i = 0; i < CallHelper.Ds.structACC.bTotalFeatures; i++) {
                            CallHelper.Ds.structACC.wUsedDuration[day][i] = (short) settings.getInt("structACC.wUsedDuration" + day + "" + i, CallHelper.Ds.structACC.wUsedDuration[day][i]);
                            CallHelper.Ds.structACC.wTotalDuration[day][i] = (short) settings.getInt("structACC.wTotalDuration" + day + "" + i, CallHelper.Ds.structACC.wTotalDuration[day][i]);
                            CallHelper.Ds.structACC.wEndTime[day][i] = (short) settings.getInt("structACC.wEndTime" + day + "" + i, CallHelper.Ds.structACC.wEndTime[day][i]);
                            CallHelper.Ds.structACC.wStartTime[day][i] = (short) settings.getInt("structACC.wStartTime" + day + "" + i, CallHelper.Ds.structACC.wStartTime[day][i]);
                            CallHelper.Ds.structACC.bPresentlyStopped[day][i] = settings.getBoolean("structACC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structACC.bPresentlyStopped[day][i]);
                            CallHelper.Ds.structACC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structACC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structACC.bTimeDurationCtrl[day][i]);
                            for (int j = 0; j < 24; j++) {
                                CallHelper.Ds.structACC.lAllowedTime[day][i][j] = settings.getLong("structACC.lAllowedTime" + day + "" + i + "" + j, 0L);
                            }

                        }

                    }
                }
            }
        } catch (Exception e1) {
            DeBug.ShowLogD("EXP", " CallDetectService killed by OS :(" + e1.getMessage());
            e1.printStackTrace();
        }
    }

    private void getTimeSettingsFromPreferance(Context context) {
        for (int day = 0; day < 7; day++) {
            for (int i = 0; i < 3; i++) {
                CallHelper.Ds.structNCC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structNCC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structNCC.bTimeDurationCtrl[day][i]);
                CallHelper.Ds.structNCC.wUsedDuration[day][i] = (short) settings.getInt("structNCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structNCC.wUsedDuration[day][i]);
                CallHelper.Ds.structNCC.wTotalDuration[day][i] = (short) settings.getInt("structNCC.wTotalDuration" + day + "" + i, CallHelper.Ds.structNCC.wTotalDuration[day][i]);
                CallHelper.Ds.structNCC.bPresentlyStopped[day][i] = settings.getBoolean("structNCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structNCC.bPresentlyStopped[day][i]);
                for (int j = 0; j < 24; j++) {
                    CallHelper.Ds.structNCC.lAllowedTime[day][i][j] = settings.getLong("structNCC.lAllowedTime" + day + "" + i + "" + j, 0L);
                }
            }
            for (int i = 0; i < CallHelper.Ds.structFCC.bTotalFeatures; i++) {
                CallHelper.Ds.structFCC.wUsedDuration[day][i] = (short) settings.getInt("structFCC.wUsedDuration" + day + "" + i, CallHelper.Ds.structFCC.wUsedDuration[day][i]);
                CallHelper.Ds.structFCC.wTotalDuration[day][i] = (short) settings.getInt("structFCC.wTotalDuration" + day + "" + i, CallHelper.Ds.structFCC.wTotalDuration[day][i]);
                CallHelper.Ds.structFCC.wEndTime[day][i] = (short) settings.getInt("structFCC.wEndTime" + day + "" + i, CallHelper.Ds.structFCC.wEndTime[day][i]);
                CallHelper.Ds.structFCC.wStartTime[day][i] = (short) settings.getInt("structFCC.wStartTime" + day + "" + i, CallHelper.Ds.structFCC.wStartTime[day][i]);
                CallHelper.Ds.structFCC.bPresentlyStopped[day][i] = settings.getBoolean("structFCC.bPresentlyStopped" + day + "" + i, CallHelper.Ds.structFCC.bPresentlyStopped[day][i]);
                CallHelper.Ds.structFCC.bTimeDurationCtrl[day][i] = (byte) settings.getInt("structFCC.bTimeDurationCtrl" + day + "" + i, CallHelper.Ds.structFCC.bTimeDurationCtrl[day][i]);
                for (int j = 0; j < 24; j++) {
                    CallHelper.Ds.structFCC.lAllowedTime[day][i][j] = settings.getLong("structFCC.lAllowedTime" + day + "" + i + "" + j, 0L);
                }
            }
        }
    }

    private void saveDataToShearedprefarenceWhileCallisGoingOn(Context context) {

        if (CallHelper.Ds.structNCC.bOutgoingCallOn == 1) //means the call is on
        {
            short temp = (short) settings.getInt("structNCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + 1, CallHelper.Ds.structNCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][1]);
            temp = (short) (temp + 60);
            editor.putInt("structNCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + 1, temp);
        } else if (CallHelper.Ds.structNCC.bOutgoingCallOn == 2)  //means the call went off.
        {
            CallHelper.Ds.structNCC.bOutgoingCallOn = 0;
            editor.putInt("structNCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + 1, CallHelper.Ds.structNCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][1]);
            DeBug.ShowLog("Outcoming", "OneMInuteTimerOutgoingCall2 " + " wUsedDuration " + CallHelper.Ds.structNCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][1]);
        }
        if (CallHelper.Ds.structNCC.bIncomingCallOn == 1) //means the call is on
        {
            short temp = (short) settings.getInt("structNCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + 0, CallHelper.Ds.structNCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][0]);
            temp = (short) (temp + 60);
            editor.putInt("structNCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + 0, temp);
            DeBug.ShowLog("Outcoming", "OneMInuteTimer IncomingCall1 " + " wUsedDuration " + temp);

        } else if (CallHelper.Ds.structNCC.bIncomingCallOn == 2)  //means the call went off.
        {
            CallHelper.Ds.structNCC.bIncomingCallOn = 0;
            editor.putInt("structNCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + 0, CallHelper.Ds.structNCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][0]);
            DeBug.ShowLog("features", "OneMInuteTimer IncomingCall2 " + " wUsedDuration " + CallHelper.Ds.structNCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][0]);

        }

        for (int i = 0; i < CallHelper.Ds.structFCC.bTotalFeatures; i++) {
            if (CallHelper.Ds.structFCC.bPresentlyOn[i] > 0) {
                //save the value of duration and stopped into the SharedPre
                editor.putInt("structFCC.wUsedDuration" + CallHelper.Ds.structPC.bWeekDay + "" + i, CallHelper.Ds.structFCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][i]);
                editor.putBoolean("structFCC.bPresentlyStopped" + CallHelper.Ds.structPC.bWeekDay + "" + i, CallHelper.Ds.structFCC.bPresentlyStopped[CallHelper.Ds.structPC.bWeekDay][i]);
                CallHelper.Ds.structFCC.bPresentlyOn[i] = 0;
                DeBug.ShowLog("features", "OneMInuteTimer feature=" + i + " wUsedDuration " + CallHelper.Ds.structFCC.wUsedDuration[CallHelper.Ds.structPC.bWeekDay][i] + " " + CallHelper.Ds.structFCC.bPresentlyStopped[CallHelper.Ds.structPC.bWeekDay][i]);
            }
        }

    }

    private void saveAndsendAppLog(Context context) {
        LogInfoDatabaseHandler mLogInfoDatabaseHandler = LogInfoDatabaseHandler.getInstance(context);
        List<LogInfoStruct> listLogInfoStruct = mLogInfoDatabaseHandler.getDataByFeatureIndex("" + 0);

        for (int i = 0; i < listLogInfoStruct.size(); i++) {
            DeBug.ShowLog("EXP", "" + listLogInfoStruct.get(i).getSubfeature_index() + " " + listLogInfoStruct.get(i).getDuration());
        }

        if (AppBlock.usedTimeCounter.size() > 0) {

            ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);
            Set<Integer> en = AppBlock.usedTimeCounter.keySet();

            String todasDate = CallHelper.GetTimeWithDate().substring(0, 10);

            for (int i : en) {
                LogInfoStruct mLogInfoStruct = mLogInfoDatabaseHandler.getDataByFeatureIndexAndSubFeatureIndexAndDate("" + 0, "" + i, todasDate);

                if (mLogInfoStruct != null) {
                    if (AppBlock.usedTimeCounter.get(i) > 0) {
                        int duration = Integer.parseInt(mLogInfoStruct.getDuration());
                        int totalTimeUsed = duration + AppBlock.usedTimeCounter.get(i);

                        mLogInfoStruct.setDuration("" + totalTimeUsed);
                        mLogInfoStruct.setLastused_time("" + AppBlock.lastUsedTime.get(i));

                        String appName = mApplicationInfoDB.getAppName("" + i);
                        mLogInfoStruct.setAppName(appName);

                        mLogInfoStruct.setStatus("" + 2);
                        String logdate_time = CallHelper.GetTimeWithDate();

                        mLogInfoStruct.setLogdate_time(logdate_time);
                        mLogInfoDatabaseHandler.updateLogInfo(mLogInfoStruct);

                        AppBlock.usedTimeCounter.put(i, 0);
                    }

                } else {
                    mLogInfoStruct = new LogInfoStruct("0", "" + i, "" + CallHelper.GetTimeWithDate(), "" + AppBlock.lastUsedTime.get(i),
                            "" + AppBlock.usedTimeCounter.get(i), "" + CallHelper.GetTimeWithDate(), "" + 0,
                            "" + 0);
                    String appName = mApplicationInfoDB.getAppName("" + i);
                    mLogInfoStruct.setAppName(appName);
                    mLogInfoStruct.setStatus("" + 2);
                    mLogInfoDatabaseHandler.addLogInfo(mLogInfoStruct);

                    AppBlock.usedTimeCounter.put(i, 0);

                }

                DeBug.ShowLog("EXP", " : " + AppBlock.usedTimeCounter.get(i));
            }
        }

        if (CallHelper.Ds.structPC.AppHrMin % 1 == 0) {
            Intent msgIntent = new Intent(context, UplopadLogsToServer.class);
            Bundle b = new Bundle();
            msgIntent.putExtra("UploadStatus", 0);
            msgIntent.putExtras(b);
            context.startService(msgIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OneMinuteTimerService.servicesrestart = true;
        startService(new Intent(OneMinuteTimerService.this, OneMinuteTimerService.class));
    }
}