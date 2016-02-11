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
    @SuppressWarnings("FieldCanBeLocal")
    private final int minCelsiusTemp = 10;
    @SuppressWarnings("FieldCanBeLocal")
    private final int maxCelsiusTemp = 30;
    @SuppressWarnings("FieldCanBeLocal")
    private final int minFahrenheitTemp = 50;
    @SuppressWarnings("FieldCanBeLocal")
    private final int maxFahrenheitTemp = 90;
    @SuppressWarnings({"FieldCanBeLocal", "PointlessArithmeticExpression"})
    private final int animationDuration = 1 * 1000;             // 1 second

    private DialogActionListener dialogActionListener;
    private Context mContext;
    private SeekArc temperatureControl;
    private TextView temperatureText;
    private double currentTemperature = 20;
    private int minTemp;
    private String tempSign = "";
    private boolean isFahrenheit = false;

    public TemperatureDialog(Context mContext, double temp) {
        this.mContext = mContext;

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_temperature, false)
                .negativeText(android.R.string.cancel)
                .positiveText(android.R.string.ok)
                .onAny(this);

        ConfigInfo configInfo = new ServerUtil(mContext).getActiveServer().getConfigInfo();
        if (configInfo != null) {
            tempSign = configInfo.getTempSign();
            if (!configInfo.getTempSign().equals(Domoticz.Temperature.Sign.CELSIUS))
                isFahrenheit = true;
        }

        minTemp = getMinTemp();
        currentTemperature = checkTempForSanity(temp);
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

        final String text = String.valueOf(currentTemperature);
        temperatureText.setText(text);

        if (!isFahrenheit) temperatureControl.setMax((maxCelsiusTemp - minCelsiusTemp) * 2);       // 40 points for 20 degree difference
        else temperatureControl.setMax((maxFahrenheitTemp - minFahrenheitTemp) * 2);                    // 80 points for 40 degree difference

        int arcProgress = tempToProgress(currentTemperature);
        ObjectAnimator animation = ObjectAnimator.ofInt(temperatureControl, "progress", arcProgress);
        animation.setDuration(animationDuration);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();

        temperatureControl.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                if (b) {
                    double temp = progressToTemp(i);
                    temperatureText.setText(String.valueOf(temp));
                    currentTemperature = temp;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
                temperatureText.setText(String.valueOf(progressToTemp(seekArc.getProgress())));
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                temperatureText.setText(String.valueOf(progressToTemp(seekArc.getProgress())));
            }
        });

        bntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = temperatureControl.getProgress() + 1;
                progress = checkProgressForSanity(progress);
                currentTemperature = progressToTemp(progress);
                temperatureText.setText(progressToTemperatureString(progress));
                temperatureControl.setProgress(progress);
            }
        });
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = temperatureControl.getProgress() - 1;
                progress = checkProgressForSanity(progress);
                currentTemperature = progressToTemp(progress);
                temperatureText.setText(progressToTemperatureString(progress));
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

    private double checkTempForSanity(double temp) {
        if (isFahrenheit) {
            if (temp < minFahrenheitTemp) temp = minFahrenheitTemp;     // Fahrenheit min = 50 (10 degrees Celsius)
            if (temp > maxFahrenheitTemp) temp = maxFahrenheitTemp;     // Fahrenheit max = 90 (32 degrees Celsius)
        } else {
            minTemp = minCelsiusTemp;
            if (temp < minCelsiusTemp) temp = minCelsiusTemp;           // Celsius min = 10
            if (temp > maxCelsiusTemp) temp = maxCelsiusTemp;           // Celsius max = 30
        }
        return temp;
    }

    private int checkProgressForSanity(int progress) {
        double temp = progressToTemp(progress);

        if (isFahrenheit) {
            if (temp < minFahrenheitTemp) temp = minFahrenheitTemp;
            if (temp > maxFahrenheitTemp) temp = maxFahrenheitTemp;
        } else {
            if (temp < minCelsiusTemp) temp = minCelsiusTemp;
            if (temp > maxCelsiusTemp) temp = maxCelsiusTemp;
        }
        return tempToProgress(temp);
    }

    private int getMinTemp() {
        if (isFahrenheit)
            return minFahrenheitTemp;
        else
            return minCelsiusTemp;
    }

    private double progressToTemp(int progress) {
        return ((double) progress / 2) + minTemp;
    }

    private String progressToTemperatureString(int progress) {
        double temp = progressToTemp(progress);
        return String.valueOf(temp);
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