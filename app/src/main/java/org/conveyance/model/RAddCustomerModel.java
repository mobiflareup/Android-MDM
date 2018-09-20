package org.conveyance.model;

import com.google.gson.annotations.SerializedName;

/**
 * @author Narayanan
 * @version V 0.0.5
 */

public class RAddCustomerModel {
    @SerializedName("appId")
    public String appId;
    @SerializedName("CustomerName")
    public String CustomerName;
    @SerializedName("MobileNo")
    public String MobileNo;
    @SerializedName("EmailId")
    public String EmailId;
    @SerializedName("ALtMobileNo")
    public String ALtMobileNo;
    @SerializedName("ContactPersion")
    public String ContactPersion;
    @SerializedName("AltContactPersion")
    public String AltContactPersion;
    @SerializedName("AltEmailId")
    public String AltEmailId;
    @SerializedName("Address")
    public String Address;
    @SerializedName("AltAddress")
    public String AltAddress;
    @SerializedName("Latitude")
    public String Latitude;
    @SerializedName("Longitude")
    public String Longitude;
    @SerializedName("City")
    public String City;
    @SerializedName("District")
    public String District;
    @SerializedName("state")
    public String state;
    @SerializedName("country")
    public String country;
    @SerializedName("PinCode")
    public String PinCode;
    @SerializedName("TinNumber")
    public String TinNumber;

    public RAddCustomerModel(String appId, String customerName, String mobileNo, String emailId, String ALtMobileNo, String contactPersion, String altContactPersion, String altEmailId, String address, String altAddress, String latitude, String longitude, String city, String district, String state, String country, String pinCode, String tinNumber) {
        this.appId = appId;
        CustomerName = customerName;
        MobileNo = mobileNo;
        EmailId = emailId;
        this.ALtMobileNo = ALtMobileNo;
        ContactPersion = contactPersion;
        AltContactPersion = altContactPersion;
        AltEmailId = altEmailId;
        Address = address;
        AltAddress = altAddress;
        Latitude = latitude;
        Longitude = longitude;
        City = city;
        District = district;
        this.state = state;
        this.country = country;
        PinCode = pinCode;
        TinNumber = tinNumber;
    }

}