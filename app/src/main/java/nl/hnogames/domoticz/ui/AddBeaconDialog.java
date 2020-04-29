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
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.BeaconInfo;
import nl.hnogames.domoticz.utils.UsefulBits;

public class AddBeaconDialog {
    private final MaterialDialog.Builder mdb;
    private Context mContext;
    private AppCompatEditText uuid;
    private AppCompatEditText major;
    private AppCompatEditText minor;
    private OnDoneListener listener;

    public AddBeaconDialog(final Context mContext, OnDoneListener l) {
        this.mContext = mContext;
        this.listener = l;
        mdb = new MaterialDialog.Builder(mContext);
        boolean wrapInScrollView = true;
        mdb.customView(R.layout.dialog_beacon, wrapInScrollView)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String UUID = String.valueOf(uuid.getText());
                        String Major = String.valueOf(major.getText());
                        if (UsefulBits.isEmpty(Major))
                            Major = "0";
                        String Minor = String.valueOf(minor.getText());
                        if (UsefulBits.isEmpty(Minor))
                            Minor = "0";

                        if (UsefulBits.isEmpty(UUID))
                            Toast.makeText(mContext, "The UUID is mandatory", Toast.LENGTH_LONG).show();
                        else {
                            BeaconInfo beacon = new BeaconInfo();
                            beacon.setId(UUID);
                            beacon.setMinor(Integer.parseInt(Minor));
                            beacon.setMajor(Integer.parseInt(Major));
                            if (listener != null)
                                listener.onAdded(beacon);
                        }
                    }
                })
                .negativeText(android.R.string.cancel);
    }

    public void show() {
        mdb.title(mContext.getString(R.string.beacon));
        MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        uuid = view.findViewById(R.id.uuid);
        major = view.findViewById(R.id.major);
        minor = view.findViewById(R.id.minor);
        md.show();
    }

    public interface OnDoneListener {
        void onAdded(BeaconInfo beacon);
    }
}