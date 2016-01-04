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

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class DevicesInfo implements Comparable {

    private final String UNKNOWN = "Unknown";
    private final String TAG = DevicesInfo.class.getSimpleName();
    private JSONObject jsonObject;
    private boolean timers;
    private int idx;
    private String Name;
    private String LastUpdate;
    private double setPoint;
    private String Type;
    private String SubType;
    private int Favorite;
    private int HardwareID;
    private String HardwareName;
    private String TypeImg;
    private String PlanID;
    private int batteryLevel;
    private int maxDimLevel;
    private int signalLevel;
    private boolean useCustomImage;
    private String status;
    private int level;
    private int switchTypeVal;
    private String switchType;
    private String CounterToday;
    private String Counter;
    private String Usage;
    private String Image;
    private String Data;
    private String Timers;
    private boolean statusBoolean;
    private boolean isProtected;

    public DevicesInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        try {
            if (row.has("LevelInt"))
                level = row.getInt("LevelInt");
        } catch (Exception e) {
            level = 0;
        }
        try {
            if (row.has("MaxDimLevel"))
                maxDimLevel = row.getInt("MaxDimLevel");
        } catch (Exception e) {
            maxDimLevel = 1;
        }
        try {
            if (row.has("CustomImage")) {
                if (row.getInt("CustomImage") > 0)
                    useCustomImage = true;
                else
                    useCustomImage = false;
            } else
                useCustomImage = false;
        } catch (Exception e) {
            useCustomImage = false;
        }

        if (row.has("Counter"))
            Counter = row.getString("Counter");
        if (row.has("Image"))
            Image = row.getString("Image");

        if (row.has("CounterToday"))
            CounterToday = row.getString("CounterToday");

        if (row.has("Usage"))
            Usage = row.getString("Usage");

        try {
            if (row.has("Status"))
                status = row.getString("Status");
        } catch (Exception e) {
            status = "";
        }
        try {
            if (row.has("Timers"))
                Timers = row.getString("Timers");
        } catch (Exception e) {
            Timers = "False";
        }
        try {
            if (row.has("Data"))
                Data = row.getString("Data");
        } catch (Exception e) {
            status = "";
        }
        try {
            if (row.has("PlanID"))
                PlanID = row.getString("PlanID");
        } catch (Exception e) {
            PlanID = "";
        }
        try {
            if (row.has("BatteryLevel"))
                batteryLevel = row.getInt("BatteryLevel");
        } catch (Exception e) {
            batteryLevel = 0;
        }
        try {
            isProtected = row.getBoolean("Protected");
        } catch (Exception e) {
            isProtected = false;
        }
        try {
            if (row.has("SignalLevel"))
                signalLevel = row.getInt("SignalLevel");
        } catch (Exception e) {
            signalLevel = 0;
        }
        try {
            if (row.has("SwitchType"))
                switchType = row.getString("SwitchType");
        } catch (Exception e) {
            switchType = UNKNOWN;
        }
        try {
            if (row.has("SwitchTypeVal"))
                switchTypeVal = row.getInt("SwitchTypeVal");
        } catch (Exception e) {
            switchTypeVal = 999999;
        }

        if (row.has("Favorite"))
            Favorite = row.getInt("Favorite");
        if (row.has("HardwareID"))
            HardwareID = row.getInt("HardwareID");
        if (row.has("HardwareName"))
            HardwareName = row.getString("HardwareName");
        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        if (row.has("Type"))
            Type = row.getString("Type");
        if (row.has("SubType"))
            SubType = row.getString("SubType");
        if (row.has("Timers"))
            timers = row.getBoolean("Timers");

        idx = row.getInt("idx");

        try {
            signalLevel = row.getInt("SignalLevel");
        } catch (Exception ex) {
            signalLevel = 0;
        }

        try {
            if (row.has("SetPoint")) {
                setPoint = Double.parseDouble(row.getString("SetPoint"));
            }
        } catch (Exception ex) {
        }
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

    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public String getCounter() {
        return Counter;
    }

    public String getUsage() {
        return Usage;
    }

    public String getTimers() {
        return Timers;
    }

    public String getSubType() {
        return SubType;
    }

    public String getPlanID() {
        return PlanID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getStatusBoolean() {
        try {
            boolean statusBoolean = true;

            if (status.equalsIgnoreCase("On") || status.equalsIgnoreCase("Off")) {
                if (status.equalsIgnoreCase("On")) statusBoolean = true;
                else if (status.equalsIgnoreCase("Off")) statusBoolean = false;
            } else {
                if (status.equalsIgnoreCase("Open")) statusBoolean = true;
                else if (status.equalsIgnoreCase("Closed")) statusBoolean = false;
            }

            this.statusBoolean = statusBoolean;
            return statusBoolean;
        } catch (Exception ex) {
            return false;
        }
    }

    public void setStatusBoolean(boolean status) {
        this.statusBoolean = status;
        if (status)
            setStatus("On");
        else
            setStatus("Off");
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean getUseCustomImage() {
        return useCustomImage;
    }

    public int getFavorite() {
        return Favorite;
    }

    public void setFavorite(int favorite) {
        Favorite = favorite;
    }

    public int getHardwareID() {
        return HardwareID;
    }

    public void setHardwareID(int hardwareID) {
        HardwareID = hardwareID;
    }

    public String getHardwareName() {
        return HardwareName;
    }

    public String getCounterToday() {
        return CounterToday;
    }

    public String getImage() {
        return Image;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getData() {
        return Data;
    }

    public String getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setIsProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public int getMaxDimLevel() {
        return maxDimLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getSwitchTypeVal() {
        return switchTypeVal;
    }

    public String getSwitchType() {
        return switchType;
    }

    @Override
    public int compareTo(Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}