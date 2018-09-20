package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class SensorListBean {
    @SerializedName("ProfileId")
    public int ProfileId;
    @SerializedName("BSSID")
    public String BSSID;
    @SerializedName("SSID")
    public String SSID;
    @SerializedName("Password")
    public String Password;
    
}