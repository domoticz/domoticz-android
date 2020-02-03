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

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class NotificationInfo implements IMessage {
    private int idx = -1;
    private String title;
    private String text;
    private int priority;
    private Date date;

    public NotificationInfo(int idx, String title, String text, int priority, Date date) {
        this.idx = idx;
        this.title = title;
        this.text = text;
        this.priority = priority;
        this.date = date;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
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

    @Override
    public String getId() {
        return String.valueOf(getText().hashCode());
    }

    public String getText() {
        return text;
    }

    @Override
    public IUser getUser() {
        return new IUser() {
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

    @Override
    public Date getCreatedAt() {
        return date;
    }
}
