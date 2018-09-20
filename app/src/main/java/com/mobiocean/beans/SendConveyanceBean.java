package com.mobiocean.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 * @version V 0.0.1
 */
public class SendConveyanceBean {
    @SerializedName("appId")
    public String appId;
    @SerializedName("IsLogin")
    public String IsLogin;
    @SerializedName("ConveyanceId")
    public int ConveyanceId;
    @SerializedName("Remark")
    public String Remark;
    @SerializedName("ImagePath")
    public String ImagePath;
    @SerializedName("VehicleReading")
    public float VehicleReading;
    @SerializedName("Latitude")
    public String Latitude;
    @SerializedName("Longitude")
    public String Longitude;
    @SerializedName("LogDateTime")
    public String LogDateTime;
    @SerializedName("CellId")
    public String CellId;
    @SerializedName("MCC")
    public String MCC;
    @SerializedName("MNC")
    public String MNC;
    @SerializedName("LAC")
    public String LAC;

}
