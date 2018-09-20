package com.mobiocean.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobiocean.util.DeBug;
import com.mobiocean.util.Struct_Send_SMSInfo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHabdler_SMSSent extends SQLiteOpenHelper {


    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 4;

    // Database Name
    private static final String DATABASE_NAME = "SentSMSManager";

    // Contacts table name
    protected static final String TABLE_SMS = "SMSSent";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_INFO_TYPE = "_iInfoType";
    private static final String KEY_TIME = "_stime";
    private static final String KEY_APP_NAME = "_sAppName";
    private static final String KEY_INFO2 = "_sInfo2";
    private static final String KEY_SAVE_TIME = "_sSaveTime";
    private static final String KEY_LAT = "_slat";
    private static final String KEY_LON = "_slon";
    private static final String KEY_INDEX = "_iIndex";
    private static final String KEY_LAST_SMS_TIME = "_sLast_Sms_Time";
    private static final String KEY_CELL_ID = "_sCell_Id";
    private static final String KEY_LAC = "_sLac";
    private static final String KEY_MCC = "_sMcc";
    private static final String KEY_MNC = "_sMnc";

    private static DatabaseHabdler_SMSSent db = null;

    protected static final String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_SMS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_INFO_TYPE + " integer,"
            + KEY_TIME + " TEXT,"
            + KEY_APP_NAME + " TEXT,"
            + KEY_INFO2 + " TEXT,"
            + KEY_LAT + " TEXT,"
            + KEY_LON + " TEXT,"
            + KEY_INDEX + " integer,"
            + KEY_SAVE_TIME + " TEXT,"
            + KEY_LAST_SMS_TIME + " TEXT,"
            + KEY_CELL_ID + " TEXT,"
            + KEY_LAC + " TEXT,"
            + KEY_MCC + " TEXT,"
            + KEY_MNC + " TEXT"
            + ")";


    public DatabaseHabdler_SMSSent(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DatabaseHabdler_SMSSent getInstance(Context context) {
        if (db == null)
            db = new DatabaseHabdler_SMSSent(context);

        return db;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_SMS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);

        // Create tables again
        onCreate(db);
    }

    public synchronized int recordExist(String TimeStamp, int info_type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String getRow = "SELECT COUNT(" + KEY_SAVE_TIME + ") FROM " + TABLE_SMS + " WHERE " + KEY_TIME + "='" + TimeStamp + "'";
        DeBug.ShowLog("CallDB", getRow);
        Cursor cursor = db.query
                (TABLE_SMS,
                        new String[]{KEY_ID, KEY_INFO_TYPE, KEY_TIME, KEY_APP_NAME, KEY_INFO2, KEY_LAT, KEY_LON, KEY_INDEX, KEY_SAVE_TIME, KEY_LAST_SMS_TIME,
                                KEY_CELL_ID, KEY_LAC, KEY_MCC, KEY_MNC},
                        KEY_TIME + "=?" + " AND " + KEY_INFO_TYPE + "=?",
                        new String[]{TimeStamp, String.valueOf(info_type)},
                        null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count;
    }

    // Adding new struct_sent_sms
    public synchronized void addSMS(Struct_Send_SMSInfo struct_SMS) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_INFO_TYPE, struct_SMS.getInfoType()); // Struct_Contact Name
        values.put(KEY_TIME, struct_SMS.getTime()); // Struct_Contact Phone
        values.put(KEY_APP_NAME, struct_SMS.getAppName());
        values.put(KEY_INFO2, struct_SMS.getInfo2());
        values.put(KEY_LAT, struct_SMS.getLat());
        values.put(KEY_LON, struct_SMS.getLon());
        values.put(KEY_INDEX, struct_SMS.getIndex());
        values.put(KEY_SAVE_TIME, struct_SMS.getSave_Time());
        values.put(KEY_LAST_SMS_TIME, struct_SMS.getLast_Sms_Time());
        values.put(KEY_CELL_ID, struct_SMS.getCellId());
        values.put(KEY_LAC, struct_SMS.getLAC());
        values.put(KEY_MCC, struct_SMS.getMCC());
        values.put(KEY_MNC, struct_SMS.getMNC());
        DeBug.ShowLog("CallDB", db.insert(TABLE_SMS, null, values) + "INFO TYPE " + struct_SMS.getInfoType() + " Time " + struct_SMS.getTime() + " SaveTime" + struct_SMS.getSave_Time());
        // Inserting Row
        //db.insert(TABLE_SMS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single struct_sent_sms
    synchronized public List<Struct_Send_SMSInfo> getSMSBytype(int Type) {
        List<Struct_Send_SMSInfo> contactList = new ArrayList<Struct_Send_SMSInfo>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query
                (TABLE_SMS,
                        new String[]{KEY_ID, KEY_INFO_TYPE, KEY_TIME, KEY_APP_NAME, KEY_INFO2, KEY_LAT, KEY_LON, KEY_INDEX, KEY_SAVE_TIME, KEY_LAST_SMS_TIME,
                                KEY_CELL_ID, KEY_LAC, KEY_MCC, KEY_MNC},
                        KEY_INFO_TYPE + "=?",
                        new String[]{String.valueOf(Type)},
                        null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Struct_Send_SMSInfo struct_sent_sms = new Struct_Send_SMSInfo();
                struct_sent_sms.Id = cursor.getInt(0);
                struct_sent_sms.setInfoType(cursor.getInt(1));
                struct_sent_sms.setTime(cursor.getString(2));
                struct_sent_sms.setAppName(cursor.getString(3));
                struct_sent_sms.setInfo2(cursor.getString(4));
                struct_sent_sms.setLat(cursor.getDouble(5));
                struct_sent_sms.setLon(cursor.getDouble(6));
                struct_sent_sms.setIndex(cursor.getInt(7));
                struct_sent_sms.setSave_Time(cursor.getLong(8));
                struct_sent_sms.setLast_Sms_Time(cursor.getLong(9));
                struct_sent_sms.setCellId(cursor.getString(10));
                struct_sent_sms.setLAC(cursor.getString(11));
                struct_sent_sms.setMCC(cursor.getString(12));
                struct_sent_sms.setMNC(cursor.getString(13));

                DeBug.ShowLog("CallDB", " Time " + struct_sent_sms.getTime() + " SaveTime " + struct_sent_sms.getSave_Time());

                // Adding struct_sent_sms to list
                contactList.add(struct_sent_sms);
            } while (cursor.moveToNext());
        }
        db.close();
        // return struct_sent_sms
        return contactList;
    }

    // Getting All Contacts
    synchronized public List<Struct_Send_SMSInfo> getAllSMS() {
        List<Struct_Send_SMSInfo> contactList = new ArrayList<Struct_Send_SMSInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
    /*	Cursor cursor = db.query
                   (TABLE_SMS,
					new String[] { KEY_ID,KEY_INFO_TYPE, KEY_TIME,KEY_APP_NAME, KEY_INFO2, KEY_LAT,KEY_LON,KEY_INDEX, KEY_SAVE_TIME ,KEY_LAST_SMS_TIME}, 
					null,
					null, 
					null, null, KEY_SAVE_TIME+" DESC", null);*/

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Struct_Send_SMSInfo struct_sent_sms = new Struct_Send_SMSInfo();
                struct_sent_sms.Id = cursor.getInt(0);
                struct_sent_sms.setInfoType(cursor.getInt(1));
                struct_sent_sms.setTime(cursor.getString(2));
                struct_sent_sms.setAppName(cursor.getString(3));
                struct_sent_sms.setInfo2(cursor.getString(4));
                struct_sent_sms.setLat(cursor.getDouble(5));
                struct_sent_sms.setLon(cursor.getDouble(6));
                struct_sent_sms.setIndex(cursor.getInt(7));
                struct_sent_sms.setSave_Time(cursor.getLong(8));
                struct_sent_sms.setLast_Sms_Time(cursor.getLong(9));
                struct_sent_sms.setCellId(cursor.getString(10));
                struct_sent_sms.setLAC(cursor.getString(11));
                struct_sent_sms.setMCC(cursor.getString(12));
                struct_sent_sms.setMNC(cursor.getString(13));
                // Adding struct_sent_sms to list
                contactList.add(struct_sent_sms);
            } while (cursor.moveToNext());
        }
        db.close();
        // return struct_sent_sms list
        return contactList;
    }

    // Updating single struct_sent_sms
    synchronized public int updateLastSMSSentTime(Struct_Send_SMSInfo struct_sent_sms) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_NAME, struct_sent_sms.getName());

        values.put(KEY_LAST_SMS_TIME, struct_sent_sms.getLast_Sms_Time());

        // updating row
        int update = db.update(TABLE_SMS, values,
                KEY_SAVE_TIME + "=?",
                new String[]{String.valueOf(struct_sent_sms.getSave_Time())});
        db.close();
        return update;

    }

    // Deleting single struct_sent_sms
    synchronized public void deleteSMS(Struct_Send_SMSInfo struct_sent_sms) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SMS, KEY_SAVE_TIME + " = ?",
                new String[]{String.valueOf(struct_sent_sms.getSave_Time())});
        //db.delete(TABLE_CONTACTS, null, null);
        DeBug.ShowLog("DELETED", "" + struct_sent_sms.getInfoType());
        db.close();
    }

    // Getting contacts Count
    synchronized public int getSMSCount() {
        String countQuery = "SELECT  * FROM " + TABLE_SMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count;

    }

}
