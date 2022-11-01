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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ads.NativeTemplateStyle;
import nl.hnogames.domoticz.ads.TemplateView;
import nl.hnogames.domoticz.helpers.ItemMoveAdapter;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.PlanInfo;

@SuppressWarnings("unused")
public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.DataObjectHolder> implements ItemMoveAdapter {
    public static List<String> mCustomSorting;
    private static onClickListener onClickListener;
    private final Context mContext;
    private final SharedPrefUtil mSharedPrefs;
    private ArrayList<PlanInfo> mDataset;
    private boolean adLoaded = false;

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
        if (this.mDataset != null)
            SaveSorting();
        ArrayList<PlanInfo> sortedData = SortData(data);
        this.mDataset = sortedData;
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
        if (Build.VERSION.SDK_INT >= 21) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.plan_row, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.plan_row_noads, parent, false);
        }
        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        try {
            if (mDataset != null && mDataset.size() > 0) {
                PlanInfo plan = mDataset.get(position);
                if (plan.getIdx() == MainActivity.ADS_IDX) {
                    holder.contentWrapper.setVisibility(View.GONE);
                    holder.iconRow.setVisibility(View.GONE);
                    holder.name.setVisibility(View.GONE);
                    holder.devices.setVisibility(View.GONE);
                    setAdsLayout(holder);
                } else {
                    holder.itemView.setVisibility(View.VISIBLE);
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                    holder.adview.setVisibility(View.GONE);
                    holder.iconRow.setVisibility(View.VISIBLE);
                    holder.name.setVisibility(View.VISIBLE);
                    holder.devices.setVisibility(View.VISIBLE);
                    String name = plan.getName();
                    holder.name.setText(name);
                    holder.iconRow.setAlpha(0.5f);
                    int numberOfDevices = plan.getDevices();
                    String text = mContext.getResources().getQuantityString(R.plurals.devices, numberOfDevices, numberOfDevices);
                    holder.devices.setText(text);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * Set the data for the ads row
     *
     * @param holder Holder to use
     */
    private void setAdsLayout(DataObjectHolder holder) {
        try {
            if (holder.adview == null)
                return;
            if (!adLoaded)
                holder.adview.setVisibility(View.GONE);

            MobileAds.initialize(mContext, mContext.getString(R.string.ADMOB_APP_KEY));
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("A18F9718FC3511DC6BCB1DC5AF076AE4")
                    .addTestDevice("1AAE9D81347967A359E372B0445549DE")
                    .addTestDevice("440E239997F3D1DD8BC59D0ADC9B5DB5")
                    .addTestDevice("D6A4EE627F1D3912332E0BFCA8EA2AD2")
                    .addTestDevice("6C2390A9FF8F555BD01BA560068CD366")
                    .addTestDevice("2C114D01992840EC6BF853D44CB96754")
                    .addTestDevice("7ABE5FC9B0E902B7CF857CE3A57831AB")
                    .build();

            AdLoader adLoader = new AdLoader.Builder(mContext, mContext.getString(R.string.ad_unit_id))
                    .forUnifiedNativeAd(unifiedNativeAd -> {
                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().build();
                        if (holder.adview != null) {
                            holder.adview.setStyles(styles);
                            holder.adview.setNativeAd(unifiedNativeAd);
                            holder.adview.setVisibility(View.VISIBLE);
                            adLoaded = true;
                        }
                    })
                    .withAdListener(new AdListener() {
                        @Override
                        public void onAdFailedToLoad(int errorCode) {
                            if (holder.adview != null)
                                holder.adview.setVisibility(View.GONE);
                        }
                    })
                    .withNativeAdOptions(new NativeAdOptions.Builder().build())
                    .build();
            adLoader.loadAd(adRequest);
        } catch (Exception ignored) {
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
        TextView name;
        TextView devices;
        ImageView iconRow;
        TemplateView adview;
        RelativeLayout contentWrapper;

        public DataObjectHolder(View itemView) {
            super(itemView);
            contentWrapper = itemView.findViewById(R.id.contentWrapper);
            adview = itemView.findViewById(R.id.adview);
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