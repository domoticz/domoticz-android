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

import org.json.JSONException;
import org.json.JSONObject;

public class EventXmlInfo {
    JSONObject jsonObject;

    int id;
    String Name;
    String Status;
    String Xmlstatement;

    public EventXmlInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("name"))
            Name = row.getString("name");
        if (row.has("eventstatus"))
            Status = row.getString("eventstatus");
        if (row.has("xmlstatement"))
            Xmlstatement = row.getString("xmlstatement");
        id = row.getInt("id");
    }

    public String getName() {
        return Name;
    }
    public String getStatus() {
        return Status;
    }
    public String getXmlstatement() {
        return Xmlstatement;
    }

    public boolean getStatusBoolean() {
        if(getStatus().equals("1"))
            return true;
        else
            return false;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "EventXmlInfo{" +
                "id=" + id +
                ", Name='" + Name +"', " +
                "Status='" + Status +
                "'}";
    }
}