package com.mobiocean.util;

/*
 Worthwhile functions : name(), ordinal().. 
                        Documentation available under java.lang.Enum
 						values() - returns an array (CAT[]) of all the enum elements
*/

public enum CAT {
	GAME,	 //0
	MEDIA,	 //1
	RADIO,	 //2
	CAMERA,	 //3
	WIFI,	 //4
	DATACONNECTIVITY,//5
	BLUETOOTH,//6
	WEB,	 //7
	CHAT,	 //8	
	ALWAYS,
	HIDDEN;
	
	
	public static final int SIZE=11;
	
	public int intValue() {
		return ordinal();
	}
	
	public boolean equalTo(int i) {
		if(ordinal()==i)
			return true;
		return false;
	}
}
