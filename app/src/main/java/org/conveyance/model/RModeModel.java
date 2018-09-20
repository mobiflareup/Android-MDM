package org.conveyance.model;

import com.google.gson.annotations.SerializedName;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          21-09-16         Initial creation
 ****************************************************************************/

public class RModeModel {
    @SerializedName("ModeId")
    private int ModeId;
    @SerializedName("ModeOfTravel")
    private String ModeOfTravel;

    public int getModeId() {
        return ModeId;
    }

    public void setModeId(int modeId) {
        ModeId = modeId;
    }

    public String getModeOfTravel() {
        return ModeOfTravel;
    }

    public void setModeOfTravel(String modeOfTravel) {
        ModeOfTravel = modeOfTravel;
    }
}
