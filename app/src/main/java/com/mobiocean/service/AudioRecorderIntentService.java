package com.mobiocean.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.mobiocean.R;
import com.mobiocean.mobidb.SOSandTheftInfoDB;
import com.mobiocean.mobidb.SOSandTheftInfoStruct;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;

import org.sn.location.LocationBean;
import org.sn.location.LocationDetails;

import java.io.File;
import java.io.IOException;

public class  AudioRecorderIntentService extends IntentService {


	private static final String TAG = AudioRecorderIntentService.class.getSimpleName();

	private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
	private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";


	private static final int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP };
	private static final String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP };

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	public static final int AUDIO       = 0;
	public static final int VIDEO_FRONT = 1;
	public static final int VIDEO_BACK  = 2;
	private static final String AUDIO_FOLDER = "Mobiocean/audio";
	private static final String VIDEO_FOLDER = "Mobiocean/video";
	
	private MediaRecorder recorder = null;	
	public static SurfaceView mSurfaceView;
	public static SurfaceHolder mSurfaceHolder;
	public static WindowManager wm;
	public static View myview;
	Camera mCamera;
	
	private int whatToRecord = 0;
	private int currentFormat = 0;
	private int isForSOS = 1;
	private int recordtimeInSeconds = 15;
	
	public AudioRecorderIntentService(String name) 
	{
		super(name);
	}

	public AudioRecorderIntentService() 
	{
		super("AudioRecorderIntentService");
	}

	@Override
	public void onCreate() {
		CallHelper.createFolder(AUDIO_FOLDER);
		CallHelper.createFolder(VIDEO_FOLDER);
		createVideoSarface();
		DeBug.ShowLogD(TAG, "onCreate");
		super.onCreate();
	}


	@Override
	protected void onHandleIntent(Intent intent) {

		
		whatToRecord        = intent.getIntExtra("whatToRecord", whatToRecord);
		recordtimeInSeconds = intent.getIntExtra("recordtimeInSeconds", recordtimeInSeconds);
		isForSOS            = intent.getIntExtra("isForSOS", isForSOS);
		int recordCount     = (recordtimeInSeconds * 1000)/50;
		
		DeBug.ShowLogD(TAG, "onHandleIntent Start "+whatToRecord);
		String fileName ="";
		if(whatToRecord==AUDIO)
		{
			fileName = startAudioRecording();
		}
		else if(whatToRecord==VIDEO_FRONT)
		{
			CallHelper.SO(1);
			fileName = startVideoRecorder(Camera.CameraInfo.CAMERA_FACING_FRONT);
			if(TextUtils.isEmpty(fileName))
			{
				whatToRecord = AUDIO;
				fileName = startAudioRecording();
			}
		}
		else if(whatToRecord==VIDEO_BACK)
		{
			CallHelper.SO(1);
			fileName = startVideoRecorder(Camera.CameraInfo.CAMERA_FACING_BACK);
			if(TextUtils.isEmpty(fileName))
			{
				whatToRecord = AUDIO;
				fileName = startAudioRecording();
			}
		}


		int count = 0;
		while(count <= recordCount)
		{
			try {
				Thread.sleep(50);
				count++;
				if(count==1 && (whatToRecord==VIDEO_FRONT||whatToRecord==VIDEO_BACK))
					CallHelper.SO(0);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		int isAudio = 0;
		SOSandTheftInfoStruct mInfoStruct = new SOSandTheftInfoStruct();
		if(whatToRecord==AUDIO)
		{
			stopAudioRecording();	
			isAudio = 1;
		}
		else if(whatToRecord==VIDEO_FRONT)
		{

			CallHelper.SO(1);
			stopVideoRecorder();
			CallHelper.SO(0);

		}
		else if(whatToRecord==VIDEO_BACK)
		{
			CallHelper.SO(1);
			stopVideoRecorder();
			CallHelper.SO(0);

		}
		String tempadd="";
		if(isForSOS == 1)
		{
			tempadd = "/SOSVideo/";
			if (isAudio == 1)
				tempadd = "/SOSAudio/";
		}
		else
		{
			tempadd="/TheftVideo/";
			if(isAudio==1)
				tempadd="/TheftAudio/";
		}
		String endfileName = fileName.substring( fileName.lastIndexOf('/')+1, fileName.length() );
		tempadd = "/"+CallHelper.Ds.structPC.iStudId+tempadd+endfileName;

		LocationBean locationBean = new LocationDetails(getBaseContext()).getLocation();
		mInfoStruct.setAppId(CallHelper.Ds.structPC.iStudId);
		mInfoStruct.setLocalFilePath(fileName);
		mInfoStruct.setFilePath(tempadd);
		mInfoStruct.setIsAudio(isAudio);
		mInfoStruct.setIsforSos(isForSOS);
		mInfoStruct.setFileName(endfileName);
		mInfoStruct.setIsUploaded(0);
		mInfoStruct.setLogDateTime(""+System.currentTimeMillis());
		mInfoStruct.setCellId(locationBean.CellId);
		mInfoStruct.setMobileNetworkCode(locationBean.MNC);
		mInfoStruct.setMobileCountryCode(locationBean.MCC);
		mInfoStruct.setLocationAreaCode(locationBean.LAC);
		mInfoStruct.setLongitude(locationBean.Longt);
		mInfoStruct.setLatitude(locationBean.Lat);
		SOSandTheftInfoDB mDb = SOSandTheftInfoDB.getInstance(this);
	//	mDb.deleteDB();
		mDb.addData(mInfoStruct);
		
		startService(new Intent (this,UploadFileToserverIntentService.class));

		DeBug.ShowLogD("SOSRCV", "onHandleIntent Stop  "+whatToRecord);
		
		DeBug.ShowLogD(TAG, "onHandleIntent Stop  "+whatToRecord);

	}
	private String startAudioRecording()
	{
		String fileName = "";
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

		recorder.setOutputFormat(output_formats[currentFormat]);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		fileName= getAudioFilename();
		recorder.setOutputFile(fileName);
		recorder.setOnErrorListener(errorListener);
		recorder.setOnInfoListener(infoListener);
		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return fileName;
	}
	private void stopAudioRecording() {
		try {
			if (null != recorder) {
				recorder.stop();
				recorder.reset();
				recorder.release();
				recorder = null;
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private String startVideoRecorder(int whichCamera){
		
		String fileName = "";
		if(recorder == null)
			recorder = new MediaRecorder();


		if(mCamera == null) {
			mCamera = openRequiredFacingCamera(whichCamera);
			if(mCamera == null )
				return null;
			mCamera.unlock();
		}
		recorder.setCamera(mCamera);

		// Step 2: Set sources
		recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));

		// Step 4: Set output file
		fileName=getVideoFilename().toString();
		recorder.setOutputFile(fileName);

		recorder.setPreviewDisplay(mSurfaceHolder.getSurface());
		// Step 6: Prepare configured MediaRecorder
		try {
			recorder.prepare();
			recorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
	}
	private void stopVideoRecorder()
	{
		if (recorder != null) {
			recorder.reset();   // clear recorder configuration
			recorder.release(); // release the recorder object
			recorder = null;
			if (mCamera != null)
			mCamera.lock();           // lock camera for later use
		}
		if (mCamera != null){
			mCamera.release();        // release the camera for other applications
			mCamera = null;
		}
	}
	
	private String getAudioFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath()+File.separator+AUDIO_FOLDER;
		/* File file = new File(filepath, AUDIO_RECORDER_FOLDER);
	    if (!file.exists()) {
	        file.mkdirs();
	    }*/
		return (filepath + File.separator + System.currentTimeMillis() + file_exts[currentFormat]);
	}
	private String getVideoFilename() {
		String filepath = Environment.getExternalStorageDirectory().getPath()+File.separator+VIDEO_FOLDER;
		/* File file = new File(filepath, AUDIO_RECORDER_FOLDER);
	    if (!file.exists()) {
	        file.mkdirs();
	    }*/
		return (filepath + File.separator + System.currentTimeMillis() + file_exts[0]);
	}
	
	private Camera openRequiredFacingCamera(int whichCamera) {
		int cameraCount = 0;
		Camera cam = null;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == whichCamera) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
					DeBug.ShowLog(TAG, "Camera failed to open: " + e.getLocalizedMessage());
				}
			}
		}

		return cam;
	}
	private void createVideoSarface()
	{
		try {
			LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
			myview = li.inflate(R.layout.view_videorecord, null);
			mSurfaceView = (SurfaceView) myview.findViewById(R.id.surfaceView1);
			mSurfaceHolder = mSurfaceView.getHolder();

			wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
			LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
					WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
					PixelFormat.TRANSLUCENT);
			params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

			wm.addView(myview, params);

			mSurfaceView.setZOrderOnTop(true);
			mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
		@Override
		public void onError(MediaRecorder mr, int what, int extra) {
			//Toast.makeText(AudioRecorderIntentService.this, "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
		}
	};

	private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
		@Override
		public void onInfo(MediaRecorder mr, int what, int extra) {
			//Toast.makeText(AudioRecorderIntentService.this, "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
		}
	};


	@Override
	public void onDestroy() {
		wm.removeView(myview);
		DeBug.ShowLogD(TAG, "onDestroy");
		super.onDestroy();
	}

}