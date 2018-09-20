package com.mobiocean.util;

import com.google.gson.annotations.SerializedName;

public class InsertIntoContactSync {

    @SerializedName("ContactName")
    String ContactName;

    @SerializedName("ContactMobileNo1")
    String ContactMobileNo1;

    @SerializedName("EmailId")
    String EmailId;

    @SerializedName("LogDate")
    String LogDate;

    @SerializedName("MessangerId")
    String MessangerId;

    @SerializedName("LogDateTime")
    String LogDateTime;

    public InsertIntoContactSync() {

    }

    public InsertIntoContactSync(String mcontact_name, String mcont_mob1, String memail_id, String logdate) {
        this.ContactName = mcontact_name;
        this.ContactMobileNo1 = mcont_mob1;
        this.EmailId = memail_id;
        this.LogDate = logdate;
    }


    public String getLogDateTime() {
        return LogDateTime;
    }

    public void setLogDateTime(String logDateTime) {
        LogDateTime = logDateTime;
    }

    /**
     * @return the LogDate
     */
    public String getLogdate() {
        return LogDate;
    }

    /**
     * @param LogDate the LogDate to set
     */
    public void setLogdate(String logdate) {
        this.LogDate = logdate;
    }

    /**
     * @return the contact_name
     */
    public String getContact_name() {
        return ContactName;
    }

    /**
     * @param contact_name the contact_name to set
     */
    public void setContact_name(String contact_name) {
        this.ContactName = contact_name;
    }

    /**
     * @return the cont_mob1
     */
    public String getCont_mob1() {
        return ContactMobileNo1;
    }

    /**
     * @param cont_mob1 the cont_mob1 to set
     */
    public void setCont_mob1(String cont_mob1) {
        this.ContactMobileNo1 = cont_mob1;
    }

    /**
     * @return the email_id
     */
    public String getEmail_id() {
        return EmailId;
    }

    /**
     * @param email_id the email_id to set
     */
    public void setEmail_id(String email_id) {
        this.EmailId = email_id;
    }

    public String getMessangerId() {
        return MessangerId;
    }

    public void setMessangerId(String messangerId) {
        MessangerId = messangerId;
    }

}
