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
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class GraphPointInfo {

    private JSONObject jsonObject;
    private String dateTime;
    private String hu;
    private String ba;
    private float te = Float.NaN;
    private float se = Float.NaN;
    private String v;
    private String c;
    private String di;
    private String gu;
    private String sp;
    private String uvi;
    private String mm;
    private String u;

    public GraphPointInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("te"))
            te = (float) row.optDouble("te");
        if (row.has("se"))
            se = (float) row.optDouble("se");
        if (row.has("d"))
            dateTime = row.getString("d");
        if (row.has("v"))
            v = row.getString("v");
        else if (row.has("v_avg"))
            v = row.getString("v_avg");
        if (row.has("c"))
            c = row.getString("c");
        if (row.has("hu"))
            hu = row.getString("hu");
        if (row.has("ba"))
            ba = row.getString("ba");
        if (row.has("sp"))
            sp = row.getString("sp");
        if (row.has("di"))
            di = row.getString("di");
        if (row.has("gu"))
            gu = row.getString("gu");
        if (row.has("uvi"))
            uvi = row.getString("uvi");
        if (row.has("u"))
            u = row.getString("u");
        else if (row.has("u_max"))
            u = row.getString("u_max");
        if (row.has("mm"))
            mm = row.getString("mm");
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

    public String getPercentage() {
        return v;
    }

    public float getTemperature() {
        return te;
    }

    public float getSetPoint() {
        return se;
    }

    public String getHumidity() {
        return hu;
    }

    public String getSunPower() {
        return uvi;
    }

    public String getCounter() {
        return c;
    }

    public String getSpeed() {
        return sp;
    }

    public String getDirection() {
        return di;
    }

    public String getRain() {
        return mm;
    }

    public String getUsage() {
        return u;
    }

    public String getBarometer() {
        return ba;
    }

    public String getDateTime() {
        return dateTime;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

}