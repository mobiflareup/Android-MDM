package com.mobiocean.receiver;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.mobiocean.database.DatabaseHabdler_SMSSent;
import com.mobiocean.database.DatabaseHandler_Call;
import com.mobiocean.database.DatabaseHandler_SMS;

//import com.gingerbox.ipmsg.main.ConveyanceMainActivity;
//import com.gingerbox.ipmsg.net.Packets;

/**
 *Test HandSet Model     = s01
 *ADT Pakage Version     = 21.0.1.201212060302
 *Eclipse Platform       = 4.2.1.v20120814
 *Date					 = October 17,2013
 *Functionality			 = OneMinuteTimerReceiver  for maintain AppHrMin timer , resend info to server if There is no Internet
 *
 *Android version		 = 2.3.6 [Gingerbread (API level 10)]
 */

public class OneMinuteTimerReceiver extends BroadcastReceiver
{
	public static boolean bOneMinuteTimerStarted=false;
	public static boolean timerRunning =false;

	protected static final String PREFS_NAME = "MyPrefsFile";
	public SharedPreferences settings;
	public SharedPreferences.Editor editor;

	protected static final String BLOCKED_WEBSITES_PREFS_NAME = "blockedWebsitesPref";
	public SharedPreferences blockedWebsitesPreferences;
	public static SharedPreferences.Editor blockedWebsitesPreferenceEditor;

	protected static long oldtimeinmilies=0;

	protected static Notification note;

	protected PendingIntent pi;
	public static int tempAppHrMin = 0;
	public static int prevAppHrMin = 0;
	protected static Context ctx;
	public static boolean servicesrestart=false;
	public static boolean appresarted=true;
	protected static int ServiceStartCounter;

	DatabaseHandler_Call Call_db;
	DatabaseHandler_SMS SMS_db;
	DatabaseHabdler_SMSSent SMSSent_db;
	protected static int responce=0;

	static int  x=0;

	@Override
	public void onReceive(final Context context, Intent arg1)
	{
	}
}
