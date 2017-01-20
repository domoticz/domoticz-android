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
import android.support.annotation.ColorInt;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.OnColorListener;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class ColorPickerDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private LobsterPicker lobsterPicker;
    private LobsterShadeSlider shadeSlider;
    private SharedPrefUtil mSharedPrefs;
    private int idx;

    public ColorPickerDialog(Context mContext, int idx) {
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);
        this.idx = idx;

        if (mSharedPrefs.darkThemeEnabled()) {
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
        mdb.customView(R.layout.dialog_color, true)
                .theme(mSharedPrefs.darkThemeEnabled() ? Theme.DARK : Theme.LIGHT)
                .positiveText(android.R.string.ok);

        mdb.dismissListener(this);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.choose_color));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        if (view != null) {
            lobsterPicker = (LobsterPicker) view.findViewById(R.id.lobsterpicker);
            shadeSlider = (LobsterShadeSlider) view.findViewById(R.id.shadeslider);
            lobsterPicker.addDecorator(shadeSlider);
            lobsterPicker.setColorHistoryEnabled(true);
            lobsterPicker.setHistory(mSharedPrefs.getPreviousColor(idx));
            lobsterPicker.setColorPosition(mSharedPrefs.getPreviousColorPosition(idx));

            shadeSlider.addOnColorListener(new OnColorListener() {
                @Override
                public void onColorChanged(@ColorInt int color) {
                }

                @Override
                public void onColorSelected(@ColorInt int color) {
                    mSharedPrefs.savePreviousColor(idx, color, lobsterPicker.getColorPosition());
                    dismissListener.onChangeColor(color);
                }
            });

            lobsterPicker.addOnColorListener(new OnColorListener() {
                @Override
                public void onColorChanged(@ColorInt int color) {
                }

                @Override
                public void onColorSelected(@ColorInt int color) {
                    mSharedPrefs.savePreviousColor(idx, color, lobsterPicker.getColorPosition());
                    dismissListener.onChangeColor(color);
                }
            });
        }

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (dismissListener != null)
            dismissListener.onDismiss(shadeSlider.getColor());
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int color);

        void onChangeColor(int color);
    }
}