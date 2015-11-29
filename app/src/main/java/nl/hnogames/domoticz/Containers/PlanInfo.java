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

public class PlanInfo {

    JSONObject jsonObject;

    int devices;
    String name;
    int order;
    int idx;

    public PlanInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        devices = row.getInt("Devices");
        name = row.getString("Name");
        order = row.getInt("Order");
        idx = row.getInt("idx");
    }

    @Override
    public String toString() {
        return "PlanInfo{" +
                "idx=" + idx +
                ", order='" + order +
                "', name='" + name +
                "', devices='" + devices +
                "', json='" + jsonObject +
                "'}";
    }

    public String getName() {
        return name;
    }

    public int getIdx() {
        return idx;
    }

    public int getDevices() {
        return devices;
    }

    public int getOrder() {
        return order;
    }
}