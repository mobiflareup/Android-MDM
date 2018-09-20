package org.conveyance.configuration;

import android.content.Context;
import android.content.SharedPreferences;

/*************************************************************************
 * CHANGE_HISTORY     MODIFIED_BY        DATE           REASON_FOR_CHANGE
 * Initial creation   Sivamurugu           22-09-2016.    Initial creation
 **************************************************************************/
public class RSharedData {

    private static final String PREF_NAME = "ConveyanceSharedFile";
    SharedPreferences sPref;
    SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;
    private Context context;

    //Constants
    private final String APP_ID = "Key1";
    private final String VISIT_ID = "Key2";
    private final String CUSTOMER_NAME = "Key3";
    private final String STARTED="Key4";

    public RSharedData(Context context) {
        this.context = context;
        sPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sPref.edit();
    }

    public void setVisitID(String visit_id) {
        editor.putString(VISIT_ID, visit_id);
        editor.commit();
    }

    public String getVisitId() {
        return sPref.getString(VISIT_ID, null);
    }

    public void setCustomerName(String customername) {
        editor.putString(CUSTOMER_NAME, customername);
        editor.commit();
    }

    public String getCustomerName() {
        return sPref.getString(CUSTOMER_NAME, null);
    }

    public void setAppId(String state) {
        editor.putString(APP_ID, state);
        editor.commit();
    }

    public String getAppId() {
        return sPref.getString(APP_ID, "0");
    }

    public void setStatus(boolean state) {
        editor.putBoolean(STARTED, state);
        editor.commit();
    }

    public boolean getStatus() {
        return sPref.getBoolean(STARTED, false);
    }

    /**
     * Clearing all data from Shared Preferences
     */
    public void clearData() {
        editor.clear();
        editor.commit();
    }

}