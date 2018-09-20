package org.conveyance.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.conveyance.model.RModeModel;

import java.util.ArrayList;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          14-11-16         Initial creation
 ****************************************************************************/

public class RModeTravelTable extends RTableData.ModeOfTravelTable {

    private SQLiteDatabase database;
    private RDBHelper dbHelper;
    private Context context;

    public RModeTravelTable(Context context) {
        this.context = context;
        dbHelper = new RDBHelper(context);
    }

    public void insertMode(ArrayList<RModeModel> modeModels) {
        long insertId = 0;
        Open();
        for (RModeModel modeModel : modeModels) {
            ContentValues values = new ContentValues();
            values.put(modeoftravelid, modeModel.getModeId());
            values.put(modename, modeModel.getModeOfTravel());
            insertId = database.insert(TABLE_NAME, null, values);
        }
        Close();
    }

    public ArrayList<RModeModel> getModes() {
        Open();
        ArrayList<RModeModel> modeModels = new ArrayList<>();

        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        Cursor cursor = database.rawQuery(countQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                RModeModel modeModel = new RModeModel();
                modeModel.setModeId(cursor.getInt(cursor.getColumnIndex(modeoftravelid)));
                modeModel.setModeOfTravel(cursor.getString(cursor.getColumnIndex(modename)));
                modeModels.add(modeModel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        Close();
        return modeModels;
    }

    public void Delete_Table() {
        int deletedRows = 0;
        Open();
        deletedRows = database.delete(TABLE_NAME, null, null);
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
