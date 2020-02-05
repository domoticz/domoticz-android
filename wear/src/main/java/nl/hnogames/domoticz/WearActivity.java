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

package nl.hnogames.domoticz;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.wearable.view.WearableListView;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapter.ListAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.app.DomoticzActivity;

public class WearActivity extends DomoticzActivity
        implements WearableListView.ClickListener,
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks {

    private ArrayList<DevicesInfo> switches = null;
    private WearableListView listView;
    private ListAdapter adapter;
    private Domoticz mDomoticz;

    // Sample dataset for the list
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mDomoticz = new Domoticz();
        listView = (WearableListView) findViewById(R.id.wearable_list);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String switchesRawData = prefs.getString(PREF_SWITCH, "");
        if (switchesRawData != null && switchesRawData.length() > 0)
            createListView(new Gson().fromJson(switchesRawData, String[].class));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected " + bundle);
    }

    private void createListView(String[] switchesRawData) {
        try {
            switches = new ArrayList<>();
            for (String s : switchesRawData) {
                switches.add(new DevicesInfo(new JSONObject(s)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v("WEAR", "Parsing error: " + e.getMessage());
        }

        Log.v("WEAR", "Parsing information: " + switches.toString());
        adapter = new ListAdapter(this, switches);
        listView.setAdapter(adapter);
        listView.setClickListener(this);
    }

    // WearableListView click listener
    @Override
    public void onClick(WearableListView.ViewHolder v) {
        Integer tag = (Integer) v.itemView.getTag();
        DevicesInfo clickedDevice = switches.get(tag);

        int switchTypeVal = clickedDevice.getSwitchTypeVal();
        String switchType = clickedDevice.getSwitchType();

        //only handle click event for supported switches (others are read only!!)
        if ((mDomoticz.getWearSupportedSwitchesValues().contains(switchTypeVal) &&
                mDomoticz.getWearSupportedSwitchesNames().contains(switchType)) ||
                (clickedDevice.getType().equals(Domoticz.Scene.Type.GROUP) || clickedDevice.getType().equals(Domoticz.Scene.Type.SCENE))) {
            Intent intent = new Intent(this, SendActivity.class);
            String sendData = "";

            try {
                JSONObject switchJSON = switches.get(tag).getJsonObject();
                if (switchJSON.has("nameValuePairs"))
                    sendData = switchJSON.getString("nameValuePairs").toString();
                else
                    sendData = switchJSON.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            intent.putExtra("SWITCH", sendData);
            startActivity(intent);
        }
    }

    @Override
    public void onTopEmptyRegionClick() {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "Receive: " + messageEvent.getPath() + " - " + messageEvent.getData());
        if (messageEvent.getPath().equalsIgnoreCase(SEND_DATA)) {
            String[] rawData = new Gson().fromJson(new String(messageEvent.getData()), String[].class);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_SWITCH, new String(messageEvent.getData())).apply();
            createListView(rawData);
        }
    }
}