package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SOSContacts
{

	@SerializedName("SosContacts")
	private ArrayList<SosContactsClass> SosContacts;

	@SerializedName("appID")
	private String appID;

	@SerializedName("mSosContacts")
	private SosContactsClass mSosContacts;
	
	public SOSContacts()
	{
		SosContacts = new ArrayList<SosContactsClass>();
		appID = "";
	}
	
	public ArrayList<SosContactsClass> getListContacts() {
		return SosContacts;
	}
	public ArrayList<SosContactsClass> getNewListContacts() {
		SosContacts = new ArrayList<SosContactsClass>() ;
		return  SosContacts;
	}
	public void setListContacts(ArrayList<SosContactsClass> listContacts) {
		this.SosContacts = listContacts;
	}

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}
	
	public SosContactsClass getmSosContacts() {
		return mSosContacts;
	}
	public SosContactsClass getNewmSosContacts() {
		return new SosContactsClass();
	}
	public void setmSosContacts(SosContactsClass mSosContacts) {
		this.mSosContacts = mSosContacts;
	}

	public class SosContactsClass
	{
		@SerializedName("ContactPersonName")
		String ContactPersonName;

		@SerializedName("MobileNo")
		String MobileNo;
		public String getContactPersonName() {
			return ContactPersonName;
		}
		public void setContactPersonName(String contactPersonName) {
			ContactPersonName = contactPersonName;
		}
		public String getMobileNo() {
			return MobileNo;
		}
		public void setMobileNo(String mobileNo) {
			MobileNo = mobileNo;
		}	
		
		
	}
}
