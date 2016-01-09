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
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.triggertrap.seekarc.SeekArc;

import nl.hnogames.domoticz.R;

public class TemperatureDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private int idx;
    private int currentTemperature = 200;
    private SeekArc temperatureControl;
    private TextView temperatureText;

    public TemperatureDialog(Context mContext, int idx, double temp) {
        this.mContext = mContext;
        this.idx = idx;
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_temperature, false)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
        currentTemperature = (int) temp * 10;
    }

    public void show() {

        mdb.title(mContext.getString(R.string.temperature));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        temperatureControl = (SeekArc) view.findViewById(R.id.seekTemperature);
        temperatureText = (TextView) view.findViewById(R.id.seekTempProgress);
        Button bntPlus = (Button) view.findViewById(R.id.plus);
        Button btnMin = (Button) view.findViewById(R.id.min);
        float temp = currentTemperature / 10;
        temperatureText.setText(String.valueOf(temp));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ObjectAnimator animation =
                    ObjectAnimator.ofFloat(temperatureControl, "progress", currentTemperature);
            animation.setDuration(1000); // 1 second
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
        else
            temperatureControl.setProgress(currentTemperature); // no animation on Honeycomb or lower

        temperatureControl.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int progress, boolean b) {
                currentTemperature = progress;
                float temp = progress / 10;
                temperatureText.setText(String.valueOf(temp));
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {
            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
            }
        });

        bntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTemperature-=5;
                temperatureControl.setProgress(currentTemperature + 5);
            }
        });
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentTemperature-=5;
                temperatureControl.setProgress(currentTemperature - 5);
            }
        });
        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (dismissListener != null)
            dismissListener.onDismiss(((double) temperatureControl.getProgress() / 10));
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(double setPoint);
    }
}