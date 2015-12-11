/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Utils;

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

import nl.hnogames.domoticz.Interfaces.WifiSSIDListener;

public class PhoneConnectionUtil {

    private final WifiManager wifiManager;
    private Context mContext;
    private ConnectivityManager connManager;
    private NetworkInfo networkWifiInfo;
    private NetworkInfo networkCellInfo;
    private WifiSSIDListener listener;
    private BroadcastReceiver receiver;

    public PhoneConnectionUtil(Context mContext,
                               final WifiSSIDListener listener) {
        this.mContext = mContext;
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        networkCellInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        this.listener=listener;

        receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
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
                listener.ReceiveSSIDs(entries);
            }
        };
    }

    public void stopReceiver(){
        try {
            mContext.unregisterReceiver(receiver);
        }catch(Exception ex){}
    }

    public void startSsidScan() {
        try {
            mContext.registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }catch(Exception ex){}
        wifiManager.startScan();
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
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}