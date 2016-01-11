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

package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

public class ConfigInfo {

    private JSONObject jsonObject;

    private double DegreeDaysBaseTemperature;
    private int FiveMinuteHistoryDays;
    private double Latitude;
    private double Longitude;
    private double TempScale;
    private String TempSign;
    private double WindScale;
    private String WindSign;

    public ConfigInfo(JSONObject row) throws JSONException {
        mapFields(row);
    }

    public ConfigInfo(String json) {
        try {
            mapFields(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void mapFields(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("WindSign"))
            WindSign = row.getString("WindSign");
        if (row.has("TempSign"))
            TempSign = row.getString("TempSign");
        if (row.has("DegreeDaysBaseTemperature"))
            DegreeDaysBaseTemperature = row.getDouble("DegreeDaysBaseTemperature");
        if (row.has("Latitude"))
            Latitude = row.getDouble("Latitude");
        if (row.has("Longitude"))
            Longitude = row.getDouble("Longitude");
        if (row.has("TempScale"))
            TempScale = row.getDouble("TempScale");
        if (row.has("WindScale"))
            WindScale = row.getDouble("WindScale");
        if (row.has("FiveMinuteHistoryDays"))
            FiveMinuteHistoryDays = row.getInt("FiveMinuteHistoryDays");
    }

    public String getJsonObject() {
        return jsonObject.toString();
    }

    public String getWindSign() {
        return WindSign;
    }

    public String getTempSign() {
        return TempSign;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

    public double getDegreeDaysBaseTemperature() {
        return DegreeDaysBaseTemperature;
    }

    public double getTempScale() {
        return TempScale;
    }

    public double getWindScale() {
        return WindScale;
    }

    public int getFiveMinuteHistoryDays() {
        return FiveMinuteHistoryDays;
    }
}