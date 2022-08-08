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
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.ItemMoveAdapter;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.PlanInfo;

@SuppressWarnings("unused")
public class SmallPlansAdapter extends RecyclerView.Adapter<SmallPlansAdapter.DataObjectHolder> implements ItemMoveAdapter {
    public static List<String> mCustomSorting;
    private static onClickListener onClickListener;
    private final Context mContext;
    private final SharedPrefUtil mSharedPrefs;
    private ArrayList<PlanInfo> mDataset;
    private final boolean adLoaded = false;

    public SmallPlansAdapter(ArrayList<PlanInfo> data, Context mContext) {
        this.mContext = mContext;
        mSharedPrefs = new SharedPrefUtil(mContext);

        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("plans");
        setData(data);
    }

    public void setOnItemClickListener(onClickListener onClickListener) {
        SmallPlansAdapter.onClickListener = onClickListener;
    }

    public void setData(ArrayList<PlanInfo> data) {
        if (this.mDataset != null)
            SaveSorting();
        ArrayList<PlanInfo> sortedData = SortData(data);
        this.mDataset = sortedData;
    }

    public PlanInfo getData(int counter) {
        try {
            if (this.mDataset != null)
                return this.mDataset.get(counter);
        } catch (Exception ex) {
        }
        return null;
    }

    private ArrayList<PlanInfo> SortData(ArrayList<PlanInfo> dat) {
        ArrayList<PlanInfo> data = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            data = dat;
        } else {
            for (PlanInfo d : dat) {
                if (d.getIdx() != MainActivity.ADS_IDX)
                    data.add(d);
            }
        }
        ArrayList<PlanInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            PlanInfo adView = null;
            for (String s : mCustomSorting) {
                for (PlanInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())) && d.getIdx() != MainActivity.ADS_IDX)
                        customdata.add(d);
                    if (d.getIdx() == MainActivity.ADS_IDX)
                        adView = d;
                }
            }
            for (PlanInfo d : data) {
                if (!customdata.contains(d) && d.getIdx() != MainActivity.ADS_IDX)
                    customdata.add(d);
            }
            if (adView != null && customdata != null && customdata.size() > 0)
                customdata.add(1, adView);
        } else
            customdata = data;
        return customdata;
    }

    private void SaveSorting() {
        mSharedPrefs.saveSortingList("plans", mCustomSorting);
    }

    public void onDestroy() {
        SaveSorting();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.small_plan_row, parent, false);
        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        try {
            if (mDataset != null && mDataset.size() > 0) {
                PlanInfo plan = mDataset.get(position);
                holder.itemView.setVisibility(View.VISIBLE);
                holder.plan.setVisibility(View.VISIBLE);
                String name = plan.getName();
                int numberOfDevices = plan.getDevices();
                holder.plan.setText(name + " (" + numberOfDevices + ")");
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
        if (firstPosition == (secondPosition + 1) || firstPosition == (secondPosition - 1)) {
            Collections.swap(mDataset, firstPosition, secondPosition);
            notifyItemMoved(firstPosition, secondPosition);
        } else {
            if (firstPosition < secondPosition) {
                for (int i = firstPosition; i < secondPosition; i++) {
                    Collections.swap(mDataset, i, i + 1);
                    notifyItemMoved(i, i + 1);
                }
            } else {
                for (int i = firstPosition; i > secondPosition; i--) {
                    Collections.swap(mDataset, i, i - 1);
                    notifyItemMoved(i, i - 1);
                }
            }
        }

        List<String> ids = new ArrayList<>();
        for (PlanInfo d : mDataset) {
            if (d.getIdx() != -9998)
                ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface onClickListener {
        void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener, RVHViewHolder {
        Chip plan;

        public DataObjectHolder(View itemView) {
            super(itemView);
            plan = itemView.findViewById(R.id.plan);
            plan.setOnClickListener(this);
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