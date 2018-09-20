package org.sn.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.mobiocean.beans.SendConveyanceBean;
import com.mobiocean.beans.SendConveyanceImageBean;
import com.mobiocean.util.CallHelper;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.sn.beans.ConveyanceBean;
import org.sn.database.ConveyanceLocationTable;
import org.sn.location.NetworkUtil;
import org.sn.util.Constants;
import org.sn.util.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateConveyanceService extends IntentService {

    private ConveyanceLocationTable conveyanceLocationTable;
    private Context context;
    private static boolean isLocationUploading = false;
    private SharedPreferences settings;
    SharedPreferences.Editor editor;
    final String PREFS_NAME = "MyPrefsFile";
    private Helper helper = new Helper();

    public UpdateConveyanceService() {
        super("UpdateConveyanceService");
        context = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            conveyanceLocationTable = new ConveyanceLocationTable(context);
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                if (!isLocationUploading) {
                    ArrayList<ConveyanceBean> conveyanceLocations = conveyanceLocationTable.getAllLocation();
                    if (conveyanceLocations != null && conveyanceLocations.size() > 0) {
                        uploadStartStopLocation();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadStartStopLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isLocationUploading = true;
                ArrayList<ConveyanceBean> conveyanceLocations = conveyanceLocationTable.getAllLocation();
                if (conveyanceLocations != null && conveyanceLocations.size() > 0) {
                    final ConveyanceBean upload = conveyanceLocations.get(0);
                    if (upload.IsLogin != null && !upload.IsLogin.isEmpty()) {
                        switch (upload.IsLogin) {
                            case "1":
                            case "2":
                            case "3":
                                SendConveyanceBean bean = new SendConveyanceBean();
                                bean.appId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
                                bean.ConveyanceId = settings.getInt("Conveyance.ConveyanceId", CallHelper.Ds.structPC.ConveyanceId);
                                bean.IsLogin = upload.IsLogin;
                                bean.LogDateTime = upload.LogDateTime;
                                bean.Latitude = upload.Latitude;
                                bean.Longitude = upload.Longitude;
                                bean.CellId = upload.CellId;
                                bean.LAC = upload.LAC;
                                bean.MCC = upload.MCC;
                                bean.MNC = upload.MNC;
                                bean.Remark = upload.Remark;
                                bean.VehicleReading = upload.VehicleReading;
                                bean.ImagePath = upload.ImagePath;
                                boolean isFileUploaded = true;
                                if (upload.ImagePath != null && !upload.ImagePath.isEmpty())
                                    isFileUploaded = uploadImageToServer(upload.ImagePath, true);
                                if (isFileUploaded) {
                                    String galleryPath = upload.ImagePath;
                                    if (!TextUtils.isEmpty(galleryPath)) {
                                        String fileName = galleryPath.substring(galleryPath.lastIndexOf("/") + 1);
                                        bean.ImagePath = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId) + "/Conveyance/Reading/" + fileName;
                                    }
                                    Call<String> call = helper.getInterface().sendConveyance(bean);
                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            if (response != null) {
                                                String visitId = response.body();
                                                if (visitId != null && !visitId.isEmpty()) {
                                                    if (upload.IsLogin.equals("1")) {
                                                        CallHelper.Ds.structPC.ConveyanceId = Integer.parseInt(visitId);
                                                        editor.putInt("Conveyance.ConveyanceId", CallHelper.Ds.structPC.ConveyanceId);
                                                        editor.apply();
                                                    }
                                                    conveyanceLocationTable.deleteValue(upload._Id);
                                                    uploadStartStopLocation();
                                                } else {
                                                    isLocationUploading = false;
                                                }
                                            } else {
                                                isLocationUploading = false;
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            isLocationUploading = false;
                                        }
                                    });
                                }
                                break;
                            case "4":
                                SendConveyanceImageBean imageBean = new SendConveyanceImageBean();
                                imageBean.appId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
                                imageBean.ConveyanceId = settings.getInt("Conveyance.ConveyanceId", CallHelper.Ds.structPC.ConveyanceId);
                                imageBean.Remark = upload.Remark;
                                imageBean.ImagePath = upload.ImagePath;
                                isFileUploaded = true;
                                if (upload.ImagePath != null && !upload.ImagePath.isEmpty())
                                    isFileUploaded = uploadImageToServer(upload.ImagePath, false);
                                if (isFileUploaded) {
                                    String galleryPath = upload.ImagePath;
                                    if (!TextUtils.isEmpty(galleryPath)) {
                                        String fileName = galleryPath.substring(galleryPath.lastIndexOf("/") + 1);
                                        imageBean.ImagePath = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId) + "/Conveyance/Proof/" + fileName;
                                    }
                                    Call<String> call = helper.getInterface().sendConveyanceRemarks(imageBean);
                                    call.enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            if (response != null) {
                                                String visitId = response.body();
                                                if (visitId != null && !visitId.isEmpty()) {
                                                    conveyanceLocationTable.deleteValue(upload._Id);
                                                    uploadStartStopLocation();
                                                } else {
                                                    isLocationUploading = false;
                                                }
                                            } else {
                                                isLocationUploading = false;
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {
                                            isLocationUploading = false;
                                        }
                                    });
                                } else {
                                    isLocationUploading = false;
                                }
                                break;
                        }
                    }
                } else {
                    isLocationUploading = false;
                }
            }
        }).start();
    }

    private boolean uploadImageToServer(String _filepath, boolean isForMeter) {
        boolean finalResult = false;
        String path = "/";
        String fileNameOnServer = "";
        try {
            fileNameOnServer = _filepath.substring(_filepath.lastIndexOf("/") + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName(Constants.FTP_HOST));
            if (ftpClient.login(Constants.FTP_USER, Constants.FTP_PASS)) {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.changeWorkingDirectory(path);
                path = path + settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
                finalResult = ftpClient.makeDirectory(path);
                path = path + "/Conveyance";
                finalResult = ftpClient.makeDirectory(path);
                if(isForMeter)
                    path = path + "/Reading";
                else
                    path = path + "/Proof";
                finalResult = ftpClient.makeDirectory(path);
                ftpClient.changeWorkingDirectory(path);
                FileInputStream in = new FileInputStream(new File(_filepath));
                finalResult = ftpClient.storeFile(fileNameOnServer, in);
                in.close();
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (FileNotFoundException e) {
            finalResult = true;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                boolean status = false;
                try {
                    InetAddress ipAddr = InetAddress.getByName("mobiocean.com");
                    status = !ipAddr.equals("");
                } catch (Exception ex) {
                    status = false;
                    ex.printStackTrace();
                } finally {
                    if(status){
                        finalResult = uploadImageToServer(_filepath, isForMeter);
                    }
                }
            }
        }
        return finalResult;
    }

}
