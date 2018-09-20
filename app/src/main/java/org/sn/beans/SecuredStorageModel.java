package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class SecuredStorageModel {
    @SerializedName("_id")
    public long _id;
    @SerializedName("DownloadUrl")
    public String DownloadUrl;
    @SerializedName("StoragePath")
    public String StoragePath;
    @SerializedName("FileName")
    public String FileName;
    @SerializedName("IsDownloaded")
    public boolean IsDownloaded;
}
