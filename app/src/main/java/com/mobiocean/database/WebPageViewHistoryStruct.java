package com.mobiocean.database;


public class WebPageViewHistoryStruct {
	
	private String pagename;
	private String logdatetime;
	private int _uploadation_Status;
	
	public WebPageViewHistoryStruct()
	{
		this.pagename="";
		this.logdatetime="";
		this._uploadation_Status=0;
	}
	
	public WebPageViewHistoryStruct(String pagename, String logdatetime ,int _uploadation_Status)
	{
		this.pagename=pagename;
		this.logdatetime=logdatetime;
		this._uploadation_Status=_uploadation_Status;	
	}

	/**
	 * @return the _uploadation_Status
	 */
	public int get_uploadation_Status() {
		return _uploadation_Status;
	}

	/**
	 * @param _uploadation_Status the _uploadation_Status to set
	 */
	public void set_uploadation_Status(int _uploadation_Status) {
		this._uploadation_Status = _uploadation_Status;
	}

	
	/**
	 * @return the pagename
	 */
	public String getPagename() {
		return pagename;
	}

	/**
	 * @param pagename the pagename to set
	 */
	public void setPagename(String pagename) {
		this.pagename = pagename;
	}

	/**
	 * @return the logdatetime
	 */
	public String getLogdatetime() {
		return logdatetime;
	}

	/**
	 * @param logdatetime the logdatetime to set
	 */
	public void setLogdatetime(String logdatetime) {
		this.logdatetime = logdatetime;
	}
	
	
}

