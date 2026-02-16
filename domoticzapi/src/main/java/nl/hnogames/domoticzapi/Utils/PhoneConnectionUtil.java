
package nl.hnogames.domoticzapi.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import nl.hnogames.domoticzapi.Interfaces.WifiSSIDListener;

public class PhoneConnectionUtil {
    private WifiManager wifiManager;
    private Context mContext;
    private NetworkInfo networkWifiInfo;
    private NetworkInfo networkCellInfo;
    private WifiSSIDListener listener;
    private BroadcastReceiver receiver;
    private AtomicBoolean unregistered;

    public PhoneConnectionUtil(Context mContext, final WifiSSIDListener listener) {
        if (mContext == null)
            return;
        this.mContext = mContext;
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkWifiInfo = connManager != null ? connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) : null;
        networkCellInfo = connManager != null ? connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) : null;
        this.listener = listener;
    }

    public PhoneConnectionUtil(Context mContext) {
        if (mContext == null)
            return;
        this.mContext = mContext;
        wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkWifiInfo = connManager != null ? connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) : null;
        networkCellInfo = connManager != null ? connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) : null;
    }

    public void stopReceiver() {
        try {
            if (receiver != null) {
                synchronized (unregistered) {
                    if (!unregistered.get()) {
                        mContext.unregisterReceiver(receiver);
                        unregistered.set(true);
                    }
                }
            }
        } catch (Exception ex) {
            receiver = null;
        }
    }

    public void startSsidScan() {
        wifiManager.startScan();
        unregistered = new AtomicBoolean(false);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                CharSequence[] entries = new CharSequence[0];

                if (results != null && results.size() > 0) {
                    entries = new CharSequence[results.size()];

                    int i = 0;
                    for (ScanResult result : results) {
                        if (result.SSID != null && result.SSID.length() > 0) {
                            entries[i] = result.SSID;
                            i++;
                        }
                    }
                }
                if (listener != null)
                    listener.ReceiveSSIDs(entries);
            }
        };
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                stopReceiver();
            }
        }, 30, TimeUnit.SECONDS);

        mContext.registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @SuppressWarnings("unused")
    public boolean isCellConnected() {
        return networkCellInfo.isConnected();
    }

    public boolean isWifiConnected() {
        return networkWifiInfo.isConnected();
    }

    public String getCurrentSsid() {
        String ssid = null;

        if (networkWifiInfo.isConnected()) {
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !connectionInfo.getSSID().isEmpty()) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        } catch (Exception ex) {
            return false;
        }
    }
}