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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class TemperatureWidgetAdapter extends BaseAdapter implements Filterable {
    public ArrayList<TemperatureInfo> filteredData = null;
    private Domoticz domoticz;
    private Context context;
    private ArrayList<TemperatureInfo> data = null;
    private int layoutResourceId;
    private ItemFilter mFilter = new ItemFilter();
    private ConfigInfo mConfigInfo;
    private SharedPrefUtil mSharedPrefs;

    public TemperatureWidgetAdapter(Context context,
                                    Domoticz mDomoticz,
                                    ServerUtil configInfo,
                                    ArrayList<TemperatureInfo> data) {
        super();
        mSharedPrefs = new SharedPrefUtil(context);
        this.mConfigInfo = configInfo.getActiveServer() != null ? configInfo.getActiveServer().getConfigInfo(context) : null;

        this.context = context;
        domoticz = mDomoticz;

        Collections.sort(data, new Comparator<TemperatureInfo>() {
            @Override
            public int compare(TemperatureInfo left, TemperatureInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });

        this.filteredData = data;
        this.data = data;
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
        TemperatureInfo deviceInfo = filteredData.get(position);

        holder = new ViewHolder();
        convertView = setDefaultRowId(holder);
        convertView.setTag(holder);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((convertView.findViewById(R.id.card_global_wrapper)) != null)
                convertView.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((convertView.findViewById(R.id.row_wrapper)) != null)
                (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.color.card_background_dark));
            if ((convertView.findViewById(R.id.row_global_wrapper)) != null)
                (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
        }

        setDefaultRowData(deviceInfo, holder);
        return convertView;
    }

    private View setDefaultRowId(ViewHolder holder) {
        layoutResourceId = R.layout.widget_configuration_row;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.signal_level = row.findViewById(R.id.switch_signal_level);
        holder.iconRow = row.findViewById(R.id.rowIcon);
        holder.switch_name = row.findViewById(R.id.switch_name);
        holder.switch_battery_level = row.findViewById(R.id.switch_battery_level);

        return row;
    }

    private void setDefaultRowData(TemperatureInfo mDeviceInfo,
                                   ViewHolder holder) {
        final double temperature = mDeviceInfo.getTemperature();
        final double setPoint = mDeviceInfo.getSetPoint();
        holder.isProtected = mDeviceInfo.isProtected();

        String sign = mConfigInfo != null ? mConfigInfo.getTempSign() : "C";
        holder.switch_name.setText(mDeviceInfo.getName());
        if (Double.isNaN(temperature) || Double.isNaN(setPoint)) {
            if (holder.signal_level != null)
                holder.signal_level.setVisibility(View.GONE);

            if (holder.switch_battery_level != null) {
                String batteryText = context.getString(R.string.temperature)
                        + ": "
                        + mDeviceInfo.getData();
                holder.switch_battery_level.setText(batteryText);
            }
        } else {
            if (holder.signal_level != null)
                holder.signal_level.setVisibility(View.VISIBLE);
            if (holder.switch_battery_level != null) {
                String batteryLevelText = context.getString(R.string.temperature)
                        + ": "
                        + temperature
                        + " " + sign;
                holder.switch_battery_level.setText(batteryLevelText);
            }

            if (holder.signal_level != null) {
                String signalText = context.getString(R.string.set_point)
                        + ": "
                        + mDeviceInfo.getSetPoint()
                        + " " + sign;
                holder.signal_level.setText(signalText);
            }
        }
    }

    static class ViewHolder {
        TextView switch_name, signal_level, switch_battery_level;
        Boolean isProtected;
        ImageView iconRow;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<TemperatureInfo> list = data;

            int count = list.size();
            final ArrayList<TemperatureInfo> nlist = new ArrayList<TemperatureInfo>(count);

            TemperatureInfo filterableObject;
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
            filteredData = (ArrayList<TemperatureInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}