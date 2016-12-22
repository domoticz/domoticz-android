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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;

public class WidgetsAdapter extends BaseAdapter implements Filterable {
    private final int iVoiceAction = -55;
    private final int iQRCodeAction = -66;
    public ArrayList<DevicesInfo> filteredData = null;
    private Domoticz domoticz;
    private Context context;
    private ArrayList<DevicesInfo> data = null;
    private int layoutResourceId;
    private ItemFilter mFilter = new ItemFilter();
    private SharedPrefUtil mSharedPrefs;

    public WidgetsAdapter(Context context,
                          Domoticz mDomoticz,
                          ArrayList<DevicesInfo> data) {
        super();
        mSharedPrefs = new SharedPrefUtil(context);

        this.context = context;
        domoticz = mDomoticz;

        Collections.sort(data, new Comparator<DevicesInfo>() {
            @Override
            public int compare(DevicesInfo left, DevicesInfo right) {
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
        DevicesInfo deviceInfo = filteredData.get(position);

        holder = new ViewHolder();
        convertView = setDefaultRowId(holder);
        convertView.setTag(holder);

        if (mSharedPrefs.darkThemeEnabled()) {
            (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(context.getResources().getColor(R.color.background_dark));
        }

        setDefaultRowData(deviceInfo, holder);
        return convertView;
    }

    private View setDefaultRowId(ViewHolder holder) {
        layoutResourceId = R.layout.widget_configuration_row;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);

        return row;
    }

    private void setDefaultRowData(DevicesInfo mDeviceInfo,
                                   ViewHolder holder) {
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        try {
            String text = context.getString(R.string.last_update) + ": " +
                    String.valueOf(mDeviceInfo.getLastUpdate().substring(mDeviceInfo.getLastUpdate().indexOf(" ") + 1));
            if (holder.signal_level != null)
                holder.signal_level.setText(text);
            text = context.getString(R.string.data) + ": " +
                    String.valueOf(mDeviceInfo.getData());
            if (holder.switch_battery_level != null)
                holder.switch_battery_level.setText(text);
            if (mDeviceInfo.getUsage() != null && mDeviceInfo.getUsage().length() > 0)
                holder.switch_battery_level.setText(context.getString(R.string.usage) + ": " + mDeviceInfo.getUsage());
            if (mDeviceInfo.getCounterToday() != null && mDeviceInfo.getCounterToday().length() > 0)
                holder.switch_battery_level.append(" " + context.getString(R.string.today) + ": " + mDeviceInfo.getCounterToday());
            if (mDeviceInfo.getCounter() != null && mDeviceInfo.getCounter().length() > 0 &&
                    !mDeviceInfo.getCounter().equals(mDeviceInfo.getData()))
                holder.switch_battery_level.append(" " + context.getString(R.string.total) + ": " + mDeviceInfo.getCounter());
        } catch (Exception ex) {
            holder.switch_battery_level.setText("");
            holder.switch_battery_level.setVisibility(View.GONE);
        }

        if (mDeviceInfo.getIdx() == iVoiceAction) {
            Picasso.with(context).load(R.drawable.mic).into(holder.iconRow);
        } else if (mDeviceInfo.getIdx() == iQRCodeAction) {
            Picasso.with(context).load(R.drawable.qrcode).into(holder.iconRow);
        } else {
            Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                    mDeviceInfo.getType(),
                    mDeviceInfo.getSubType(),
                    mDeviceInfo.getStatusBoolean(),
                    mDeviceInfo.getUseCustomImage(),
                    mDeviceInfo.getImage())).into(holder.iconRow);
        }

        holder.iconRow.setAlpha(1f);
        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
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

            final ArrayList<DevicesInfo> list = data;

            int count = list.size();
            final ArrayList<DevicesInfo> nlist = new ArrayList<DevicesInfo>(count);

            DevicesInfo filterableObject;
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
            filteredData = (ArrayList<DevicesInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}