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

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import nl.hnogames.domoticzapi.R;

public class LoginInfo implements Serializable {
    // private JSONObject jsonObject;
    private int Rights;
    private String Status;
    private String Title;
    private String User;
    private String Version;
    private JSONObject json;

    public LoginInfo(){}

    public LoginInfo(JSONObject row) throws JSONException {
        json = row;
        // this.setJsonObject(row);
        if (row.has("rights"))
            Rights = row.getInt("rights");
        if (row.has("status"))
            Status = row.getString("status");
        if (row.has("title"))
            Title = row.getString("title");
        if (row.has("user"))
            User = row.getString("user");
        if (row.has("version"))
            Version = row.getString("version");
    }

    @Override
    public String toString() {
        return "LoginInfo{" +
                "Rights=" + Rights +
                ", Status='" + Status +
                ", Title='" + Title +
                ", User=" + User +
                "'}";
    }

    public int getRights() {
        return Rights;
    }

    public void setRights(int rights) {
        Rights = rights;
    }

    public String getRightsValue(Context context) {
        switch (Rights) {
            case 0:
                return context.getString(R.string.user_viewer);
            case 1:
                return context.getString(R.string.user_user);
            case 2:
                return context.getString(R.string.user_admin);
        }
        return "";
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUser() {
        return User;
    }

    public String getVersion() {
        return Version;
    }

    public void setVersion(String version) {
        Version = version;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
}