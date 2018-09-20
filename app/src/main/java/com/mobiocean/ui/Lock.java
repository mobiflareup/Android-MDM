package com.mobiocean.ui;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.DeBug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Lock extends Activity implements OnClickListener {
    int IntentValue = 0;
    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;
    private boolean isOpened = false;
    private String LockPin = "";
    public static MediaPlayer m;
    private int origionalVolume = 0;
    private Uri url;
    private boolean currentFocus, isPaused;
    private Handler collapseNotificationHandler;
    private String pin = "";
    private TextView passText;
    public static boolean screenOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DeBug.ShowLogD("NarayananLock", "Lock OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        LockPin = settings.getString("LockPin", "8520");
        Intent mIntent1 = getIntent();
        IntentValue = mIntent1.getIntExtra("isForLogin", 0);
        screenOn = true;
        Button no_0 = (Button) findViewById(R.id.no_0);
        Button no_1 = (Button) findViewById(R.id.no_1);
        Button no_2 = (Button) findViewById(R.id.no_2);
        Button no_3 = (Button) findViewById(R.id.no_3);
        Button no_4 = (Button) findViewById(R.id.no_4);
        Button no_5 = (Button) findViewById(R.id.no_5);
        Button no_6 = (Button) findViewById(R.id.no_6);
        Button no_7 = (Button) findViewById(R.id.no_7);
        Button no_8 = (Button) findViewById(R.id.no_8);
        Button no_9 = (Button) findViewById(R.id.no_9);
        Button no_c = (Button) findViewById(R.id.no_c);
        Button no_ac = (Button) findViewById(R.id.no_ac);
        passText = (TextView) findViewById(R.id.passText);
        no_0.setOnClickListener(this);
        no_1.setOnClickListener(this);
        no_2.setOnClickListener(this);
        no_3.setOnClickListener(this);
        no_4.setOnClickListener(this);
        no_5.setOnClickListener(this);
        no_6.setOnClickListener(this);
        no_7.setOnClickListener(this);
        no_8.setOnClickListener(this);
        no_9.setOnClickListener(this);
        no_c.setOnClickListener(this);
        no_ac.setOnClickListener(this);
        url = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.siren);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        DeBug.ShowLogD("NarayananLock", "Lock KeyDown " + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_POWER:
                DeBug.ShowLogD("NarayananLock", "Lock KeyDown");
                if (IntentValue == 2) {
                    loud();
                }
        }
        return true;
    }

    @Override
    protected void onPause() {
        DeBug.ShowLogD("NarayananLock", "Lock onPause");
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        DeBug.ShowLogD("NarayananLock", "Lock onResume");
        isPaused = false;
        super.onResume();
        if (IntentValue == 2 && (m == null || !m.isPlaying())) {
            loud();
        }
    }

    @Override
    protected void onStop() {
        DeBug.ShowLogD("NarayananLock", "Lock onStop");
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        currentFocus = hasFocus;
        if (!hasFocus) {
            collapseNow();
        }
    }

    public void collapseNow() {
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }
        if (!currentFocus && !isPaused) {
            collapseNotificationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Object statusBarService = getSystemService("statusbar");
                    Class<?> statusBarManager = null;
                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    Method collapseStatusBar = null;
                    try {
                        if (Build.VERSION.SDK_INT > 16) {
                            collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                        } else {
                            collapseStatusBar = statusBarManager.getMethod("collapse");
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    collapseStatusBar.setAccessible(true);
                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }
                }
            }, 300L);
        }
    }

    @Override
    protected void onDestroy() {
        DeBug.ShowLogD("NarayananLock", "Lock onDestroy");
        if (isOpened) {
            screenOn = false;
            editor.putBoolean("isLockEnabled", false);
            editor.commit();
        }
        super.onDestroy();
    }

    private void loud() {
        try {
            setBack();
            AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            origionalVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
            manager.setStreamVolume(AudioManager.STREAM_MUSIC, manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            if (m == null || !m.isPlaying()) {
                m = MediaPlayer.create(getApplicationContext(), url);
                m.setLooping(true);
                m.start();
            }
            editor.putBoolean("unlockedSoundOn", true);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBack() {
        try {
            if (m != null) {
                m.stop();
                AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                manager.setStreamVolume(AudioManager.STREAM_MUSIC, origionalVolume, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.no_0:
                pin += "0";
                break;
            case R.id.no_1:
                pin += "1";
                break;
            case R.id.no_2:
                pin += "2";
                break;
            case R.id.no_3:
                pin += "3";
                break;
            case R.id.no_4:
                pin += "4";
                break;
            case R.id.no_5:
                pin += "5";
                break;
            case R.id.no_6:
                pin += "6";
                break;
            case R.id.no_7:
                pin += "7";
                break;
            case R.id.no_8:
                pin += "8";
                break;
            case R.id.no_9:
                pin += "9";
                break;
            case R.id.no_c:
                if (pin != null && !pin.isEmpty()) {
                    pin = pin.substring(0, pin.length() - 1);
                }
                break;
            default:
                pin = "";
                break;
        }
        if (pin != null && !pin.isEmpty()) {
            if (pin.equals(LockPin)) {
                if (IntentValue == 0) {
                    editor.putBoolean("unlockedSOS", true);
                } else if (IntentValue == 1) {
                    editor.putBoolean("unlocked", true);
                } else if (IntentValue == 2) {
                    setBack();
                    editor.putBoolean("unlockedSound", true);
                    editor.putBoolean("unlockedSoundOn", false);
                }
                editor.commit();
                isOpened = true;
                finish();
            } else {
                int no = pin.length();
                passText.setText("");
                if (no < LockPin.length()) {
                    for (int i = 0; i < no; i++)
                        passText.append("*");
                } else {
                    pin = "";
                    Toast.makeText(this, "Entered pin is invalid", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            passText.setText("");
        }
    }
}
