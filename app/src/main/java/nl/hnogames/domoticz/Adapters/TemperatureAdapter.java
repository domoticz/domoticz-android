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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.TemperatureInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.TemperatureClickListener;
import nl.hnogames.domoticz.R;


public class TemperatureAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = TemperatureAdapter.class.getSimpleName();

    private final TemperatureClickListener listener;
    private Domoticz domoticz;
    private Context context;
    public ArrayList<TemperatureInfo> filteredData = null;
    private ArrayList<TemperatureInfo> data = null;
    private ItemFilter mFilter = new ItemFilter();

    public TemperatureAdapter(Context context,
                              ArrayList<TemperatureInfo> data,
                              TemperatureClickListener listener) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);
        Collections.sort(data, new Comparator<TemperatureInfo>() {
            @Override
            public int compare(TemperatureInfo left, TemperatureInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
        this.data = data;
        this.filteredData = data;
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

        TemperatureInfo mTemperatureInfo = filteredData.get(position);
        final long setPoint = mTemperatureInfo.getSetPoint();

        //if (convertView == null) {
        holder = new ViewHolder();

        layoutResourceId = R.layout.temperature_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.isProtected = mTemperatureInfo.isProtected();
        holder.dayButton = (Button) convertView.findViewById(R.id.day_button);
        holder.monthButton = (Button) convertView.findViewById(R.id.month_button);
        holder.yearButton = (Button) convertView.findViewById(R.id.year_button);
        holder.name = (TextView) convertView.findViewById(R.id.temperature_name);
        holder.data = (TextView) convertView.findViewById(R.id.temperature_data);
        holder.hardware = (TextView) convertView.findViewById(R.id.temperature_hardware);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);


        boolean toHot = false;
        if (mTemperatureInfo.getTemperature() > 30)
            toHot = true;

        Picasso.with(context).load(domoticz.getDrawableIcon(mTemperatureInfo.getTypeImg(), mTemperatureInfo.getType(), toHot)).into(holder.iconRow);

        holder.dayButton.setId(mTemperatureInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TemperatureInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mTemperatureInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TemperatureInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.MONTH);
                }
            }
        });
        holder.yearButton.setId(mTemperatureInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TemperatureInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.YEAR);
                }
            }
        });

        holder.name.setText(mTemperatureInfo.getName());
        holder.data.append(": " + mTemperatureInfo.getData());
        holder.hardware.append(": " + mTemperatureInfo.getHardwareName());

        convertView.setTag(holder);
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        TextView lastSeen;
        TextView setPoint;
        ImageButton buttonPlus;
        ImageView iconRow;
        ImageButton buttonMinus;
        Button dayButton;
        Button monthButton;
        Button yearButton;
        Boolean isProtected;
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