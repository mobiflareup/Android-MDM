package com.mobiocean.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
*Test HandSet Model     = s01
*ADT Pakage Version     = 21.0.1.201212060302
*Eclipse Platform       = 4.2.1.v20120814
*Date					= April 29,2013
*Functionality			= Managing the time Whenever user Changes the Android Phone Time					
 *Android version		= 2.3.6 [Gingerbread (API level 10)]
 */
public class TimeChangedReceiver extends BroadcastReceiver 
{
//	protected static final String PREFS_NAME = "MyPrefsFile";
//	public SharedPreferences settings;
//	public SharedPreferences.Editor editor;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) 
	{
//		settings = arg0.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
//		editor = settings.edit();
//
//		SystemClock.elapsedRealtime();
//		System.currentTimeMillis();
//		CallHelper.Ds.structPC.bDateChanged = true;
	

//		CallHelper.TimeChangedCounter ++;
//		try
//		{	AlarmManager alarmManager;
//			alarmManager = (AlarmManager) arg0.getSystemService(Context.ALARM_SERVICE);
//			alarmManager.cancel(pendingintent);
//			Intent intentTimer = new Intent(arg0, OneMinuteTimerReceiver.class);
//			pendingintent = PendingIntent.getBroadcast(arg0, 0, intentTimer, 0);
//			/*alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 , pendingintent);*/
//			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 1000 * 60 , pendingintent);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		OneMinuteTimerReceiver.getTimeFromServer(arg0);
	}
}
