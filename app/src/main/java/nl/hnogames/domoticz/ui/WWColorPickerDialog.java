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
import android.widget.SeekBar;

import com.afollestad.materialdialogs.MaterialDialog;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class WWColorPickerDialog implements DialogInterface.OnDismissListener {
    private final MaterialDialog.Builder mdb;
    private final Context mContext;
    private final SharedPrefUtil mSharedPrefs;
    private final int idx;
    private DismissListener dismissListener;
    private SeekBar kelvinBar;

    public WWColorPickerDialog(Context mContext, int idx) {
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);
        this.idx = idx;
        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(R.layout.dialog_wwcolor, true)
                .positiveText(android.R.string.ok);
        mdb.dismissListener(this);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.choose_color));
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();

        if (view != null) {
            kelvinBar = view.findViewById(R.id.kelvinBar);
            kelvinBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (dismissListener != null)
                        dismissListener.onChangeColor(kelvinBar.getProgress());
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
            dismissListener.onDismiss(kelvinBar.getProgress());
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int color);

        void onChangeColor(int color);
    }
}