package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sn.beans.AppGroupBean;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class AppGroupTable extends TableData.AppGroup {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public AppGroupTable(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
    }

    public void addGroups(ArrayList<AppGroupBean> appGroups) {
        if (appGroups != null) {
            Open();
            database.delete(TABLE_NAME, null, null);
            if (appGroups.size() > 0)
                for (AppGroupBean bean : appGroups) {
                    ContentValues values = new ContentValues();
                    values.put(PROFILE_ID, bean.ProfileId);
                    values.put(CODE, bean.Message);
                    values.put(GROUP_ID, bean.ChatGroupId);
                    values.put(IS_ENABLED, bean.IsEnable);
                    database.insert(TABLE_NAME, null, values);
                }
            Close();
        }
    }

    public ArrayList<AppGroupBean> getProfiles(int profileId) {
        ArrayList<AppGroupBean> result = null;
        Open();
        String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROFILE_ID + " = " + profileId;
        Cursor cursor = database.rawQuery(countQuery, null);
        if (cursor.moveToFirst()) {
            result = new ArrayList<>();
            do {
                AppGroupBean profile = new AppGroupBean();
                profile.Message = cursor.getString(cursor.getColumnIndex(CODE));
                profile.ProfileId = cursor.getInt(cursor.getColumnIndex(PROFILE_ID));
                profile.IsEnable = cursor.getInt(cursor.getColumnIndex(IS_ENABLED));
                profile.ChatGroupId = cursor.getInt(cursor.getColumnIndex(GROUP_ID));
                result.add(profile);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return result;
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