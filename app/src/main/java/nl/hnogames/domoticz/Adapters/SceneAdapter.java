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

package nl.hnogames.domoticz.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.SceneInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.ScenesClickListener;
import nl.hnogames.domoticz.R;

public class SceneAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = SceneAdapter.class.getSimpleName();

    private final ScenesClickListener listener;
    Context context;
    ArrayList<SceneInfo> filteredData = null;
    ArrayList<SceneInfo> data = null;
    Domoticz domoticz;

    ItemFilter mFilter = new ItemFilter();

    public SceneAdapter(Context context,
                        ArrayList<SceneInfo> data,
                        ScenesClickListener listener) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);
        Collections.sort(data, new Comparator<SceneInfo>() {
            @Override
            public int compare(SceneInfo left, SceneInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });

        this.filteredData = data;
        this.data = data;

        this.listener = listener;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int i) {
        return filteredData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int layoutResourceId;

        SceneInfo mSceneInfo = filteredData.get(position);

        //if (convertView == null) {
        holder = new ViewHolder();
        if (Domoticz.Scene.Type.SCENE.equalsIgnoreCase(mSceneInfo.getType())) {
            holder.isProtected = mSceneInfo.isProtected();
            layoutResourceId = R.layout.scene_row_scene;
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder.buttonOn = (Button) convertView.findViewById(R.id.on_button);
            holder.signal_level = (TextView) convertView.findViewById(R.id.switch_signal_level);
            holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
            holder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
            holder.switch_battery_level = (TextView) convertView.findViewById(R.id.switch_battery_level);

            holder.switch_name.setText(mSceneInfo.getName());
            String text = context.getText(R.string.last_update) + ": " + String.valueOf(mSceneInfo.getLastUpdate());
            holder.signal_level.setText(text);
            holder.switch_battery_level.setText(Domoticz.Scene.Type.SCENE);
            if (holder.isProtected) holder.onOffSwitch.setEnabled(false);
            Picasso.with(context).load(domoticz.getDrawableIcon(Domoticz.Scene.Type.SCENE.toLowerCase())).into(holder.iconRow);

            holder.buttonOn.setId(mSceneInfo.getIdx());
            holder.buttonOn.setText(context.getString(R.string.button_state_on));
            holder.buttonOn.setBackground(context.getResources().getDrawable(R.drawable.button));

            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    handleClick(view.getId(), true);
                }
            });
        } else if (mSceneInfo.getType().equalsIgnoreCase(Domoticz.Scene.Type.GROUP)) {

            holder.isProtected = mSceneInfo.isProtected();
            layoutResourceId = R.layout.scene_row_group;
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder.onOffSwitch = (Switch) convertView.findViewById(R.id.switch_button);
            holder.signal_level = (TextView) convertView.findViewById(R.id.switch_signal_level);
            holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
            holder.switch_name = (TextView) convertView.findViewById(R.id.switch_name);
            holder.switch_battery_level = (TextView) convertView.findViewById(R.id.switch_battery_level);

            holder.switch_name.setText(mSceneInfo.getName());
            String text = context.getText(R.string.last_update) + ": " + String.valueOf(mSceneInfo.getLastUpdate());
            holder.signal_level.setText(text);
            holder.switch_battery_level.setText(Domoticz.Scene.Type.GROUP);

            if (holder.onOffSwitch != null) {
                if (holder.isProtected) {
                    holder.onOffSwitch.setEnabled(false);
                }
                holder.onOffSwitch.setId(mSceneInfo.getIdx());
                holder.onOffSwitch.setChecked(mSceneInfo.getStatusInBoolean());
                holder.onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                        handleClick(compoundButton.getId(), checked);
                    }
                });
            }

            Picasso.with(context).load(domoticz.getDrawableIcon(Domoticz.Scene.Type.GROUP.toLowerCase())).into(holder.iconRow);

        } else throw new NullPointerException("Scene type not supported in the adapter for:\n"
                + mSceneInfo.toString());
        convertView.setTag(holder);

        return convertView;
    }

    public void handleClick(int idx, boolean action) {
        listener.onSceneClick(idx, action);
    }

    static class ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        Button buttonOn;
        Boolean isProtected;
        ImageView iconRow;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<SceneInfo> list = data;

            int count = list.size();
            final ArrayList<SceneInfo> nlist = new ArrayList<SceneInfo>(count);

            SceneInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableObject);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<SceneInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}