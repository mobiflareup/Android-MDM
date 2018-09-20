package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class ConveyanceBean {
    @SerializedName("_Id")
    public int _Id;
    @SerializedName("IsLogin")
    public String IsLogin;
    @SerializedName("Remark")
    public String Remark;
    @SerializedName("VehicleReading")
    public float VehicleReading;
    @SerializedName("ImagePath")
    public String ImagePath;
    @SerializedName("LogDateTime")
    public String LogDateTime;
    @SerializedName("Latitude")
    public String Latitude;
    @SerializedName("Longitude")
    public String Longitude;
    @SerializedName("CellId")
    public String CellId;
    @SerializedName("MCC")
    public String MCC;
    @SerializedName("MNC")
    public String MNC;
    @SerializedName("LAC")
    public String LAC;

    public ConveyanceBean(){

    }

    public ConveyanceBean(int i){
        IsLogin = Integer.toString(i);
    }
}
