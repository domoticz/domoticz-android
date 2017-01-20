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

import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class ServerUpdateInfo implements Serializable {
    @SuppressWarnings("FieldCanBeLocal")
    private final String RESPONSE_SYSTEM_NAME = "SystemName";
    @SuppressWarnings("FieldCanBeLocal")
    private final String RESPONSE_HAVE_UPDATE = "HaveUpdate";
    @SuppressWarnings({"FieldCanBeLocal", "SpellCheckingInspection"})
    private final String RESPONSE_STATUS_CODE = "statuscode";
    @SuppressWarnings("FieldCanBeLocal")
    private final String RESPONSE_URL = "DomoticzUpdateURL";
    @SuppressWarnings("FieldCanBeLocal")
    private final String RESPONSE_REVISION = "revision";
    @SuppressWarnings("FieldCanBeLocal")
    private final String RESPONSE_REVISION_NEW = "Revision";
    @SuppressWarnings("FieldCanBeLocal")
    private final String STRING_CHANNEL = "channel=";
    @SuppressWarnings("FieldCanBeLocal")
    private final String STRING_TYPE = "&type=";
    String revision = "";
    String currentServerVersion = "";
    String systemName = "";
    String url = "";
    int statusCode = -1;
    String updateChannel = "";
    boolean haveUpdate;

    public ServerUpdateInfo(JSONObject row) throws JSONException {

        if (row.has(RESPONSE_REVISION))
            revision = row.getString(RESPONSE_REVISION);
        else if (row.has(RESPONSE_REVISION_NEW))
            revision = row.getString(RESPONSE_REVISION_NEW);

        //noinspection RedundantIfStatement
        if (row.has(RESPONSE_HAVE_UPDATE) && !row.getBoolean(RESPONSE_HAVE_UPDATE))
            haveUpdate = false;
        else haveUpdate = true;

        if (row.has(RESPONSE_SYSTEM_NAME))
            systemName = row.getString(RESPONSE_SYSTEM_NAME);

        if (row.has(RESPONSE_STATUS_CODE)) {
            try {
                statusCode = row.getInt(RESPONSE_STATUS_CODE);
            } catch (Exception ignored) {
            }
        }

        if (row.has(RESPONSE_URL)) {
            url = row.getString(RESPONSE_URL);
            updateChannel = extractUpdateChannel(url);
        }
    }

    private String extractUpdateChannel(String string) {
        if (UsefulBits.isEmpty(string))
            return null;

        if (string.contains(STRING_CHANNEL) && string.contains(STRING_TYPE))
            return string.substring(string.indexOf(STRING_CHANNEL) + STRING_CHANNEL.length(), string.indexOf(STRING_TYPE));
        else if (string.contains(STRING_CHANNEL))
            return string.substring(string.indexOf(STRING_CHANNEL) + STRING_CHANNEL.length());
        else
            return null;
    }

    @Override
    public String toString() {
        return "ServerUpdateInfo{" +
                "revision='" + revision + '\'' +
                ", currentServerVersion='" + currentServerVersion + '\'' +
                ", systemName='" + systemName + '\'' +
                ", url='" + url + '\'' +
                ", statusCode=" + statusCode +
                ", updateChannel='" + updateChannel + '\'' +
                ", haveUpdate=" + haveUpdate +
                '}';
    }

    public boolean isUpdateAvailable() {
        return haveUpdate;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        haveUpdate = updateAvailable;
    }

    public String getUpdateRevisionNumber() {
        return revision;
    }

    public void setUpdateRevisionNumber(String revision) {
        this.revision = revision;
    }

    public String getSystemName() {
        return systemName;
    }

    public String getUrl() {
        return url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getUpdateChannel() {
        return updateChannel;
    }

    public String getCurrentServerVersion() {
        return currentServerVersion;
    }

    public void setCurrentServerVersion(String currentServerVersion) {
        this.currentServerVersion = currentServerVersion;
    }
}