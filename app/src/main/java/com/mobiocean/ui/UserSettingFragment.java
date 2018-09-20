package com.mobiocean.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiocean.BuildConfig;
import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.LKONagarNigam;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.RoundedImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UserSettingFragment extends Fragment implements OnClickListener {
    private ImageView imgview;
    private Button Update;
    private String[] temp;
    private String fName;
    private String picturePath;
    private String tempadd = "";
    private Boolean imageselect = false;
    private Bitmap bitmap;
    private String uname, email, mobile, profilepath, emp_code;
    private TextView text_name, text_email, text_mobile, employee_code, versionName;
    private String ImagePath;


    private static final String PREFS_NAME = "MyPrefsFile";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;


    public UserSettingFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_setting, container, false);

        TextView txtProfileName = (TextView) rootView.findViewById(R.id.textViewProfile);

        text_name = (TextView) rootView.findViewById(R.id.text_name);
        text_email = (TextView) rootView.findViewById(R.id.text_emailid);
        text_mobile = (TextView) rootView.findViewById(R.id.text_mobile);
        employee_code = (TextView) rootView.findViewById(R.id.textView5);
        versionName = (TextView) rootView.findViewById(R.id.versionName);

        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName.setText(pInfo.versionName);
        } catch (Exception e) {
            e.printStackTrace();
            versionName.setText("Error contact Admin");
        }

        imgview = (ImageView) rootView.findViewById(R.id.profile_image);
        Update = (Button) rootView.findViewById(R.id.update_button);
        Update.setOnClickListener(this);

        settings = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();

        CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);

        imgview.setOnClickListener(this);

        uname = settings.getString("UserSettingFragment.uname", "");
        email = settings.getString("UserSettingFragment.email", "");
        mobile = settings.getString("UserSettingFragment.mobile", "");
        emp_code = settings.getString("UserSettingFragment.emp_code", "");
        ImagePath = settings.getString("UserSettingFragment.ImagePath", null);

        text_name.setText(uname);
        text_email.setText(email);
        text_mobile.setText(mobile);
        employee_code.setText(emp_code);
        if (ImagePath != null) {
            bitmap = (BitmapFactory.decodeFile(ImagePath));
            if (bitmap != null) {
                RoundedImage roundedImage = new RoundedImage(bitmap);
                imgview.setImageDrawable(roundedImage);
            }
        }

        new Getprofile().execute(); // get details from server

        int isEnable = settings.getInt("structCCC.isProfileEnabled", CallHelper.Ds.structCCC.isProfileEnabled);
        String profileId = settings.getString("structCCC.stProfileId", CallHelper.Ds.structCCC.stProfileId);
        String senProfileId = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
        String active = "Yes";
        if (isEnable != 1)
            active = "No";
        txtProfileName.setText("Enable : " + active + " : " + profileId + " : " + senProfileId + " : " + CallHelper.Ds.structPC.iStudId);
        if (BuildConfig.DEBUG)
            txtProfileName.setVisibility(View.VISIBLE);

        return rootView;
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image) {
            selectImage();
        } else if (v.getId() == R.id.update_button) {
            new UploadImage().execute();
        }
    }


    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Close"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if (android.os.Build.VERSION.SDK_INT > 23) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            String imageFolderPath = "/profile/image/";
                            if (CheckFolder(imageFolderPath) != 1)
                                createFolder(imageFolderPath);
                            fName = "" + System.currentTimeMillis() + ".jpg";
                            picturePath = Environment.getExternalStorageDirectory().getAbsolutePath() + imageFolderPath + fName;
                            File f = new File(picturePath);
                            Uri photoURI = null;
                            photoURI = FileProvider.getUriForFile(
                                    getActivity(), getActivity().getApplicationContext()
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
                        picturePath = Environment.getExternalStorageDirectory()
                                .getAbsolutePath() + imageFolderPath + fName;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            imageselect = true;
            if (requestCode == 1) {
                if (!picturePath.isEmpty()) {
                    File ImagefileName = new File(picturePath);
                    if (ImagefileName.exists()) {
                        Uri uri = Uri.fromFile(ImagefileName);
                        imgview.setImageURI(uri);
                    }
                }

            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};

                Cursor c = getActivity().getContentResolver().query(
                        selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                picturePath = c.getString(columnIndex);
                c.close();
                bitmap = (BitmapFactory.decodeFile(picturePath));
                imgview.setImageBitmap(bitmap);
                temp = picturePath.split("/");
                fName = temp[temp.length - 1];
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
                    // result++;
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

    private class UploadImage extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        private String resp;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Uploading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            pDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                if (LKONagarNigam.upLoadProfileFile(picturePath, fName)) {
                    RestApiCall apiCall = new RestApiCall();
                    tempadd = "";
                    String Appid = CallHelper.Ds.structPC.iStudId;
                    tempadd = tempadd + "/profile/image/" + Appid + "/";
                    JSONObject jsonobj = new JSONObject();

                    jsonobj.put("ProfileImagePath", tempadd + fName);
                    jsonobj.put("APPId", CallHelper.Ds.structPC.iStudId);

                    String output = apiCall.UploadProfile(jsonobj);

                    return output;
                }

            } catch (Exception e) {
                e.printStackTrace();
                resp = e.getMessage();
            }

            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            try {
                pDialog.dismiss();
                Toast.makeText(getActivity(), "Profile Updated.", Toast.LENGTH_SHORT).show();
            } catch (Exception e1) {

                e1.printStackTrace();
            }
        }

    }

    private class Getprofile extends AsyncTask<String, String, String> {
        String Appid = CallHelper.Ds.structPC.iStudId;
        JSONObject json = null;
        String str;

        @Override
        protected String doInBackground(String... params) {

            try {

                String imageServerPath = "";
                try {
                    RestApiCall apiCall = new RestApiCall();
                    str = apiCall.profiledetails(Appid);
                    DeBug.ShowLog("json Responce", "" + str);
                    JSONArray jArray = new JSONArray(str);
                    json = jArray.getJSONObject(0);
                    uname = json.getString("UserName");
                    email = json.getString("EmailId");
                    mobile = json.getString("MobileNo1");
                    emp_code = json.getString("EmpCompanyId");
                    profilepath = json.getString("ProfileImagePath");
                    imageServerPath = MobiApplication.CONTACT_SERVER + profilepath;
                    DeBug.ShowLog("Username", "" + uname);
                    DeBug.ShowLog("profilepath", "" + profilepath);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                String imageServerPath = "http://mobiocean.gingerboxmobility.com" + profilepath;
                String imageFolderPath = "/MobiOcean/";

//				LKONagarNigam.downloadFile(profilepath, imageFolderPath);

//				if(false)
                {
                    String path = android.os.Environment.getExternalStorageDirectory() + imageFolderPath;

                    File file = new File(path);

                    if (!file.exists())
                        file.mkdir();
                }

                if (!TextUtils.isEmpty(imageServerPath)) {
                    imageselect = true;
                    String imageDir = Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + imageFolderPath;
                    String Imagename = imageServerPath.substring(imageServerPath
                            .lastIndexOf("/") + 1); // image is not there
                    String file = imageDir + Imagename;
                    String imageSDCardPath = file;

                    // DeBug.ShowLog("Image", ""+imageServerPath);

                    if (!TextUtils.isEmpty(imageSDCardPath)) {

                        if (CheckImageFolder(imageFolderPath) != 1)
                            createnewFolder(imageFolderPath);

                        File imageFile = new File(imageSDCardPath);
                        if (imageFile.exists()) {
                            Intent intent = new Intent("ImageChanged");
                            // You can also include some extra data.
                            intent.putExtra("message", imageSDCardPath);
                            LocalBroadcastManager.getInstance(getActivity())
                                    .sendBroadcast(intent);
                        } else {

                            File ImagefileName = new File(imageSDCardPath);
                            try {
                                ImagefileName.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            DownloadFileFromURL(imageServerPath,
                                    ImagefileName.getAbsolutePath());
                        }

                        // DeBug.ShowLog("Image", ""+imageSDCardPath);
                        DeBug.ShowLog("Image", "" + imageSDCardPath);
                    } else {
                        if (CheckFolder(imageFolderPath) != 1)
                            createFolder(imageFolderPath);

                        File ImagefileName = new File(imageSDCardPath);
                        try {
                            ImagefileName.createNewFile();
                        } catch (IOException e) {

                            e.printStackTrace();
                        }

                        DownloadFileFromURL(imageServerPath,
                                ImagefileName.getAbsolutePath());

                    }

                    ImagePath = Environment.getExternalStorageDirectory()
                            + imageFolderPath + Imagename;

                } else {
                    // Toast.makeText(this, "No Image.",Toast.LENGTH_SHORT).show();
                    DeBug.ShowLog("Image", "No Image.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return str;

        }

        public int CheckImageFolder(String Path) {
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

        public int createnewFolder(String Path) {

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
                    if (NewFolderCreator.mkdirs()) {
                        fileexist = true;
                        // result++;
                    }
                } else {
                    fileexist = true;
                }

                if (lastFolderIndex == i && fileexist) {
                    result = 1;
                }
            }

            return result;

        }

        private void DownloadFileFromURL(String URL, String FilePath) {

            /**
             * Before starting background thread Show Progress Bar Dialog
             * */

            int count;
            try {
                File f = new File(FilePath);
                DeBug.ShowLog("DownloadFileFromURL ", "FilePath :" + FilePath);
                URL url = new URL(URL);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(f);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                DeBug.ShowLog("Error: ", e.getMessage());
            }

            bitmap = (BitmapFactory.decodeFile(FilePath));
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            text_name.setText(uname);
            text_email.setText(email);
            text_mobile.setText(mobile);
            employee_code.setText(emp_code);
            if (ImagePath != null) {
                bitmap = (BitmapFactory.decodeFile(ImagePath));
                if (bitmap != null) {
                    RoundedImage roundedImage = new RoundedImage(bitmap);
                    imgview.setImageDrawable(roundedImage);
                }
            }
            editor.putString("UserSettingFragment.uname", uname);
            editor.putString("UserSettingFragment.email", email);
            editor.putString("UserSettingFragment.mobile", mobile);
            editor.putString("UserSettingFragment.emp_code", emp_code);
            editor.putString("UserSettingFragment.ImagePath", ImagePath);
            editor.commit();
        }

    }

}
