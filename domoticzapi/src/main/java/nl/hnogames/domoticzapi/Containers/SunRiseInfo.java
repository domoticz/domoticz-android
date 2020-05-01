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

public class SunRiseInfo implements Serializable {
    private String AstrTwilightEnd;
    private String AstrTwilightStart;
    private String CivTwilightEnd;
    private String CivTwilightStart;
    private String DayLength;
    private String NautTwilightEnd;
    private String NautTwilightStart;
    private String ServerTime;
    private String SunAtSouth;
    private String Sunrise;
    private String Sunset;

    public SunRiseInfo(JSONObject row) throws JSONException {
        if (row.has("AstrTwilightEnd"))
            AstrTwilightEnd = (row.getString("AstrTwilightEnd"));
        if (row.has("AstrTwilightStart"))
            AstrTwilightStart = (row.getString("AstrTwilightStart"));
        if (row.has("CivTwilightEnd"))
            CivTwilightEnd = (row.getString("CivTwilightEnd"));
        if (row.has("CivTwilightStart"))
            CivTwilightStart = (row.getString("CivTwilightStart"));
        if (row.has("DayLength"))
            DayLength = (row.getString("DayLength"));
        if (row.has("NautTwilightEnd"))
            NautTwilightEnd = (row.getString("NautTwilightEnd"));
        if (row.has("NautTwilightStart"))
            NautTwilightStart = (row.getString("NautTwilightStart"));
        if (row.has("ServerTime"))
            ServerTime = (row.getString("ServerTime"));
        if (row.has("SunAtSouth"))
            SunAtSouth = (row.getString("SunAtSouth"));
        if (row.has("Sunrise"))
            Sunrise = (row.getString("Sunrise"));
        if (row.has("Sunset"))
            Sunset = (row.getString("Sunset"));
    }

    public String getAstrTwilightEnd() {
        return AstrTwilightEnd;
    }

    public void setAstrTwilightEnd(String astrTwilightEnd) {
        AstrTwilightEnd = astrTwilightEnd;
    }

    public String getAstrTwilightStart() {
        return AstrTwilightStart;
    }

    public void setAstrTwilightStart(String astrTwilightStart) {
        AstrTwilightStart = astrTwilightStart;
    }

    public String getCivTwilightEnd() {
        return CivTwilightEnd;
    }

    public void setCivTwilightEnd(String civTwilightEnd) {
        CivTwilightEnd = civTwilightEnd;
    }

    public String getCivTwilightStart() {
        return CivTwilightStart;
    }

    public void setCivTwilightStart(String civTwilightStart) {
        CivTwilightStart = civTwilightStart;
    }

    public String getDayLength() {
        return DayLength;
    }

    public void setDayLength(String dayLength) {
        DayLength = dayLength;
    }

    public String getNautTwilightEnd() {
        return NautTwilightEnd;
    }

    public void setNautTwilightEnd(String nautTwilightEnd) {
        NautTwilightEnd = nautTwilightEnd;
    }

    public String getNautTwilightStart() {
        return NautTwilightStart;
    }

    public void setNautTwilightStart(String nautTwilightStart) {
        NautTwilightStart = nautTwilightStart;
    }

    public String getServerTime() {
        return ServerTime;
    }

    public void setServerTime(String serverTime) {
        ServerTime = serverTime;
    }

    public String getSunAtSouth() {
        return SunAtSouth;
    }

    public void setSunAtSouth(String sunAtSouth) {
        SunAtSouth = sunAtSouth;
    }

    public String getSunrise() {
        return Sunrise;
    }

    public void setSunrise(String sunrise) {
        Sunrise = sunrise;
    }

    public String getSunset() {
        return Sunset;
    }

    public void setSunset(String sunset) {
        Sunset = sunset;
    }
}