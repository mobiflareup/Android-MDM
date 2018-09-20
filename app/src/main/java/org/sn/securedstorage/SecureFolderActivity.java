package org.sn.securedstorage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobiocean.R;
import com.mobiocean.util.CallHelper;
import com.mobiocean.util.DeBug;

import org.sn.util.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileOutputStream;
import info.guardianproject.iocipher.VirtualFileSystem;


public class SecureFolderActivity extends AppCompatActivity {

    private ArrayList<String> str = new ArrayList<>();
    private Boolean firstLvl = true;
    private ArrayList<SecureFileModel> fileList;
    private File path;
    private ListView listView;
    private TextView textView;
    private Context context;
    private VirtualFileSystem vfs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Secured Storage");
        }
        setContentView(R.layout.activity_secure_folder);
        context = this;
        listView = (ListView) findViewById(R.id.file_list);
        textView = (TextView) findViewById(R.id.folder_path);
        textView.setMovementMethod(new ScrollingMovementMethod());
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String appId = settings.getString("structPC.iStudId", CallHelper.Ds.structPC.iStudId);

        vfs = VirtualFileSystem.get();
        try {
            java.io.File file = new java.io.File(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + appId.trim() + "-secured.db");
            if (!file.exists()) {
                file.createNewFile();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        vfs.setContainerPath(getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/"+appId.trim()+"-secured.db");
        if (!vfs.isMounted())
            vfs.mount(appId + Constants.FILE_PASSWORD);
        path = new File("/");
        createSampleFile("README.txt", R.raw.readme);
        loadFileList();
    }

    private void createSampleFile(String fileName, @RawRes int rawFile){
        File sample = new File("/"+fileName);
        if (!sample.exists()) {
            try {
                InputStream in = getResources().openRawResource(rawFile);
                OutputStream out = new FileOutputStream(sample);
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong please try again later", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
        if (path.exists()) {
            String[] fList = path.list();
            fileList = new ArrayList<>();
            for (String aFList : fList) {
                SecureFileModel model = new SecureFileModel(aFList, R.mipmap.ic_logout);
                File sel = new File(path, aFList);
                if (sel.isDirectory()) {
                    model.icon = R.mipmap.ic_logout;
                    DeBug.ShowLogD("AttachDoc", "Folder" + path.getAbsolutePath());
                } else {
                    switch (aFList.substring(aFList.lastIndexOf(".") + 1)) {
                        case "apk":
                            model.icon = R.mipmap.ic_doc_apk;
                            break;
                        case "txt":
                        case "xml":
                        case "htm":
                        case "html":
                        case "php":
                            model.icon = R.mipmap.ic_doc_text;
                            break;
                        case "png":
                        case "gif":
                        case "jpg":
                        case "jpeg":
                        case "tif":
                            model.icon = R.mipmap.ic_doc_image;
                            break;
                        case "mp3":
                        case "wav":
                        case "ogg":
                        case "mid":
                        case "midi":
                        case "amr":
                            model.icon = R.mipmap.ic_doc_audio;
                            break;
                        case "mp4":
                        case "avi":
                        case "mov":
                        case "3gp":
                        case "mpeg":
                            model.icon = R.mipmap.ic_doc_video;
                            break;
                        case "pdf":
                            model.icon = R.mipmap.ic_doc_pdf;
                            break;
                        default:
                            model.icon = R.mipmap.ic_doc_others;
                            break;
                    }
                    DeBug.ShowLogD("AttachDoc", "File" + path.getAbsolutePath());
                }
                fileList.add(model);
            }
            if (!firstLvl) {
                SecureFileModel model = new SecureFileModel("", R.mipmap.ic_app_update);
                fileList.add(0, model);
            }
        } else {
            Toast.makeText(context, "Path doesn't exist", Toast.LENGTH_SHORT).show();
        }
        SecureListAdapter adapter = new SecureListAdapter(context, fileList);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        textView.setText("/");
        if (!str.isEmpty()) {
            for (String s : str)
                textView.append(s + "/");
        }
    }

    public void adapterOnClick(String file) {
        File sel = new File(path + "/" + file);
        if (isEmpty(file) && !str.isEmpty()) {
            String s = str.remove(str.size() - 1);
            path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
            fileList = null;
            if (str.isEmpty()) {
                firstLvl = true;
            }
            loadFileList();
            DeBug.ShowLogD("AttachDoc", path.getAbsolutePath());
        } else if (sel.isDirectory()) {
            firstLvl = false;
            str.add(file);
            fileList = null;
            path = new File(sel + "");
            loadFileList();
            DeBug.ShowLogD("AttachDoc", path.getAbsolutePath());
        } else {
            if (sel.exists()) {
                String aFList = sel.getName();
                switch (aFList.substring(aFList.lastIndexOf(".") + 1)) {
                    case "apk":
                        openOtherDocument(sel);
                        break;
                    case "txt":
                    case "cvs":
                    case "xml":
                    case "htm":
                    case "html":
                    case "php":
                    case "png":
                    case "gif":
                    case "jpg":
                    case "tif":
                    case "jpeg":
                        openMyDocument(sel);
                        break;
                    case "pdf":
                        openMyPdf(sel);
                        break;
                    case "mp3":
                    case "wav":
                    case "ogg":
                    case "mid":
                    case "amr":
                        openMyAudio(sel);
                        break;
                    case "mp4":
                    case "avi":
                    case "mov":
                    case "3gp":
                    case "mpeg":
                        openMyVideo(sel);
                        break;
                    default:
                        Toast.makeText(context, "The format is not supported please contact admin", Toast.LENGTH_SHORT).show();
                        break;
                }
            } else {
                Toast.makeText(context, "Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void adapterOnLongClick(String file) {
        final File sel = new File(path + "/" + file);
        AlertDialog.Builder dialogue = new AlertDialog.Builder(context);
        dialogue.setTitle("Delete")
                .setMessage("Are you sure you want to delete "+file+"?")
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            if (sel.isDirectory()) {
                                deleteRecursive(sel);
                            } else {
                                if (sel.exists()) {
                                    sel.delete();
                                } else {
                                    Toast.makeText(context, "Something went wrong please try again later", Toast.LENGTH_SHORT).show();
                                }
                            }
                            loadFileList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog alertDialog = dialogue.create();
        alertDialog.show();
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);
        fileOrDirectory.delete();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!firstLvl)
            adapterOnClick("");
        else
            super.onBackPressed();
    }

    public boolean isEmpty(CharSequence text) {
        return (text == null || text.toString().trim().isEmpty());
    }

    private void openMyDocument(File file) {
        if(file.exists()){
            try{
                Intent i = new Intent(SecureFolderActivity.this, WebViewSecuredFileActivity.class);
                i.setData(Uri.parse(SecureSpaceContentProvider.FILES_URI+file.getAbsolutePath()));
                context.startActivity(i);
            }catch (Exception e){
                Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(context, "File doesn't exist", Toast.LENGTH_SHORT).show();
    }

    private void openMyAudio(File file) {
        if(file.exists()){
            try{
                Intent i = new Intent(SecureFolderActivity.this, AudioViewActivity.class);
                i.setData(Uri.parse(SecureSpaceContentProvider.FILES_URI+file.getAbsolutePath()));
                context.startActivity(i);
            }catch (Exception e){
                Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(context, "File doesn't exist", Toast.LENGTH_SHORT).show();
    }

    private void openMyVideo(File file) {
        if(file.exists()){
            try{
                Intent i = new Intent(SecureFolderActivity.this, VideoViewActivity.class);
                i.setData(Uri.parse(SecureSpaceContentProvider.FILES_URI+file.getAbsolutePath()));
                context.startActivity(i);
            }catch (Exception e){
                Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(context, "File doesn't exist", Toast.LENGTH_SHORT).show();
    }

    private void openMyPdf(File file) {
        if(file.exists()){
            try{
                Intent i = new Intent(SecureFolderActivity.this, PdfViewSecuredFileActivity.class);
                i.setData(Uri.parse(SecureSpaceContentProvider.FILES_URI+file.getAbsolutePath()));
                context.startActivity(i);
            }catch (Exception e){
                Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(context, "File doesn't exist", Toast.LENGTH_SHORT).show();
    }

    private void openOtherDocument(File file) {
        String path = file.getAbsolutePath();
        if(file.exists()){
            try{
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                String ext = path.substring(path.lastIndexOf(".") + 1);
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                if (isEmpty(mimeType))
                        mimeType = "file/*";
                Uri fileUri = Uri.parse(SecureSpaceContentProvider.FILES_URI + file.getAbsolutePath());
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setDataAndType(fileUri, mimeType);
                context.startActivity(i);
            }catch (Exception e){
                Toast.makeText(context, "Error "+e, Toast.LENGTH_SHORT).show();
            }
        }else
            Toast.makeText(context, "File doesn't exist", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!vfs.isMounted())
            vfs.unmount();
    }
}
