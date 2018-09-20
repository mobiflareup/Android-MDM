package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class SecuredStorageApiModel {
    @SerializedName("FileId")
    public int FileId;
    @SerializedName("FilePath")
    public String FilePath;
    @SerializedName("FileName")
    public String FileName;
}
