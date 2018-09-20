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

public class MobiProfileDetailTable extends TableData.MobiProfileDetails {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public MobiProfileDetailTable(Context context) {
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
                    values.put(CODE, bean.Message);
                    values.put(PROFILE_ID, bean.ProfileId);
                    values.put(FEATURE_ID, bean.FeatureId);
                    values.put(IS_ENABLED, bean.IsEnable);
                    values.put(IS_WHITE_LIST, bean.IsBlackList);
                    database.insert(TABLE_NAME, null, values);
                }
            Close();
        }
    }

    public ArrayList<ProfileInfoBean> getProfiles(int profileId) {
        ArrayList<ProfileInfoBean> result = null;
        Open();
        String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROFILE_ID + " = " + profileId;
        Cursor cursor = database.rawQuery(countQuery, null);
        if (cursor.moveToFirst()) {
            result = new ArrayList<>();
            do {
                ProfileInfoBean profile = new ProfileInfoBean();
                profile.Message = cursor.getString(cursor.getColumnIndex(CODE));
                profile.ProfileId = cursor.getInt(cursor.getColumnIndex(PROFILE_ID));
                profile.FeatureId = cursor.getInt(cursor.getColumnIndex(FEATURE_ID));
                profile.IsEnable = cursor.getInt(cursor.getColumnIndex(IS_ENABLED));
                profile.IsBlackList = cursor.getInt(cursor.getColumnIndex(IS_WHITE_LIST));
                result.add(profile);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return result;
    }

    public ProfileInfoBean getProfile(int profileId, int featureId) {
        ProfileInfoBean profile = null;
        Open();
        String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + FEATURE_ID + " = " + featureId + " AND " + PROFILE_ID + " = " + profileId + " AND " + IS_ENABLED + " =1 ";
        Cursor cursor = database.rawQuery(countQuery, null);
        if (cursor.moveToFirst()) {
            profile = new ProfileInfoBean();
            profile.Message = cursor.getString(cursor.getColumnIndex(CODE));
            profile.ProfileId = cursor.getInt(cursor.getColumnIndex(PROFILE_ID));
            profile.FeatureId = cursor.getInt(cursor.getColumnIndex(FEATURE_ID));
            profile.IsEnable = cursor.getInt(cursor.getColumnIndex(IS_ENABLED));
            profile.IsBlackList = cursor.getInt(cursor.getColumnIndex(IS_WHITE_LIST));
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