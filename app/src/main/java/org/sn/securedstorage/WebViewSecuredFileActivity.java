package org.sn.securedstorage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.mobiocean.R;

public class WebViewSecuredFileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_secured_file);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        Context context = this;
        WebView web_view = (WebView) findViewById(R.id.web_view);
        WebSettings webSettings = web_view.getSettings();
        webSettings.setBuiltInZoomControls(true);
        Uri uri = getIntent().getData();
        if (uri != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
            }
            web_view.loadUrl(uri.toString());
        } else {
            Toast.makeText(context, "Undefined error please try again later", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
