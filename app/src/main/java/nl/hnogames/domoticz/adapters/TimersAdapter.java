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

package nl.hnogames.domoticz.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;

public class TimersAdapter extends BaseAdapter {
    private SharedPrefUtil mSharedPrefs;
    private Context context;
    private ArrayList<SwitchTimerInfo> data;

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


    @SuppressLint("DefaultLocale")
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
                    if ((convertView.findViewById(R.id.card_global_wrapper)) != null)
                        convertView.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
                    if ((convertView.findViewById(R.id.row_wrapper)) != null)
                        (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.color.card_background_dark));
                    if ((convertView.findViewById(R.id.row_global_wrapper)) != null)
                        (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
                }

                holder.switch_name = convertView.findViewById(R.id.switch_name);
                holder.switch_status = convertView.findViewById(R.id.switch_battery_level);
                holder.signal_level = convertView.findViewById(R.id.switch_signal_level);
                holder.switch_name.setText(mSwitchTimerInfo.getActive());
                String commando = "";
                if (mSwitchTimerInfo.getCmd() == 0)
                    commando += context.getString(R.string.command) + ": " + context.getString(R.string.button_state_on);
                else
                    commando += context.getString(R.string.command) + ": " + context.getString(R.string.button_state_off);

                String occurence = "";
                String days = "";
                String type = "";
                String month = "";

                char[] daysBinary = UsefulBits.Reverse(mSwitchTimerInfo.getDaysBinary());

                if (mSwitchTimerInfo.getType() == 0)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.timer_before_sunrise));
                else if (mSwitchTimerInfo.getType() == 1)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.timer_after_sunrise));
                else if (mSwitchTimerInfo.getType() == 2)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.timer_ontime));
                else if (mSwitchTimerInfo.getType() == 3)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.timer_before_sunset));
                else if (mSwitchTimerInfo.getType() == 4)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.timer_after_sunset));
                else if (mSwitchTimerInfo.getType() == 5)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.timer_fixed));
                else if (mSwitchTimerInfo.getType() == 6)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.odd_day_numbers));
                else if (mSwitchTimerInfo.getType() == 7)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.even_day_numbers));
                else if (mSwitchTimerInfo.getType() == 8)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.odd_week_numbers));
                else if (mSwitchTimerInfo.getType() == 9)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.even_week_numbers));
                else if (mSwitchTimerInfo.getType() == 10)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.monthly));
                else if (mSwitchTimerInfo.getType() == 11)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.monthly_weekday));
                else if (mSwitchTimerInfo.getType() == 12)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.yearly));
                else if (mSwitchTimerInfo.getType() == 13)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.yearly_weekday));
                else if (mSwitchTimerInfo.getType() == 14)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_sun_at_south));
                else if (mSwitchTimerInfo.getType() == 15)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_sun_at_south));
                else if (mSwitchTimerInfo.getType() == 16)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_civil_twilight_start));
                else if (mSwitchTimerInfo.getType() == 17)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_civil_twilight_start));
                else if (mSwitchTimerInfo.getType() == 18)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_civil_twilight_end));
                else if (mSwitchTimerInfo.getType() == 19)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_civil_twilight_end));
                else if (mSwitchTimerInfo.getType() == 20)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_nautical_twilight_start));
                else if (mSwitchTimerInfo.getType() == 21)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_nautical_twilight_start));
                else if (mSwitchTimerInfo.getType() == 22)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_nautical_twilight_end));
                else if (mSwitchTimerInfo.getType() == 23)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_nautical_twilight_end));
                else if (mSwitchTimerInfo.getType() == 24)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_austronomical_twilight_start));
                else if (mSwitchTimerInfo.getType() == 25)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_austronomical_twilight_start));
                else if (mSwitchTimerInfo.getType() == 26)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.before_austronomical_twilight_end));
                else if (mSwitchTimerInfo.getType() == 27)
                    type += String.format("%s: %s", context.getString(R.string.type), context.getString(R.string.after_austronomical_twilight_end));
                else
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.notapplicable);

                if (mSwitchTimerInfo.getDate() != null && mSwitchTimerInfo.getDate().length() > 0)
                    holder.switch_name.setText(String.format("%s | %s",
                        holder.switch_name.getText(), mSwitchTimerInfo.getDate()));
                else if (mSwitchTimerInfo.getMonthDay() > 0)
                    days = String.format("%s %d", context.getString(R.string.button_status_day).toLowerCase(), mSwitchTimerInfo.getMonthDay());
                else {
                    if (mSwitchTimerInfo.getDays() == 128)
                        holder.switch_name.setText(String.format("%s | %s",
                            holder.switch_name.getText(), context.getString(R.string.timer_every_days)));
                    else if (mSwitchTimerInfo.getDays() == 512)
                        holder.switch_name.setText(String.format("%s | %s",
                            holder.switch_name.getText(), context.getString(R.string.timer_weekend)));
                    else if (mSwitchTimerInfo.getDays() == 256)
                        holder.switch_name.setText(String.format("%s | %s",
                            holder.switch_name.getText(), context.getString(R.string.timer_working_days)));

                    if (mSwitchTimerInfo.getOccurence() > 0) {
                        if (mSwitchTimerInfo.getOccurence() == 1)
                            occurence = context.getString(R.string.first);
                        else if (mSwitchTimerInfo.getOccurence() == 2)
                            occurence = context.getString(R.string.second);
                        else if (mSwitchTimerInfo.getOccurence() == 3)
                            occurence = context.getString(R.string.third);
                        else if (mSwitchTimerInfo.getOccurence() == 4)
                            occurence = context.getString(R.string.fourth);
                        else if (mSwitchTimerInfo.getOccurence() == 5)
                            occurence = context.getString(R.string.last);
                    }

                    if (mSwitchTimerInfo.getDays() > 0) {
                        try {
                            for (int i = 0; i < 7; i++) {
                                if (daysBinary[i] == '1') {
                                    if (UsefulBits.isEmpty(days))
                                        days = UsefulBits.getWeekDay(i);
                                    else
                                        days += ", " + UsefulBits.getWeekDay(i);
                                }
                            }
                        } catch (Exception ignored) {
                        }
                    }
                    if (mSwitchTimerInfo.getMonth() > 0)
                        month = UsefulBits.getMonth(mSwitchTimerInfo.getMonth());
                }

                if (!UsefulBits.isEmpty(occurence) || !UsefulBits.isEmpty(days) || !UsefulBits.isEmpty(month))
                    holder.switch_name.setText(String.format("%s | %s %s %s",
                        holder.switch_name.getText(), occurence != null ? occurence.toLowerCase() : null,
                        days != null ? days.toLowerCase() : null,
                        month != null ? month.toLowerCase() : null));
                else
                    holder.switch_name.setText(String.format("%s", holder.switch_name.getText().toString().toLowerCase()));
                if (mSwitchTimerInfo.getRandomness())
                    holder.switch_status.setText(String.format("%s (random)", commando));
                else
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