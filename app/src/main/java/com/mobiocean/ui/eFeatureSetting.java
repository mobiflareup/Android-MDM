package com.mobiocean.ui;

public interface eFeatureSetting 
	{ 
		public static final byte OFF = 0;  //means feature is not restricted; for calls there is no restriction
		public static final byte ON = 0x1; //means feature is restricted; for calls the restriction is Applicable
		public static final byte RESTRICTED_IN_DURATION = 0x2; //for SMS means no of SMS restriction applicable
		public static final byte RESTRICTED_IN_CLOCK_TIME = 0x4;
		public static final byte RESTRICTED_IN_DURATION_CLOCK_TIME = 0x6;
		public static final byte RESTRICTED_IN_NUMBERS = 0x8; //For calls no. restriction applicable														
	}
