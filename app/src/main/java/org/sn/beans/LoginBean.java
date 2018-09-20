package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class LoginBean {
    @SerializedName("ClientCode")
    public String ClientCode="";
    @SerializedName("EmpCompanyId")
    public String EmpCompanyId="";
    @SerializedName("MobileNo")
    public String MobileNo="";

}
