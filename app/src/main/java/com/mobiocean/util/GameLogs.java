package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

public class GameLogs {

	@SerializedName("AppId")
	private String AppId;

	@SerializedName("Duration")
	private String Duration;

	@SerializedName("AppName")
	private String AppName;

	@SerializedName("StartTime")
	private String StartTime;

	@SerializedName("EndTime")
	private String EndTime;

	@SerializedName("LogDateTime")
	private String LogDateTime;

	@SerializedName("AppIndx")
	private String AppIndx;
	
	public String getAppId() {
		return AppId;
	}
	public void setAppId(String appId) {
		AppId = appId;
	}
	public String getDuration() {
		return Duration;
	}
	public void setDuration(String duration) {
		Duration = duration;
	}
	public String getAppName() {
		return AppName;
	}
	public void setAppName(String appName) {
		AppName = appName;
	}
	public String getStartTime() {
		return StartTime;
	}
	public void setStartTime(String startTime) {
		StartTime = startTime;
	}
	public String getEndTime() {
		return EndTime;
	}
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}
	public String getLogDateTime() {
		return LogDateTime;
	}
	public void setLogDateTime(String logDateTime) {
		LogDateTime = logDateTime;
	}
	public String getAppIndex() {
		return AppIndx;
	}
	public void setAppIndex(String appIndex) {
		AppIndx = appIndex;
	}
	
	
}
