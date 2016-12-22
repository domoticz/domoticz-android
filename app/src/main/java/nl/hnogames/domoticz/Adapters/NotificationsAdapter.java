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

package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.NotificationInfo;

public class NotificationsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NotificationInfo> data = null;

    private SharedPrefUtil mSharedPrefs;

    public NotificationsAdapter(Context context,
                                ArrayList<NotificationInfo> data) {
        super();
        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (data != null) {
            ViewHolder holder;
            int layoutResourceId;

            NotificationInfo mNotificationInfo = data.get(position);

            if (mNotificationInfo != null) {
                holder = new ViewHolder();
                layoutResourceId = R.layout.timer_row;
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);

                holder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
                holder.switch_status = (TextView) convertView.findViewById(R.id.switch_battery_level);
                holder.signal_level = (TextView) convertView.findViewById(R.id.switch_signal_level);

                holder.switch_name.setText(mNotificationInfo.getCustomMessage());
                String priority = "";
                if (mNotificationInfo.getPriority() == 0)
                    priority = context.getString(R.string.priority) + ": " + context.getString(R.string.normal);
                else if (mNotificationInfo.getPriority() == 1)
                    priority = context.getString(R.string.priority) + ": " + context.getString(R.string.high);
                else if (mNotificationInfo.getPriority() == 2)
                    priority = context.getString(R.string.priority) + ": " + context.getString(R.string.emergency);
                else if (mNotificationInfo.getPriority() == -1)
                    priority = context.getString(R.string.priority) + ": " + context.getString(R.string.low);
                else if (mNotificationInfo.getPriority() == -2)
                    priority = context.getString(R.string.priority) + ": " + context.getString(R.string.verylow);

                String type = "";
                if (UsefulBits.isEmpty(mNotificationInfo.getActiveSystems()))
                    type = context.getString(R.string.allsystems);
                else
                    type += context.getString(R.string.systems) + ": " + mNotificationInfo.getActiveSystems().replace(";", ", ");

                holder.switch_name.setText(mNotificationInfo.getCustomMessage());
                holder.switch_status.setText(priority);
                holder.signal_level.setText(type);
                convertView.setTag(holder);
            }

            return convertView;
        }
        return null;
    }

    static class ViewHolder {
        TextView switch_name, signal_level, switch_status;
    }
}