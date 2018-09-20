package com.mobiocean.mobidb;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ApplicationInfoStruct {

	private int appIndex ;
	private int IsInstalled ; 
	private int isSyncWithServer ;
	private int appGroup ;
	private String appName ;
	private String appPackege ;
	private int isAllowed ;
	private int isAllowedNow ;
	private long Timestamp;
	
	public int getAppIndex() {
		return appIndex;
	}
	public void setAppIndex(int appIndex) {
		this.appIndex = appIndex;
	}
	public int getIsInstalled() {
		return IsInstalled;
	}
	public void setIsInstalled(int isInstalled) {
		IsInstalled = isInstalled;
	}
	public int getIsSyncWithServer() {
		return isSyncWithServer;
	}
	public void setIsSyncWithServer(int isSyncWithServer) {
		this.isSyncWithServer = isSyncWithServer;
	}
	public int getAppGroup() {
		return appGroup;
	}
	public void setAppGroup(int appGroup) {
		this.appGroup = appGroup;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getAppPackege() {
		return appPackege;
	}
	public void setAppPackege(String appPackege) {
		this.appPackege = appPackege;
	}
	public int getIsAllowed() {
		return isAllowed;
	}
	public void setIsAllowed(int isAllowed) {
		this.isAllowed = isAllowed;
	}
	public int getIsAllowedNow() {
		return isAllowedNow;
	}
	public void setIsAllowedNow(int isAllowedNow) {
		this.isAllowedNow = isAllowedNow;
	}
	public long getTimestamp() {
		return Timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.Timestamp = timestamp;
	}
	
	public String getTimestampForServer() {
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Timestamp);
		return formatter.format(calendar.getTime()).toString();
	}
}
