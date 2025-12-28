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

package nl.hnogames.domoticz.containers;

import java.util.Date;

import nl.hnogames.domoticz.utils.UsefulBits;

/**
 * Local lightweight replacement for chat message model to avoid external dependency.
 */
public class NotificationInfo implements Comparable<NotificationInfo> {
    public interface IUserLocal {
        String getId();
        String getName();
        String getAvatar();
    }

    private int idx = -1;
    private String title;
    private String text;
    private int priority;
    private Date date;
    private String systems;
    private boolean sendOurselves;
    private String deviceID;

    public NotificationInfo(int idx, String title, String text, int priority, Date date) {
        this.idx = idx;
        this.title = title;
        this.text = text;
        this.priority = priority;
        this.date = date;
    }

    public NotificationInfo(int idx, String title, String text, int priority, Date date, boolean send, String deviceID) {
        this.idx = idx;
        this.title = title;
        this.text = text;
        this.priority = priority;
        this.date = date;
        this.sendOurselves = send;
        this.deviceID = deviceID;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getTitle() {
        if (UsefulBits.isEmpty(title))
            return "";
        else
            return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getId() {
        return String.valueOf(getText().hashCode());
    }

    public String getText() {
        if (UsefulBits.isEmpty(text))
            return "";
        else
            return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public IUserLocal getUser() {
        if (sendOurselves) {
            return new IUserLocal() {
                @Override
                public String getId() {
                    return deviceID;
                }

                @Override
                public String getName() {
                    return "";
                }

                @Override
                public String getAvatar() {
                    return null;
                }
            };
        } else {
            return new IUserLocal() {
                @Override
                public String getId() {
                    return "654s6f84sef"; //dummy value
                }

                @Override
                public String getName() {
                    return "";
                }

                @Override
                public String getAvatar() {
                    return null;
                }
            };
        }
    }

    public Date getCreatedAt() {
        return date;
    }

    public String getSystems() {
        return systems;
    }

    public void setSystems(String systems) {
        this.systems = systems;
    }

    public boolean isSendOurselves() {
        return sendOurselves;
    }

    public void setSendOurselves(boolean sendOurselves) {
        this.sendOurselves = sendOurselves;
    }

    @Override
    public int compareTo(NotificationInfo o) {
        return getDate().compareTo(o.getDate());
    }
}
