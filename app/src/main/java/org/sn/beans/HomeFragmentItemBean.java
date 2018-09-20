package org.sn.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 * @version V 0.0.5
 */

public class HomeFragmentItemBean {
    @SerializedName("text")
    public String text;
    @SerializedName("img")
    public
    @android.support.annotation.DrawableRes
    int img;

    public HomeFragmentItemBean(String text, @android.support.annotation.DrawableRes int img) {
        this.text = text;
        this.img = img;
    }
}