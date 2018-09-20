package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class OtpBean {
    @SerializedName("OTP")
    public String OTP="";
    @SerializedName("ClientCode")
    public String ClientCode="";
    @SerializedName("MobileNo")
    public String MobileNo="";

}
