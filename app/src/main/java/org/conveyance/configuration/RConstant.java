package org.conveyance.configuration;

import android.graphics.Bitmap;

import java.io.FileOutputStream;
import java.io.IOException;

/****************************************************************************
 * CHANGE_HISTORY       MODIFIED_BY         DATE            REASON_FOR_CHANGE
 * Initial creation     SIVAMURUGU          23-09-16         Initial creation
 ****************************************************************************/

public class RConstant {
    public static String FTP_HOST = "mobiocean.gingerboxmobility.com";
    public static String FTP_USER = "MobiAndroid";
    public static String FTP_PASS = "Ocean@Ginger123";
    public static final long DEFAULT_DELAY = 60 * 1000;

    /**
     * Function to save bitmap to a file
     */
    public static boolean saveImageBitmap(Bitmap bmp, String filename) {
        boolean isSaved = false;
        try {
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filename);
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, out);
                isSaved = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSaved;
    }
}
