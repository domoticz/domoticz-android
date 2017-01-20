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

package nl.hnogames.domoticzapi.Containers;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsInfo {
    private JSONObject jsonObject;
    private String secPassword;
    private int secOnDelay;

    public SettingsInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("SecPassword"))
            secPassword = row.getString("SecPassword");
        if (row.has("SecOnDelay"))
            secOnDelay = row.getInt("SecOnDelay");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new Gson().toJson(this) +
                '}';
    }

    public String getSecPassword() {
        return secPassword;
    }

    public int getSecOnDelay() {
        return secOnDelay;
    }

}