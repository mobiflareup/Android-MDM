package com.mobiocean.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobiocean.util.DeBug;

public class WebPageViewHistorySQLiteOpenHelper extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "WebPageHistoryName";

	// Contacts table name
	private static final String TABLE_CONTACTS = "WebPageHistoryInfo";
	
	private static WebPageViewHistorySQLiteOpenHelper db = null;

	// Contacts Table Columns names
	private static final String KEY_PageName = "pageName";
	private static final String KEY_LogdateTime = "logDateTime";
	private static final String KEY_uploadation_Status = "_uploadation_Status";

	public WebPageViewHistorySQLiteOpenHelper(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static WebPageViewHistorySQLiteOpenHelper getInstance(Context context)
	{
		
		if (db==null)
		 db = new WebPageViewHistorySQLiteOpenHelper(context);		
		return db;
	}
	
	public static SQLiteDatabase startProjectDataBaseAccess(Context context)
	{
		WebPageViewHistorySQLiteOpenHelper db = getInstance(context);
		SQLiteDatabase DB = db.getWritableDatabase();

		try {
			DB.beginTransaction();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DeBug.ShowLogD("LKON", "Locked");
		return DB; 
	}

	public static void stopProjectDataBaseAccess(Context context)
	{
		try {		
			
			WebPageViewHistorySQLiteOpenHelper db = getInstance(context);
		      SQLiteDatabase DB = db.getWritableDatabase();

		DB.setTransactionSuccessful();
		DB.endTransaction();
		//DB.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		DeBug.ShowLogD("LKON", "Un-Locked");
	}
	
	public static boolean isProjectDatabaseInTransaction(Context context)
	{
		boolean result = false;

		try {
			WebPageViewHistorySQLiteOpenHelper db = getInstance(context);
		SQLiteDatabase DB = db.getWritableDatabase();
		result =DB.inTransaction();
		} catch (Exception e) {
			DeBug.ShowLogD("LKON", "Un-Locked" +e.getMessage());
			e.printStackTrace();
		}
		DeBug.ShowLogD("LKON", "Un-Locked" +result);
		return result;
	}
	
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
			    + KEY_PageName + " TEXT, " + KEY_LogdateTime + " TEXT, " 
				+ KEY_uploadation_Status + " TEXT" +")";
		
		    db.execSQL(CREATE_CONTACTS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	public // Adding new contact
	void addContact(WebPageViewHistoryStruct mWebPageViewHistoryStruct) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_PageName, mWebPageViewHistoryStruct.getPagename());
		values.put(KEY_LogdateTime, mWebPageViewHistoryStruct.getLogdatetime());
		values.put(KEY_uploadation_Status, mWebPageViewHistoryStruct.get_uploadation_Status());
			
		// Inserting Row
		db.insert(TABLE_CONTACTS, null, values);
		 // Closing database connection
	}

	// Deleting single contact
		public void deleteContact(WebPageViewHistoryStruct mWebPageViewHistoryStruct) {
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(TABLE_CONTACTS, KEY_LogdateTime + " = ?",
					new String[] { mWebPageViewHistoryStruct.getLogdatetime()});
			
		}
	
	// Getting All Contacts
	public List<WebPageViewHistoryStruct> getAllContacts() {
		List<WebPageViewHistoryStruct> mWebPageViewHistoryStructList = new ArrayList<WebPageViewHistoryStruct>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				WebPageViewHistoryStruct mWebPageViewHistoryStruct = new WebPageViewHistoryStruct();
				mWebPageViewHistoryStruct.setPagename(cursor.getString(0));
				mWebPageViewHistoryStruct.setLogdatetime(cursor.getString(1));
				mWebPageViewHistoryStruct.set_uploadation_Status(cursor.getInt(2));
			
				mWebPageViewHistoryStructList.add(mWebPageViewHistoryStruct);
			} while (cursor.moveToNext());
		}
		
		// return contact list
		return mWebPageViewHistoryStructList;
	}
	

	// Updating single contact
	public int updateContact(WebPageViewHistoryStruct mWebPageViewHistoryStruct) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		//values.put(KEY_NAME, contact.getName());
		values.put(KEY_PageName, mWebPageViewHistoryStruct.getPagename());
		values.put(KEY_LogdateTime, mWebPageViewHistoryStruct.getLogdatetime());
		values.put(KEY_uploadation_Status, mWebPageViewHistoryStruct.get_uploadation_Status());
		
		// updating row
		return db.update(TABLE_CONTACTS, values, 
				null,
				null);
	}
	
	// Getting contacts Count
	public int getContactsCount() {
		String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		 int count = cursor.getCount();
		    cursor.close();
		    
		    return count;
	}
	
	
	public List<WebPageViewHistoryStruct>  getEnterUrlByFeatureIndex(int featureIndex) 
	{
		List<WebPageViewHistoryStruct> mWebPageViewHistoryStructList = new ArrayList<WebPageViewHistoryStruct>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_CONTACTS, 
				new String[] {KEY_PageName, KEY_LogdateTime, KEY_uploadation_Status}, 
				
				KEY_uploadation_Status + "=?",
						new String[] {String.valueOf(featureIndex) },
						null, null,  null, null);
		
		if (cursor.moveToFirst()) 
		{
			do {
				WebPageViewHistoryStruct mWebPageViewHistoryStruct = new WebPageViewHistoryStruct();
				mWebPageViewHistoryStruct.setPagename(cursor.getString(0));
				mWebPageViewHistoryStruct.setLogdatetime(cursor.getString(1));
				mWebPageViewHistoryStruct.set_uploadation_Status(cursor.getInt(2));
				
				mWebPageViewHistoryStructList.add(mWebPageViewHistoryStruct);
			} while (cursor.moveToNext());
		}
		// return contact
		return mWebPageViewHistoryStructList;
	}
	
}
