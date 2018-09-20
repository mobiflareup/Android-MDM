package org.sn.securedstorage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import info.guardianproject.iocipher.File;
import info.guardianproject.iocipher.FileInputStream;

public class SecureSpaceContentProvider extends ContentProvider {
    public static final String TAG = "SecureSpace";
    public static final String FILES_URI = "content://com.mobiocean.securedSpace";
    private MimeTypeMap mimeTypeMap;

    @Override
    public boolean onCreate() {
        mimeTypeMap = MimeTypeMap.getSingleton();
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        return mimeTypeMap.getMimeTypeFromExtension(fileExtension);
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode)
            throws FileNotFoundException {
        ParcelFileDescriptor[] pipe = null;
        InputStream in = null;

        try {
            pipe = ParcelFileDescriptor.createPipe();
            String path = uri.getPath();
            Log.i(TAG, "streaming " + path);
            // BufferedInputStream could help, AutoCloseOutputStream conflicts
            in = new FileInputStream(new File(path));
            new PipeFeederThread(in, new AutoCloseOutputStream(pipe[1])).start();
        } catch (IOException e) {
            Log.e(TAG, "Error opening pipe", e);
            throw new FileNotFoundException("Could not open pipe for: "
                    + uri.toString());
        }

        return (pipe[0]);
    }

    @Override
    public Cursor query(@NonNull Uri url, String[] projection, String selection,
                        String[] selectionArgs, String sort) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        throw new RuntimeException("Operation not supported");
    }

    private static class PipeFeederThread extends Thread {
        InputStream in;
        OutputStream out;

        PipeFeederThread(InputStream in, OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            byte[] buf = new byte[8192];
            int len;

            try {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.flush();
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "File transfer failed:", e);
            }
        }
    }
}