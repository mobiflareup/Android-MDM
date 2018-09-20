package org.conveyance.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.mobiocean.util.DeBug;

import org.conveyance.configuration.RSharedData;
import org.conveyance.model.RControlModel;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class RStartStopLocation extends RTableData.StartStopLocation {

    private SQLiteDatabase database;
    private RDBHelper dbHelper;
    private Context context;
    private RSharedData sharedDatas;

    public RStartStopLocation(Context context) {
        this.context = context;
        dbHelper = new RDBHelper(context);
        sharedDatas = new RSharedData(context);
    }

    public void insertLocation(RControlModel controlModel) {
        long insertId = 0;
        Open();
        boolean canAddLoc = true;
        if (controlModel.getIsLogin().equals("2")) {
            String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + IS_LOGIN + "='" + controlModel.getIsLogin() + "' AND " + DATETIME + " = '" + controlModel.getLogDateTime() + "' AND "+PROVIDER+" = '"+controlModel.getProvider()+"'";
            Cursor cursor = database.rawQuery(countQuery, null);
            if (cursor.getCount() > 0)
                canAddLoc = false;
            cursor.close();
        }
//        if (canAddLoc)
        {
            ContentValues values = new ContentValues();
            values.put(IS_LOGIN, controlModel.getIsLogin());
            values.put(CUSTOMER_ID, controlModel.getCustomerId());
            values.put(MODE_OF_TRAVEL, controlModel.getModeOfTravel());
            values.put(FILE_PATH, controlModel.getFilePath());
            values.put(DATETIME, controlModel.getLogDateTime());
            values.put(REMARKS, controlModel.getRemark());
            values.put(LATITUDE, controlModel.getLatitude());
            values.put(LONGITUDE, controlModel.getLongitude());
            values.put(ACCURACY, controlModel.getAccuracy());
            values.put(ALTITUDE, controlModel.getAltitude());
            values.put(BEARING, controlModel.getBearing());
            values.put(ELAPSED_REAL_TIME_NANOS, controlModel.getElapsedRealtimeNanos());
            values.put(PROVIDER, controlModel.getProvider());
            values.put(SPEED, controlModel.getSpeed());
            values.put(TIME, controlModel.getTime());
            values.put(CELL_ID, controlModel.getCellId());
            values.put(MCC, controlModel.getMCC());
            values.put(MNC, controlModel.getMNC());
            values.put(LAC, controlModel.getLAC());
            insertId = database.insert(TABLE_NAME, null, values);
            DeBug.ShowLog("insertLocation: ", "Inserted Values : " + new Gson().toJson(controlModel) + " : " + insertId);
        }
        Close();
    }

    public ArrayList<RControlModel> getAllOffLocation() {
        Open();
        ArrayList<RControlModel> controlModelArrayList = new ArrayList<>();

        String countQuery = "SELECT  * FROM " + TABLE_NAME + " ORDER BY " + _ID;
        Cursor cursor = database.rawQuery(countQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RControlModel controlModel = new RControlModel();
                controlModel.setSno(cursor.getInt(cursor.getColumnIndex(_ID)));
                controlModel.setAppId(sharedDatas.getAppId());
                controlModel.setIsLogin(cursor.getString(cursor.getColumnIndex(IS_LOGIN)));
                controlModel.setCustomerId(cursor.getString(cursor.getColumnIndex(CUSTOMER_ID)));
                controlModel.setModeOfTravel(cursor.getString(cursor.getColumnIndex(MODE_OF_TRAVEL)));
                controlModel.setFilePath(cursor.getString(cursor.getColumnIndex(FILE_PATH)));
                controlModel.setLogDateTime(cursor.getString(cursor.getColumnIndex(DATETIME)));
                controlModel.setRemark(cursor.getString(cursor.getColumnIndex(REMARKS)));
                controlModel.setLatitude(cursor.getString(cursor.getColumnIndex(LATITUDE)));
                controlModel.setLongitude(cursor.getString(cursor.getColumnIndex(LONGITUDE)));
                controlModel.setAltitude(cursor.getString(cursor.getColumnIndex(ALTITUDE)));
                controlModel.setAccuracy(cursor.getString(cursor.getColumnIndex(ACCURACY)));
                controlModel.setBearing(cursor.getString(cursor.getColumnIndex(BEARING)));
                controlModel.setElapsedRealtimeNanos(cursor.getString(cursor.getColumnIndex(ELAPSED_REAL_TIME_NANOS)));
                controlModel.setProvider(cursor.getString(cursor.getColumnIndex(PROVIDER)));
                controlModel.setSpeed(cursor.getString(cursor.getColumnIndex(SPEED)));
                controlModel.setTime(cursor.getString(cursor.getColumnIndex(TIME)));
                controlModel.setCellId(cursor.getString(cursor.getColumnIndex(CELL_ID)));
                controlModel.setMCC(cursor.getString(cursor.getColumnIndex(MCC)));
                controlModel.setMNC(cursor.getString(cursor.getColumnIndex(MNC)));
                controlModel.setLAC(cursor.getString(cursor.getColumnIndex(LAC)));
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