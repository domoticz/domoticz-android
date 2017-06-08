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

import android.media.Image;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CameraInfo implements Comparable, Serializable {
    private String jsonObject;
    private int devices;
    private String Name;
    private String Address;
    private String Username;
    private String Password;
    private String Port;
    private boolean Enabled;
    private String ImageURL;
    private String SnapShotURL;
    private int idx;

    public CameraInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Enabled") &&
                row.getString("Enabled").equals("true"))
            Enabled = true;
        else
            Enabled = false;
        if (row.has("Address"))
            Address = row.getString("Address");
        if (row.has("Password"))
            Password = row.getString("Password");
        if (row.has("Port"))
            Port = row.getString("Port");
        if (row.has("Username"))
            Username = row.getString("Username");
        if (row.has("ImageURL"))
            ImageURL = row.getString("ImageURL");
        idx = row.getInt("idx");
    }

    @Override
    public String toString() {
        return "CameraInfo{" +
                "idx=" + idx +
                ", Name='" + Name +
                "', Address='" + Address +
                "', ImageURL='" + ImageURL +
                ", Port='" + Port +
                ", Username='" + Username +
                ", Password='" + Password +
                ", Enabled=" + Enabled +
                "'}";
    }

    public String getName() {
        return Name;
    }

    public boolean getEnabled() {
        return Enabled;
    }

    public String getSnapShotURL() {
        return SnapShotURL;
    }

    public void setSnapShotURL(String url) {
        SnapShotURL = url;
    }

    public String getAddress() {
        return Address;
    }

    public String getTotalImageURL() {
        return "http://" + Address + ":"+Port + "/" + ImageURL;
    }
    public String getImageURL() {
        return ImageURL;
    }

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }

    public String getPort() {
        return Port;
    }

    public int getIdx() {
        return idx;
    }

    public int getDevices() {
        return devices;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}