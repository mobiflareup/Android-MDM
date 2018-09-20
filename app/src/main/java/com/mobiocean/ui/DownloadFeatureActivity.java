package com.mobiocean.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;

import com.mobiocean.R;
import com.mobiocean.util.DeBug;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

public class DownloadFeatureActivity extends Activity {

    private String APKFilePath = Environment.getExternalStorageDirectory().getPath() + "/mobiOcean.apk";
    private ProgressDialog prgDialog;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloadfeatureactivity);
        String URL = MobiApplication.CONTACT_SERVER + "Api.aspx";
        File mFile = new File(APKFilePath);
        if (mFile.exists()) {
            mFile.delete();
        }
        prgDialog = new ProgressDialog(DownloadFeatureActivity.this);
        prgDialog.setMessage("Downloading file. Please wait...");
        prgDialog.setIndeterminate(false);
        prgDialog.setMax(100);
        prgDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        prgDialog.setCancelable(false);
        new DownloadApk().execute(URL);
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
        }
        return true;
    }

    class DownloadApk extends AsyncTask<String, String, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (prgDialog != null)
                                prgDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean corruptedApkFile = false;
            try {
                new JarFile(APKFilePath);
            } catch (Exception ex) {
                corruptedApkFile = true;
            }

            try {
                if (!corruptedApkFile) {
                    File apkfile = new File(APKFilePath);
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    installIntent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
                    startActivity(installIntent);
                } else {
                    File mFile = new File(APKFilePath);
                    if (mFile.exists()) {
                        mFile.delete();
                    }
                }
                DownloadFeatureActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            int count;
            try {
                URL url = new URL(strings[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);
                OutputStream output = new FileOutputStream(APKFilePath);

                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (prgDialog != null)
                                prgDialog.show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void onProgressUpdate(final String... progress) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (prgDialog != null)
                            prgDialog.setProgress(Integer.parseInt(progress[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}