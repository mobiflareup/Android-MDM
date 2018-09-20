package com.mobiocean.service;

import java.util.List;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.mobiocean.mobidb.LogInfoDatabaseHandler;
import com.mobiocean.mobidb.LogInfoStruct;
import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.GameLogs;
import com.mobiocean.util.RestApiCall;

public class UplopadLogsToServer extends IntentService 
{

	
	protected static final String PREFS_NAME = "MyPrefsFile";
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editor;

	protected static final int MIN_SMS_SEND_TIME_IN_MINUTES = 5;
	protected static final int MIN_SMS_SEND_TIME_IN_MINUTES_FOR_SPECIFIC_TYPE = 10;
	protected static int SMSsent=0;

	public UplopadLogsToServer(String name) 
	{
		super(name);
	}
	public UplopadLogsToServer() 
	{
		super("UploadService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) 
	{		
		
		 Bundle bundle = arg0.getExtras();

		 final int UploadStatus= bundle.getInt("UploadStatus");
		 
		    
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS);
		else
			settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		editor = settings.edit();
		

		CallHelper.Ds.structPC.stSIMSerialno = settings.getString("structPC.stSIMSerialno", CallHelper.Ds.structPC.stSIMSerialno);
		CallHelper.Ds.structPC.iStudId       = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
		CallHelper.Ds.structPC.stPhoneNo     = settings.getString("structPC.stPhoneNo", CallHelper.Ds.structPC.stPhoneNo);

		LogInfoDatabaseHandler mLogInfoDatabaseHandler = LogInfoDatabaseHandler.getInstance(this);
		List<LogInfoStruct> listLogInfoStruct = mLogInfoDatabaseHandler.getAllContacts();
		
		RestApiCall mRestApiCall = new RestApiCall();
		for(int i=0 ;i<listLogInfoStruct.size(); i++)
		{
			LogInfoStruct  mLogInfoStruct  = listLogInfoStruct.get(i);
			
			
			GameLogs mGameLogs = new GameLogs();
			mGameLogs.setAppId(CallHelper.Ds.structPC.iStudId);
			mGameLogs.setAppName(mLogInfoStruct.getAppName());
			mGameLogs.setAppIndex(mLogInfoStruct.getSubfeature_index());
			mGameLogs.setDuration(mLogInfoStruct.getDuration());
			mGameLogs.setStartTime(mLogInfoStruct.getStart_time());
			mGameLogs.setEndTime(mLogInfoStruct.getLastused_time());
			mGameLogs.setLogDateTime(mLogInfoStruct.getLogdate_time());
			String Result = mRestApiCall.postApplicationLogs((MobiApplication) getApplication(), mGameLogs);
			
			if(!TextUtils.isEmpty(Result))
			{
				if(Result!="-1")
				{
					mLogInfoStruct.setStatus("1");
					String date = CallHelper.GetTimeWithDate().substring(0, 10);
					mLogInfoDatabaseHandler.updateStatus(mLogInfoStruct, date);
				}
			}
						
		}
		
	}

	
	@Override
	public void onDestroy() 
	{
		CallHelper.locationIsUploading = false;
		DeBug.ShowLogD("UplopadLogsToServer", "onDestroy");
		super.onDestroy();
	}

}
