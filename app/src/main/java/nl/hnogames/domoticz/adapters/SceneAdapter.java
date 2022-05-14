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
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.material.chip.Chip;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ads.NativeTemplateStyle;
import nl.hnogames.domoticz.ads.TemplateView;
import nl.hnogames.domoticz.interfaces.ScenesClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;

@SuppressWarnings("unused")
public class SceneAdapter extends RecyclerView.Adapter<SceneAdapter.DataObjectHolder> implements RVHAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = SceneAdapter.class.getSimpleName();
    public static List<String> mCustomSorting;
    private final ScenesClickListener listener;
    private final Context context;
    private final Domoticz domoticz;
    private final SharedPrefUtil mSharedPrefs;
    private final ItemFilter mFilter = new ItemFilter();
    public ArrayList<SceneInfo> filteredData = null;
    private ArrayList<SceneInfo> data = null;
    private boolean adLoaded = false;

    public SceneAdapter(Context context,
                        Domoticz mDomoticz,
                        ArrayList<SceneInfo> data,
                        ScenesClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;
        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("scenes");
        setData(data);
        this.listener = listener;
    }

    public void setData(ArrayList<SceneInfo> data) {
        if (this.filteredData != null)
            SaveSorting();
        ArrayList<SceneInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= 21) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scene_row_default, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.scene_row_default_noads, parent, false);
        }
        return new DataObjectHolder(view);
    }

    private ArrayList<SceneInfo> SortData(ArrayList<SceneInfo> dat) {
        ArrayList<SceneInfo> data = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            data = dat;
        } else {
            for (SceneInfo d : dat) {
                if (d.getIdx() != MainActivity.ADS_IDX)
                    data.add(d);
            }
        }
        ArrayList<SceneInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            SceneInfo adView = null;
            for (String s : mCustomSorting) {
                for (SceneInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())) && d.getIdx() != MainActivity.ADS_IDX)
                        customdata.add(d);
                    if (d.getIdx() == MainActivity.ADS_IDX)
                        adView = d;
                }
            }
            for (SceneInfo d : data) {
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
        mSharedPrefs.saveSortingList("scenes", mCustomSorting);
    }

    public void onDestroy() {
        SaveSorting();
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final SceneInfo mSceneInfo = filteredData.get(position);

            holder.infoIcon.setTag(mSceneInfo.getIdx());
            holder.infoIcon.setOnClickListener(v -> listener.onItemLongClicked((int) v.getTag()));

            if (mSceneInfo.getIdx() == MainActivity.ADS_IDX) {
                setButtons(holder, Buttons.ADS);
                setAdsLayout(holder);
            } else if (DomoticzValues.Scene.Type.SCENE.equalsIgnoreCase(mSceneInfo.getType())) {
                holder.isProtected = mSceneInfo.isProtected();
                setButtons(holder, Buttons.SCENE);
                if (holder.buttonTimer != null)
                    holder.buttonTimer.setVisibility(View.GONE);
                if (holder.buttonNotifications != null)
                    holder.buttonNotifications.setVisibility(View.GONE);

                holder.switch_name.setText(mSceneInfo.getName());
                String text = context.getString(R.string.last_update)
                        + ": "
                        + UsefulBits.getFormattedDate(context,
                        mSceneInfo.getLastUpdateDateTime().getTime());
                holder.signal_level.setText(text);
                holder.switch_battery_level.setText(DomoticzValues.Scene.Type.SCENE);

                Picasso.get().load(DomoticzIcons.getDrawableIcon(
                        DomoticzValues.Scene.Type.SCENE.toLowerCase(),
                        null,
                        null,
                        false,
                        false,
                        null)).into(holder.iconRow);

                if (holder.buttonOn != null) {
                    holder.buttonOn.setId(mSceneInfo.getIdx());
                    //  holder.buttonOn.setText(context.getString(R.string.button_state_on));
                    holder.buttonOn.setOnClickListener(view -> handleClick(view.getId(), true));
                    if (holder.isProtected) {
                        holder.buttonOn.setEnabled(false);
                    }
                }

                if (holder.likeButton != null) {
                    holder.likeButton.setId(mSceneInfo.getIdx());
                    holder.likeButton.setLiked(mSceneInfo.getFavoriteBoolean());
                    holder.likeButton.setOnLikeListener(new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), true);
                        }

                        @Override
                        public void unLiked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), false);
                        }
                    });
                }
                if (holder.buttonLog != null) {
                    holder.buttonLog.setId(mSceneInfo.getIdx());
                    holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
                }
            } else if (mSceneInfo.getType().equalsIgnoreCase(DomoticzValues.Scene.Type.GROUP)) {
                holder.isProtected = mSceneInfo.isProtected();

                setButtons(holder, Buttons.GROUP);

                if (holder.buttonTimer != null)
                    holder.buttonTimer.setVisibility(View.GONE);
                if (holder.buttonNotifications != null)
                    holder.buttonNotifications.setVisibility(View.GONE);

                holder.switch_name.setText(mSceneInfo.getName());

                String text = context.getString(R.string.last_update)
                        + ": "
                        + UsefulBits.getFormattedDate(context,
                        mSceneInfo.getLastUpdateDateTime().getTime());

                holder.signal_level.setText(text);
                holder.switch_battery_level.setText(DomoticzValues.Scene.Type.GROUP);
                if (holder.buttonOn != null) {
                    holder.buttonOn.setId(mSceneInfo.getIdx());
                    holder.buttonOn.setOnClickListener(v -> handleClick(v.getId(), true));
                }

                if (holder.buttonOff != null) {
                    holder.buttonOff.setId(mSceneInfo.getIdx());
                    holder.buttonOff.setOnClickListener(v -> handleClick(v.getId(), false));
                }

                Picasso.get().load(DomoticzIcons.getDrawableIcon(
                        DomoticzValues.Scene.Type.GROUP.toLowerCase(),
                        null,
                        null,
                        mSceneInfo.getStatusInBoolean(),
                        false,
                        null)).into(holder.iconRow);

                if (!mSceneInfo.getStatusInBoolean())
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);

                if (holder.likeButton != null) {
                    holder.likeButton.setId(mSceneInfo.getIdx());
                    holder.likeButton.setLiked(mSceneInfo.getFavoriteBoolean());
                    holder.likeButton.setOnLikeListener(new OnLikeListener() {
                        @Override
                        public void liked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), true);
                        }

                        @Override
                        public void unLiked(LikeButton likeButton) {
                            handleLikeButtonClick(likeButton.getId(), false);
                        }
                    });
                }
                if (holder.buttonLog != null) {
                    holder.buttonLog.setId(mSceneInfo.getIdx());
                    holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
                }
            } else throw new NullPointerException("Scene type not supported in the adapter for:\n"
                    + mSceneInfo);
            holder.itemView.setOnClickListener(v -> listener.onItemClicked(v, position));
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

            MobileAds.initialize(context, context.getString(R.string.ADMOB_APP_KEY));
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("A18F9718FC3511DC6BCB1DC5AF076AE4")
                    .addTestDevice("1AAE9D81347967A359E372B0445549DE")
                    .addTestDevice("440E239997F3D1DD8BC59D0ADC9B5DB5")
                    .addTestDevice("D6A4EE627F1D3912332E0BFCA8EA2AD2")
                    .addTestDevice("6C2390A9FF8F555BD01BA560068CD366")
                    .build();

            AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.ad_unit_id))
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
    public int getItemCount() {
        return filteredData.size();
    }

    public void setButtons(DataObjectHolder holder, int button) {
        if (holder.adview != null)
            holder.adview.setVisibility(View.GONE);
        if (holder.contentWrapper != null)
            holder.contentWrapper.setVisibility(View.VISIBLE);
        if (holder.buttonLog != null) {
            holder.buttonLog.setVisibility(View.GONE);
        }
        if (holder.buttonTimer != null) {
            holder.buttonTimer.setVisibility(View.GONE);
        }
        if (holder.buttonOff != null) {
            holder.buttonOff.setVisibility(View.GONE);
        }
        if (holder.buttonOn != null) {
            holder.buttonOn.setVisibility(View.GONE);
        }
        if (holder.switch_name != null)
            holder.switch_name.setVisibility(View.VISIBLE);
        if (holder.signal_level != null)
            holder.signal_level.setVisibility(View.VISIBLE);
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setVisibility(View.VISIBLE);
        if (holder.iconRow != null)
            holder.iconRow.setVisibility(View.VISIBLE);

        switch (button) {
            case Buttons.ADS:
                if (holder.adview != null)
                    holder.adview.setVisibility(View.VISIBLE);
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.GONE);
                break;
            case Buttons.SCENE:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.buttonOn.setVisibility(View.VISIBLE);
                holder.buttonLog.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.GROUP:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.buttonOn.setVisibility(View.VISIBLE);
                holder.buttonOff.setVisibility(View.VISIBLE);
                holder.buttonLog.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
        }
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    public void handleClick(int idx, boolean action) {
        listener.onSceneClick(idx, action);
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
        filteredData.remove(position);
        notifyItemRemoved(position);
    }

    private void swap(int firstPosition, int secondPosition) {
        if (firstPosition == (secondPosition + 1) || firstPosition == (secondPosition - 1)) {
            Collections.swap(filteredData, firstPosition, secondPosition);
            notifyItemMoved(firstPosition, secondPosition);
        } else {
            if (firstPosition < secondPosition) {
                for (int i = firstPosition; i < secondPosition; i++) {
                    Collections.swap(filteredData, i, i + 1);
                    notifyItemMoved(i, i + 1);
                }
            } else {
                for (int i = firstPosition; i > secondPosition; i--) {
                    Collections.swap(filteredData, i, i - 1);
                    notifyItemMoved(i, i - 1);
                }
            }
        }

        List<String> ids = new ArrayList<>();
        for (SceneInfo d : filteredData) {
            if (d.getIdx() != -9998)
                ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
    }

    interface Buttons {
        int SCENE = 0;
        int GROUP = 1;
        int ADS = 2;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        TextView switch_name, signal_level, switch_battery_level;
        Boolean isProtected;
        ImageView iconRow;
        LikeButton likeButton;
        LinearLayout extraPanel;
        Button buttonOn, buttonOff;
        Chip buttonLog, buttonTimer, buttonNotifications;
        ImageView infoIcon;
        TemplateView adview;
        RelativeLayout contentWrapper;

        public DataObjectHolder(View itemView) {
            super(itemView);

            contentWrapper = itemView.findViewById(R.id.contentWrapper);
            adview = itemView.findViewById(R.id.adview);
            buttonOn = itemView.findViewById(R.id.on_button);
            signal_level = itemView.findViewById(R.id.switch_signal_level);
            iconRow = itemView.findViewById(R.id.rowIcon);
            switch_name = itemView.findViewById(R.id.switch_name);
            switch_battery_level = itemView.findViewById(R.id.switch_battery_level);
            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            buttonLog = itemView.findViewById(R.id.log_button);
            buttonTimer = itemView.findViewById(R.id.timer_button);
            buttonNotifications = itemView.findViewById(R.id.notifications_button);
            likeButton = itemView.findViewById(R.id.fav_button);

            if (buttonTimer != null)
                buttonTimer.setVisibility(View.GONE);
            if (buttonNotifications != null)
                buttonNotifications.setVisibility(View.GONE);

            likeButton = itemView.findViewById(R.id.fav_button);
            iconRow = itemView.findViewById(R.id.rowIcon);
            buttonLog = itemView.findViewById(R.id.log_button);
            buttonOff = itemView.findViewById(R.id.off_button);

            extraPanel = itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
        }

        @Override
        public void onItemSelected(int actionstate) {
            System.out.println("Item is selected");
        }

        @Override
        public void onItemClear() {
            System.out.println("Item is unselected");
        }
    }

    /**
     * Item filter
     */
    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<SceneInfo> list = data;

            int count = list.size();
            final ArrayList<SceneInfo> devicesInfos = new ArrayList<>(count);

            SceneInfo filterableObject;
            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString) || (filterableObject.getType() != null && filterableObject.getType().equals("advertisement"))) {
                    devicesInfos.add(filterableObject);
                }
            }
            results.values = devicesInfos;
            results.count = devicesInfos.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<SceneInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}