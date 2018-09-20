package org.conveyance.model;

import com.google.gson.annotations.SerializedName;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          21-09-16         Initial creation
 ****************************************************************************/

public class RControlModel {
    @SerializedName("sno")
    private int sno;
    @SerializedName("appId")
    private String appId;
    @SerializedName("Latitude")
    private String Latitude = "0";
    @SerializedName("Longitude")
    private String Longitude = "0";
    @SerializedName("Accuracy")
    private String Accuracy = "0";
    @SerializedName("Altitude")
    private String Altitude = "0";
    @SerializedName("Bearing")
    private String Bearing = "0";
    @SerializedName("ElapsedRealtimeNanos")
    private String ElapsedRealtimeNanos = "0";
    @SerializedName("Provider")
    private String Provider = "0";
    @SerializedName("Speed")
    private String Speed = "0";
    @SerializedName("Time")
    private String Time = "0";
    @SerializedName("CellId")
    private String CellId = "0";
    @SerializedName("MCC")
    private String MCC = "0";
    @SerializedName("MNC")
    private String MNC = "0";
    @SerializedName("LAC")
    private String LAC = "0";
    @SerializedName("LogDateTime")
    private String LogDateTime = "";
    @SerializedName("IsLogin")
    private String IsLogin;
    @SerializedName("CustomerId")
    private String CustomerId = "";
    @SerializedName("ModeOfTravel")
    private String ModeOfTravel = "";
    @SerializedName("visitId")
    private String visitId = "";
    @SerializedName("filePath")
    private String filePath = "";
    @SerializedName("remark")
    private String remark = "";

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getAccuracy() {
        return Accuracy;
    }

    public void setAccuracy(String accuracy) {
        Accuracy = accuracy;
    }

    public String getAltitude() {
        return Altitude;
    }

    public void setAltitude(String altitude) {
        Altitude = altitude;
    }

    public String getBearing() {
        return Bearing;
    }

    public void setBearing(String bearing) {
        Bearing = bearing;
    }

    public String getElapsedRealtimeNanos() {
        return ElapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(String elapsedRealtimeNanos) {
        ElapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public String getProvider() {
        return Provider;
    }

    public void setProvider(String provider) {
        Provider = provider;
    }

    public String getSpeed() {
        return Speed;
    }

    public void setSpeed(String speed) {
        Speed = speed;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getCellId() {
        return CellId;
    }

    public void setCellId(String cellId) {
        CellId = cellId;
    }

    public String getMCC() {
        return MCC;
    }

    public void setMCC(String MCC) {
        this.MCC = MCC;
    }

    public String getMNC() {
        return MNC;
    }

    public void setMNC(String MNC) {
        this.MNC = MNC;
    }

    public String getLAC() {
        return LAC;
    }

    public void setLAC(String LAC) {
        this.LAC = LAC;
    }

    public String getLogDateTime() {
        return LogDateTime;
    }

    public void setLogDateTime(String logDateTime) {
        LogDateTime = logDateTime;
    }

    public String getIsLogin() {
        return IsLogin;
    }

    public void setIsLogin(String isLogin) {
        IsLogin = isLogin;
    }

    public String getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(String customerId) {
        CustomerId = customerId;
    }

    public String getModeOfTravel() {
        return ModeOfTravel;
    }

    public void setModeOfTravel(String modeOfTravel) {
        ModeOfTravel = modeOfTravel;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}