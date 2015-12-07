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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import nl.hnogames.domoticz.Containers.LogInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;

public class LogAdapter extends BaseAdapter implements Filterable {

    private static final String TAG = LogAdapter.class.getSimpleName();

    Context context;
    ArrayList<LogInfo> filteredData = null;
    ArrayList<LogInfo> data = null;
    Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();

    public LogAdapter(Context context,
                      ArrayList<LogInfo> data) {
        super();

        this.context = context;
        domoticz = new Domoticz(context);

        Collections.reverse(data);
        this.data = data;
        this.filteredData = data;
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

        LogInfo mLogInfo = filteredData.get(position);

        //if (convertView == null) {
        holder = new ViewHolder();

        String dateTime = mLogInfo.getMessage().substring(0, mLogInfo.getMessage().indexOf("  ")).trim();
        String message = mLogInfo.getMessage().substring(mLogInfo.getMessage().indexOf("  ") + 1).trim();

        layoutResourceId = R.layout.logs_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);

        holder.name = (TextView) convertView.findViewById(R.id.logs_name);
        holder.datetime = (TextView) convertView.findViewById(R.id.logs_datetime);
        holder.message = (TextView) convertView.findViewById(R.id.logs_message);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);

        if (message.indexOf(":") > 0) {
            holder.name.setText(message.substring(0, message.indexOf(":")).trim());
            holder.message.setText(message.substring(message.indexOf(":") + 1).trim());
        } else {
            if (message.startsWith("(")) {
                holder.name.setText(message.substring(0, message.indexOf(")")).replace("(", "").trim());
                holder.message.setText(message.substring(message.indexOf(")") + 1).trim());
            } else
                holder.name.setText(message);
        }

        holder.datetime.setText(dateTime);

        Picasso.with(context).load(R.drawable.text).into(holder.iconRow);

        convertView.setTag(holder);


        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView datetime;
        TextView message;
        ImageView iconRow;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<LogInfo> list = data;

            int count = list.size();
            final ArrayList<LogInfo> nlist = new ArrayList<LogInfo>(count);

            LogInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getMessage().toLowerCase().contains(filterString)) {
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
            filteredData = (ArrayList<LogInfo>) results.values;
            notifyDataSetChanged();
        }
    }

}