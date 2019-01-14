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

import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class SwitchTimerInfo {

    JSONObject jsonObject;
    int Type = 0;
    String Date;
    String Time;
    String Active;
    int idx = 0;
    int Cmd = 0;
    int Days = 0;
    int MDay = 0;
    int Month = 0;
    int Occurence = 0;
    boolean Randomness = false;

    public SwitchTimerInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("Date"))
            Date = row.getString("Date");
        if (row.has("Active"))
            Active = row.getString("Active");
        if (row.has("Time"))
            Active = row.getString("Time");
        if (row.has("Type"))
            Type = row.getInt("Type");
        if (row.has("MDay"))
            MDay = row.getInt("MDay");
        if (row.has("Days"))
            Days = row.getInt("Days");
        if (row.has("Cmd"))
            Cmd = row.getInt("Cmd");
        if (row.has("Month"))
            Month = row.getInt("Month");
        if (row.has("Occurence"))
            Occurence = row.getInt("Occurence");
        if (row.has("Randomness"))
            Randomness = row.getBoolean("Randomness");

        idx = row.getInt("idx");
    }

    public String getDate() {
        return Date;
    }

    public String getActive() {
        return Active;
    }

    public String getTime() {
        return Time;
    }

    public int getIdx() {
        return idx;
    }

    public int getType() {
        return Type;
    }

    public int getCmd() {
        return Cmd;
    }

    public int getMonthDay() {
        return MDay;
    }

    public int getDays() {
        return Days;
    }

    public int getMonth() {
        return Month;
    }

    public int getOccurence() {
        return Occurence;
    }

    public char[] getDaysBinary() {
        String binary = Integer.toBinaryString(Days);
        if(!UsefulBits.isEmpty(binary))
            return binary.toCharArray();
        else return null;
    }

    public boolean getRandomness() {
        return Randomness;
    }

}