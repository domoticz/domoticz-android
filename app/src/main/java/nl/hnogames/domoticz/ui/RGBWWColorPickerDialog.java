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
import android.content.DialogInterface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class RGBWWColorPickerDialog implements DialogInterface.OnDismissListener {
    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private SharedPrefUtil mSharedPrefs;
    private int idx;
    private SeekBar kelvinBar;
    private ColorPickerView colorPickerView;
    private SwitchMaterial rgbSwitch;
    private LinearLayout wrapperKelvin;

    public RGBWWColorPickerDialog(Context mContext, int idx) {
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);
        this.idx = idx;
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_rgbwwcolor, true)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.choose_color));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        if (view != null) {
            kelvinBar = view.findViewById(R.id.kelvinBar);
            colorPickerView = view.findViewById(R.id.colorPickerView);
            rgbSwitch = view.findViewById(R.id.rgbSwitch);
            wrapperKelvin = view.findViewById(R.id.kelvinBarWrapper);

            rgbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    colorPickerView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    wrapperKelvin.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                }
            });

            colorPickerView.setColorListener(new ColorListener() {
                @Override
                public void onColorSelected(int color, boolean fromUser) {
                    if (fromUser) {
                        if (dismissListener != null)
                            dismissListener.onChangeRGBColor(color);
                    }
                }
            });

            kelvinBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (dismissListener != null)
                        dismissListener.onChangeKelvinColor(kelvinBar.getProgress());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (dismissListener != null)
            dismissListener.onDismiss(colorPickerView.getVisibility() == View.VISIBLE ? colorPickerView.getColor() : kelvinBar.getProgress(), colorPickerView.getVisibility() == View.VISIBLE);
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int color, final boolean isRGB);

        void onChangeRGBColor(int color);

        void onChangeKelvinColor(int color);
    }
}