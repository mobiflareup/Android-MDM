package org.conveyance.model;

import com.google.gson.annotations.SerializedName;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          21-09-16         Initial creation
 ****************************************************************************/

public class RCustomerModel {
    @SerializedName("CustomerId")
    private int CustomerId;
    @SerializedName("UserId")
    private int UserId;
    @SerializedName("ClientId")
    private int ClientId;
    @SerializedName("CustomerName")
    private String CustomerName;
    @SerializedName("MobileNo")
    private String MobileNo;
    @SerializedName("ALtMobileNo")
    private String ALtMobileNo;
    @SerializedName("ContactPersion")
    private String ContactPersion;
    @SerializedName("AltContactPersion")
    private String AltContactPersion;
    @SerializedName("EmailId")
    private String EmailId;
    @SerializedName("AltEmailId")
    private String AltEmailId;
    @SerializedName("Address")
    private String Address;
    @SerializedName("AltAddress")
    private String AltAddress;
    @SerializedName("Latitude")
    private String Latitude;
    @SerializedName("Longitude")
    private String Longitude;
    @SerializedName("City")
    private String City;
    @SerializedName("District")
    private String District;
    @SerializedName("state")
    private String state;
    @SerializedName("country")
    private String country;
    @SerializedName("PinCode")
    private String PinCode;
    @SerializedName("TinNumber")
    private String TinNumber;
    @SerializedName("ApprovedAmmount")
    private int ApprovedAmmount;
    @SerializedName("ApproverRemark")
    private String ApproverRemark;
    @SerializedName("IsInsert")
    private int IsInsert;
    @SerializedName("MasterID")
    private int MasterID;
    @SerializedName("CreatedBy")
    private int CreatedBy;


    public int getCustomerId() {
        return CustomerId;
    }

    public void setCustomerId(int customerId) {
        CustomerId = customerId;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getClientId() {
        return ClientId;
    }

    public void setClientId(int clientId) {
        ClientId = clientId;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getMobileNo() {
        return MobileNo;
    }

    public void setMobileNo(String mobileNo) {
        MobileNo = mobileNo;
    }

    public String getALtMobileNo() {
        return ALtMobileNo;
    }

    public void setALtMobileNo(String ALtMobileNo) {
        this.ALtMobileNo = ALtMobileNo;
    }

    public String getContactPersion() {
        return ContactPersion;
    }

    public void setContactPersion(String contactPersion) {
        ContactPersion = contactPersion;
    }

    public String getAltContactPersion() {
        return AltContactPersion;
    }

    public void setAltContactPersion(String altContactPersion) {
        AltContactPersion = altContactPersion;
    }

    public String getEmailId() {
        return EmailId;
    }

    public void setEmailId(String emailId) {
        EmailId = emailId;
    }

    public String getAltEmailId() {
        return AltEmailId;
    }

    public void setAltEmailId(String altEmailId) {
        AltEmailId = altEmailId;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getAltAddress() {
        return AltAddress;
    }

    public void setAltAddress(String altAddress) {
        AltAddress = altAddress;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        City = city;
    }

    public String getDistrict() {
        return District;
    }

    public void setDistrict(String district) {
        District = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPinCode() {
        return PinCode;
    }

    public void setPinCode(String pinCode) {
        PinCode = pinCode;
    }

    public String getTinNumber() {
        return TinNumber;
    }

    public void setTinNumber(String tinNumber) {
        TinNumber = tinNumber;
    }

    public int getApprovedAmmount() {
        return ApprovedAmmount;
    }

    public void setApprovedAmmount(int approvedAmmount) {
        ApprovedAmmount = approvedAmmount;
    }

    public String getApproverRemark() {
        return ApproverRemark;
    }

    public void setApproverRemark(String approverRemark) {
        ApproverRemark = approverRemark;
    }

    public int getIsInsert() {
        return IsInsert;
    }

    public void setIsInsert(int isInsert) {
        IsInsert = isInsert;
    }

    public int getMasterID() {
        return MasterID;
    }

    public void setMasterID(int masterID) {
        MasterID = masterID;
    }

    public int getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(int createdBy) {
        CreatedBy = createdBy;
    }
}
