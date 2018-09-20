package org.sn.location;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author Narayanan S
 * @since V 0.0.1
 */
public class NetworkUtil {

    public enum NetworkStatus {
        WIFI, MOBILE, OTHERS, NO_NET;
    }

    public static NetworkStatus getConnectivityStatus(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    NetworkInfo.State network = activeNetwork.getState();
                    if (network != null) {
                        if (network == NetworkInfo.State.CONNECTED) {
                            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                                return NetworkStatus.WIFI;
                            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                                return NetworkStatus.MOBILE;
                            else
                                return NetworkStatus.OTHERS;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NetworkStatus.NO_NET;
    }

}