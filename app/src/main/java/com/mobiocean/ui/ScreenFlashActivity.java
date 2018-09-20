package com.mobiocean.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.Window;

import com.mobiocean.util.DeBug;

import java.io.IOException;

/**
 *Test HandSet Model    = s01
 *ADT Package Version   = 21.0.1.201212060302
 *Eclipse Platform      = 4.2.1.v20120814
 *Date					= October 17,2013
 *Functionality			= Show a Flash Screen when the Call is about to be Disconnected with a POP-UP 				
 *Android version		= 2.3.6 [Gingerbread (API level 10)]
 */
public class ScreenFlashActivity extends Activity
{
	//Variable to store the system brightness
	protected static int brightness;
	//Content resolver used as a handle to the system's settings
	protected static ContentResolver cResolver;
	//Window object, that will store a reference to the current window
	protected static Window window;
	protected boolean zero=true;
	protected boolean full=true;
	protected static Handler handler;

	protected static AudioManager audioManager1 ;
	protected OnAudioFocusChangeListener afChangeListener;

	protected static String Title="";
	protected static String Message="";
	protected static String Message1="";

	protected static MediaPlayer mMediaPlayer;
	protected static Vibrator mVibrator;

	protected static Context ctx;
	
	protected static boolean isRingerPlaying = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		handler= new Handler();
		mMediaPlayer = new MediaPlayer();
		ctx = ScreenFlashActivity.this;
		Intent mIntent = getIntent();
		int IntentValue = mIntent.getIntExtra("AlertBoxIntent",0);
		DeBug.ShowLog("NOLOC",""+IntentValue);
		if(IntentValue == 2 || IntentValue == 3)
		{

			
		//	audioManager1 = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
			AlertDialog mAlert = null ;
			/*if(CallHelper.Ds.structPC.bMode==ePhoneMode.SCHOOL_RESTRICTED)
		{
			Title  = "School Mode";
			Message= "You are in School Mode.";	
			if( !audioManager1.isMusicActive())		            	 
			{
				int result = audioManager1.requestAudioFocus(afChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
			}

		}
		else
		{
			Title  = "Call is being Interrupted";
			Message= "Your daily Call Limit is  expired.";
		}*/

			Title  = "Call is being Interrupted";
			Message= "Your attendance was not proper, please mark again after switching on GPS";
			Message1= "Your attendance was not proper, please mark again after switching on Internet";
			if(IntentValue == 2)
				alert.setMessage(Message);
			else
				alert.setMessage(Message1);
			alert.setCancelable(false);
			alert.setPositiveButton("Accept",new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog,int whichButton)
				{
					//	handler.removeCallbacksAndMessages(null);
					if(mMediaPlayer.isPlaying())
						mMediaPlayer.stop();
					isRingerPlaying = false;
					mVibrator.cancel();
					finish();
				}});
			mAlert = alert.create();
			mAlert.show();
			
			new Thread(new Runnable()
			{
				public void run() 
				{		
					Play_Notification_Tone();		
				}
			}).start();
			
			

			//Screen Flash 
			/*cResolver = getContentResolver();
		window = getWindow();
		try 
		{
			//Get the current system brightness
			brightness = System.getInt(cResolver, System.SCREEN_BRIGHTNESS);
		} 
		catch (SettingNotFoundException e) 
		{
			//Throw an error case it couldn't be retrieved
			DeBug.ShowLogD("Error", "Cannot access system brightness");
			e.printStackTrace();
		}*/
			//Handler to show the flash Activity

			/*Runnable runnable = new Runnable() 
		{
			public void run() 
			{
				if(zero)
				{
					System.putInt(cResolver, System.SCREEN_BRIGHTNESS, 200);
					android.view.WindowManager.LayoutParams layoutpars = window.getAttributes();
					layoutpars.screenBrightness = 200 / (float)255;
					window.setAttributes(layoutpars);
					full=true;
					zero=false;
					DeBug.ShowLog("Brightness","brightness 200" );
				}
				else if(full)
				{
					System.putInt(cResolver, System.SCREEN_BRIGHTNESS, 20);
					android.view.WindowManager.LayoutParams layoutpars = window.getAttributes();
					layoutpars.screenBrightness = 20 / (float)255;
					window.setAttributes(layoutpars);
					zero=true;
					full=false;
				}
				handler.postDelayed(this, 1000);
			}
		};
		runnable.run();
			 */		





		}
		super.onResume();
	}

	@Override
	protected void onPause() 
	{
		//	 handler.removeCallbacksAndMessages(null);
		try {
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			isRingerPlaying = false;
			mVibrator.cancel();
			finish();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onPause();
	}
	protected class afChangeListener implements OnAudioFocusChangeListener 
	{
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				DeBug.ShowToast(getApplicationContext(), "AudioManager.AUDIOFOCUS_GAIN");
			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
			}
		}
	};

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_DOWN) 
		{
			switch (event.getKeyCode()) 
			{
			case KeyEvent.KEYCODE_BACK:
				return true;
			case KeyEvent.KEYCODE_HOME:
				return true;
			}
		} 
		else if (event.getAction() == KeyEvent.ACTION_UP)
		{
			switch (event.getKeyCode())
			{
			case KeyEvent.KEYCODE_BACK:
				//	 handler.removeCallbacksAndMessages(null);
				if(mMediaPlayer.isPlaying())
					mMediaPlayer.stop();
				isRingerPlaying = false;
				mVibrator.cancel();
				finish();
				return true;
			case KeyEvent.KEYCODE_HOME:
				//	 handler.removeCallbacksAndMessages(null);
				finish();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}


	protected static void Play_Notification_Tone()
	{
		mVibrator = (Vibrator) ctx.getSystemService(ctx.VIBRATOR_SERVICE);
		// Vibrate for 500 milliseconds
		mVibrator.vibrate(10000);
		isRingerPlaying = true;
		mMediaPlayer = new MediaPlayer();
		AudioManager audioManager = (AudioManager)ctx.getSystemService(ctx.AUDIO_SERVICE);

		int maxVolumeMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolumeMusic,AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		// Log.i("EXP",""+maxVolumeMusic);
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		try {
			mMediaPlayer.setDataSource(ctx, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			//mMediaPlayer.setLooping(false);
			mMediaPlayer.prepare();
			for(int i=0;i<50;i++)
			{
				Thread.sleep(1000);
				if(!mMediaPlayer.isPlaying())
				{
					mMediaPlayer.start();
				}
				if(!isRingerPlaying)
				{
					if(!mMediaPlayer.isPlaying())
						mMediaPlayer.stop();
						break;
				}
				
			}

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			DeBug.ShowLog("EXP", "exp1  " +e.getMessage());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			DeBug.ShowLog("EXP", "exp2  " +e.getMessage());
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			DeBug.ShowLog("EXP", "exp3  " +e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			DeBug.ShowLog("EXP", "exp4  " +e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			DeBug.ShowLog("EXP", "exp5  " +e.getMessage());
		}
	}





}
