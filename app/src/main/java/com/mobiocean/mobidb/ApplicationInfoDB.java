package com.mobiocean.mobidb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mobiocean.util.Struct_SMS;

import java.util.ArrayList;
import java.util.List;

public class ApplicationInfoDB extends SQLiteOpenHelper 
{

	public static ApplicationInfoDB mApplicationInfoDatabaseHandler;
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "ApplicationInfo";

	// Contacts table name
	private static final String TABLE_NAME = "ApplicationInfoTable";

	// Contacts Table Columns names
	private static final String KEY_ID = "_id";
	private static final String appIndex = "appIndex";
	private static final String IsInstalled = "IsInstalled";
	private static final String isSyncWithServer = "isSyncWithServer";
	private static final String appGroup = "appGroup";
	private static final String appName = "appName";
	private static final String appPackege = "appPackege";
	private static final String isAllowed = "isAllowed";
	private static final String isAllowedNow= "isAllowedNow";
	private static final String Timestamp="Timestamp";
	
	public ApplicationInfoDB(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static ApplicationInfoDB getInstance(Context context)
	{
		if(mApplicationInfoDatabaseHandler==null)
			mApplicationInfoDatabaseHandler = new ApplicationInfoDB(context);
		
		return mApplicationInfoDatabaseHandler;
	}
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID  + " INTEGER PRIMARY KEY," 
				+ appIndex + " INTEGER,"
				+ IsInstalled + " TEXT," 
				+ isSyncWithServer+ " TEXT," 
				+ appGroup + " TEXT," 
				+ appName + " TEXT,"
				+ appPackege + " TEXT,"
				+ isAllowed + " TEXT,"
     			+ isAllowedNow + " TEXT,"
     			+ Timestamp + " TEXT"
				+")";
		db.execSQL(CREATE_SMS_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new contact
	public void addData(ApplicationInfoStruct mApplicationInfoStruct) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		//Log.i("CallDB", struct_SMS.get_slon()+" lon storing send SMS lat.."+struct_SMS.get_slat());
		ContentValues values = new ContentValues();
		values.put(appIndex, mApplicationInfoStruct.getAppIndex()); // Struct_Contact Name
		values.put(IsInstalled, mApplicationInfoStruct.getIsInstalled()); // Struct_Contact Phone
		values.put(appPackege, mApplicationInfoStruct.getAppPackege());
		values.put(isAllowed, mApplicationInfoStruct.getIsAllowed());
		values.put(isSyncWithServer, mApplicationInfoStruct.getIsSyncWithServer());
		values.put(appGroup, mApplicationInfoStruct.getAppGroup());
		values.put(appName, mApplicationInfoStruct.getAppName());
		values.put(isAllowedNow, mApplicationInfoStruct.getIsAllowedNow());
		values.put(Timestamp, mApplicationInfoStruct.getTimestamp());
	
		// Inserting Row
		db.insert(TABLE_NAME, null, values);
		//db.close(); // Closing database connection
	}

	// Getting single contact
	public ArrayList<String>  getDataByGroup(int groupIndex) 
	{
		ArrayList<String> listPack = new ArrayList<String>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,appIndex, IsInstalled,isSyncWithServer, appGroup, 
					   appName,appPackege,isAllowed,isAllowedNow,Timestamp}, 
					   appGroup + "=?",
				new String[] { String.valueOf(groupIndex) }, 
				null, null, null, null);

		if (cursor.moveToFirst()) 
		{
			do {
					listPack.add(cursor.getString(cursor.getColumnIndex(appPackege)));
				// Adding contact to list
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact
		return listPack;
	}
	
	public boolean checkIsinGroup(int groupIndex, int AppIndex) 
	{
	boolean result = false;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,appIndex, IsInstalled,isSyncWithServer, appGroup, 
					   appName,appPackege,isAllowed,isAllowedNow,Timestamp}, 
					   appGroup + "=? AND "+appIndex + "=?",
				new String[] { String.valueOf(groupIndex), String.valueOf(AppIndex) }, 
				null, null, null, null);

		if (cursor.moveToFirst()) 
		{
			do {
				result = true;
				// Adding contact to list
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact
		return result;
	}
	
	public boolean checkIsinGroup(int groupIndex, String packgName) 
	{
	boolean result = false;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,appIndex, IsInstalled,isSyncWithServer, appGroup, 
					   appName,appPackege,isAllowed,isAllowedNow,Timestamp}, 
					   appGroup + "=? AND "+appPackege + "=?",
				new String[] { String.valueOf(groupIndex), String.valueOf(packgName) }, 
				null, null, null, null);

		if (cursor.moveToFirst()) 
		{
			do {
				result = true;
				// Adding contact to list
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact
		return result;
	}
	public ArrayList<ApplicationInfoStruct>  getAllUnuploadedData(int SyncWithServer) 
	{
		ArrayList<ApplicationInfoStruct> listApplicationInfoStruct = new ArrayList<ApplicationInfoStruct>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,appIndex, IsInstalled,isSyncWithServer, appGroup, 
					   appName,appPackege,isAllowed,isAllowedNow,Timestamp}, 
					           isSyncWithServer + "=?",
				new String[] { String.valueOf(SyncWithServer) }, 
				null, null, null, null);

		if (cursor.moveToFirst()) 
		{
			do {
				ApplicationInfoStruct mApplicationInfoStruct = new ApplicationInfoStruct();
				mApplicationInfoStruct.setAppIndex(cursor.getInt(cursor.getColumnIndex(appIndex)));
				mApplicationInfoStruct.setIsInstalled(cursor.getInt(cursor.getColumnIndex(IsInstalled)));
				mApplicationInfoStruct.setIsSyncWithServer(cursor.getInt(cursor.getColumnIndex(isSyncWithServer)));
				mApplicationInfoStruct.setAppGroup(cursor.getInt(cursor.getColumnIndex(appGroup)));
				mApplicationInfoStruct.setAppName(cursor.getString(cursor.getColumnIndex(appName)));
				mApplicationInfoStruct.setAppPackege(cursor.getString(cursor.getColumnIndex(appPackege)));
				mApplicationInfoStruct.setIsAllowed(cursor.getInt(cursor.getColumnIndex(isAllowed)));
				mApplicationInfoStruct.setIsAllowedNow(cursor.getInt(cursor.getColumnIndex(isAllowedNow)));
				mApplicationInfoStruct.setTimestamp(cursor.getLong(cursor.getColumnIndex(Timestamp)));
			
				// Adding contact to list
				listApplicationInfoStruct.add(mApplicationInfoStruct);
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact
		return listApplicationInfoStruct;
	}
	public ArrayList<Integer>  getGroups() 
	{
		ArrayList<Integer> listGroups = new ArrayList<Integer>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,appIndex, IsInstalled,isSyncWithServer, appGroup, 
					   appName,appPackege,isAllowed,isAllowedNow,Timestamp}, 
					           isSyncWithServer + "=?",
				null, 
				null, null, appGroup, null);

		if (cursor.moveToFirst()) 
		{
			do {
				listGroups.add(cursor.getInt(cursor.getColumnIndex(appGroup)));
				
				// Adding contact to list
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact
		return listGroups;
	}
	
	// Getting All Contacts
	public ArrayList<ApplicationInfoStruct> getData() {
		ArrayList<ApplicationInfoStruct> listApplicationInfoStruct = new ArrayList<ApplicationInfoStruct>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_NAME;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				ApplicationInfoStruct mApplicationInfoStruct = new ApplicationInfoStruct();
				mApplicationInfoStruct.setAppIndex(cursor.getInt(cursor.getColumnIndex(appIndex)));
				mApplicationInfoStruct.setIsInstalled(cursor.getInt(cursor.getColumnIndex(IsInstalled)));
				mApplicationInfoStruct.setIsSyncWithServer(cursor.getInt(cursor.getColumnIndex(isSyncWithServer)));
				mApplicationInfoStruct.setAppGroup(cursor.getInt(cursor.getColumnIndex(appGroup)));
				mApplicationInfoStruct.setAppName(cursor.getString(cursor.getColumnIndex(appName)));
				mApplicationInfoStruct.setAppPackege(cursor.getString(cursor.getColumnIndex(appPackege)));
				mApplicationInfoStruct.setIsAllowed(cursor.getInt(cursor.getColumnIndex(isAllowed)));
				mApplicationInfoStruct.setIsAllowedNow(cursor.getInt(cursor.getColumnIndex(isAllowedNow)));
				mApplicationInfoStruct.setTimestamp(cursor.getLong(cursor.getColumnIndex(Timestamp)));

				// Adding contact to list
				listApplicationInfoStruct.add(mApplicationInfoStruct);
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact list
		return listApplicationInfoStruct;
	}

	// Updating single contact
	public int updateAllInstallationStatus(int isInstalled) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		//values.put(appIndex, contact.getName());
		values.put(isSyncWithServer, isInstalled);

		// updating row
		int update=db.update(TABLE_NAME, values, 
				null,
				null);
		//db.close();
		return update;
		
	}
	// Deleting single contact
		public void deleteData(Struct_SMS contact) 
		{
			SQLiteDatabase db = this.getWritableDatabase();
			db.delete(TABLE_NAME, KEY_ID + " = ?",
					new String[] { String.valueOf(contact.getID()) });
			//db.delete(TABLE_CONTACTS, null, null);
		//	db.close();
		}
	// Deleting single contact
	public void deleteDB() 
	{
		SQLiteDatabase db = this.getWritableDatabase();
	//	db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
	//			new String[] { String.valueOf(contact.getID()) });
		db.delete(TABLE_NAME, null, null);
		//db.close();
	}


	// Getting contacts Count
	public int getDataCount() {
		String countQuery = "SELECT  * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int  count=cursor.getCount();
		cursor.close();
		//db.close();
		
		return count;
		
	}
	   // Getting single contact
		public List<ApplicationInfoStruct>  getSMSFromDate(long Startdate,long Enddate) 
		{
			List<ApplicationInfoStruct> listApplicationInfoStruct = new ArrayList<ApplicationInfoStruct>();
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_NAME, 
					new String[] { KEY_ID,appIndex, IsInstalled,isSyncWithServer, appGroup, 
						   appName,appPackege,isAllowed,isAllowedNow}, 
					isSyncWithServer + ">="+Startdate+" AND "+isSyncWithServer + "<="+Enddate,
					null, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					ApplicationInfoStruct mApplicationInfoStruct = new ApplicationInfoStruct();
					mApplicationInfoStruct.setAppIndex(cursor.getInt(cursor.getColumnIndex(appIndex)));
					mApplicationInfoStruct.setIsInstalled(cursor.getInt(cursor.getColumnIndex(IsInstalled)));
					mApplicationInfoStruct.setIsSyncWithServer(cursor.getInt(cursor.getColumnIndex(isSyncWithServer)));
					mApplicationInfoStruct.setAppGroup(cursor.getInt(cursor.getColumnIndex(appGroup)));
					mApplicationInfoStruct.setAppName(cursor.getString(cursor.getColumnIndex(appName)));
					mApplicationInfoStruct.setAppPackege(cursor.getString(cursor.getColumnIndex(appPackege)));
					mApplicationInfoStruct.setIsAllowed(cursor.getInt(cursor.getColumnIndex(isAllowed)));
					mApplicationInfoStruct.setIsAllowedNow(cursor.getInt(cursor.getColumnIndex(isAllowedNow)));
					mApplicationInfoStruct.setTimestamp(cursor.getLong(cursor.getColumnIndex(Timestamp)));

					// Adding contact to list
					listApplicationInfoStruct.add(mApplicationInfoStruct);
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return listApplicationInfoStruct;
		}

	public void removeGroups(){
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("update "+TABLE_NAME+" set "+appGroup+"=''");
	}

		public int  updateGroup(int AppIndex, int GroupIndex) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			//values.put(KEY_NAME, contact.getName());

			values.put(appGroup,GroupIndex);


			// updating row
			int update=db.update(TABLE_NAME, values, 
					appIndex + "=?",
					new String[] {String.valueOf(AppIndex)});
			//db.close();//not sync 
			return update;

		}
		
		public int  updateAppInstallationStatus(String pkg, int status) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			//values.put(KEY_NAME, contact.getName());

			values.put(IsInstalled,status);
			values.put(isSyncWithServer,0);


			// updating row
			int update=db.update(TABLE_NAME, values, 
					appPackege + "=?",
					new String[] {String.valueOf(pkg)});
			//db.close();//not sync 
			return update;

		}
		
		public int  updateAppInstallationStatusWithGroup(String pkg, int status, int group) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			//values.put(KEY_NAME, contact.getName());

			values.put(IsInstalled,status);
			values.put(isSyncWithServer,0);
			values.put(appGroup,group);

			// updating row
			int update=db.update(TABLE_NAME, values, 
					appPackege + "=?",
					new String[] {String.valueOf(pkg)});
			//db.close();//not sync 
			return update;

		}
		public int updateAppSyncStatusByNameandIndex(String Name, int index) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			//values.put(KEY_NAME, contact.getName());

			
			values.put(isSyncWithServer,1);


			// updating row
			int update=db.update(TABLE_NAME, values, 
					appName + "=? AND "+appIndex+ "=?",
					new String[] {String.valueOf(Name),String.valueOf(index)});
			//db.close();//not sync 
			return update;

		}
		public int getMaxIndex() 
		{
			int result = -1;
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_NAME, 
					new String[] {appIndex 
						  }, 
						  null,
						  null, 
					null, null, appIndex+" DESC", null);
			
		
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getInt(cursor.getColumnIndex(appIndex));
					break;

					// Adding contact to list
				
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return result;
		}
		
		public int getAppIndex(String pkg) 
		{
			int result = -1;
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_NAME, 
					new String[] {appIndex 
						  }, 
						  appPackege+" =?",
						  new String[] {pkg}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getInt(cursor.getColumnIndex(appIndex));
				

					// Adding contact to list
				
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return result;
		}
		public String getAppNameByPkg(String pkg) 
		{
			String result = "";
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_NAME, 
					new String[] {appName 
						  }, 
						  appPackege+" =?",
						  new String[] {pkg}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getString(cursor.getColumnIndex(appName));
					// Adding contact to list
				
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return result;
		}
		public String getAppName(String index) 
		{
			String result = "";
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_NAME, 
					new String[] {appName 
						  }, 
						  appIndex+" =?",
						  new String[] {index}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getString(cursor.getColumnIndex(appName));
				

					// Adding contact to list
				
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return result;
		}
		
		public int getAppGroup(String pkg) 
		{
			int result = -1;
			SQLiteDatabase db = this.getReadableDatabase();

			Cursor cursor = db.query
				   (TABLE_NAME, 
					new String[] {appGroup
						  }, 
						  appPackege+" =?",
						  new String[] {pkg}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getInt(cursor.getColumnIndex(appGroup));
				

					// Adding contact to list
				
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return result;
		}
}
