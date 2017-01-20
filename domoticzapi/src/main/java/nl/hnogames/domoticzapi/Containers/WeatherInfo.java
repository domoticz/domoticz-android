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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class WeatherInfo implements Comparable, Serializable {
    private final boolean isProtected;
    private String jsonObject;
    private int idx;

    private String Name;
    private String Data;
    private String LastUpdate;
    private String Type;
    private String ForecastStr;
    private String HumidityStatus;
    private String DirectionStr;
    private String Direction;
    private String Chill;
    private String Rain;
    private String RainRate;
    private String Speed;
    private long DewPoint;
    private long Temp;
    private int Barometer;
    private int Favorite;
    private int HardwareID;
    private String HardwareName;
    private String TypeImg;
    private int signalLevel;


    public WeatherInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();

        isProtected = row.getBoolean("Protected");
        idx = row.getInt("idx");

        if (row.has("Favorite")) Favorite = row.getInt("Favorite");
        if (row.has("Barometer")) Barometer = row.getInt("Barometer");
        if (row.has("HardwareID")) HardwareID = row.getInt("HardwareID");
        if (row.has("Type")) HardwareName = row.getString("Type");
        if (row.has("Type")) Type = row.getString("Type");
        if (row.has("TypeImg")) TypeImg = row.getString("TypeImg");
        if (row.has("LastUpdate")) LastUpdate = row.getString("LastUpdate");
        if (row.has("Name")) Name = row.getString("Name");
        if (row.has("Data")) Data = row.getString("Data");

        if (row.has("Rain")) Rain = row.getString("Rain");
        if (row.has("RainRate")) RainRate = row.getString("RainRate");

        if (Type.equals("Rain") && Data.indexOf(';') >= 0) {
            Data = Data.substring(Data.indexOf(';') + 1);
        }
        if (Type.equals("Wind") && Data.indexOf(';') >= 0) {
            Data = Data.substring(0, Data.indexOf(';'));
        }
        if (row.has("DewPoint")) DewPoint = row.getLong("DewPoint");
        if (row.has("Temp")) Temp = row.getLong("Temp");
        if (row.has("ForecastStr")) ForecastStr = row.getString("ForecastStr");
        if (row.has("HumidityStatus")) HumidityStatus = row.getString("HumidityStatus");
        if (row.has("Direction")) Direction = row.getString("Direction");
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
        return "WeatherInfo{" +
                "isProtected=" + isProtected +
                ", Direction='" + Direction + '\'' +
                ", jsonObject=" + jsonObject +
                ", idx=" + idx +
                ", Name='" + Name + '\'' +
                ", Data='" + Data + '\'' +
                ", LastUpdate='" + LastUpdate + '\'' +
                ", Type='" + Type + '\'' +
                ", ForecastStr='" + ForecastStr + '\'' +
                ", HumidityStatus='" + HumidityStatus + '\'' +
                ", DirectionStr='" + DirectionStr + '\'' +
                ", Chill='" + Chill + '\'' +
                ", Rain='" + Rain + '\'' +
                ", RainRate='" + RainRate + '\'' +
                ", Speed='" + Speed + '\'' +
                ", DewPoint=" + DewPoint +
                ", Temp=" + Temp +
                ", Barometer=" + Barometer +
                ", Favorite=" + Favorite +
                ", HardwareID=" + HardwareID +
                ", HardwareName='" + HardwareName + '\'' +
                ", TypeImg='" + TypeImg + '\'' +
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

    public String getDirection() {
        return Direction;
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

    public String getJsonObject() {
        return this.jsonObject;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}