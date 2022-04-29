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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import androidx.recyclerview.widget.RecyclerView;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.LogInfo;
import nl.hnogames.domoticzapi.Domoticz;

@SuppressWarnings("unused")
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.DataObjectHolder> {
    private static final String TAG = LogAdapter.class.getSimpleName();

    private final Context context;
    private final Domoticz domoticz;
    private final SharedPrefUtil mSharedPrefs;
    private final ItemFilter mFilter = new ItemFilter();
    private ArrayList<LogInfo> filteredData = null;
    private ArrayList<LogInfo> data = null;

    public LogAdapter(Context context,
                      Domoticz mDomoticz,
                      ArrayList<LogInfo> data) {
        super();

        this.context = context;
        this.domoticz = mDomoticz;
        mSharedPrefs = new SharedPrefUtil(context);
        setData(data);
    }

    public void setData(ArrayList<LogInfo> data) {
        Collections.reverse(data);
        this.data = data;
        this.filteredData = data;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.logs_row_default, parent, false);

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final LogInfo mLogInfo = filteredData.get(position);
            String dateTime = "";
            String message = "";
            String name = "";

            if (mLogInfo.getMessage().contains("  ")) {
                dateTime = mLogInfo.getMessage().substring(0, mLogInfo.getMessage().indexOf("  ")).trim();
                message = mLogInfo.getMessage().substring(mLogInfo.getMessage().indexOf("  ") + 1).trim();
            } else
                message = mLogInfo.getMessage();
            if (message.indexOf(":") > 0) {
                name = (message.substring(0, message.indexOf(":")).trim());
                message = (message.substring(message.indexOf(":") + 1).trim());
            } else {
                if (message.startsWith("(")) {
                    name = (message.substring(0, message.indexOf(")")).replace("(", "").trim());
                    message = (message.substring(message.indexOf(")") + 1).trim());
                } else
                    name = (message);
            }
            holder.datetime.setText(dateTime);
            holder.name.setText(name);
            holder.message.setText(message);

            if (mLogInfo.getLevel() == 4)
                Picasso.get().load(R.drawable.power).into(holder.iconRow);
            else if (mLogInfo.getLevel() == 2)
                Picasso.get().load(R.drawable.power).into(holder.iconRow);
            else
                Picasso.get().load(R.drawable.power).into(holder.iconRow);
        }
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public interface onClickListener {
        void onItemClick(int position, View v);
    }


    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView name;
        TextView datetime;
        TextView message;
        ImageView iconRow;

        public DataObjectHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.logs_name);
            datetime = itemView.findViewById(R.id.logs_datetime);
            message = itemView.findViewById(R.id.logs_message);
            iconRow = itemView.findViewById(R.id.rowIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
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