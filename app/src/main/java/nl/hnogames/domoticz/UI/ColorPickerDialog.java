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
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.larswerkman.lobsterpicker.LobsterPicker;
import com.larswerkman.lobsterpicker.sliders.LobsterOpacitySlider;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import nl.hnogames.domoticz.R;

public class ColorPickerDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private DismissListener dismissListener;
    private Context mContext;
    private int currentColor = 0;
    private LobsterPicker lobsterPicker;

    public ColorPickerDialog(Context mContext) {
        this.mContext = mContext;

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;

        mdb.customView(R.layout.dialog_color, wrapInScrollView)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void setCurrentColor(int currentColor) {
        this.currentColor = currentColor;
    }

    public void show() {
        mdb.title(mContext.getString(R.string.choose_color));

        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        lobsterPicker = (LobsterPicker) view.findViewById(R.id.lobsterpicker);
        LobsterShadeSlider shadeSlider = (LobsterShadeSlider) view.findViewById(R.id.shadeslider);
        LobsterOpacitySlider opacitySlider = (LobsterOpacitySlider) view.findViewById(R.id.opacityslider);

        lobsterPicker.addDecorator(shadeSlider);
        lobsterPicker.addDecorator(opacitySlider);

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (dismissListener != null)
            dismissListener.onDismiss(lobsterPicker.getColor());
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int color);
    }
}