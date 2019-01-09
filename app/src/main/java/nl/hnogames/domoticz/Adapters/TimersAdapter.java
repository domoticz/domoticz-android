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

import androidx.core.content.ContextCompat;
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
                else if (mSwitchTimerInfo.getType() == 14)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_sun_at_south);
                else if (mSwitchTimerInfo.getType() == 15)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_sun_at_south);
                else if (mSwitchTimerInfo.getType() == 16)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_civil_twilight_start);
                else if (mSwitchTimerInfo.getType() == 17)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_civil_twilight_start);
                else if (mSwitchTimerInfo.getType() == 18)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_civil_twilight_end);
                else if (mSwitchTimerInfo.getType() == 19)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_civil_twilight_end);
                else if (mSwitchTimerInfo.getType() == 20)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_nautical_twilight_start);
                else if (mSwitchTimerInfo.getType() == 21)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_nautical_twilight_start);
                else if (mSwitchTimerInfo.getType() == 22)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_nautical_twilight_end);
                else if (mSwitchTimerInfo.getType() == 23)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_nautical_twilight_end);
                else if (mSwitchTimerInfo.getType() == 24)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_austronomical_twilight_start);
                else if (mSwitchTimerInfo.getType() == 25)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_austronomical_twilight_start);
                else if (mSwitchTimerInfo.getType() == 26)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.before_austronomical_twilight_end);
                else if (mSwitchTimerInfo.getType() == 27)
                    type += context.getString(R.string.type) + ": " + context.getString(R.string.after_austronomical_twilight_end);
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

                    else if (mSwitchTimerInfo.getOccurence() > 0 && mSwitchTimerInfo.getDays() < 8) {
                        String occurence = "";
                        String days = "";

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

                        if (mSwitchTimerInfo.getDays() == 1)
                            days = context.getString(R.string.monday);
                        else if (mSwitchTimerInfo.getDays() == 2)
                            days = context.getString(R.string.tuesday);
                        else if (mSwitchTimerInfo.getDays() == 3)
                            days = context.getString(R.string.wednesday);
                        else if (mSwitchTimerInfo.getDays() == 4)
                            days = context.getString(R.string.thursday);
                        else if (mSwitchTimerInfo.getDays() == 5)
                            days = context.getString(R.string.friday);
                        else if (mSwitchTimerInfo.getDays() == 6)
                            days = context.getString(R.string.saturday);
                        else if (mSwitchTimerInfo.getDays() == 7)
                            days = context.getString(R.string.sunday);

                        String month = "";
                        if (mSwitchTimerInfo.getMonth() == 1)
                            month = context.getString(R.string.January);
                        else if (mSwitchTimerInfo.getMonth() == 2)
                            month = context.getString(R.string.February);
                        else if (mSwitchTimerInfo.getMonth() == 3)
                            month = context.getString(R.string.March);
                        else if (mSwitchTimerInfo.getMonth() == 4)
                            month = context.getString(R.string.April);
                        else if (mSwitchTimerInfo.getMonth() == 5)
                            month = context.getString(R.string.May);
                        else if (mSwitchTimerInfo.getMonth() == 6)
                            month = context.getString(R.string.June);
                        else if (mSwitchTimerInfo.getMonth() == 7)
                            month = context.getString(R.string.July);
                        else if (mSwitchTimerInfo.getMonth() == 8)
                            month = context.getString(R.string.August);
                        else if (mSwitchTimerInfo.getMonth() == 9)
                            month = context.getString(R.string.September);
                        else if (mSwitchTimerInfo.getMonth() == 10)
                            month = context.getString(R.string.October);
                        else if (mSwitchTimerInfo.getMonth() == 11)
                            month = context.getString(R.string.November);
                        else if (mSwitchTimerInfo.getMonth() == 12)
                            month = context.getString(R.string.December);

                        holder.switch_name.setText(holder.switch_name.getText() + " | " + occurence + " " + days + " " + month);
                    } else
                        holder.switch_name.setText(holder.switch_name.getText() + " | " + context.getString(R.string.timer_other));
                }

                if (mSwitchTimerInfo.getRandomness())
                    holder.switch_status.setText(commando + " (random)");
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