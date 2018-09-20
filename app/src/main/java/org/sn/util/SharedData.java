package org.sn.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author Narayanan
 */

public class SharedData {

    private static final String PREF_NAME = "SundarShared";
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    private Context context;

    //Constants
    private final String CHECK_SET_NETWORK_STATUS = "Key1";
    private final String CHECK_NETWORK_STATUS = "Key2";

    public SharedData(Context context) {
        this.context = context;
        sPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sPref.edit();
    }

    public void setSetCheckNetworkStatus(boolean canLock) {
        editor.putBoolean(CHECK_SET_NETWORK_STATUS, canLock);
        editor.commit();
    }

    public boolean canSetCheckNetworkStatus() {
        return sPref.getBoolean(CHECK_SET_NETWORK_STATUS, false);
    }

    public void setCheckNetworkStatus(boolean canLock) {
        editor.putBoolean(CHECK_NETWORK_STATUS, canLock);
        editor.commit();
    }

    public boolean canCheckNetworkStatus() {
        return sPref.getBoolean(CHECK_NETWORK_STATUS, false);
    }

    /**
     * Clearing all data from Shared Preferences
     */
    public void clearData() {
        editor.clear();
        editor.commit();
    }

}
