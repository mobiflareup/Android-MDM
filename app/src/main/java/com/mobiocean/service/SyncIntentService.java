package com.mobiocean.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.mobiocean.rootfeatures.DisableQuickSettingsService;
import com.mobiocean.rootfeatures.GpsWifiIntentService;
import com.mobiocean.rootfeatures.RootConstants;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.AanwlaService;
import com.mobiocean.util.CalendarSyncServer;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.ContactStruct;
import com.mobiocean.util.DataStructure;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.InsertIntoContactSync;
import com.mobiocean.util.LKONagarNigam;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.Sms;
import com.mobiocean.util.UpdateFromServer;
import com.mobiocean.util.eFeatureControl;

import org.json.JSONException;
import org.json.JSONObject;
import org.sn.beans.AppGroupBean;
import org.sn.beans.ContactListBean;
import org.sn.beans.EnabledExtraFeatureBean;
import org.sn.beans.ProfileInfoBean;
import org.sn.beans.SensorListBean;
import org.sn.beans.VpnStatusBean;
import org.sn.beans.WebListBean;
import org.sn.database.AppGroupTable;
import org.sn.database.ContactListTable;
import org.sn.database.MobiProfileDetailTable;
import org.sn.database.MobiProfileInfoTable;
import org.sn.database.SensorListTable;
import org.sn.database.WebListTable;
import org.sn.util.Constants;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SyncIntentService extends IntentService {


    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;
    public static boolean serviceStarted = false;

    public SyncIntentService() {
        super("SyncIntentService");
    }

    @Override
    public void onCreate() {
        serviceStarted = true;
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        serviceStarted = false;
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();

            RestApiCall mRestApiCall = new RestApiCall();
            if (intent.getBooleanExtra("upload_data", false)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
                ArrayList<InsertIntoContactSync> listInsertIntoContactSync = CallHelper.getContactList(this);
                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SYNC_ADRESS) == eFeatureControl.SYNC_ADRESS) {
                    DeBug.ShowLog("Narayanan", "Uploading Contacts");
                    if (listInsertIntoContactSync.size() > 0) {
                        {
                            ContactStruct contactStruct = new ContactStruct();
                            contactStruct.setAppId(CallHelper.Ds.structPC.iStudId);
                            contactStruct.setLogDateTime(SMSTimeStamp);
                            contactStruct.setList(listInsertIntoContactSync);

                            String result = mRestApiCall.postContacts((MobiApplication) getApplication(), CallHelper.Ds.structPC.iStudId, contactStruct);
                            DeBug.ShowLog("Narayanan", "Uploading Contacts result:" + result);
                        }

                    }
                    DeBug.ShowLog("Narayanan", "Uploading Contacts Success");
                }
                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SYNC_CALENDER) == eFeatureControl.SYNC_CALENDER) {
                    if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SYNC_ADRESS) == eFeatureControl.SYNC_ADRESS) {
                        DeBug.ShowLog("Narayanan", "Uploading SMS");
                        {
                            List<Sms.SMS> smsList = CallHelper.getAllSms(getApplicationContext());
                            if (smsList.size() > 0) {
                                Sms sms = new Sms();
                                sms.setAppId(CallHelper.Ds.structPC.iStudId);
                                sms.setLogDateTime(SMSTimeStamp);
                                sms.setSmsList(smsList);

                                String result = mRestApiCall.postSmsBackUp((MobiApplication) getApplication(), sms);
                                DeBug.ShowLog("Narayanan", "Uploading SMS result:" + result);
                            }
                        }
                    }
                }
                DeBug.ShowLog("Narayanan", "Uploading SMS Success");
                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SYNC_CALENDER) == eFeatureControl.SYNC_CALENDER) {
                    DeBug.ShowLog("Narayanan", "Uploading Calendar");
                    {
                        CalendarSyncServer cc = new CalendarSyncServer();
                        cc.getCalendarListInit();
                        {

                            ArrayList<LKONagarNigam.CalendarSync> mCalendarSyncList = new ArrayList<LKONagarNigam.CalendarSync>();
                            Cursor cursor = getContentResolver().
                                    query(Uri.parse("content://com.android.calendar/events"),
                                            new String[]{"_id", "title", "description", "dtstart", "dtend", "eventLocation"}, null, null, null);
                            cursor.moveToFirst();
                            String[] CalNames = new String[cursor.getCount()];

                            mCalendarSyncList.clear();


                            for (int i = 0; i < CalNames.length; i++) {
                                CalendarSyncServer.ItemList mItemList = cc.getmItemListInstance();

                                LKONagarNigam.CalendarSync mCalendarSync = new LKONagarNigam().new CalendarSync();

                                mCalendarSync.Repetition = "" + cursor.getInt(0);
                                mCalendarSync.EventName = "" + cursor.getString(1);
                                mCalendarSync.Description = "" + cursor.getString(2);
                                mCalendarSync.Location = "" + cursor.getString(5);
                                mCalendarSync.LogDate = SMSTimeStamp;

                                mItemList.setRepetition(mCalendarSync.Repetition);
                                mItemList.setEventName(mCalendarSync.EventName);
                                mItemList.setDescription(mCalendarSync.Description);
                                mItemList.setLocation(mCalendarSync.Location);
                                mItemList.setSyncDateTime(SMSTimeStamp);

                                Calendar calendar1 = Calendar.getInstance();
                                calendar1.setTimeInMillis(cursor.getLong(3));
                                DateFormat formatter11 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                                String SMSTimeStamp1 = formatter11.format(calendar1.getTime()).toString();

                                Calendar calendar11 = Calendar.getInstance();
                                calendar11.setTimeInMillis(cursor.getLong(4));
                                DateFormat formatter111 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                                String SMSTimeStamp11 = formatter111.format(calendar11.getTime()).toString();

                                mCalendarSync.StartDateTime = SMSTimeStamp1;
                                mCalendarSync.EndDateTime = SMSTimeStamp11;

                                mItemList.setStartDateTime(mCalendarSync.StartDateTime);
                                mItemList.setEndDateTime(mCalendarSync.EndDateTime);

                                mCalendarSyncList.add(mCalendarSync);
                                cc.addCalendarList(mItemList);

                                cursor.moveToNext();
                            }
                            cc.setAppId(CallHelper.Ds.structPC.iStudId);
                            if (cursor.getCount() > 0)

                                cursor.close();

                        }
                        if (!cc.getCalendarList().isEmpty()) {
                            String result = mRestApiCall.postCalendarsEvents((MobiApplication) getApplication(), cc);
                            DeBug.ShowLog("Narayanan", "Uploading Calendar result:" + result);
                        }
                    }
                    DeBug.ShowLog("Narayanan", "Uploading Calendar Success");
                }
            } else if (intent.getBooleanExtra("sensor", false)) {
                //Narayanan edit in mobiocean fetching starts.
                CallHelper.profileSensor = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
                CallHelper.oldSensor = settings.getString("CallHelper.oldSensor", CallHelper.oldSensor);
                CallHelper.Ds.structCCC.stProfileId = settings.getString("structCCC.stProfileId", CallHelper.Ds.structCCC.stProfileId);
                if (!CallHelper.profileSensor.equals(CallHelper.oldSensor)) {
                    try {
                        editor.putString("CallHelper.oldSensor", CallHelper.oldSensor);
                        editor.commit();
                        int profile = Integer.parseInt(CallHelper.profileSensor);
                        resetProfile(profile);
                        notifyUi();
                    } catch (Exception ignore) {
                    }
                }
                //Narayanan edit in mobiocean sensor fetching ends.
            } else {

                // Mandatory things to be fetched Narayanan Starts

                Retrofit retrofit = new Retrofit.Builder().baseUrl(MobiApplication.CONTACT_SERVER).addConverterFactory(GsonConverterFactory.create()).build();
                AanwlaService webInterface = retrofit.create(AanwlaService.class);
                Call<String> call = webInterface.checkSubscription(CallHelper.Ds.structPC.iStudId);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        String s = response.body();
                        DeBug.ShowLog("Expire", "Received result " + s);
                        if (s == null || !s.equals("-1")) {
                            CallHelper.decodeMessage(getBaseContext(), s);
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        DeBug.ShowLog("Expire", "Received result " + t.getMessage());
                    }
                });

                try {
                    JSONObject jsonobj = new JSONObject();
                    try {
                        jsonobj.put("AppId", CallHelper.Ds.structPC.iStudId);
                    } catch (JSONException e2) {
                        e2.printStackTrace();
                    }
                    String MSG = mRestApiCall.getPendingSmS(jsonobj);
                    //if(!MSG.equalsIgnoreCase("-1"))
                    DeBug.ShowLog("MSG", "responce  " + MSG);

                    if (!TextUtils.isEmpty(MSG) && MSG.contains("GBox")) {
                        String Body = MSG.substring(MSG.indexOf("GBox"), MSG.length());
                        String MSGId = MSG.substring(0, MSG.indexOf("GBox"));
                        DeBug.ShowLog("GCMIntentService", "" + Body);
                        DeBug.ShowLog("GCMIntentService", "" + MSGId);
                        CallHelper.Ds.structGCM.stMessage = (Body);
                        CallHelper.Ds.structGCM.stMessageId = (MSGId);
                        editor.putString("structGCM.stMessage", CallHelper.Ds.structGCM.stMessage);
                        editor.putString("structGCM.stMessageId", CallHelper.Ds.structGCM.stMessageId);
                        if (!CallHelper.Ds.structGCM.bAlreadyRunning)
                            ACKtoGCM(this);
                        editor.commit();
                    }
                } catch (Exception ignore) {
                }

                String lockPin = mRestApiCall.getLock((MobiApplication) getApplication());
                if (lockPin != null && !lockPin.isEmpty()) {
                    editor.putString("LockPin", lockPin);
                    editor.commit();
                }

                JSONObject json3 = new JSONObject();
                try {
                    json3.put("StuMob", "1");
                    json3.put("countryId", 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String p = mRestApiCall.RT(json3);
                if (p != null && !p.isEmpty()) {
                    CallHelper.decodeMessage(getBaseContext(), p);
                }

                // Mandatory things to be fetched Narayanan Ends

                Context context = getBaseContext();
                MobiApplication mobiApplication = (MobiApplication) getApplication();

                EnabledExtraFeatureBean extraFeatures = mRestApiCall.getEnabledExtraFeature(mobiApplication);
                if (extraFeatures != null) {
               /* editor.putBoolean("IsAttendance", extraFeatures.IsAttendance);
                editor.putBoolean("IsTravelAllowance", extraFeatures.IsTravelAllowance);
                editor.putBoolean("IsConveyance", extraFeatures.IsConveyance);*/
                    editor.putBoolean("IsAttendance", false);
                    editor.putBoolean("IsTravelAllowance", false);
                    editor.putBoolean("IsConveyance", false);
                    editor.putBoolean("IsSecureStorage", extraFeatures.IsSecureStorage);
                    editor.commit();
                    notifyUi();
                }

                VpnStatusBean vpnStatusBean = mRestApiCall.getVpnDetails(mobiApplication);
                if (vpnStatusBean != null) {
                    editor.putBoolean("VpnIsEnabled", vpnStatusBean.IsEnabled);
                    editor.putString("VpnIpAddress", vpnStatusBean.IpAddress);
                    editor.commit();
                }

                ArrayList<SensorListBean> sensorLists = mRestApiCall.getSensorList(mobiApplication);
                if (sensorLists != null) {
                    SensorListTable table = new SensorListTable(context);
                    table.addSensors(sensorLists);
                }

                ArrayList<UpdateFromServer> listUpdates = mRestApiCall.getWhatIsUpdated(mobiApplication);
                int size = (listUpdates != null) ? listUpdates.size() : 0;
                //Narayanan edit in mobiocean fetching starts.
                if (size > 0) {
                    //Set update index for ack
                    ArrayList<Integer> updateIndexes = new ArrayList<>();
                    for (int i = 0; i < size; i++)
                        updateIndexes.add(listUpdates.get(i).getUpdateResultInt());

                    //Get Values
                    ArrayList<ContactListBean> contactLists = mRestApiCall.getContactList(mobiApplication); //Allowed  - 7
                    int activeProfileId = mRestApiCall.getActivatedProfile(mobiApplication); //ProfileUser - 3
                    ArrayList<ProfileInfoBean> profileInfoLists = mRestApiCall.getProfileList(mobiApplication); // Profile - 1
                    ArrayList<ProfileInfoBean> profileInfoDetails = mRestApiCall.getProfileDetails(mobiApplication); // ProfileFeature - 2
                    ArrayList<AppGroupBean> appGroups = mRestApiCall.getAppGroups(mobiApplication); // AppGroup and User - 4 and 5
                    ArrayList<WebListBean> webLists = mRestApiCall.getWebSiteList(mobiApplication); //Website - 6

                    int updateDone = 0;

                    //Send Update
                    if (contactLists != null)
                        updateDone++;
                    if (activeProfileId > -1)
                        updateDone++;
                    if (profileInfoDetails != null)
                        updateDone++;
                    if (appGroups != null)
                        updateDone++;
                    if (webLists != null)
                        updateDone++;
                    if (profileInfoLists != null)
                        updateDone++;

                    if (contactLists != null && activeProfileId > -1 && profileInfoDetails != null && appGroups != null && webLists != null & profileInfoLists != null) {
                        if (updateIndexes.size() <= updateDone) {
                            for (int i : updateIndexes)
                                sendAck(i);
                        }

                        if (contactLists != null) {
                            ContactListTable table = new ContactListTable(context);
                            table.addContacts(contactLists);
                        }

                        if (profileInfoLists != null) {
                            MobiProfileInfoTable table = new MobiProfileInfoTable(context);
                            table.addProfiles(profileInfoLists);
                        }

                        if (profileInfoDetails != null) {
                            MobiProfileDetailTable table = new MobiProfileDetailTable(context);
                            table.addProfiles(profileInfoDetails);
                        }

                        if (appGroups != null) {
                            AppGroupTable table = new AppGroupTable(context);
                            table.addGroups(appGroups);
                        }

                        if (webLists != null) {
                            WebListTable table = new WebListTable(context);
                            table.addGroups(webLists);
                        }

                        if (activeProfileId > -1) {
                            if (activeProfileId > 0) {
                                CallHelper.Ds.structCCC.stProfileId = "" + activeProfileId;
                                CallHelper.Ds.structCCC.isProfileEnabled = 1;
                            } else {
                                CallHelper.Ds.structCCC.stProfileId = "0";
                                CallHelper.Ds.structCCC.isProfileEnabled = 0;
                            }
                            CallHelper.profileSensor = "" + activeProfileId;
                            CallHelper.oldSensor = "" + activeProfileId;
                            editor.putString("CallHelper.profileSensor", CallHelper.profileSensor);
                            editor.putString("structCCC.stProfileId", CallHelper.Ds.structCCC.stProfileId);
                            editor.putInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
                            editor.putString("CallHelper.oldSensor", CallHelper.profileSensor);
                            editor.commit();
                            resetProfile(activeProfileId);
                        }
                        notifyUi();
                    }

                }
                //Narayanan edit in mobiocean fetching ends.
            }
            serviceStarted = false;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void resetProfile(int profileId) {

        //reset current Profile
        CallHelper.Ds.structFCC.bTotalFeatures = 80;
        CallHelper.Ds.structFCC.lAllowedTime = new long[7][CallHelper.Ds.structFCC.bTotalFeatures][24];
        CallHelper.Ds.structACC.lAllowedTime = new long[7][CallHelper.Ds.structFCC.bTotalFeatures][24];
        CallHelper.Ds.structNCC.lAllowedTime = new long[7][CallHelper.Ds.structFCC.bTotalFeatures][24];
        CallHelper.Ds.structNCC.wTotalDuration = new short[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structACC.bTimeDurationCtrl = new byte[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structFCC.bTimeDurationCtrl = new byte[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structNCC.wStartTime = new short[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structNCC.wEndTime = new short[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structNCC.wUsedDuration = new short[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structFCC.wUsedDuration = new short[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structNCC.bPresentlyStopped = new boolean[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structFCC.bPresentlyStopped = new boolean[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structACC.bPresentlyStopped = new boolean[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structNCC.bAlarmManager = new boolean[7][CallHelper.Ds.structFCC.bTotalFeatures];
        CallHelper.Ds.structACC.listGroupIds = new ArrayList<>();
        CallHelper.runTime = null;
        CallHelper.Ds.structCCC.tempwFeatureControlWord = 0L;

        boolean IsAttendance = settings.getBoolean("IsAttendance", false);
        boolean IsTravelAllowance = settings.getBoolean("IsTravelAllowance", false);
        boolean IsConveyance = settings.getBoolean("IsConveyance", false);
        boolean IsSecureStorage = settings.getBoolean("IsSecureStorage", false);
        String pin = settings.getString("LockPin", "1111");
        CallHelper.profileSensor = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
        CallHelper.Ds.structCCC.stProfileId = settings.getString("structCCC.stProfileId", CallHelper.Ds.structCCC.stProfileId);
        CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
        String uname = settings.getString("UserSettingFragment.uname", "");
        String email = settings.getString("UserSettingFragment.email", "");
        String mobile = settings.getString("UserSettingFragment.mobile", "");
        String emp_code = settings.getString("UserSettingFragment.emp_code", "");
        String ImagePath = settings.getString("UserSettingFragment.ImagePath", "");
        boolean vpnIsEnabled = settings.getBoolean("VpnIsEnabled", false);
        String vpnIpAddress = settings.getString("VpnIpAddress", "");
        boolean appUpdate = settings.getBoolean("CallHelper.AppUpdate", false);
        boolean conveyanceStart = settings.getBoolean("Conveyance.isStarted", false);
        float vehicleReading = settings.getFloat("Conveyance.vehicleReading", 0);
        int conveyanceId = settings.getInt("Conveyance.ConveyanceId", 0);
        long tempTime = settings.getLong("CallDetectService.smsLastTime", 0L);
        boolean AttendanceIsLogged = settings.getBoolean("AttendanceActivity.isLogged", false);
        String InstallApp = settings.getString("CallHelper.InstallApp", "");
        String UpdateOs = settings.getString("CallHelper.UpdateOs", "");
        int FileId = settings.getInt("Secure.FileID", 0);
        int isUpdate = settings.getInt("Secure.isUpdate", 0);
        String stNew = settings.getString("stNewSimSerialNumber", "");
        boolean SimChangeSentToServer = settings.getBoolean("CallHelper.SimChangeSentToServer", false);
        boolean isBlockQuickSettings = settings.getBoolean(RootConstants.ISBLOCKQUICKSETTINGS, false);
        editor.clear();
        editor.putBoolean(RootConstants.ISBLOCKQUICKSETTINGS, isBlockQuickSettings);
        editor.putBoolean("CallHelper.SimChangeSentToServer", SimChangeSentToServer);
        editor.putString("stNewSimSerialNumber", stNew);
        editor.putInt("Secure.isUpdate", isUpdate);
        editor.putInt("Secure.FileID", FileId);
        editor.putString("CallHelper.InstallApp", UpdateOs);
        editor.putString("CallHelper.UpdateOs", InstallApp);
        editor.putBoolean("AttendanceActivity.isLogged", AttendanceIsLogged);
        editor.putLong("CallDetectService.smsLastTime", tempTime);
        editor.putInt("Conveyance.ConveyanceId", conveyanceId);
        editor.putFloat("Conveyance.vehicleReading", vehicleReading);
        editor.putBoolean("Conveyance.isStarted", conveyanceStart);
        editor.putBoolean("CallHelper.AppUpdate", appUpdate);
        editor.putBoolean("VpnIsEnabled", vpnIsEnabled);
        editor.putString("VpnIpAddress", vpnIpAddress);
        editor.putBoolean("IsAttendance", IsAttendance);
        editor.putBoolean("IsTravelAllowance", IsTravelAllowance);
        editor.putBoolean("IsConveyance", IsConveyance);
        editor.putBoolean("IsSecureStorage", IsSecureStorage);
        editor.putString("LockPin", pin);
        editor.putString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
        editor.putString("CallHelper.profileSensor", "" + profileId);
        editor.putString("CallHelper.oldSensor", "" + profileId);
        editor.putString("structCCC.stProfileId", CallHelper.Ds.structCCC.stProfileId);
        editor.putString("UserSettingFragment.uname", uname);
        editor.putString("UserSettingFragment.email", email);
        editor.putString("UserSettingFragment.mobile", mobile);
        editor.putString("UserSettingFragment.emp_code", emp_code);
        editor.putString("UserSettingFragment.ImagePath", ImagePath);
        editor.commit();

        DataStructure.runInTime = new HashMap<String, boolean[][]>();
        DataStructure.totalApplication = new ArrayList<String>();

        if (profileId > 0) {
            CallHelper.Ds.structCCC.isProfileEnabled = 1;
            editor.putInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
            editor.commit();
        } else {
            CallHelper.Ds.structCCC.isProfileEnabled = 0;
            editor.putInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
            editor.commit();
        }

        Context context = getBaseContext();

        MobiProfileDetailTable profileTable = new MobiProfileDetailTable(context);
        AppGroupTable appTable = new AppGroupTable(context);

        ArrayList<ProfileInfoBean> profileInfoDetails = profileTable.getProfiles(profileId);
        ArrayList<AppGroupBean> appGroups = appTable.getProfiles(profileId);

        if (appGroups != null && appGroups.size() > 0 && profileId > 0) {
            for (AppGroupBean bean : appGroups)
                if (bean.ProfileId == profileId)
                    CallHelper.decodeMessage(context, bean.Message);
        }

        if (profileInfoDetails != null && profileInfoDetails.size() > 0 && profileId > 0) {
            for (ProfileInfoBean bean : profileInfoDetails) {
                if (bean.ProfileId == profileId) {
                    if (bean.FeatureId == 54) {
                        if (bean.IsEnable == 1) {
                            CallHelper.Ds.structFCC.IsSosAutoAnswer = true;
                        } else {
                            CallHelper.Ds.structFCC.IsSosAutoAnswer = false;
                        }
                        editor.putBoolean("sosAuto", CallHelper.Ds.structFCC.IsSosAutoAnswer);
                        editor.commit();
                    } else if (bean.FeatureId == eFeatureControl.iBLOCK_GPS) { //SIVA _Block GPS Off
                        DeBug.ShowLog("FEAUTUREGPS",""+bean.IsEnable);
                        if (bean.IsEnable == 1) {
                            CallHelper.Ds.structFCC.IsBlockGps = true;
                        } else {
                            CallHelper.Ds.structFCC.IsBlockGps = false;
                        }
                        editor.putBoolean(RootConstants.ISBLOCKGPS, CallHelper.Ds.structFCC.IsBlockGps);
                        editor.commit();
                        try {
                            context.startService(new Intent(context, GpsWifiIntentService.class).putExtra("PUTFEATURETYPE",1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (bean.FeatureId == eFeatureControl.iBLOCK_NOTIFICATION) { //SIVA _Block Notifications
                        DeBug.ShowLog("FEAUTURENOTIFICATION",""+bean.IsEnable);
                        if (bean.IsEnable == 1) {
                            CallHelper.Ds.structFCC.IsBlockNotification = true;
                        } else {
                            CallHelper.Ds.structFCC.IsBlockNotification = false;
                        }
                        editor.putBoolean(RootConstants.ISBLOCKNOTIFICATION, CallHelper.Ds.structFCC.IsBlockNotification);
                        editor.commit();
                    } else if (bean.FeatureId == eFeatureControl.iBLOCK_USBROOT) { //SIVA _Block USB Connection
                        DeBug.ShowLog("FEAUTUREUSB",""+bean.IsEnable);
                        if (bean.IsEnable == 1) {
                            CallHelper.Ds.structFCC.IsBlockUsb = true;
                        } else {
                            CallHelper.Ds.structFCC.IsBlockUsb = false;
                        }
                        editor.putBoolean(RootConstants.ISBLOCKUSB, CallHelper.Ds.structFCC.IsBlockUsb);
                        editor.commit();
                    } else if (bean.FeatureId == eFeatureControl.iBLOCK_MOBILEDATA) { //SIVA _can't disable mobiledata
                        DeBug.ShowLog("FEAUTUREMOBILEDATA",""+bean.IsEnable);
                        if (bean.IsEnable == 1) {
                            CallHelper.Ds.structFCC.IsBlockMobileData = true;
                        } else {
                            CallHelper.Ds.structFCC.IsBlockMobileData = false;
                        }
                        editor.putBoolean(RootConstants.ISBLOCKMOBILEDATA, CallHelper.Ds.structFCC.IsBlockMobileData);
                        editor.commit();
                    } else if (bean.FeatureId == eFeatureControl.iBLOCK_QUICKSETTINGS) { //SIVA disable quick settings and notification bar
                        DeBug.ShowLog("FEAUTUREQUICKSETTINGS", "" + bean.IsEnable);
                        if (bean.IsEnable == 1) {
                            CallHelper.Ds.structFCC.IsBlockQuickSettings = true;
                            editor.putBoolean(RootConstants.ISBLOCKQUICKSETTINGS, CallHelper.Ds.structFCC.IsBlockQuickSettings);
                            editor.commit();
                            if (!Constants.isMyServiceRunning(context, DisableQuickSettingsService.class)) {
                                context.startService(new Intent(context, DisableQuickSettingsService.class));
                            }
                        } else {
                            CallHelper.Ds.structFCC.IsBlockQuickSettings = false;
                            editor.putBoolean(RootConstants.ISBLOCKQUICKSETTINGS, CallHelper.Ds.structFCC.IsBlockQuickSettings);
                            editor.commit();
                            context.stopService(new Intent(context, DisableQuickSettingsService.class));
                        }
                    }


                    if (bean.IsEnable == 1) {
                        {
                            CallHelper.Ds.structCCC.tempwFeatureControlWord = (long) (CallHelper.Ds.structCCC.tempwFeatureControlWord | (1L << Long.parseLong("" + bean.FeatureId)));
                            if (bean.Message != null && !bean.Message.isEmpty()) {
                                CallHelper.decodeMessage(context, bean.Message);
                            }
                        }
                        if (bean.FeatureId == 19) {
                            if (bean.IsBlackList == 1) {
                                CallHelper.Ds.structFCC.IsWhiteList = 1;
                            } else {
                                CallHelper.Ds.structFCC.IsWhiteList = 0;
                            }
                            editor.putInt("isCallBack", CallHelper.Ds.structFCC.IsWhiteList);
                            editor.commit();
                        } else if (bean.FeatureId == 22) {
                            if (bean.IsBlackList == 1) {
                                CallHelper.Ds.structFCC.IsSmsBlack = 1;
                            } else {
                                CallHelper.Ds.structFCC.IsSmsBlack = 0;
                            }
                            editor.putInt("isSmsBack", CallHelper.Ds.structFCC.IsSmsBlack);
                            editor.commit();
                        } else if (bean.FeatureId == 42) {
                            String msg = "";
                            if(bean.IsBlackList == 1){
                                CallHelper.Ds.structFCC.IsWebBlack = 0;
                            }else{
                                CallHelper.Ds.structFCC.IsWebBlack = 1;
                            }
                            if (!bean.Message.isEmpty())
                                msg = bean.Message;
                            else {
                                DataStructure.runInTime = new HashMap<String, boolean[][]>();
                                DataStructure.totalApplication = new ArrayList<String>();
                            }
                            editor.putString("webCode", msg);
                            editor.putInt("webCode.IsWebBlack", CallHelper.Ds.structFCC.IsWebBlack);
                            editor.commit();
                        }
                    }
                }
            }
        }

        CallHelper.Ds.structCCC.wFeatureControlWord[0] = CallHelper.Ds.structCCC.tempwFeatureControlWord;
        editor.putLong("structCCC.wFeatureControlWord" + 0, CallHelper.Ds.structCCC.tempwFeatureControlWord);
        editor.commit();

    }

    private void notifyUi() {
        Intent send = new Intent("UpdateUi");
        send.putExtra("structCCC.wFeatureControlWord", (long) CallHelper.Ds.structCCC.wFeatureControlWord[0]);
        LocalBroadcastManager.getInstance(this).sendBroadcast(send);
    }

    private void sendAck(int featureId) {
        RestApiCall mRestApiCall = new RestApiCall();
        UpdateFromServer mfromServer = new UpdateFromServer();
        mfromServer.setAckDateTime(CallHelper.GetTimeWithDate());
        mfromServer.setSyncFeatureId("" + featureId);
        mfromServer.setAppId(CallHelper.Ds.structPC.iStudId);
        mRestApiCall.postUpdateAck((MobiApplication) getApplication(), mfromServer);
    }

    private void ACKtoGCM(final Context ctx) {
        final String TAG = "callURLGCM";
        CallHelper.Ds.structGCM.bAlreadyRunning = true;
        int responce = 0;
        RestApiCall mRestApiCall = new RestApiCall();
        JSONObject jsonobj = new JSONObject();
        try {
            jsonobj.put("SendMsgIdList", CallHelper.Ds.structGCM.stMessageId);
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        String result = mRestApiCall.sendACKGCM(jsonobj);
        if (Integer.parseInt(result) != -1) {
            CallHelper.Ds.structGCM.bCheckGCMACK = false;
            CallHelper.decodeMessage(ctx, CallHelper.Ds.structGCM.stMessage);
            DeBug.ShowLog(TAG, "responce of  " + CallHelper.Ds.structGCM.stMessageId + " = " + (responce - 48));
        } else {
            CallHelper.decodeMessage(ctx, CallHelper.Ds.structGCM.stMessage);
            DeBug.ShowLog(TAG, "responce of IOException " + CallHelper.Ds.structGCM.stMessage + " = " + (responce - 48));
            CallHelper.Ds.structGCM.bCheckGCMACK = true;
        }
        editor.putBoolean("structGCM.bCheckGCMACK", CallHelper.Ds.structGCM.bCheckGCMACK);
        editor.commit();
        CallHelper.Ds.structGCM.bAlreadyRunning = false;
    }

}
