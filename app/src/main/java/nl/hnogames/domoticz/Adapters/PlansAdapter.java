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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.PlanInfo;

@SuppressWarnings("unused")
public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.DataObjectHolder> implements RVHAdapter {
    public static List<String> mCustomSorting;
    private static onClickListener onClickListener;
    private final Context mContext;
    private SharedPrefUtil mSharedPrefs;
    private ArrayList<PlanInfo> mDataset;

    public PlansAdapter(ArrayList<PlanInfo> data, Context mContext) {
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);

        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("plans");
        setData(data);
    }

    public void setOnItemClickListener(onClickListener onClickListener) {
        PlansAdapter.onClickListener = onClickListener;
    }

    public void setData(ArrayList<PlanInfo> data) {
        ArrayList<PlanInfo> sortedData = SortData(data);
        this.mDataset = sortedData;
    }

    private ArrayList<PlanInfo> SortData(ArrayList<PlanInfo> data) {
        ArrayList<PlanInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            for (String s : mCustomSorting) {
                for (PlanInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())))
                        customdata.add(d);
                }
            }
            for (PlanInfo d : data) {
                if (!customdata.contains(d))
                    customdata.add(d);
            }
        } else
            customdata = data;
        return customdata;
    }

    private void SaveSorting() {
        List<String> ids = new ArrayList<>();
        for (PlanInfo d : mDataset) {
            ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
        mSharedPrefs.saveSortingList("plans", ids);
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plan_row, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((view.findViewById(R.id.card_global_wrapper)) != null)
                view.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(mContext, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(mContext, R.color.card_background_dark));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        try {
            if (mDataset != null && mDataset.size() > 0) {
                String name = mDataset.get(position).getName();
                holder.name.setText(name);
                holder.iconRow.setAlpha(0.5f);
                int numberOfDevices = mDataset.get(position).getDevices();
                String text = mContext.getResources().getQuantityString(R.plurals.devices, numberOfDevices, numberOfDevices);
                holder.devices.setText(String.valueOf(text));
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }

    private void remove(int position) {
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    private void swap(int firstPosition, int secondPosition) {
        Collections.swap(mDataset, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
        SaveSorting();
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface onClickListener {
        void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener, RVHViewHolder {
        TextView name;
        TextView devices;
        ImageView iconRow;

        public DataObjectHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            devices = itemView.findViewById(R.id.devices);
            iconRow = itemView.findViewById(R.id.rowIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onItemSelected(int actionstate) {
            System.out.println("Item is selected");
        }

        @Override
        public void onItemClear() {
            System.out.println("Item is unselected");
        }

        @Override
        public void onClick(View v) {
            onClickListener.onItemClick(getLayoutPosition(), v);
        }
    }
}