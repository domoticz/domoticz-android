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
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.triggertrap.seekarc.SeekArc;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.DomoticzValues;

public class TemperatureDialog implements MaterialDialog.SingleButtonCallback {

    private final MaterialDialog.Builder mdb;
    private final double maxTemp;
    private final double minTemp;
    private final Context mContext;
    private DialogActionListener dialogActionListener;
    private double currentTemperature = 20;
    private double step = 0.5;
    private SeekArc temperatureControl;
    private TextView temperatureText;
    private String tempSign = UsefulBits.getDegreeSymbol() + "C";
    private SharedPrefUtil mSharedPrefUtil;

    public TemperatureDialog(Context mContext, double temp, boolean hasStep, double step, boolean hasMax, double max, boolean hasMin, double min, String vunit) {
        this.mContext = mContext;
        if (mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(mContext);

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_temperature, false)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .onAny(this);

        ConfigInfo configInfo = StaticHelper.getServerUtil(mContext).getActiveServer().getConfigInfo(mContext);
        if(UsefulBits.isEmpty(vunit)) {
            if (configInfo != null) {
                tempSign = UsefulBits.getDegreeSymbol() + configInfo.getTempSign();
            }
        }else{
            tempSign = vunit;
        }

        minTemp = hasMin ? min : mSharedPrefUtil.getTemperatureSetMin(configInfo != null ? configInfo.getTempSign() : "C");
        maxTemp = hasMax ? max : mSharedPrefUtil.getTemperatureSetMax(configInfo != null ? configInfo.getTempSign() : "C");
        currentTemperature = temp;
        this.step = hasStep ? step : 0.5;
    }

    public void show() {
        mdb.title(mContext.getString(R.string.temperature));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        temperatureControl = view.findViewById(R.id.seekTemperature);
        temperatureText = view.findViewById(R.id.seekTempProgress);
        final TextView temperatureSign = view.findViewById(R.id.seekTempSign);
        temperatureSign.setText(tempSign);

        Button bntPlus = view.findViewById(R.id.plus);
        Button btnMin = view.findViewById(R.id.min);

        final String text = String.valueOf(currentTemperature);
        temperatureText.setText(text);
        temperatureControl.setMax((int) ((maxTemp - minTemp) * (1 / step)));

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

        bntPlus.setOnClickListener(v -> {
            int progress = temperatureControl.getProgress();
            if (progressToTemp(progress) < maxTemp) {
                progress += 1;
                temperatureControl.setProgress(progress);
            }
        });
        btnMin.setOnClickListener(v -> {
            int progress = temperatureControl.getProgress();
            if (progressToTemp(progress) > minTemp) {
                progress -= 1;
                temperatureControl.setProgress(progress);
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
        return ((double) progress / (1 / step)) + minTemp;
    }

    private int tempToProgress(double temp) {
        return (int) ((temp - minTemp) * (1 / step));
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