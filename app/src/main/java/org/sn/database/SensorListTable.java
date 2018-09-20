package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sn.beans.SensorListBean;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class SensorListTable extends TableData.SensorList {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public SensorListTable(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
    }

    public void addSensors(ArrayList<SensorListBean> sensorLists) {
        if (sensorLists != null) {
            Open();
            database.delete(TABLE_NAME, null, null);
            for (SensorListBean bean : sensorLists) {
                ContentValues values = new ContentValues();
                values.put(PROFILE_ID, bean.ProfileId);
                values.put(BSSID, bean.BSSID);
                values.put(SSID, bean.SSID);
                values.put(PASSWORD, bean.Password);
                database.insert(TABLE_NAME, null, values);
            }
            Close();
        }
    }

    public ArrayList<SensorListBean> getSensors() {
        ArrayList<SensorListBean> result = null;
        try {
            Open();
            String countQuery = "SELECT  * FROM " + TABLE_NAME;
            Cursor cursor = database.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                result = new ArrayList<>();
                do {
                    SensorListBean sensors = new SensorListBean();
                    sensors.ProfileId = cursor.getInt(cursor.getColumnIndex(PROFILE_ID));
                    sensors.BSSID = cursor.getString(cursor.getColumnIndex(BSSID));
                    sensors.SSID = cursor.getString(cursor.getColumnIndex(SSID));
                    sensors.Password = cursor.getString(cursor.getColumnIndex(PASSWORD));
                    result.add(sensors);
                } while (cursor.moveToNext());
            }
            cursor.close();
            Close();
        }catch (Exception ignore){}
        return result;
    }

    /***
     * Open Database
     */
    private void Open() {
        try {
            database = dbHelper.getWritableDatabase();
        }catch (Exception ignore){}
    }

    /***
     * Close Database
     */
    private void Close() {
        dbHelper.close();
    }

}