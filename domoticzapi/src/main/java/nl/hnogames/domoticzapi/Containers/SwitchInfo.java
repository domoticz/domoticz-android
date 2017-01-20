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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Domoticz;

public class SwitchInfo {

    private static final String UNKNOWN = "Unknown";
    private final String TAG = SwitchInfo.class.getSimpleName();
    private JSONObject jsonObject;
    private String IsDimmer;
    private String Name;
    private String SubType;
    private String type;
    private String TypeImg;
    private int idx;
    private String Timers;

    private int switchTypeVal;
    private String switchType;


    public SwitchInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        try {
            IsDimmer = row.getString("IsDimmer");
        } catch (Exception e) {
            exceptionHandling(e);
            IsDimmer = "False";
        }

        try {
            if (row.has("Timers"))
                Timers = row.getString("Timers");
        } catch (Exception e) {
            Timers = "False";
        }
        try {
            Name = row.getString("Name");
        } catch (Exception e) {
            exceptionHandling(e);
            Name = UNKNOWN;
        }
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        try {
            SubType = row.getString("SubType");
        } catch (Exception e) {
            exceptionHandling(e);
            SubType = UNKNOWN;
        }
        try {
            type = row.getString("Type");
        } catch (Exception e) {
            exceptionHandling(e);
            type = UNKNOWN;
        }
        try {
            idx = row.getInt("idx");
        } catch (Exception e) {
            exceptionHandling(e);
            idx = Domoticz.DOMOTICZ_FAKE_ID;
        }

        try {
            if (row.has("SwitchType"))
                switchType = row.getString("SwitchType");
        } catch (Exception e) {
            switchType = UNKNOWN;
            exceptionHandling(e);
        }
        try {
            if (row.has("SwitchTypeVal"))
                switchTypeVal = row.getInt("SwitchTypeVal");
        } catch (Exception e) {
            switchTypeVal = 999999;
            exceptionHandling(e);
        }
    }

    @Override
    public String toString() {
        return "SwitchInfo{" +
                "IsDimmer='" + IsDimmer + '\'' +
                ", name='" + Name + '\'' +
                ", SubType='" + SubType + '\'' +
                ", type='" + type + '\'' +
                ", idx=" + idx +
                '}';
    }

    public String getIsDimmerString() {
        return IsDimmer;
    }

    public boolean getIsDimmerBoolean() {
        return IsDimmer.equalsIgnoreCase("true") ? true : false;
    }

    public void setIsDimmer(String isDimmer) {
        IsDimmer = isDimmer;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSubType() {
        return SubType;
    }

    public void setSubType(String subType) {
        SubType = subType;
    }

    public String getTimers() {
        return Timers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    private void exceptionHandling(Exception error) {
        Log.e(TAG, "Exception occurred");
        error.printStackTrace();
    }


    public int getSwitchTypeVal() {
        return switchTypeVal;
    }

    public String getSwitchType() {
        return switchType;
    }
}