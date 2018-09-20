package com.mobiocean.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.mobiocean.mobidb.SOSandTheftInfoDB;
import com.mobiocean.mobidb.SOSandTheftInfoStruct;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.RestApiCall;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.sn.location.NetworkUtil;
import org.sn.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class UploadFileToserverIntentService extends IntentService {

	private Context context;

	public UploadFileToserverIntentService() {
		super("UploadFileToserverIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		context = this;
		final SOSandTheftInfoDB mDb = SOSandTheftInfoDB.getInstance(this);
		if(NetworkUtil.NetworkStatus.NO_NET!=NetworkUtil.getConnectivityStatus(context)) {
			new Thread(new Runnable() {
                @Override
                public void run() {
                    RestApiCall mApiCall = new RestApiCall();
                    ArrayList<SOSandTheftInfoStruct> listSOSandTheftInfoStruct = mDb.getAllUnUploadedData();
                    for (SOSandTheftInfoStruct info : listSOSandTheftInfoStruct) {
                        if(uploadImageToServer(info)){
                            long time = Long.parseLong(info.getLogDateTime());
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(time);
                            DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
                            String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();
                            info.setLogDateTime(SMSTimeStamp);
                            String result = mApiCall.sendFileInfo((MobiApplication) getApplication(), info);
                            if (!TextUtils.isEmpty(result) && result.equals("1")) {
                                mDb.deleteData(info.getLogDateTime());
                            }
                        }
                    }
                }
            }).start();
		}
	}

	private boolean uploadImageToServer(final SOSandTheftInfoStruct mInfoStruct) {
		boolean finalResult = false;
		String path = "/";
		String fileNameOnServer = mInfoStruct.getFileName();
		FTPClient ftpClient = null;
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(InetAddress.getByName(Constants.FTP_HOST));
			if (ftpClient.login(Constants.FTP_USER, Constants.FTP_PASS)) {
				ftpClient.enterLocalPassiveMode();
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				ftpClient.changeWorkingDirectory(path);
				path = path + mInfoStruct.getAppId();
				finalResult = ftpClient.makeDirectory(path);
				if(mInfoStruct.getIsforSos() == 1)
				{
					if(mInfoStruct.getIsAudio()==1)
						path = path + "/SOSAudio";
					else
						path = path + "/SOSVideo";
				}
				else
				{
					if(mInfoStruct.getIsAudio()==1)
						path = path + "/TheftAudio";
					else
						path = path + "/TheftVideo";
				}
				finalResult = ftpClient.makeDirectory(path);
				ftpClient.changeWorkingDirectory(path);
				FileInputStream in = new FileInputStream(new File(mInfoStruct.getLocalFilePath()));
				finalResult = ftpClient.storeFile(fileNameOnServer, in);
				in.close();
				ftpClient.logout();
				ftpClient.disconnect();
				if(finalResult)
				{
					File file = new File(mInfoStruct.getLocalFilePath());
					file.delete();
				}
			}
		} catch (FileNotFoundException e) {
			finalResult = true;
			e.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
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
						finalResult = uploadImageToServer(mInfoStruct);
					}
				}
			}
		}
		return finalResult;
	}
}
