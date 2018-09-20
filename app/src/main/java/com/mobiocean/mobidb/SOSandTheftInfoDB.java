package com.mobiocean.mobidb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SOSandTheftInfoDB extends SQLiteOpenHelper 
{

	public static SOSandTheftInfoDB mApplicationInfoDatabaseHandler;
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 2;

	// Database Name
	private static final String DATABASE_NAME = "SOSandTheftInfoDB";

	// Contacts table name
	private static final String TABLE_NAME = "SOSandTheftInfoTable";

	// Contacts Table Columns names
	private static final String KEY_ID = "_id";
	private static final String IsforSos = "IsforSos";
	private static final String IsAudio = "IsAudio";
	private static final String FileName = "FileName";
	private static final String FilePath = "FilePath";
	private static final String localFilePath = "localFilePath";
	private static final String AppId = "AppId";
	private static final String Latitude = "Latitude";
	private static final String Longitude= "Longitude";
	private static final String CellId="CellId";
	private static final String locationAreaCode="locationAreaCode";
	private static final String mobileCountryCode="mobileCountryCode";
	private static final String mobileNetworkCode="mobileNetworkCode";
	private static final String LogDateTime="LogDateTime";
	private static final String IsUploaded="IsUploaded";
	

	
	public SOSandTheftInfoDB(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static SOSandTheftInfoDB getInstance(Context context)
	{
		if(mApplicationInfoDatabaseHandler==null)
			mApplicationInfoDatabaseHandler = new SOSandTheftInfoDB(context);
		
		return mApplicationInfoDatabaseHandler;
	}
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_SMS_TABLE = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID  + " INTEGER PRIMARY KEY," 
				+ IsUploaded + " INTEGER,"
				+ IsforSos + " INTEGER,"//
				+ IsAudio + " INTEGER," 
				+ FileName+ " TEXT," 
				+ FilePath + " TEXT," 
				+ localFilePath + " TEXT,"
				+ AppId + " TEXT,"
				+ Latitude + " TEXT,"
     			+ Longitude + " TEXT,"
     			+ CellId + " TEXT,"
				+ locationAreaCode + " TEXT,"
				+ mobileCountryCode + " TEXT,"
     			+ mobileNetworkCode + " TEXT,"
     			+ LogDateTime + " INTEGER"
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
	public void addData(SOSandTheftInfoStruct mSOSandTheftInfoStruct) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		//Log.i("CallDB", struct_SMS.get_slon()+" lon storing send SMS lat.."+struct_SMS.get_slat());
		ContentValues values = new ContentValues();
		values.put(IsforSos, mSOSandTheftInfoStruct.getIsforSos()); // Struct_Contact Name
		values.put(IsAudio, mSOSandTheftInfoStruct.getIsAudio()); // Struct_Contact Phone
		values.put(AppId, mSOSandTheftInfoStruct.getAppId());
		values.put(Latitude, mSOSandTheftInfoStruct.getLatitude());
		values.put(FileName, mSOSandTheftInfoStruct.getFileName());
		values.put(FilePath, mSOSandTheftInfoStruct.getFilePath());
		values.put(localFilePath, mSOSandTheftInfoStruct.getLocalFilePath());
		values.put(Longitude, mSOSandTheftInfoStruct.getLongitude());
		values.put(CellId, mSOSandTheftInfoStruct.getCellId());	
		values.put(locationAreaCode, mSOSandTheftInfoStruct.getLocationAreaCode());
		values.put(mobileCountryCode, mSOSandTheftInfoStruct.getMobileCountryCode());
		values.put(mobileNetworkCode, mSOSandTheftInfoStruct.getMobileNetworkCode());
		values.put(LogDateTime, mSOSandTheftInfoStruct.getLogDateTime());
		values.put(IsUploaded, mSOSandTheftInfoStruct.getIsUploaded());

	
		// Inserting Row
		db.insert(TABLE_NAME, null, values);
		//db.close(); // Closing database connection
	}
	public ArrayList<SOSandTheftInfoStruct> getAllUnUploadedData()
	{
		ArrayList<SOSandTheftInfoStruct> listSOSandTheftInfoStruct = new ArrayList<SOSandTheftInfoStruct>();
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { IsforSos, IsAudio,FileName, FilePath, 
					   localFilePath,AppId,Latitude,Longitude,CellId,
					   locationAreaCode,mobileCountryCode,mobileNetworkCode,
					   LogDateTime,IsUploaded},null, null, null, null, null, null);

		if (cursor.moveToFirst()) 
		{
			do {
				SOSandTheftInfoStruct mSOSandTheftInfoStruct = new SOSandTheftInfoStruct();
				
				mSOSandTheftInfoStruct.setIsforSos(cursor.getInt(cursor.getColumnIndex(IsforSos)));
				mSOSandTheftInfoStruct.setIsAudio(cursor.getInt(cursor.getColumnIndex(IsAudio)));
				mSOSandTheftInfoStruct.setFileName(cursor.getString(cursor.getColumnIndex(FileName)));
				mSOSandTheftInfoStruct.setFilePath(cursor.getString(cursor.getColumnIndex(FilePath)));
				mSOSandTheftInfoStruct.setLocalFilePath(cursor.getString(cursor.getColumnIndex(localFilePath)));
				mSOSandTheftInfoStruct.setAppId(cursor.getString(cursor.getColumnIndex(AppId)));
				mSOSandTheftInfoStruct.setLatitude(cursor.getString(cursor.getColumnIndex(Latitude)));
				mSOSandTheftInfoStruct.setLongitude(cursor.getString(cursor.getColumnIndex(Longitude)));
				mSOSandTheftInfoStruct.setCellId(cursor.getString(cursor.getColumnIndex(CellId)));
			
				mSOSandTheftInfoStruct.setLocationAreaCode(cursor.getString(cursor.getColumnIndex(locationAreaCode)));
				mSOSandTheftInfoStruct.setMobileCountryCode(cursor.getString(cursor.getColumnIndex(mobileCountryCode)));
				mSOSandTheftInfoStruct.setMobileNetworkCode(cursor.getString(cursor.getColumnIndex(mobileNetworkCode)));
				mSOSandTheftInfoStruct.setLogDateTime(cursor.getString(cursor.getColumnIndex(LogDateTime)));
				mSOSandTheftInfoStruct.setIsUploaded(cursor.getInt(cursor.getColumnIndex(IsUploaded)));
				
				// Adding contact to list
				listSOSandTheftInfoStruct.add(mSOSandTheftInfoStruct);
			} while (cursor.moveToNext());
		}
		//db.close();
		// return contact
		return listSOSandTheftInfoStruct;
	}
	public int  updateUpdatationStatus(String date, int status) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		//values.put(KEY_NAME, contact.getName());

		values.put(IsUploaded,status);


		// updating row
		int update=db.update(TABLE_NAME, values, 
				LogDateTime + "=?",
				new String[] {String.valueOf(date)});
		//db.close();//not sync 
		return update;

	}
	
	public void deleteData(String  date) 
	{
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, LogDateTime + " = ?",
				new String[] { String.valueOf(date) });
		//db.delete(TABLE_CONTACTS, null, null);
	//	db.close();
	}
	// Getting single contact
	public ArrayList<String>  getDataByGroup(int groupIndex) 
	{
		ArrayList<String> listPack = new ArrayList<String>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,IsforSos, IsAudio,FileName, FilePath, 
					   localFilePath,AppId,Latitude,Longitude,CellId}, 
					   FilePath + "=?",
				new String[] { String.valueOf(groupIndex) }, 
				null, null, null, null);

		if (cursor.moveToFirst()) 
		{
			do {
					listPack.add(cursor.getString(cursor.getColumnIndex(AppId)));
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
				new String[] { KEY_ID,IsforSos, IsAudio,FileName, FilePath, 
					   localFilePath,AppId,Latitude,Longitude,CellId}, 
					   FilePath + "=? AND "+IsforSos + "=?",
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
				new String[] { KEY_ID,IsforSos, IsAudio,FileName, FilePath, 
					   localFilePath,AppId,Latitude,Longitude,CellId}, 
					   FilePath + "=? AND "+AppId + "=?",
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
	
	public ArrayList<Integer>  getGroups() 
	{
		ArrayList<Integer> listGroups = new ArrayList<Integer>();
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query
			   (TABLE_NAME, 
				new String[] { KEY_ID,IsforSos, IsAudio,FileName, FilePath, 
					   localFilePath,AppId,Latitude,Longitude,CellId}, 
					           FileName + "=?",
				null, 
				null, null, FilePath, null);

		if (cursor.moveToFirst()) 
		{
			do {
				listGroups.add(cursor.getInt(cursor.getColumnIndex(FilePath)));
				
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
				mApplicationInfoStruct.setAppIndex(cursor.getInt(cursor.getColumnIndex(IsforSos)));
				mApplicationInfoStruct.setIsInstalled(cursor.getInt(cursor.getColumnIndex(IsAudio)));
				mApplicationInfoStruct.setIsSyncWithServer(cursor.getInt(cursor.getColumnIndex(FileName)));
				mApplicationInfoStruct.setAppGroup(cursor.getInt(cursor.getColumnIndex(FilePath)));
				mApplicationInfoStruct.setAppName(cursor.getString(cursor.getColumnIndex(localFilePath)));
				mApplicationInfoStruct.setAppPackege(cursor.getString(cursor.getColumnIndex(AppId)));
				mApplicationInfoStruct.setIsAllowed(cursor.getInt(cursor.getColumnIndex(Latitude)));
				mApplicationInfoStruct.setIsAllowedNow(cursor.getInt(cursor.getColumnIndex(Longitude)));
				mApplicationInfoStruct.setTimestamp(cursor.getLong(cursor.getColumnIndex(CellId)));

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
		//values.put(IsforSos, contact.getName());
		values.put(FileName, isInstalled);

		// updating row
		int update=db.update(TABLE_NAME, values, 
				null,
				null);
		//db.close();
		return update;
		
	}
	// Deleting single contact

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
					new String[] { KEY_ID,IsforSos, IsAudio,FileName, FilePath, 
						   localFilePath,AppId,Latitude,Longitude}, 
					FileName + ">="+Startdate+" AND "+FileName + "<="+Enddate,
					null, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					ApplicationInfoStruct mApplicationInfoStruct = new ApplicationInfoStruct();
					mApplicationInfoStruct.setAppIndex(cursor.getInt(cursor.getColumnIndex(IsforSos)));
					mApplicationInfoStruct.setIsInstalled(cursor.getInt(cursor.getColumnIndex(IsAudio)));
					mApplicationInfoStruct.setIsSyncWithServer(cursor.getInt(cursor.getColumnIndex(FileName)));
					mApplicationInfoStruct.setAppGroup(cursor.getInt(cursor.getColumnIndex(FilePath)));
					mApplicationInfoStruct.setAppName(cursor.getString(cursor.getColumnIndex(localFilePath)));
					mApplicationInfoStruct.setAppPackege(cursor.getString(cursor.getColumnIndex(AppId)));
					mApplicationInfoStruct.setIsAllowed(cursor.getInt(cursor.getColumnIndex(Latitude)));
					mApplicationInfoStruct.setIsAllowedNow(cursor.getInt(cursor.getColumnIndex(Longitude)));
					mApplicationInfoStruct.setTimestamp(cursor.getLong(cursor.getColumnIndex(CellId)));

					// Adding contact to list
					listApplicationInfoStruct.add(mApplicationInfoStruct);
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return listApplicationInfoStruct;
		}

		
	
		
		public int  updateAppInstallationStatus(String pkg, int status) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			//values.put(KEY_NAME, contact.getName());

			values.put(IsAudio,status);
			values.put(FileName,0);


			// updating row
			int update=db.update(TABLE_NAME, values, 
					AppId + "=?",
					new String[] {String.valueOf(pkg)});
			//db.close();//not sync 
			return update;

		}
		public int updateAppSyncStatusByNameandIndex(String Name, int index) {
			SQLiteDatabase db = this.getWritableDatabase();

			ContentValues values = new ContentValues();
			//values.put(KEY_NAME, contact.getName());

			
			values.put(FileName,1);


			// updating row
			int update=db.update(TABLE_NAME, values, 
					localFilePath + "=? AND "+IsforSos+ "=?",
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
					new String[] {IsforSos 
						  }, 
						  null,
						  null, 
					null, null, IsforSos+" DESC", null);
			
		
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getInt(cursor.getColumnIndex(IsforSos));
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
					new String[] {IsforSos 
						  }, 
						  AppId+" =?",
						  new String[] {pkg}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getInt(cursor.getColumnIndex(IsforSos));
				

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
					new String[] {localFilePath 
						  }, 
						  AppId+" =?",
						  new String[] {pkg}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getString(cursor.getColumnIndex(localFilePath));
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
					new String[] {localFilePath 
						  }, 
						  IsforSos+" =?",
						  new String[] {index}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getString(cursor.getColumnIndex(localFilePath));
				

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
					new String[] {FilePath
						  }, 
						  AppId+" =?",
						  new String[] {pkg}, 
					null, null, null, null);
			if (cursor.moveToFirst()) 
			{
				do {
					result = cursor.getInt(cursor.getColumnIndex(FilePath));
				

					// Adding contact to list
				
				} while (cursor.moveToNext());
			}
			//db.close();
			// return contact
			return result;
		}
}
