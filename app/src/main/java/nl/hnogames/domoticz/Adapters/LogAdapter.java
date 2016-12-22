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

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.LogInfo;
import nl.hnogames.domoticzapi.Domoticz;

@SuppressWarnings("unused")
public class LogAdapter extends RecyclerView.Adapter<LogAdapter.DataObjectHolder> {
    private static final String TAG = LogAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<LogInfo> filteredData = null;
    private ArrayList<LogInfo> data = null;
    private Domoticz domoticz;
    private SharedPrefUtil mSharedPrefs;
    private ItemFilter mFilter = new ItemFilter();

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


        if (mSharedPrefs.darkThemeEnabled()) {
            ((android.support.v7.widget.CardView) view.findViewById(R.id.card_global_wrapper)).setCardBackgroundColor(Color.parseColor("#3F3F3F"));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, int position) {

        if (filteredData != null && filteredData.size() > 0) {
            final LogInfo mLogInfo = filteredData.get(position);
            String dateTime = "";
            String message = "";

            if (mLogInfo.getMessage().indexOf("  ") >= 0) {
                dateTime = mLogInfo.getMessage().substring(0, mLogInfo.getMessage().indexOf("  ")).trim();
                message = mLogInfo.getMessage().substring(mLogInfo.getMessage().indexOf("  ") + 1).trim();
            } else
                message = mLogInfo.getMessage();
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

            name = (TextView) itemView.findViewById(R.id.logs_name);
            datetime = (TextView) itemView.findViewById(R.id.logs_datetime);
            message = (TextView) itemView.findViewById(R.id.logs_message);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);

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