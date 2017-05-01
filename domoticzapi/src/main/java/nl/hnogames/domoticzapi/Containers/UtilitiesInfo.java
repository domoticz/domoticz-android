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

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UtilitiesInfo implements Comparable, Serializable {

    private final boolean isProtected;
    private String jsonObject;

    private int idx;
    private String Name;
    private String Description;
    private String Data;
    private String LastUpdate;
    private double setPoint;
    private String Type;
    private String SubType;
    private int Favorite;
    private int HardwareID;
    private String HardwareName;
    private String TypeImg;
    private String CounterToday;
    private String Counter;
    private String Usage;
    private String UsageDeliv;
    private int signalLevel;

    public UtilitiesInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();

        if (row.has("Favorite"))
            Favorite = row.getInt("Favorite");
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        isProtected = row.getBoolean("Protected");
        if (row.has("HardwareID"))
            HardwareID = row.getInt("HardwareID");
        if (row.has("HardwareName"))
            HardwareName = row.getString("HardwareName");
        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");

        if (row.has("SetPoint")) {
            setPoint = Double.parseDouble(row.getString("SetPoint"));
        }

        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Description"))
            Description = row.getString("Description");
        if (row.has("Data"))
            Data = row.getString("Data");
        if (row.has("Type"))
            Type = row.getString("Type");
        if (row.has("Counter"))
            Counter = row.getString("Counter");
        if (row.has("CounterToday"))
            CounterToday = row.getString("CounterToday");

        if (row.has("Usage"))
            Usage = row.getString("Usage");
        if (row.has("UsageDeliv"))
            UsageDeliv = row.getString("UsageDeliv");

        if (row.has("SubType"))
            SubType = row.getString("SubType");
        idx = row.getInt("idx");

        if (row.has("SignalLevel")) {
            try {
                signalLevel = row.getInt("SignalLevel");
            } catch (Exception ex) {
                signalLevel = 0;
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new Gson().toJson(this) +
                '}';
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public String getCounter() {
        return Counter;
    }

    public String getUsage() {
        return Usage;
    }

    public String getUsageDeliv() {
        return UsageDeliv;
    }

    public String getCounterToday() {
        return CounterToday;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    public String getData() {
        return Data;
    }

    public String getHardwareName() {
        return HardwareName;
    }

    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public int getFavorite() {
        return Favorite;
    }

    public void setFavorite(int favorite) {
        Favorite = favorite;
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = false;
        if (this.Favorite == 1) favorite = true;
        return favorite;
    }

    public void setFavoriteBoolean(boolean favorite) {
        if (favorite) this.Favorite = 1;
        else this.Favorite = 0;
    }

    public int getHardwareID() {
        return HardwareID;
    }

    public void setHardwareID(int hardwareID) {
        HardwareID = hardwareID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getSubType() {
        return SubType;
    }

    public String getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public String getJsonObject() {
        return this.jsonObject;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}