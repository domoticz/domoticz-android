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
import nl.hnogames.domoticz.containers.BeaconInfo;
import nl.hnogames.domoticz.interfaces.BeaconClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class BeaconAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = BeaconAdapter.class.getSimpleName();
    private final Context context;
    private final BeaconClickListener listener;
    private final SharedPrefUtil mSharedPrefs;
    public ArrayList<BeaconInfo> data = null;

    public BeaconAdapter(Context context,
                         ArrayList<BeaconInfo> data,
                         BeaconClickListener l) {
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

        final BeaconInfo mBeaconInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.beacon_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.enable = convertView.findViewById(R.id.enablebeacon);
        holder.beacon_name = convertView.findViewById(R.id.beacon_name);
        holder.beacon_uuid = convertView.findViewById(R.id.beacon_uuid);
        holder.beacon_major = convertView.findViewById(R.id.beacon_major);
        holder.beacon_minor = convertView.findViewById(R.id.beacon_minor);
        holder.beacon_switch_idx = convertView.findViewById(R.id.beacon_switchidx);
        holder.remove = convertView.findViewById(R.id.remove_button);

        holder.beacon_name.setText(mBeaconInfo.getName());
        if (!UsefulBits.isEmpty(mBeaconInfo.getSwitchName())) {
            holder.beacon_switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mBeaconInfo.getSwitchName());
        } else if (mBeaconInfo.getSwitchIdx() > 0) {
            holder.beacon_switch_idx.setText(context.getString(R.string.connectedSwitch) + ": " + mBeaconInfo.getSwitchIdx());
        } else {
            holder.beacon_switch_idx.setText(context.getString(R.string.connectedSwitch)
                    + ": " + context.getString(R.string.not_available));
        }
        if (!UsefulBits.isEmpty(mBeaconInfo.getValue()))
            holder.beacon_switch_idx.setText(holder.beacon_switch_idx.getText() + " - " + mBeaconInfo.getValue());

        holder.beacon_uuid.setText(mBeaconInfo.getId());
        holder.beacon_major.setText(context.getString(R.string.beacon_major) + ": " + mBeaconInfo.getMajor());
        holder.beacon_minor.setText(context.getString(R.string.beacon_minor) + ": " + mBeaconInfo.getMinor());

        holder.remove.setId(position);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                handleRemoveButtonClick(data.get(v.getId()));
            }
        });

        holder.enable.setId(position);
        holder.enable.setChecked(mBeaconInfo.isEnabled());
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleEnableChanged(data.get(buttonView.getId()), isChecked);
            }
        });

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(BeaconInfo beacon) {
        listener.onRemoveClick(beacon);
    }

    private boolean handleEnableChanged(BeaconInfo beacon, boolean enabled) {
        return listener.onEnableClick(beacon, enabled);
    }

    static class ViewHolder {
        TextView beacon_name;
        TextView beacon_uuid, beacon_minor, beacon_major;
        TextView beacon_switch_idx;
        CheckBox enable;
        Button remove;
    }
}