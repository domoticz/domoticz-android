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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ConfigInfo implements Serializable {

    private double DegreeDaysBaseTemperature;
    private int FiveMinuteHistoryDays;
    private double Latitude;
    private double Longitude;
    private double TempScale;
    private String TempSign;
    private double WindScale;
    private String WindSign;
    private long dateOfConfig;
    private String language;
    private String DashboardType;
    private ArrayList<UserInfo> mUsers;

    private boolean EnableTabCustom = false;
    private boolean EnableTabDashboard = false;
    private boolean EnableTabFloorplans = false;
    private boolean EnableTabLights = false;
    private boolean EnableTabProxy = false;
    private boolean EnableTabScenes = false;
    private boolean EnableTabTemp = false;
    private boolean EnableTabUtility = false;
    private boolean EnableTabWeather = false;
    private boolean ShowUpdatedEffect = false;

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
        if (row.has("language"))
            language = row.getString("language");
        if (row.has("DashboardType"))
            DashboardType = row.getString("DashboardType");

        if (row.has("result")) {
            JSONObject json = row.getJSONObject("result");
            if (json.has("EnableTabCustom"))
                EnableTabCustom = json.getBoolean("EnableTabCustom");
            if (json.has("EnableTabDashboard"))
                EnableTabDashboard = json.getBoolean("EnableTabDashboard");
            if (json.has("EnableTabProxy"))
                EnableTabProxy = json.getBoolean("EnableTabProxy");
            if (json.has("EnableTabLights"))
                EnableTabLights = json.getBoolean("EnableTabLights");
            if (json.has("EnableTabFloorplans"))
                EnableTabFloorplans = json.getBoolean("EnableTabFloorplans");
            if (json.has("EnableTabScenes"))
                EnableTabScenes = json.getBoolean("EnableTabScenes");
            if (json.has("EnableTabTemp"))
                EnableTabTemp = json.getBoolean("EnableTabTemp");
            if (json.has("EnableTabUtility"))
                EnableTabUtility = json.getBoolean("EnableTabUtility");
            if (json.has("EnableTabWeather"))
                EnableTabWeather = json.getBoolean("EnableTabWeather");
            if (json.has("ShowUpdatedEffect"))
                ShowUpdatedEffect = json.getBoolean("ShowUpdatedEffect");
        }
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

    public long getDateOfConfig() {
        return dateOfConfig;
    }

    public void setDateOfConfig(long dateOfConfig) {
        this.dateOfConfig = dateOfConfig;
    }

    public String getLanguage() {
        return language;
    }

    public String getDashboardType() {
        return DashboardType;
    }

    public ArrayList<UserInfo> getUsers() {
        return mUsers;
    }

    public void setUsers(ArrayList<UserInfo> mUsers) {
        this.mUsers = mUsers;
    }

    public boolean isEnableTabCustom() {
        return EnableTabCustom;
    }

    public void setEnableTabCustom(boolean enableTabCustom) {
        EnableTabCustom = enableTabCustom;
    }

    public boolean isEnableTabDashboard() {
        return EnableTabDashboard;
    }

    public void setEnableTabDashboard(boolean enableTabDashboard) {
        EnableTabDashboard = enableTabDashboard;
    }

    public boolean isEnableTabFloorplans() {
        return EnableTabFloorplans;
    }

    public void setEnableTabFloorplans(boolean enableTabFloorplans) {
        EnableTabFloorplans = enableTabFloorplans;
    }

    public boolean isEnableTabLights() {
        return EnableTabLights;
    }

    public void setEnableTabLights(boolean enableTabLights) {
        EnableTabLights = enableTabLights;
    }

    public boolean isEnableTabProxy() {
        return EnableTabProxy;
    }

    public void setEnableTabProxy(boolean enableTabProxy) {
        EnableTabProxy = enableTabProxy;
    }

    public boolean isEnableTabScenes() {
        return EnableTabScenes;
    }

    public void setEnableTabScenes(boolean enableTabScenes) {
        EnableTabScenes = enableTabScenes;
    }

    public boolean isEnableTabTemp() {
        return EnableTabTemp;
    }

    public void setEnableTabTemp(boolean enableTabTemp) {
        EnableTabTemp = enableTabTemp;
    }

    public boolean isEnableTabUtility() {
        return EnableTabUtility;
    }

    public void setEnableTabUtility(boolean enableTabUtility) {
        EnableTabUtility = enableTabUtility;
    }

    public boolean isEnableTabWeather() {
        return EnableTabWeather;
    }

    public void setEnableTabWeather(boolean enableTabWeather) {
        EnableTabWeather = enableTabWeather;
    }

    public boolean isShowUpdatedEffect() {
        return ShowUpdatedEffect;
    }

    public void setShowUpdatedEffect(boolean showUpdatedEffect) {
        ShowUpdatedEffect = showUpdatedEffect;
    }
}