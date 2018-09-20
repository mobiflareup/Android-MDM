package com.mobiocean.util;

import android.content.Context;
import android.text.TextUtils;

import com.mobiocean.mobidb.SOSandTheftInfoStruct;
import com.mobiocean.ui.MobiApplication;

import org.json.JSONObject;
import org.sn.beans.AppGroupBean;
import org.sn.beans.ContactListBean;
import org.sn.beans.EnabledExtraFeatureBean;
import org.sn.beans.ProfileInfoBean;
import org.sn.beans.ResponseModel;
import org.sn.beans.SecuredStorageApiModel;
import org.sn.beans.SensorListBean;
import org.sn.beans.VpnStatusBean;
import org.sn.beans.WebListBean;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.mobiocean.ui.MobiApplication.CONTACT_SERVER;

public class RestApiCall {
    private static final String TAG = RestApiCall.class.getSimpleName();

    public String profiledetails(String appid) {
        //Yes
        String url = CONTACT_SERVER + "api/Deviceuser/Employee?AppId=" + appid;
        String mgetAcknowledge = getUrlConnection(url);
        String finalResult;
        if (!TextUtils.isEmpty(mgetAcknowledge)) {
            finalResult = mgetAcknowledge.trim();
        } else {
            finalResult = null;
        }
        return finalResult;
    }

    public String UploadProfile(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Deviceuser/UpdateImage";
        String stResult = null;
        String finalResult;
        DeBug.ShowLog("JSON", "URL: " + url + " JSON: " + jsonObj.toString());
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DeBug.ShowLog("JSON", "Output " + stResult);
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;

    }

    public String getUninstallationPass() {
        //YES
        String url = CONTACT_SERVER + "api/Uninstall/getUninstallationPass?Id=" + CallHelper.Ds.structPC.iStudId;
        String finalResult;
        String statusOfLocNeededWithCallLogs;
        statusOfLocNeededWithCallLogs = getUrlConnection(url);
        if (!TextUtils.isEmpty(statusOfLocNeededWithCallLogs)) {
            finalResult = statusOfLocNeededWithCallLogs.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String BtnPress_XMLFormat(Context ctx, JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/AndroidDoc/BtnPress_XMLFormat";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String InsertIntoContactSync(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/ContactSyncList/InsertIntoContactSync";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String ChkEmp(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/ChkUser/Registration";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String ChkOTP(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/ChkUser/OTP";
        String stResult = null;
        String finalResult = null;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        }
        return finalResult;
    }

    public String StuLoc(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Location/DeviceLoc";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";

        }
        return finalResult;
    }

    public String CallLogs(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Log/CallLogs";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String SMSLogs(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Log/SMSLogs";
        String stResult = null;
        String finalResult = null;

        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }

        return finalResult;

    }

    public String VersionNo(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/DeviceInfo/InsertMdMVersion";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String RT(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/ResetDateTime/RT?isSMS=0";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String setAppInstallationStatus(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Uninstall/setAppInstallationStatus";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String SetAppList(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/ChatApp/GetAppList";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String uploadHarwareInfo(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/DeviceInfo/InsertDeviceInfo";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;

    }

    public String uploadBatteryInfo(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/DeviceInfo/InsertBatteryInfo";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;

    }

    public String uploadNetworkInfo(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/DeviceInfo/InsertNetworkInfo";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String updateInternetConnectivity(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/DeviceInfo/InsertInternetConnectivity";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;

    }

    public String uploadBrowserWebsitesInfo(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/WebsiteLogs/WebsiteLogs";
        String stResult = null;
        String finalResult;

        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
            DeBug.ShowLog("TAG", finalResult);
        } else {
            finalResult = "-1";
        }
        return finalResult;

    }

    public String postContacts(MobiApplication app, String id, ContactStruct mContactStruct) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendContactList(id, mContactStruct).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }
        DeBug.ShowLog("UploadingContact", "" + listAppGrouping);
        if (listAppGrouping == null) {
            return null;
        }

        return listAppGrouping;
    }

    public String postSmsBackUp(MobiApplication app, Sms mSms) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendSmsBackUp(mSms).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }
        DeBug.ShowLog("UploadingContact", "" + listAppGrouping);
        if (listAppGrouping == null) {
            return null;
        }

        return listAppGrouping;
    }

    public String sendSosContacts(MobiApplication app, SOSContacts mSOSContacts) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendSosContacts(mSOSContacts).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }
        DeBug.ShowLog("UploadingContact", "" + listAppGrouping);
        if (listAppGrouping == null) {
            return null;
        }

        return listAppGrouping;
    }

    public ArrayList<SOSContacts.SosContactsClass> getSosContacts(MobiApplication app, String Appid) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<SOSContacts.SosContactsClass> listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.getSosContacts(Appid).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }
        DeBug.ShowLog("UploadingContact", "" + listAppGrouping);
        if (listAppGrouping == null) {
            return null;
        }

        return listAppGrouping;
    }

    public String postCalendarsEvents(MobiApplication app, CalendarSyncServer mCalendarSyncServer) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendCalendarList(mCalendarSyncServer).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }


        return listAppGrouping;
    }

    public String postUpdateAck(MobiApplication app, UpdateFromServer mfromServer) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendUpdateAck(mfromServer).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }

        return listAppGrouping;
    }

    public String sendFileInfo(MobiApplication app, SOSandTheftInfoStruct mSOSandTheftInfoStruct) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendFileInfo(mSOSandTheftInfoStruct).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }

        return listAppGrouping;
    }

    public ArrayList<UpdateFromServer> getWhatIsUpdated(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<UpdateFromServer> listPersonalProfileInfoStruct = null;
        try {
            listPersonalProfileInfoStruct = aanwlaService.getWhatIsUpdated(CallHelper.Ds.structPC.iStudId).execute().body();
        } catch (Exception e) {
            DeBug.ShowLog("ContactsListLoader", "" + e.getMessage());
            e.printStackTrace();
        }


        return listPersonalProfileInfoStruct;
    }

    public String postApplicationLogs(MobiApplication app, GameLogs mGameLogs) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.sendApplicationLogs(mGameLogs).execute().body();
            //listAppGrouping = listAppGroupingaa.execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("postApplicationLogs", "" + e.getMessage());
            e.printStackTrace();
        }

        if (listAppGrouping == null) {
            return null;
        }

        return listAppGrouping;
    }

    public String getLock(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        String result = null;
        try {
            result = aanwlaService.getLockPin(CallHelper.Ds.structPC.iStudId).execute().body();

        } catch (Exception e) {
            DeBug.ShowLog("postApplicationLogs", "" + e.getMessage());
            e.printStackTrace();
        }
        if (result == null) {
            return null;
        }

        return result;
    }

    private String postUrlConnection(String URL, JSONObject mJSONObject) {
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        String output = null;
        try {
            URL url = new URL(URL);
            String message = mJSONObject.toString();
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 * 50 /*milliseconds*/);
            conn.setConnectTimeout(1000 * 50 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            try {
                conn.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            os.flush();
            conn.getResponseCode();
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                output = "";
                String data;
                System.out.println("Output from Server .... \n");
                while ((data = br.readLine()) != null) {
                    output = output + data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
                if (is != null)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (conn != null)
                conn.disconnect();
        }
        DeBug.ShowLogD(TAG, "URL : " + URL + " input " + mJSONObject.toString() + " \n output : " + output);
        return output;
    }

    private String getUrlConnection(String url) {
        HttpURLConnection c = null;
        String output = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(20000);
            c.setReadTimeout(20000);
            c.connect();
            int status = c.getResponseCode();
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    output = "";
                    String data;
                    System.out.println("Output from Server .... \n");
                    while ((data = br.readLine()) != null) {
                        output = output + data;
                    }
                    br.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        DeBug.ShowLogD(TAG, "URL : " + url + " \n output : " + output);
        return output;
    }


    public String sendGCMRegId(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Gcm/GCMSender";
        String stResult = null;
        String finalResult;
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String getPendingSmS(JSONObject jsonObj) {
        //YES
        String url = CONTACT_SERVER + "api/Gcm/PendingSmS";
        String stResult = null;
        String finalResult;
        try {
            url = url + "?AppId=" + jsonObj.getString("AppId");
        } catch (Exception e) {
            e.printStackTrace();
        }
        jsonObj = new JSONObject();
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;
    }

    public String sendACKGCM(JSONObject jsonObj) {
        String url = CONTACT_SERVER + "api/Gcm/GCMAck";
        String stResult = null;
        String finalResult;
        try {
            url = url + "?SendMsgIdList=" + jsonObj.getString("SendMsgIdList");
        } catch (Exception e) {
            jsonObj = new JSONObject();
        }
        try {
            stResult = postUrlConnection(url, jsonObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(stResult)) {
            finalResult = stResult.replace('"', ' ');
            finalResult = finalResult.trim();
        } else {
            finalResult = "-1";
        }
        return finalResult;

    }

    public ArrayList<ContactListBean> getContactList(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<ContactListBean> listBlockedNumbers = null;
        try {
            listBlockedNumbers = aanwlaService.getContactList(CallHelper.Ds.structPC.iStudId, "0").execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listBlockedNumbers;
    }

    public ArrayList<SensorListBean> getSensorList(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<SensorListBean> result = new ArrayList<>();
        try {
            result = aanwlaService.getSensorList(CallHelper.Ds.structPC.iStudId).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getActivatedProfile(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        int mProfileInfostruct;
        try {
            mProfileInfostruct = aanwlaService.getActiveProfileId(CallHelper.Ds.structPC.iStudId).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
            mProfileInfostruct = -1;
        }
        return mProfileInfostruct;
    }

    public ArrayList<ProfileInfoBean> getProfileList(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<ProfileInfoBean> listProfileInfostruct = null;
        try {
            listProfileInfostruct = aanwlaService.getProfileList(CallHelper.Ds.structPC.iStudId, "0").execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listProfileInfostruct;
    }

    public ArrayList<ProfileInfoBean> getProfileDetails(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<ProfileInfoBean> listProfileInfostruct = null;
        try {
            listProfileInfostruct = aanwlaService.getProfileDetails(CallHelper.Ds.structPC.iStudId, "0").execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listProfileInfostruct;
    }

    public ArrayList<AppGroupBean> getAppGroups(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<AppGroupBean> listAppGrouping = null;
        try {
            listAppGrouping = aanwlaService.getAppGroups(CallHelper.Ds.structPC.iStudId, "0").execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listAppGrouping;
    }

    public ArrayList<WebListBean> getWebSiteList(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        ArrayList<WebListBean> listBlockedWeb = null;
        try {
            listBlockedWeb = aanwlaService.getWebsites(CallHelper.Ds.structPC.iStudId, "0").execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listBlockedWeb;
    }

    public EnabledExtraFeatureBean getEnabledExtraFeature(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        EnabledExtraFeatureBean bean = null;
        try {
            bean = aanwlaService.getEnabledExtraFeature(CallHelper.Ds.structPC.iStudId).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    public VpnStatusBean getVpnDetails(MobiApplication app) {
        //YES
        AanwlaService aanwlaService = app.getContactService();
        VpnStatusBean bean = null;
        try {
            bean = aanwlaService.getVpnDetails(CallHelper.Ds.structPC.iStudId).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }

    public ResponseModel<ArrayList<SecuredStorageApiModel>> getFileList(MobiApplication app, int isUpdate, int FileID) {
        ResponseModel<ArrayList<SecuredStorageApiModel>> result = null;
        AanwlaService aanwlaService = app.getContactService();
        try {
            result = aanwlaService.getFileList(CallHelper.Ds.structPC.iStudId, isUpdate, FileID).execute().body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}