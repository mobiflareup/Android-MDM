package com.mobiocean.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.service.OneMinuteService;
import com.mobiocean.util.CallHelper;

import org.sn.beans.ConveyanceBean;
import org.sn.database.ConveyanceLocationTable;
import org.sn.location.LocationDetails;
import org.sn.location.NetworkUtil;
import org.sn.services.UpdateConveyanceService;
import org.sn.util.Constants;
import org.sn.util.ServiceCallback;

import java.io.File;


public class Conveyance extends Activity {

    Button start, stop, send, attach;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    final String PREFS_NAME = "MyPrefsFile";
    public static boolean isStarted = false;
    public static float vehicleReading = 0;
    private String picturePath, fName;
    private TextView bug_attach_path;
    private EditText report, vehicle_reading;
    private Context context;
    private ConveyanceLocationTable conveyanceLocationTable;
    private ProgressDialog progress = null;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conveyance);

        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        editor = settings.edit();

        context = this;

        handler = new Handler();

        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.stop);
        send = (Button) findViewById(R.id.send);
        attach = (Button) findViewById(R.id.attach);
        bug_attach_path = (TextView) findViewById(R.id.bug_attach_path);
        vehicle_reading = (EditText) findViewById(R.id.vehicle_reading);
        report = (EditText) findViewById(R.id.report);
        Constants.serviceResponse.put(ServiceCallback.CONVEYANCE_ACTIVITY, context);

        conveyanceLocationTable = new ConveyanceLocationTable(context);

        isStarted = settings.getBoolean("Conveyance.isStarted", isStarted);

        vehicleReading = settings.getFloat("Conveyance.vehicleReading", vehicleReading);

        if (isStarted) {
            start.setEnabled(false);
            stop.setEnabled(true);
            send.setEnabled(true);
        } else {
            vehicleReading = 0;
            start.setEnabled(true);
            stop.setEnabled(false);
            send.setEnabled(false);
        }

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationDetails locationDetails = new LocationDetails(context);
                locationDetails.conveyanceActivity(1);
                startProgress();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float f = 0;
                try {
                    f = Float.parseFloat(vehicle_reading.getText().toString());
                } catch (Exception ignore) {
                }
                if (vehicleReading <= f) {
                    LocationDetails locationDetails = new LocationDetails(context);
                    locationDetails.conveyanceActivity(3);
                    startProgress();
                } else {
                    Toast.makeText(context, "Entered previous vehicle reading is " + vehicleReading + " please enter proper vehicle reading", Toast.LENGTH_SHORT).show();
                }
            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConveyanceBean conveyanceBean = new ConveyanceBean();
                conveyanceBean.IsLogin = "4";
                conveyanceBean.LogDateTime = CallHelper.GetTimeWithDate();
                conveyanceBean.Remark = report.getText().toString();
                conveyanceBean.ImagePath = picturePath;
                conveyanceLocationTable.insertLocation(conveyanceBean);
                bug_attach_path.setText("Select Attachment");
                report.setText("");
                picturePath = ""; //SIVA
                Toast.makeText(context, "Recorded Successfully", Toast.LENGTH_SHORT).show();
                if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                    startService(new Intent(context, UpdateConveyanceService.class));
                }
            }
        });

    }

    public void startStopConveyance(final ConveyanceBean conveyanceBean) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                stopProgress();
                if (conveyanceBean != null) {
                    conveyanceBean.Remark = report.getText().toString();
                    float f = 0;
                    try {
                        f = Float.parseFloat(vehicle_reading.getText().toString());
                        vehicleReading = !isStarted ? f : 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    conveyanceBean.VehicleReading = f;
                    conveyanceBean.ImagePath = picturePath;
                    conveyanceLocationTable.insertLocation(conveyanceBean);
                    bug_attach_path.setText("Select Attachment");
                    report.setText("");
                    picturePath = ""; //SIVA
                    vehicle_reading.setText("");
                    isStarted = conveyanceBean.IsLogin.equals("1");
                    start.setEnabled(!isStarted);
                    stop.setEnabled(isStarted);
                    send.setEnabled(isStarted);
                    OneMinuteService.isPermitted = isStarted;
                    editor.putFloat("Conveyance.vehicleReading", vehicleReading);
                    editor.putBoolean("Conveyance.isStarted", isStarted);
                    editor.apply();
                    if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                        startService(new Intent(context, UpdateConveyanceService.class));
                    }
                    if (isStarted) {
                        if (!Constants.isMyServiceRunning(context, OneMinuteService.class))
                            startService(new Intent(Conveyance.this, OneMinuteService.class));
                    } else {
                        if (Constants.isMyServiceRunning(context, OneMinuteService.class))
                            stopService(new Intent(Conveyance.this, OneMinuteService.class));
                    }
                }
            }
        });
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(Conveyance.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    if (android.os.Build.VERSION.SDK_INT > 23) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            String imageFolderPath = "/profile/image/";
                            if (CheckFolder(imageFolderPath) != 1)
                                createFolder(imageFolderPath);
                            fName = "" + System.currentTimeMillis() + ".jpg";
                            picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + imageFolderPath + fName;
                            File f = new File(picturePath);
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
                        String imageFolderPath = "/profile/image/";
                        if (CheckFolder(imageFolderPath) != 1)
                            createFolder(imageFolderPath);
                        fName = "" + System.currentTimeMillis() + ".jpg";
                        picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + imageFolderPath + fName;
                        File f = new File(picturePath);
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

    private void createFolder(String Path) {
        String SDCardLocation = Environment.getExternalStorageDirectory()
                .getAbsolutePath();

        String path_names[] = Path.split("/");

        String PathToBeCteated = SDCardLocation + "/";
        int lastFolderIndex = path_names.length - 1;
        for (int i = 0; i < path_names.length; i++) {
            PathToBeCteated = PathToBeCteated + path_names[i] + "/";
            File NewFolderCreator = new File(PathToBeCteated);

            boolean fileexist = false;
            if (!NewFolderCreator.exists()) {
                if (NewFolderCreator.mkdirs()) {
                    fileexist = true;
                }
            } else {
                fileexist = true;
            }

            if (lastFolderIndex == i && fileexist) {

            }
        }
    }

    private int CheckFolder(String Path) {

        int result = -1;
        String SDCardLocation = Environment.getExternalStorageDirectory()
                .getAbsolutePath();

        String path_names[] = Path.split("/");

        String PathToBeCteated = SDCardLocation + "/";
        int lastFolderIndex = path_names.length - 1;
        for (int i = 0; i < path_names.length; i++) {
            PathToBeCteated = PathToBeCteated + path_names[i] + "/";
            File NewFolderCreator = new File(PathToBeCteated);

            boolean fileexist = false;
            if (!NewFolderCreator.exists()) {
                fileexist = false;
            } else {
                fileexist = true;
            }

            if (lastFolderIndex == i && fileexist) {
                result = 1;
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (!picturePath.isEmpty()) {
                    File ImagefileName = new File(picturePath);
                    if (ImagefileName.exists()) {
                        bug_attach_path.setText(ImagefileName.getName());
                    }
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = context.getContentResolver().query(selectedImage, filePath, null, null, null);
                if (c != null && c.moveToFirst()) {
                    int columnIndex = c.getColumnIndex(filePath[0]);
                    picturePath = c.getString(columnIndex);
                    c.close();
                    String[] temp;
                    temp = picturePath.split("/");
                    fName = temp[temp.length - 1];
                    bug_attach_path.setText(fName);
                }
            }
        }
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

    @Override
    protected void onDestroy() {
        Constants.serviceResponse.remove(ServiceCallback.CONVEYANCE_ACTIVITY);
        super.onDestroy();
    }
}