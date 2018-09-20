package org.sn.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;

import org.sn.beans.ContactListBean;

import java.util.ArrayList;

/**
 * @author Narayanan
 */

public class ContactListTable extends TableData.ContactList {
    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;
    public SharedPreferences settings;
    protected static final String PREFS_NAME = "MyPrefsFile";

    public ContactListTable(Context context) {
        this.context = context;
        dbHelper = new DbHelper(context);
        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void addContacts(ArrayList<ContactListBean> contactLists) {
        if (contactLists != null) {
            Open();
            database.delete(TABLE_NAME, null, null);
            if (contactLists.size() > 0)
                for (ContactListBean bean : contactLists) {
                    ContentValues values = new ContentValues();
                    values.put(PROFILE_ID, bean.ProfileId);
                    values.put(CONTACT_NUMBER, bean.ContactNumber);
                    values.put(IS_INCOMING, bean.IsIncoming);
                    values.put(IS_OUTGOING, bean.IsOutGoing);
                    values.put(IS_SMS, bean.IsSms);
                    values.put(IS_WHITE_LIST, bean.IsWhiteList);
                    database.insert(TABLE_NAME, null, values);
                }
            Close();
        }
    }

    // Getting single contact
    public boolean checkNumber(String Number1, int Category1) {
        String profileId = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
        CallHelper.Ds.structFCC.IsWhiteList = settings.getInt("isCallBack", CallHelper.Ds.structFCC.IsWhiteList);
        Open();
        boolean isAllowed = false;
        Cursor cursor = null;
        switch (Category1) {
            case 0:
                cursor = database.query
                        (TABLE_NAME,
                                new String[]{_ID},
                                CONTACT_NUMBER + " LIKE ? AND " + IS_WHITE_LIST + "=? AND " + PROFILE_ID + "=? AND " + IS_OUTGOING + "=?",
                                new String[]{"%" + Number1 + "%", String.valueOf(CallHelper.Ds.structFCC.IsWhiteList), profileId, String.valueOf(1)},
                                null, null, null, null);
                break;
            case 1:
                cursor = database.query
                        (TABLE_NAME,
                                new String[]{_ID},
                                CONTACT_NUMBER + " LIKE ? AND " + IS_WHITE_LIST + "=? AND " + PROFILE_ID + "=? AND " + IS_INCOMING + "=?",
                                new String[]{"%" + Number1 + "%", String.valueOf(CallHelper.Ds.structFCC.IsWhiteList), profileId, String.valueOf(1)},
                                null, null, null, null);
                break;
            case 2:
                cursor = database.query
                        (TABLE_NAME,
                                new String[]{_ID},
                                CONTACT_NUMBER + " LIKE ? AND " + IS_WHITE_LIST + "=? AND " + PROFILE_ID + "=? AND " + IS_SMS + "=?",
                                new String[]{"%" + Number1 + "%", String.valueOf(CallHelper.Ds.structFCC.IsWhiteList), profileId, String.valueOf(1)},
                                null, null, null, null);
                break;
            default:
                cursor = database.query
                        (TABLE_NAME,
                                new String[]{_ID},
                                CONTACT_NUMBER + " LIKE ? AND " + IS_WHITE_LIST + "=? AND " + PROFILE_ID + "=?",
                                new String[]{"%" + Number1 + "%", String.valueOf(CallHelper.Ds.structFCC.IsWhiteList), profileId},
                                null, null, null, null);

        }

        if (cursor.moveToFirst()) {
            do {
                isAllowed = true;
                break;
            } while (cursor.moveToNext());
        }
        DeBug.ShowLog("ISCallAllowed", "isWhitelist = " + CallHelper.Ds.structFCC.IsWhiteList + " Status = " + Category1 + " Allowed " + isAllowed);
        Close();
        if (CallHelper.Ds.structFCC.IsWhiteList == 0) {
            return isAllowed;
        } else {
            return !isAllowed;
        }
    }

    // Getting single contact is allowed on outgoing call.
    public boolean canBlockOutgoingCall(String Number1) {
        String profileId = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
        CallHelper.Ds.structFCC.IsWhiteList = settings.getInt("isCallBack", CallHelper.Ds.structFCC.IsWhiteList);
        Open();
        boolean isAllowed = false;

        Cursor cursor = database.query (TABLE_NAME, new String[]{_ID}, CONTACT_NUMBER + " LIKE ? AND " + IS_WHITE_LIST + "=? AND " + PROFILE_ID + "=? AND " + IS_OUTGOING + "=?", new String[]{"%" + Number1 + "%", String.valueOf(CallHelper.Ds.structFCC.IsWhiteList), profileId, String.valueOf(1)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                isAllowed = true;
                break;
            } while (cursor.moveToNext());
        }

        DeBug.ShowLog("OutgoingCall", "In Db found the call is isWhitelist = " + CallHelper.Ds.structFCC.IsWhiteList + " Allowed " + isAllowed);

        Close();
        if (CallHelper.Ds.structFCC.IsWhiteList == 1) {
            return isAllowed;
        } else {
            return !isAllowed;
        }
    }

    // Getting single contact is allowed on incoming call.
    public boolean canBlockIncomingCall(String Number1) {
        String profileId = settings.getString("CallHelper.profileSensor", CallHelper.profileSensor);
        CallHelper.Ds.structFCC.IsWhiteList = settings.getInt("isCallBack", CallHelper.Ds.structFCC.IsWhiteList);
        Open();
        boolean isAllowed = false;

        Cursor cursor = database.query (TABLE_NAME, new String[]{_ID}, CONTACT_NUMBER + " LIKE ? AND " + IS_WHITE_LIST + "=? AND " + PROFILE_ID + "=? AND " + IS_INCOMING + "=?", new String[]{"%" + Number1 + "%", String.valueOf(CallHelper.Ds.structFCC.IsWhiteList), profileId, String.valueOf(1)}, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                isAllowed = true;
                break;
            } while (cursor.moveToNext());
        }

        DeBug.ShowLog("OutgoingCall", "test Db found the call is isWhitelist = " + CallHelper.Ds.structFCC.IsWhiteList + " Allowed " + isAllowed);

        Close();
        if (CallHelper.Ds.structFCC.IsWhiteList == 1) {
            return isAllowed;
        } else {
            return !isAllowed;
        }
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