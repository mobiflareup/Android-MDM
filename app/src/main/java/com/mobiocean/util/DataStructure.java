package com.mobiocean.util;

import android.content.Context;

import com.mobiocean.service.OneMinuteTimerService;
import com.mobiocean.ui.PackageList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


// Phone Modes for Gingerbox
interface ePhoneMode 
{
	public static final byte PARENT_RESTRICTED = 0x20;
}

interface ePhoneFeatureCtrl 
{
	public static final byte CALL_NO_SMS = 0x8;
}		

public class DataStructure
{
	private int MAXAllowedPhoneNumber = 16;
	private byte MAXAllowedEXTPhoneNumber = 12;
	private byte MAXAllowedFeatures = 7;
	// Custom Inbox Varibles for Aborted Messages
	public   int id = 0;
	public   int no_tableid =0;
	public   List<String> sms_numberlist;
	public   List<Integer> sms_numberFlaglist;
	// To check the SMS Hash Code on OnReceive()(The hash code of every SMS is different) 
	public long msgrcvrhashcode =0;

	public static HashMap<String, boolean[][]> runInTime = new HashMap<String, boolean[][]>();
	public static List<String> totalApplication = new ArrayList<String>();

	public DataStructure()
	{
		sms_numberlist=new ArrayList<String>();
		sms_numberFlaglist=new ArrayList<Integer>();
		structPC  = new cPhoneConfiguration();
		structMCC = new cModeControlConfig();
		structDNCC= new cDefunctNumberControlConfig();
		structNCC = new cNumberControlConfig();
		structFCC = new cFeatureControlConfig();
		structLGC = new cLocationGPSConfig();
		structLGS = new cLocationGPSStorage();
		structGCM = new cGCM();
		structCCC = new cCompnayControlConfig();
		structSosControlMode = new SosControlMode();

		bSchoolCalendar = new byte[365];
		lSchoolSchedule = new cSchoolSchedule();
		structACC = new cApplicationControlConfig();
		structWCC = new cApplicationControlConfig();
		/*int k=0;
		for(;k<lSchoolSchedule.length;k++)
		{

			lSchoolSchedule[k].iStartDate = 1;
			lSchoolSchedule[k].iStartMonth= 1;
			lSchoolSchedule[k].iStartYear = 1;
			lSchoolSchedule[k].StartDate.set(0,0,0);
		}*/

		//sms_numberFlaglist=new ArrayList<Integer>();
	}


	public class cPhoneConfiguration //implements Serializable
	{
		public byte bFactoryControlByte;
		public byte  bLength;
		public byte  bType;
		public short wManufacturer;
		public short wProduct;
		public short wTotalLength;
		public byte bMode;
		public byte bTotalNoFeatures;
		public boolean bRunApp;
		public char[] stSign;
		public String stPassword;
		public String stEDPassword;
		public String[] stParentPassword;
		public int AppHrMin;
		public int iExpiryDate;
		public int iExpiryMonth;
		public int iExpiryYear;
		public int iCurrentDate;
		public int iCurrentMonth;
		public int iCurrentYear;
		public Calendar ExpiryDate;
		public Calendar CurrentDate;
		public boolean bTimeExpired = false;
		public boolean bDateChanged;
		public int bTotalApps=0;
		public boolean bMicroPhone = false;
		public boolean bSpeaker    = false;
		public String stSIMSerialno;
		public boolean bExpiryDateUpdated;
		public boolean bPhoneNoUploded;
		public String stPhoneNo;
		public int ParentRegistered;
		public int TotalApps;
		public boolean InstallCompeted;
		public boolean requestProcessing;
		public byte bWeekDay;
		public int bNo_of_App_Uploaded;
		public String Country;
		public boolean bSimChanged;
		public boolean bDateChangedToDefault;
		public boolean bDeviceAdminEnabled;
		public String iStudId;
		public int ConveyanceId= 0;
		public byte bMaxPassWordAllowed;
		public String stMobile_SwitchOFF_dateTime;

		public String stUpdatedApkFileName;
		public String stOldApkFileName;
		public boolean bNewAppReadyToDownload;
		public boolean bDownloadedReadyToInstall;
		/**
		 * bFactoryControlByte:0  Parent can send msg. to child for setting  0:No 1:Yes
		 * bFactoryControlByte:1  Take Location at the time of Call/SMS Logs 0:No 1:Yes
		 * bFactoryControlByte:2
		 * bFactoryControlByte:3  every 30min GPS loc. request               0:No 1:Yes
		 */

		//constructor
		public cPhoneConfiguration()
		{
			Calendar calendarObj = Calendar.getInstance();
			bFactoryControlByte=0x08;
			bLength = 22;
			bType = 0x01;
			wManufacturer=0x0001;
			wProduct=0x0001;
			wTotalLength=0x100;
			bMode=ePhoneMode.PARENT_RESTRICTED;
			bTotalNoFeatures=MAXAllowedFeatures;
			bRunApp = true;
			stSign = new char[] { '*', '#', '!','#','*'};
			stPassword  = "GBox set as";
			stEDPassword="1234567890abcdef";
			bMaxPassWordAllowed = 100;
			stParentPassword = new String[bMaxPassWordAllowed];
			stParentPassword[0]="123456";
			CurrentDate=Calendar.getInstance();
			iCurrentDate  = calendarObj.get(Calendar.DATE);
			iCurrentMonth = calendarObj.get(Calendar.MONTH);
			iCurrentYear  = calendarObj.get(Calendar.YEAR);
			CurrentDate.set(iCurrentYear,iCurrentMonth,iCurrentDate);   //30,4,2013 This is 30th of May and not 30th of April
			calendarObj.add(Calendar.MONTH, 5);
			ExpiryDate   = Calendar.getInstance(); 
			iExpiryDate  = calendarObj.get(Calendar.DATE);
			iExpiryMonth = calendarObj.get(Calendar.MONTH);
			iExpiryYear  = calendarObj.get(Calendar.YEAR);
			ExpiryDate.set(iExpiryYear,iExpiryMonth,iExpiryDate);   //30,4,2013 This is 30th of May and not 30th of April
			bDateChanged=true;
			stSIMSerialno=null;
			bExpiryDateUpdated=false;
			bPhoneNoUploded=false;
			stPhoneNo = null;
			ParentRegistered=0;
			TotalApps=0;
			InstallCompeted=false;
			requestProcessing=false;
			bWeekDay = 0;
			bNo_of_App_Uploaded=0;
			Country=null;
			bSimChanged=false;
			bDateChangedToDefault=false;
			bDeviceAdminEnabled=false;
			iStudId="";
			stMobile_SwitchOFF_dateTime = "";
			stUpdatedApkFileName = "";
			stOldApkFileName = "";
			bNewAppReadyToDownload = false;
			bDownloadedReadyToInstall = false;
			bWeekDay = 0;
			bNo_of_App_Uploaded=0;
		}

	}

	public String getStudentId()
	{
		return structPC.iStudId;
	}

	public Calendar getExpDate()
	{
		return structPC.ExpiryDate;
	}
	public void setExpDate(int iCurrentYear,int iCurrentMonth,int iCurrentDate)
	{
		structPC.ExpiryDate.set(iCurrentYear, iCurrentMonth,iCurrentDate);
	}
	public boolean bTimeExpired()
	{
		return structPC.bTimeExpired;
	}
	public class cModeControlConfig
	{
		public byte bLength;
		public byte bType;
		public short wDay;
		public long dwReserved;
		//Schange can be changed to array	
		public short[] wStartingTime;
		public short[] wEndingTime;
		//
		cModeControlConfig()
		{
			bLength =0;
			bType = 0x02;
			wDay = 0;
			//Schange can be changed to array	
			wStartingTime = new short[8];
			wEndingTime = new short[8];
		}
	}

	public class SosControlMode
	{

		//Schange can be changed to array	
		public boolean isInSosMode;
		public long lSosStartTimeInMilies;
		protected byte bSosdurationInMins;
		protected byte bLocationFrequency;
		SosControlMode()
		{

			isInSosMode = false;
			lSosStartTimeInMilies = 0;
			bSosdurationInMins    = 120;
			bLocationFrequency    = 10;

		}
	}

	public class cDefunctNumberControlConfig
	{
		public byte bLength;
		public byte bType;
		public byte bTotalNumbers;
		public byte bReserved;
		public String[] stDefunctNumber;
		public String stPullMsgNo;

		cDefunctNumberControlConfig()
		{
			bLength=0x14;
			bType=0x04;
			bTotalNumbers=4;
			bReserved=0;
			stDefunctNumber = new String[4];
			stPullMsgNo=null;//+918015993955//+918376013432
			stDefunctNumber[0]= "GINGER";  //Factory //DM-GINGER//"+918015993955";//"+918124068679";
			stDefunctNumber[1]= "0000000000";  //Father  //+919342241900";  
			stDefunctNumber[2]= "0000000000";  //Mother   "+918124068679";
			stDefunctNumber[3]= "0000000000";            //Additional
		}
	}

	public class cNumberControlConfig
	{
		public byte bLength;
		public byte bType;
		public byte bTotalNumbers;
		public byte bPhoneFeatureCtrl;
		public byte bReserved;
		public short[][] wTotalDuration;			
		public short[][] wStartTime;
		public short[][] wEndTime;				
		public short[][] wUsedDuration;
		public boolean[][] bPresentlyStopped;		
		public boolean[][] bAlarmManager;
		public byte[][] bTimeDurationCtrl;
		public long[][][] lAllowedTime;
		public byte bIncomingCallOn=0;
		public byte bOutgoingCallOn=0;
		public boolean bMuted = false;
		public String stEXT_ALWD_Numbers[];
		public String stEXT_Called_Duration[];
		public String stEXT_Sent_SMS[];

		cNumberControlConfig()
		{
			wTotalDuration = new short[7][3];
			wStartTime     = new short[7][3];
			wEndTime       = new short[7][3];	
			wUsedDuration  = new short[7][3];
			bPresentlyStopped = new boolean[7][3];
			bAlarmManager = new boolean[7][2];
			bTimeDurationCtrl= new byte[7][3];
			bLength=0x3C;
			bType=0x05;
			bTotalNumbers     = MAXAllowedEXTPhoneNumber;
			bPhoneFeatureCtrl = 0;  //ePhoneFeatureCtrl.CALL_NO_OUTCOMING;
			bReserved=0;
			lAllowedTime = new long[7][64][24];
			//0 For InComing
			//1 For OutGoing
			//2 For SMS
			for(int day=0;day<7;day++)
				for(int feature=0;feature<3;feature++)
				{
					for(int hr=0;hr<24;hr++)
						lAllowedTime[day][feature][hr]=0x0FFFFFFFFFFFFFFFL;

				}
			for(int day=0;day<7;day++)
			{
				for(int i=0;i<2;i++)	
				{
					bTimeDurationCtrl[day][i] = 0  ;
					wTotalDuration[day][i] = 1440;				
					wStartTime[day][i]     = 0000;
					wEndTime[day][i]       = 2400;			 		 
					wUsedDuration[day][i]  = 0;
					bPresentlyStopped[day][i] = false;			
					bAlarmManager[day][i]     = false;			 
				}
				bTimeDurationCtrl[day][2] = 0;//Restricted in clock time and no of SMS;
				wTotalDuration[day][2] = 1000;	   //allowed no. of SMS	Allowed		
				wStartTime[day][2]     = 0000;
				wEndTime[day][2]       = 2400;			 		 
				wUsedDuration[day][2]  = 0;    //Total Used SMS
				bPresentlyStopped[day][2] = false;			
			}
			bMuted         = false;
			stEXT_ALWD_Numbers=new String[bTotalNumbers];
			for (int i=0; i<bTotalNumbers; i++)
				stEXT_ALWD_Numbers[i] = null;
		}
	}

	public class cFeatureControlConfig
	{
		public byte bLength;
		public byte bType;
		public byte bTotalFeatures;
		public byte bReserved1;
		public byte bFeatureType[];
		
		/**
		 * What control is applied on feature (blocked in time etc.) 
		 * */
		public byte bTimeDurationCtrl[][];
		public short wStartTime[][];
		public short wEndTime[][];
		public short wTotalDuration[][];
	
		public short wUsedDuration[][];
		/**
		 * tracks the feature is permanently blocked for the day
		 * */
		public boolean bPresentlyStopped[][];
		/**
		 * long value corresponding to feature is blocked/allowed for hour (1 bit correspons to 1 min). 
		 * */
		public long[][][] lAllowedTime;

		public int IsWhiteList = 0;
		public int IsSmsBlack = 0;
		public int IsWebBlack = 0;

		public boolean IsSosAutoAnswer = false;
		public boolean IsBlockGps = false;
		public boolean IsBlockNotification = false;
		public boolean IsBlockUsb = false;
		public boolean IsBlockMobileData = false;
		public boolean IsBlockQuickSettings = false;

		public byte bPresentlyOn[];

		public  ArrayList<String> stBlockedWebsitesName;

		public  List<Boolean> lbApps_Uploaded;
		public  List<String>  lstAppsTobeUploaded;
    	
		public int bTotalApps_To_Be_Uploaded;

		public cFeatureControlConfig()
		{
			bLength=0x10;
			bType=0x06;
			bTotalFeatures=80; //MAXAllowedFeatures
			bFeatureType      = new byte[bTotalFeatures];
			bTimeDurationCtrl = new byte [7][bTotalFeatures];
			wStartTime        = new short[7][bTotalFeatures];
			wEndTime          = new short[7][bTotalFeatures];
			wTotalDuration    = new short[7][bTotalFeatures];
			wUsedDuration     = new short[7][bTotalFeatures];
			bPresentlyStopped = new boolean[7][bTotalFeatures];
			bPresentlyOn      = new byte[bTotalFeatures];  // 0=> off, 1=> on 2=> just stopped 3=> Stopped value changed
			stBlockedWebsitesName = new ArrayList<String>();

			lbApps_Uploaded =  new ArrayList<Boolean>();
			lstAppsTobeUploaded= new ArrayList<String>();;

			lAllowedTime = new long[7][bTotalFeatures][24];
			bTotalApps_To_Be_Uploaded=0;

			for(int day=0;day<7;day++)
				for(int feature=0;feature<bTotalFeatures;feature++)
				{
					for(int hr=0;hr<24;hr++)
						lAllowedTime[day][feature][hr]=0x0FFFFFFFFFFFFFFFL;					
				}

			for(int i=0;i<bTotalFeatures;i++)
			{
				bFeatureType[i]     = 0;
				bPresentlyOn[i]     = 0;

				for(int day=0;day<7;day++)
				{	
					bTimeDurationCtrl[day][i]= 0;		
					wStartTime[day][i]       = 0000;
					wEndTime[day][i]         = 2400;
					wTotalDuration[day][i]   = 1440;
					wUsedDuration[day][i]    =   0;
					bPresentlyStopped[day][i]= false;
				}	
			}
			//packageList=new PackageList();
		}
	}

	public class cApplicationControlConfig
	{
		public byte bLength;
		public byte bType;
		public byte bTotalFeatures;
		public byte bReserved1;
		public byte bFeatureType[];		
		public short wStartTime[][];
		public short wEndTime[][];
		
		public byte bTimeDurationCtrl[][];  ///
		public short wTotalDuration[][];   ///
		public short wUsedDuration[][];  ///
		public boolean bPresentlyStopped[][]; ///
		public byte bPresentlyOn[];          ///
		public long[][][] lAllowedTime;   ///
		
		public PackageList packageList;
		public  ArrayList<String> stBlockedWebsitesName;
		public  List<String> ALL_APPS;
		public  List<String> ALL_APPS_Name;
		public  List<Boolean> lbApps_Uploaded;
		public  List<String>  lstAppsTobeUploaded;		
		public int bTotalApps_To_Be_Uploaded;
		public  ArrayList<String> listGroupIds;

		public cApplicationControlConfig()
		{
			bLength=0x10;
			bType=0x06;
			bTotalFeatures=80; //MAXAllowedFeatures
			bFeatureType      = new byte[bTotalFeatures];
			bTimeDurationCtrl = new byte [7][bTotalFeatures];
			wStartTime        = new short[7][bTotalFeatures];
			wEndTime          = new short[7][bTotalFeatures];
			wTotalDuration    = new short[7][bTotalFeatures];
			wUsedDuration     = new short[7][bTotalFeatures];
			bPresentlyStopped = new boolean[7][bTotalFeatures];
			bPresentlyOn      = new byte[bTotalFeatures];  // 0=> off, 1=> on 2=> just stopped 3=> Stopped value changed
			ALL_APPS = new ArrayList<String>();
			ALL_APPS_Name = new ArrayList<String>();
			listGroupIds  = new ArrayList<String>();
			lAllowedTime  = new long[7][80][24];

			for(int day=0;day<7;day++)
				for(int feature=0;feature<bTotalFeatures;feature++)
				{
					for(int hr=0;hr<24;hr++)
						lAllowedTime[day][feature][hr]=0x0FFFFFFFFFFFFFFFL;					
				}

			for(int i=0;i<bTotalFeatures;i++)
			{
				bFeatureType[i]     = 0;
				bPresentlyOn[i]     = 0;

				for(int day=0;day<7;day++)
				{	
					bTimeDurationCtrl[day][i]= 0;		
					wStartTime[day][i]       = 0000;
					wEndTime[day][i]         = 2400;
					wTotalDuration[day][i]   = 1440;
					wUsedDuration[day][i]    =   0;
					bPresentlyStopped[day][i]= false;
				}	
			}
			packageList=new PackageList();
		}
	}

	public void inItApplicationStruct(Context context, int size,ArrayList<String> listGroupIds)
	{
		structACC.bTotalFeatures=(byte) 80; //MAXAllowedFeatures
		structACC.bFeatureType      = new byte[structACC.bTotalFeatures];
		structACC.bTimeDurationCtrl = new byte [7][structACC.bTotalFeatures];
		structACC.wStartTime        = new short[7][structACC.bTotalFeatures];
		structACC.wEndTime          = new short[7][structACC.bTotalFeatures];
		structACC.wTotalDuration    = new short[7][structACC.bTotalFeatures];
		structACC.wUsedDuration     = new short[7][structACC.bTotalFeatures];
		structACC.bPresentlyStopped = new boolean[7][structACC.bTotalFeatures];
		structACC.bPresentlyOn      = new byte[structACC.bTotalFeatures];  // 0=> off, 1=> on 2=> just stopped 3=> Stopped value changed
		structACC.ALL_APPS = new ArrayList<String>();
		structACC.ALL_APPS_Name = new ArrayList<String>();
		structACC.listGroupIds  = listGroupIds;
		structACC.lAllowedTime  = new long[7][structACC.bTotalFeatures][24];

		for(int day=0;day<7;day++)
			for(int feature=0;feature<structACC.bTotalFeatures;feature++)
			{
				for(int hr=0;hr<24;hr++)
					structACC.lAllowedTime[day][feature][hr]=0x0FFFFFFFFFFFFFFFL;					
			}

		for(int i=0;i<structACC.bTotalFeatures;i++)
		{
			structACC.bFeatureType[i]     = 0;
			structACC.bPresentlyOn[i]     = 0;

			for(int day=0;day<7;day++)
			{	
				structACC.bTimeDurationCtrl[day][i]= 0;		
				structACC.wStartTime[day][i]       = 0000;
				structACC.wEndTime[day][i]         = 2400;
				structACC.wTotalDuration[day][i]   = 1440;
				structACC.wUsedDuration[day][i]    =   0;
				structACC.bPresentlyStopped[day][i]= false;
			}	
		}
		structACC.packageList=new PackageList(context,size);

		//NARAYANAN
		structWCC.listGroupIds  = listGroupIds;
		structWCC.lAllowedTime  = new long[7][80][24];

		for(int day=0;day<7;day++)
			for(int feature=0;feature<80;feature++)
			{
				for(int hr=0;hr<24;hr++)
					structWCC.lAllowedTime[day][feature][hr]=0x0FFFFFFFFFFFFFFFL;
			}
		structWCC.packageList=new PackageList(context,size);
		//NARAYANAN
	}
	public String getFathersNumber()
	{
		return "+91"+structDNCC.stDefunctNumber[1];
	}

	public String getMothersNumber()
	{
		return "+91"+structDNCC.stDefunctNumber[2];
	}



	public class cLocationGPSConfig
	{
		public byte bfeatureValue;
		public byte bNoOfRequestGPSLocn;
		public int  wRequestGPSLocn;
		public byte bTimesCount;
		public double dLatitude;
		public double dLongitude;
		public boolean bNetwork;

		public int bTotalLocationSchedule;
		public short[] wStartTime;
		public short[] wEndTime;
		public short[] wLocFreq;

		public short[] wStartGeoFenceTime;
		public short[] wEndGeoFenceTime;
		public double[] dGeoFenceLatitude;
		public double[] dGeoFenceLongitude;
		public double[] dGeoFenceRadius;

		public double dCurrentGeoFenceLatitude;
		public double dCurrentGeoFenceLongitude;
		public double dCurrentGeoFenceRadius;	
		public byte bGPSRequestTimer;

		public int bGeofenceActive;
		public byte bUserGeogenceStatus;

		public cLocationGPSConfig()
		{
			bfeatureValue = 0;
			bNoOfRequestGPSLocn = 0;
			wRequestGPSLocn = 0;
			bTimesCount = 1;
			dLatitude = 0;
			dLongitude = 0;
			bNetwork = false;
			bGPSRequestTimer = 10;
			bTotalLocationSchedule=15;
			wStartTime= new short[bTotalLocationSchedule];
			wEndTime  = new short[bTotalLocationSchedule];
			wLocFreq  = new short[bTotalLocationSchedule];
			wStartGeoFenceTime = new short[bTotalLocationSchedule];
			wEndGeoFenceTime   = new short[bTotalLocationSchedule];
			dGeoFenceLatitude  = new double[bTotalLocationSchedule];
			dGeoFenceLongitude = new double[bTotalLocationSchedule];
			dGeoFenceRadius    = new double[bTotalLocationSchedule];
			dCurrentGeoFenceLatitude = 0;
			dCurrentGeoFenceLongitude= 0;
			dCurrentGeoFenceRadius   = 0;
			bGeofenceActive= -1;
			bUserGeogenceStatus= -1;
		}
	}
	class cLocationGPSStorage
	{
		public byte bLength;
		public byte bType; //Storage of No to give the GPS Physical Locations
		public byte bTotalNumbers;
		public byte breserved;
		public String[] stChildNo;
		public String[] FatherNo;
		public String[] MotherNo;

		public cLocationGPSStorage()
		{
			stChildNo = new String[30];
			FatherNo  = new String[30];
			MotherNo  = new String[30];
			stChildNo[0] = "+918124068679";
			FatherNo[0]  = "+918124066480";
			MotherNo[0]  = "+918124066480";
			bTotalNumbers = 1;
		}
	}
	public class cGCM
	{
		public String regkeyGCM;
		public boolean bGCMPhoneRegisteredOnServer;
		public int GCMCounter;
		public boolean bCheckGCMACK;
		public  String stMessage;
		public  String stMessageId;
		public boolean bUpdateRegIdSMS;
		public int GCMmessageCounter;
		public boolean bAlreadyRunning;
		public cGCM()
		{
			regkeyGCM=null;
			bGCMPhoneRegisteredOnServer=false;
			GCMCounter=0;
			bCheckGCMACK=false;	
			stMessage = null;
			stMessageId = null;
			bUpdateRegIdSMS=false;
			bAlreadyRunning=false;
		}
	}
	public class  cSchoolSchedule
	{	
		public byte bLength;
		public byte bType;
		public byte bTotalSchoolSchedule;
		public byte bReserved;
		public String stServerIp;
		public String stSchoolCode;

		public byte bSentRequestToServer;

		public short wCurrentDayTriggerTime;

		public short[] wStartTime;
		public short[] wEndTime;

		public int iCurrentDate;
		public int iCurrentMonth;
		public int iCurrentYear;

		public int[] iStartDate;
		public int[] iStartMonth;
		public int[] iStartYear;


		public int[] iEndDate;
		public int[] iEndMonth;
		public int[] iEndYear;

		public Calendar[] StartDate;
		public Calendar[] EndDate;
		public Calendar CurrentDate;

		public long[][] lAllowedTime;
		public int iCurrentSchoolSchedule;
		public boolean StartWIFI;
		public int wCurrentDayTriggerEndTime;
		public boolean bSchoolHoliday;

		public String stBSSID= "";
		public String stSSID = "";
		public String stPass = "";

		cSchoolSchedule()
		{
			bTotalSchoolSchedule=5;
			iCurrentSchoolSchedule=-1;
			bSchoolHoliday=false;
			stSchoolCode = "Gingerbox";
			bSentRequestToServer=0;

			stServerIp="";

			CurrentDate= Calendar.getInstance();	
			CurrentDate.setTimeInMillis(System.currentTimeMillis());
			iCurrentDate=CurrentDate.get(Calendar.DAY_OF_MONTH);
			iCurrentMonth=CurrentDate.get(Calendar.MONTH);
			iCurrentYear=CurrentDate.get(Calendar.YEAR);
			int minute = CurrentDate.get(Calendar.MINUTE);
			int hour = CurrentDate.get(Calendar.HOUR_OF_DAY);
			OneMinuteTimerService.tempAppHrMin = hour*100+minute;

			wStartTime= new short[bTotalSchoolSchedule];
			wEndTime  = new short[bTotalSchoolSchedule];

			iStartDate=new int[bTotalSchoolSchedule];
			iStartMonth=new int[bTotalSchoolSchedule];
			iStartYear=new int[bTotalSchoolSchedule];

			iEndDate=new int[bTotalSchoolSchedule];
			iEndMonth=new int[bTotalSchoolSchedule];
			iEndYear=new int[bTotalSchoolSchedule];

			StartDate= new Calendar[bTotalSchoolSchedule];// Calendar.getInstance();
			EndDate=new Calendar[bTotalSchoolSchedule];//Calendar.getInstance();
			for(int i=0;i<bTotalSchoolSchedule;i++)
			{
				iStartDate[i] = 0;
				iStartMonth[i]= 0;
				iStartYear[i] = 0;

				iEndDate[i] = 0;
				iEndMonth[i]= 0;
				iEndYear[i] = 0;
				StartDate[i]=Calendar.getInstance();
				EndDate[i]=Calendar.getInstance();
				StartDate[i].set(iStartYear[i],iStartMonth[i],iStartDate[i]);
				EndDate[i].set(iEndYear[i], iStartMonth[i], iEndDate[i]);
			}
			iStartDate[1] = StartDate[1].get(Calendar.DATE);
			iStartMonth[1]= StartDate[1].get(Calendar.MONTH);
			iStartYear[1] = StartDate[1].get(Calendar.YEAR);

			EndDate[1].add(Calendar.MONTH, 5);

			lAllowedTime=new long[bTotalSchoolSchedule][24];

			StartWIFI=false;
			wCurrentDayTriggerEndTime=0;
			wCurrentDayTriggerTime=0;
		}
	} 
	public int getwCurrentDayTriggerEndTime()
	{		
		return lSchoolSchedule.wCurrentDayTriggerEndTime; 
	}
	public int getwCurrentAppHrMin()
	{		
		return structPC.AppHrMin; 
	}
	public String getServerIP()
	{		
		return lSchoolSchedule.stServerIp; 
	}

	public String getBSSID()
	{		
		return lSchoolSchedule.stBSSID; 
	}
	public String getSSID()
	{		
		return lSchoolSchedule.stSSID; 
	}
	public String getPass()
	{		
		return lSchoolSchedule.stPass; 
	}

	public class cCompnayControlConfig
	{

		public byte bLength;
		public byte bType;
		public byte bTotalNumbers;
		public byte bReserved;
		public String stCompID;
		public String stEmpID;
		public long lLastSentSMSTime;
		public  List<String> stImageDate_Type;
		public long wFeatureControlWord[];
		public long tempwFeatureControlWord;
		public byte bTotalFeatures;
		public long lExpDatesOfFeatures[][];
		public String [] stGoogleAPIKey;
		public String stAppVer;
		public String stProfileId;
		public int isProfileEnabled;

		
		cCompnayControlConfig()
		{
			/*** 0 - regular key , 1 - attendance key ***/ 
			stGoogleAPIKey   = new String [2] ;
			bTotalFeatures   = 10;
			wFeatureControlWord   = new long[1];
			wFeatureControlWord[0]= 0x7FFFFFFF;
			tempwFeatureControlWord = 0x7FFFFFFF;
			lExpDatesOfFeatures   = new long[1][bTotalFeatures];
			bLength=0x14;
			bType=0x04;
			bTotalNumbers=4;
			bReserved=0;
			stCompID="";
			stEmpID="";
			lLastSentSMSTime=0;
			stImageDate_Type = new ArrayList<String>();
			stAppVer = "";
			stProfileId="0";
			isProfileEnabled=0;
		}
	}
	//++ Declaring Structures
	public  cPhoneConfiguration structPC;
	public  cModeControlConfig structMCC;
	public  cDefunctNumberControlConfig structDNCC;
	public  cNumberControlConfig structNCC;
	public  cFeatureControlConfig structFCC;
	public  cLocationGPSConfig structLGC;
	public  cLocationGPSStorage structLGS;
	public  cGCM structGCM;
	public  cCompnayControlConfig structCCC;
	public  byte[] bSchoolCalendar;	
	public   cSchoolSchedule lSchoolSchedule;
	public  SosControlMode structSosControlMode;
	public cApplicationControlConfig structACC;
	public cApplicationControlConfig structWCC;

}

