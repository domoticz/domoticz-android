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

public class SwitchLogInfo {
    JSONObject jsonObject;
    int MaxDimLevel = 0;
    int Level = 0;
    String Status;
    String Date;
    String Data;
    int idx;

    public SwitchLogInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("Status"))
            Status = row.getString("Status");
        if (row.has("Date"))
            Date = row.getString("Date");
        if (row.has("Data"))
            Data = row.getString("Data");
        if (row.has("MaxDimLevel"))
            MaxDimLevel = row.getInt("MaxDimLevel");
        if (row.has("Level"))
            Level = row.getInt("Level");

        idx = row.getInt("idx");
    }

    public String getData() {
        return Data;
    }

    public String getStatus() {
        return Status;
    }

    public String getDate() {
        return Date;
    }

    public int getIdx() {
        return idx;
    }

    public int getLevel() {
        return Level;
    }

    public int getMaxDimLevel() {
        return MaxDimLevel;
    }

}