package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class WebListBean {
    @SerializedName("ProfileId")
    public int ProfileId;
    @SerializedName("WebsiteUrl")
    public String WebsiteUrl;
    @SerializedName("CategoryId")
    public int CategoryId;
    @SerializedName("IsWhiteList")
    public int IsWhiteList;
    
}