package com.mobiocean.mobidb;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SOSandTheftInfoStruct {

	@SerializedName("IsforSos")
	private int IsforSos ;

	@SerializedName("IsAudio")
	private int IsAudio ;

	@SerializedName("FileName")
	private String FileName ;

	@SerializedName("FilePath")
	private String FilePath ;

	@SerializedName("localFilePath")
	private String localFilePath ;

	@SerializedName("AppId")
	private String AppId ;

	@SerializedName("Latitude")
	private String Latitude ;

	@SerializedName("Longitude")
	private String Longitude ;

	@SerializedName("CellId")
	private String CellId ;

	@SerializedName("locationAreaCode")
	private String locationAreaCode ;

	@SerializedName("mobileCountryCode")
	private String mobileCountryCode ;

	@SerializedName("mobileNetworkCode")
	private String mobileNetworkCode ;

	@SerializedName("LogDateTime")
	private String LogDateTime;

	@SerializedName("MobileNo")
	private int IsUploaded;
	/*
	 * >{
>"IsforSos":"0",
>"IsAudio":"0",
>"FileName":"Test.mp3",
>"FilePath":"/Files/Test.mp3",
>"localFilePath"
>"AppId":"MDM-1",
>"Latitude":"0",
>"Longitude":"0",
>"LogDateTime":"01-JAN?-2016 11:00",
>"CellId":"0",
>"locationAreaCode":"0",
>"mobileCountryCode":"0",
>"mobileNetworkCode":"0"
>}
	 * */
	



	public int getIsforSos() {
		return IsforSos;
	}


	public int getIsUploaded() {
		return IsUploaded;
	}


	public void setIsUploaded(int isUploaded) {
		IsUploaded = isUploaded;
	}


	public void setIsforSos(int isforSos) {
		IsforSos = isforSos;
	}


	public int getIsAudio() {
		return IsAudio;
	}


	public void setIsAudio(int isAudio) {
		IsAudio = isAudio;
	}


	public String getFileName() {
		return FileName;
	}


	public void setFileName(String fileName) {
		FileName = fileName;
	}


	public String getFilePath() {
		return FilePath;
	}


	public void setFilePath(String filePath) {
		FilePath = filePath;
	}


	public String getLocalFilePath() {
		return localFilePath;
	}


	public void setLocalFilePath(String localFilePath) {
		this.localFilePath = localFilePath;
	}


	public String getAppId() {
		return AppId;
	}


	public void setAppId(String appId) {
		AppId = appId;
	}


	public String getLatitude() {
		return Latitude;
	}


	public void setLatitude(String latitude) {
		Latitude = latitude;
	}


	public String getLongitude() {
		return Longitude;
	}


	public void setLongitude(String longitude) {
		Longitude = longitude;
	}


	public String getCellId() {
		return CellId;
	}


	public void setCellId(String cellId) {
		CellId = cellId;
	}


	public String getLocationAreaCode() {
		return locationAreaCode;
	}


	public void setLocationAreaCode(String locationAreaCode) {
		this.locationAreaCode = locationAreaCode;
	}


	public String getMobileCountryCode() {
		return mobileCountryCode;
	}


	public void setMobileCountryCode(String mobileCountryCode) {
		this.mobileCountryCode = mobileCountryCode;
	}


	public String getMobileNetworkCode() {
		return mobileNetworkCode;
	}


	public void setMobileNetworkCode(String mobileNetworkCode) {
		this.mobileNetworkCode = mobileNetworkCode;
	}


	public String getLogDateTime() {
		return LogDateTime;
	}


	public void setLogDateTime(String logDateTime) {
		LogDateTime = logDateTime;
	}
	
	
	public String getTimestampForServer() {
		DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(LogDateTime));
		return formatter.format(calendar.getTime()).toString();
	}

}
