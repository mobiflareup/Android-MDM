package com.mobiocean.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;

public class UpdateService extends GcmTaskService {

	private static final String TAG = UpdateService.class.getSimpleName();
	private static final String GCM_REPEAT_TAG = "repeat|[60]";

	protected static final String PREFS_NAME = "MyPrefsFile";
	public SharedPreferences settings;
	protected SharedPreferences.Editor editor;
	
	@Override
	public int onRunTask(TaskParams arg0) {
		
		settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		editor   = settings.edit();
		  DeBug.ShowLogD(TAG, "in onRunTask");
		CallHelper.Ds.structPC.iStudId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
		if (!TextUtils.isEmpty(CallHelper.Ds.structPC.iStudId))
			startService(new Intent(this, SyncIntentService.class));
		return  GcmNetworkManager.RESULT_SUCCESS;
	}

	  public static void scheduleRepeat(Context context) {
			//in this method, single Repeating task is scheduled (the target service that will be called is MyTaskService.class)
		        try {
		            PeriodicTask periodic = new PeriodicTask.Builder()
				        //specify target service - must extend GcmTaskService
		                .setService(UpdateService.class)
				        //repeat every 3600 seconds i.e. 1hour
		                .setPeriod(3600)
				        //specify how much earlier the task can be executed (in seconds)
		                .setFlex(30)
		                //tag that is unique to this task (can be used to cancel task)
		                .setTag(GCM_REPEAT_TAG)
				        //whether the task persists after device reboot
		                .setPersisted(true)
				        //if another task with same tag is already scheduled, replace it with this task
		                .setUpdateCurrent(true)
				        //set required network state, this line is optional
		                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
				        //request that charging must be connected, this line is optional
		                .setRequiresCharging(false)
		                .build();
		            GcmNetworkManager.getInstance(context).schedule(periodic);
		            DeBug.ShowLogD(TAG, "repeating task scheduled");
		        } catch (Exception e) {
		        	DeBug.ShowLogD(TAG, "scheduling failed");
		            e.printStackTrace();
		        }
		    }
	  
	  public static void cancelRepeat(Context context) {
	        GcmNetworkManager
	                .getInstance(context)
	                .cancelTask(GCM_REPEAT_TAG, UpdateService.class);
	    }
}
