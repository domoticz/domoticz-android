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

public class LogInfo implements Serializable {

    private String jsonObject;
    private int level;
    private String message;

    public LogInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();

        if (row.has("level"))
            level = row.getInt("level");
        if (row.has("message"))
            message = row.getString("message");

    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "level=" + level +
                ", message='" + message + '}';
    }

    public int getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getJsonObject() {
        return this.jsonObject;
    }

}