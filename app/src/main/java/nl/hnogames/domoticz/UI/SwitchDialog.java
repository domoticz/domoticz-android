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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.StatusReceiver;

public class SwitchDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private ArrayList<DevicesInfo> info;
    private DismissListener dismissListener;
    private Context mContext;
    private Domoticz mDomoticz;

    public SwitchDialog(Context c,
                        ArrayList<DevicesInfo> _info,
                        int layout,
                        Domoticz domoticz) {
        this.info = _info;
        this.mContext = c;
        this.mDomoticz = domoticz;

        if ((new SharedPrefUtil(mContext)).darkThemeEnabled()) {
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
        mdb.customView(layout, true)
                .theme((new SharedPrefUtil(mContext)).darkThemeEnabled() ? Theme.DARK : Theme.LIGHT)
                .negativeText(android.R.string.cancel);
        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    public void show() {
        mdb.title("Connect Switch");
        final MaterialDialog md = mdb.build();
        View view = md.getCustomView();
        ListView listView = (ListView) view.findViewById(R.id.list);
        String[] listData = processSwitches();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, android.R.id.text1, listData);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mDomoticz.getStatus(info.get(position).getIdx(), new StatusReceiver() {
                    @Override
                    public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {
                        if (!extendedStatusInfo.isProtected()) {
                            if (dismissListener != null)
                                dismissListener.onDismiss(info.get(position).getIdx(), null, info.get(position).getName(), info.get(position).isSceneOrGroup());
                        } else {
                            PasswordDialog passwordDialog = new PasswordDialog(mContext, mDomoticz);
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(String password) {
                                    dismissListener.onDismiss(info.get(position).getIdx(), password, info.get(position).getName(), info.get(position).isSceneOrGroup());
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                    }
                });

                md.dismiss();
            }
        });

        listView.setAdapter(adapter);
        md.show();
    }

    public String[] processSwitches() {
        String[] listData = new String[info.size()];
        int counter = 0;
        for (DevicesInfo s : info) {
            String log = s.getIdx() + " | " + s.getName();
            listData[counter] = log;
            counter++;
        }
        return listData;
    }

    public void onDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public interface DismissListener {
        void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName, boolean isSceneOrGroup);
    }
}
