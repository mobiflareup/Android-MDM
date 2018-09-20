package org.sn.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Narayanan
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "MobiDatabase";
    private static final int DB_VERSION = 3;
    private static final String DROP = "DROP TABLE IF EXISTS ";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TableData.MobiProfileDetails.CREATE_TABLE);
        db.execSQL(TableData.MobiProfileInfo.CREATE_TABLE);
        db.execSQL(TableData.AppGroup.CREATE_TABLE);
        db.execSQL(TableData.WebList.CREATE_TABLE);
        db.execSQL(TableData.ContactList.CREATE_TABLE);
        db.execSQL(TableData.SensorList.CREATE_TABLE);
        db.execSQL(TableData.ConveyanceLocation.CREATE_TABLE);
        db.execSQL(TableData.SecuredStorageTableVar.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP + TableData.MobiProfileDetails.TABLE_NAME);
        db.execSQL(DROP + TableData.MobiProfileInfo.TABLE_NAME);
        db.execSQL(DROP + TableData.AppGroup.TABLE_NAME);
        db.execSQL(DROP + TableData.WebList.TABLE_NAME);
        db.execSQL(DROP + TableData.ContactList.TABLE_NAME);
        db.execSQL(DROP + TableData.SensorList.TABLE_NAME);
        db.execSQL(DROP + TableData.ConveyanceLocation.TABLE_NAME);
        db.execSQL(DROP + TableData.SecuredStorageTableVar.TABLE_NAME);
        onCreate(db);
    }

}