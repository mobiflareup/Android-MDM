package org.conveyance.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mobiocean.util.DeBug;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.conveyance.configuration.RConstant;
import org.conveyance.configuration.RHelper;
import org.conveyance.configuration.RSharedData;
import org.conveyance.database.RExtraAllowanceTable;
import org.conveyance.database.RStartStopLocation;
import org.conveyance.model.RControlModel;
import org.conveyance.model.RExtraAllowanceModel;
import org.sn.location.NetworkUtil;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RUploadDetailsService extends IntentService {

    private RSharedData sharedData;
    private RStartStopLocation startStopLocation;
    private RExtraAllowanceTable extraAllowanceTable;
    private RHelper rHelper = new RHelper();
    private Context context;
    private static boolean isLocationUploading = false;
    private static boolean isExtraSync = false;

    public RUploadDetailsService() {
        super("RUploadDetailsService");
        DeBug.ShowLog("FLNSU", "Upload Details Service");
        context = this;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            sharedData = new RSharedData(context);
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                startStopLocation = new RStartStopLocation(context);
                extraAllowanceTable = new RExtraAllowanceTable(context);

                if (!isLocationUploading) {
                    ArrayList<RControlModel> offlineStartStopLocation = startStopLocation.getAllOffLocation();
                    if (offlineStartStopLocation != null && offlineStartStopLocation.size() > 0) {
                        uploadStartStopLocation();
                    }
                }

                if (!isExtraSync) {
                    ArrayList<RExtraAllowanceModel> offLineAllowance = extraAllowanceTable.getSyncExtraAllowance();
                    if (offLineAllowance != null && offLineAllowance.size() > 0) {
                        uploadExtraAllowance();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadStartStopLocation() {
        isLocationUploading = true;
        DeBug.ShowLog("FLNSU", "uploadStartStopLocation started");
        ArrayList<RControlModel> offlineStartStopLocation = startStopLocation.getAllOffLocation();
        if (offlineStartStopLocation != null && offlineStartStopLocation.size() > 0) {
            final RControlModel upload = offlineStartStopLocation.get(0);
            upload.setAppId(sharedData.getAppId());
            upload.setVisitId(sharedData.getVisitId());
            if (upload.getIsLogin() != null && !upload.getIsLogin().isEmpty()) {
                switch (upload.getIsLogin()) {
                    case "1":
                    case "2":
                    case "3":
                        Call<Integer> call = rHelper.getInterface().startStopConveyance(upload);
                        call.enqueue(new Callback<Integer>() {
                            @Override
                            public void onResponse(Call<Integer> call, Response<Integer> response) {
                                if (response != null) {
                                    Integer visitId = response.body();
                                    if (visitId != null && visitId > 0) {
                                        DeBug.ShowLog("FLNSU", "uploadStartStopLocation Response vId:" + visitId + " Login:" + upload.getIsLogin());
                                        if (upload.getIsLogin().equals("1") || upload.getIsLogin().equals("2"))
                                            sharedData.setVisitID(String.valueOf(visitId));
                                        else
                                            sharedData.setVisitID("");
                                        startStopLocation.deleteValue(upload.getSno());
                                        uploadStartStopLocation();
                                    } else {
                                        isLocationUploading = false;
                                        DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                                    }
                                } else {
                                    isLocationUploading = false;
                                    DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                                }
                            }

                            @Override
                            public void onFailure(Call<Integer> call, Throwable t) {
                                isLocationUploading = false;
                                DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                            }
                        });
                        break;
                    case "4":
                        boolean isFileUploaded = true;
                        if (upload.getFilePath() != null && !upload.getFilePath().isEmpty())
                            isFileUploaded = uploadImageToServer(upload.getFilePath(), true);
                        if (isFileUploaded) {
                            String galleryPath = upload.getFilePath();
                            if (!TextUtils.isEmpty(galleryPath)) {
                                String fileName = galleryPath.substring(galleryPath.lastIndexOf("/") + 1);
                                String serverPath = sharedData.getAppId() + "/Travel Allowance/Visit/" + fileName;
                                upload.setFilePath(serverPath);
                            }
                            call = rHelper.getInterface().visitInfo(upload);
                            call.enqueue(new Callback<Integer>() {
                                @Override
                                public void onResponse(Call<Integer> call, Response<Integer> response) {
                                    if (response != null) {
                                        Integer visitId = response.body();
                                        if (visitId != null && visitId > 0) {
                                            DeBug.ShowLog("FLNSU", "uploadStartStopLocation Response vId:" + visitId + " Login:" + upload.getIsLogin());
                                            startStopLocation.deleteValue(upload.getSno());
                                            uploadStartStopLocation();
                                        } else {
                                            isLocationUploading = false;
                                            DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                                        }
                                    } else {
                                        isLocationUploading = false;
                                        DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                                    }
                                }

                                @Override
                                public void onFailure(Call<Integer> call, Throwable t) {
                                    isLocationUploading = false;
                                    DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                                }
                            });
                        } else {
                            isLocationUploading = false;
                            DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
                        }
                        break;
                }
            }
        } else {
            isLocationUploading = false;
            DeBug.ShowLog("FLNSU", "uploadStartStopLocation stopped");
        }
    }

    private void uploadExtraAllowance() {
        isExtraSync = true;
        DeBug.ShowLog("FLNSU", "uploadExtraAllowance started");
        ArrayList<RExtraAllowanceModel> offLineAllowance = extraAllowanceTable.getSyncExtraAllowance();
        if (offLineAllowance != null && offLineAllowance.size() > 0) {
            boolean isFileUploaded = true;
            final RExtraAllowanceModel upload = offLineAllowance.get(0);
            if (upload.getFilePath() != null && !upload.getFilePath().isEmpty())
                isFileUploaded = uploadImageToServer(upload.getFilePath(), false);
            if (isFileUploaded) {
                String galleryPath = upload.getFilePath();
                if (!TextUtils.isEmpty(galleryPath)) {
                    String fileName = galleryPath.substring(galleryPath.lastIndexOf("/") + 1);
                    String serverPath = sharedData.getAppId() + "/Travel Allowance/ExtraAllowance/" + fileName;
                    upload.setFilePath(serverPath);
                }
                Call<Integer> call = rHelper.getInterface().extraAllowance(upload);
                call.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        if (response != null) {
                            Integer visitId = response.body();
                            if (visitId != null && visitId > 0) {
                                DeBug.ShowLog("FLNSU", "uploadExtraAllowance Response visitId:" + visitId);
                                extraAllowanceTable.extraAllowanceUpdate(1, upload.getSno());
                                uploadExtraAllowance();
                            } else {
                                isExtraSync = false;
                                DeBug.ShowLog("FLNSU", "uploadExtraAllowance stopped");
                            }
                        } else {
                            isExtraSync = false;
                            DeBug.ShowLog("FLNSU", "uploadExtraAllowance stopped");
                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        isExtraSync = false;
                        DeBug.ShowLog("FLNSU", "uploadExtraAllowance stopped");
                    }
                });
            } else {
                isExtraSync = false;
                DeBug.ShowLog("FLNSU", "uploadExtraAllowance stopped");
            }
        } else {
            isExtraSync = false;
            DeBug.ShowLog("FLNSU", "uploadExtraAllowance stopped");
        }
    }

    private boolean uploadImageToServer(String _filepath, boolean isForRemarks) {
        DeBug.ShowLog("FLNSU", "uploadImageToServer Started " + (isForRemarks ? "Remarks" : "Visit"));
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
            ftpClient.connect(InetAddress.getByName(RConstant.FTP_HOST));
            if (ftpClient.login(RConstant.FTP_USER, RConstant.FTP_PASS)) {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.changeWorkingDirectory(path);
                path = path + sharedData.getAppId();
                finalResult = ftpClient.makeDirectory(path);
                path = path + "/Travel Allowance";
                finalResult = ftpClient.makeDirectory(path);
                if (isForRemarks)
                    path = path + "/Visit";
                else
                    path = path + "/ExtraAllowance";
                finalResult = ftpClient.makeDirectory(path);
                ftpClient.changeWorkingDirectory(path);
                FileInputStream in = new FileInputStream(new File(_filepath));
                finalResult = ftpClient.storeFile(fileNameOnServer, in);
                in.close();
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (SocketException se) {
            DeBug.ShowLog("FLNSU", "uploadImageToServer SocketException error " + (isForRemarks ? "Remarks" : "Visit") + " " + se.getMessage());
            se.printStackTrace();
            if (NetworkUtil.NetworkStatus.NO_NET != NetworkUtil.getConnectivityStatus(context)) {
                boolean status = false;
                try {
                    InetAddress ipAddr = InetAddress.getByName("mobiocean.com");
                    status = !ipAddr.equals("");
                } catch (Exception e) {
                    status = false;
                    e.printStackTrace();
                } finally {
                    if(status){
                        finalResult = uploadImageToServer(_filepath, isForRemarks);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            DeBug.ShowLog("FLNSU", "uploadImageToServer error " + (isForRemarks ? "Remarks" : "Visit") + " " + e.getMessage());
        }
        DeBug.ShowLog("FLNSU", "uploadImageToServer Stopped " + (isForRemarks ? "Remarks" : "Visit") + " isUploaded:" + finalResult);
        return finalResult;
    }

}