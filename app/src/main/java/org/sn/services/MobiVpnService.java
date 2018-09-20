package org.sn.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

public class MobiVpnService extends VpnService {

    public static Thread mThread;
    private ParcelFileDescriptor mInterface;
    Builder builder = new Builder();

    protected static final String PREFS_NAME = "MyPrefsFile";
    public SharedPreferences settings;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {

        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        final String dnsIp = settings.getString("VpnIpAddress", null);

        if (dnsIp != null && !dnsIp.isEmpty()) {
            mThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mInterface = builder.setSession("MyVPNService")
                                .addAddress("192.168.0.101", 24)
                                .addDnsServer(dnsIp).establish();
                        DatagramChannel tunnel = DatagramChannel.open();
                        tunnel.connect(new InetSocketAddress("127.0.0.1", 8087));
                        protect(tunnel.socket());
                        while (true) {
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mThread.interrupt();
                    } finally {
                        try {
                            if (mInterface != null) {
                                mInterface.close();
                                mInterface = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            stopSelf(startId);
                        }
                    }
                }

            }, "MyVpnRunnable");
            mThread.start();
        } else {
            stopSelf(startId);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }
}