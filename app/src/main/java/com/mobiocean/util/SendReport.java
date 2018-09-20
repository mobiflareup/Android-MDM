package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan S
 */
public class SendReport {
    @SerializedName("appId")
    private String appId = "";
    @SerializedName("DefectName")
    private String DefectName = "";
    @SerializedName("DefectDesc")
    private String DefectDesc = "";
    @SerializedName("DocPath")
    private String DocPath = "";

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getDefectName() {
        return DefectName;
    }

    public void setDefectName(String defectName) {
        DefectName = defectName;
    }

    public String getDefectDesc() {
        return DefectDesc;
    }

    public void setDefectDesc(String defectDesc) {
        DefectDesc = defectDesc;
    }

    public String getDocPath() {
        return DocPath;
    }

    public void setDocPath(String docPath) {
        DocPath = docPath;
    }
}
