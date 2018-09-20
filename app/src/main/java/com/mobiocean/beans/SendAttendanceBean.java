package com.mobiocean.beans;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.annotations.SerializedName;
import com.mobiocean.util.CallHelper;

/**
 * @author Narayanan
 */
public class SendAttendanceBean {
    @SerializedName("appId")
    public String appId;
    @SerializedName("Latitude")
    public String Latitude;
    @SerializedName("Longitude")
    public String Longitude;
    @SerializedName("LogDateTime")
    public String LogDateTime;
    @SerializedName("IsLogin")
    public String IsLogin;
    @SerializedName("CellId")
    public String CellId;
    @SerializedName("MCC")
    public String MCC;
    @SerializedName("MNC")
    public String MNC;
    @SerializedName("LAC")
    public String LAC;

    public SendAttendanceBean(Context context, int i) {
        final String PREFS_NAME = "MyPrefsFile";
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        appId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
        IsLogin = String.valueOf(i);
    }

}
