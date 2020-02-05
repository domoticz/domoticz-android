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

package nl.hnogames.domoticz.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.BluetoothInfo;
import nl.hnogames.domoticz.interfaces.BluetoothClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class BluetoothAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = BluetoothAdapter.class.getSimpleName();
    public ArrayList<BluetoothInfo> data = null;
    private Context context;
    private BluetoothClickListener listener;

    private SharedPrefUtil mSharedPrefs;

    public BluetoothAdapter(Context context,
                            ArrayList<BluetoothInfo> data,
                            BluetoothClickListener l) {
        super();

        mSharedPrefs = new SharedPrefUtil(context);
        this.context = context;
        this.data = data;
        this.listener = l;
    }

    @Override
    public int getCount() {
        if (data == null)
            return 0;

        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        int layoutResourceId;

        final BluetoothInfo mBluetoothInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.bluetooth_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.enable = convertView.findViewById(R.id.enable);
        holder.name = convertView.findViewById(R.id.name);
        holder.tag_id = convertView.findViewById(R.id.tag_id);
        holder.switch_idx = convertView.findViewById(R.id.switchidx);
        holder.remove = convertView.findViewById(R.id.remove_button);

        holder.name.setText(mBluetoothInfo.getName());
        holder.tag_id.setText(mBluetoothInfo.getId());
        holder.tag_id.setVisibility(View.GONE);

        if (!UsefulBits.isEmpty(mBluetoothInfo.getSwitchName())) {
            holder.switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mBluetoothInfo.getSwitchName());
        } else if (mBluetoothInfo.getSwitchIdx() > 0) {
            holder.switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mBluetoothInfo.getSwitchIdx());
        } else {
            holder.switch_idx.setText(context.getString(R.string.connectedSwitch)
                    + ": " + context.getString(R.string.not_available));
        }

        if (!UsefulBits.isEmpty(mBluetoothInfo.getValue()))
            holder.switch_idx.setText(holder.switch_idx.getText() + " - " + mBluetoothInfo.getValue());

        holder.remove.setId(position);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                handleRemoveButtonClick(data.get(v.getId()));
            }
        });

        holder.enable.setId(position);
        holder.enable.setChecked(mBluetoothInfo.isEnabled());
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleEnableChanged(data.get(buttonView.getId()), isChecked);
            }
        });

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(BluetoothInfo qr) {
        listener.onRemoveClick(qr);
    }

    private boolean handleEnableChanged(BluetoothInfo qr, boolean enabled) {
        return listener.onEnableClick(qr, enabled);
    }

    static class ViewHolder {
        TextView name;
        TextView tag_id;
        TextView switch_idx;
        CheckBox enable;
        Button remove;
    }
}