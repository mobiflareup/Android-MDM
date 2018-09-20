package com.mobiocean.mobidb;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobiocean.util.DeBug;

public class LogInfoDatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "logInfo";

	// Contacts table name
	private static final String TABLE_CONTACTS = "loginfo";
	
	private static LogInfoDatabaseHandler db = null;

	// Contacts Table Columns names
	private static final String KEY_ID = "_id";
	private static final String KEY_FEATURE_INDEX = "_feature_index";
	private static final String KEY_SUBFEATURE_INDEX = "_subfeature_index";
	private static final String KEY_START_TIME = "_start_time";
	private static final String KEY_LASTUSED_TIME = "_lastused_time";
	private static final String KEY_DURATION = "_duration";	
	private static final String KEY_LOGDATE_TIME = "_logdate_time";
	private static final String KEY_GAMELOG_ID= "_gamelog_id";
	private static final String APP_NAME= "app_name";
	private static final String KEY_STATUS = "_Status";


	public LogInfoDatabaseHandler(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static LogInfoDatabaseHandler getInstance(Context context)
	{
		
		if (db==null)
		 db = new LogInfoDatabaseHandler(context);		
		return db;
	}
	
	public static SQLiteDatabase startProjectDataBaseAccess(Context context)
	{
		LogInfoDatabaseHandler db = getInstance(context);
		SQLiteDatabase DB = db.getWritableDatabase();

		try {
			DB.beginTransaction();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DeBug.ShowLogD("LKON", "Locked");
		return DB; 
	}

	public static void stopProjectDataBaseAccess(Context context)
	{
		try {		
			
			LogInfoDatabaseHandler db = getInstance(context);
		      SQLiteDatabase DB = db.getWritableDatabase();

		DB.setTransactionSuccessful();
		DB.endTransaction();
		//DB.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DeBug.ShowLogD("LKON", "Un-Locked");
	}
	
	public static boolean isProjectDatabaseInTransaction(Context context)
	{
		boolean result = false;

		try {
			LogInfoDatabaseHandler db = getInstance(context);
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
		String CREATE_LOGINFO_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_FEATURE_INDEX + " TEXT," + KEY_SUBFEATURE_INDEX + " TEXT," 
				+ KEY_START_TIME + " TEXT,"+ KEY_LASTUSED_TIME + " TEXT,"
				+ KEY_DURATION+ " TEXT," + KEY_LOGDATE_TIME + " TEXT,"
				+ KEY_GAMELOG_ID + " TEXT,"
				+ APP_NAME + " TEXT,"
				+ KEY_STATUS + " TEXT"+")";
		
		    db.execSQL(CREATE_LOGINFO_TABLE);
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

	public void addLogInfo(LogInfoStruct mLogInfo) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_FEATURE_INDEX, mLogInfo.getFeature_index());
		values.put(KEY_SUBFEATURE_INDEX, mLogInfo.getSubfeature_index());
		values.put(KEY_START_TIME, mLogInfo.getStart_time());
		values.put(KEY_LASTUSED_TIME, mLogInfo.getLastused_time());
		values.put(KEY_DURATION, mLogInfo.getDuration());
		values.put(KEY_LOGDATE_TIME, mLogInfo.getLogdate_time());
		values.put(KEY_GAMELOG_ID, mLogInfo.getGamelog_id());
		values.put(KEY_STATUS, mLogInfo.getStatus());
		values.put(APP_NAME, mLogInfo.getAppName());
		// Inserting Row
		db.insert(TABLE_CONTACTS, null, values);
		 // Closing database connection
	}

	// Getting single contact
	public List<LogInfoStruct>  getProjectByName(String projectname) 
	{
		List<LogInfoStruct> listLogInfo = new ArrayList<LogInfoStruct>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_CONTACTS, 
				new String[] {KEY_ID,KEY_FEATURE_INDEX,KEY_SUBFEATURE_INDEX, 
					   KEY_START_TIME,KEY_LASTUSED_TIME,KEY_DURATION,
					   KEY_LOGDATE_TIME,KEY_GAMELOG_ID,APP_NAME,
					   KEY_STATUS},				   
					   KEY_GAMELOG_ID + "=?",
				new String[] {projectname}, 
				KEY_GAMELOG_ID, null, null, null);
		if (cursor.moveToFirst()) 		{
			do {
				LogInfoStruct mLogInfo = new LogInfoStruct();
				mLogInfo.set_id(cursor.getString(cursor.getColumnIndex(KEY_ID)));
				mLogInfo.setFeature_index(cursor.getString(cursor.getColumnIndex(KEY_FEATURE_INDEX)));
				mLogInfo.setSubfeature_index(cursor.getString(cursor.getColumnIndex(KEY_SUBFEATURE_INDEX)));
				mLogInfo.setStart_time(cursor.getString(cursor.getColumnIndex(KEY_START_TIME)));
				mLogInfo.setLastused_time(cursor.getString(cursor.getColumnIndex(KEY_LASTUSED_TIME)));
				mLogInfo.setDuration(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
				mLogInfo.setLogdate_time(cursor.getString(cursor.getColumnIndex(KEY_LOGDATE_TIME)));
				mLogInfo.setGamelog_id(cursor.getString(cursor.getColumnIndex(KEY_GAMELOG_ID)));
				mLogInfo.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
				mLogInfo.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
			
				// Adding contact to list
				listLogInfo.add(mLogInfo);
			} while (cursor.moveToNext());
		}
		// return contact
		return listLogInfo;
	}
	// Getting single contact
		public List<LogInfoStruct>  getDataByFeatureIndex(String FeatureIndex) 
		{
			List<LogInfoStruct> listLogInfo = new ArrayList<LogInfoStruct>();
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_CONTACTS, 
					new String[] {KEY_ID,KEY_FEATURE_INDEX,KEY_SUBFEATURE_INDEX, KEY_START_TIME,KEY_LASTUSED_TIME,KEY_DURATION,KEY_LOGDATE_TIME,KEY_GAMELOG_ID,
						   APP_NAME,KEY_STATUS},				   
						   KEY_FEATURE_INDEX + "=?",
					new String[] {FeatureIndex}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 		{
				do {
					LogInfoStruct mLogInfo = new LogInfoStruct();
					mLogInfo.set_id(cursor.getString(cursor.getColumnIndex(KEY_ID)));
					mLogInfo.setFeature_index(cursor.getString(cursor.getColumnIndex(KEY_FEATURE_INDEX)));
					mLogInfo.setSubfeature_index(cursor.getString(cursor.getColumnIndex(KEY_SUBFEATURE_INDEX)));
					mLogInfo.setStart_time(cursor.getString(cursor.getColumnIndex(KEY_START_TIME)));
					mLogInfo.setLastused_time(cursor.getString(cursor.getColumnIndex(KEY_LASTUSED_TIME)));
					mLogInfo.setDuration(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
					mLogInfo.setLogdate_time(cursor.getString(cursor.getColumnIndex(KEY_LOGDATE_TIME)));
					mLogInfo.setGamelog_id(cursor.getString(cursor.getColumnIndex(KEY_GAMELOG_ID)));
					mLogInfo.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
					mLogInfo.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
				
					// Adding contact to list
					listLogInfo.add(mLogInfo);
				} while (cursor.moveToNext());
			}
			// return contact
			return listLogInfo;
		}
		
		public LogInfoStruct  getDataByFeatureIndexAndSubFeatureIndex(String featureIndex, String subFeatureIndex) 
		{
			List<LogInfoStruct> listLogInfo = new ArrayList<LogInfoStruct>();
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_CONTACTS, 
					new String[] {KEY_ID,KEY_FEATURE_INDEX,KEY_SUBFEATURE_INDEX, KEY_START_TIME,KEY_LASTUSED_TIME,KEY_DURATION,KEY_LOGDATE_TIME,KEY_GAMELOG_ID,
						   APP_NAME,KEY_STATUS},				   
						   KEY_FEATURE_INDEX + "=? AND "+KEY_SUBFEATURE_INDEX+"=?",
					new String[] {featureIndex,subFeatureIndex}, 
					null, null, null, null);
			
			LogInfoStruct mLogInfo = null;
			if (cursor.moveToFirst()) 		
			{
				do {
					mLogInfo = new LogInfoStruct();
					mLogInfo.set_id(cursor.getString(cursor.getColumnIndex(KEY_ID)));
					mLogInfo.setFeature_index(cursor.getString(cursor.getColumnIndex(KEY_FEATURE_INDEX)));
					mLogInfo.setSubfeature_index(cursor.getString(cursor.getColumnIndex(KEY_SUBFEATURE_INDEX)));
					mLogInfo.setStart_time(cursor.getString(cursor.getColumnIndex(KEY_START_TIME)));
					mLogInfo.setLastused_time(cursor.getString(cursor.getColumnIndex(KEY_LASTUSED_TIME)));
					mLogInfo.setDuration(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
					mLogInfo.setLogdate_time(cursor.getString(cursor.getColumnIndex(KEY_LOGDATE_TIME)));
					mLogInfo.setGamelog_id(cursor.getString(cursor.getColumnIndex(KEY_GAMELOG_ID)));
					mLogInfo.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
					mLogInfo.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
				
					// Adding contact to list
				//	listLogInfo.add(mLogInfo);
				} while (cursor.moveToNext());
			}
			// return contact
			return mLogInfo;
		}
		
		public LogInfoStruct  getDataByFeatureIndexAndSubFeatureIndexAndDate(String featureIndex, String subFeatureIndex,String date) 
		{
			List<LogInfoStruct> listLogInfo = new ArrayList<LogInfoStruct>();
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_CONTACTS, 
					new String[] {KEY_ID,KEY_FEATURE_INDEX,KEY_SUBFEATURE_INDEX, KEY_START_TIME,KEY_LASTUSED_TIME,KEY_DURATION,KEY_LOGDATE_TIME,KEY_GAMELOG_ID,
						   APP_NAME,KEY_STATUS},				   
						   KEY_FEATURE_INDEX + "=? AND "+KEY_SUBFEATURE_INDEX+"=? AND "+KEY_LOGDATE_TIME+" LIKE ?",
					new String[] {featureIndex,subFeatureIndex, date+"%" }, 
					null, null, null, null);
			
			LogInfoStruct mLogInfo = null;
			if (cursor.moveToFirst()) 		
			{
				do {
					mLogInfo = new LogInfoStruct();
					mLogInfo.set_id(cursor.getString(cursor.getColumnIndex(KEY_ID)));
					mLogInfo.setFeature_index(cursor.getString(cursor.getColumnIndex(KEY_FEATURE_INDEX)));
					mLogInfo.setSubfeature_index(cursor.getString(cursor.getColumnIndex(KEY_SUBFEATURE_INDEX)));
					mLogInfo.setStart_time(cursor.getString(cursor.getColumnIndex(KEY_START_TIME)));
					mLogInfo.setLastused_time(cursor.getString(cursor.getColumnIndex(KEY_LASTUSED_TIME)));
					mLogInfo.setDuration(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
					mLogInfo.setLogdate_time(cursor.getString(cursor.getColumnIndex(KEY_LOGDATE_TIME)));
					mLogInfo.setGamelog_id(cursor.getString(cursor.getColumnIndex(KEY_GAMELOG_ID)));
					mLogInfo.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
					mLogInfo.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
				
					// Adding contact to list
				//	listLogInfo.add(mLogInfo);
				} while (cursor.moveToNext());
			}
			// return contact
			return mLogInfo;
		}
	// Getting All Contacts
	public List<LogInfoStruct> getAllContacts() 
	{
		List<LogInfoStruct> listLogInfo = new ArrayList<LogInfoStruct>();
		// Select All Query

		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.query
				   (TABLE_CONTACTS, 
					new String[] {KEY_ID,KEY_FEATURE_INDEX,KEY_SUBFEATURE_INDEX, KEY_START_TIME,KEY_LASTUSED_TIME,KEY_DURATION,KEY_LOGDATE_TIME,KEY_GAMELOG_ID,
						   APP_NAME,KEY_STATUS}, 					   
						   KEY_STATUS+"=?",
						   new String[] {"2"}, 
					null, null, null, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				LogInfoStruct mLogInfo = new LogInfoStruct();
				mLogInfo.set_id(cursor.getString(cursor.getColumnIndex(KEY_ID)));
				mLogInfo.setFeature_index(cursor.getString(cursor.getColumnIndex(KEY_FEATURE_INDEX)));
				mLogInfo.setSubfeature_index(cursor.getString(cursor.getColumnIndex(KEY_SUBFEATURE_INDEX)));
				mLogInfo.setStart_time(cursor.getString(cursor.getColumnIndex(KEY_START_TIME)));
				mLogInfo.setLastused_time(cursor.getString(cursor.getColumnIndex(KEY_LASTUSED_TIME)));
				mLogInfo.setDuration(cursor.getString(cursor.getColumnIndex(KEY_DURATION)));
				mLogInfo.setLogdate_time(cursor.getString(cursor.getColumnIndex(KEY_LOGDATE_TIME)));
				mLogInfo.setGamelog_id(cursor.getString(cursor.getColumnIndex(KEY_GAMELOG_ID)));
				mLogInfo.setStatus(cursor.getString(cursor.getColumnIndex(KEY_STATUS)));
				mLogInfo.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
			
				// Adding contact to list
				listLogInfo.add(mLogInfo);
			} while (cursor.moveToNext());
		}
		// return contact list
		return listLogInfo;
	}

	// Updating single contact
	public int updateLogInfo(LogInfoStruct mLogInfo ) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		//values.put(KEY_NAME, contact.getName());
		values.put(KEY_START_TIME, mLogInfo.getStart_time());
		values.put(KEY_LASTUSED_TIME, mLogInfo.getLastused_time());
		values.put(KEY_DURATION, mLogInfo.getDuration());
		values.put(KEY_LOGDATE_TIME, mLogInfo.getLogdate_time());
		values.put(KEY_GAMELOG_ID, mLogInfo.getGamelog_id());
		values.put(KEY_STATUS, mLogInfo.getStatus());
		values.put(APP_NAME, mLogInfo.getAppName());
		// updating row
		return db.update(TABLE_CONTACTS, values, 
				KEY_FEATURE_INDEX +"=? AND "+KEY_SUBFEATURE_INDEX+"=?",
				new String[] {mLogInfo.getFeature_index(),mLogInfo.getSubfeature_index()});
	}
	public int updateStatus(LogInfoStruct mLogInfo, String date) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		//values.put(KEY_NAME, contact.getName());

		values.put(KEY_STATUS, mLogInfo.getStatus());

		// updating row
		return db.update(TABLE_CONTACTS, values, 
				KEY_FEATURE_INDEX +"=? AND "+KEY_SUBFEATURE_INDEX+"=? AND "+KEY_LOGDATE_TIME+" LIKE ?",
				new String[] {mLogInfo.getFeature_index(),mLogInfo.getSubfeature_index(),date+"%" });
	}
	public int updateLogInfoIncrement(String Where) 
	{
		SQLiteDatabase db = this.getWritableDatabase();
	//	String countQuery = "UPDATE words SET KEY_DURATION = KEY_DURATION + 1 WHERE Where= ? ", new String[] {Where};
	
		Cursor cursor = db.rawQuery("Select * FROM "+ TABLE_CONTACTS + " SET "+ KEY_DURATION + " = " + KEY_DURATION + "+1", new String[] {Where});
		int  count=cursor.getCount();
		cursor.close();
	
		return count;	
	}


	// Deleting single contact
	public void deleteLogInfo(LogInfoStruct mLogInfo) 
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_CONTACTS, KEY_GAMELOG_ID + " = ?",
				new String[] { mLogInfo.getGamelog_id()});
		
	}


	// Getting contacts Count
	public int getLogInfoCount() 
	{
		String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
	//	cursor.close();

		// return count
		return cursor.getCount();
	}
			
}
