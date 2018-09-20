package com.mobiocean.util;

import java.util.ArrayList;
import java.util.List;





public class Struct_Send_SMSInfo 
{
	public int Id;
	private int InfoType; 
	private String Time; 
	private String AppName; 
	private String Info2; 
	private double Lat;  
	private double Lon; 
	private int index;
	private long Save_Time;
	private long Last_Sms_Time;
	public static byte SMSsent = 0;
	private String CellId;
	private String LAC;
	private String MCC;
	private String MNC;

	/**
	// 0= no sms request
	// 1= sms request sent
	// 2= sms sent/info sent to server
	// 3= sms not sent
	**/
	
	public static  List<Struct_Send_SMSInfo> oListSMSInfo = new ArrayList<Struct_Send_SMSInfo>();
	
/*	public static Thread_SendSMS thread_SendSMS = new Thread_SendSMS();
	
	public static void startSMSSendThread()
	{
		thread_SendSMS = new Thread_SendSMS();
		thread_SendSMS.start();
	}*/
	
	public Struct_Send_SMSInfo()
	{
		this.CellId="0";
		this.LAC="0";
		this.MCC="0";
		this.MNC="0";
	}
	
	public Struct_Send_SMSInfo(final int InfoType , final String Time, final String AppName, 
			 final String Info2,final double Lat  ,final double Lon,
			 final int index, final long Save_Time ,final String CellId,
			 final String LAC,final String MCC,	final String MNC )
	 {
		 this.InfoType= InfoType; 
		 this.Time=Time; 
		 this.AppName=AppName; 		
		 this.Info2= Info2; 
		 this.Lat=Lat;
		 this.Lon=Lon; 
		 this.index=index;	
		 this.Save_Time = Save_Time;
		 this.CellId = CellId;
		 this.LAC = LAC;
		 this.MCC =MCC;
		 this.MNC =MNC;
		 
	 }
	
	public String getCellId() {
		return CellId;
	}

	public void setCellId(String cellId) {
		CellId = cellId;
	}

	public String getLAC() {
		return LAC;
	}

	public void setLAC(String lAC) {
		LAC = lAC;
	}

	public String getMCC() {
		return MCC;
	}

	public void setMCC(String mCC) {
		MCC = mCC;
	}

	public String getMNC() {
		return MNC;
	}

	public void setMNC(String mNC) {
		MNC = mNC;
	}

	public long getLast_Sms_Time() {
		return Last_Sms_Time;
	}

	public void setLast_Sms_Time(long last_Sms_Time) {
		Last_Sms_Time = last_Sms_Time;
	}

	public int getInfoType() {
		return InfoType;
	}
	public String getTime() {
		return Time;
	}
	public String getAppName() {
		return AppName;
	}
	public String getInfo2() {
		return Info2;
	}
	public double getLat() {
		return Lat;
	}
	public double getLon() {
		return Lon;
	}
	public int getIndex() {
		return index;
	}
	public long getSave_Time() {
		return Save_Time;
	}

	public void setSave_Time(long save_Time) {
		Save_Time = save_Time;
	}

	public void setInfoType(int infoType) {
		InfoType = infoType;
	}

	public void setTime(String time) {
		Time = time;
	}

	public void setAppName(String appName) {
		AppName = appName;
	}

	public void setInfo2(String info2) {
		Info2 = info2;
	}

	public void setLat(double lat) {
		Lat = lat;
	}

	public void setLon(double lon) {
		Lon = lon;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
}
