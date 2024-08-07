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

import android.text.Html;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class UtilitiesInfo implements Comparable, Serializable {

    private boolean isProtected = false;
    private String jsonObject;
    private String vunit;

    private int idx;
    private String Name;
    private String Description;
    private String Data;
    private String LastUpdate;
    private double setPoint;

    private double Step;
    private boolean hasStep;
    private double Max;
    private boolean hasMax;
    private double Min;
    private boolean hasMin;
    private String Type;
    private String SubType;
    private int Favorite;
    private int HardwareID;
    private String HardwareName;
    private String TypeImg;
    private String CounterToday;
    private String Counter;
    private String CounterDelivToday;
    private String CounterDeliv;
    private String Usage;
    private String UsageDeliv;
    private int signalLevel;
    private String Modes;
    private int Mode;
    private boolean useCustomImage;
    private String Image;

    public UtilitiesInfo() {
    }

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
        if (row.has("vunit"))
            vunit = row.getString("vunit");

        if (row.has("step")) {
            hasStep = true;
            try {
                Step = row.getDouble("step");
            } catch (Exception ignored) {
                Step = 0;
            }
        } else {
            hasStep = false;
        }

        if (row.has("max")) {
            hasMax = true;
            try {
                Max = row.getDouble("max");
            } catch (Exception ignored) {
                Max = 0;
            }
        } else {
            hasMax = false;
        }

        if (row.has("min")) {
            hasMin = true;
            try {
                Min = row.getDouble("min");
            } catch (Exception ignored) {
                Min = 0;
            }
        } else {
            hasMin = false;
        }
        if (row.has("SetPoint")) {
            try {
                setPoint = Double.parseDouble(row.getString("SetPoint"));
            } catch (Exception ignored) {
                setPoint = 0;
            }
        }
        try {
            if (row.has("CustomImage"))
                useCustomImage = row.getInt("CustomImage") > 0;
            else
                useCustomImage = false;
        } catch (Exception ignored) {
            useCustomImage = false;
        }
        if (row.has("Image"))
            Image = row.getString("Image");
        if (row.has("Modes")) {
            Modes = row.getString("Modes");
            if (UsefulBits.isBase64Encoded(Modes))
                Modes = UsefulBits.decodeBase64(Modes);
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
        if (row.has("CounterDeliv"))
            CounterDeliv = row.getString("CounterDeliv");
        if (row.has("CounterDelivToday"))
            CounterDelivToday = row.getString("CounterDelivToday");
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
        if (row.has("Mode")) {
            try {
                Mode = row.getInt("Mode");
            } catch (Exception ex) {
                Mode = 0;
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new Gson().toJson(this) +
                '}';
    }

    public String getVUnit() {
        return vunit;
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

    public int getModeId() {
        return Mode;
    }

    public void setModeId(int mode) {
        Mode = mode;
    }

    public void SetModeId(int mode) {
        this.Mode = mode;
    }

    public ArrayList<String> getModes() {
        if (UsefulBits.isEmpty(Modes))
            return null;
        String[] names = Pattern.compile(";", Pattern.LITERAL).split(Modes);

        ArrayList<String> newNames = new ArrayList<String>();
        for (String value : names) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                newNames.add(Html.fromHtml(value).toString());
            }
        }

        return newNames;
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

    public String getUsage() {
        return Usage;
    }

    public String getUsageDeliv() {
        return UsageDeliv;
    }

    public boolean getUseCustomImage() {
        return useCustomImage;
    }

    public String getImage() {
        return Image;
    }

    public String getCounter() {
        return Counter;
    }

    public String getCounterToday() {
        return CounterToday;
    }

    public String getCounterDeliv() {
        return CounterDeliv;
    }

    public String getCounterDelivToday() {
        return CounterDelivToday;
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

    public double getStep() {
        return Step;
    }

    public void setStep(double step) {
        this.Step = step;
    }

    public boolean hasStep() {
        return hasStep;
    }

    public double getMax() {
        return Max;
    }

    public void setMax(double Max) {
        this.Max = Max;
    }

    public boolean hasMax() {
        return hasMax;
    }

    public double getMin() {
        return Min;
    }

    public void setMin(double Min) {
        this.Min = Min;
    }

    public boolean hasMin() {
        return hasMin;
    }

    public int getFavorite() {
        return Favorite;
    }

    public void setFavorite(int favorite) {
        Favorite = favorite;
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = this.Favorite == 1;
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