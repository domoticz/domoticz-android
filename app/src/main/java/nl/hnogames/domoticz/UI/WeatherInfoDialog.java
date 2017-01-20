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

package nl.hnogames.domoticz.UI;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.WeatherInfo;

public class WeatherInfoDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private WeatherInfo info;
    private Switch favorite_switch;

    public WeatherInfoDialog(Context mContext,
                             WeatherInfo info,
                             int layout) {
        this.info = info;
        if ((new SharedPrefUtil(mContext)).darkThemeEnabled()) {
            mdb = new MaterialDialog.Builder(mContext)
                    .titleColorRes(R.color.white)
                    .contentColor(Color.WHITE) // notice no 'res' postfix for literal color
                    .dividerColorRes(R.color.white)
                    .backgroundColorRes(R.color.primary)
                    .positiveColorRes(R.color.white)
                    .neutralColorRes(R.color.white)
                    .negativeColorRes(R.color.white)
                    .widgetColorRes(R.color.white)
                    .buttonRippleColorRes(R.color.white);
        } else
            mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        //noinspection ConstantConditions
        mdb.customView(layout, wrapInScrollView)
                .theme((new SharedPrefUtil(mContext)).darkThemeEnabled() ? Theme.DARK : Theme.LIGHT)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void setWeatherInfo(WeatherInfo weather) {
        this.info = weather;
    }

    public void show() {
        mdb.title(info.getName());
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        TextView IDX_value = (TextView) view.findViewById(R.id.IDX_value);

        TextView weather_forecast_title = (TextView) view.findViewById(R.id.weather_forcast);
        TextView weather_humidity_title = (TextView) view.findViewById(R.id.weather_humidity);
        TextView weather_barometer_title = (TextView) view.findViewById(R.id.weather_barometer);
        TextView weather_dewpoint_title = (TextView) view.findViewById(R.id.weather_drewpoint);
        TextView weather_temperature_title = (TextView) view.findViewById(R.id.weather_temperature);
        TextView weather_chill_title = (TextView) view.findViewById(R.id.weather_chill);
        TextView weather_direction_title = (TextView) view.findViewById(R.id.weather_direction);
        TextView weather_speed_title = (TextView) view.findViewById(R.id.weather_speed);

        TextView weather_forecast = (TextView) view.findViewById(R.id.weather_forcast_value);
        TextView weather_humidity = (TextView) view.findViewById(R.id.weather_humidity_value);
        TextView weather_barometer = (TextView) view.findViewById(R.id.weather_barometer_value);
        TextView weather_dewpoint = (TextView) view.findViewById(R.id.weather_drewpoint_value);
        TextView weather_temperature = (TextView) view.findViewById(R.id.weather_temperature_value);
        TextView weather_chill = (TextView) view.findViewById(R.id.weather_chill_value);
        TextView weather_direction = (TextView) view.findViewById(R.id.weather_direction_value);
        TextView weather_speed = (TextView) view.findViewById(R.id.weather_speed_value);

        IDX_value.setText(String.valueOf(info.getIdx()));
        TextView LastUpdate_value = (TextView) view.findViewById(R.id.LastUpdate_value);
        LastUpdate_value.setText(info.getLastUpdate());

        favorite_switch = (Switch) view.findViewById(R.id.favorite_switch);
        favorite_switch.setChecked(info.getFavoriteBoolean());
        favorite_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });

        //data of weather object:
        if (!UsefulBits.isEmpty(info.getForecastStr()))
            weather_forecast.setText(info.getForecastStr());
        else {
            weather_forecast.setVisibility(View.GONE);
            weather_forecast_title.setVisibility(View.GONE);
        }
        if (!UsefulBits.isEmpty(info.getSpeed()))
            weather_speed.setText(info.getSpeed());
        else {
            weather_speed.setVisibility(View.GONE);
            weather_speed_title.setVisibility(View.GONE);
        }
        if (info.getDewPoint() > 0)
            weather_dewpoint.setText(String.valueOf(info.getDewPoint()));
        else {
            weather_dewpoint.setVisibility(View.GONE);
            weather_dewpoint_title.setVisibility(View.GONE);
        }
        if (info.getTemp() > 0)
            weather_temperature.setText(String.valueOf(info.getTemp()));
        else {
            weather_temperature.setVisibility(View.GONE);
            weather_temperature_title.setVisibility(View.GONE);
        }
        if (info.getBarometer() > 0)
            weather_barometer.setText(String.valueOf(info.getBarometer()));
        else {
            weather_barometer.setVisibility(View.GONE);
            weather_barometer_title.setVisibility(View.GONE);
        }
        if (!UsefulBits.isEmpty(info.getChill()))
            weather_chill.setText(info.getChill());
        else {
            weather_chill.setVisibility(View.GONE);
            weather_chill_title.setVisibility(View.GONE);
        }
        if (!UsefulBits.isEmpty(info.getDirectionStr()))
            weather_direction.setText(info.getDirectionStr());
        else {
            weather_direction.setVisibility(View.GONE);
            weather_direction_title.setVisibility(View.GONE);
        }
        if (!UsefulBits.isEmpty(info.getHumidityStatus()))
            weather_humidity.setText(info.getHumidityStatus());
        else {
            weather_humidity.setVisibility(View.GONE);
            weather_humidity_title.setVisibility(View.GONE);
        }

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        boolean isChanged = false;
        boolean isChecked = favorite_switch.isChecked();
        if (isChecked != info.getFavoriteBoolean()) isChanged = true;
        if (dismissListener != null)
            dismissListener.onDismiss(isChanged, isChecked);
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(boolean isChanged, boolean isFavorite);
    }
}