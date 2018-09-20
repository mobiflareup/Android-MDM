package com.mobiocean.receiver;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.mobiocean.service.CallDetectService;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
/**
 *Test HandSet Model     = s01
 *ADT Pakage Version     = 21.0.1.201212060302
 *Eclipse Platform       = 4.2.1.v20120814
 *Date					 = October 17,2013
 *Functionality			 = Device Admin Receiver 
 *						  				
 *Android version		 = 2.3.6 [Gingerbread (API level 10)]
 */

public class DemoDeviceAdminReceiver extends DeviceAdminReceiver 
{

	static int count;
	static final String TAG = "DemoDeviceAdminReceiver";

	/** Called when this application is approved to be a device administrator. */
	@Override
	public void onEnabled(Context context, Intent intent) 
	{
		super.onEnabled(context, intent);
		try {
			final Handler h=new Handler();
			h.postDelayed(new Runnable() 
			{
				@Override
				public void run() 
				{
					CallHelper.Ds.structPC.bDeviceAdminEnabled = true;
					CallDetectService.callDetectService.editor.putBoolean("structPC.bDeviceAdminEnabled", CallHelper.Ds.structPC.bDeviceAdminEnabled);
					CallDetectService.callDetectService.editor.commit();
					DeBug.ShowLogD(TAG, "onEnabled  " +CallHelper.Ds.structPC.bDeviceAdminEnabled);
				}
			}, 3000);
			
			} catch (Exception e1)
			{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			}
		DeBug.ShowToast(context, "onEnabled");
	}

	/** Called when this application is no longer the device administrator. */
	@Override
	public void onDisabled(Context context, Intent intent) 
	{
		super.onDisabled(context, intent);
		DeBug.ShowToast(context, "onDisabled");
		DeBug.ShowLogD(TAG, "onDisabled");
	}

	@Override
	public CharSequence onDisableRequested(Context context, Intent intent) 
	{
		
		/*intent= new Intent(context,Abc.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);*/
		
		
		/*new Thread(new Runnable()
		{
			public void run() 
			{
			//	while(true)
				{
					CallDetectService.devicePolicyManager.lockNow(); 
				}
			}
		}).start();*/
		return "Want to remove Nimboli as deviceadmin";
	}

	@Override
	public void onPasswordChanged(Context context, Intent intent) 
	{
		super.onPasswordChanged(context, intent);
		DeBug.ShowLogD(TAG, "onPasswordChanged");
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context, intent);
	}

	@Override
	public void onPasswordFailed(Context context, Intent intent) 
	{
		//Receives when wrong password is greater than 3 points in case of pattern matching
		DeBug.ShowLogD(TAG, "onPasswordFailed");
		//count=AndroidCalls.devicePolicyManager.getCurrentFailedPasswordAttempts();
	}

	@Override
	public void onPasswordSucceeded(Context context, Intent intent) {
		super.onPasswordSucceeded(context, intent);
		DeBug.ShowLogD(TAG, "onPasswordSucceeded");
		DeBug.ShowToast(context, " My Failed "+count+" Times ");
		count=0;
	}

}
