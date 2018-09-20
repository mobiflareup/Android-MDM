package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class VpnStatusBean {
    @SerializedName("IpAddress")
    public String IpAddress;
    @SerializedName("IsEnabled")
    public boolean IsEnabled;

}
