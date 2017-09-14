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

import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class GraphPointInfo {

    private JSONObject jsonObject;
    private String dateTime;
    private String hu;
    private String ba;
    private float se = Float.NaN;

    private float te = Float.NaN;
    private float ta = Float.NaN;
    private float tm = Float.NaN;
    boolean hasTemperatureRange = false;

    private String v;
    private String v2;
    private String r1;
    private String r2;
    private String eg;//energie levering
    private String eu;//energie usage

    private String vMin;
    private String vMax;
    boolean hasPercentageRange = false;

    private String c;
    private String di;
    private String gu;
    private String sp;
    private String uvi;
    private String mm;
    private String u;
    private String co2_min;
    private String co2_max;
    private String co2;

    private String lux;
    private String lux_min;
    private String lux_max;
    private String lux_avg;

    public GraphPointInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("te"))
            te = (float) row.optDouble("te");
        if (row.has("ta")) {
            ta = (float) row.optDouble("ta");
            if (row.has("tm"))
                tm = (float) row.optDouble("tm");
        }
        else
            hasTemperatureRange=false;
        if (row.has("se"))
            se = (float) row.optDouble("se");
        if (row.has("d"))
            dateTime = row.getString("d");
        if (row.has("v"))
            v = row.getString("v");
        else if (row.has("v_avg")) {
            hasPercentageRange=true;
            v = row.getString("v_avg");
            vMin = row.getString("v_min");
            vMax = row.getString("v_max");
        }

        if (row.has("lux"))
            lux = row.getString("lux");
        if (row.has("lux_min"))
            lux_min = row.getString("lux_min");
        if (row.has("lux_max"))
            lux_max = row.getString("lux_max");
        if (row.has("lux_avg"))
            lux_avg = row.getString("lux_avg");

        if (row.has("v2"))
            v2 = row.getString("v2");
        if (row.has("r1"))
            r1 = row.getString("r1");
        if (row.has("r2"))
            r2 = row.getString("r2");
        if (row.has("eg"))
            eg = row.getString("eg");
        if (row.has("eu"))
            eu = row.getString("eu");
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

        if (row.has("co2"))
            co2 = row.getString("co2");
        if (row.has("co2_max"))
            co2_max = row.getString("co2_max");
        if (row.has("co2_min"))
            co2_min = row.getString("co2_min");
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

    public String getLux() {
        return lux;
    }
    public String getLuxMin() {
        return lux_min;
    }
    public String getLuxMax() {
        return lux_max;
    }
    public String getLuxAvg() {
        return lux_avg;
    }

    public String getValue() {
        return v;
    }
    public String getSecondValue() {
        return v2;
    }

    public String getPowerReturn() {
        return r1;
    }
    public String getSecondPowerReturn() {
        return r2;
    }

    public String getPowerUsage() {
        return eu;
    }
    public String getPowerDelivery() {
        return eg;
    }

    public String getValueMin() {
        return vMin;
    }
    public String getValueMax() {
        return vMax;
    }
    public boolean hasValueRange() {
        return hasPercentageRange;
    }

    public float getTemperature() {
        if(!hasTemperatureRange)
            return te;
        else
            return ta;
    }

    public float getTemperatureMin() {
        return tm;
    }

    public float getTemperatureMax() {
        return te;
    }

    public boolean hasTemperatureRange() {
        return hasTemperatureRange;
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

    public String getCo2() {
        return co2;
    }

    public String getCo2Max() {
        return co2_max;
    }

    public String getCo2Min() {
        return co2_min;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

}