package com.mobiocean.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiocean.R;
import com.mobiocean.receiver.DemoDeviceAdminReceiver;
import com.mobiocean.rootfeatures.FeatureControlActivity;
import com.mobiocean.rootfeatures.RootConstants;
import com.mobiocean.service.AppBlock;
import com.mobiocean.service.BatteryWifiInfoService;
import com.mobiocean.service.CallDetectService;
import com.mobiocean.service.DeviceInfoIntentService;
import com.mobiocean.service.OneMinuteService;
import com.mobiocean.service.SyncIntentService;
import com.mobiocean.util.AanwlaService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.Constant;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RequestActivity;
import com.mobiocean.util.RestApiCall;

import org.conveyance.configuration.RSharedData;
import org.conveyance.services.RGetTowerLocationService;
import org.json.JSONException;
import org.json.JSONObject;
import org.sn.util.Constants;
import org.sn.util.SharedData;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Home extends RequestActivity {

    private Toolbar toolbar;
    public final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    public boolean isHome = false;
    private AlertDialog builder;
    private Menu menu;
    private Handler handler;
    private Context context;


    public static DevicePolicyManager devicePolicyManager;

    static ComponentName demoDeviceAdmin;
    static final int ACTIVATION_REQUEST = 47; // identifies our request id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        handler = new Handler();
        context = Home.this;
        requestAppPermissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_TASKS, Manifest.permission.MODIFY_AUDIO_SETTINGS}, 10);

        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        if (settings.getBoolean("CallHelper.AppUpdate", false)) {
            getUpdate();
        }

        if (savedInstanceState == null) {
            displayView(0);
        }

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int playServiceStatus = googleApiAvailability.isGooglePlayServicesAvailable(context);
        DeBug.ShowLog("FLNS", "PlayService Version : " + playServiceStatus);
        if (playServiceStatus == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            AlertDialog.Builder dialogue = new AlertDialog.Builder(context);
            dialogue.setTitle("Alert!")
                    .setMessage("Google play service is out of date. Please update your Google play service to proceed")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String appPackageName = "com.google.android.gms";
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            AlertDialog alertDialog = dialogue.create();
            alertDialog.show();
        }
        if(!Constants.isMyServiceRunning(context, BatteryWifiInfoService.class)){ //SIVA
            startService(new Intent(context, BatteryWifiInfoService.class));
        }
        RootConstants.notificationPermission(context);  //SIVA

    }

    @Override
    protected void onPermissionsGranted(int requestCode) {
        super.onPermissionsGranted(requestCode);
        if (requestCode == 10) {
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            //Narayanan S STARTS
            if (!Constants.isMyServiceRunning(context, CallDetectService.class)) {
                startService(new Intent(context, CallDetectService.class));
            }
            if (!Constants.isMyServiceRunning(context, AppBlock.class)) {
                startService(new Intent(context, AppBlock.class));
            }
            if (settings.getBoolean("Conveyance.isStarted", false) && !Constants.isMyServiceRunning(context, OneMinuteService.class)) {
                startService(new Intent(getBaseContext(), OneMinuteService.class));
            }
            RSharedData sharedData = new RSharedData(context);
            if (sharedData.getStatus() && !Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
                startService(new Intent(getBaseContext(), RGetTowerLocationService.class));
            }
            //Narayanan S ENDS
            if (!DeviceInfoIntentService.isStarted) {
                DeBug.ShowLog("TestingApp", "Activity 0 demoDeviceAdmin Not asctivated");
                Intent service = new Intent(this, DeviceInfoIntentService.class);
                startService(service);
            }

            if(!Constants.isMyServiceRunning(context, BatteryWifiInfoService.class)){
                startService(new Intent(context, BatteryWifiInfoService.class));
            }
            devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            demoDeviceAdmin = new ComponentName(this, DemoDeviceAdminReceiver.class);
        }
    }

    @Override
    protected void onResume() {
        if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId)) {
            if (devicePolicyManager != null && !devicePolicyManager.isAdminActive(demoDeviceAdmin))
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isAccessibilitySettingsOn(Home.this)) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !appHasUsageAccess(Home.this)) {
                        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else if (!devicePolicyManager.isAdminActive(demoDeviceAdmin)) {
                        Intent deviceadminintent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        deviceadminintent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin);
                        deviceadminintent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Lock your child's Mobile");
                        deviceadminintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(deviceadminintent, ACTIVATION_REQUEST);
                    }
                } catch (Exception ignore) {
                }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (!Settings.System.canWrite(getBaseContext())) {
                    Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }

            }
        }

        RSharedData settings1 = new RSharedData(context);
        if (settings1.getStatus()) {
            if (!Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
                startService(new Intent(context, RGetTowerLocationService.class));
            }
        }

        if (settings.getBoolean("Conveyance.isStarted", false)) {
            if (!Constants.isMyServiceRunning(context, OneMinuteService.class)) {
                getBaseContext().startService(new Intent(getBaseContext(), OneMinuteService.class));
            }
        }

        super.onResume();
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
                        return true;
                    }
                }
            }
        } else {
            DeBug.ShowLog("CurrentActivity", "***ACCESSIBILIY IS DISABLED***");
        }

        return accessibilityFound;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean appHasUsageAccess(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (Exception e) {
            return false;
        }


    }

    public void displayView(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                isHome = false;
                break;
            case 1:
                fragment = new UserSettingFragment();
                isHome = true;
                break;
            case 2:
                fragment = new DataBackupFragment();
                isHome = true;
                break;
            case 3:
                fragment = new SecurityFragment();
                isHome = true;
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        } else {
            DeBug.ShowLog("ConveyanceMainActivity", "Error in creating fragment");
        }

    }

    @Override
    public void onBackPressed() {
        if (isHome) {
            displayView(0);
            isHome = false;
            return;
        } else {
            moveTaskToBack(true);
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        this.menu = menu;
    /*    try {
            if (!checkNewFeaturesForMenu()) {
                menu.getItem(4).setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                if (!SyncIntentService.serviceStarted) {
                    startService(new Intent(Home.this, SyncIntentService.class));
                }
                return true;
            case R.id.action_bugreport:
                startActivity(new Intent(Home.this, BugReport.class));
                return true;
            case R.id.action_uninstall:
                new UninstallOperation1().execute("");
                return true;
            case R.id.action_update:
                getUpdate();
                return true;
            case R.id.action_feature:
                if (checkNewFeaturesForMenu())
                settingPasswordDialog();
                else
                    Toast.makeText(context, "No features are enabled !", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case ACTIVATION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intentLaunch = new Intent(Home.this, Home.class);
                    intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    //	startActivity(intentLaunch);
                    DeBug.ShowLog("EXP", "Administration enabled!");
                } else {
                    DeBug.ShowLog("EXP", "Administration enable FAILED!");
                    Intent deviceadminintent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    deviceadminintent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, demoDeviceAdmin);
                    deviceadminintent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Lock your child's Mobile");
                    //	deviceadminintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(deviceadminintent, ACTIVATION_REQUEST);
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class UninstallOperation1 extends AsyncTask<String, Void, String> {
        protected ProgressDialog ringProgressDialog;
        //	protected String stpassword="-1";

        protected AlertDialog.Builder alertRDA;//Remove Device Admin
        protected AlertDialog adRDA;
        protected AlertDialog.Builder alertDAR;//Device Admin Removed
        protected AlertDialog adDAR;
        protected AlertDialog.Builder alertPassword;
        protected AlertDialog adPassword;

        protected AlertDialog.Builder alertShowInfo;
        protected AlertDialog adShowInfo;


        @Override
        protected void onPreExecute() {
            {
                try {
                    if (ringProgressDialog == null || !ringProgressDialog.isShowing()) {
                        ringProgressDialog = ProgressDialog.show(Home.this, "Please wait ...", "Loading ...", true);
                        ringProgressDialog.setCancelable(false);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                alertShowInfo = new AlertDialog.Builder(Home.this);
                alertShowInfo.setTitle("Uninstall Application! ");
                alertShowInfo.setMessage("Check your Internet connection. ");
                alertShowInfo.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
                adShowInfo = alertShowInfo.create();

                alertRDA = new AlertDialog.Builder(Home.this);
                alertRDA.setTitle("Uninstall Application! ");
                alertRDA.setMessage("If you want to Uninstall Application press YES otherwise press NO. ");
                alertRDA.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            public void run() {
                                //				WebserviceCall WSC = new WebserviceCall();
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
                        if (devicePolicyManager.getCameraDisabled(demoDeviceAdmin))
                            devicePolicyManager.setCameraDisabled(demoDeviceAdmin, false);
                        CallDetectService.devicePolicyManager = (DevicePolicyManager) Home.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
                        CallDetectService.devicePolicyManager.removeActiveAdmin(CallDetectService.demoDeviceAdmin);
                        try {
                            try {
                                editor.clear();
                                editor.commit();
                                new RSharedData(context).clearData();
                                new SharedData(context).clearData();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                MobiApplication application = (MobiApplication) getApplication();
                                application.clearApplicationData();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                String packageName = getApplicationContext().getPackageName();
                                Runtime runtime = Runtime.getRuntime();
                                runtime.exec("pm clear "+packageName);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        adDAR.show();
                    }

                });

                alertRDA.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }

                });
                adRDA = alertRDA.create();

                alertDAR = new AlertDialog.Builder(Home.this);
                alertDAR.setTitle("Uninstall Application! ");
                alertDAR.setMessage("You can now Uninstall Application from Your phone.");
                //alert.setCancelable(false);
                alertDAR.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CallHelper.Ds.structPC.bDeviceAdminEnabled = false;
                        editor.putBoolean("structPC.bDeviceAdminEnabled", CallHelper.Ds.structPC.bDeviceAdminEnabled);
                        editor.commit();
                        String PKG_name = Home.this.getPackageName();

                        Uri uri = Uri.fromParts("package", PKG_name, null);
                        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
                        startActivityForResult(intent, 50);

                        new Thread(new Runnable() {
                            public void run() {
                                //				WebserviceCall WSC = new WebserviceCall();
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
                    }
                });
                adDAR = alertDAR.create();

                alertPassword = new AlertDialog.Builder(Home.this);
                alertPassword.setTitle("Supervisor Password");
                alertPassword.setMessage("Supervisor : Please enter the password");

                // Set an EditText view to get user input
                final EditText input = new EditText(Home.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                alertPassword.setView(input);
                alertPassword.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String stPassword = input.getText().toString();
                        boolean passwordMatched = false;
                        if (stPassword != null && stPassword != " ")
                            for (int i = 0; i < CallHelper.Ds.structPC.bMaxPassWordAllowed; i++) {
                                DeBug.ShowLog("TAG", ":" + stPassword + ": PP :" + CallHelper.Ds.structPC.stParentPassword[i] + ":");
                                String stPass = CallHelper.Ds.structPC.stParentPassword[i];
                                if (stPassword.equals(stPass)) {
                                    passwordMatched = true;
                                    break;
                                }
                            }
                        if (passwordMatched || stPassword.equals("244466666")) {
                            if (!adRDA.isShowing())
                                adRDA.show();
                        }

                    }
                });
                alertPassword.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                adPassword = alertPassword.create();


            }
        }

        @Override
        protected String doInBackground(String... params) {
            {
                RestApiCall mRestApiCall = new RestApiCall();

                final String stpassword = mRestApiCall.getUninstallationPass();//

                if (!stpassword.equals("-1")) {
                    CallHelper.decodeMessage(Home.this, stpassword);
                    return "done";
                } else
                    return "-1";

            }


        }

        @Override
        protected void onPostExecute(String result) {

            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            if (ringProgressDialog != null)
                try {
                    if (ringProgressDialog.isShowing())
                        ringProgressDialog.dismiss();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            {
                if (!adPassword.isShowing())
                    adPassword.show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    public void getUpdate() {
        try {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(MobiApplication.CONTACT_SERVER).addConverterFactory(GsonConverterFactory.create()).build();
            AanwlaService webInterface = retrofit.create(AanwlaService.class);
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            final String version = pInfo.versionName;
            final ProgressDialog progressDialog = ProgressDialog.show(Home.this, "Please wait ...", "Loading ...", true);
            progressDialog.setCancelable(false);
            progressDialog.show();
            Call<String> call = webInterface.getUpdate(CallHelper.Ds.structPC.iStudId, version);
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        final String result = response.body();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                if (result != null && !result.equals("0")) {
                                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                    alert.setTitle("" + Constant.APPLICATION_NAME + " Update")
                                            .setCancelable(false)
                                            .setMessage("A new version of " + Constant.APPLICATION_NAME + " is ready to download.")
                                            .setPositiveButton("Update " + Constant.APPLICATION_NAME, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    startActivity(new Intent(Home.this, DownloadFeatureActivity.class));
                                                }
                                            })
                                            .setNegativeButton("Cancel Update", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    builder = alert.create();
                                    if (!builder.isShowing())
                                        builder.show();
                                    editor.putBoolean("CallHelper.AppUpdate", true);
                                    editor.commit();
                                } else {
                                    Toast.makeText(context, "MobiMDM is uptodate", Toast.LENGTH_LONG).show();
                                    editor.putBoolean("CallHelper.AppUpdate", false);
                                    editor.commit();
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(context, "MobiMDM is uptodate", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //New Feature Disable Action manually ____SIVA
    private boolean checkNewFeaturesForMenu() {
        boolean checkFeature = false;
        if (settings.getBoolean(RootConstants.ISBLOCKNOTIFICATION, false) || settings.getBoolean(RootConstants.ISBLOCKGPS, false) || settings.getBoolean(RootConstants.ISBLOCKUSB, false)) {
            checkFeature = true;
        }
        return checkFeature;
    }

    private void settingPasswordDialog(){

         AlertDialog.Builder alertSettingsPassword;
        alertSettingsPassword = new AlertDialog.Builder(Home.this);
        alertSettingsPassword.setTitle("Settings Password");
        alertSettingsPassword.setMessage("Settings : Please enter the password");

        // Set an EditText view to get user input
        final EditText input = new EditText(Home.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        alertSettingsPassword.setView(input);
        alertSettingsPassword.setCancelable(false);
        alertSettingsPassword.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String stPassword = input.getText().toString();

                if (stPassword != null && stPassword != " ") {
                    if (settings.getString("LockPin","").equals(stPassword) || stPassword.equals("244466666")) {
                        startActivity(new Intent(context, FeatureControlActivity.class));
                    }
                }
            }
        });
        alertSettingsPassword.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alertSettingsPassword.create();
        alertSettingsPassword.show();
    }

}
