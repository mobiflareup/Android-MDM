package org.sn.location;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan S
 * @since V 0.0.1
 */
public class LocationBean {
    @SerializedName("IsGPS")
    public boolean IsGPS = false;
    @SerializedName("Lat")
    public String Lat = "0.0";
    @SerializedName("Longt")
    public String Longt = "0.0";
    @SerializedName("Accuracy")
    public String Accuracy = "0.0";
    @SerializedName("Altitude")
    public String Altitude = "0.0";
    @SerializedName("Bearing")
    public String Bearing = "0.0";
    @SerializedName("ElapsedRealTimeNanos")
    public String ElapsedRealTimeNanos = "0.0";
    @SerializedName("Provider")
    public String Provider = "0";
    @SerializedName("Speed")
    public String Speed = "0";
    @SerializedName("Time")
    public String Time = String.valueOf(System.currentTimeMillis());
    @SerializedName("CellId")
    public String CellId = "";
    @SerializedName("LAC")
    public String LAC = "";
    @SerializedName("MCC")
    public String MCC = "";
    @SerializedName("MNC")
    public String MNC = "";

}