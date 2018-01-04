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
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.triggertrap.seekarc.SeekArc;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class TemperatureDialog implements MaterialDialog.SingleButtonCallback {

    private final MaterialDialog.Builder mdb;
    private final int maxTemp;
    private int minTemp;

    private DialogActionListener dialogActionListener;
    private Context mContext;
    private double currentTemperature = 20;
    private SeekArc temperatureControl;
    private TextView temperatureText;
    private String tempSign = UsefulBits.getDegreeSymbol() + "C";
    private boolean isFahrenheit = false;
    private SharedPrefUtil mSharedPrefUtil;

    public TemperatureDialog(Context mContext, double temp) {
        this.mContext = mContext;
        if (mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(mContext);

        if (mSharedPrefUtil.darkThemeEnabled()) {
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
        mdb.customView(R.layout.dialog_temperature, false)
                .negativeText(android.R.string.cancel)
                .theme(mSharedPrefUtil.darkThemeEnabled() ? Theme.DARK : Theme.LIGHT)
                .positiveText(android.R.string.ok)
                .onAny(this);

        ConfigInfo configInfo = new ServerUtil(mContext).getActiveServer().getConfigInfo(mContext);
        if (configInfo != null) {
            tempSign = UsefulBits.getDegreeSymbol() + configInfo.getTempSign();
            if (!UsefulBits.isEmpty(configInfo.getTempSign()) && !configInfo.getTempSign().equals(DomoticzValues.Temperature.Sign.CELSIUS)) {
                isFahrenheit = true;
            }
        }

        minTemp = mSharedPrefUtil.getTemperatureSetMin(configInfo != null ? configInfo.getTempSign() : "C");
        maxTemp = mSharedPrefUtil.getTemperatureSetMax(configInfo != null ? configInfo.getTempSign() : "C");
        currentTemperature = temp;
    }

    public void show() {
        mdb.title(mContext.getString(R.string.temperature));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        temperatureControl = (SeekArc) view.findViewById(R.id.seekTemperature);
        temperatureText = (TextView) view.findViewById(R.id.seekTempProgress);
        final TextView temperatureSign = (TextView) view.findViewById(R.id.seekTempSign);
        temperatureSign.setText(tempSign);

        Button bntPlus = (Button) view.findViewById(R.id.plus);
        Button btnMin = (Button) view.findViewById(R.id.min);

        if (mSharedPrefUtil.darkThemeEnabled()) {
            bntPlus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_status_dark));
            btnMin.setBackground(ContextCompat.getDrawable(mContext, R.drawable.button_status_dark));
        }

        final String text = String.valueOf(currentTemperature);
        temperatureText.setText(text);
        temperatureControl.setMax((maxTemp - minTemp) * 2);

        int arcProgress = tempToProgress(currentTemperature);
        temperatureControl.setProgress(arcProgress);

        temperatureControl.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean fromUser) {
                double temp = progressToTemp(progress);
                temperatureText.setText(String.valueOf(temp));
                currentTemperature = temp;
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                temperatureText.setText(String.valueOf(progressToTemp(seekArc.getProgress())));
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                temperatureText.setText(String.valueOf(progressToTemp(seekArc.getProgress())));
            }

            @Override
            public boolean onTrackingLeap(SeekArc seekArc, boolean isRising) {
                return false;
            }
        });

        bntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = temperatureControl.getProgress();
                if (progressToTemp(progress) < maxTemp) {
                    progress += 1;
                    temperatureControl.setProgress(progress);
                }
            }
        });
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = temperatureControl.getProgress();
                if (progressToTemp(progress) > minTemp) {
                    progress -= 1;
                    temperatureControl.setProgress(progress);
                }
            }
        });
        md.show();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if (dialogActionListener != null)
            dialogActionListener.onDialogAction(currentTemperature, which);
    }

    private double progressToTemp(int progress) {
        return ((double) progress / 2) + minTemp;
    }

    private int tempToProgress(double temp) {
        return (int) (temp - minTemp) * 2;
    }

    public void onDismissListener(DialogActionListener dialogActionListener) {
        this.dialogActionListener = dialogActionListener;
    }

    protected MaterialDialog.Builder getMaterialDialogBuilder() {
        return mdb;
    }

    public interface DialogActionListener {
        void onDialogAction(double setPoint, DialogAction dialogAction);
    }
}