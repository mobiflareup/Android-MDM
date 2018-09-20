package com.mobiocean.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.mobiocean.rootfeatures.DisableQuickSettingsService;
import com.mobiocean.ui.PassWordActivity;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.eFeatureControl;

import org.sn.util.Constants;

public class WindowChangeDetectingService extends AccessibilityService {

    public static String packageName = "";
    public static String ClassName = "";

    @Override
    protected void onServiceConnected() {
        try {
            super.onServiceConnected();
            DeBug.ShowLog("CurrentActivity", "service started");
            //Configure these here for compatibility with API 13 and below.
            AccessibilityServiceInfo config = new AccessibilityServiceInfo();
            config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
            config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

            if (Build.VERSION.SDK_INT >= 16)
                //Just in case this helps
                config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

            setServiceInfo(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        try {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );

                ClassName = componentName.getClassName();
                packageName = componentName.getPackageName();


                //ActivityInfo activityInfo = tryGetActivity(componentName);
                //boolean isActivity = activityInfo != null;
//                if (isActivity)
                DeBug.ShowLog("CurrentActivity1", componentName.getClassName());

                if (!CallHelper.Ds.structPC.bTimeExpired)
                    if (CallDetectService.devicePolicyManager != null && CallDetectService.devicePolicyManager.isAdminActive(CallDetectService.demoDeviceAdmin)) {
                        if (componentName.getClassName().equalsIgnoreCase("com.android.settings.deviceadminadd") || componentName.getClassName().equalsIgnoreCase("com.android.packageinstaller.permission.ui.ManagePermissionsActivity")) {
                            Intent intent = new Intent(this, PassWordActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            AppBlock.DeviceAdminThread_Started = false;

                        }
                        if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BLOCK_WIFI_GATEWAY) == eFeatureControl.BLOCK_WIFI_GATEWAY) {
                            if (componentName.getClassName().equalsIgnoreCase("com.android.settings.wifi.WifiDialog") || componentName.getClassName().equalsIgnoreCase("com.android.settings.Settings$WifiSettingsActivity")) {
                                Intent intent = new Intent(this, PassWordActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                AppBlock.DeviceAdminThread_Started = false;
                            }
                        }

                        try {
                            if (componentName.getClassName().equalsIgnoreCase("android.widget.FrameLayout")) { // SIVA TEST FOR BLOCK QUICK SETTINGS
                                if (!Constants.isMyServiceRunning(this, DisableQuickSettingsService.class)) {
                                    DeBug.ShowLog("QUICKSETTINGS", "START FORM WINDOWS CHANGE");
                                    startService(new Intent(this, DisableQuickSettingsService.class)); //SIVA
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub
        DeBug.ShowLog("CurrentActivity", "onInterrupt");
    }

}
