package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class ContactListBean {
    @SerializedName("ProfileId")
    public int ProfileId;
    @SerializedName("ContactNumber")
    public String ContactNumber;
    @SerializedName("IsIncoming")
    public int IsIncoming;
    @SerializedName("IsOutGoing")
    public int IsOutGoing;
    @SerializedName("IsSms")
    public int IsSms;
    @SerializedName("IsWhiteList")
    public int IsWhiteList;

}