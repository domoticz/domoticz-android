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

package nl.hnogames.domoticz.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticzapi.Containers.SunRiseInfo;
import rm.com.clocks.ClockImageView;

public class SunriseInfoDialog {

    private final MaterialDialog.Builder mdb;
    private SunRiseInfo info;
    private Context context;
    private ClockImageView clock, sunrise, sunset;
    private TextView clockText, sunriseText, sunsetText;
    private ClockImageView astrTwilightStart, astrTwilightEnd;
    private TextView astrTwilightStartText, astrTwilightEndText;
    private ClockImageView civTwilightStart, civTwilightEnd;
    private TextView civTwilightStartText, civTwilightEndText;
    private ClockImageView nautTwilightStart, nautTwilightEnd;
    private TextView nautTwilightStartText, nautTwilightEndText;

    public SunriseInfoDialog(Context mContext,
                             SunRiseInfo info) {
        this.info = info;
        this.context = mContext;

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;

        //noinspection ConstantConditions
        mdb.customView(R.layout.dialog_sunrise, wrapInScrollView)
                .positiveText(android.R.string.ok);
    }

    public void show() {
        mdb.title(context.getString(R.string.category_clock));
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        InitViews(view);
        SetData();

        md.show();
    }

    private void SetData() {
        String s1 = info.getSunrise();
        sunrise.setHours(Integer.valueOf(s1.substring(0, s1.indexOf(":"))));
        sunrise.setMinutes(Integer.valueOf(s1.substring(s1.indexOf(":") + 1)));

        String s2 = info.getSunset();
        sunset.setHours(Integer.valueOf(s2.substring(0, s2.indexOf(":"))));
        sunset.setMinutes(Integer.valueOf(s2.substring(s2.indexOf(":") + 1)));

        String s3 = info.getAstrTwilightStart();
        astrTwilightStart.setHours(Integer.valueOf(s3.substring(0, s3.indexOf(":"))));
        astrTwilightStart.setMinutes(Integer.valueOf(s3.substring(s3.indexOf(":") + 1)));

        String s4 = info.getAstrTwilightEnd();
        astrTwilightEnd.setHours(Integer.valueOf(s4.substring(0, s4.indexOf(":"))));
        astrTwilightEnd.setMinutes(Integer.valueOf(s4.substring(s4.indexOf(":") + 1)));

        String s5 = info.getCivTwilightStart();
        civTwilightStart.setHours(Integer.valueOf(s5.substring(0, s5.indexOf(":"))));
        civTwilightStart.setMinutes(Integer.valueOf(s5.substring(s5.indexOf(":") + 1)));

        String s6 = info.getCivTwilightEnd();
        civTwilightEnd.setHours(Integer.valueOf(s6.substring(0, s6.indexOf(":"))));
        civTwilightEnd.setMinutes(Integer.valueOf(s6.substring(s6.indexOf(":") + 1)));

        String s7 = info.getNautTwilightStart();
        nautTwilightStart.setHours(Integer.valueOf(s7.substring(0, s7.indexOf(":"))));
        nautTwilightStart.setMinutes(Integer.valueOf(s7.substring(s7.indexOf(":") + 1)));

        String s8 = info.getNautTwilightEnd();
        nautTwilightEnd.setHours(Integer.valueOf(s8.substring(0, s8.indexOf(":"))));
        nautTwilightEnd.setMinutes(Integer.valueOf(s8.substring(s8.indexOf(":") + 1)));

        String current = info.getServerTime();
        current = current.substring((current.indexOf(":") - 2), (current.indexOf(":") + 3));
        clock.setHours(Integer.valueOf(current.substring(0, current.indexOf(":"))));
        clock.setMinutes(Integer.valueOf(current.substring(current.indexOf(":") + 1)));

        clockText.setText(current);
        sunriseText.setText(s1);
        sunsetText.setText(s2);
        astrTwilightStartText.setText(s3);
        astrTwilightEndText.setText(s4);
        civTwilightStartText.setText(s5);
        civTwilightEndText.setText(s6);
        nautTwilightStartText.setText(s7);
        nautTwilightEndText.setText(s8);
    }

    private void InitViews(View view) {
        clockText = view.findViewById(R.id.clockText);
        clock = view.findViewById(R.id.clock);

        sunriseText = view.findViewById(R.id.sunriseText);
        sunrise = view.findViewById(R.id.sunrise);

        sunsetText = view.findViewById(R.id.sunsetText);
        sunset = view.findViewById(R.id.sunset);

        astrTwilightStartText = view.findViewById(R.id.astrTwilightStartText);
        astrTwilightStart = view.findViewById(R.id.astrTwilightStart);

        astrTwilightEndText = view.findViewById(R.id.astrTwilightEndText);
        astrTwilightEnd = view.findViewById(R.id.astrTwilightEnd);

        nautTwilightStartText = view.findViewById(R.id.nautTwilightStartText);
        nautTwilightStart = view.findViewById(R.id.nautTwilightStart);

        nautTwilightEndText = view.findViewById(R.id.nautTwilightEndText);
        nautTwilightEnd = view.findViewById(R.id.nautTwilightEnd);

        civTwilightStartText = view.findViewById(R.id.civTwilightStartText);
        civTwilightStart = view.findViewById(R.id.civTwilightStart);

        civTwilightEndText = view.findViewById(R.id.civTwilightEndText);
        civTwilightEnd = view.findViewById(R.id.civTwilightEnd);
    }
}