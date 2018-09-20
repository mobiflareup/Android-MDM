package org.sn.securedstorage;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;

import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;

import org.sn.beans.SecuredStorageModel;
import org.sn.database.SecuredStorageTable;
import org.sn.util.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import info.guardianproject.iocipher.VirtualFileSystem;

public class AddToSecureIntentService extends IntentService {

    private String APKFilePath = Environment.getExternalStorageDirectory().getPath() + "/MobiOcean/";
    private SecuredStorageTable database;

    public AddToSecureIntentService() {
        super("AddToSecureIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        database = new SecuredStorageTable(this);
        ArrayList<SecuredStorageModel> models = database.get(false);
        if (models != null && models.size() > 0) {
            for (SecuredStorageModel model : models) {
                if (model.StoragePath == null && model.StoragePath.isEmpty())
                    model.StoragePath = "/";
                File file = new File(APKFilePath);
                file.mkdirs();
                Download(model);
            }
        }
    }

    public void Download(SecuredStorageModel model) {
        try {
            URL url = new URL(model.DownloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            int lengthOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);
            OutputStream output = new FileOutputStream(APKFilePath + model.FileName);
            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                DeBug.ShowLogD("Downloading", "" + (int) ((total * 100) / lengthOfFile));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            try {
                //Put file to Secured Space adn delete the downloaded file.
                VirtualFileSystem vfs;
                SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
                String appId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);
                vfs = VirtualFileSystem.get();
                vfs.setContainerPath(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + appId.trim() + "-secured.db");
                if (!vfs.isMounted())
                    vfs.mount(appId + Constants.FILE_PASSWORD);
                info.guardianproject.iocipher.File sample = new info.guardianproject.iocipher.File("/"+model.FileName);
                if (!sample.exists()) {
                    InputStream in = new FileInputStream(APKFilePath+model.FileName);
                    OutputStream out = new info.guardianproject.iocipher.FileOutputStream(sample);
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();
                }
                if (!vfs.isMounted())
                    vfs.unmount();
                //delete files after save
                File deleteFile = new File(APKFilePath + model.FileName);
                if (deleteFile.exists())
                    deleteFile.delete();
                database.delete(model);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
