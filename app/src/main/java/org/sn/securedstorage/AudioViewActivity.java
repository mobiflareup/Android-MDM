package org.sn.securedstorage;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiocean.R;

public class AudioViewActivity extends AppCompatActivity {

    private ImageView secure_frame_image;
    private MediaPlayer mediaPlayer = null;
    private SeekBar seekBar;
    private TextView seekBarTime;
    private Handler handler = new Handler();
    private HttpMediaStreamer mediaStreamer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_view);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        Context context = this;
        FrameLayout secured_frame = (FrameLayout) findViewById(R.id.secured_frame);
        secure_frame_image = (ImageView) findViewById(R.id.secure_frame_image);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBarTime = (TextView) findViewById(R.id.seekBarTime);
        secured_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClick();
            }
        });
        Uri uri = getIntent().getData();
        if (uri != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
            }
            try {
                if (mediaStreamer == null)
                    mediaStreamer = new HttpMediaStreamer(uri.getPath(), MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uri.toString())));
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, mediaStreamer.getUri());
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                seekBar.setMax(mediaPlayer.getDuration());
                seekBarTime.setText(convertIntToTime(mediaPlayer.getDuration()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Undefined error please try again later", Toast.LENGTH_SHORT).show();
            finish();
        }
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                seekChange(v);
                return false;
            }
        });
    }

    // This is event handler for buttonClick event
    private void buttonClick() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                secure_frame_image.setImageResource(R.mipmap.ic_pause);
            } else {
                mediaPlayer.pause();
                secure_frame_image.setImageResource(R.mipmap.ic_play1);
            }
            try {
                startPlayProgressUpdater();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void startPlayProgressUpdater() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            seekBarTime.setText(getTimeDiff(mediaPlayer));
            if (mediaPlayer.isPlaying()) {
                Runnable notification = new Runnable() {
                    public void run() {
                        startPlayProgressUpdater();
                    }
                };
                handler.postDelayed(notification, 1000);
            } else {
                mediaPlayer.pause();
            }
        }
    }

    private void seekChange(View v) {
        if (mediaPlayer.isPlaying()) {
            SeekBar sb = (SeekBar) v;
            mediaPlayer.seekTo(sb.getProgress());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaStreamer != null)
            mediaStreamer.destroy();
        if (mediaPlayer != null)
            mediaPlayer.stop();
    }

    private String getTimeDiff(MediaPlayer player) {
        String result = "";
        if (player != null) {
            result = convertIntToTime(player.getDuration()) + "/" + convertIntToTime(player.getCurrentPosition());
        }
        return result;
    }

    private String convertIntToTime(int time) {
        String result = "";
        if (time > 0) {
            int milli = time % 1000;
            if (milli > 0) {
                int sec = (time / 1000) % 60;
                if (sec > 0) {
                    int min = (time / (60 * 1000)) % 60;
                    if (min > 0) {
                        int hr = (time / (60 * 60 * 1000)) % 24;
                        if (hr > 0) {
                            int day = (time / (24 * 60 * 60 * 1000));
                            if (day > 0) {
                                result = day + ":" + hr + ":" + (min > 9 ? min : "0" + min) + ":" + (sec > 9 ? sec : "0" + sec);
                            } else {
                                result = hr + ":" + (min > 9 ? min : "0" + min) + ":" + (sec > 9 ? sec : "0" + sec);
                            }
                        } else {
                            result = (min > 9 ? min : "0" + min) + ":" + (sec > 9 ? sec : "0" + sec);
                        }
                    } else {
                        result = "00:" + (sec > 9 ? sec : "0" + sec);
                    }
                } else {
                    result = "00:00";
                }
            } else {
                result = "00:00";
            }
        }
        return result;
    }
}
