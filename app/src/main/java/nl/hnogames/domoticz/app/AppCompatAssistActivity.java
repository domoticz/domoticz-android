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

import android.annotation.TargetApi;
import android.app.assist.AssistContent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class AppCompatAssistActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        super.onProvideAssistContent(outContent);
        try {
            outContent.setStructuredData(
                    new JSONObject()
                            .put("@type", "SoftwareApplication")
                            .put("author", "Domoticz")
                            .put("name", "Domoticz")
                            .put("id", "http://www.domoticz.com")
                            .put("description", "Domoticz is a very light weight home automation system that lets you monitor and configure miscellaneous devices, including lights, switches, various sensors/meters like temperature, rainfall, wind, ultraviolet (UV) radiation, electricity usage/production, gas consumption, water consumption and many more."
                            ).toString()
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
