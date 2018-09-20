package com.mobiocean.mobidb;


public class LogInfoStruct
{
	
	private String _id;
	private String feature_index;
	private String subfeature_index;
	private String start_time;
	private String lastused_time;
	private String duration = "0";
	private String logdate_time;
	private String gamelog_id;
	private String Status;
	private String AppName;
	
	
	public LogInfoStruct()
	{
		
	}
	// constructor
	public LogInfoStruct(String _id,String feature_index, String subfeature_index, String start_time, String lastused_time , 
			String duration, String logdate_time, String  gamelog_id,
			String Status)
	
	{
		this._id         = _id;
		this.feature_index         = feature_index;
		this.subfeature_index = subfeature_index;	
		this.start_time        = start_time;
		this.lastused_time     = lastused_time; 
		this.duration     = duration;	
		this.logdate_time          = logdate_time; 
		this.gamelog_id          = gamelog_id;	
		this.Status          = Status;	
	
	}
	public LogInfoStruct(String feature_index, String subfeature_index, String start_time, String lastused_time , 
			String duration, String logdate_time, String  gamelog_id,
			String Status)
	
	{
		
		this.feature_index         = feature_index;
		this.subfeature_index = subfeature_index;	
		this.start_time        = start_time;
		this.lastused_time     = lastused_time; 
		this.duration     = duration;	
		this.logdate_time          = logdate_time; 
		this.gamelog_id          = gamelog_id;	
		this.Status          = Status;	
	
	}
	
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getFeature_index() {
		return feature_index;
	}

	public void setFeature_index(String feature_index) {
		this.feature_index = feature_index;
	}

	public String getSubfeature_index() {
		return subfeature_index;
	}

	public void setSubfeature_index(String subfeature_index) {
		this.subfeature_index = subfeature_index;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getLastused_time() {
		return lastused_time;
	}

	public void setLastused_time(String lastused_time) {
		this.lastused_time = lastused_time;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getLogdate_time() {
		return logdate_time;
	}

	public void setLogdate_time(String logdate_time) {
		this.logdate_time = logdate_time;
	}

	public String getGamelog_id() {
		return gamelog_id;
	}

	public void setGamelog_id(String gamelog_id) {
		this.gamelog_id = gamelog_id;
	}

	public String getStatus() {
		return Status;
	}

	public void setStatus(String status) {
		Status = status;
	}
	public String getAppName() {
		return AppName;
	}
	public void setAppName(String appName) {
		AppName = appName;
	}
	
}
