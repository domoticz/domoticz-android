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

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TemperatureInfo implements Comparable, Serializable {

    private boolean isProtected = false;
    private String jsonObject;

    private int idx;
    private String Name;
    private String Data;
    private String LastUpdate;
    private double setPoint;
    private String Type;
    private int Favorite;
    private int HardwareID;
    private String HardwareName;
    private String Status;
    private String TypeImg;
    private String Direction;
    private String Description;
    private double Temp;
    private int signalLevel;
    private double Step;
    private boolean hasStep;
    private double Max;
    private boolean hasMax;
    private double Min;
    private boolean hasMin;

    public TemperatureInfo() {
    }

    public TemperatureInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();

        if (row.has("Favorite"))
            Favorite = row.getInt("Favorite");
        isProtected = row.getBoolean("Protected");
        if (row.has("HardwareID"))
            HardwareID = row.getInt("HardwareID");
        if (row.has("HardwareName"))
            HardwareName = row.getString("HardwareName");

        try {
            if (row.has("Temp"))
                Temp = row.getDouble("Temp");
        } catch (Exception ex) {
            Temp = 0;
        }

        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");
        if (row.has("Status"))
            Status = row.getString("Status");
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        if (row.has("DirectionStr"))
            Direction = row.getString("DirectionStr");

        if (row.has("SetPoint")) {
            try {
                setPoint = row.getDouble("SetPoint");
            } catch (Exception ex) {
                setPoint = 0;
            }
        }

        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Description"))
            Description = row.getString("Description");
        if (row.has("Data")) {
            Data = row.getString("Data");
            if (Data.indexOf(';') >= 0) {
                Data = Data.substring(0, Data.indexOf(';'));
            }
        }
        if (row.has("Type"))
            Type = row.getString("Type");
        idx = row.getInt("idx");

        if (row.has("SignalLevel")) {
            try {
                signalLevel = row.getInt("SignalLevel");
            } catch (Exception ex) {
                signalLevel = 0;
            }
        }
        if (row.has("step")) {
            hasStep = true;
            try {
                Step = Double.parseDouble(row.getString("step"));
            } catch (Exception ignored) {
                Step = 0;
            }
        }
        else{
            hasStep = false;
        }

        if (row.has("Max")) {
            hasMax = true;
            try {
                Max = Double.parseDouble(row.getString("Max"));
            } catch (Exception ignored) {
                Max = 0;
            }
        }
        else{
            hasMax = false;
        }

        if (row.has("Min")) {
            hasMin = true;
            try {
                Min = Double.parseDouble(row.getString("Min"));
            } catch (Exception ignored) {
                Min = 0;
            }
        }
        else{
            hasMin = false;
        }
    }

    @Override
    public String toString() {
        return "TemperatureInfo{" +
                "isProtected=" + isProtected +
                ", jsonObject=" + jsonObject +
                ", idx=" + idx +
                ", Name='" + Name + '\'' +
                ", LastUpdate='" + LastUpdate + '\'' +
                ", setPoint=" + setPoint +
                ", Type='" + Type + '\'' +
                ", Favorite=" + Favorite +
                ", HardwareID=" + HardwareID +
                ", signalLevel=" + signalLevel +
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

    public double getTemperature() {
        return Temp;
    }

    public double getSetPoint() {
        return setPoint;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getData() {
        return Data;
    }

    public String getDirection() {
        return Direction;
    }

    public String getDescription() {
        return Description;
    }

    public String getHardwareName() {
        return HardwareName;
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

    public String getStatus() {
        return Status;
    }

    public String getTypeImg() {
        return TypeImg;
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

    public String getJsonObject() {
        return this.jsonObject;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}