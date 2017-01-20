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
import java.text.SimpleDateFormat;
import java.util.Date;

public class SceneInfo implements Comparable, Serializable {

    private final boolean isProtected;
    private String jsonObject;

    private int favorite;
    private int hardwareID;
    private String lastUpdate;
    private String name;
    private String offAction;
    private String onAction;
    private String status;
    private Boolean timers;
    private String type;
    private int idx;

    public SceneInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has("Favorite"))
            favorite = row.getInt("Favorite");

        isProtected = row.getBoolean("Protected");
        if (row.has("HardwareID"))
            hardwareID = row.getInt("HardwareID");
        if (row.has("LastUpdate"))
            lastUpdate = row.getString("LastUpdate");
        if (row.has("Name"))
            name = row.getString("Name");
        if (row.has("OffAction"))
            offAction = row.getString("OffAction");
        if (row.has("OnAction"))
            onAction = row.getString("OnAction");
        if (row.has("Status"))
            status = row.getString("Status");
        if (row.has("Timers"))
            timers = row.getBoolean("Timers");
        if (row.has("Type"))
            type = row.getString("Type");

        idx = row.getInt("idx");
    }

    @Override
    public String toString() {
        return "SceneInfo{" +
                "isProtected=" + isProtected +
                ", jsonObject=" + jsonObject +
                ", favorite=" + favorite +
                ", hardwareID=" + hardwareID +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", name='" + name + '\'' +
                ", offAction='" + offAction + '\'' +
                ", onAction='" + onAction + '\'' +
                ", status='" + status + '\'' +
                ", timers=" + timers +
                ", type='" + type + '\'' +
                ", idx=" + idx +
                '}';
    }

    public boolean isProtected() {
        return isProtected;
    }

    public int getFavorite() {
        return favorite;
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = false;
        if (this.favorite == 1) favorite = true;
        return favorite;
    }

    public void setFavoriteBoolean(boolean favorite) {
        if (favorite) this.favorite = 1;
        else this.favorite = 0;
    }

    public int getHardwareID() {
        return hardwareID;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public Date getLastUpdateDateTime() {
        //2016-01-30 12:48:37
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(lastUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getOffAction() {
        return offAction;
    }

    public String getOnAction() {
        return onAction;
    }

    public boolean getStatusInBoolean() {
        return status.equalsIgnoreCase("on");
    }

    public String getStatusInString() {
        return status;
    }

    public Boolean isTimers() {
        return timers;
    }

    public String getType() {
        return type;
    }

    public int getIdx() {
        return idx;
    }

    public String getJsonObject() {
        return this.jsonObject;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((SceneInfo) another).getName());
    }
}