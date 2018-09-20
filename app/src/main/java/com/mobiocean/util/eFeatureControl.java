/**
 *
 */
package com.mobiocean.util;

/**
 * @author Gingerbox
 *         IsPhotoAttendanceNeeded   = 0
 *         IsAudioAttendanceNeeded   = 1
 *         IsWorkReportNeeded        = 2
 *         IsVideoReportNeeded       = 3
 *         IsBinStatusNeeded         = 4
 *         IsCallSMSLogNeeded        = 5
 *         IsLocationNeeded          = 6
 */
public interface eFeatureControl {

    /**
     * 0
     */
    public static final byte LOCATION = 0x0001;//
    /**
     * 1
     */
    public static final byte BLOCK_CAMERA = 0x0002;//
    /**
     * 2
     */
    public static final byte FACT_RESET = 0x0004;//
    /**
     * 3
     */
    public static final byte BLOCK_BLUE = 0x0008;//
    /**
     * 4
     */
    public static final byte BLOCK_WIFI = 0x0010;//
    /**
     * 5
     */
    public static final byte BLOCK_HOTSPOT = 0x0020;//
    /**
     * 6
     */
    public static final byte BLOCK_NFC = 0x0040;
    /**
     * 7
     */
    public static final short BLOCK_CERT = 0x0080;
    /**
     * 8
     */
    public static final short BLOCK_TXT_CPY_PST = 0x0100;
    /**
     * 9
     */
    public static final short BLOCK_USB = 0x0200;
    /**
     * 10
     */
    public static final short BLOCK_VOICE_RECORD = 0x0400; // Block Mic
    /**
     * 11
     */
    public static final short BLOCK_SCREEN_CAPT = 0x0800;
    /**
     * 12
     */
    public static final short SEND_SMS = 0x1000;
    /**
     * 13
     */
    public static final short BLOCK_AIRPLANE = 0x2000;
    /**
     * 14
     */
    public static final short BLOCK_SCREEN_LOCK_TIME = 0x4000;
    /**
     * 15
     */
    public static final int DATA_CONNECTION = 0x8000;//

    /**
     * 16
     */
    public static final int SCREEN_CPTR_LOCT_OPTION = 0x00010000;
    /**
     * 17
     */
    public static final int SYNC_ADRESS = 0x00020000;//
    /**
     * 18
     */
    public static final int SYNC_CALENDER = 0x00040000;//
    /**
     * 19
     */
    public static final int ALLOWED_NUMBERS_CALL = 0x00080000;//from And to both
    /**
     * 20
     */
    public static final int CALL_HIST = 0x00100000;//
    /**
     * 21
     */
    public static final int SMS_HIST = 0x00200000;//
    /**
     * 22
     */
    public static final int ALLOWED_NUMBERS_SMS = 0x00400000;//from And to both
    /**
     * 23
     */
    public static final int ALLOW_SMS_From_NO = 0x00800000;//no need
    /**
     * 24
     */
    public static final int KEYWORD_FOR_ALERT = 0x01000000;
    /**
     * 25
     */
    public static final int SMS_TOCONT_NO = 0x02000000;
    /**
     * 26
     */
    public static final int AUTO_VOICE_REC = 0x04000000;
    /**
     * 27
     */
    public static final int FRONT_CAMERA_REC = 0x08000000;
    /**
     * 28
     */
    public static final int BACK_CAMERA_REC = 0x10000000;
    /**
     * 29
     */
    public static final int AUTO_PHONE_LOCK = 0x20000000;//           *****************************
    /**
     * 30
     */
    public static final int APP_REPSOTORY = 0x40000000;//
    /**
     * 31
     */
    public static final long BLOCK_NEW_APP = 0x80000000L;//

    /**
     * 32
     */
    public static final long BLOCK_PLAYSTORE = 0x0100000000L;//
    /**
     * 33
     */
    public static final long BLOCK_ITUNES = 0x0200000000L;//
    /**
     * 34
     */
    public static final long BLOCK_VIDEO_CHAT = 0x0400000000L;
    /**
     * 35
     */
    public static final long FORCE_SET_DATA_ACC = 0x0800000000L;
    /**
     * 36
     */
    public static final long BLOCK_GOOGLE_DRIVE_ACC = 0x1000000000L;
    /**
     * 37
     */
    public static final long lOCATE_DEVICE = 0x2000000000L;//
    /**
     * 38
     */
    public static final long GEO_FENCE = 0x4000000000L;//

    /**
     * 39
     */
    public static final long BROWSER_PROTECTION_old = 0x08000000000L;
    /**
     * 40
     */
    public static final long BROWSER_BLACK_LIST = 0x10000000000L;
    /**
     * 41
     */
    public static final long BROWSER_BLOCK_PORN = 0x20000000000L;
    /**
     * 42
     */
    public static final long BROWSER_PROTECTION = 0x40000000000L;//browser protection server

    /**
     * 43
     */
    public static final long INCOMING_CALL = 0x080000000000L;//incoming call
    /**
     * 44
     */
    public static final long OUTGOING_CALL = 0x100000000000L;//outgoing call
    /**
     * 45
     */
    public static final long INCOMING_SMS = 0x200000000000L;//inco SMS
    /**
     * 46
     */
    public static final long REMOTE_LOC = 0x400000000000L;//remot loc   *******************************
    /**
     * 47
     */
    public static final long REMOTE_TRIGGER = 0x800000000000L;//remot trigger
/**48*/
    /**
     * 49
     */
    public static final long MEMORY_WIPE = 0x2000000000000L;//memory wipe
    /**
     * 50
     */
    public static final long APP_LOG = 0x4000000000000L;//App log
    /**
     * 51
     */
    public static final long WEB_LOG = 0x8000000000000L;//web log

    /**
     * 52
     */
    public static final long AUTO_ANS = 0x10000000000000L;//web log

    //NARAYANAN S
    /**
     * 56
     */
    public static final long GPS_BUZZER = 0x100000000000000L;//gps turn off buzzer
    /**
     * 57
     */
    public static final long UNINSTALL_APP = 0x200000000000000L;//uninstall app
    /**
     * 58
     */
    public static final long BLOCK_OPEN_WIFI = 0x400000000000000L;//block open wifi
    /**
     * 59
     */
    public static final long BLOCK_WIFI_GATEWAY = 0x800000000000000L;//Block wifi configuration
    /**
     * 60
     */
    public static final long SET_LOCATION_ALERT = 0x1000000000000000L;//Set LocationAlert


    public static final byte iBLOCK_CAMERA = 1;
    public static final byte iFACT_RESET = 2;
    public static final byte iBLOCK_BLUE = 3;
    public static final byte iBLOCK_WIFI = 4;
    public static final byte iBLOCK_HOTSPOT = 5;
    public static final byte iBLOCK_NFC = 6;
    public static final short iBLOCK_CERT = 7;
    public static final short iBLOCK_TXT_CPY_PST = 8;
    public static final short iBLOCK_USB = 9;
    public static final short iBLOCK_VOICE_RECORD = 10;
    public static final short iBLOCK_SCREEN_CAPT = 11;
    public static final short iBLOCK_SIM_SWAP = 12;
    public static final short iBLOCK_AIRPLANE = 13;
    public static final short iBLOCK_SCREEN_LOCK_TIME = 14;
    public static final int iDATA_CONNECTION = 15;

    public static final int iSCREEN_CPTR_LOCT_OPTION = 16;
    public static final int iSYNC_ADRESS = 17;
    public static final int iSYNC_CALENDER = 18;
    public static final int iALLOWED_NUMBERS_CALL = 19;
    public static final int iCALL_HIST = 20;
    public static final int iSMS_HIST = 21;
    public static final int iALLOWED_NUMBERS_SMS = 22;
    public static final int iALLOW_SMS_From_NO = 23;
    public static final int iKEYWORD_FOR_ALERT = 24;
    public static final int iSMS_TOCONT_NO = 25;
    public static final int iAUTO_VOICE_REC = 26;
    public static final int iFRONT_CAMERA_REC = 27;
    public static final int iBACK_CAMERA_REC = 28;
    public static final int iAUTO_PHONE_LOCK = 29;
    public static final int iAPP_REPSOTORY = 30;

    public static final int iBLOCK_NEW_APP = 31;
    public static final int iBLOCK_PLAYSTORE = 32;
    public static final int iBLOCK_ITUNES = 33;
    public static final int iBLOCK_VIDEO_CHAT = 34;
    public static final int iFORCE_SET_DATA_ACC = 35;
    public static final int iBLOCK_GOOGLE_DRIVE_ACC = 36;
    public static final int ilOCATE_DEVICE = 37;
    public static final int iGEO_FENCE = 38;

    public static final int iBROWSER_PROTECTION_old = 39;
    public static final int iBROWSER_BLACK_LIST = 40;
    public static final int iBROWSER_BLOCK_PORN = 41;
    public static final int iBROWSER_PROTECTION = 42;

    public static final int iINCOMING_CALL = 43;//incoming call
    public static final int iOUTGOING_CALL = 44;//outgoing call
    public static final int iINCOMING_SMS = 45;//inco SMS
    public static final int iREMOTE_LOC = 46;//remot loc
    public static final int iREMOTE_TRIGGER = 47;//remot trigger
    /**
     * 48
     */
    public static final int iMEMORY_WIPE = 49;//memory wipe
    public static final int iAPP_LOG = 50;//App log
    public static final int iWEB_LOG = 51;//web log
    public static final int iAUTO_ANS = 52;//web log

    //NARAYANAN S
    public static final int iGPS_BUZZER = 56;//gps turn off buzzer
    public static final int iUNINSTALL_APP = 57;//uninstall app
    public static final int iBLOCK_OPEN_WIFI = 58;//block open wifi
    public static final int iBLOCK_WIFI_GATEWAY = 59;//Block wifi configuration
    public static final int iSET_LOCATION_ALERT = 60;//Set LocationAlert

    //SIVA
    public static final int iBLOCK_GPS = 63;//block gps in root device
    public static final int iBLOCK_NOTIFICATION = 64;//block notification
    public static final int iBLOCK_USBROOT = 65;//block usb root device
    public static final int iBLOCK_MOBILEDATA = 66;//block usb root device
    public static final int iBLOCK_QUICKSETTINGS = 67;//block QuickSettings and notification bar
/**
 * [
 {

 "FeatureId": 1,

 "FeatureCode": "Block Camera",

 },
 {

 "FeatureId": 2,

 "FeatureCode": "Block Factory Reset",

 },
 {

 "FeatureId": 3,

 "FeatureCode": "Block Bluetooth & Tethering",

 },
 {

 "FeatureId": 4,
 "CategoryId": 5,
 "FeatureCode": "Block Wi-Fi",

 },
 {

 "FeatureId": 5,
 "CategoryId": 5,
 "FeatureCode": "Block Hotspot",

 },
 {

 "FeatureId": 6,
 "CategoryId": 5,
 "FeatureCode": "Block NFC",

 },
 {

 "FeatureId": 7,
 "CategoryId": 5,
 "FeatureCode": "Block Certificates",

 },
 {

 "FeatureId": 8,
 "CategoryId": 5,
 "FeatureCode": "Block Text Copy-Paste",

 },
 {

 "FeatureId": 9,
 "CategoryId": 5,
 "FeatureCode": "Block USB Connection",

 },
 {

 "FeatureId": 10,
 "CategoryId": 5,
 "FeatureCode": "Block Voice Recording",

 },
 {

 "FeatureId": 11,
 "CategoryId": 5,
 "FeatureCode": "Block Screen Capture",

 },
 {

 "FeatureId": 12,
 "CategoryId": 5,
 "FeatureCode": "Block Sim Swap Protection",

 },
 {

 "FeatureId": 13,
 "CategoryId": 5,
 "FeatureCode": "Lock on Airplane Mode",

 },
 {

 "FeatureId": 14,
 "CategoryId": 5,
 "FeatureCode": "Screen Lock Time",

 },
 {

 "FeatureId": 15,
 "CategoryId": 5,
 "FeatureCode": "Data Connection",

 },
 {

 "FeatureId": 16,
 "CategoryId": 5,
 "FeatureCode": "Screen Capture Lock Reset Option",

 },
 {

 "FeatureId": 17,
 "CategoryId": 6,
 "FeatureCode": "Sync Address Book",

 },
 {

 "FeatureId": 18,
 "CategoryId": 6,
 "FeatureCode": "Sync Calender",

 },
 {

 "FeatureId": 19,
 "CategoryId": 6,
 "FeatureCode": "Allowed Calls to Numbers",

 },
 {

 "FeatureId": 20,
 "CategoryId": 6,
 "FeatureCode": "Call History",

 },
 {

 "FeatureId": 21,
 "CategoryId": 6,
 "FeatureCode": "SMS History",

 },
 {

 "FeatureId": 22,
 "CategoryId": 6,
 "FeatureCode": "Allowed SMS to Numbers",

 },
 {

 "FeatureId": 23,
 "CategoryId": 6,
 "FeatureCode": "Allowed SMS From Numbers",

 },
 {

 "FeatureId": 24,
 "CategoryId": 6,
 "FeatureCode": "Keyword for Alert ",

 },
 {

 "FeatureId": 25,
 "CategoryId": 7,
 "FeatureCode": "SMS to Contact Numbers",

 },
 {

 "FeatureId": 26,
 "CategoryId": 7,
 "FeatureCode": "Auto Voice Recording ",

 },
 {

 "FeatureId": 27,
 "CategoryId": 7,
 "FeatureCode": "Auto Front Camera Recording",

 },
 {

 "FeatureId": 28,
 "CategoryId": 7,
 "FeatureCode": "Auto Rear Camera Recording",

 },
 {

 "FeatureId": 29,
 "CategoryId": 7,
 "FeatureCode": "Auto Phone Locked",

 },
 {

 "FeatureId": 30,
 "CategoryId": 8,
 "FeatureCode": "App Repository",

 },
 {

 "FeatureId": 31,
 "CategoryId": 8,
 "FeatureCode": "Restrict New App Installation",

 },
 {

 "FeatureId": 32,
 "CategoryId": 8,
 "FeatureCode": "Block App store & Windows Store",

 },
 {

 "FeatureId": 33,
 "CategoryId": 8,
 "FeatureCode": "Block iTunes",

 },
 {

 "FeatureId": 34,
 "CategoryId": 8,
 "FeatureCode": "Block Video Chat",

 },
 {

 "FeatureId": 35,
 "CategoryId": 8,
 "FeatureCode": "Force to set Data Drive Account",

 },
 {

 "FeatureId": 36,
 "CategoryId": 8,
 "FeatureCode": "Block Google Drive Account",

 },
 {

 "FeatureId": 37,
 "CategoryId": 9,
 "FeatureCode": "Locate Device",

 },
 {

 "FeatureId": 38,
 "CategoryId": 9,
 "FeatureCode": "Geo  Fence",

 }
 ,
 {

 "FeatureId": 39,
 "CategoryId": 4,
 "FeatureCode": "Geo  Fence",

 }
 ,
 {

 "FeatureId": 41,
 "CategoryId": 7,
 "FeatureCode": "Geo  Fence",

 }
 ]*/
    /*FunctionId	FunctionName
	/*FunctionId	FunctionName
	 * 
	 */
	/*FunctionId	FunctionName
	1	Location	
	2	Incoming Call Logs	
	3	Outgoing Call Logs	
	4	Incoming SMS Logs	
	5	Outgoing SMS Logs	
	6	Work Report	
	7	Video Report	
	8	Image Attendance	
	9	Voice Attendance
		
	10	Bin Management	
	11	Regulate Incoming Call	
	12	Regulate Outgoing Call	
	13	Regulate Incoming SMS	
	14	Regulate App Usage
	15	Regulate Music
	16	Regulate Wi-Fi
	
	17	Regulate Data Connectivity
	18	Regulate Bluetooth	
	19	In-Out Attendance	
	21	Regulate Camera	
	22	Allowed PhoneNo	
	23	Website Restrictions	*/
}
