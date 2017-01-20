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

public class UserInfo implements Serializable {
    // private JSONObject jsonObject;
    private boolean Enabled = true;
    private String Password;
    private int Rights;
    private String Username;
    private int idx;

    public UserInfo(String username, String password, int rights) {
        Password = password;
        Username = username;
        Rights = rights;
    }

    public UserInfo(JSONObject row) throws JSONException {
        // this.setJsonObject(row);
        if (row.has("Password"))
            setPassword(row.getString("Password"));
        if (row.has("Username"))
            setUsername(row.getString("Username"));
        if (row.has("idx"))
            setIdx(row.getInt("idx"));
        if (row.has("Rights"))
            setRights(row.getInt("Rights"));
        if (row.has("Password"))
            setPassword(row.getString("Password"));
        if (row.has("Enabled"))
            setEnabled(row.getBoolean("Enabled"));
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "idx=" + getIdx() +
                ", Username='" + getUsername() +
                ", Password='" + getPassword() +
                ", Enabled=" + isEnabled() +
                "'}";
    }

    public boolean isEnabled() {
        return Enabled;
    }

    public void setEnabled(boolean enabled) {
        Enabled = enabled;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
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

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}