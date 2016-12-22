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

package nl.hnogames.domoticz.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SpeechInfo;
import nl.hnogames.domoticz.Interfaces.SpeechClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class SpeechAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = SpeechAdapter.class.getSimpleName();
    public ArrayList<SpeechInfo> data = null;
    private Context context;
    private SpeechClickListener listener;

    private SharedPrefUtil mSharedPrefs;

    public SpeechAdapter(Context context,
                         ArrayList<SpeechInfo> data,
                         SpeechClickListener l) {
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

        final SpeechInfo mSpeechInfo = data.get(position);
        holder = new ViewHolder();

        layoutResourceId = R.layout.speech_row;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));

            if ((convertView.findViewById(R.id.remove_button)) != null)
                (convertView.findViewById(R.id.remove_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }

        holder.enable = (CheckBox) convertView.findViewById(R.id.enableSpeech);
        holder.Speech_name = (TextView) convertView.findViewById(R.id.speech_name);
        holder.Speech_tag_id = (TextView) convertView.findViewById(R.id.speech_tag_id);
        holder.Speech_switch_idx = (TextView) convertView.findViewById(R.id.speech_switchidx);
        holder.remove = (Button) convertView.findViewById(R.id.remove_button);

        holder.Speech_name.setText(mSpeechInfo.getName());
        if (!UsefulBits.isEmpty(mSpeechInfo.getSwitchName())) {
            holder.Speech_tag_id.setText(context.getString(R.string.connectedSwitch) + ": " + mSpeechInfo.getSwitchName());
        } else if (mSpeechInfo.getSwitchIdx() > 0) {
            holder.Speech_tag_id.setText(context.getString(R.string.connectedSwitch) + ": " + mSpeechInfo.getSwitchIdx());
        } else {
            holder.Speech_tag_id.setText(context.getString(R.string.connectedSwitch)
                    + ": " + context.getString(R.string.not_available));
        }

        if (!UsefulBits.isEmpty(mSpeechInfo.getValue()))
            holder.Speech_tag_id.setText(holder.Speech_tag_id.getText() + " - " + mSpeechInfo.getValue());

        holder.Speech_switch_idx.setText("Commando's: \r\n" + "'" + mSpeechInfo.getName() + "' " + "\r\n'" + mSpeechInfo.getName() + " " + context.getString(R.string.button_state_on).toLowerCase() + "'\r\n" +
                "'" + mSpeechInfo.getName() + " " + context.getString(R.string.button_state_off).toLowerCase() + "'");

        holder.remove.setId(position);
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                handleRemoveButtonClick(data.get(v.getId()));
            }
        });

        holder.enable.setId(position);
        holder.enable.setChecked(mSpeechInfo.isEnabled());
        holder.enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                handleEnableChanged(data.get(buttonView.getId()), isChecked);
            }
        });

        convertView.setTag(holder);
        return convertView;
    }

    private void handleRemoveButtonClick(SpeechInfo Speech) {
        listener.onRemoveClick(Speech);
    }

    private boolean handleEnableChanged(SpeechInfo Speech, boolean enabled) {
        return listener.onEnableClick(Speech, enabled);
    }

    static class ViewHolder {
        TextView Speech_name;
        TextView Speech_tag_id;
        TextView Speech_switch_idx;
        CheckBox enable;
        Button remove;
    }
}