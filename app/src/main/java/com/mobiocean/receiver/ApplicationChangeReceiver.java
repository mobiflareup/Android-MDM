package com.mobiocean.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;

import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.mobidb.ApplicationInfoDB;
import com.mobiocean.mobidb.ApplicationInfoStruct;
import com.mobiocean.service.UploadService;
import com.mobiocean.ui.eFeatureSetting;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.Struct_Send_SMSInfo;
import com.mobiocean.util.eFeatureControl;

import static com.mobiocean.util.Constant.MIN_LUT;

public class ApplicationChangeReceiver extends BroadcastReceiver {
    protected static final String PREFS_NAME = "MyPrefsFile";
    protected SharedPreferences settings;
    protected SharedPreferences.Editor editor;

    byte iWeekDay;
    int Hr;
    int Min;

    @Override
    public void onReceive(Context context, Intent intent) {
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        iWeekDay = CallHelper.Ds.structPC.bWeekDay;
        Hr = CallHelper.Ds.structPC.AppHrMin / 100;
        Min = CallHelper.Ds.structPC.AppHrMin % 100;
        int index = 0;
        PackageManager manager = context.getPackageManager();
        Uri u = intent.getData();
        String pack = u.getSchemeSpecificPart().toString();
        if (!pack.toLowerCase().contains("mobiocean"))
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);
                ApplicationInfo ai;
                try {
                    ai = manager.getApplicationInfo(pack, 0);
                } catch (final NameNotFoundException e) {
                    ai = null;
                }
                final String applicationName = (String) (ai != null ? manager.getApplicationLabel(ai) : "(unknown)");
                pack = pack.toLowerCase();
                index = mApplicationInfoDB.getMaxIndex() + 1;

                ApplicationInfoStruct mApplicationInfoStruct = new ApplicationInfoStruct();
                boolean blocked = false;
                CallHelper.Ds.structCCC.wFeatureControlWord[0] = settings.getLong("structCCC.wFeatureControlWord" + 0, CallHelper.Ds.structCCC.wFeatureControlWord[0]);

                if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_NEW_APP) == eFeatureControl.BLOCK_NEW_APP)
                    blocked = blockNewInstalledApps();

                int appGroup = 0;

                if (blocked) {
                    appGroup = 1;
                } else {
                    appGroup = 0;
                }
                mApplicationInfoStruct.setAppGroup(appGroup);
                mApplicationInfoStruct.setAppIndex(index);
                mApplicationInfoStruct.setAppName(applicationName);
                mApplicationInfoStruct.setAppPackege(pack);
                mApplicationInfoStruct.setIsAllowed(1);
                mApplicationInfoStruct.setIsAllowedNow(1);
                mApplicationInfoStruct.setIsInstalled(1);
                mApplicationInfoStruct.setIsSyncWithServer(0);
                mApplicationInfoStruct.setTimestamp(System.currentTimeMillis());

                if (mApplicationInfoDB.updateAppInstallationStatusWithGroup(pack.toLowerCase(), 1, appGroup) < 1)
                    mApplicationInfoDB.addData(mApplicationInfoStruct);
                try {
                    DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                    String TimeStamp1 = CallHelper.GetTimeWithDate();
                    if (SMS_db.recordExist(TimeStamp1, 1) == 0) {
                        SMS_db.addSMS(new Struct_Send_SMSInfo(3, TimeStamp1, applicationName, "", 0.0, 0.0, index, System.currentTimeMillis(), "0", "0", "0", "0"));


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
            } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED") || intent.getAction().equals("android.intent.action.PACKAGE_FULLY_REMOVED")) {
                ApplicationInfoDB mApplicationInfoDB = ApplicationInfoDB.getInstance(context);
                {

                    ApplicationInfo ai;
                    try {
                        ai = manager.getApplicationInfo(pack, 0);
                    } catch (final NameNotFoundException e) {
                        ai = null;
                    }
                    String applicationName = (String) (ai != null ? manager.getApplicationLabel(ai) : "(unknown)");
                    pack = pack.toLowerCase();
                    index = mApplicationInfoDB.getAppIndex(pack);
                    applicationName = mApplicationInfoDB.getAppNameByPkg(pack);
                    mApplicationInfoDB.updateAppInstallationStatus(pack.toLowerCase(), 0);
                    CallHelper.Ds.structACC.packageList.removePkgIgnoreCase(pack);
                    try {
                        DatabaseHabdler_SMSSent SMS_db = DatabaseHabdler_SMSSent.getInstance(context);
                        String TimeStamp1 = CallHelper.GetTimeWithDate();
                        if (SMS_db.recordExist(TimeStamp1, 1) == 0) {
                            SMS_db.addSMS(new Struct_Send_SMSInfo(4, TimeStamp1, applicationName, "", 0.0, 0.0, index, System.currentTimeMillis(), "0", "0", "0", "0"));
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

    private boolean blockNewInstalledApps() {
        boolean blocked = false;
        boolean isCurrentlyBlockedInTime = false;
        if (CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_NEW_APP]) {
            blocked = true;
        } else {
            if (CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_NEW_APP] == 0) {
                CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_NEW_APP] = false;
                CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_NEW_APP] = 3;
            } else {
                isCurrentlyBlockedInTime = false;
                if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_NEW_APP] & eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) == eFeatureSetting.RESTRICTED_IN_CLOCK_TIME) {
                    if ((CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iBLOCK_NEW_APP][Hr] & MIN_LUT[Min]) != 0) {
                        isCurrentlyBlockedInTime = true;
                        blocked = true;
                    }

                }
                if (!isCurrentlyBlockedInTime)
                    if ((CallHelper.Ds.structFCC.bTimeDurationCtrl[iWeekDay][eFeatureControl.iBLOCK_NEW_APP] & eFeatureSetting.RESTRICTED_IN_DURATION) == eFeatureSetting.RESTRICTED_IN_DURATION) {
                        if (!CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_NEW_APP]) {
                            CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_WIFI]++;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_NEW_APP] = 1;
                        }
                        if (CallHelper.Ds.structFCC.wUsedDuration[iWeekDay][eFeatureControl.iBLOCK_NEW_APP] / 60 >= CallHelper.Ds.structFCC.wTotalDuration[iWeekDay][eFeatureControl.iBLOCK_NEW_APP]) {
                            // check if the feature is running with a Flag, then check if the duration is over, if not then let this activity to start/continue and
                            //1. when the activity is being stopped then it saves the current duration,
                            //2. if the duration has expired then stops the activity and save the current duration.
                            CallHelper.Ds.structFCC.bPresentlyStopped[iWeekDay][eFeatureControl.iBLOCK_NEW_APP] = true;
                            CallHelper.Ds.structFCC.bPresentlyOn[eFeatureControl.iBLOCK_NEW_APP] = 3;
                        }
                    }
            }
        }
        return blocked;
    }

}

