package com.mobiocean.ui;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.TelephonyManager;

import com.mobiocean.receiver.DemoDeviceAdminReceiver;
import com.mobiocean.service.AudioRecorderIntentService;
import com.mobiocean.service.CallDetectService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.eFeatureControl;

import org.sn.location.LocationDetails;

public class SOSReceiver extends BroadcastReceiver
{
	private static int countPowerOff = 0;
	NotificationCompat.Builder notification;
	PendingIntent pIntent;
	NotificationManager manager;
	Intent resultIntent;
	TaskStackBuilder stackBuilder;
	public static boolean wasScreenOn = true;
	protected static DevicePolicyManager devicePolicyManager;
	protected static ComponentName demoDeviceAdmin;

	private long timeElapsed;
	private static boolean timerHasStopped = true;

	private final long maxtime = 10000;
	private final long interval = 1000;
	private static boolean isScreenOpen = false;
	private static boolean isInCall = false;

	private MalibuCountDownTimer countDownTimer;
	Context context;

	protected static final String PREFS_NAME = "MyPrefsFile";
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editor;
	public static int REPEAT_COUNT = 5;
	public int RECORD_COUNT = 3;
	static long oldTime = 0;

	public SOSReceiver()
	{

	}

	@Override
	public void onReceive(final Context context, Intent intent)
	{
		devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		demoDeviceAdmin = new ComponentName(context, DemoDeviceAdminReceiver.class);
		this.context = context;
/*		if(devicePolicyManager.isAdminActive(demoDeviceAdmin) && !CallHelper.Ds.structPC.bTimeExpired) {
			DeBug.ShowLog("MobiSOS", "countPowerOff " + ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SMS_TOCONT_NO) == eFeatureControl.SMS_TOCONT_NO));
			if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.SMS_TOCONT_NO) == eFeatureControl.SMS_TOCONT_NO)
				;
			else {
				countPowerOff = 0;
				return;
			}
			DeBug.ShowLog("MobiSOS", "countPowerOff " + countPowerOff);
			if (countPowerOff == 0 && timerHasStopped) {
				countDownTimer = new MalibuCountDownTimer(maxtime, interval);
				countDownTimer.start();
				timerHasStopped = false;
			}
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) && !isInCall) {
				DeBug.ShowLog("MobiSOS", "In Method:  ACTION_SCREEN_OFF");
				DeBug.ShowLog("MobiSosTest", "Screen Turned OFF "+System.currentTimeMillis());
				if(isScreenOpen) {
					sosPressed();
					isScreenOpen = false;
				}
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && !isInCall) {
				DeBug.ShowLog("MobiSosTest", "Screen Turned ON "+System.currentTimeMillis());
					if (!isScreenOpen) {
						sosPressed();
						isScreenOpen = true;
					}
			} else if (intent.getAction().equals("android.intent.action.PHONE_STATE")){
				Bundle extras = intent.getExtras();
				String state = extras.getString(TelephonyManager.EXTRA_STATE);
				DeBug.ShowLog("MobiSOS", "In Method:  ACTION_CALL : "+state);
				if(state!=null && (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) || state.equals(TelephonyManager.EXTRA_STATE_RINGING))){
					isInCall = true;
				}else if(state!=null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
					isInCall = false;
				}
			}
		}*/
	}

	public void sosPressed(){
		if ((System.currentTimeMillis() - oldTime) < 500) {
			oldTime = System.currentTimeMillis();
			return;
		}

		oldTime = System.currentTimeMillis();
		countPowerOff++;
		DeBug.ShowLog("MobiSOS", "In Method:  ACTION_SCREEN_ON");
		DeBug.ShowLog("MobiSOS", "ACTION_SCREEN_ON countPowerOff " + countPowerOff);

		if ((countPowerOff >= REPEAT_COUNT && !timerHasStopped)) {
			countDownTimer.cancel();
			countPowerOff = 0;
			timerHasStopped = true;
			isScreenOpen = false;
//			isForSos = false;

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
			else
				settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

			editor = settings.edit();


			if(!(settings!=null && settings.getBoolean("sosAuto", CallHelper.Ds.structFCC.IsSosAutoAnswer)) && (CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.AUTO_PHONE_LOCK) == eFeatureControl.AUTO_PHONE_LOCK) {
				editor.putBoolean("unlockedSOS", false);
				editor.putBoolean("isLockEnabled", true);
				editor.commit();
			}
			if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.BACK_CAMERA_REC) == eFeatureControl.BACK_CAMERA_REC)
				for (int i = 0; i < RECORD_COUNT; i++) {
					CallHelper.sosOpened = System.currentTimeMillis();
					Intent intentService1 = new Intent(CallDetectService.callDetectService, AudioRecorderIntentService.class);
					intentService1.putExtra("whatToRecord", (int) AudioRecorderIntentService.VIDEO_BACK);
					intentService1.putExtra("recordtimeInSeconds", (int) 15);
					intentService1.putExtra("isForSOS", (int) 1);
					context.startService(intentService1);
				}
			else if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.FRONT_CAMERA_REC) == eFeatureControl.FRONT_CAMERA_REC)
				for (int i = 0; i < RECORD_COUNT; i++) {
					CallHelper.sosOpened = System.currentTimeMillis();
					Intent intentService11 = new Intent(CallDetectService.callDetectService, AudioRecorderIntentService.class);
					intentService11.putExtra("whatToRecord", (int) AudioRecorderIntentService.VIDEO_FRONT);
					intentService11.putExtra("recordtimeInSeconds", (int) 15);
					intentService11.putExtra("isForSOS", (int) 1);
					context.startService(intentService11);

				}
			else if ((CallHelper.Ds.structCCC.wFeatureControlWord[0] & eFeatureControl.AUTO_VOICE_REC) == eFeatureControl.AUTO_VOICE_REC)
				for (int i = 0; i < RECORD_COUNT; i++) {
					CallHelper.sosOpened = System.currentTimeMillis();
					Intent intentService = new Intent(CallDetectService.callDetectService, AudioRecorderIntentService.class);
					intentService.putExtra("whatToRecord", (int) AudioRecorderIntentService.AUDIO);
					intentService.putExtra("recordtimeInSeconds", (int) 15);
					intentService.putExtra("isForSOS", (int) 1);
					context.startService(intentService);
				}
			new Handler().post(new Runnable() {
				public void run() {
					try {
						DeBug.ShowLog("MobiSOS", "waiting for location");
						startNotification(context);

						CallHelper.Ds.structSosControlMode.isInSosMode = true;
						CallHelper.Ds.structSosControlMode.lSosStartTimeInMilies = System.currentTimeMillis();

						editor.putBoolean("Ds.structSosControlMode.isInSosMode", CallHelper.Ds.structSosControlMode.isInSosMode);
						editor.putLong("Ds.structSosControlMode.lSosStartTimeInMilies", CallHelper.Ds.structSosControlMode.lSosStartTimeInMilies);
						editor.commit();
						{
							LocationDetails locationDetails = new LocationDetails(context);
							locationDetails.sos();
						}

						countDownTimer.cancel();
						countPowerOff = 0;

					} catch (Exception e) {
						e.printStackTrace();
						countDownTimer.cancel();
						countPowerOff = 0;
					}
				}
			});

		}
	}


	@SuppressLint("NewApi")
	private void startNotification(Context context) {
//		// TODO Auto-generated method stub
//		//Creating Notification Builder
//		notification = new NotificationCompat.Builder(context);
//		//Title for Notification
//		notification.setContentTitle("SOS Alert");
//		notification.setVibrate(new long[] { -100, 5000 });
//		//Message in the Notification
//		notification.setContentText("SOS alert genarated.");
//		//Alert shown when Notification is received
//		notification.setTicker("SOS");
//		//Icon to be set on Notification
//		notification.setSmallIcon(R.drawable.ic_launcher);
//		//Creating new Stack Builder
//		stackBuilder = TaskStackBuilder.create(context);
//		stackBuilder.addParentStack(Result.class);
//		//Intent which is opened when notification is clicked
//		resultIntent = new Intent(context, Result.class);
//		stackBuilder.addNextIntent(resultIntent);
//		pIntent =  stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
//		notification.setContentIntent(pIntent);
//		manager =(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//
//		Notification n = notification.build();
//		manager.notify(1, n);

		Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(3000);

	}

	public class MalibuCountDownTimer extends CountDownTimer
	{

		public MalibuCountDownTimer(long maxtime, long interval)
		{
			super(maxtime, interval);
			countPowerOff=0;
		}

		@Override
		public void onFinish()
		{
			timerHasStopped = true;
			isScreenOpen = false;
			/*DeBug.ShowLog("Finish" ,""+ String.valueOf(timeElapsed));*/
			countPowerOff=0;
//			isForSos = false;
			DeBug.ShowLog("MobiSOS" ,"onFinish countPowerOff "+ countPowerOff);

		}



		@Override
		public void onTick(long millisUntilFinished)
		{
			DeBug.ShowLog("MobiSOS","Time remain:"+ millisUntilFinished);
			timeElapsed = maxtime - millisUntilFinished;
			DeBug.ShowLog("MobiSOS" ,"Time Elapsed:"+ String.valueOf(maxtime));
		}
	}

}
