package com.mobiocean.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 * @version V 0.0.1
 */
public class SendConveyanceImageBean {
    @SerializedName("appId")
    public String appId;
    @SerializedName("Remark")
    public String Remark;
    @SerializedName("ImagePath")
    public String ImagePath;
    @SerializedName("ConveyanceId")
    public int ConveyanceId;

}
