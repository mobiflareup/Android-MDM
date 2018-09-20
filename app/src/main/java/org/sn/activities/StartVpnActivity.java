package org.sn.activities;

import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mobiocean.R;

import org.sn.services.MobiVpnService;

public class StartVpnActivity extends AppCompatActivity {

    public static boolean screenOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_vpn);
        screenOn = true;
        Intent intent = VpnService.prepare(this);
        if (intent != null) {
            startActivityForResult(intent, 0);
        } else {
            onActivityResult(0, RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result == RESULT_OK) {
            Intent i = new Intent(this, MobiVpnService.class);
            startService(i);
            finish();
        } else if (result == RESULT_CANCELED){
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        screenOn = false;
        super.onDestroy();
    }
}
