package com.mobiocean.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.beans.SendAttendanceBean;
import com.mobiocean.util.AanwlaService;

import org.sn.location.LocationDetails;
import org.sn.util.Constants;
import org.sn.util.ServiceCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceActivity extends Activity {

    Button login, logout;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    final String PREFS_NAME = "MyPrefsFile";
    public static boolean isLogged = false;
    private Context context;
    Handler handler = new Handler();
    ProgressDialog progress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_attendance);
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        context = this;
        login = (Button) findViewById(R.id.login);
        logout = (Button) findViewById(R.id.logout);
        Constants.serviceResponse.put(ServiceCallback.ATTENDANCE_ACTIVITY, context);
        isLogged = settings.getBoolean("AttendanceActivity.isLogged", isLogged);
        editor.putBoolean("AttendanceActivity.isLogged", isLogged);
        editor.apply();
        if (isLogged) {
            login.setEnabled(false);
            logout.setEnabled(true);
        } else {
            login.setEnabled(true);
            logout.setEnabled(false);
        }
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setEnabled(false);
                LocationDetails locationDetails = new LocationDetails(context);
                locationDetails.attendanceActivity(1);
                startProgress();
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout.setEnabled(false);
                LocationDetails locationDetails = new LocationDetails(context);
                locationDetails.attendanceActivity(0);
                startProgress();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.serviceResponse.remove(ServiceCallback.ATTENDANCE_ACTIVITY);
    }

    public void callService(final SendAttendanceBean sendAttendanceBean) {
        AanwlaService webInterface = Constants.getService();
        Call<String> call = webInterface.getAttendance(sendAttendanceBean);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, final Response<String> response) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String result = response.body();
                        if (result != null && result.equals("1")) {
                            Toast.makeText(getBaseContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                            if (sendAttendanceBean.IsLogin.equals("1")) {
                                login.setEnabled(false);
                                logout.setEnabled(true);
                                isLogged = true;
                                editor.putBoolean("AttendanceActivity.isLogged", isLogged);
                                editor.apply();
                            } else if (sendAttendanceBean.IsLogin.equals("0")) {
                                login.setEnabled(true);
                                logout.setEnabled(false);
                                isLogged = false;
                                editor.putBoolean("AttendanceActivity.isLogged", isLogged);
                                editor.apply();
                            }
                        } else {
                            Toast.makeText(getBaseContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                            if (sendAttendanceBean.IsLogin.equals("1")) {
                                login.setEnabled(true);
                            } else if (sendAttendanceBean.IsLogin.equals("0")) {
                                logout.setEnabled(true);
                            }
                        }
                        stopProgress();
                    }
                });
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getBaseContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                        if (sendAttendanceBean.IsLogin.equals("1")) {
                            login.setEnabled(true);
                        } else if (sendAttendanceBean.IsLogin.equals("0")) {
                            logout.setEnabled(true);
                        }
                        stopProgress();
                    }
                });
            }
        });
    }

    private void startProgress() {
        try {
            if (progress == null)
                progress = new ProgressDialog(context);
            progress.setMessage("Please wait...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopProgress() {
        try {
            if (progress != null && progress.isShowing())
                progress.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
