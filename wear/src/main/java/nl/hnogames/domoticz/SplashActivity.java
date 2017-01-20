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
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

import nl.hnogames.domoticz.app.DomoticzActivity;

public class SplashActivity extends DomoticzActivity implements
        MessageApi.MessageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_main);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Log.d(TAG, "Splash Receive: " + messageEvent.getPath() + " - " + messageEvent.getData());
        if (messageEvent.getPath().equalsIgnoreCase(this.SEND_DATA)) {
            String rawData = new String(messageEvent.getData());
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_SWITCH, rawData).apply();

            Intent intent = new Intent(this, WearActivity.class);
            startActivity(intent);
            this.finish();
        } else if (messageEvent.getPath().equalsIgnoreCase(SEND_ERROR)) {
            String errorMessage = new String(messageEvent.getData());
            if (errorMessage.equals(ERROR_NO_SWITCHES)) {
                Intent intent = new Intent(this, ErrorActivity.class);
                startActivity(intent);
                this.finish();
            }
        }
        super.onMessageReceived(messageEvent);
    }
}
