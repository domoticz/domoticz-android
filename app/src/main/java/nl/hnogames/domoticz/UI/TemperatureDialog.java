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

import android.content.Context;
import android.content.DialogInterface;
import android.renderscript.Double2;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.triggertrap.seekarc.SeekArc;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class TemperatureDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private SharedPrefUtil mSharedPrefs;
    private int idx;
    private double currentTemperature = 20;
    private SeekArc temperatureControl;
    private TextView temperatureText;
    private Button bntPlus, btnMin;

    public TemperatureDialog(Context mContext, int idx, double temp) {
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);
        this.idx = idx;
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_temperature, false)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
        currentTemperature=temp;
    }

    public void show() {
        mdb.title(mContext.getString(R.string.temperature));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        temperatureControl = (SeekArc)view.findViewById(R.id.seekTemperature);
        temperatureText = (TextView)view.findViewById(R.id.seekTempProgress);
        bntPlus = (Button)view.findViewById(R.id.plus);
        btnMin = (Button)view.findViewById(R.id.min);

        temperatureText.setText(String.valueOf(currentTemperature));
        int Progress = (int)(currentTemperature*2);
        temperatureControl.setProgress(Progress);

        temperatureControl.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                double temp = ((double)temperatureControl.getProgress()/2);
                temperatureText.setText(String.valueOf(temp));
            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {}

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {}
        });

        bntPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                temperatureControl.setProgress(temperatureControl.getProgress()+1);
            }
        });
        btnMin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                temperatureControl.setProgress(temperatureControl.getProgress()-1);
            }
        });
        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (dismissListener != null)
            dismissListener.onDismiss(((double)temperatureControl.getProgress()/2));
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(double setPoint);
    }
}