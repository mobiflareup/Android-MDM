package org.conveyance.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobiocean.R;

import org.conveyance.configuration.RConstant;
import org.conveyance.configuration.RHelper;
import org.conveyance.configuration.RSharedData;
import org.conveyance.database.RStartStopLocation;
import org.conveyance.model.RControlModel;
import org.conveyance.services.RGetTowerLocationService;
import org.conveyance.services.RUploadDetailsService;
import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;
import org.sn.location.NetworkUtil;
import org.sn.util.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          21-09-16         Initial creation
 ****************************************************************************/

public class RVisitActivity extends AppCompatActivity implements View.OnClickListener, RCustomDialog.DialogResponse {
    private EditText remarkEtxt, uploadfile;
    private Context context;
    private RSharedData settings;
    private RHelper helper = new RHelper();
    private String notify = "";
    private String galleryPath = "";
    private LocationDetails locationDetails;
    private LocationBean locationBean;
    private RStartStopLocation startStopLocation;
    boolean checkVisit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_visit_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        context = RVisitActivity.this;
        settings = new RSharedData(context);
        startStopLocation = new RStartStopLocation(context);
        TextView nameTxt = (TextView) findViewById(R.id.nameTxt);
        remarkEtxt = (EditText) findViewById(R.id.remarkEtxt);
        Button update_btn = (Button) findViewById(R.id.update_btn);
        Button visitend_btn = (Button) findViewById(R.id.visitend_btn);
        uploadfile = (EditText) findViewById(R.id.uploadfile);
        uploadfile.setClickable(true);
        if (settings.getCustomerName() != null) {
            nameTxt.setText(settings.getCustomerName());
        }
        update_btn.setOnClickListener(this);
        visitend_btn.setOnClickListener(this);
        uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_btn:
                collectVisitDetails();
                break;
            case R.id.visitend_btn:
                notify = "visitend";
                if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                    if (!TextUtils.isEmpty(remarkEtxt.getText().toString()) || !TextUtils.isEmpty(galleryPath)) {
                        alertDialogUpdate();
                    } else {
                        stopConveyanceDetails();
                    }
                } else {
                    if (!TextUtils.isEmpty(remarkEtxt.getText().toString()) || !TextUtils.isEmpty(galleryPath)) {
                        alertDialogUpdate();
                    } else {
                        stopConveyanceDetails();
                        dialogClickAction();
                    }
                }
                break;
        }
    }

    @Override
    public void response(String response) {
        if ("TRIPEND".equalsIgnoreCase(response)) {
            notify = "tripend";
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                stopConveyanceDetails();
            } else {
                stopConveyanceDetails();
                dialogClickAction();
            }
        }
        if ("NEXTCUSTOMER".equalsIgnoreCase(response)) {
            notify = "newcustomer";
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                stopConveyanceDetails();
            } else {
                stopConveyanceDetails();
                dialogClickAction();
            }

        }
        if ("RETURN".equalsIgnoreCase(response)) {
            remarkEtxt.setText("");
            uploadfile.setText("Choose File");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.r_visit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remark:
                startActivity(new Intent(context, RRemarksActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void dialogAction() {
        RCustomDialog cdd = new RCustomDialog(RVisitActivity.this);
        cdd.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cdd.setCanceledOnTouchOutside(false);
        cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cdd.show();
    }

    private void stopConveyanceDetails() {
        try {
            locationDetails = new LocationDetails(context);
            locationBean = locationDetails.getLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        controlModel.setMCC(locationBean.MCC);
        controlModel.setLAC(locationBean.LAC);
        controlModel.setMNC(locationBean.MNC);
        controlModel.setCellId(locationBean.CellId);
        controlModel.setIsLogin("3");
        startStopLocation.insertLocation(controlModel);
        if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
            startService(new Intent(context, RUploadDetailsService.class));
        }
        dialogClickAction();
    }

    private void dialogClickAction() {
        settings.setStatus(false);
        if (Constants.isMyServiceRunning(context, RGetTowerLocationService.class)) {
            Intent destroyMe = new Intent(context, RGetTowerLocationService.class);
            context.stopService(destroyMe);
        }
        if ("tripend".equalsIgnoreCase(notify)) {
            finish();
        }
        if ("newcustomer".equalsIgnoreCase(notify)) {
            startActivity(new Intent(context, RMainActivity.class));
            finish();
        }
        if ("visitend".equalsIgnoreCase(notify)) {
            finish();
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (android.os.Build.VERSION.SDK_INT > 23) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                            Uri photoURI = null;
                            photoURI = FileProvider.getUriForFile(
                                    context, context.getApplicationContext()
                                            .getPackageName() + ".provider", f);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(takePictureIntent, 1);
                        }
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                        startActivityForResult(intent, 1);
                    }
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), bitmapOptions);
                    String folder_main = "MOBI";
                    File f1 = new File(Environment.getExternalStorageDirectory(), folder_main);
                    if (!f1.exists()) {
                        f1.mkdirs();
                    }
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "MOBI";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    uploadfile.setText(file.toString().substring(file.toString().lastIndexOf("/") + 1));
                    try {
                        outFile = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                        galleryPath = file.toString();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {
                try {
                    Uri selectedImage = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImage);
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "MOBI";
                    File file = new File(path);
                    if (!file.exists())
                        file.mkdir();
                    path = path + File.separator + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    if (RConstant.saveImageBitmap(bitmap, path)) {
                        galleryPath = path;
                        uploadfile.setText(path.substring(path.lastIndexOf("/") + 1));
                    }
                } catch (Exception e) {
                    galleryPath = "";
                }
            }
        }
    }

    private void collectVisitDetails() {
        String remarks = remarkEtxt.getText().toString();
        if (!TextUtils.isEmpty(remarks)) {
            try {
                locationDetails = new LocationDetails(context);
                locationBean = locationDetails.getLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            controlModel.setMCC(locationBean.MCC);
            controlModel.setLAC(locationBean.LAC);
            controlModel.setMNC(locationBean.MNC);
            controlModel.setCellId(locationBean.CellId);
            controlModel.setRemark(remarks);
            controlModel.setIsLogin("4");
            controlModel.setFilePath(galleryPath);
            controlModel.setVisitId("0");
            startStopLocation.insertLocation(controlModel);
            galleryPath = ""; //SIVA
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                startService(new Intent(context, RUploadDetailsService.class));
            }
            if (!checkVisit)
                dialogAction();
            else {
                checkVisit = false;
                stopConveyanceDetails();
            }
        } else {
            remarkEtxt.setError("Enter Remarks");
        }
    }

    private void alertDialogUpdate() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Are you update remarks");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        checkVisit = true;
                        collectVisitDetails();
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!TextUtils.isEmpty(remarkEtxt.getText().toString()) || !TextUtils.isEmpty(galleryPath)) {
                            stopConveyanceDetails();
                        } else {
                            stopConveyanceDetails();
                            dialogClickAction();
                        }
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}