package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

public class UpdateFromServer {

    @SerializedName("UpdateResult")
    private String UpdateResult;

    @SerializedName("AppId")
    private String AppId;

    @SerializedName("AckDateTime")
    private String AckDateTime;

    @SerializedName("SyncFeatureId")
    private String SyncFeatureId;

    public String getUpdateResult() {
        return UpdateResult;
    }

    public int getUpdateResultInt() {
        try {
            return Integer.parseInt(UpdateResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setUpdateResult(String updateResult) {
        UpdateResult = updateResult;
    }

    public String getAppId() {
        return AppId;
    }

    public void setAppId(String appId) {
        AppId = appId;
    }

    public String getAckDateTime() {
        return AckDateTime;
    }

    public void setAckDateTime(String ackDateTime) {
        AckDateTime = ackDateTime;
    }

    public String getSyncFeatureId() {
        return SyncFeatureId;
    }

    public void setSyncFeatureId(String syncFeatureId) {
        SyncFeatureId = syncFeatureId;
    }


}
