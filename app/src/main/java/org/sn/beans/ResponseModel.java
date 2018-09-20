package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 */

public class ResponseModel<T> {
    @SerializedName("Result")
    public int Result;
    @SerializedName("Message")
    public String Message;
    @SerializedName("data")
    public T data;
}
