package com.mobiocean.util;

public class Struct_SMS {

	
	//private variables
	int _id;
	String _sName;
	String _sPhone_number;
    long _dwdate;
    String _sBody; 
    int _iSMSType;
    String _slat;
    String _slon;
	private String CellId;
	private String LAC;
	private String MCC;
	private String MNC;
	private String LogDateTime;
	// Empty constructor
	public Struct_SMS(){
		
	}
	// constructor
	public Struct_SMS(int id, String name, String _phone_number){
		this._id = id;
		this._sName = name;
		this._sPhone_number = _phone_number;
	}
	
	// constructor
	public Struct_SMS(String name, String phoneNumber,long date,String  Body , int SMStype,String lat, String  lon)
	{
		this._sName         = name;
		this._sPhone_number = phoneNumber;	
		this._dwdate        = date;
		this._sBody         = Body; 
		this._iSMSType      = SMStype;	
		this._slat          = lat; 
		this._slon          = lon;	
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
	// getting ID
	public int getID(){
		return this._id;
	}
	
	// setting id
	public void setID(int id){
		this._id = id;
	}
	
	// getting name
	public String getName(){
		return this._sName;
	}
	
	// setting name
	public void setName(String name){
		this._sName = name;
	}
	
	// getting phone number
	public String getPhoneNumber(){
		return this._sPhone_number;
	}
	
	// setting phone number
	public void setPhoneNumber(String phone_number){
		this._sPhone_number = phone_number;
	}
	//set  and get Date
	public void setDate(long Date){
		this._dwdate = Date;
	}
	
	public long getDate(){
		return this._dwdate;
	}
	//set  and get duration
	public void setSMSBody(String Body){
		this._sBody = Body;
	}
	
	public String getSMSBody(){
		return this._sBody;
	}
	
	//set  and get CallType
	public void setSMSType(int SMSType){
		this._iSMSType = SMSType;
	}
	
	public int getSMSType(){
		return this._iSMSType;
	}
	public String get_slat() {
		return _slat;
	}
	public void set_slat(String _slat) {
		this._slat = _slat;
	}
	public String get_slon() {
		return _slon;
	}
	public void set_slon(String _slon) {
		this._slon = _slon;
	}

	public String getLogDateTime() {
		return LogDateTime;
	}

	public void setLogDateTime(String logDateTime) {
		LogDateTime = logDateTime;
	}
}
