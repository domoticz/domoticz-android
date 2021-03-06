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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;

public class TemperatureInfoDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private final TemperatureInfo info;
    private DismissListener dismissListener;
    private String idx;
    private String lastUpdate;
    private boolean isFavorite;
    private SwitchMaterial favorite_switch;

    public TemperatureInfoDialog(Context mContext,
                                 TemperatureInfo info,
                                 int layout) {
        this.info = info;

        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        //noinspection ConstantConditions
        mdb.customView(layout, wrapInScrollView)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void setIdx(String idx) {
        this.idx = idx;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void show() {
        mdb.title(info.getName());
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        TextView IDX_value = view.findViewById(R.id.IDX_value);
        IDX_value.setText(idx);

        TextView LastUpdate_value = view.findViewById(R.id.LastUpdate_value);
        LastUpdate_value.setText(lastUpdate);

        favorite_switch = view.findViewById(R.id.favorite_switch);
        favorite_switch.setChecked(isFavorite);
        favorite_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });

        md.show();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        boolean isChanged = false;
        boolean isChecked = favorite_switch.isChecked();
        if (isChecked != isFavorite) isChanged = true;
        if (dismissListener != null)
            dismissListener.onDismiss(isChanged, isChecked);
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(boolean isChanged, boolean isFavorite);
    }
}