package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class EnabledExtraFeatureBean {
    @SerializedName("IsAttendance")
    public boolean IsAttendance;
    @SerializedName("IsTravelAllowance")
    public boolean IsTravelAllowance;
    @SerializedName("IsConveyance")
    public boolean IsConveyance;
    @SerializedName("IsSecureStorage")
    public boolean IsSecureStorage;
}
