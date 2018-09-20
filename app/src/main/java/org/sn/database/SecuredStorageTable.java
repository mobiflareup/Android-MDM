package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.sn.beans.SecuredStorageModel;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class SecuredStorageTable extends TableData.SecuredStorageTableVar {

    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    public SecuredStorageTable(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
    }

    public long insert(SecuredStorageModel model) {
        long result = 0;
        try {
            Open();
            if (model != null) {
                result = check(model);
                ContentValues values = new ContentValues();
                values.put(DOWNLOAD_URL, model.DownloadUrl);
                values.put(STORAGE_PATH, model.StoragePath);
                values.put(FILE_NAME, model.FileName);
                values.put(IS_DOWNLOADED, model.IsDownloaded);
                if (result > 0) {
                    result = database.update(TABLE_NAME, values, _ID + " = " + result, null);
                } else {
                    result = database.insert(TABLE_NAME, null, values);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Close();
        return result;
    }

    private long check(SecuredStorageModel model) {
        long result = 0;
        try {
            if (model != null) {
                String countQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + DOWNLOAD_URL + " = '" + model.DownloadUrl + "'";
                Cursor cursor = database.rawQuery(countQuery, null);
                if (cursor.moveToFirst()) {
                    result = cursor.getLong(cursor.getColumnIndex(_ID));
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public long delete(SecuredStorageModel model) {
        long result = 0;
        Open();
        try {
            if (model != null) {
                result = check(model);
                if (result > 0)
                    result = database.delete(TABLE_NAME, _ID + "=?", new String[]{String.valueOf(result)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Close();
        return result;
    }

    public ArrayList<SecuredStorageModel> get(boolean isDownloaded) {
        ArrayList<SecuredStorageModel> result = null;
        Open();
        try {
            String countQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + IS_DOWNLOADED + (isDownloaded?" > ":" =") +" 0 ";
            Cursor cursor = database.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                result = new ArrayList<>();
                do {
                    SecuredStorageModel model = new SecuredStorageModel();
                    model._id = cursor.getLong(cursor.getColumnIndex(_ID));
                    model.DownloadUrl = cursor.getString(cursor.getColumnIndex(DOWNLOAD_URL));
                    model.StoragePath = cursor.getString(cursor.getColumnIndex(STORAGE_PATH));
                    model.FileName = cursor.getString(cursor.getColumnIndex(FILE_NAME));
                    model.IsDownloaded = cursor.getInt(cursor.getColumnIndex(IS_DOWNLOADED)) > 0;
                    result.add(model);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Close();
        return result;
    }

    public void deleteAll() {
        Open();
        try {
            database.delete(TABLE_NAME, null, null);
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