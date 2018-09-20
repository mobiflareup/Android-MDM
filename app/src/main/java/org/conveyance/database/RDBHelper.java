package org.conveyance.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Narayanan
 */

public class RDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "RsmConveyance";
    private static final int DB_VERSION = 2;
    private static final String DROP = "DROP TABLE IF EXISTS ";

    public RDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RTableData.ExtraAllowance.CREATE_TABLE);
        db.execSQL(RTableData.StartStopLocation.CREATE_TABLE);
        db.execSQL(RTableData.ModeOfTravelTable.CREATE_TABLE);
        db.execSQL(RTableData.CustomerListTable.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP + RTableData.ExtraAllowance.TABLE_NAME);
        db.execSQL(DROP + RTableData.StartStopLocation.TABLE_NAME);
        db.execSQL(DROP + RTableData.ModeOfTravelTable.TABLE_NAME);
        db.execSQL(DROP + RTableData.CustomerListTable.TABLE_NAME);
        onCreate(db);
    }
}
