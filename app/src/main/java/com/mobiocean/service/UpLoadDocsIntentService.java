package com.mobiocean.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.mobiocean.database.WebPageViewHistorySQLiteOpenHelper;
import com.mobiocean.database.WebPageViewHistoryStruct;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RestApiCall;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class UpLoadDocsIntentService extends IntentService 
{

	File ImagefileName=null;
	static int FeatereValue;
	static int Duration;
	public static final String TAG = UpLoadDocsIntentService.class.getSimpleName();
	
	public UpLoadDocsIntentService(String name) 
	{
		super(name);
	}

	public UpLoadDocsIntentService() 
	{
		super("UpLoadDocsIntentService");
	}

	@Override
	protected void onHandleIntent(Intent arg0)
	{		
		Bundle bundle = arg0.getExtras();

		CallHelper.InfoUploading = true;

		FeatereValue = bundle.getInt("FeatereValue");
		final int FeatureIndex= bundle.getInt("FeatureIndex");
		String Duration = bundle.getString("IsBlackList");
		switch(FeatereValue)
		{
		case 6 :

			DeBug.ShowLog("BusinessCardData", "ContactInfoSQLiteOpenHelper");
			WebPageViewHistorySQLiteOpenHelper mWebPageViewHistorySQLiteOpenHelper = WebPageViewHistorySQLiteOpenHelper.getInstance(this);

			List<WebPageViewHistoryStruct> mWebPageViewHistoryStructList = mWebPageViewHistorySQLiteOpenHelper.getEnterUrlByFeatureIndex(FeatureIndex);

			DeBug.ShowLog("BusinessCardData", "mContactInfoStructList"+mWebPageViewHistoryStructList.size());

			RestApiCall mRestApiCall = new RestApiCall();
			
			for(int i=0;i<mWebPageViewHistoryStructList.size();i++)
			{
			
				WebPageViewHistoryStruct mWebPageViewHistoryStruct = mWebPageViewHistoryStructList.get(i);

				if(mWebPageViewHistoryStruct!=null && !mWebPageViewHistoryStruct.equals(""))
				{
					  JSONObject jsonMemoryInfo= new JSONObject();
					  try {

						  jsonMemoryInfo.put("AppId", CallHelper.Ds.structPC.iStudId);
						  jsonMemoryInfo.put("WebsiteUrl", mWebPageViewHistoryStruct.getPagename());
						  jsonMemoryInfo.put("LogDateTime", mWebPageViewHistoryStruct.getLogdatetime());

					  } catch (JSONException e) {
						  e.printStackTrace();
					  }

					  int memoryInfo = Integer.parseInt(mRestApiCall.uploadBrowserWebsitesInfo(jsonMemoryInfo));
					  DeBug.ShowLog("BusinessCardData", " mWebPageViewHistoryStruct "+jsonMemoryInfo+" response "+memoryInfo);
					  
					  if(memoryInfo==1)
					  {
						  mWebPageViewHistorySQLiteOpenHelper.deleteContact(mWebPageViewHistoryStruct);
					  }
				}
					
			}

			break;

		}

	}

	@Override
	public void onDestroy() 
	{
		CallHelper.InfoUploading = false;
		super.onDestroy();
	}


}