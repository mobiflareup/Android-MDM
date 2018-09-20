package org.conveyance.main;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RequestActivity;

import org.conveyance.configuration.RHelper;
import org.conveyance.configuration.RSharedData;
import org.conveyance.database.RCustomerListTableDB;
import org.conveyance.database.RModeTravelTable;
import org.conveyance.database.RStartStopLocation;
import org.conveyance.model.RControlModel;
import org.conveyance.model.RCustomerModel;
import org.conveyance.model.RModeModel;
import org.conveyance.services.RGetTowerLocationService;
import org.conveyance.services.RUploadDetailsService;
import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;
import org.sn.location.NetworkUtil;
import org.sn.util.Constants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RMainActivity extends RequestActivity {

    private Spinner customarname_spinner, modeoftravel_spinner;
    private Context context;
    private String customerNameSpin;
    private static String modeId, CustomerId;
    private ArrayList<RModeModel> selectedModeModels;
    private ArrayList<RCustomerModel> selectedcustomerModel;
    private RSharedData settings;
    private RHelper helper = new RHelper();
    private RStartStopLocation startStopLocation;
    private RModeTravelTable modeTravelTable;
    private RCustomerListTableDB customerListTableDB;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_activity_main);

        requestAppPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK}, 10);

        context = RMainActivity.this;
        startStopLocation = new RStartStopLocation(context);
        modeTravelTable = new RModeTravelTable(context);
        customerListTableDB = new RCustomerListTableDB(context);
        handler = new Handler();
        settings = new RSharedData(context);
        settings.setAppId(CallHelper.Ds.structPC.iStudId);
        if (settings.getStatus()) {
            if (!Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
                startService(new Intent(context, RGetTowerLocationService.class));
            }
            startActivity(new Intent(context, RVisitActivity.class));
            finish();
        } else {
            initializeControl();
        }

    }

    private void initializeControl() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        customarname_spinner = (Spinner) findViewById(R.id.customarname_spinner);
        modeoftravel_spinner = (Spinner) findViewById(R.id.modeoftravel_spinner);
        Button startbtn = (Button) findViewById(R.id.startbtn);
        selectedModeModels = new ArrayList<>();
        selectedcustomerModel = new ArrayList<>();
        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                int playServiceStatus = googleApiAvailability.isGooglePlayServicesAvailable(context);
                DeBug.ShowLog("FLNS", "PlayService Version : " + playServiceStatus);
                if (playServiceStatus == ConnectionResult.SUCCESS) {
                    StartTrip();
                } else if (playServiceStatus == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
                    AlertDialog.Builder dialogue = new AlertDialog.Builder(context);
                    dialogue.setTitle("Alert!")
                            .setMessage("Google play service is out of date. Please update your Google play service to proceed")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final String appPackageName = "com.google.android.gms";
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (android.content.ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    AlertDialog alertDialog = dialogue.create();
                    alertDialog.show();
                }
            }
        });

        selectedModeModels = modeTravelTable.getModes();
        selectedcustomerModel = customerListTableDB.getAllCustomer();

        if (selectedcustomerModel != null && selectedModeModels != null && selectedcustomerModel.size() > 0 && selectedModeModels.size() > 0) {
            loadLocalMode();
            loadLocalCustomer();
        } else {
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                loadCustomerAndMode();
            } else {
                Toast.makeText(context, "No Internet!", Toast.LENGTH_SHORT).show();
            }
        }

        modeoftravel_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos > 0) {
                    String name = parent.getItemAtPosition(pos).toString();
                    for (RModeModel modeModel : selectedModeModels) {
                        if (name.equalsIgnoreCase(modeModel.getModeOfTravel())) {
                            modeId = String.valueOf(modeModel.getModeId());
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        customarname_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                if (pos > 0) {
                    String name = parent.getItemAtPosition(pos).toString();
                    for (RCustomerModel customerModel : selectedcustomerModel) {
                        if (name.equalsIgnoreCase(customerModel.getCustomerName())) {
                            CustomerId = String.valueOf(customerModel.getCustomerId());
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }

    private void StartTrip() {
        if (isValidData()) {
            try {
                LocationDetails locationDetails = new LocationDetails(context);
                final LocationBean locationBean = locationDetails.getLocation();
                RControlModel controlModel = new RControlModel();
                controlModel.setAppId(settings.getAppId());
                controlModel.setLatitude(locationBean.Lat);
                controlModel.setLongitude(locationBean.Longt);
                controlModel.setAccuracy(locationBean.Accuracy);
                controlModel.setAltitude(locationBean.Altitude);
                controlModel.setBearing(locationBean.Bearing);
                controlModel.setElapsedRealtimeNanos(locationBean.ElapsedRealTimeNanos);
                controlModel.setProvider(locationBean.Provider);
                controlModel.setSpeed(locationBean.Speed);
                controlModel.setTime(locationBean.Time);
                controlModel.setLogDateTime(helper.dateTime());
                controlModel.setIsLogin("1");
                controlModel.setMCC(locationBean.MCC);
                controlModel.setLAC(locationBean.LAC);
                controlModel.setMNC(locationBean.MNC);
                controlModel.setCellId(locationBean.CellId);
                controlModel.setCustomerId(CustomerId);
                controlModel.setModeOfTravel(modeId);
                controlModel.setVisitId("0");
                startStopLocation.insertLocation(controlModel);
                AlertDialog.Builder dialogue = new AlertDialog.Builder(context);
                dialogue.setTitle("Alert!")
                        .setMessage("Please ensure that your GPS is turned for accurate conveyance calculation.")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                settings.setStatus(true);
                                settings.setCustomerName(customerNameSpin);
                                if (!Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
                                    startService(new Intent(context, RGetTowerLocationService.class));
                                }
                                if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                                    startService(new Intent(context, RUploadDetailsService.class));
                                }
                                Intent intent = new Intent(context, RVisitActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                AlertDialog alertDialog = dialogue.create();
                alertDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.r_menus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remark:
                startActivity(new Intent(context, RRemarksActivity.class));
                break;
            case R.id.refresh:
                if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                    loadCustomerAndMode();
                } else {
                    Toast.makeText(context, "Check Your Internet !", Toast.LENGTH_SHORT).show();
                    loadLocalMode();
                    loadLocalCustomer();
                }
                break;
            case R.id.add_customer:
                startActivity(new Intent(context, RAddCustomerActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadCustomerAndMode() {
        String appId = settings.getAppId();
        if (appId != null && !appId.isEmpty()) {

            Call<ArrayList<RCustomerModel>> call = helper.getInterface().getCustomer(appId);
            call.enqueue(new Callback<ArrayList<RCustomerModel>>() {
                @Override
                public void onResponse(Call<ArrayList<RCustomerModel>> call, Response<ArrayList<RCustomerModel>> response) {
                    if (response != null) {
                        ArrayList<RCustomerModel> result = response.body();
                        if (result != null && result.size() > 0) {
                            customerListTableDB.Delete_Table();
                            ArrayList<String> stringArrayList = new ArrayList<>();
                            selectedcustomerModel = result;
                            customerListTableDB.insertCustomers(selectedcustomerModel, settings.getAppId());
                            stringArrayList.add("Select Customer Name");
                            for (RCustomerModel customerModel : result) {
                                stringArrayList.add(customerModel.getCustomerName());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.r_spinner_dropdown, stringArrayList);
                            customarname_spinner.setAdapter(adapter);
                        } else {
                            showErrorToast();
                        }
                    } else {
                        showErrorToast();
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<RCustomerModel>> call, Throwable t) {
                    showErrorToast();
                }
            });

            Call<ArrayList<RModeModel>> call1 = helper.getInterface().getMode(appId);
            call1.enqueue(new Callback<ArrayList<RModeModel>>() {
                @Override
                public void onResponse(Call<ArrayList<RModeModel>> call, Response<ArrayList<RModeModel>> response) {
                    if (response != null) {
                        ArrayList<RModeModel> result = response.body();
                        if (result != null && result.size() > 0) {
                            modeTravelTable.Delete_Table();
                            ArrayList<String> modeStrArr = new ArrayList<>();
                            selectedModeModels = result;
                            modeTravelTable.insertMode(selectedModeModels);
                            modeStrArr.add("Select Mode of Travel");
                            for (RModeModel modeModel : result) {
                                modeStrArr.add(modeModel.getModeOfTravel());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.r_spinner_dropdown, modeStrArr);
                            modeoftravel_spinner.setAdapter(adapter);
                        } else {
                            showErrorToast();
                        }
                    } else {
                        showErrorToast();
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<RModeModel>> call, Throwable t) {
                    showErrorToast();
                }
            });
        }
    }

    private void showErrorToast() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Unable to fetch details try again!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidData() {
        boolean isValid = true;
        try {
            customerNameSpin = customarname_spinner.getSelectedItem().toString();
            String modeSpin = modeoftravel_spinner.getSelectedItem().toString();
            if ("Select Customer Name".equalsIgnoreCase(customerNameSpin)) {
                Toast.makeText(context, "Select Customer", Toast.LENGTH_SHORT).show();
                isValid = false;
            } else if ("Select Mode of Travel".equalsIgnoreCase(modeSpin)) {
                Toast.makeText(context, "Select Mode", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    private void loadLocalMode() {
        ArrayList<String> modeStrArr = new ArrayList<>();
        modeStrArr.add("Select Mode of Travel");
        for (RModeModel modeModel : selectedModeModels) {
            modeStrArr.add(modeModel.getModeOfTravel());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.r_spinner_dropdown, modeStrArr);
        modeoftravel_spinner.setAdapter(adapter);
    }

    private void loadLocalCustomer() {
        ArrayList<String> stringArrayList = new ArrayList<>();
        stringArrayList.add("Select Customer Name");
        for (RCustomerModel customerModel : selectedcustomerModel) {
            stringArrayList.add(customerModel.getCustomerName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, R.layout.r_spinner_dropdown, stringArrayList);
        customarname_spinner.setAdapter(adapter);
    }

}