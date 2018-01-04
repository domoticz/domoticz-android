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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.PlanInfo;

@SuppressWarnings("unused")
public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.DataObjectHolder> {
    private static onClickListener onClickListener;
    private final Context mContext;
    private SharedPrefUtil mSharedPrefs;
    private ArrayList<PlanInfo> mDataset;

    public PlansAdapter(ArrayList<PlanInfo> data, Context mContext) {
        setData(data);
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);
    }

    public void setOnItemClickListener(onClickListener onClickListener) {
        PlansAdapter.onClickListener = onClickListener;
    }

    public void setData(ArrayList<PlanInfo> data) {
        this.mDataset = data;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plan_row, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            ((android.support.v7.widget.CardView) view.findViewById(R.id.row_global_wrapper)).setCardBackgroundColor(Color.parseColor("#3F3F3F"));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        try {
            if (mDataset != null && mDataset.size() > 0) {
                String name = mDataset.get(position).getName();
                holder.name.setText(name);

                int numberOfDevices = mDataset.get(position).getDevices();
                String text = mContext.getResources().getQuantityString(R.plurals.devices, numberOfDevices, numberOfDevices);
                holder.devices.setText(String.valueOf(text));
            }
        }catch(Exception ex){}
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface onClickListener {
        void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView name;
        TextView devices;

        public DataObjectHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            devices = (TextView) itemView.findViewById(R.id.devices);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onItemClick(getLayoutPosition(), v);
        }
    }
}