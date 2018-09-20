package org.sn.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.mobiocean.ui.MobiApplication;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.RestApiCall;
import com.mobiocean.util.UpdateFromServer;

import org.sn.beans.ResponseModel;
import org.sn.beans.SecuredStorageApiModel;
import org.sn.beans.SecuredStorageModel;
import org.sn.database.SecuredStorageTable;
import org.sn.securedstorage.AddToSecureIntentService;

import java.util.ArrayList;

public class DownloadSecuredIntentService extends IntentService {

    private Context context;
    private RestApiCall restApiCall;
    private SharedPreferences settings;
    SharedPreferences.Editor editor;
    final String PREFS_NAME = "MyPrefsFile";
    private int FileId = 0;
    private int isUpdate = 0;

    public DownloadSecuredIntentService() {
        super("DownloadSecuredIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        context = getBaseContext();
        restApiCall = new RestApiCall();
        try {
            settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            editor = settings.edit();
            FileId = settings.getInt("Secure.FileID", 0);
            isUpdate = settings.getInt("Secure.isUpdate", 0);
            MobiApplication mobiApplication = (MobiApplication) getApplication();
            ResponseModel<ArrayList<SecuredStorageApiModel>> response = restApiCall.getFileList(mobiApplication, isUpdate, FileId);
            if (response != null) {
                if (response.Result == 1) {
                    ArrayList<SecuredStorageApiModel> models = response.data;
                    if (models != null && models.size() > 0) {
                        SecuredStorageTable database = new SecuredStorageTable(context);
                        for (SecuredStorageApiModel secureModel : models) {
                            SecuredStorageModel secure = new SecuredStorageModel();
                            secure.StoragePath = "/";
                            secure.DownloadUrl = MobiApplication.CONTACT_SERVER+secureModel.FilePath;
                            secure.FileName = secureModel.FileName;
                            secure.IsDownloaded = false;
                            database.insert(secure);
                            if(FileId < secureModel.FileId){
                                FileId = secureModel.FileId;
                                editor.putInt("Secure.FileID", FileId);
                                if(isUpdate == 0) {
                                    isUpdate = 1;
                                    editor.putInt("Secure.isUpdate", 1);
                                }
                            }
                        }
                        startService(new Intent(context, AddToSecureIntentService.class));
                        sendAck();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendAck() {
        RestApiCall mRestApiCall = new RestApiCall();
        UpdateFromServer mfromServer = new UpdateFromServer();
        mfromServer.setAckDateTime(CallHelper.GetTimeWithDate());
        mfromServer.setSyncFeatureId("10");
        mfromServer.setAppId(CallHelper.Ds.structPC.iStudId);
        mRestApiCall.postUpdateAck((MobiApplication) getApplication(), mfromServer);
    }
}