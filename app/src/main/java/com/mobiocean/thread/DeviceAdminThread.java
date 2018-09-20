package com.mobiocean.thread;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.mobiocean.service.AppBlock;
import com.mobiocean.ui.PassWordActivity;
import com.mobiocean.util.DeBug;

public class DeviceAdminThread implements Runnable {

	Context context;
	ActivityManager am;
	String packageName;
	String ClassName;
	public DeviceAdminThread(Context context)
	{
		this.context = context;
	}
	@Override
	public void run() 
	{
		AppBlock.DeviceAdminThread_Started=true;
		am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		while(true)
		{
			packageName = am.getRunningTasks(1).get(0).topActivity.getPackageName().toLowerCase();
			ClassName   = am.getRunningTasks(1).get(0).topActivity.getClassName().toLowerCase();
			DeBug.ShowLog("DemoDeviceAdminReceiver", "out of Ifelse  "+ClassName);

			if(ClassName.equals("com.android.settings.deviceadminadd"))
			{
				Intent intent= new Intent(context,PassWordActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
				AppBlock.DeviceAdminThread_Started=false;
				break;
			}
			else if(!ClassName.contains("com.android.settings") && !ClassName.contains("com.android.packageinstaller"))
			{
				DeBug.ShowLog("DemoDeviceAdminReceiver", "Else  "+AppBlock.DeviceAdminThread_Started);
				AppBlock.DeviceAdminThread_Started=false;
				break;
			}
		}
	}

}
