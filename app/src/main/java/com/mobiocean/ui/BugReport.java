package com.mobiocean.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.AanwlaService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.LKONagarNigam;
import com.mobiocean.util.SendReport;

import java.io.File;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BugReport extends Activity {

    private EditText bug_name, bug_desc;
    private Button bug_add_attach, bug_send;
    private TextView bug_attach_path;
    private ImageView bug_img_attach;
    private Context context;
    private String picturePath, fName;
    private String[] temp;

    protected static final String PREFS_NAME = "MyPrefsFile";
    public static SharedPreferences settings;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bug_report);
        bug_name = (EditText) findViewById(R.id.bug_name);
        bug_desc = (EditText) findViewById(R.id.bug_desc);
        bug_add_attach = (Button) findViewById(R.id.bug_add_attach);
        bug_send = (Button) findViewById(R.id.bug_send);
        bug_attach_path = (TextView) findViewById(R.id.bug_attach_path);
        bug_img_attach = (ImageView) findViewById(R.id.bug_img_attach);
        context = BugReport.this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
        else
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        bug_add_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        bug_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBug();
            }
        });
    }

    public void sendBug() {
        String name = bug_name.getText().toString().trim();
        String desc = bug_desc.getText().toString().trim();
        CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
        if (!(name.isEmpty() || desc.isEmpty())) {
            SendReport sendReport = new SendReport();
            sendReport.setAppId(CallHelper.Ds.structPC.iStudId);
            sendReport.setDefectName(name);
            sendReport.setDefectDesc(desc);
            {
                new UpdateReport().execute(sendReport);
            }
        } else {
            Toast.makeText(context, "Please fill all fields properly.", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(BugReport.this);
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
                                    getApplicationContext(), getApplicationContext()
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
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (!picturePath.isEmpty()) {
                    File ImagefileName = new File(picturePath);
                    if (ImagefileName.exists()) {
                        Uri uri = Uri.fromFile(ImagefileName);
                        bug_img_attach.setImageURI(uri);
                        bug_img_attach.setVisibility(View.VISIBLE);
                        bug_attach_path.setText(ImagefileName.getName());
                    }
                }
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = context.getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                picturePath = c.getString(columnIndex);
                c.close();
                Bitmap bitmap = (BitmapFactory.decodeFile(picturePath));
                bug_img_attach.setImageBitmap(bitmap);
                temp = picturePath.split("/");
                fName = temp[temp.length - 1];
                bug_img_attach.setVisibility(View.VISIBLE);
                bug_attach_path.setText(fName);
            }
        }
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

    public class UpdateReport extends AsyncTask<SendReport, Void, Void> {
        protected ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            if (progressDialog == null || !progressDialog.isShowing()) {
                progressDialog = ProgressDialog.show(BugReport.this, "Please wait ...", "Loading ...", true);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(SendReport... params) {
            try {
                if (params[0] != null) {

                    if (picturePath != null && !picturePath.isEmpty() && fName != null && !fName.isEmpty())
                        if (LKONagarNigam.upLoadBugImage(picturePath, fName)) {
                            params[0].setDocPath("/bugReport/" + fName);
                        } else {
                            params[0].setDocPath("");
                        }
                    Retrofit retrofit = new Retrofit.Builder().baseUrl(MobiApplication.CONTACT_SERVER).addConverterFactory(GsonConverterFactory.create()).build();
                    AanwlaService webInterface = retrofit.create(AanwlaService.class);
                    Call<String> call = webInterface.updateReport(params[0]);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            String result = response.body();
                            if (result != null) {
                                if (!result.isEmpty()) {
                                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(context, "Network Network avilable Please try again later", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
