package org.conveyance.model;

import com.google.gson.annotations.SerializedName;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          22-09-16         Initial creation
 ****************************************************************************/

public class RExtraAllowanceModel {
    @SerializedName("appId")
    private String appId;
    @SerializedName("remark")
    private String remark;
    @SerializedName("filePath")
    private String filePath;
    @SerializedName("ClaimedAmt")
    private String ClaimedAmt;
    @SerializedName("LogDateTime")
    private String LogDateTime;
    @SerializedName("sno")
    private int sno;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getClaimedAmt() {
        return ClaimedAmt;
    }

    public void setClaimedAmt(String claimedAmt) {
        ClaimedAmt = claimedAmt;
    }

    public String getLogDateTime() {
        return LogDateTime;
    }

    public void setLogDateTime(String logDateTime) {
        LogDateTime = logDateTime;
    }

    public int getSno() {
        return sno;
    }

    public void setSno(int sno) {
        this.sno = sno;
    }

}
