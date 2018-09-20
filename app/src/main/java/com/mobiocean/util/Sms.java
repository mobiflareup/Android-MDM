package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Sms{

	@SerializedName("AppId")
	private String AppId ;

	@SerializedName("LogDateTime")
	private String LogDateTime ;

	@SerializedName("SMS")
	private List<SMS> SMS = new ArrayList<Sms.SMS>();
	
	public String getAppId() {
		return AppId;
	}
	public void setAppId(String appId) {
		AppId = appId;
	}
	public String getLogDateTime() {
		return LogDateTime;
	}
	public void setLogDateTime(String logDateTime) {
		LogDateTime = logDateTime;
	}
	public List<SMS> getSmsList() {
		return SMS;
	}
	public void setSmsList(List<SMS> smsList) {
		this.SMS = smsList;
	}
	public void addToList(SMS sms)
	{
		SMS.add(sms);
	}
    public SMS newSMS()
    {
    	return new SMS();
    }
	public class SMS
	{
		@SerializedName("MobileNo")
		private String MobileNo;

		@SerializedName("MsgText")
		private String MsgText;

		@SerializedName("IsIncoming")
		private int IsIncoming;

		@SerializedName("startDateTime")
		private String startDateTime;

		public String getAddress(){
			return MobileNo;
		}
		public String getMsg(){
			return MsgText;
		}


		public void setAddress(String address){
			MobileNo = address;
		}
		public void setMsg(String msg){
			MsgText = msg;
		}

		public int getIsIncoming() {
			return IsIncoming;
		}
		public void setIsIncoming(int isIncoming) {
			IsIncoming = isIncoming;
		}
		public String getStartDateTime() {
			return startDateTime;
		}
		public void setStartDateTime(String startDateTime) {
			this.startDateTime = startDateTime;
		}
	}
}
