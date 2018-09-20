package org.sn.activities;

import android.content.Context;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.eFeatureControl;

import static com.mobiocean.service.AppBlock.isGpsAlertOn;
import static com.mobiocean.util.Constant.MIN_LUT;

public class GpsOffActivity extends AppCompatActivity {

    MediaPlayer m;
    private int origionalVolume = 0;
    Uri url;
    Handler handler;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_off);
        handler = new Handler();
        context = GpsOffActivity.this;
        url = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.siren);
        loud();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int iWeekDay = CallHelper.Ds.structPC.bWeekDay;
                int Hr = CallHelper.Ds.structPC.AppHrMin / 100;
                int Min = CallHelper.Ds.structPC.AppHrMin % 100;
                LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                if (manager != null) {
                    boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    if (statusOfGPS || (CallHelper.Ds.structFCC.lAllowedTime[iWeekDay][eFeatureControl.iGPS_BUZZER][Hr] & MIN_LUT[Min]) == 0)
                        finish();
                    else
                        handler.postDelayed(this, 1000);
                } else
                    finish();
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    @Override
    protected void onDestroy() {
        isGpsAlertOn = false;
        super.onDestroy();
        setBack();
    }

    private void loud() {
        try {
            AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            origionalVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            m = MediaPlayer.create(getApplicationContext(), url);
            m.setLooping(true);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBack() {
        try {
            m.stop();
            AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, origionalVolume, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
