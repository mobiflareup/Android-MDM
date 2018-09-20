package com.mobiocean.ui;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobiocean.R;
import com.mobiocean.service.AppBlock;
import com.mobiocean.service.CallDetectService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.Constant;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RequestActivity;
import com.mobiocean.util.RestApiCall;

import org.json.JSONObject;
import org.sn.util.Constants;

import static com.mobiocean.util.Constant.COUNTRY_CODE;
import static com.mobiocean.util.Constant.COUNTRY_INDEX;
import static com.mobiocean.util.Constant.GCM_SERVER_URL;

public class ActivationActivity extends RequestActivity {

    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;
    public SharedPreferences.Editor editor;

    EditText etName;
    EditText etPhNum;
    EditText etEmail;

    Button bLogin;

    //static String stName = "";
    static String stPhNum = "";
    //static String stEmail = "";

    protected static AlertDialog.Builder alertFFC;
    protected static AlertDialog adFFC;

    //Please Check Filds
    protected static AlertDialog.Builder alertPCF;
    protected static AlertDialog adPCF;

    //Wrong info inserted
    protected static AlertDialog.Builder alertWII;
    protected static AlertDialog adWII;

    Context mContext;

    static boolean ringing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        mContext = ActivationActivity.this;
        Constant.Init("mobi");        // For Nimboli Of Saral Server               #GS
        //		Constant.Init(Country);	   //  For Nimboli of Our Server  aanwla.com      #GS

        {
            editor.putString("COUNTRY_CODE", COUNTRY_CODE);
            editor.putString("GCM_SERVER_URL", GCM_SERVER_URL);
            editor.putInt("COUNTRY_INDEX", COUNTRY_INDEX);
            editor.commit();
        }
        /*		final  Intent intentsmssend = new Intent(getBaseContext(), SMSSend.class);
		startService(intentsmssend);
		 */
        setContentView(R.layout.fragment_profile);
//		wipingSdcard();
        //Assign from .xml/r.id
		/*		tLoginHead=(TextView)findViewById(R.id.txt_login);
		tEmpId=(TextView)findViewById(R.id.txt_nimboliEmp_id);
		tPhNum=(TextView)findViewById(R.id.txt_PhNum);	
		tCompCode=(TextView)findViewById(R.id.txt_nimboli_com_id);*/

        TextView registration = (TextView) findViewById(R.id.registration);
        registration.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivationActivity.this, RegistrationActivity.class);
                startActivity(i);
            }
        });

     /*   etName = (EditText) findViewById(R.id.editName);
        etEmail = (EditText) findViewById(R.id.editEmail);*/
        etPhNum = (EditText) findViewById(R.id.editPhone);
        bLogin = (Button) findViewById(R.id.btnSendOTP);

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

        {
            COUNTRY_CODE = settings.getString("COUNTRY_CODE", COUNTRY_CODE);
            GCM_SERVER_URL = settings.getString("GCM_SERVER_URL", GCM_SERVER_URL);
            COUNTRY_INDEX = settings.getInt("COUNTRY_INDEX", COUNTRY_INDEX);
        }

        bLogin.setText("Activate");
        bLogin.setOnClickListener(listn_login);

        //Alart for PCF
        alertPCF = new AlertDialog.Builder(ActivationActivity.this);
        alertPCF.setTitle("Check Fields");
        alertPCF.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }

        });

        //Alart for FFC
        alertFFC = new AlertDialog.Builder(ActivationActivity.this);
        alertFFC.setTitle("Check Fields");
        alertFFC.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //launch ring dialog for checking and varifying entered filds
                adFFC.dismiss();
                new RegisterUser().execute("");
            }

        });
        alertFFC.setNegativeButton("No", null);
        adFFC = alertFFC.create();

        alertWII = new AlertDialog.Builder(ActivationActivity.this);
        //		alertWII.setTitle(" ");
        alertWII.setCancelable(false);
        alertWII.setMessage("Please Check the Information you have Inserted OR \nCheck your Internet Connection ");
        alertWII.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }

        });
        adWII = alertWII.create();
    }

    OnClickListener listn_login = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub
            //stName = etName.getText().toString();
            //stEmail = etEmail.getText().toString();
            stPhNum = etPhNum.getText().toString();

        /*    if (TextUtils.isEmpty(stEmail)) {
                alertPCF.setMessage("Please check eMail-id ");
                adPCF = alertPCF.create();
                if (!adPCF.isShowing())
                    adPCF.show();
            } else if (stName.equals("") || stName == null) {
                alertPCF.setMessage("Please check Name. ");
                adPCF = alertPCF.create();
                if (!adPCF.isShowing())
                    adPCF.show();
            } else*/ if (stPhNum.equals("") || stPhNum == null) {
                alertPCF.setMessage("Please Enter Valid Mobile Number. ");
                adPCF = alertPCF.create();
                if (!adPCF.isShowing())
                    adPCF.show();

            } else {
                alertFFC.setMessage("Mobile Number : " + stPhNum + "" +
                        "\n Is this Information Correct ?");
                adFFC = alertFFC.create();
                if (!adFFC.isShowing())
                    adFFC.show();
            }
        }

    };

    @Override
    protected void onPermissionsGranted(int requestCode) {
        if (requestCode == 10) {
            bLogin.setEnabled(true);
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            //Narayanan S STARTS
            Context context = getBaseContext();
            if(!Constants.isMyServiceRunning(context, CallDetectService.class)){
                startService(new Intent(context, CallDetectService.class));
            }
            if(!Constants.isMyServiceRunning(context, AppBlock.class)){
                startService(new Intent(context, AppBlock.class));
            }
            //Narayanan S ENDS
        }
    }


    private class RegisterUser extends AsyncTask<String, Void, Integer> {
        private ProgressDialog ringProgressDialog;
        private String loginResp = "1";
        @Override
        protected void onPreExecute() {
            ringProgressDialog = ProgressDialog.show(ActivationActivity.this, "Please wait ...", "Loading ...", true);
            ringProgressDialog.setCancelable(false);
        }

        @Override
        protected Integer doInBackground(String... params) {
            //  ringProgressDialog.show();
            try {
                RestApiCall mRestApiCall = new RestApiCall();
                JSONObject jsonobj = new JSONObject();
                //jsonobj.put("ClientCode", stName);//stEmail);//"Gngr-1"
                //jsonobj.put("EmpCompanyId", stEmail);//stName);//"Demo1"
                jsonobj.put("MobileNo", stPhNum);//stPhNum);//"9579543434"//"9958421037"
                DeBug.ShowLog("chkemp", " chkemp json data : " + jsonobj);
                loginResp = mRestApiCall.ChkEmp(jsonobj);
                DeBug.ShowLog("chkemp", " chkemp response : " + loginResp);
                if (!loginResp.equals("-1") && !loginResp.equals("0")) {
                    //CallHelper.Ds.structCCC.stEmpID = stName;
                    CallHelper.Ds.structPC.stPhoneNo = stPhNum;
                    //DeBug.ShowLog("NIMBOLI", "" + stName);
                    editor.putString("structPC.stPhoneNo", CallHelper.Ds.structPC.stPhoneNo);
                    editor.commit();
                    return 3;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        protected void onPostExecute(Integer resultInt) {
            ringProgressDialog.dismiss();
            if (resultInt == 3) {
                DeBug.ShowLog("TestingApp", "demoDeviceAdmin  asctivated");
                Intent intentLaunch = new Intent(ActivationActivity.this, OTP.class);
                intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                //intentLaunch.putExtra("ClientCode", stEmail);
                //intentLaunch.putExtra("EmpCompanyId", stName);
                intentLaunch.putExtra("MobileNo", stPhNum);
                intentLaunch.putExtra("loginResp", loginResp);
                startActivity(intentLaunch);
                finish();
            } else if (resultInt == 2 || resultInt == 0) {
                ringing = false;
                alertWII.setTitle("Installation InComplete.");
                alertWII.setMessage("Please Check the Information you have Inserted OR \nCheck your Internet Connection");
                adWII = alertWII.create();
                if (!adWII.isShowing())
                    adWII.show();
            }
        }
    }

    @Override
    protected void onResume() {
        try {
            CallHelper.isAppForground = false;
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            {
                COUNTRY_CODE = settings.getString("COUNTRY_CODE", COUNTRY_CODE);
                GCM_SERVER_URL = settings.getString("GCM_SERVER_URL", GCM_SERVER_URL);
                COUNTRY_INDEX = settings.getInt("COUNTRY_INDEX", COUNTRY_INDEX);
            }
            try {
                if (PassWordActivity.mPassWordActivity != null) {
                    PassWordActivity.mPassWordActivity.finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
            if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId)) {
                DeBug.ShowLog("TestingApp", "demoDeviceAdmin  activated");
                Intent intentLaunch = new Intent(ActivationActivity.this, Home.class);
                intentLaunch.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                CallHelper.isAppForground = true;
                startActivity(intentLaunch);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }


}
