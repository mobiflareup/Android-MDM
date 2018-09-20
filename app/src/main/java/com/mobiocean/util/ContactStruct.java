package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ContactStruct {

	@SerializedName("AppId")
	private String AppId;

	@SerializedName("LogDateTime")
	private String LogDateTime;

	@SerializedName("contactlst")
	private List<InsertIntoContactSync> contactlst;
	
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
	public List<InsertIntoContactSync> getList() {
		return contactlst;
	}
	public void setList(List<InsertIntoContactSync> list) {
		contactlst = list;
	}
	
	
}
