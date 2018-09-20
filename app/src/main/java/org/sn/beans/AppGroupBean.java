package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class AppGroupBean {
    @SerializedName("ProfileId")
    public int ProfileId;
    @SerializedName("IsEnable")
    public int IsEnable;
    @SerializedName("Message")
    public String  Message;
    @SerializedName("ChatGroupId")
    public int  ChatGroupId;

}