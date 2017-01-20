/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import nl.hnogames.domoticz.R;

public class DomoticzActivity extends Activity implements
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks {

    public final String TAG = "WEARDEVICE";
    public final String SEND_DATA = "/send_data";
    public final String RECEIVE_DATA = "/receive_data";
    public final String SEND_ERROR = "/error";
    public final String ERROR_NO_SWITCHES = "NO_SWITCHES";
    public final String PREF_SWITCH = "SWITCHES";
    public final String SEND_SWITCH = "/send_switch";
    public GoogleApiClient mApiClient;
    public boolean receiveOnce = false;

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
        Wearable.MessageApi.addListener(mApiClient, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Wearable.MessageApi.removeListener(mApiClient, this);
        mApiClient.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_main);
        receiveOnce = false;
        initGoogleApiClient();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        receiveOnce = true;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected " + bundle);

        if (!receiveOnce)
            sendMessage(RECEIVE_DATA, "");
    }

    public void sendMessage(final String path, final String text) {
        Log.d(TAG, "Send: " + text);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.v(TAG, "Message: {" + "my object" + "} sent to: " + node.getDisplayName());
                    } else {
                        Log.v(TAG, "ERROR: failed to send Message");
                        Toast.makeText(getApplicationContext(), "Connection to phone failed", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }
}
