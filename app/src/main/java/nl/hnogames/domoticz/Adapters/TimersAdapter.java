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
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;

public class TimersAdapter extends BaseAdapter {

    private static final String TAG = TimersAdapter.class.getSimpleName();

    private SharedPrefUtil mSharedPrefs;

    private Context context;
    private ArrayList<SwitchTimerInfo> data = null;

    public TimersAdapter(Context context,
                         ArrayList<SwitchTimerInfo> data) {
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

            SwitchTimerInfo mSwitchTimerInfo = data.get(position);

            if (mSwitchTimerInfo != null) {
                holder = new ViewHolder();
                layoutResourceId = R.layout.timer_row;
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);

                if (mSharedPrefs.darkThemeEnabled()) {
                    if ((convertView.findViewById(R.id.row_wrapper)) != null)
                        (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
                    if ((convertView.findViewById(R.id.row_global_wrapper)) != null)
                        (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
                }

                holder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
                holder.switch_status = (TextView) convertView.findViewById(R.id.switch_battery_level);
                holder.signal_level = (TextView) convertView.findViewById(R.id.switch_signal_level);
                holder.switch_name.setText(mSwitchTimerInfo.getActive());
                String commando = "";
                if (mSwitchTimerInfo.getCmd() == 0)
                    commando += context.getString(R.string.command) + ": " + context.getString(R.string.button_state_on);
                else
                    commando += context.getString(R.string.command) + ": " + context.getString(R.string.button_state_off);

                String type = "";
                if (mSwitchTimerInfo.getType() == 0)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.timer_before_sunrise);
                else if (mSwitchTimerInfo.getType() == 1)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.timer_after_sunrise);
                else if (mSwitchTimerInfo.getType() == 2)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.timer_ontime);
                else if (mSwitchTimerInfo.getType() == 3)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.timer_before_sunset);
                else if (mSwitchTimerInfo.getType() == 4)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.timer_after_sunset);
                else if (mSwitchTimerInfo.getType() == 5)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.timer_fixed);
                else if (mSwitchTimerInfo.getType() == 6)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.odd_day_numbers);
                else if (mSwitchTimerInfo.getType() == 7)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.even_day_numbers);
                else if (mSwitchTimerInfo.getType() == 8)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.odd_week_numbers);
                else if (mSwitchTimerInfo.getType() == 9)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.even_week_numbers);
                else if (mSwitchTimerInfo.getType() == 10)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.monthly);
                else if (mSwitchTimerInfo.getType() == 11)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.monthly_weekday);
                else if (mSwitchTimerInfo.getType() == 12)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.yearly);
                else if (mSwitchTimerInfo.getType() == 13)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.yearly_weekday);
                else
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.notapplicable);

                if (mSwitchTimerInfo.getDate() != null && mSwitchTimerInfo.getDate().length() > 0)
                    holder.switch_name.setText(holder.switch_name.getText() + " | " + mSwitchTimerInfo.getDate());
                else {
                    if (mSwitchTimerInfo.getDays() == 128)
                        holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_every_days));
                    else if (mSwitchTimerInfo.getDays() == 512)
                        holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_weekend));
                    else if (mSwitchTimerInfo.getDays() == 256)
                        holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_working_days));
                    else if (mSwitchTimerInfo.getDays() == 512)
                        holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_weekend));
                    else
                        holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_other));
                }

                holder.switch_status.setText(commando);
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