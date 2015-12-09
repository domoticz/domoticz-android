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
import android.text.TextUtils;
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

import nl.hnogames.domoticz.Containers.WeatherInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.WeatherClickListener;
import nl.hnogames.domoticz.R;

public class WeatherAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = WeatherAdapter.class.getSimpleName();

    private final WeatherClickListener listener;
    public ArrayList<WeatherInfo> filteredData = null;
    private Context context;
    private ArrayList<WeatherInfo> data = null;
    private Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();

    public WeatherAdapter(Context context,
                          ArrayList<WeatherInfo> data,
                          WeatherClickListener listener) {
        super();
        this.context = context;
        domoticz = new Domoticz(context);
        Collections.sort(data, new Comparator<WeatherInfo>() {
            @Override
            public int compare(WeatherInfo left, WeatherInfo right) {
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

        WeatherInfo mWeatherInfo = filteredData.get(position);
        final long setPoint = mWeatherInfo.getSetPoint();

        //if (convertView == null) {
        holder = new ViewHolder();

        layoutResourceId = R.layout.weather_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.isProtected = mWeatherInfo.isProtected();
        holder.name = (TextView) convertView.findViewById(R.id.weather_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.data = (TextView) convertView.findViewById(R.id.weather_data);
        holder.hardware = (TextView) convertView.findViewById(R.id.weather_hardware);
        holder.dayButton = (Button) convertView.findViewById(R.id.day_button);
        holder.monthButton = (Button) convertView.findViewById(R.id.month_button);
        holder.yearButton = (Button) convertView.findViewById(R.id.year_button);

        holder.name.setText(mWeatherInfo.getName());
        holder.hardware.append(": " + mWeatherInfo.getHardwareName());

        holder.data.setEllipsize(TextUtils.TruncateAt.END);
        holder.data.setMaxLines(3);
        holder.data.append(mWeatherInfo.getData());

        if (mWeatherInfo.getForecastStr() != null && mWeatherInfo.getForecastStr().length() > 0)
            holder.data.append(", " + mWeatherInfo.getForecastStr());
        if (mWeatherInfo.getSpeed() != null && mWeatherInfo.getSpeed().length() > 0)
            holder.data.append(", " + context.getString(R.string.speed) + ": " + mWeatherInfo.getSpeed());
        if (mWeatherInfo.getDewPoint() > 0)
            holder.data.append(", " + context.getString(R.string.drewpoint) + ": " + mWeatherInfo.getDewPoint());
        if (mWeatherInfo.getTemp() > 0)
            holder.data.append(", " + context.getString(R.string.temp) + ": " + mWeatherInfo.getTemp());
        if (mWeatherInfo.getBarometer() > 0)
            holder.data.append(", " + context.getString(R.string.barometer) + ": " + mWeatherInfo.getBarometer());
        if (mWeatherInfo.getChill() != null && mWeatherInfo.getChill().length() > 0)
            holder.data.append(", " + context.getString(R.string.chill) + ": " + mWeatherInfo.getChill());
        if (mWeatherInfo.getDirectionStr() != null && mWeatherInfo.getDirectionStr().length() > 0)
            holder.data.append(", " + context.getString(R.string.direction) + ": " + mWeatherInfo.getDirectionStr());
        if (mWeatherInfo.getHumidityStatus() != null && mWeatherInfo.getHumidityStatus().length() > 0)
            holder.data.append(", " + context.getString(R.string.humidity) + ": " + mWeatherInfo.getHumidityStatus());

        holder.dayButton.setId(mWeatherInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (WeatherInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.DAY);
                }
            }
        });

        holder.monthButton.setId(mWeatherInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (WeatherInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.MONTH);
                }
            }
        });

        holder.yearButton.setId(mWeatherInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (WeatherInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.YEAR);
                }
            }
        });

        convertView.setTag(holder);
        Picasso.with(context).load(domoticz.getDrawableIcon(mWeatherInfo.getTypeImg(), mWeatherInfo.getType(), false)).into(holder.iconRow);
        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        TextView lastSeen;
        TextView setPoint;
        ImageButton buttonPlus;
        ImageButton buttonMinus;
        ImageView iconRow;
        Boolean isProtected;
        Button dayButton;
        Button monthButton;
        Button yearButton;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<WeatherInfo> list = data;

            int count = list.size();
            final ArrayList<WeatherInfo> nlist = new ArrayList<WeatherInfo>(count);

            WeatherInfo filterableObject;

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
            filteredData = (ArrayList<WeatherInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}