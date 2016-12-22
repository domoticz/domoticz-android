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

public class EventInfo implements Comparable, Serializable {
    @SuppressWarnings({"SpellCheckingInspection", "FieldCanBeLocal"})
    private final String EVENT_STATUS = "eventstatus";
    @SuppressWarnings("FieldCanBeLocal")
    private final String NAME = "name";
    private String jsonObject;
    private int id;
    private String Name;
    private String Status;

    public EventInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has(NAME))
            Name = row.getString(NAME);
        if (row.has(EVENT_STATUS))
            Status = row.getString(EVENT_STATUS);
        id = row.getInt("id");
    }

    public String getName() {
        return Name;
    }

    public String getStatus() {
        return Status;
    }

    public boolean getStatusBoolean() {
        return getStatus().equals("1");
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "EventInfo{" +
                "id=" + id +
                ", Name='" + Name + "', " +
                "Status='" + Status +
                "'}";
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}