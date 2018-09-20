package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sn.beans.ProfileInfoBean;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class MobiProfileInfoTable extends TableData.MobiProfileInfo {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public MobiProfileInfoTable(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
    }

    public void addProfiles(ArrayList<ProfileInfoBean> profiles) {
        if (profiles != null) {
            Open();
            database.delete(TABLE_NAME, null, null);
            if (profiles.size() > 0)
                for (ProfileInfoBean bean : profiles) {
                    ContentValues values = new ContentValues();
                    values.put(PROFILE_NAME, bean.ProfileName);
                    values.put(PROFILE_ID, bean.ProfileId);
                    values.put(PROFILE_CODE, bean.ProfileCode);
                    database.insert(TABLE_NAME, null, values);
                }
            Close();
        }
    }

    public ProfileInfoBean getProfile(int profileId) {
        ProfileInfoBean profile = null;
        Open();
        String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROFILE_ID + " = " + profileId;
        Cursor cursor = database.rawQuery(countQuery, null);
        if (cursor.moveToFirst()) {
            profile = new ProfileInfoBean();
            profile.ProfileName = cursor.getString(cursor.getColumnIndex(PROFILE_NAME));
            profile.ProfileId = cursor.getInt(cursor.getColumnIndex(PROFILE_ID));
            profile.ProfileCode = cursor.getString(cursor.getColumnIndex(PROFILE_CODE));
        }
        cursor.close();
        Close();
        return profile;
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