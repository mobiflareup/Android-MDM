package com.mobiocean.ui;

public interface ePhoneMode 
{ 
	
	public static final byte NONE = 0x00;
	public static final byte GENERAL = 0x01;
	public static final byte OUTDOOR = 0x02;
	public static final byte SILENT = 0x03;
	public static final byte VIBRATE = 0x04;
	public static final byte FLASHING = 0x05; 
	public static final byte MEETING = 0x06;
	public static final byte HEADSETS = 0x07;
	public static final byte FLIGHTMODE = 0x08;
	public static final byte GSM_POWERDOWN_MODE = 0x10;
	public static final byte GSM_GPS_POWERDOWN_MODE = 0x12;
	public static final byte GSM_GPS_WIFI_POWERDOWN_MODE = 0x16;
	public static final byte PARENT_RESTRICTED = 0x20;
	public static final byte SCHOOL_RESTRICTED = 0x40;
	
}
