package com.mobiocean.service;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.TelephonyInfo;
import com.mobiocean.ui.Connectivity;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;
import com.mobiocean.util.DualSimManager;
import com.mobiocean.util.NetworkUtil;
import com.mobiocean.util.RestApiCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DeviceInfoIntentService extends IntentService {

	private static final String TAG = DeviceInfoIntentService.class.getSimpleName();
	public static boolean isStarted = false;
	PackageManager pm;
	NfcManager manager;
	
	// os info
	static String osApiLevel="";
	static String osBrand="";
	static String osProcessor1="";
	static String osProcessor2="";
	static String osBuildId="";
	static String osManufacturer="";
	static String osModel="";
	static String osProduct="";
	static String osDevice="";
	static String osVersion="";
	static String backCamera="";
	static String frontCamera="";
	static String sensorName="";
	static String sensorVendor="";
	static String processorname = "";
	static Camera camera = null;
	static String maxFreq = "-";
	static SensorManager mgr = null;

	// battery info
	static String batteryHealth = "";
	static String batterystatus = "";
	static String technology = "";
	static int voltage = 0;
	static int temperature = 0;
	static String value = "";
	static ArrayList<SensorInfo> mSensorInfoList = new ArrayList<SensorInfo>();

	// wifi connectivity
	static boolean ConnectedWifi;
	static boolean ConnectedMobile;
	static String  connectivityName ="";
	static String connectivityType ="";
	static boolean  Roaming ;
	static boolean IsConnectedToProvisioningNetwork ;
	static String NetworkTypeInfo ="";
	static String roaming ="";
	static String isconnectedtoprovisioningnetwork ="";
	static DetailedState State;
	static String status ="";

	// sim and network info
	static String OperatorName1 = "";
	static String OperatorName2 = "";
	static String imsiSIM1 = "";
	static String imsiSIM2 = "";
	static boolean isSIM1Ready;
	static boolean isSIM2Ready;
	static boolean isDualSIM;

	static String dualSim = "";
	static String sim1 = "";
	static String sim2 = "";

	static String connectedWifi="";
	static String connectednetwork="";

	static TelephonyManager mtelephonyManager;
	static String stSIMSerialno = "";

	//	public static String TAG = MainMenuActivity;
	// Memory Variables
	static long availableMegs = 0;
	static long availableMegs1 = 0;

	static String jvmMaxMemory = "";
	
	static Context ctx;
	public DeviceInfoIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	public DeviceInfoIntentService() {
		super("DeviceInfoIntentService");
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public void onCreate() {
		isStarted = true;
		DeBug.ShowLogD(TAG, ""+isStarted);
		ctx = this;
		
		pm = getPackageManager(); 
		manager = (NfcManager)getSystemService(Context.NFC_SERVICE);
		
		super.onCreate();
	}
	@Override
	public void onDestroy() {
		isStarted = false;
		DeBug.ShowLogD(TAG, ""+isStarted);
		super.onDestroy();
	}
	@Override
	protected void onHandleIntent(Intent arg0) {

		RestApiCall mRestApiCall= new RestApiCall();

		//while(!intilizationComplited);

		String  CurrentTime = ""+System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(CurrentTime));
		DateFormat formatter1 = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		String SMSTimeStamp = formatter1.format(calendar.getTime()).toString();

		DeBug.ShowLog("Test","IN thread  "+CallHelper.Ds.structPC.iStudId);

		DeBug.ShowLog(TAG, "CallHelper.Ds.structPC.iStudId "+ CallHelper.Ds.structPC.iStudId);
		/*			WebserviceCall WSC = new WebserviceCall();
		 */			
		//	 LKONagarNigam.init(MainMenuActivity.this);
		if(!CallHelper.Ds.structPC.iStudId.equals(""))
		{		

			boolean isBT = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH); 
			boolean isBLE = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
			String bluetooth = "";
			if(isBT)
				bluetooth = "Yes";
			if(isBLE)
				bluetooth = bluetooth+", with BLE.";

			String NFC = "No";
			NfcAdapter adapter = manager.getDefaultAdapter();
			if (adapter != null ) {
				NFC = "Yes";
			}

			/** hardwareInformation();**/
			try {
				hardwareInformation();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			getSimInfo();

			JSONArray jsonArrayHardware = new JSONArray();

			for(int i=0 ;i<mSensorInfoList.size();i++)
			{
				JSONObject jsonApp= new JSONObject();
				SensorInfo mSensorInfo = new SensorInfo();

				//String resDocName  = mSensorInfoList.get(i).sensorName;
				//String resDocNumber = mSensorInfoList.get(i).sensorValue;

				mSensorInfo = mSensorInfoList.get(i);
				try 
				{
					jsonApp.put("SensorName",mSensorInfo.sensorName);
					jsonApp.put("SensorValue",mSensorInfo.sensorValue);
				}
				catch (JSONException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				jsonArrayHardware.put(jsonApp);	
				//DeBug.ShowLog(TAG, "sensorName "+jsonArrayHardware);
			}
			String totalInternalMenory = getTotalInternalMemorySize();
			String externalMemory = getTotalExternalMemorySize();
			boolean externalSDSCard = externalMemoryAvailable();
			memeoryInformation();
			String sdCard = "";
			if(externalSDSCard)
			{
				sdCard = "Yes";
			}
			else{
				sdCard = "No";
			}

			/*JSONObject jsonMemoryInfo= new JSONObject();
			try {
				jsonMemoryInfo.put("AndroidAppId", CallHelper.Ds.structPC.iStudId);

				jsonMemoryInfo.put("LogDateTime", SMSTimeStamp);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			DeBug.ShowLog("BusinessCardData", "jsonMemoryInfo "  +jsonMemoryInfo);*/

			//	 mRestApiCall.uploadMemoryInfo(getBaseContext(), jsonMemoryInfo); 
			JSONObject jsonObjHarware= new JSONObject();
			try {
				jsonObjHarware.put("Stdnt_Id", "");
				jsonObjHarware.put("AppId", CallHelper.Ds.structPC.iStudId);
				jsonObjHarware.put("DeviceModel", osModel);
				jsonObjHarware.put("Manufacturer", osManufacturer);
				jsonObjHarware.put("DeviceName", osDevice);
				jsonObjHarware.put("Product", osProduct);
				jsonObjHarware.put("Brand", osBrand);
				jsonObjHarware.put("OSVersion", osVersion);
				jsonObjHarware.put("APILevel", osApiLevel);
				jsonObjHarware.put("BuildID", osBuildId);

				jsonObjHarware.put("Processor", processorname);
				jsonObjHarware.put("ProcessorCores", osProcessor2);
				jsonObjHarware.put("MaxFrequency", maxFreq);
				jsonObjHarware.put("InstructionSets", osProcessor1);

				jsonObjHarware.put("SIMDInstructions", "");
				jsonObjHarware.put("RearCamera", backCamera);
				jsonObjHarware.put("FrontCamera", frontCamera);
				jsonObjHarware.put("Sensors", jsonArrayHardware.toString());
				jsonObjHarware.put("Bluetooth", bluetooth);
				jsonObjHarware.put("NFC", NFC);

				jsonObjHarware.put("InternalMemory", totalInternalMenory);
				jsonObjHarware.put("ExternalMemory", externalMemory);
				jsonObjHarware.put("IsExternalSDCard", sdCard);
				jsonObjHarware.put("RAMSize", ""+availableMegs1+"MB");
				jsonObjHarware.put("AvailableRAMSize", ""+availableMegs+"MB");
				jsonObjHarware.put("JVMMaxMemory", jvmMaxMemory);

				jsonObjHarware.put("TelecomProvider1", OperatorName1);
				jsonObjHarware.put("TelecomProvider2", OperatorName2);
				jsonObjHarware.put("IMEINo1", imsiSIM1);
				jsonObjHarware.put("IMEINo2", imsiSIM2);
				jsonObjHarware.put("SIMNo1", stSIMSerialno);
				jsonObjHarware.put("SIMNo2", "");
				jsonObjHarware.put("IsDualSIM", dualSim);
				jsonObjHarware.put("IsSIM2Ready", sim2);
				jsonObjHarware.put("IsSIM1Ready", sim1);

				jsonObjHarware.put("LogDateTime", SMSTimeStamp);

			} catch (JSONException e) {
				e.printStackTrace();
			}


			//      DeBug.ShowLog(TAG, "Result "+hardwareInfo);
			//	      memeoryInformation();
			//		  String internalMemorySize = getTotalInternalMemorySize();
			//		  String externalMemorySize = getTotalExternalMemorySize();
			//	      DeBug.ShowLog(TAG, "internalMemorySize" +internalMemorySize+ "externalMemorySize"+externalMemorySize);

			batteryInformation();

			JSONObject jsonBatteryInfo= new JSONObject();
			try {
				jsonBatteryInfo.put("AppId", CallHelper.Ds.structPC.iStudId);
				jsonBatteryInfo.put("Voltage", voltage);
				jsonBatteryInfo.put("Temperature", temperature);
				jsonBatteryInfo.put("BatteryPercent", value);
				jsonBatteryInfo.put("BatteryStatus", batterystatus);
				jsonBatteryInfo.put("BatteryHealth", batteryHealth);
				jsonBatteryInfo.put("Technology", technology);
				jsonBatteryInfo.put("LogDateTime", SMSTimeStamp);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//DeviceInfo/InsertBatteryInfo---(AppId,DeviceId, Battery_Info, Voltage, Temperature, BatteryPercent, BatteryStatus,BatteryHealth)

			// DeBug.ShowLog("BusinessCardData", "batteryInfo" + batteryInfo + "jsonBatteryInfo "  +jsonBatteryInfo);

			getNetworkInfo();

			JSONObject jsonNetworkInfo= new JSONObject();
			try {
				jsonNetworkInfo.put("AppId", CallHelper.Ds.structPC.iStudId);										
				jsonNetworkInfo.put("NetworkStatus", status);
				jsonNetworkInfo.put("Roaming", roaming);
				jsonNetworkInfo.put("NetworkTypeInfo", NetworkTypeInfo);
				jsonNetworkInfo.put("IsConnectedToProvisioningNetwork", isconnectedtoprovisioningnetwork);
				jsonNetworkInfo.put("NetworkStrength", State);

				jsonNetworkInfo.put("LogDateTime", SMSTimeStamp);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			DeBug.ShowLog("BusinessCardData", "jsonNetworkInfo "  +jsonNetworkInfo);

			JSONObject connectivityInfo= new JSONObject();
			try {

				connectivityInfo.put("AppId", CallHelper.Ds.structPC.iStudId);					
				connectivityInfo.put("ConnectivityName", connectivityName);
				connectivityInfo.put("ConnectivityType", connectivityType);					
				connectivityInfo.put("ConnectedToWiFi", connectedWifi);
				connectivityInfo.put("ConnectedToMobile", connectednetwork);

				connectivityInfo.put("State", State);
				connectivityInfo.put("LogDateTime", SMSTimeStamp);

			} catch (JSONException e) {
				e.printStackTrace();
			}
			DeBug.ShowLog("BusinessCardData", "jsonSimInfo "  +connectivityInfo);

			mRestApiCall.uploadHarwareInfo(jsonObjHarware);
			mRestApiCall.uploadBatteryInfo(jsonBatteryInfo);
			mRestApiCall.uploadNetworkInfo(jsonNetworkInfo);
			mRestApiCall.updateInternetConnectivity(connectivityInfo);

		}

	
	

	}
	@SuppressWarnings("deprecation")
	public static void hardwareInformation()
	{
		osApiLevel = ""+Build.VERSION.SDK_INT;
		osBrand = Build.BRAND;
		osProcessor1 = Build.CPU_ABI;
		osProcessor2 = Build.CPU_ABI2;
		osBuildId = Build.ID;
		osManufacturer = Build.MANUFACTURER;
		osModel = Build.MODEL;
		osProduct = Build.PRODUCT;
		osDevice = Build.DEVICE ;
		//	osVersion = System.getProperty("os.version");
		osVersion = Build.VERSION.RELEASE;

		PackageManager packageManager = ctx.getPackageManager();
		boolean result1 ;

		try {
			camera=Camera.open(0);    // For Back Camera
			android.hardware.Camera.Parameters params = camera.getParameters();
			List sizes = params.getSupportedPictureSizes();
			Camera.Size  result = null;

			ArrayList<Integer> arrayListForWidth = new ArrayList<Integer>();
			ArrayList<Integer> arrayListForHeight = new ArrayList<Integer>();

			for (int i=0;i<sizes.size();i++){
				result = (Size) sizes.get(i);
				arrayListForWidth.add(result.width);
				arrayListForHeight.add(result.height);

				System.out.println("BACK PictureSize Supported Size: " + result.width + "height : " + result.height);  
			} 

			if(arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0){

				backCamera = ""+(((Collections.max(arrayListForWidth)) * (Collections.max(arrayListForHeight))) / 1024000 )+"Mpx";
			}

			if (camera!=null)
			{
				camera.stopPreview();
				camera.release();
				camera=null;
			}

			arrayListForWidth.clear();
			arrayListForHeight.clear();

			if (packageManager
					.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
				result1 = true;
			}else{
				result1 = false;
			}

			if(result1)
			{

				camera=Camera.open(1);    //  For Front Camera
				android.hardware.Camera.Parameters params1 = camera.getParameters();
				List sizes1 = params1.getSupportedPictureSizes();
				Camera.Size  result11 = null;
				for (int i=0;i<sizes1.size();i++){
					result11 = (Size) sizes1.get(i);
					arrayListForWidth.add(result11.width);
					arrayListForHeight.add(result11.height);	
				} 

				if(arrayListForWidth.size() != 0 && arrayListForHeight.size() != 0){

					frontCamera = ""+(((Collections.max(arrayListForWidth)) * (Collections.max(arrayListForHeight))) / 1024000 )+"Mpx";
				}
			}
			else{
				;
			}
			//camera.release();

			if (camera!=null){
				camera.stopPreview();
				camera.release();
				camera=null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mgr = (SensorManager) ctx.getSystemService(SENSOR_SERVICE);
		List<Sensor> sensors = mgr.getSensorList(Sensor.TYPE_ALL);

		mSensorInfoList.clear();

		for(Sensor sensor : sensors) {

			SensorInfo mSensorInfo = new SensorInfo();

			mSensorInfo.sensorName = sensor.getName();
			mSensorInfo.sensorValue = sensor.getVendor();

			mSensorInfoList.add(mSensorInfo);
		}

		String getInfo = getInfo();

		int a = getInfo.indexOf("processor");  
		int b = getInfo.indexOf("BogoMIPS");
		String sss=  getInfoFreq();

		if(b==-1)
		{
			int c = getInfo.indexOf("model name\t: ");  
			int d = getInfo.indexOf("\n",c+13);
			processorname = ""+getInfo.substring(c+12, d);

			String[] ss = getInfo.split("processor\t:");
			if(ss.length>1)				
				osProcessor2 = ""+(ss.length-1);
			else
				osProcessor2 = ""+(ss.length);

			double max = 0;
			for(int i=0;i<ss.length;i++)
			{
				int freA = ss[i].indexOf("cpu MHz");

				if(freA==-1)
					continue;
				maxFreq ="0";
				freA = ss[i].indexOf(":",freA);
				int freB = ss[i].indexOf("\n",freA);
				maxFreq = ""+ss[i].substring(freA+2, freB);
				if(Double.parseDouble(maxFreq)>max)
					max = Double.parseDouble(maxFreq);


			}

			maxFreq =""+max;
		}

		else
		{
			int c = getInfo.indexOf("Processor\t: ");  
			int d = getInfo.indexOf("\n",c+13);
			processorname = ""+getInfo.substring(c+12, d);
			String[] ss = getInfo.split("processor\t:");

			if(ss.length>1)				
				osProcessor2 = ""+(ss.length-1);
			else
				osProcessor2 = ""+(ss.length);
			//	processorname = ""+getInfo.substring(a+22, b);
			String f= getInfoFreq();
			if(!TextUtils.isEmpty(f))
				maxFreq = ""+Double.parseDouble(f)/1000;
			else
				maxFreq= "-";
		}

		DeBug.ShowLog("processorname", "processorname "+processorname);
	}  

	private static String getInfo() {

		StringBuffer sb = new StringBuffer();

		sb.append("Processor: ").append("\n");

		if (new File("/proc/cpuinfo").exists()) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));

				String aLine;

				while ((aLine = br.readLine()) != null) {

					sb.append(aLine + "\n");

				}

				if (br != null) {

					br.close();
				}

			} catch (IOException e) {

				e.printStackTrace();
			} 
		}

		return sb.toString();

	}
	private static String getInfoFreq() {

		StringBuffer sb = new StringBuffer();

		sb.append("");

		if (new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq").exists()) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(new File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")));

				String aLine;

				while ((aLine = br.readLine()) != null) {

					sb.append(aLine);

				}

				if (br != null) {

					br.close();
				}

			} catch (IOException e) {

				e.printStackTrace();
			} 
		}

		return sb.toString();

	}
	private void batteryInformation()
	{
		Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int level   = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale   = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		int percent = (level*100)/scale;

		technology = batteryIntent.getExtras().getString(BatteryManager.EXTRA_TECHNOLOGY);
		int health = batteryIntent.getIntExtra("health", 0);
		int status = batteryIntent.getIntExtra("status", 0);
		voltage    = batteryIntent.getIntExtra("voltage", 0);
		temperature= batteryIntent.getIntExtra("temperature", 0);

		batteryHealth = getHealthString(health);
		batterystatus = getStatusString(status); 
		value = String.valueOf(percent) + "%";

		/*DeBug.ShowLog.i(TAG," voltage : "+ voltage + " temperature : "+ temperature +" battery percent : "+ value +" batterystatus : "+ batterystatus +
					" batteryHealth : "+ batteryHealth + " technology : "+ technology);*/
	}

	private String getHealthString(int health) {
		String healthString = "Unknown";
		switch (health) {
		case BatteryManager.BATTERY_HEALTH_DEAD:
			healthString = "Dead";
			break;
		case BatteryManager.BATTERY_HEALTH_GOOD:
			healthString = "Good";
			break;
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			healthString = "Over Voltage";
			break;
		case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			healthString = "Over Heat";
			break;
		case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			healthString = "Failure";
			break;
		}
		return healthString;
	}

	private String getStatusString(int status) {
		String statusString = "Unknown";
		switch (status) {
		case BatteryManager.BATTERY_STATUS_CHARGING:
			statusString = "Charging";
			break;
		case BatteryManager.BATTERY_STATUS_DISCHARGING:
			statusString = "Discharging";
			break;
		case BatteryManager.BATTERY_STATUS_FULL:
			statusString = "Full";
			break;
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			statusString = "Not Charging";
			break;
		}
		return statusString;
	}

	public void memeoryInformation()
	{
		MemoryInfo mi = new MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		try {
			availableMegs = mi.availMem / 1048576L;
			availableMegs1 = mi.availMem / 1048576L;// mi.totalMem check  device API > 15 have it
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DeBug.ShowLog(TAG, "Sysytem Ram " + availableMegs + "Total Memory " + availableMegs1);	 

		Double allocated = new Double(Debug.getNativeHeapAllocatedSize())/new Double((1048576));
		// Double available = new Double(Debug.getNativeHeapSize())/1048576.0;
		// Double free = new Double(Debug.getNativeHeapFreeSize())/1048576.0;
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		jvmMaxMemory = df.format(new Double(Runtime.getRuntime().maxMemory()/1048576));
		// DeBug.ShowLog(TAG, "debug.heap native: allocated " +jvmMaxMemory);
	}

	public static boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	public static String getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return formatSize(totalBlocks * blockSize);
	}

	public static String getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return formatSize(totalBlocks * blockSize);
		} else {
			return null;
		}
	}

	public static String formatSize(long size) {
		String suffix = null;

		if (size >= 1024) {
			suffix = "KB";
			size /= 1024;
			if (size >= 1024) {
				suffix = "MB";
				size /= 1024;
			}
		}

		StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
		int commaOffset = resultBuffer.length() - 3;
		while (commaOffset > 0) {
			resultBuffer.insert(commaOffset, ',');
			commaOffset -= 3;
		}

		if (suffix != null) resultBuffer.append(suffix);
		return resultBuffer.toString();
	}

	public static void getNetworkInfo()
	{
		NetworkInfo info = Connectivity.getNetworkInfo(ctx);

		if(info!=null && !info.equals(""))
		{
			ConnectedWifi= Connectivity.isConnectedWifi(ctx);
			ConnectedMobile= Connectivity.isConnectedMobile(ctx);
			Roaming = info.isRoaming();
			IsConnectedToProvisioningNetwork = info.isConnectedOrConnecting();
			State= Connectivity.connectivityInfo(ctx).getDetailedState();
			status = NetworkUtil.getConnectivityStatusString(ctx);
			NetworkTypeInfo =info.getTypeName();
			connectivityName= info.getExtraInfo();
			connectivityType= Connectivity.connectivityInfo(ctx).getTypeName();

			if(TextUtils.isEmpty(connectivityName))
			{
				connectivityName="";
			}
			else{
				connectivityName = connectivityName.replaceAll("[^a-zA-Z]","");
			}

			if(ConnectedWifi==true)
			{
				connectedWifi = "Yes";
			}
			else{
				connectedWifi = "No";
			}

			if(ConnectedMobile == true)
			{
				connectednetwork = "Yes";
			}
			else{
				connectednetwork = "No";
			}

			if(Roaming==true)
			{
				roaming = "Yes";
			}
			else{
				roaming = "No";
			}

			if(IsConnectedToProvisioningNetwork==true)
			{
				isconnectedtoprovisioningnetwork = "Yes";
			}
			else{
				isconnectedtoprovisioningnetwork = "No";
			}

			if(TextUtils.isEmpty(connectivityType))
			{
				connectivityType="";
			}
			else{
				connectivityType= Connectivity.connectivityInfo(ctx).getTypeName();
			}

		}

		else{
			;
		}

	}

	public static void getSimInfo()
	{
		DualSimManager mDualSimManager=new DualSimManager(ctx);
		TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(ctx);

		mtelephonyManager = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);

		if(mtelephonyManager!=null)
		{
			stSIMSerialno = mtelephonyManager.getSimSerialNumber();
			OperatorName1 = mDualSimManager.getNETWORK_OPERATOR_NAME(0);
			OperatorName2 = mDualSimManager.getNETWORK_OPERATOR_NAME(1);
			imsiSIM1 = telephonyInfo.getImsiSIM1();
			imsiSIM2 = telephonyInfo.getImsiSIM2();
			isSIM1Ready = telephonyInfo.isSIM1Ready();
			isSIM2Ready = telephonyInfo.isSIM2Ready();
			isDualSIM = telephonyInfo.isDualSIM();

			if(isDualSIM)
			{
				dualSim = "Yes";
			}
			else{
				dualSim = "No";
			}
			if(isSIM1Ready)
			{
				sim1 = "Yes";
			}
			else{
				sim1="No";
			}
			if(isSIM2Ready)
			{
				sim2 = "Yes";
			}
			else{
				sim2 = "No";
			}
		}

		else{
			stSIMSerialno = "";
			OperatorName1 = "";
			OperatorName2 = "";
			imsiSIM1 = "";
			imsiSIM2 = "";
			dualSim = "No";
			sim1="No";
			sim2 = "No";
		}

		/*String NetworkStrength = mDualSimManager.getSIM_NETWORK_SIGNAL_STRENGTH(0);
		int SimLocation = mDualSimManager.getSIM_LOCID(0);
		int SimCellId = mDualSimManager.getSIM_CELLID(0);
		int[] OperaterCode = mDualSimManager.getNETWORK_OPERATOR_CODE(0);*/
	}
	
}
class SensorInfo
{
	String sensorName ="";
	String sensorValue="";
}