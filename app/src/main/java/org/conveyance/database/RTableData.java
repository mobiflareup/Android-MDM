package org.conveyance.database;

import android.provider.BaseColumns;

/**
 * @author Narayanan
 */

public class RTableData {

    public static abstract class ExtraAllowance implements BaseColumns {
        static final String TABLE_NAME = "ExtraAllowance";
        static final String APPID = "appId";
        static final String REMARKS = "remarks";
        static final String AMOUNT = "amount";
        static final String PROOF = "proof";
        static final String DATETIME = "datetime";
        static final String INSERTDATE = "insertdate";
        static final String ISSYNC = "issync";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                REMARKS + " TEXT," +
                APPID + " TEXT," +
                AMOUNT + " TEXT," +
                PROOF + " TEXT," +
                DATETIME + " TEXT," +
                INSERTDATE + " TEXT," +
                ISSYNC + " INTEGER)";
    }

    public static abstract class StartStopLocation implements BaseColumns {

        static final String TABLE_NAME = "StartStopLocation";
        static final String IS_LOGIN = "IsLogin";
        static final String CUSTOMER_ID = "CustomerId";
        static final String MODE_OF_TRAVEL = "ModeOfTravel";
        static final String FILE_PATH = "FilePath";
        static final String DATETIME = "DateTime";
        static final String REMARKS = "Remark";
        static final String LATITUDE = "Latitude";
        static final String LONGITUDE = "Longitude";
        static final String ACCURACY = "Accuracy";
        static final String ALTITUDE = "Altitude";
        static final String BEARING = "Bearing";
        static final String ELAPSED_REAL_TIME_NANOS = "ElapsedRealTimeNanos";
        static final String PROVIDER = "Provider";
        static final String SPEED = "Speed";
        static final String TIME = "Time";
        static final String CELL_ID = "CellId";
        static final String MCC = "Mcc";
        static final String MNC = "Mnc";
        static final String LAC = "Lac";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                IS_LOGIN + " INTEGER," +
                CUSTOMER_ID + " TEXT," +
                MODE_OF_TRAVEL + " TEXT," +
                FILE_PATH + " TEXT," +
                DATETIME + " TEXT," +
                REMARKS + " TEXT," +
                LATITUDE + " TEXT," +
                LONGITUDE + " TEXT," +
                ACCURACY + " TEXT," +
                ALTITUDE + " TEXT," +
                BEARING + " TEXT," +
                ELAPSED_REAL_TIME_NANOS + " TEXT," +
                PROVIDER + " TEXT," +
                SPEED + " TEXT," +
                TIME + " TEXT," +
                CELL_ID + " TEXT," +
                MCC + " TEXT," +
                MNC + " TEXT," +
                LAC + " TEXT)";

    }

    public static abstract class ModeOfTravelTable implements BaseColumns {
        static final String TABLE_NAME = "modeTravelTable";
        static final String modeoftravelid = "modeoftravelid";
        static final String modename = "modename";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                modeoftravelid + " INTEGER UNIQUE," +
                modename + " TEXT)";
    }

    public static abstract class CustomerListTable implements BaseColumns {
        static final String TABLE_NAME = "customerListTable";
        static final String APPID = "appId";
        static final String CustomerId = "CustomerId";
        static final String CustomerTypeId = "CustomerTypeId";
        static final String CustomerName = "CustomerName";
        static final String EmailId = "EmailId";
        static final String MobileNo = "MobileNo";
        static final String Country = "Country";
        static final String State = "State";
        static final String City = "City";
        static final String Latitude = "Latitude";
        static final String Longitude = "Longitude";
        static final String IsActive = "IsActive";
        static final String Address = "Address";
        static final String AltEmailId = "AltEmailId";
        static final String AltMobileNo = "AltMobileNo";
        static final String District = "District";
        static final String ContactPerson = "ContactPerson";
        static final String AltContactPerson = "AltContactPerson";
        static final String PinCode = "PinCode";
        static final String TinNumber = "TinNumber";
        static final String AltAddress = "AltAddress";
        static final String cname = "cname";
        static final String sname = "sname";
        static final String cityname = "cityname";
        static final String ctypename = "ctypename";
        static final String userid = "userid";

        static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " " + "(" +
                _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                APPID + " TEXT," +
                CustomerId + " INTEGER UNIQUE," +
                CustomerTypeId + " INTEGER," +
                CustomerName + " TEXT," +
                EmailId + " TEXT, " +
                MobileNo + " TEXT, " +
                Country + " INTEGER," +
                State + " INTEGER," +
                City + " INTEGER," +
                Latitude + " DOUBLE," +
                Longitude + " DOUBLE," +
                IsActive + " INTEGER, " +
                Address + " TEXT, " +
                AltEmailId + " TEXT," +
                AltMobileNo + " TEXT," +
                District + " TEXT," +
                ContactPerson + " TEXT," +
                AltContactPerson + " TEXT," +
                PinCode + " TEXT, " +
                TinNumber + " TEXT, " +
                AltAddress + " TEXT," +
                cname + " TEXT," +
                sname + " TEXT," +
                cityname + " TEXT," +
                ctypename + " TEXT," +
                userid + " INTEGER)";
    }
}