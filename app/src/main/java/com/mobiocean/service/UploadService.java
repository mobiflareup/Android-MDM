package com.mobiocean.service;

import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;

import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.Struct_Send_SMSInfo;
import com.mobiocean.util.eFeatureControl;

import java.util.ArrayList;
import java.util.List;

import static com.mobiocean.util.Constant.APP_CODE;

public class UploadService extends IntentService 
{


	protected static final String PREFS_NAME = "MyPrefsFile";
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editor;

	protected static final int MIN_SMS_SEND_TIME_IN_MINUTES = 5;
	protected static final int MIN_SMS_SEND_TIME_IN_MINUTES_FOR_SPECIFIC_TYPE = 5;
	protected static int SMSsent=0;

	public UploadService(String name) 
	{
		super(name);
	}
	public UploadService() 
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


		DatabaseHabdler_SMSSent SMS_db=DatabaseHabdler_SMSSent.getInstance(this);
		List<Struct_Send_SMSInfo> oListSMSInfo = new ArrayList<Struct_Send_SMSInfo>();
		oListSMSInfo = SMS_db.getAllSMS();
		//	if(UploadStatus==0)
		{
			int count = 0;
			oListSMSInfo = SMS_db.getSMSBytype(5);
			if(UploadStatus==0)
				count = oListSMSInfo.size()-1;
			else
				count = oListSMSInfo.size();

//			for(int i=0;i<count;i++)
//			{
//				SMS_db.deleteSMS(oListSMSInfo.get(i));
//				//DeBug.ShowLog("SMSSENT", " SMSs time  "+oListSMSInfo.get(i).getSave_Time());
//			}
			oListSMSInfo = SMS_db.getAllSMS();
		}

		for(int i=0;i<oListSMSInfo.size();i++)
		{

			DeBug.ShowLog("SMSSENT", " SMSsent 0 "+Struct_Send_SMSInfo.SMSsent);

			int InfoType; 
			String Time; 
			String AppName; 
			String Info2; 
			double Lat;  
			double Lon; 
			int index;
			long CurrentTime;
			long LastSMSSentTime;
			Struct_Send_SMSInfo smsinfo = oListSMSInfo.get(i);
			InfoType= smsinfo.getInfoType(); 
			Time    = smsinfo.getTime(); 
			AppName = smsinfo.getAppName(); 
			Info2   = smsinfo.getInfo2(); 
			Lat     = smsinfo.getLat();  
			Lon     = smsinfo.getLon(); 
			index   = smsinfo.getIndex();
			CurrentTime= smsinfo.getSave_Time();
			LastSMSSentTime = smsinfo.getLast_Sms_Time();

			DeBug.ShowLog("THREAD","Thread is started "+i +" "+ Time +" "+InfoType);

			int SMSsend = 0;
			int InfosentBy_Internet_SMS = -1;

			InfosentBy_Internet_SMS = CallHelper.sendInfoToServer(this, smsinfo, SMSsend);

			long lastSMSTime = settings.getLong("LastSMSSentTime", 0);

			DeBug.ShowLog("THREAD","Thread is started "+i +" "+ Time +" "+InfosentBy_Internet_SMS);

			if(InfosentBy_Internet_SMS!= -1)
			{
				try
				{
					SMS_db.deleteSMS(smsinfo);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			else
				if(checkSMSCanBeSend(lastSMSTime,LastSMSSentTime,InfoType)  &&   !CallHelper.SendingSMS && (CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SEND_SMS) == eFeatureControl.SEND_SMS )
				{
					CallHelper.SendingSMS = true;
					String SMS = genarateSMS( smsinfo, SMSsend);
					
					if(!TextUtils.isEmpty(SMS) && !TextUtils.isEmpty(CallHelper.Ds.structDNCC.stPullMsgNo))
					{
						editor.putLong("LastSMSSentTime", System.currentTimeMillis());
						int dailySmsCount = settings.getInt("dailySmsCount", 0);
						dailySmsCount++;
						editor.putInt("dailySmsCount", dailySmsCount);
						editor.commit();

						smsinfo.setLast_Sms_Time(System.currentTimeMillis());
						SMS_db.updateLastSMSSentTime(smsinfo);

						SMSsent = 0;
						sendSMS(CallHelper.Ds.structDNCC.stPullMsgNo, SMS, Time);
						int count=0;
						while(SMSsent == 0 && count < 300)
						{
							count++;
							try 
							{
								Thread.sleep(100);
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
						}
						if(SMSsent == 2)
						{
							try
							{
								SMS_db.deleteSMS(smsinfo);
								deleteSMS(getBaseContext(), SMS, CallHelper.Ds.structDNCC.stPullMsgNo);
							} 
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
					}
				}
			CallHelper.SendingSMS = false;
		}


	}

	public void deleteSMS(Context context, String message, String number) {
		try {
			Uri uriSms = Uri.parse("content://sms/inbox");
			Cursor c = context.getContentResolver().query(uriSms,
					new String[] { "_id", "thread_id", "address",
							"person", "date", "body" }, null, null, null);

			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(0);
					long threadId = c.getLong(1);
					String address = c.getString(2);
					String body = c.getString(5);

					if (message.equals(body) && address.equals(number)) {
						context.getContentResolver().delete(
								Uri.parse("content://sms/" + id), null, null);
					}
				} while (c.moveToNext());
			}
		} catch (Exception e) {
//			mLogger.logError("Could not delete SMS from inbox: " + e.getMessage());
		}
	}

	/******************************************************
	 * Function for sending SMSs 
	 * *****************************************************/
	private void sendSMS(String phoneNumber, String message, String TAG)
	{        
		String SENT = "SMS_SENT";
		Intent isms = new Intent(SENT);
		isms.putExtra("sms", TAG);
		isms.putExtra("index", (int) 1);
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,isms, PendingIntent.FLAG_ONE_SHOT);
		DeBug.ShowLog("SMSSENT", " message "+" "+message);
		//---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context arg0, Intent arg1) 
			{
				String ss= arg1.getStringExtra("sms");
				Bundle bundle = arg1.getExtras();
				ss = bundle.getString("sms");
				int result=0;
				switch (getResultCode())
				{
				case Activity.RESULT_OK:

					DeBug.ShowLog("SMSSENT", " OK "+" "+ss);
					result=SMSsent = 2;
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					result=SMSsent = 3;
					DeBug.ShowLog("SMSSENT", " Fail ");
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					result=SMSsent = 4;
					DeBug.ShowLog("SMSSENT", " Fail ");
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					result=SMSsent = 5;
					DeBug.ShowLog("SMSSENT", " Fail ");
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					result=SMSsent = 6;
					DeBug.ShowLog("SMSSENT", " Fail ");
					break;
				default:
					result=SMSsent = 7;
					break;
				}


				editor.putInt("ordersmsStatus"+ss, result);
				editor.commit();

				unregisterReceiver(this);
			}
		}, new IntentFilter(SENT));



		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, null);        
	}

	private boolean checkSMSCanBeSend(long LastSMSSent, long LastSMSsentOfThisType, int InfoType) 
	{
			if(InfoType == 26)// || InfoType == 5 || InfoType == 6 || InfoType == 9 || InfoType == 10 ||InfoType ==  15 )
				if((System.currentTimeMillis() - LastSMSSent)/(1000*60) > MIN_SMS_SEND_TIME_IN_MINUTES)
				{
					if((System.currentTimeMillis() - LastSMSsentOfThisType)/(1000*60) > MIN_SMS_SEND_TIME_IN_MINUTES_FOR_SPECIFIC_TYPE)
					{
						return true;
					}
				}
		return false;

	}

	private String genarateSMS(final Struct_Send_SMSInfo smsinfo, final int SMSsend) 
	{
		int InfoType; 
		String Time; 
		String AppName; 
		String Info2; 
		double Lat;  
		double Lon; 
		int index;
		long CurrentTime;
		long LastSMSSentTime;
		String CellID;
		String MCC;
		String LAC;
		String MNC;
		String MSG=null;

		InfoType= smsinfo.getInfoType(); 
		Time    = smsinfo.getTime(); 
		AppName = smsinfo.getAppName(); 
		Info2   = smsinfo.getInfo2(); 
		Lat     = smsinfo.getLat();  
		Lon     = smsinfo.getLon(); 
		index   = smsinfo.getIndex();
		CurrentTime= smsinfo.getSave_Time();
		LastSMSSentTime = smsinfo.getLast_Sms_Time();
		CellID = smsinfo.getCellId();
		MCC = smsinfo.getMCC();
		MNC = smsinfo.getMNC();
		LAC = smsinfo.getLAC();
		try {
			switch (InfoType)
			{
				case 2://For GL
					MSG = CallHelper.Ds.structPC.stPassword + " GC" + APP_CODE + " " + "GL" + index + " " + CallHelper.Ds.structPC.iStudId + " " + Info2 + "~ " + AppName + " " + Lat + " " + Lon + " " + Time + " " + CellID + " " + LAC + " " + MCC + " " + MNC;
					DeBug.ShowLog("TESTMSG", " latitude " + MSG + " to " + CallHelper.Ds.structDNCC.stPullMsgNo);
					break;
				case 6:// " MO " Mobile On/Off Timing auto generation from Nimboli   #GS
					MSG = CallHelper.Ds.structPC.stPassword + " GC" + APP_CODE + " " + "MO" + " " + CallHelper.Ds.structPC.iStudId + " " + Info2 + " " + Time;
					DeBug.ShowLog("LMMSG", "Sending Mobile On Off...via SMS " + MSG);
					break;
				case 10:// " VN " Version Number and it is  generated from Nimboli   #GS
					MSG = CallHelper.Ds.structPC.stPassword + " GC" + APP_CODE + " " + "VN" + " " + CallHelper.Ds.structPC.iStudId + " " + Info2 + " " + Time;
					DeBug.ShowLog("LMMSG", "Sending version number Message...via SMS " + MSG);
					break;
			}
		} catch (java.lang.Exception e) {
			DeBug.ShowLog("TESTMSG", " SMS Exception "+ e.getMessage());
			e.printStackTrace();
		}
		return MSG;
	}

	@Override
	public void onDestroy() 
	{
		CallHelper.locationIsUploading = false;
		DeBug.ShowLogD("UploadService", "onDestroy");
		super.onDestroy();
	}
}