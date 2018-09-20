package com.mobiocean.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobiocean.util.Struct_Contact;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler_Call extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";

    // Contacts Table Columns names
    private static final String KEY_ID = "_id";
    private static final String KEY_NAME = "_sName";
    private static final String KEY_PH_NO = "_sPhone_number";
    private static final String KEY_DATE = "_dwdate";
    private static final String KEY_DUARTION = "_dwduration";
    private static final String KEY_CALLTYPE = "_iCallType";
    private static final String KEY_LAT = "_slat";
    private static final String KEY_LON = "_slon";
    private static final String KEY_CELL_ID = "_sCell_Id";
    private static final String KEY_LAC = "_sLac";
    private static final String KEY_MCC = "_sMcc";
    private static final String KEY_MNC = "_sMnc";

    public DatabaseHandler_Call(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT,"
                + KEY_LAT + " TEXT,"
                + KEY_LON + " TEXT,"
                + KEY_DATE + " integer,"
                + KEY_DUARTION + " integer,"
                + KEY_CALLTYPE + " integer,"
                + KEY_CELL_ID + " TEXT,"
                + KEY_LAC + " TEXT,"
                + KEY_MCC + " TEXT,"
                + KEY_MNC + " TEXT"
                + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
    public void addContact(Struct_Contact struct_Contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, struct_Contact.getName()); // Struct_Contact Name
        values.put(KEY_PH_NO, struct_Contact.getPhoneNumber()); // Struct_Contact Phone
        values.put(KEY_DATE, struct_Contact.getDate());
        values.put(KEY_LAT, struct_Contact.get_slat());
        values.put(KEY_LON, struct_Contact.get_slon());
        values.put(KEY_DUARTION, struct_Contact.getDuration());
        values.put(KEY_CALLTYPE, struct_Contact.getCallType());
        values.put(KEY_CELL_ID, struct_Contact.getCellId());
        values.put(KEY_LAC, struct_Contact.getLAC());
        values.put(KEY_MCC, struct_Contact.getMCC());
        values.put(KEY_MNC, struct_Contact.getMNC());
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting All Contacts
    public List<Struct_Contact> getAllContacts() {
        List<Struct_Contact> contactList = new ArrayList<Struct_Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Struct_Contact struct_Contact = new Struct_Contact();
                struct_Contact.setID(Integer.parseInt(cursor.getString(0)));
                struct_Contact.setName(cursor.getString(1));
                struct_Contact.setPhoneNumber(cursor.getString(2));
                struct_Contact.set_slat(cursor.getString(3));
                struct_Contact.set_slon(cursor.getString(4));
                struct_Contact.setDate(Long.parseLong(cursor.getString(5)));
                struct_Contact.setDuration(Integer.parseInt(cursor.getString(6)));
                struct_Contact.setCallType(Integer.parseInt(cursor.getString(7)));
                struct_Contact.setCellId(cursor.getString(8));
                struct_Contact.setLAC(cursor.getString(9));
                struct_Contact.setMCC(cursor.getString(10));
                struct_Contact.setMNC(cursor.getString(11));
                // Adding contact to list
                contactList.add(struct_Contact);
            } while (cursor.moveToNext());
        }
        db.close();
        // return contact list
        return contactList;
    }

    // Deleting single contact
    public void deleteContact(Struct_Contact struct_Contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[]{String.valueOf(struct_Contact.getID())});
        db.close();
    }

}
