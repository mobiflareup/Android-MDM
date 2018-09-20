package org.conveyance.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobiocean.util.DeBug;

import org.conveyance.model.RExtraAllowanceModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          23-09-16         Initial creation
 ****************************************************************************/

public class RExtraAllowanceTable extends RTableData.ExtraAllowance {
    private SQLiteDatabase database;
    private RDBHelper dbHelper;
    private Context context;

    public RExtraAllowanceTable(Context context) {
        this.context = context;
        dbHelper = new RDBHelper(context);
    }

    public void insertExtraAllowance(RExtraAllowanceModel extraAllowanceModel) {
        long insertId = 0;
        Open();
        ContentValues values = new ContentValues();
        values.put(REMARKS, extraAllowanceModel.getRemark());
        values.put(APPID, extraAllowanceModel.getAppId());
        values.put(AMOUNT, extraAllowanceModel.getClaimedAmt());
        values.put(PROOF, extraAllowanceModel.getFilePath());
        values.put(DATETIME, extraAllowanceModel.getLogDateTime());
        values.put(INSERTDATE, getCurrentTimeStamp());
        values.put(ISSYNC, 0);
        insertId = database.insert(TABLE_NAME, null, values);
        DeBug.ShowLog("Templates Inserted : ", extraAllowanceModel.getRemark() + " : " + insertId);
        Close();
    }


    public ArrayList<RExtraAllowanceModel> getExtraAllowance() {
        Open();
        ArrayList<RExtraAllowanceModel> extraAllowanceModels = new ArrayList<>();
        int count = 0;
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                count = count + 1;
                RExtraAllowanceModel allowanceModel = new RExtraAllowanceModel();
                allowanceModel.setSno(count);
                allowanceModel.setAppId(cursor.getString(cursor.getColumnIndex(APPID)));
                allowanceModel.setRemark(cursor.getString(cursor.getColumnIndex(REMARKS)));
                allowanceModel.setClaimedAmt(cursor.getString(cursor.getColumnIndex(AMOUNT)));
                allowanceModel.setFilePath(cursor.getString(cursor.getColumnIndex(PROOF)));
                allowanceModel.setLogDateTime(cursor.getString(cursor.getColumnIndex(DATETIME)));
                extraAllowanceModels.add(allowanceModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return extraAllowanceModels;
    }

    public ArrayList<RExtraAllowanceModel> getSyncExtraAllowance() {
        Open();
        ArrayList<RExtraAllowanceModel> extraAllowanceModels = new ArrayList<>();
        String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + ISSYNC + " =" + 0 + " ORDER BY " + _ID;
        Cursor cursor = database.rawQuery(countQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RExtraAllowanceModel allowanceModel = new RExtraAllowanceModel();
                allowanceModel.setSno(cursor.getInt(cursor.getColumnIndex(_ID)));
                allowanceModel.setAppId(cursor.getString(cursor.getColumnIndex(APPID)));
                allowanceModel.setRemark(cursor.getString(cursor.getColumnIndex(REMARKS)));
                allowanceModel.setClaimedAmt(cursor.getString(cursor.getColumnIndex(AMOUNT)));
                allowanceModel.setFilePath(cursor.getString(cursor.getColumnIndex(PROOF)));
                allowanceModel.setLogDateTime(cursor.getString(cursor.getColumnIndex(DATETIME)));
                extraAllowanceModels.add(allowanceModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return extraAllowanceModels;
    }

    public void extraAllowanceUpdate(int sync, int sno) {
        Open();
        ContentValues values = new ContentValues();
        values.put(ISSYNC, sync);
        int id = database.update(TABLE_NAME, values, _ID + " = " + sno, null);
        Close();
    }

    public int updateSync() {
        int updateId = 0;
        Open();
        try {
            ContentValues values = new ContentValues();
            values.put(ISSYNC, 1);
            updateId = database.update(TABLE_NAME, values, ISSYNC + " = " + 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Close();
        return updateId;
    }

    public int deleteOldAllowance() {
        Open();
        int rangeInDays = -2;
        Calendar range = Calendar.getInstance();
        range.add(Calendar.DAY_OF_MONTH, rangeInDays);
        int numDeleted = database.delete(TABLE_NAME, "insertdate" + " != '" + getCurrentTimeStamp() + "'", null);
        Close();
        return numDeleted;
    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        return sdfDate.format(now);
    }

    public void Delete_Table() {
        int deletedRows = 0;
        Open();
        try {
            deletedRows = database.delete(TABLE_NAME, ISSYNC + " = " + 1, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Close();
    }

    /***
     * Open Database
     */
    private void Open() {
        database = dbHelper.getWritableDatabase();
    }

    /***
     * Close Database
     */
    private void Close() {
        dbHelper.close();
    }

}
