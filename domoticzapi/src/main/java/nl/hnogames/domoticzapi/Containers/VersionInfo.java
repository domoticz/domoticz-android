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

import android.icu.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class VersionInfo implements Serializable {
    private boolean HaveUpdate = true;
    private boolean UseUpdate = true;
    private String build_time;
    private Date buildTimeDateTime;
    private String dzvents_version;
    private String hash;
    private String python_version;
    private String version;

    public VersionInfo(JSONObject row) throws JSONException {
        if (row.has("HaveUpdate"))
            HaveUpdate = row.getBoolean("HaveUpdate");
        if (row.has("UseUpdate"))
            UseUpdate = row.getBoolean("UseUpdate");
        if (row.has("build_time"))
            build_time = row.getString("build_time");
        if (row.has("dzvents_version"))
            dzvents_version = row.getString("dzvents_version");
        if (row.has("hash"))
            hash = row.getString("hash");
        if (row.has("version"))
            version = row.getString("version");
        if (row.has("version"))
            version = row.getString("version");
        if(!UsefulBits.isEmpty(build_time)) {
            try {
                buildTimeDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(build_time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isHaveUpdate() {
        return HaveUpdate;
    }

    public void setHaveUpdate(boolean haveUpdate) {
        HaveUpdate = haveUpdate;
    }

    public boolean isUseUpdate() {
        return UseUpdate;
    }

    public void setUseUpdate(boolean useUpdate) {
        UseUpdate = useUpdate;
    }

    public String getBuild_time() {
        return build_time;
    }

    public Date getBuildDate() {
        return buildTimeDateTime;
    }

    public void setBuild_time(String build_time) {
        this.build_time = build_time;
    }

    public String getDzvents_version() {
        return dzvents_version;
    }

    public void setDzvents_version(String dzvents_version) {
        this.dzvents_version = dzvents_version;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPython_version() {
        return python_version;
    }

    public void setPython_version(String python_version) {
        this.python_version = python_version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}