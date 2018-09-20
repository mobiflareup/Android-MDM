package com.mobiocean.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.RestApiCall;

import org.json.JSONException;
import org.json.JSONObject;


public class GcmServerIntentService extends IntentService {

	public SharedPreferences settings;
	public static SharedPreferences.Editor editor;
	protected static final String PREFS_NAME = "MyPrefsFile";
	
	public GcmServerIntentService(String name) {
		super(name);
	}
	public GcmServerIntentService() 
	{
		super("GcmServerIntentService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		 Bundle bundle = arg0.getExtras();

		 final int seq= bundle.getInt("seq");
		 final String GcmKey = bundle.getString("regkeyGCM");
	
		settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		editor   = settings.edit();
		CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId","");
		RestApiCall mRestApiCall = new RestApiCall();
	
		if(seq==1111)//resp
		{
			JSONObject jsonobj= new JSONObject();
			try {
				jsonobj.put("AppId",CallHelper.Ds.structPC.iStudId);
				jsonobj.put("GCMId",GcmKey);
				jsonobj.put("IsAndroid",1);
				jsonobj.put("GCMSenderId",getResources().getString(R.string.gcm_defaultSenderId));


			 
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			String result = mRestApiCall.sendGCMRegId(jsonobj);
			
			/*JSONObject jsonobj1= new JSONObject();
			try {
				jsonobj1.put("AppId",CallHelper.Ds.structPC.iStudId);
				jsonobj1.put("GCMSenderId",getResources().getString(R.string.gcm_defaultSenderId));
				jsonobj1.put("IsAndroid","1");
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			mRestApiCall.gcmSenderLogin(this, jsonobj1);	*/
			if(Integer.parseInt(result) != -1)
			{
				CallHelper.Ds.structGCM.bGCMPhoneRegisteredOnServer=true;
				editor.putBoolean("structGCM.bGCMPhoneRegisteredOnServer", CallHelper.Ds.structGCM.bGCMPhoneRegisteredOnServer);

			}
		}
		else if(seq==2222)
		{
			//CallHelper.syncingWithServer = true;
			//   String MSG = mWebserviceCall.getStngMsgFromServer(CallHelper.Ds.structPC.iStudId);		
			JSONObject jsonobj= new JSONObject();
			try {
				jsonobj.put("AppId",CallHelper.Ds.structPC.iStudId);
			} catch (JSONException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			String MSG =  mRestApiCall.getPendingSmS(jsonobj);
			//if(!MSG.equalsIgnoreCase("-1"))
			DeBug.ShowLog("MSG","responce  "+MSG);

			if(!TextUtils.isEmpty(MSG) && MSG.contains("GBox"))
			{
				String Body = MSG.substring(MSG.indexOf("GBox"),MSG.length());
				String MSGId= MSG.substring(0,MSG.indexOf("GBox"));
				DeBug.ShowLog("GCMIntentService", ""+Body);
				DeBug.ShowLog("GCMIntentService", ""+MSGId);
				CallHelper.Ds.structGCM.stMessage=(Body);
				CallHelper.Ds.structGCM.stMessageId=(MSGId);
				editor.putString("structGCM.stMessage", CallHelper.Ds.structGCM.stMessage);
				editor.putString("structGCM.stMessageId", CallHelper.Ds.structGCM.stMessageId);

				if(!CallHelper.Ds.structGCM.bAlreadyRunning)
					ACKtoGCM(this);
				editor.commit();

			}
			//CallHelper.syncingWithServer = false;
		}


	}
	public static void ACKtoGCM(final Context ctx)
	{
		final String TAG= "callURLGCM";
				CallHelper.Ds.structGCM.bAlreadyRunning=true;
				{
					{
						int responce=0;
						RestApiCall mRestApiCall = new RestApiCall();

						JSONObject jsonobj= new JSONObject();
						try {
							jsonobj.put("SendMsgIdList",CallHelper.Ds.structGCM.stMessageId);
						} catch (JSONException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						String result = mRestApiCall.sendACKGCM(jsonobj);
						if(Integer.parseInt(result) != -1 )
						{
							CallHelper.Ds.structGCM.bCheckGCMACK=false;

							CallHelper.decodeMessage(ctx, CallHelper.Ds.structGCM.stMessage);
							DeBug.ShowLog(TAG,"responce of  "+CallHelper.Ds.structGCM.stMessageId+" = "+(responce-48));

						}
						else
						{
							CallHelper.decodeMessage(ctx, CallHelper.Ds.structGCM.stMessage);
							DeBug.ShowLog(TAG,"responce of IOException "+CallHelper.Ds.structGCM.stMessage+" = "+(responce-48));
							CallHelper.Ds.structGCM.bCheckGCMACK=true;
						}
						editor.putBoolean("structGCM.bCheckGCMACK", CallHelper.Ds.structGCM.bCheckGCMACK);
						editor.commit();  

					}
				} 
				CallHelper.Ds.structGCM.bAlreadyRunning=false;
			

	}

}
