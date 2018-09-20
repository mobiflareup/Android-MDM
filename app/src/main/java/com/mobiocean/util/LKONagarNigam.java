package com.mobiocean.util;

import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;
import com.mobiocean.ui.MobiApplication;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.conveyance.configuration.RConstant;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.ArrayList;



public class LKONagarNigam
{
	protected static final String PREFS_NAME = "MyPrefsFile";
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editor;

/*	*//*********  work only for Dedicated IP ***********//*
	private static String FTP_HOST= "mobiocean.gingerboxmobility.com";
	*//*********  FTP USERNAME ***********//*
	private static String FTP_USER ="MobiAndroid";
	*//*********  FTP PASSWORD ***********//*
	private static String FTP_PASS  ="Ocean@Ginger123";*/

	public static ArrayList<InsertIntoContactSync> loadContactSync(MobiApplication app) 
	{
		AanwlaService aanwlaService = app.getContactService();
		ArrayList<InsertIntoContactSync> contactsList1 = null;
		
		try 
		{
			contactsList1 =  (ArrayList<InsertIntoContactSync>) aanwlaService.GetSyncDateTime(CallHelper.Ds.structPC.iStudId).execute().body();			
		} catch (Exception e) 
		{
			DeBug.ShowLog("ContactsListLoader",""+e.getMessage());
			e.printStackTrace();
		}
		
		if(contactsList1==null)
		{
			return null;
		}
			
		return contactsList1;
	
	}
	
	public static ArrayList<InsertIntoContactSync> loadContactSyncFromDate(MobiApplication app ,String logDate) 
	{

		AanwlaService aanwlaService = app.getContactService();
		ArrayList<InsertIntoContactSync> contactsList1 = null;
		try 
		{
			contactsList1 =  (ArrayList<InsertIntoContactSync>) aanwlaService.listContactAccordingTodate(CallHelper.Ds.structPC.iStudId,logDate).execute().body();			
		} catch (Exception e) 
		{
			DeBug.ShowLog("ContactsListLoader",""+e.getMessage());
			e.printStackTrace();
		}
		
		if(contactsList1==null)
		{
			return null;
		}
			
		return contactsList1;
	
	}
	
	 public class CalendarSync
	 {
		 @SerializedName("Location")
		public String Location  ="";

		 @SerializedName("StartDateTime")
	 	public String StartDateTime ="";

		 @SerializedName("EndDateTime")
	 	public String EndDateTime ="";

		 @SerializedName("Repetition")
	 	public String Repetition ="";

		 @SerializedName("EventName")
	 	public String EventName ="";

		 @SerializedName("Description")
	 	public String Description ="";

		 @SerializedName("LogDate")
	 	public String LogDate="";

		 @SerializedName("SyncDateTime")
	 	public String SyncDateTime="";
	 	
	 	
	 }
	
	public static ArrayList<CalendarSync> loadCalendarDateSync(MobiApplication app) 
	{
		AanwlaService aanwlaService = app.getContactService();
		ArrayList<CalendarSync> calendarDateList = null;
		
		try 
		{
			calendarDateList = (ArrayList<CalendarSync>) aanwlaService.listCalendarDate(CallHelper.Ds.structPC.iStudId).execute().body();			
		} catch (Exception e) 
		{
			DeBug.ShowLog("ContactsListLoader",""+e.getMessage());
			e.printStackTrace();
		}
		
		if(calendarDateList==null)
		{
			return null;
		}
			
		return  calendarDateList;
	
	}

	public static ArrayList<CalendarSync> loadCalendarSyncFromDate(MobiApplication app ,String logDate) 
	{
		AanwlaService aanwlaService = app.getContactService();
		ArrayList<CalendarSync> calendarList = null;
		try 
		{
			calendarList =  (ArrayList<CalendarSync>) aanwlaService.listCalendarAccordingTodate(CallHelper.Ds.structPC.iStudId,logDate).execute().body();			
		} catch (Exception e) 
		{
			DeBug.ShowLog("ContactsListLoader",""+e.getMessage());
			e.printStackTrace();
		}
		
		if(calendarList==null)
		{
			return null;
		}
			
		return calendarList;
	}

	public static boolean  upLoadProfileFile(String picturePath, String fName)
	{
		String tempadd =null;
		FTPClient ftpClient = null;
		
		boolean result= false;
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(InetAddress.getByName(RConstant.FTP_HOST));

			if (ftpClient.login(RConstant.FTP_USER, RConstant.FTP_PASS)) {
				ftpClient.enterLocalPassiveMode(); // important!
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

				
					tempadd = "/profile/";

				
				boolean t0 = ftpClient.makeDirectory(tempadd);
				ftpClient.changeWorkingDirectory(tempadd);
				boolean t2 = ftpClient.makeDirectory(tempadd + "image");
				tempadd = tempadd + "image" + "/";
				boolean t1 = ftpClient.makeDirectory(tempadd + CallHelper.Ds.structPC.iStudId);
				tempadd = tempadd + CallHelper.Ds.structPC.iStudId + "/";
				boolean t = ftpClient.changeWorkingDirectory(tempadd);

				FileInputStream in = new FileInputStream(new File(picturePath));
				result = ftpClient.storeFile(tempadd + fName,in);
				DeBug.ShowLog("FileInputStream", "PicturePath : " + picturePath);
				DeBug.ShowLog("IPMSG", "" + result);
				in.close();
				ftpClient.logout();
				ftpClient.disconnect();

				DeBug.ShowLog("FTP","FTP upload : "+result);

			}
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
		
	}

	public static boolean upLoadBugImage(String picturePath, String fName)
	{
		String tempadd =null;
		FTPClient ftpClient = null;
		boolean result= false;
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(InetAddress.getByName(RConstant.FTP_HOST));

			if (ftpClient.login(RConstant.FTP_USER, RConstant.FTP_PASS)) {
				ftpClient.enterLocalPassiveMode(); // important!
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				tempadd = "/bugReport/";
				boolean t0 = ftpClient.makeDirectory(tempadd);
				FileInputStream in = new FileInputStream(new File(picturePath));
				result = ftpClient.storeFile(tempadd + fName,in);
				DeBug.ShowLog("FileInputStream", "PicturePath : " + picturePath);
				DeBug.ShowLog("IPMSG", "" + result);
				in.close();
				ftpClient.logout();
				ftpClient.disconnect();
				DeBug.ShowLog("FTP","FTP upload : "+result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public static boolean upLoadConveyanceImage(String picturePath, String fName)
	{
		String tempadd =null;
		FTPClient ftpClient = null;
		boolean result= false;
		try {
			ftpClient = new FTPClient();
			ftpClient.connect(InetAddress.getByName(RConstant.FTP_HOST));

			if (ftpClient.login(RConstant.FTP_USER, RConstant.FTP_PASS)) {
				ftpClient.enterLocalPassiveMode(); // important!
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
				tempadd = "/Conveyance/";
				boolean t0 = ftpClient.makeDirectory(tempadd);
				FileInputStream in = new FileInputStream(new File(picturePath));
				result = ftpClient.storeFile(tempadd + fName,in);
				DeBug.ShowLog("FileInputStream", "PicturePath : " + picturePath);
				DeBug.ShowLog("IPMSG", "" + result);
				in.close();
				ftpClient.logout();
				ftpClient.disconnect();
				DeBug.ShowLog("FTP","FTP upload : "+result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}