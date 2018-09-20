package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sn.beans.WebListBean;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class WebListTable extends TableData.WebList {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public WebListTable(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
    }

    public void addGroups(ArrayList<WebListBean> webLists) {
        if (webLists != null) {
            Open();
            database.delete(TABLE_NAME, null, null);
            if (webLists.size() > 0)
                for (WebListBean bean : webLists) {
                    ContentValues values = new ContentValues();
                    values.put(PROFILE_ID, bean.ProfileId);
                    values.put(URL, bean.WebsiteUrl);
                    values.put(GROUP_ID, bean.CategoryId);
                    values.put(IS_WHITE_LIST, bean.IsWhiteList);
                    database.insert(TABLE_NAME, null, values);
                }
            Close();
        }
    }

    public boolean isUrlAllowed(String url, int catID, int profileId) {
        boolean isAllowed = true;
        if (url != null) {
            Open();
            String countQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE " + PROFILE_ID + " = " + profileId + " AND " + GROUP_ID + " = " + catID;
            Cursor cursor = database.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    String temp = cursor.getString(cursor.getColumnIndex(URL)).trim();
                    url = url.trim();
                    if (temp.contains(url) || url.contains(temp)) {
                        isAllowed = false;
                    }
                } while (cursor.moveToNext());
            }
            Close();
        }
        return isAllowed;
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