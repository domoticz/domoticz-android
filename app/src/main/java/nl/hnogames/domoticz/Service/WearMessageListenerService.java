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

package nl.hnogames.domoticz.Service;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class WearMessageListenerService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String TAG = "WEARLISTENER";
    private static final String SEND_DATA = "/send_data";
    private static final String RECEIVE_DATA = "/receive_data";
    private static final String SEND_ERROR = "/error";
    private static final String ERROR_NO_SWITCHES = "NO_SWITCHES";
    private static final String SEND_SWITCH = "/send_switch";
    private static GoogleApiClient mApiClient;
    private Domoticz domoticz;
    private SharedPrefUtil mSharedPrefs;
    private ArrayList<DevicesInfo> extendedStatusSwitches;

    private int currentSwitch = 1;

    public static void sendMessage(final String path, final String text) {
        Log.d("WEAR Message", "Send: " + text);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text.getBytes()).await();

                    if (result.getStatus().isSuccess()) {
                        Log.v("WEAR", "Message: {" + "my object" + "} sent to: " + node.getDisplayName());
                    } else {
                        Log.v("WEAR", "ERROR: failed to send Message");
                    }
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(RECEIVE_DATA)) {
            Log.v("WEAR SERVICE", "Get data from Domoticz");
            mSharedPrefs = new SharedPrefUtil(this);
            if (mApiClient == null || mApiClient.equals(null)) {
                Log.v("WEAR SERVICE", "Init Google Wear API");
                initGoogleApiClient();
            } else
                getSwitches();
        } else if (messageEvent.getPath().equalsIgnoreCase(SEND_SWITCH)) {
            Log.v("WEAR SERVICE", "Toggle Switch request received");
            String data = new String(messageEvent.getData());
            try {
                DevicesInfo selectedSwitch = new DevicesInfo(new JSONObject(data));
                domoticz = new Domoticz(getApplicationContext());

                if (selectedSwitch != null) {
                    switch (selectedSwitch.getSwitchTypeVal()) {
                        case Domoticz.Device.Type.Value.ON_OFF:
                        case Domoticz.Device.Type.Value.MEDIAPLAYER:
                        case Domoticz.Device.Type.Value.X10SIREN:
                        case Domoticz.Device.Type.Value.DOORLOCK:
                        case Domoticz.Device.Type.Value.DIMMER:
                        case Domoticz.Device.Type.Value.BLINDS:
                        case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
                            onSwitchToggle(selectedSwitch);
                            break;

                        case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                        case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                        case Domoticz.Device.Type.Value.DOORBELL:
                            //push on
                            onButtonClick(selectedSwitch.getIdx(), true);
                            break;

                        case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                            //push off

                            onButtonClick(selectedSwitch.getIdx(), false);
                            break;

                        default:
                            throw new NullPointerException(
                                    "Toggle event received from wear device for unsupported switch type.");
                    }

                    //now send latest status
                    getSwitches();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private void getSwitches() {
        extendedStatusSwitches = new ArrayList<>();
        currentSwitch = 1;
        domoticz = new Domoticz(getApplicationContext());
        domoticz.getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                extendedStatusSwitches = mDevicesInfo;
                processAllSwitches(extendedStatusSwitches);
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, error.getMessage());
            }
        },0,"lights");

    }

    private void processAllSwitches(ArrayList<DevicesInfo> extendedStatusSwitches) {
        final List<Integer> appSupportedSwitchesValues = domoticz.getWearSupportedSwitchesValues();
        final List<String> appSupportedSwitchesNames = domoticz.getWearSupportedSwitchesNames();
        ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();

        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(this);

        if (!mSharedPrefs.showCustomWear() || ( mSharedPrefs.getWearSwitches() == null || mSharedPrefs.getWearSwitches().length <= 0)){
            for (DevicesInfo mDevicesInfo : extendedStatusSwitches) {
                String name = mDevicesInfo.getName();
                int switchTypeVal = mDevicesInfo.getSwitchTypeVal();
                String switchType = mDevicesInfo.getSwitchType();

                if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                        appSupportedSwitchesValues.contains(switchTypeVal) &&
                        appSupportedSwitchesNames.contains(switchType) &&
                        mDevicesInfo.getFavoriteBoolean()) {//only dashboard switches..
                    supportedSwitches.add(mDevicesInfo);
                }
            }
        } else {
            String[] filterSwitches = mSharedPrefs.getWearSwitches();
            if(filterSwitches!=null && filterSwitches.length>0) {
                for (DevicesInfo mDevicesInfo : extendedStatusSwitches) {
                    String name = mDevicesInfo.getName();
                    String idx = mDevicesInfo.getIdx() + "";
                    int switchTypeVal = mDevicesInfo.getSwitchTypeVal();
                    String switchType = mDevicesInfo.getSwitchType();

                    if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                            appSupportedSwitchesValues.contains(switchTypeVal) &&
                            appSupportedSwitchesNames.contains(switchType)) {

                        for (String f : filterSwitches) {
                            if (f.equals(idx)) {
                                supportedSwitches.add(mDevicesInfo);
                            }
                        }
                    }
                }
            }
        }

        if (supportedSwitches!=null && supportedSwitches.size() > 0) {
            String parsedData = new Gson().toJson(supportedSwitches);
            Log.v(TAG, "Sending data: " + parsedData);
            sendMessage(SEND_DATA, parsedData);
        } else {
            Log.v(TAG, "Sending error to wearable: no switches on dashboard");
            sendMessage(SEND_ERROR, ERROR_NO_SWITCHES);
        }
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

        mApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("WEAR SERVICE", "Google Wear API Connected");
        getSwitches();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public void onSwitchToggle(DevicesInfo toggledDevice) {
        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        boolean checked = !toggledDevice.getStatusBoolean();
        if (toggledDevice.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                toggledDevice.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
            if (checked) jsonAction = Domoticz.Device.Switch.Action.OFF;
            else jsonAction = Domoticz.Device.Switch.Action.ON;
        } else {
            if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
            else jsonAction = Domoticz.Device.Switch.Action.OFF;
        }

        domoticz.setAction(toggledDevice.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }

    public void onButtonClick(int idx, boolean checked) {
        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction = Domoticz.Device.Switch.Action.OFF;

        domoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }
}
