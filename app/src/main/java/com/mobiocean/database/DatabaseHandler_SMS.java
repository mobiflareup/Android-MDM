package com.mobiocean.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobiocean.util.DeBug;
import com.mobiocean.util.Struct_SMS;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler_SMS extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;

    // Database Name
    private static final String DATABASE_NAME = "SMSManager";

    // Contacts table name
    private static final String TABLE_SMS = "SMS";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "_sName";
    private static final String KEY_PH_NO = "_sPhone_number";
    private static final String KEY_DATE = "_dwdate";
    private static final String KEY_BODY = "_sBody";
    private static final String KEY_SMSTYPE = "_iSMSType";
    private static final String KEY_LAT = "_slat";
    private static final String KEY_LON = "_slon";
    private static final String KEY_CELL_ID = "_sCell_Id";
    private static final String KEY_LAC = "_sLac";
    private static final String KEY_MCC = "_sMcc";
    private static final String KEY_MNC = "_sMnc";
    private static final String KEY_LOG_DATE_TIME = "_log_date";

    public DatabaseHandler_SMS(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_SMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT,"
                + KEY_LAT + " TEXT,"
                + KEY_LON + " TEXT,"
                + KEY_DATE + " integer,"
                + KEY_BODY + " TEXT,"
                + KEY_SMSTYPE + " integer,"
                + KEY_CELL_ID + " TEXT,"
                + KEY_LAC + " TEXT,"
                + KEY_MCC + " TEXT,"
                + KEY_MNC + " TEXT"
                + KEY_LOG_DATE_TIME + " TEXT"
                + ")";
        db.execSQL(CREATE_SMS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        onCreate(db);
    }

    // Adding new contact
    public void addSMS(Struct_SMS struct_SMS) {
        SQLiteDatabase db = this.getWritableDatabase();

        DeBug.ShowLog("CallDB", struct_SMS.get_slon() + " lon storing send SMS lat.." + struct_SMS.get_slat());
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, struct_SMS.getName()); // Struct_Contact Name
        values.put(KEY_PH_NO, struct_SMS.getPhoneNumber()); // Struct_Contact Phone
        values.put(KEY_LAT, struct_SMS.get_slat());
        values.put(KEY_LON, struct_SMS.get_slon());
        values.put(KEY_DATE, struct_SMS.getDate());
        values.put(KEY_BODY, struct_SMS.getSMSBody());
        values.put(KEY_SMSTYPE, struct_SMS.getSMSType());
        values.put(KEY_CELL_ID, struct_SMS.getCellId());
        values.put(KEY_LAC, struct_SMS.getLAC());
        values.put(KEY_MCC, struct_SMS.getMCC());
        values.put(KEY_MNC, struct_SMS.getMNC());
        values.put(KEY_LOG_DATE_TIME, struct_SMS.getLogDateTime());
        // Inserting Row
        db.insert(TABLE_SMS, null, values);
        db.close(); // Closing database connection
    }

    // Getting All Contacts
    public List<Struct_SMS> getAllSMS() {
        List<Struct_SMS> contactList = new ArrayList<Struct_SMS>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SMS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Struct_SMS contact = new Struct_SMS();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setPhoneNumber(cursor.getString(2));
                contact.set_slat(cursor.getString(3));
                contact.set_slon(cursor.getString(4));
                contact.setDate(Long.parseLong(cursor.getString(5)));
                contact.setSMSBody(cursor.getString(6));
                contact.setSMSType(Integer.parseInt(cursor.getString(7)));
                contact.setCellId(cursor.getString(8));
                contact.setLAC(cursor.getString(9));
                contact.setMCC(cursor.getString(10));
                contact.setMNC(cursor.getString(11));
                contact.setLogDateTime(cursor.getString(12));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return contactList;
    }

    // Deleting single contact
    public void deleteSMS(Struct_SMS contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SMS, KEY_ID + " = ?", new String[]{String.valueOf(contact.getID())});
        db.close();
    }
}
