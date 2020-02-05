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

package nl.hnogames.domoticz.Containers;

import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class DevicesInfo implements Comparable {

    @SuppressWarnings("unused")
    private final String TAG = DevicesInfo.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private final String UNKNOWN = "Unknown";
    private JSONObject jsonObject;
    private boolean timers;
    private int idx;
    private String Name;
    private String Description;
    private String LastUpdate;
    private double temp;
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
    private String LevelNames;
    private String Usage;
    private String Image;
    private String Data;
    private String Timers;

    private String ForecastStr;
    private String HumidityStatus;
    private String DirectionStr;
    private String Direction;
    private String Chill;
    private String Speed;

    private long DewPoint;
    private long Temp;
    private int Barometer;

    private boolean Notifications;
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

        if (row.has("ForecastStr")) ForecastStr = row.getString("ForecastStr");
        if (row.has("HumidityStatus")) HumidityStatus = row.getString("HumidityStatus");
        if (row.has("Direction")) Direction = row.getString("Direction");
        if (row.has("DirectionStr")) DirectionStr = row.getString("DirectionStr");
        if (row.has("Chill")) Chill = row.getString("Chill");
        if (row.has("Speed")) Speed = row.getString("Speed");
        if (row.has("DewPoint")) DewPoint = row.getLong("DewPoint");
        if (row.has("Temp")) Temp = row.getLong("Temp");
        if (row.has("Barometer")) Barometer = row.getInt("Barometer");

        try {
            if (row.has("MaxDimLevel"))
                maxDimLevel = row.getInt("MaxDimLevel");
        } catch (Exception e) {
            maxDimLevel = 1;
        }

        try {
            if (row.has("CustomImage"))
                useCustomImage = row.getInt("CustomImage") > 0;
            else
                useCustomImage = false;
        } catch (Exception ignored) {
            useCustomImage = false;
        }

        if (row.has("Counter"))
            Counter = row.getString("Counter");
        if (row.has("Image"))
            Image = row.getString("Image");

        if (row.has("LevelNames"))
            LevelNames = row.getString("LevelNames");
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
        if (row.has("Notifications"))
            Notifications = row.getBoolean("Notifications");

        idx = row.getInt("idx");

        try {
            signalLevel = row.getInt("SignalLevel");
        } catch (Exception ex) {
            signalLevel = 0;
        }

        try {
            if (row.has("Temp")) {
                temp = row.getDouble("Temp");
            } else {
                temp = Double.NaN;
            }
        } catch (Exception ex) {
            temp = Double.NaN;
        }

        try {
            if (row.has("SetPoint")) {
                setPoint = row.getDouble("SetPoint");
            } else {
                setPoint = Double.NaN;
            }
        } catch (Exception ex) {
            setPoint = Double.NaN;
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


    public double getTemperature() {
        return temp;
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

    public String[] getLevelNames() {
        return Pattern.compile("|", Pattern.LITERAL).split(LevelNames);
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
                new GsonBuilder()
                        .serializeSpecialFloatingPointValues()
                        .create()
                        .toJson(this) +
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

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
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

    public Date getLastUpdateDateTime() {
        //Time format: 2016-01-30 12:48:37
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(LastUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public boolean hasNotifications() {
        return Notifications;
    }

    public String getForecastStr() {
        return ForecastStr;
    }

    public String getHumidityStatus() {
        return HumidityStatus;
    }

    public String getDirectionStr() {
        return DirectionStr;
    }

    public String getDirection() {
        return Direction;
    }

    public String getChill() {
        return Chill;
    }

    public String getSpeed() {
        return Speed;
    }

    public int getBarometer() {
        return Barometer;
    }

    public long getDewPoint() {
        return DewPoint;
    }

    public long getTemp() {
        return Temp;
    }

    public void setNotifications(boolean notifications) {
        Notifications = notifications;
    }
}