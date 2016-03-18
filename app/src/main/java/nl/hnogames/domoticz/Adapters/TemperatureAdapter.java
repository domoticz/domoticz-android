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
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.TemperatureInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.TemperatureClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;


public class TemperatureAdapter extends BaseAdapter implements Filterable {

    @SuppressWarnings("unused")
    private static final String TAG = TemperatureAdapter.class.getSimpleName();
    private final TemperatureClickListener listener;
    public ArrayList<TemperatureInfo> filteredData = null;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private Context context;
    private ArrayList<TemperatureInfo> data = null;
    private ItemFilter mFilter = new ItemFilter();

    public TemperatureAdapter(Context context,
                              Domoticz mDomoticz,
                              ArrayList<TemperatureInfo> data,
                              TemperatureClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;
        Collections.sort(data, new Comparator<TemperatureInfo>() {
            @Override
            public int compare(TemperatureInfo left, TemperatureInfo right) {
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
        final ViewHolder holder;
        int layoutResourceId;

        TemperatureInfo mTemperatureInfo = filteredData.get(position);

        holder = new ViewHolder();
        layoutResourceId = R.layout.temperature_row_default;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        convertView = inflater.inflate(layoutResourceId, parent, false);
        holder.dayButton = (Button) convertView.findViewById(R.id.day_button);
        holder.monthButton = (Button) convertView.findViewById(R.id.month_button);
        holder.yearButton = (Button) convertView.findViewById(R.id.year_button);
        holder.weekButton = (Button) convertView.findViewById(R.id.week_button);
        holder.setButton = (Button) convertView.findViewById(R.id.set_button);
        holder.likeButton = (LikeButton) convertView.findViewById(R.id.fav_button);

        if (mSharedPrefs.darkThemeEnabled()) {
            (convertView.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            (convertView.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
            holder.dayButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.monthButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.yearButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.weekButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));

            if (holder.setButton != null)
                holder.setButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }

        holder.isProtected = mTemperatureInfo.isProtected();
        holder.name = (TextView) convertView.findViewById(R.id.temperature_name);
        holder.data = (TextView) convertView.findViewById(R.id.temperature_data);
        holder.data2 = (TextView) convertView.findViewById(R.id.temperature_data2);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.iconMode = (ImageView) convertView.findViewById(R.id.mode_icon);

        int modeIconRes = 0;
        boolean tooHot = false;
        if (mTemperatureInfo.getTemperature() > 30)
            tooHot = true;

        Picasso.with(context).load(domoticz.getDrawableIcon(mTemperatureInfo.getTypeImg(),
                mTemperatureInfo.getType(),
                null, tooHot, false, null)).into(holder.iconRow);

        if ("evohome".equals(mTemperatureInfo.getHardwareName())) {
            holder.setButton.setVisibility(View.VISIBLE);
            modeIconRes = getEvohomeStateIcon(mTemperatureInfo.getStatus());
        } else {
            holder.setButton.setVisibility(View.GONE);
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
                        listener.onLogClick(t, Domoticz.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mTemperatureInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TemperatureInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.MONTH);
                }
            }
        });
        holder.weekButton.setId(mTemperatureInfo.getIdx());
        holder.weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TemperatureInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.WEEK);
                }
            }
        });
        holder.yearButton.setId(mTemperatureInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TemperatureInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, Domoticz.Graph.Range.YEAR);
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

        holder.name.setText(mTemperatureInfo.getName());
        if (mTemperatureInfo.getType().equalsIgnoreCase(Domoticz.Device.Type.Name.WIND)) {
            holder.data.setText(R.string.wind);
            holder.data.append(": " + mTemperatureInfo.getData() + " " + mTemperatureInfo.getDirection());
            holder.data2.setVisibility(View.GONE);
        } else {
            double temperature = mTemperatureInfo.getTemperature();
            double setPoint = mTemperatureInfo.getSetPoint();
            if (temperature <= 0 || setPoint <= 0) {
                holder.data.setText(context.getString(R.string.temperature) + ": " + mTemperatureInfo.getData());
                holder.data2.setVisibility(View.GONE);
            } else {
                holder.data.setText(context.getString(R.string.temperature) + ": " + mTemperatureInfo.getTemperature() + " C");
                holder.data2.setText(context.getString(R.string.set_point) + ": " + mTemperatureInfo.getSetPoint() + " C");
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

        convertView.setTag(holder);
        return convertView;
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

    static class ViewHolder {
        TextView name;
        TextView data;
        TextView data2;
        ImageView iconRow;
        ImageView iconMode;
        Button setButton;
        Button dayButton;
        Button monthButton;
        Button weekButton;
        Button yearButton;
        Boolean isProtected;

        LikeButton likeButton;
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
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