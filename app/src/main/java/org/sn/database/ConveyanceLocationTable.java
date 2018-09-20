package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.mobiocean.util.DeBug;

import org.sn.beans.ConveyanceBean;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class ConveyanceLocationTable extends TableData.ConveyanceLocation {

    private SQLiteDatabase database;
    private DbHelper dbHelper;

    public ConveyanceLocationTable(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void insertLocation(ConveyanceBean conveyanceBean) {
        long insertId = 0;
        Open();
        ContentValues values = new ContentValues();
        values.put(IS_LOGIN, conveyanceBean.IsLogin);
        values.put(FILE_PATH, conveyanceBean.ImagePath);
        values.put(DATETIME, conveyanceBean.LogDateTime);
        values.put(REMARKS, conveyanceBean.Remark);
        values.put(VEHICLE_READING, conveyanceBean.VehicleReading);
        values.put(LATITUDE, conveyanceBean.Latitude);
        values.put(LONGITUDE, conveyanceBean.Longitude);
        values.put(CELL_ID, conveyanceBean.CellId);
        values.put(MCC, conveyanceBean.MCC);
        values.put(MNC, conveyanceBean.MNC);
        values.put(LAC, conveyanceBean.LAC);
        insertId = database.insert(TABLE_NAME, null, values);
        DeBug.ShowLog("insertLocation: ", "Inserted Values : " + new Gson().toJson(conveyanceBean) + " : " + insertId);
        Close();
    }

    public ArrayList<ConveyanceBean> getAllLocation() {
        Open();
        ArrayList<ConveyanceBean> controlModelArrayList = new ArrayList<>();

        String countQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + _ID;
        Cursor cursor = database.rawQuery(countQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ConveyanceBean controlModel = new ConveyanceBean();
                controlModel._Id = cursor.getInt(cursor.getColumnIndex(_ID));
                controlModel.IsLogin = cursor.getString(cursor.getColumnIndex(IS_LOGIN));
                controlModel.ImagePath = cursor.getString(cursor.getColumnIndex(FILE_PATH));
                controlModel.LogDateTime = cursor.getString(cursor.getColumnIndex(DATETIME));
                controlModel.Remark = cursor.getString(cursor.getColumnIndex(REMARKS));
                controlModel.VehicleReading = cursor.getFloat(cursor.getColumnIndex(VEHICLE_READING));
                controlModel.Latitude = cursor.getString(cursor.getColumnIndex(LATITUDE));
                controlModel.Longitude = cursor.getString(cursor.getColumnIndex(LONGITUDE));
                controlModel.CellId = cursor.getString(cursor.getColumnIndex(CELL_ID));
                controlModel.MCC = cursor.getString(cursor.getColumnIndex(MCC));
                controlModel.MNC = cursor.getString(cursor.getColumnIndex(MNC));
                controlModel.LAC = cursor.getString(cursor.getColumnIndex(LAC));
                controlModelArrayList.add(controlModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return controlModelArrayList;
    }

    public void deleteValue(int sno) {
        int deletedRows = 0;
        Open();
        deletedRows = database.delete(TABLE_NAME, _ID + "=?", new String[]{String.valueOf(sno)});
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
