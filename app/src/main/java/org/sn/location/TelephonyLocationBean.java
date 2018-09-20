package org.sn.location;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan S
 * @since V 0.0.1
 */
public class TelephonyLocationBean {
    @SerializedName("CellId")
    public String CellId = "0";
    @SerializedName("LAC")
    public String LAC = "0";
    @SerializedName("MCC")
    public String MCC = "0";
    @SerializedName("MNC")
    public String MNC = "0";

}
