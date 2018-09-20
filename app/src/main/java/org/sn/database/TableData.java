package org.sn.database;

import android.provider.BaseColumns;

/**
 * @author Narayanan
 */

public class TableData {

    private static String TEXT = " TEXT";
    private static String INTEGER = " INTEGER";
    private static String BOOLEAN = " BOOLEAN";
    private static String PRIMARY_KEY = " INTEGER PRIMARY KEY AUTOINCREMENT";
    private static String COMMA = ", ";
    private static String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";


    public static abstract class MobiProfileDetails implements BaseColumns {
        static final String TABLE_NAME = "MobiProfileDetails";
        static final String CODE = "code";
        static final String PROFILE_ID = "profileId";
        static final String FEATURE_ID = "featureId";
        static final String IS_ENABLED = "isEnabled";
        static final String IS_WHITE_LIST = "isWhiteList";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROFILE_ID + " INTEGER," +
                CODE + " TEXT," +
                FEATURE_ID + " INTEGER," +
                IS_ENABLED + " INTEGER," +
                IS_WHITE_LIST + " INTEGER)";
    }

    public static abstract class MobiProfileInfo implements BaseColumns {
        static final String TABLE_NAME = "MobiProfileInfo";
        static final String PROFILE_ID = "profileId";
        static final String PROFILE_NAME = "profileName";
        static final String PROFILE_CODE = "profileCode";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROFILE_ID + " INTEGER," +
                PROFILE_NAME + " TEXT," +
                PROFILE_CODE + " TEXT)";
    }

    public static abstract class AppGroup implements BaseColumns {
        static final String TABLE_NAME = "AppGroup";
        static final String CODE = "code";
        static final String PROFILE_ID = "profileId";
        static final String GROUP_ID = "groupId";
        static final String IS_ENABLED = "isEnabled";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROFILE_ID + " INTEGER," +
                CODE + " TEXT," +
                GROUP_ID + " INTEGER," +
                IS_ENABLED + " INTEGER)";
    }

    public static abstract class WebList implements BaseColumns {
        static final String TABLE_NAME = "WebList";
        static final String URL = "url";
        static final String PROFILE_ID = "profileId";
        static final String GROUP_ID = "groupId";
        static final String IS_WHITE_LIST = "isWhiteList";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROFILE_ID + " INTEGER," +
                URL + " TEXT," +
                GROUP_ID + " INTEGER," +
                IS_WHITE_LIST + " INTEGER)";
    }

    public static abstract class ContactList implements BaseColumns {
        static final String TABLE_NAME = "ContactList";
        static final String CONTACT_NUMBER = "contactNumber";
        static final String PROFILE_ID = "profileId";
        static final String IS_INCOMING = "isIncoming";
        static final String IS_OUTGOING = "isOutgoing";
        static final String IS_SMS = "isSms";
        static final String IS_WHITE_LIST = "isWhiteList";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROFILE_ID + " INTEGER," +
                CONTACT_NUMBER + " TEXT," +
                IS_INCOMING + " INTEGER," +
                IS_OUTGOING + " INTEGER," +
                IS_SMS + " INTEGER," +
                IS_WHITE_LIST + " INTEGER)";
    }

    public static abstract class SensorList implements BaseColumns {
        static final String TABLE_NAME = "SensorList";
        static final String PROFILE_ID = "profileId";
        static final String BSSID = "BSSID";
        static final String SSID = "SSID";
        static final String PASSWORD = "password";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PROFILE_ID + " INTEGER," +
                BSSID + " TEXT," +
                SSID + " TEXT," +
                PASSWORD + " TEXT)";
    }

    public static abstract class ConveyanceLocation implements BaseColumns {

        static final String TABLE_NAME = "ConveyanceLocation";
        static final String IS_LOGIN = "IsLogin";
        static final String FILE_PATH = "FilePath";
        static final String DATETIME = "DateTime";
        static final String REMARKS = "Remark";
        static final String VEHICLE_READING = "VehicleReading";
        static final String LATITUDE = "Latitude";
        static final String LONGITUDE = "Longitude";
        static final String CELL_ID = "CellId";
        static final String MCC = "Mcc";
        static final String MNC = "Mnc";
        static final String LAC = "Lac";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IS_LOGIN + " TEXT," +
                FILE_PATH + " TEXT," +
                DATETIME + " TEXT," +
                REMARKS + " TEXT," +
                VEHICLE_READING + " FLOAT," +
                LATITUDE + " TEXT," +
                LONGITUDE + " TEXT," +
                CELL_ID + " TEXT," +
                MCC + " TEXT," +
                MNC + " TEXT," +
                LAC + " TEXT)";
    }

    public static class SecuredStorageTableVar implements BaseColumns {
        protected static final String TABLE_NAME = "SecuredStorageTable";
        protected static final String DOWNLOAD_URL = "DownloadUrl";
        protected static final String STORAGE_PATH = "StoragePath";
        protected static final String FILE_NAME = "FileName";
        protected static final String IS_DOWNLOADED = "IsDownloaded";
        static final String CREATE_TABLE = CREATE_TABLE_IF_NOT_EXISTS + TABLE_NAME + " (" +
                _ID + PRIMARY_KEY + COMMA +
                DOWNLOAD_URL + TEXT + COMMA +
                STORAGE_PATH + TEXT + COMMA +
                FILE_NAME + TEXT + COMMA +
                IS_DOWNLOADED + BOOLEAN + " )";
    }
}