package com.mobiocean.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.mobiocean.BuildConfig;
import com.mobiocean.R;
import com.mobiocean.service.SyncIntentService;
import com.mobiocean.util.AanwlaService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DataStructure;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RequestActivity;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.SOSContacts.SosContactsClass;
import com.mobiocean.util.UpdateFromServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.sn.beans.AppGroupBean;
import org.sn.beans.ContactListBean;
import org.sn.beans.EnabledExtraFeatureBean;
import org.sn.beans.ProfileInfoBean;
import org.sn.beans.SensorListBean;
import org.sn.beans.VpnStatusBean;
import org.sn.beans.WebListBean;
import org.sn.database.AppGroupTable;
import org.sn.database.ContactListTable;
import org.sn.database.MobiProfileDetailTable;
import org.sn.database.MobiProfileInfoTable;
import org.sn.database.SensorListTable;
import org.sn.database.WebListTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OTP extends RequestActivity {
    private Toolbar toolbar;
    private Button btnNext;
    private EditText editOtp;

    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    protected static final String BLOCKED_WEBSITES_PREFS_NAME = "blockedWebsitesPref";
    public static SharedPreferences blockedWebsitesPreferences;
    public static SharedPreferences.Editor blockedWebsitesPreferenceEditor;

    String stPhNum="", stName="", stEmail="", stPhNumPrev="";

    //Wrong info inserted
    protected static AlertDialog.Builder alertWII;
    protected static AlertDialog adWII;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.activity_otp);

        editOtp = (EditText) findViewById(R.id.editOtp);
        btnNext = (Button) findViewById(R.id.btnNext);
        final Button new_otp = (Button) findViewById(R.id.new_otp);

        Bundle bundle = getIntent().getExtras();
        stEmail = bundle.getString("ClientCode");
        stName = bundle.getString("EmpCompanyId");
        stPhNumPrev = bundle.getString("MobileNo");
        String setUserInput = bundle.getString("loginResp");
        if (!setUserInput.equals("1")) {
            editOtp.setEnabled(false);
            btnNext.setVisibility(View.GONE);
        }

        requestAppPermissions(new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_TASKS, Manifest.permission.MODIFY_AUDIO_SETTINGS}, 10);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.System.canWrite(getBaseContext())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        else
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        editor = settings.edit();

        blockedWebsitesPreferences = getSharedPreferences(BLOCKED_WEBSITES_PREFS_NAME, Context.MODE_PRIVATE );
        blockedWebsitesPreferenceEditor = blockedWebsitesPreferences.edit();

        stPhNum = settings.getString("structPC.stPhoneNo", CallHelper.Ds.structPC.stPhoneNo);

        String otp = settings.getString("getOtp", "");
        editOtp.setText(otp);
        editOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textCount = s.length();
                if (textCount == 6) {
                    new checkOTP().execute();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new checkOTP().execute();
            }
        });

        resendOtp(new_otp);

        new_otp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new ResetOTP().execute();
                resendOtp(new_otp);
            }
        });

        new_otp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editOtp.setEnabled(true);
                return true;
            }
        });

        alertWII = new AlertDialog.Builder(OTP.this);
        //		alertWII.setTitle(" ");
        alertWII.setCancelable(false);
        alertWII.setMessage("Please Check the entered OTP OR \nCheck your Internet Connection ");
        alertWII.setPositiveButton("OK", null);
        adWII = alertWII.create();


        super.onCreate(savedInstanceState);


    }

    public void resendOtp(final View view) {
        try {
            view.setVisibility(View.GONE);
            int RESEND_VISIBLE_TIME = 20000;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.setVisibility(View.VISIBLE);
                }
            }, RESEND_VISIBLE_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPermissionsGranted(int requestCode) {
        if (requestCode == 10) {
            btnNext.setEnabled(true);
        }
    }
/*
    private void initializeControls() {
		// TODO Auto-generated method stub
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		btnNext = (Button) findViewById(R.id.btnNext);
	}*/

    class ResetOTP extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OTP.this, "Please wait ...", "Loading ...", true);
            progressDialog.setCancelable(false);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                RestApiCall mRestApiCall = new RestApiCall();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("ClientCode", stName);
                jsonobj.put("EmpCompanyId", stEmail);
                jsonobj.put("MobileNo", stPhNumPrev);
                DeBug.ShowLog("chkemp", " chkemp json data : " + jsonobj);
                String responce = mRestApiCall.ChkEmp(jsonobj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
        }

    }

    class checkOTP extends AsyncTask<String, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(OTP.this, "Please wait ...", "Loading ...", true);
            progressDialog.setCancelable(false);
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int responce1 = 0;
            RestApiCall mRestApiCall = new RestApiCall();

            try {
                String OTP = editOtp.getText().toString();
                //	WebserviceCall WSC = new WebserviceCall();
                //	WSC.getSweeperListFromServer("AAP-71", 6);
                //	String responce = WSC.CheckParentReg(stName,stPhNum);
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("ClientCode", stName);
                jsonobj.put("MobileNo", stPhNum);//stEmail);//"Gngr-1"
                jsonobj.put("OTP", OTP);//stName);//"Demo1"
                String result = mRestApiCall.ChkOTP(jsonobj);

                if (TextUtils.isEmpty(result)) {
                    return 0;
                } else if (result.contains("MDM")) {
                    CallHelper.Ds.structPC.iStudId = result;
                    CallHelper.Ds.structWCC.lAllowedTime = new long[7][80][24];
                    CallHelper.Ds.structCCC.wFeatureControlWord[0] = 0L;
                    CallHelper.runTime = null;
                    editor.putString("CallHelper.runTime", "");

                    Retrofit retrofit = new Retrofit.Builder().baseUrl(MobiApplication.CONTACT_SERVER).addConverterFactory(GsonConverterFactory.create()).build();
                    AanwlaService webInterface = retrofit.create(AanwlaService.class);

                    Call<String> call1 = webInterface.checkSubscription(CallHelper.Ds.structPC.iStudId);
                    call1.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            String s = response.body();
                            DeBug.ShowLog("Expire", "Received result " + s);
                            if (s == null || !s.equals("-1")) {
                                CallHelper.decodeMessage(getBaseContext(), s);
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            DeBug.ShowLog("Expire", "Received result " + t.getMessage());
                        }
                    });
                    {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("AndroidAppId", CallHelper.Ds.structPC.iStudId);
                            json.put("isUninstalled", 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String stpassword = mRestApiCall.setAppInstallationStatus(json);
                    }
                    editor.putString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
                    editor.commit();

                    //Narayanan Fetch Update MobiOcean Starts
                    {
                        //Send Values
                        responce1 = UpLoadAppsData.run(getApplicationContext());
                        String lockPin = mRestApiCall.getLock((MobiApplication) getApplication());
                        editor.putString("LockPin", lockPin);
                        editor.commit();
                        //Send Some ack need to sort out why this happens?
                        {
                            long CurrentTime = System.currentTimeMillis();
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(CurrentTime);
                            DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                            String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
                            UpdateFromServer mfromServer = new UpdateFromServer();
                            mfromServer.setAckDateTime(SMSTimeStamp);
                            editor.putString("updateTime", "" + SMSTimeStamp);
                            editor.commit();
                            mfromServer.setAppId(CallHelper.Ds.structPC.iStudId);
                            mRestApiCall.postUpdateAck((MobiApplication) getApplication(), mfromServer);
                        }
                        //SOS stored in shared storage wrong concept change to database storage.
                        {
                            ArrayList<SosContactsClass> contactList = mRestApiCall.getSosContacts((MobiApplication) getApplication(), CallHelper.Ds.structPC.iStudId);
                            if (contactList != null && !contactList.isEmpty()) {
                                ArrayList<String> listContact = new ArrayList<String>(contactList.size());

                                for (int i = 0; i < contactList.size(); i++)
                                    listContact.add(contactList.get(i).getMobileNo());
                                saveToPrefarence(listContact);
                            }
                        }
                        JSONObject json3 = new JSONObject();
                        try {
                            json3.put("StuMob", "1");
                            json3.put("countryId", 1);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String p = mRestApiCall.RT(json3);
                        if (p != null && !p.isEmpty()) {
                            CallHelper.decodeMessage(getBaseContext(), p);
                        }
                        startService(new Intent(OTP.this, SyncIntentService.class));
                    }
                    if (responce1 != -2)
                        return 3;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            return responce1;

        }

        @Override
        protected void onPostExecute(Integer result) {
            try {
                progressDialog.dismiss();
                if (result == 2 || result == 0) {
                    alertWII.setTitle("Installation InComplete.");
                    alertWII.setMessage("Please Check the Information you have Inserted OR \nCheck your Internet Connection");
                    adWII = alertWII.create();
                    if (!adWII.isShowing())
                        adWII.show();
                } else {
                    DeBug.ShowLog("TestingApp", "demoDeviceAdmin  asctivated");
                    Intent intentLaunch = new Intent(OTP.this, Home.class);
                    intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    CallHelper.isAppForground = true;
                    startActivity(intentLaunch);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.onPostExecute(result);
        }

        private void saveToPrefarence(ArrayList<String> contactList) {
            editor.putInt("contactSize", contactList.size());
            for (int i = 0; i < contactList.size(); i++)
                editor.putString("contact" + i, contactList.get(i));

            editor.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String stOtp = settings.getString("getOtp", "");
        if (!stOtp.isEmpty()) {
            editOtp.setText(stOtp);
        }
    }

    BroadcastReceiver otpReciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String stOtp = intent.getStringExtra("otp");
            editOtp.setText(stOtp);
        }

    };

    @Override
    protected void onStart() {
        LocalBroadcastManager.getInstance(this).registerReceiver(otpReciever, new IntentFilter("getOtp"));

        super.onStart();
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(otpReciever);

        super.onStop();
    }

}
