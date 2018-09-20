package com.mobiocean.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.mobiocean.R;
import com.mobiocean.gcm.RegistrationIntentService;
import com.mobiocean.rootfeatures.ScreenOnOffReceiver;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.eFeatureControl;

import org.sn.adapter.HomeFragmentList;
import org.sn.beans.HomeFragmentItemBean;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private ArrayList<HomeFragmentItemBean> beans;
    private GridView contents_values;
    private HomeFragmentList adapter;

    public HomeFragment() {
    }

    Context ctx;
    protected static LocationManager locationManager;
    protected static double longitude = 0;
    protected static double latitude = 0;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;
    protected Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.System.canWrite(getActivity())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            if (!Settings.canDrawOverlays(getActivity())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getActivity().getPackageName()));
                startActivity(intent);
            }

        }

        View rootView = inflater.inflate(R.layout.activity_lnn, container, false);

        beans = new ArrayList<>();

        settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        CallHelper.Ds.structCCC.wFeatureControlWord[0] = (long) settings.getLong("structCCC.wFeatureControlWord" + 0, CallHelper.Ds.structCCC.wFeatureControlWord[0]);
        ctx = getActivity();
        SetUI(rootView);
        locationManager = CallHelper.getlocationManager(getActivity());
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled && CallHelper.canOpenGPS) {
            CallHelper.canOpenGPS = false;
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(settingsIntent);
        }

        DeBug.ShowLog("TestingApp", "Activity 1 demoDeviceAdmin Not asctivated");

        Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
        getActivity().startService(intent);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(UpdateUi, new IntentFilter("UpdateUi"));

        getActivity().sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
        getActivity().sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
        {
            if (!settings.getBoolean("unlockedSOS", true)) {
                Intent mIntent = new Intent(ctx, Lock.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mIntent.putExtra("isForLogin", (int) 0);
                startActivity(mIntent);
            }
            if (!settings.getBoolean("unlocked", true)) {
                Intent mIntent = new Intent(ctx, Lock.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mIntent.putExtra("isForLogin", (int) 1);
                startActivity(mIntent);
            }

            if (!settings.getBoolean("unlockedSound", true)) {
                Intent mIntent = new Intent(ctx, Lock.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mIntent.putExtra("isForLogin", (int) 2);
                startActivity(mIntent);
            }
        }
        //SiVA for disable quick settings register receiver
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction("android.intent.action.PHONE_STATE");
            ScreenOnOffReceiver mReceiver = new ScreenOnOffReceiver();
            ctx.registerReceiver(mReceiver, filter);


          /*  CallHelper.Ds.structFCC.IsBlockQuickSettings = true;
            editor.putBoolean(RootConstants.ISBLOCKQUICKSETTINGS, CallHelper.Ds.structFCC.IsBlockQuickSettings);
            editor.commit();
            if (!Constants.isMyServiceRunning(ctx, DisableQuickSettingsService.class)) {
                ctx.startService(new Intent(ctx, DisableQuickSettingsService.class));
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(UpdateUi);
        super.onDestroy();
    }

    private void SetUI(View view) {

        beans.clear();
        adapter = new HomeFragmentList(ctx, beans);
        contents_values = (GridView) view.findViewById(R.id.contents_values);
        contents_values.setNumColumns(GridView.AUTO_FIT);
        contents_values.setAdapter(adapter);
        setGridCount();
        adapter.notifyDataSetChanged();

    }

    private BroadcastReceiver UpdateUi = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            DeBug.ShowLog("NOTIFY", "Reload Items");
            beans.clear();
            adapter = new HomeFragmentList(ctx, beans);
            contents_values.setAdapter(adapter);
            setGridCount();
            adapter.notifyDataSetChanged();

        }

    };

    public void setGridCount() {

        beans.add(new HomeFragmentItemBean("Profile", R.drawable.people));
        //beans.add(new HomeFragmentItemBean("Secure Storage", R.mipmap.ic_logout));

        if (!CallHelper.Ds.structPC.bTimeExpired) {

            long controlWord = CallHelper.Ds.structCCC.wFeatureControlWord[0];

            if ((controlWord & eFeatureControl.SYNC_ADRESS) == eFeatureControl.SYNC_ADRESS || (controlWord & eFeatureControl.SYNC_CALENDER) == eFeatureControl.SYNC_CALENDER)
                beans.add(new HomeFragmentItemBean("Backup Data", R.drawable.bkp));
            if ((controlWord & eFeatureControl.SYNC_ADRESS) == eFeatureControl.SYNC_ADRESS)
                beans.add(new HomeFragmentItemBean("Contacts", R.drawable.contacts));
            if ((controlWord & eFeatureControl.SYNC_CALENDER) == eFeatureControl.SYNC_CALENDER)
                beans.add(new HomeFragmentItemBean("Calendar", R.drawable.calendar));
            if ((controlWord & eFeatureControl.BROWSER_PROTECTION) == eFeatureControl.BROWSER_PROTECTION)
                beans.add(new HomeFragmentItemBean("Browser", R.drawable.web));
         /*   if ((controlWord & eFeatureControl.SMS_TOCONT_NO) == eFeatureControl.SMS_TOCONT_NO)
                beans.add(new HomeFragmentItemBean("SOS", R.drawable.gear));
            if (settings.getBoolean("IsAttendance", false))
                beans.add(new HomeFragmentItemBean("Attendance", R.drawable.ic_attendance));
            if (settings.getBoolean("IsConveyance", false))
                beans.add(new HomeFragmentItemBean("Conveyance", R.drawable.ic_conveyance));
            if (settings.getBoolean("IsTravelAllowance", false))
                beans.add(new HomeFragmentItemBean("Travel\nAllowance", R.drawable.ic_conveyance));*/
            if (settings.getBoolean("IsSecureStorage", false))
                beans.add(new HomeFragmentItemBean("Secure Storage", R.drawable.ic_secure_icon));
        }

    }

}