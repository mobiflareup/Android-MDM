package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class ProfileInfoBean {
    @SerializedName("Message")
    public String Message;
    @SerializedName("ProfileName")
    public String ProfileName;
    @SerializedName("ProfileCode")
    public String ProfileCode;
    @SerializedName("ProfileId")
    public int ProfileId;
    @SerializedName("FeatureId")
    public int FeatureId;
    @SerializedName("IsEnable")
    public int IsEnable;
    @SerializedName("IsBlackList")
    public int IsBlackList;

}