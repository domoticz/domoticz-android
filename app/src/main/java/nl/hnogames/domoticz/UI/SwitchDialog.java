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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.R;

public class SwitchDialog implements DialogInterface.OnDismissListener {

    private final MaterialDialog.Builder mdb;
    private ArrayList<SwitchInfo> info;
    private DismissListener dismissListener;
    private Context mContext;
    private Domoticz mDomoticz;

    public SwitchDialog(Context c,
                        ArrayList<SwitchInfo> _info,
                        int layout,
                        Domoticz domoticz) {
        this.info = _info;
        this.mContext = c;
        this.mDomoticz = domoticz;

        mdb = new MaterialDialog.Builder(mContext);
        mdb.customView(layout, true)
                .negativeText(android.R.string.cancel);
        mdb.dismissListener(this);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
    }

    public void show() {
        mdb.title(R.string.connectSwitch);
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
                                dismissListener.onDismiss(info.get(position).getIdx(), null);
                        } else {
                            // Switch is protected
                            PasswordDialog passwordDialog = new PasswordDialog(mContext);
                            passwordDialog.setTitle(mContext.getString(R.string.switch_protected));
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(String password) {
                                    dismissListener.onDismiss(info.get(position).getIdx(), password);
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
        for (SwitchInfo s : info) {
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
        void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword);
    }
}
