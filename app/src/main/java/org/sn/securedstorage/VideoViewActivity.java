package org.sn.securedstorage;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.mobiocean.R;

import java.io.IOException;

public class VideoViewActivity extends AppCompatActivity {

    private FullscreenVideoLayout videoLayout;
    private HttpMediaStreamer mediaStreamer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        this.videoLayout = (FullscreenVideoLayout) findViewById(R.id.videoview);
        videoLayout.setActivity(this);
        videoLayout.setShouldAutoplay(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        Context context = this;
        Uri uri = getIntent().getData();
        if (uri != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
            }
            try {
                try {
                    if (mediaStreamer == null)
                        mediaStreamer = new HttpMediaStreamer(uri.getPath(), MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())));
                    videoLayout.setVideoURI(mediaStreamer.getUri());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Undefined error please try again later", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        videoLayout.resize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaStreamer != null)
            mediaStreamer.destroy();
    }

}
