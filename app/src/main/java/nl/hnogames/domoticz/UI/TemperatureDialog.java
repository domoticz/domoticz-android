/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.UI;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.triggertrap.seekarc.SeekArc;

import nl.hnogames.domoticz.Containers.ConfigInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.ServerUtil;

public class TemperatureDialog implements MaterialDialog.SingleButtonCallback {

    private final MaterialDialog.Builder mdb;
    private DialogActionListener dialogActionListener;
    private Context mContext;
    private double currentTemperature = 20;
    private SeekArc temperatureControl;
    private TextView temperatureText;
    private String tempSign = "";
    private ConfigInfo configInfo;
    private boolean isFahrenheit = false;

    public TemperatureDialog(Context mContext, double temp) {
        this.mContext = mContext;
        configInfo = new ServerUtil(mContext).getActiveServer().getConfigInfo();

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_temperature, false)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .onAny(this);
        currentTemperature = temp;
    }

    public void show() {
        mdb.title(mContext.getString(R.string.temperature));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        temperatureControl = (SeekArc) view.findViewById(R.id.seekTemperature);
        temperatureText = (TextView) view.findViewById(R.id.seekTempProgress);
        Button bntPlus = (Button) view.findViewById(R.id.plus);
        Button btnMin = (Button) view.findViewById(R.id.min);

        if (configInfo != null) {
            tempSign = configInfo.getTempSign();
            if (!configInfo.getTempSign().equals(Domoticz.Temperature.Sign.CELSIUS))
                isFahrenheit = true;
        }
        final String text = String.valueOf(currentTemperature) + " " + tempSign;
        temperatureText.setText(text);

        int progress = (int) (currentTemperature);
        if (!isFahrenheit)
            progress = (int) (currentTemperature * 2);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            ObjectAnimator animation = ObjectAnimator.ofInt(temperatureControl, "progress", progress);
            animation.setDuration(1000); // 0.5 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        } else
            temperatureControl.setProgress(progress); // no animation on Gingerbread or lower

        temperatureControl.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                double temp = ((double) temperatureControl.getProgress() / 2);
                String text;

                if (isFahrenheit) {
                    text = String.valueOf(temp * 2) + " " + tempSign;
                    temperatureText.setText(text);
                }
                else {
                    text = String.valueOf(temp) + " " + tempSign;
                    temperatureText.setText(text);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                double temp = ((double) temperatureControl.getProgress() / 2);
                String text;

                if (isFahrenheit) {
                    text = String.valueOf(temp * 2) + " " + tempSign;
                    temperatureText.setText(text);
                }
                else {
                    text = String.valueOf(temp) + " " + tempSign;
                    temperatureText.setText(text);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                double temp = ((double) temperatureControl.getProgress() / 2);
                String text;

                if (isFahrenheit) {
                    text = String.valueOf(temp * 2) + " " + tempSign;
                    temperatureText.setText(text);
                }
                else {
                    text = String.valueOf(temp) + " " + tempSign;
                    temperatureText.setText(text);
                }
            }
        });

        bntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text;
                if (isFahrenheit) {
                    temperatureControl.setProgress(temperatureControl.getProgress() + 2);
                    text = String.valueOf(temperatureControl.getProgress()) + " " + tempSign;
                    temperatureText.setText(text);
                } else {
                    temperatureControl.setProgress(temperatureControl.getProgress() + 1);
                    text = String.valueOf((double) temperatureControl.getProgress() / 2) + " " + tempSign;
                    temperatureText.setText(text);
                }
            }
        });
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text;
                if (isFahrenheit) {
                    temperatureControl.setProgress(temperatureControl.getProgress() - 2);
                    text = String.valueOf(temperatureControl.getProgress()) + " " + tempSign;
                    temperatureText.setText(text);
                } else {
                    temperatureControl.setProgress(temperatureControl.getProgress() - 1);
                    text = String.valueOf((double) temperatureControl.getProgress() / 2) + " " + tempSign;
                    temperatureText.setText(text);
                }
            }
        });
        md.show();
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        if (dialogActionListener != null) {
            if (isFahrenheit)
                dialogActionListener.onDialogAction(((double) temperatureControl.getProgress()), which);
            else
                dialogActionListener.onDialogAction(((double) temperatureControl.getProgress() / 2), which);
        }
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
