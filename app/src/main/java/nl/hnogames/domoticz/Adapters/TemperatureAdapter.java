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
import android.content.res.TypedArray;
import android.support.design.chip.Chip;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.Interfaces.TemperatureClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.PlanInfo;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

@SuppressWarnings("unused")
public class TemperatureAdapter extends RecyclerView.Adapter<TemperatureAdapter.DataObjectHolder>  implements RVHAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = TemperatureAdapter.class.getSimpleName();
    private final TemperatureClickListener listener;
    public ArrayList<TemperatureInfo> filteredData = null;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private ConfigInfo mConfigInfo;
    private Context context;
    private ArrayList<TemperatureInfo> data = null;
    private ItemFilter mFilter = new ItemFilter();
    public static List<String> mCustomSorting;

    public TemperatureAdapter(Context context,
                              Domoticz mDomoticz,
                              ServerUtil configInfo,
                              ArrayList<TemperatureInfo> data,
                              TemperatureClickListener listener) {
        super();

        this.context = context;
        this.mConfigInfo = configInfo.getActiveServer() != null ? configInfo.getActiveServer().getConfigInfo(context) : null;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;
        this.listener = listener;
        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("temperature");
        setData(data);
    }

    public void setData(ArrayList<TemperatureInfo> data) {
            ArrayList<TemperatureInfo > sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    private ArrayList<TemperatureInfo> SortData(ArrayList<TemperatureInfo> data) {
        ArrayList<TemperatureInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            for (String s : mCustomSorting) {
                for (TemperatureInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())))
                        customdata.add(d);
                }
            }
            for (TemperatureInfo d : data) {
                if (!customdata.contains(d))
                    customdata.add(d);
            }
        } else
            customdata = data;
        return customdata;
    }

    private void SaveSorting() {
        List<String> ids = new ArrayList<>();
        for (TemperatureInfo d : filteredData) {
            ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
        mSharedPrefs.saveSortingList("plans", ids);
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.temperature_row_default, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            if ((view.findViewById(R.id.card_global_wrapper)) != null)
                view.findViewById(R.id.card_global_wrapper).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.color.card_background_dark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.card_background_dark));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {

        if (filteredData != null && filteredData.size() > 0) {
            final TemperatureInfo mTemperatureInfo = filteredData.get(position);

            if (mSharedPrefs.darkThemeEnabled()) {
                if (holder.setButton != null)
                    holder.setButton.setBackgroundColor(ContextCompat.getColor(context, R.color.button_dark));
            }
            holder.infoIcon.setTag(mTemperatureInfo.getIdx());
            holder.infoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemLongClicked((int) v.getTag());
                }
            });
            holder.isProtected = mTemperatureInfo.isProtected();
            String sign = mConfigInfo != null ? mConfigInfo.getTempSign() : "C";

            int modeIconRes = 0;
            if ((!UsefulBits.isEmpty(sign) && sign.equals("C") && mTemperatureInfo.getTemperature() < 0) ||
                (!UsefulBits.isEmpty(sign) && sign.equals("F") && mTemperatureInfo.getTemperature() < 30)) {
                Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mTemperatureInfo.getTypeImg(),
                    mTemperatureInfo.getType(),
                    null,
                    (mConfigInfo != null && mTemperatureInfo.getTemperature() > mConfigInfo.getDegreeDaysBaseTemperature()) ? true : false,
                    true,
                    "Freezing")).into(holder.iconRow);
            } else {
                Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mTemperatureInfo.getTypeImg(),
                    mTemperatureInfo.getType(),
                    null,
                    (mConfigInfo != null && mTemperatureInfo.getTemperature() > mConfigInfo.getDegreeDaysBaseTemperature()) ? true : false,
                    false,
                    null)).into(holder.iconRow);
            }

            if (!UsefulBits.isEmpty(mTemperatureInfo.getHardwareName()) && mTemperatureInfo.getHardwareName().equalsIgnoreCase(DomoticzValues.Device.Hardware.EVOHOME)) {
                holder.setButton.setVisibility(View.VISIBLE);
                holder.pieView.setVisibility(View.GONE);
                modeIconRes = getEvohomeStateIcon(mTemperatureInfo.getStatus());
            } else {
                holder.setButton.setVisibility(View.GONE);
                if (!UsefulBits.isEmpty(mTemperatureInfo.getType()) && mTemperatureInfo.getType().equalsIgnoreCase(DomoticzValues.Device.Type.Name.WIND)) {
                    holder.pieView.setVisibility(View.GONE);
                } else {
                    holder.pieView.setVisibility(View.VISIBLE);
                    if (!this.mSharedPrefs.darkThemeEnabled()) {
                        holder.pieView.setInnerBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        holder.pieView.setTextColor(ContextCompat.getColor(context, R.color.black));
                    }
                    holder.pieView.setPercentageTextSize(16);
                    holder.pieView.setPercentageBackgroundColor(ContextCompat.getColor(context, R.color.material_orange_600));
                    double temp = mTemperatureInfo.getTemperature();
                    if (!UsefulBits.isEmpty(sign) && !sign.equals("C"))
                        temp = temp / 2;
                    holder.pieView.setPercentage(Float.valueOf(temp + ""));
                    holder.pieView.setInnerText(mTemperatureInfo.getTemperature() + " " + sign);

                    PieAngleAnimation animation = new PieAngleAnimation(holder.pieView);
                    animation.setDuration(2000);
                    holder.pieView.startAnimation(animation);
                }
            }

            holder.setButton.setText(context.getString(R.string.set_temperature));
            holder.setButton.setId(mTemperatureInfo.getIdx());
            holder.setButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (TemperatureInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onSetClick(t);
                    }
                }
            });

            holder.dayButton.setId(mTemperatureInfo.getIdx());
            holder.dayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (TemperatureInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                    }
                }
            });
            holder.monthButton.setId(mTemperatureInfo.getIdx());
            holder.monthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (TemperatureInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                    }
                }
            });

            holder.weekButton.setVisibility(View.GONE);
            holder.weekButton.setId(mTemperatureInfo.getIdx());
            holder.weekButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (TemperatureInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
                    }
                }
            });
            holder.yearButton.setId(mTemperatureInfo.getIdx());
            holder.yearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (TemperatureInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                    }
                }
            });

            if (holder.likeButton != null) {
                holder.likeButton.setId(mTemperatureInfo.getIdx());
                holder.likeButton.setLiked(mTemperatureInfo.getFavoriteBoolean());
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

            if (!UsefulBits.isEmpty(mTemperatureInfo.getName()))
                holder.name.setText(mTemperatureInfo.getName());
            if (!UsefulBits.isEmpty(mTemperatureInfo.getType()) && mTemperatureInfo.getType().equalsIgnoreCase(DomoticzValues.Device.Type.Name.WIND)) {
                holder.data.setText(R.string.wind);
                holder.data.append(": " + mTemperatureInfo.getData() + " " + mTemperatureInfo.getDirection());
                holder.data2.setText(context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mTemperatureInfo.getLastUpdateDateTime().getTime()));
                holder.data2.setVisibility(View.VISIBLE);
            } else {
                double temperature = mTemperatureInfo.getTemperature();
                double setPoint = mTemperatureInfo.getSetPoint();
                if (temperature <= 0 || setPoint <= 0) {
                    holder.data.setText(context.getString(R.string.temperature) + ": " + mTemperatureInfo.getData());
                    holder.data2.setText(context.getString(R.string.last_update)
                        + ": "
                        + UsefulBits.getFormattedDate(context,
                        mTemperatureInfo.getLastUpdateDateTime().getTime()));
                    holder.data2.setVisibility(View.VISIBLE);
                } else {
                    holder.data.setText(context.getString(R.string.temperature) + ": " + mTemperatureInfo.getTemperature() + " " + sign);
                    holder.data2.setText(context.getString(R.string.set_point) + ": " + mTemperatureInfo.getSetPoint() + " " + sign);
                    holder.data2.setVisibility(View.VISIBLE);
                }
            }

            if (holder.iconMode != null) {
                if (modeIconRes == 0) {
                    holder.iconMode.setVisibility(View.GONE);
                } else {
                    holder.iconMode.setImageResource(modeIconRes);
                    holder.iconMode.setVisibility(View.VISIBLE);
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, position);
                }
            });
        }
    }

    public int getEvohomeStateIcon(String stateName) {
        if (stateName == null) return 0;

        TypedArray icons = context.getResources().obtainTypedArray(R.array.evohome_zone_state_icons);
        String[] states = context.getResources().getStringArray(R.array.evohome_zone_states);
        int i = 0;
        int iconRes = 0;
        for (String state : states) {
            if (stateName.equals(state)) {
                iconRes = icons.getResourceId(i, 0);
                break;
            }
            i++;
        }

        icons.recycle();
        return iconRes;
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
        Collections.swap(filteredData, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
        SaveSorting();
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements RVHViewHolder
    {
        TextView name;
        TextView data;
        TextView data2;
        ImageView iconRow;
        ImageView iconMode;
        Button setButton;
        Chip dayButton;
        Chip monthButton;
        Chip weekButton;
        Chip yearButton;
        Boolean isProtected;
        LikeButton likeButton;
        LinearLayout extraPanel;
        PieView pieView;
        ImageView infoIcon;

        @Override
        public void onItemSelected(int actionstate) {
            System.out.println("Item is selected");
        }

        @Override
        public void onItemClear() {
            System.out.println("Item is unselected");
        }

        public DataObjectHolder(View itemView) {
            super(itemView);
            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            name = itemView.findViewById(R.id.temperature_name);
            data = itemView.findViewById(R.id.temperature_data);
            data2 = itemView.findViewById(R.id.temperature_data2);
            iconRow = itemView.findViewById(R.id.rowIcon);
            iconMode = itemView.findViewById(R.id.mode_icon);
            pieView = itemView.findViewById(R.id.pieView);

            dayButton = itemView.findViewById(R.id.day_button);
            monthButton = itemView.findViewById(R.id.month_button);
            yearButton = itemView.findViewById(R.id.year_button);
            weekButton = itemView.findViewById(R.id.week_button);
            setButton = itemView.findViewById(R.id.set_button);
            likeButton = itemView.findViewById(R.id.fav_button);

            extraPanel = itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<TemperatureInfo> list = data;

            int count = list.size();
            final ArrayList<TemperatureInfo> temperatureInfos = new ArrayList<>(count);

            TemperatureInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    temperatureInfos.add(filterableObject);
                }
            }

            results.values = temperatureInfos;
            results.count = temperatureInfos.size();

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