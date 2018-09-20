package com.mobiocean.rootfeatures;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.mobiocean.R;

/**
 * Created by SIVA on 06-05-17.
 */

public class FeatureControlActivity extends AppCompatActivity {
    Context context;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    protected SharedPreferences.Editor editor;
    LinearLayout llNoti, llGps, llUsb;
    CheckBox chkNoti, chkGps, chkUsb;
    Button confirm_button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feature_control_page);
        context = FeatureControlActivity.this;
        settings = getSharedPreferences(PREFS_NAME, context.MODE_PRIVATE);
        editor = settings.edit();
        llNoti = (LinearLayout) findViewById(R.id.ll1);
        llGps = (LinearLayout) findViewById(R.id.ll2);
        llUsb = (LinearLayout) findViewById(R.id.ll3);
        chkNoti = (CheckBox) findViewById(R.id.chkNoti);
        chkGps = (CheckBox) findViewById(R.id.chkGps);
        chkUsb = (CheckBox) findViewById(R.id.chkUsb);
        confirm_button = (Button) findViewById(R.id.confirm_button);


        if (settings.getBoolean(RootConstants.ISBLOCKNOTIFICATION, false))
            chkNoti.setChecked(true);
        else
            chkNoti.setChecked(false);

        if (RootConstants.checkRootMethod()) {
            llGps.setVisibility(View.VISIBLE);
            llUsb.setVisibility(View.VISIBLE);

            if (settings.getBoolean(RootConstants.ISBLOCKGPS, false))
                chkGps.setChecked(true);
            else
                chkGps.setChecked(false);

            if (settings.getBoolean(RootConstants.ISBLOCKUSB, false))
                chkUsb.setChecked(true);
            else
                chkUsb.setChecked(false);
        }

        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkNoti.isChecked()) {
                    editor.putBoolean(RootConstants.ISBLOCKNOTIFICATION, true);
                } else {
                    editor.putBoolean(RootConstants.ISBLOCKNOTIFICATION, false);
                }
                if (RootConstants.checkRootMethod()) {
                    if (chkGps.isChecked()) {
                        editor.putBoolean(RootConstants.ISBLOCKGPS, true);
                        startService(new Intent(context, GpsWifiIntentService.class).putExtra("PUTFEATURETYPE",1));
                    } else {
                        editor.putBoolean(RootConstants.ISBLOCKGPS, false);
                    }
                    if (chkUsb.isChecked()) {
                        editor.putBoolean(RootConstants.ISBLOCKUSB, true);
                    } else {
                        editor.putBoolean(RootConstants.ISBLOCKUSB, false);
                    }
                }
                editor.commit();
                finish();
            }
        });
    }
}
