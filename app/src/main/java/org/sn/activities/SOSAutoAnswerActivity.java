package org.sn.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.mobiocean.R;
import com.mobiocean.util.DeBug;

public class SOSAutoAnswerActivity extends AppCompatActivity {

    Context context;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosauto_answer);
        DeBug.ShowLog("CAll", "Incomming CAll 1");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        context = this;
        DeBug.ShowLog("CAll", "Incomming CAll 2");
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec("input keyevent " + Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
                    DeBug.ShowLog("CAll", "Incomming CAll 3");
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        String enforcedPerm = "android.permission.CALL_PRIVILEGED";
                        Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                                Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                                        KeyEvent.KEYCODE_HEADSETHOOK));
                        Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                                Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                                        KeyEvent.KEYCODE_HEADSETHOOK));
                        context.sendOrderedBroadcast(btnDown, enforcedPerm);
                        context.sendOrderedBroadcast(btnUp, enforcedPerm);
                        DeBug.ShowLog("CAll", "Incomming CAll 4");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 3000);
            }
        });
    }
}