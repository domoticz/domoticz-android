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

public class WeatherInfo {
    private final boolean isProtected;
    JSONObject jsonObject;

    int idx;
    String Name;
    String Data;
    String LastUpdate;
    long setPoint;
    String Type;

    String ForecastStr;
    String HumidityStatus;
    String DirectionStr;
    String Chill;
    String Rain;
    String RainRate;
    String Speed;
    long DewPoint;
    long Temp;

    int Barometer;
    int Favorite;
    int HardwareID;
    String HardwareName;
    String TypeImg;
    int signalLevel;


    public WeatherInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        isProtected = row.getBoolean("Protected");
        idx = row.getInt("idx");

        if (row.has("Favorite")) Favorite = row.getInt("Favorite");
        if (row.has("Barometer")) Barometer = row.getInt("Barometer");
        if (row.has("HardwareID")) HardwareID = row.getInt("HardwareID");
        if (row.has("Type")) HardwareName = row.getString("Type");
        if (row.has("Type")) Type = row.getString("Type");
        if (row.has("TypeImg")) TypeImg = row.getString("TypeImg");
        if (row.has("LastUpdate")) LastUpdate = row.getString("LastUpdate");
        if (row.has("SetPoint")) setPoint = row.getLong("SetPoint");
        if (row.has("Name")) Name = row.getString("Name");
        if (row.has("Data")) Data = row.getString("Data");

        if (row.has("Rain")) Rain = row.getString("Rain");
        if (row.has("RainRate")) RainRate = row.getString("RainRate");

        if (Type.equals("Rain")) Data = Data.substring(Data.indexOf(';') + 1, Data.length());
        if (Type.equals("Wind")) Data = Data.substring(0, Data.indexOf(';'));

        if (row.has("DewPoint")) DewPoint = row.getLong("DewPoint");
        if (row.has("Temp")) Temp = row.getLong("Temp");
        if (row.has("ForecastStr")) ForecastStr = row.getString("ForecastStr");
        if (row.has("HumidityStatus")) HumidityStatus = row.getString("HumidityStatus");
        if (row.has("DirectionStr")) DirectionStr = row.getString("DirectionStr");
        if (row.has("Chill")) Chill = row.getString("Chill");
        if (row.has("Speed")) Speed = row.getString("Speed");

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

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getBarometer() {
        return Barometer;
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

    public String getForecastStr() {
        return ForecastStr;
    }
    
    public String getRain() {
        return Rain;
    }
    public String getRainRate() {
        return RainRate;
    }

    public String getHumidityStatus() {
        return HumidityStatus;
    }

    public String getDirectionStr() {
        return DirectionStr;
    }

    public String getChill() {
        return Chill;
    }

    public String getSpeed() {
        return Speed;
    }

    public long getDewPoint() {
        return DewPoint;
    }

    public long getTemp() {
        return Temp;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    public String getHardwareName() {
        return HardwareName;
    }

    public long getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(long setPoint) {
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

    public String getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }
}