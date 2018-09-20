package org.conveyance.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mobiocean.R;

import org.conveyance.adapters.RExtraAllowanceAdapter;
import org.conveyance.configuration.RConstant;
import org.conveyance.configuration.RHelper;
import org.conveyance.configuration.RSharedData;
import org.conveyance.database.RExtraAllowanceTable;
import org.conveyance.model.RExtraAllowanceModel;
import org.conveyance.services.RUploadDetailsService;
import org.sn.location.NetworkUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          21-09-16         Initial creation
 ****************************************************************************/

public class RRemarksActivity extends AppCompatActivity {
    Toolbar toolbar;
    Context context;
    EditText remark_txt, amounttxt, uploadfile;
    Button addbtn;
    RHelper helper = new RHelper();
    String remarkget, amountget;
    RSharedData settings;
    String gallerypath = "";
    RExtraAllowanceTable extraAllowanceTable;
    RExtraAllowanceModel extraAllowanceModel;
    ArrayList<RExtraAllowanceModel> extraAllowanceModelArrayList;
    RExtraAllowanceAdapter extraAllowanceAdapter;
    RecyclerView allowancelist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.r_remarks_page);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);
        context = RRemarksActivity.this;
        settings = new RSharedData(context);
        remark_txt = (EditText) findViewById(R.id.remark_txt);
        amounttxt = (EditText) findViewById(R.id.amount_txt);
        uploadfile = (EditText) findViewById(R.id.uploadfile);
        addbtn = (Button) findViewById(R.id.addbtn);
        allowancelist = (RecyclerView) findViewById(R.id.allowancelist);

        loadAllowance();

        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverPath = "";
                if (isValidData()) {
                    extraAllowanceModel = new RExtraAllowanceModel();
                    extraAllowanceModel.setAppId(settings.getAppId());
                    extraAllowanceModel.setRemark(remarkget);
                    extraAllowanceModel.setFilePath(gallerypath);
                    extraAllowanceModel.setClaimedAmt(amountget);
                    extraAllowanceModel.setLogDateTime(helper.dateTime());
                    extraAllowanceTable = new RExtraAllowanceTable(context);
                    extraAllowanceTable.insertExtraAllowance(extraAllowanceModel);
                    loadAllowance();
                    remark_txt.setText("");
                    amounttxt.setText("");
                    uploadfile.setText("Choose File");
                    if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                        startService(new Intent(context, RUploadDetailsService.class));
                    }
                }
            }
        });
        uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
    }

    private boolean isValidData() {
        boolean isValid = true;
        try {
            remarkget = remark_txt.getText().toString();
            amountget = amounttxt.getText().toString();
            if (TextUtils.isEmpty(remarkget)) {
                remark_txt.setError("Enter Remarks");
                isValid = false;
            }
            if (TextUtils.isEmpty(amountget)) {
                amounttxt.setError("Enter Amount");
                isValid = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
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
                        gallerypath = file.toString();
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
                    if(!file.exists())
                        file.mkdir();
                    path = path + File.separator+ String.valueOf(System.currentTimeMillis()) + ".jpg";
                    if (RConstant.saveImageBitmap(bitmap, path)) {
                        gallerypath = path;
                        uploadfile.setText(path.substring(path.lastIndexOf("/") + 1));
                    }
                }catch (Exception e){
                    gallerypath = "";
                }
            }
        }
    }

    private void loadAllowance() {
        extraAllowanceTable = new RExtraAllowanceTable(context);
        extraAllowanceTable.deleteOldAllowance();    // delete previous allowance data
        extraAllowanceModelArrayList = new ArrayList<>();
        extraAllowanceModelArrayList = extraAllowanceTable.getExtraAllowance();
        extraAllowanceAdapter = new RExtraAllowanceAdapter(context, extraAllowanceModelArrayList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        allowancelist.setLayoutManager(linearLayoutManager);
        allowancelist.setAdapter(extraAllowanceAdapter);
    }
}
