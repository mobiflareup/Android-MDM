package com.mobiocean.util;

import java.io.File;

public class Struct_Upload_data_Info 
{
	 private File ImagefileName ; 
	 private  String pathFolder; 
	 private String ParentpathFolder; 

	 private String workerId ; 
	 private boolean binOrEmp;
	 private String BinId;
	 private String siteLocationId;
	 private String StringIndexId;
	 private String ImageorAudio;
	 private long time;
	 


	public Struct_Upload_data_Info(final File ImagefileName , final String pathFolder, final String ParentpathFolder, 
			 final boolean binOrEmp,final String workerId  ,final String BinId,
			 final String siteLocationId,final String StringIndexId,final String ImageorAudio,final long time)
	 {
		 this.ImagefileName= ImagefileName; 
		 this.pathFolder=pathFolder; 
		 this.ParentpathFolder=ParentpathFolder; 		
		 this.workerId= workerId; 
		 this.binOrEmp=binOrEmp;
		 this.BinId=BinId; 
		 this.siteLocationId=siteLocationId;
		 this.StringIndexId=StringIndexId;
		 this.ImageorAudio=ImageorAudio;
		 this.time=time;
		 
	 }

	public long getTime() {
		return time;
	}

	public File getImagefileName() {
		return ImagefileName;
	}

	public String getPathFolder() {
		return pathFolder;
	}

	public String getParentpathFolder() {
		return ParentpathFolder;
	}



	public String getWorkerId() {
		return workerId;
	}

	public boolean isBinOrEmp() {
		return binOrEmp;
	}

	public String getBinId() {
		return BinId;
	}
	 public String getSiteLocationId() {
		return siteLocationId;
	}

	public String getStringIndexId() {
		return StringIndexId;
	}

	public String getImageorAudio() {
		return ImageorAudio;
	}
	 
}
