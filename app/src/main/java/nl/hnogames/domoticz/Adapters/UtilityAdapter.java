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

import nl.hnogames.domoticz.Interfaces.UtilityClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;

@SuppressWarnings("unused")
public class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.DataObjectHolder> {

    private static final String TAG = UtilityAdapter.class.getSimpleName();

    private final UtilityClickListener listener;
    public ArrayList<UtilitiesInfo> filteredData = null;
    private Context context;
    private ArrayList<UtilitiesInfo> data = null;
    private Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();
    private SharedPrefUtil mSharedPrefs;

    public UtilityAdapter(Context context,
                          Domoticz mDomoticz,
                          ArrayList<UtilitiesInfo> data,
                          UtilityClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;

        setData(data);
        this.listener = listener;
    }

    public void setData(ArrayList<UtilitiesInfo> data) {
        Collections.sort(data, new Comparator<UtilitiesInfo>() {
            @Override
            public int compare(UtilitiesInfo left, UtilitiesInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
        this.data = data;
        this.filteredData = data;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.utilities_row_default, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            ((android.support.v7.widget.CardView) view.findViewById(R.id.card_global_wrapper)).setCardBackgroundColor(Color.parseColor("#3F3F3F"));
            if ((view.findViewById(R.id.row_wrapper)) != null)
                (view.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            if ((view.findViewById(R.id.row_global_wrapper)) != null)
                (view.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
            if ((view.findViewById(R.id.on_button)) != null)
                (view.findViewById(R.id.on_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            if ((view.findViewById(R.id.off_button)) != null)
                (view.findViewById(R.id.off_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            if ((view.findViewById(R.id.set_button)) != null)
                (view.findViewById(R.id.set_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }

        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final UtilitiesInfo mUtilitiesInfo = filteredData.get(position);
            final double setPoint = mUtilitiesInfo.getSetPoint();

            if ((mUtilitiesInfo.getType() != null && DomoticzValues.Device.Utility.Type.THERMOSTAT.equalsIgnoreCase(mUtilitiesInfo.getType())) ||
                    (mUtilitiesInfo.getSubType() != null && DomoticzValues.Device.Utility.SubType.SMARTWARES.equalsIgnoreCase(mUtilitiesInfo.getSubType()))) {
                setButtons(holder, Buttons.THERMOSTAT);
                CreateThermostatRow(holder, mUtilitiesInfo, setPoint);
            } else {
                if (DomoticzValues.Device.Utility.SubType.TEXT.equalsIgnoreCase(mUtilitiesInfo.getSubType()) || DomoticzValues.Device.Utility.SubType.ALERT.equalsIgnoreCase(mUtilitiesInfo.getSubType())) {
                    CreateTextRow(holder, mUtilitiesInfo);
                    setButtons(holder, Buttons.TEXT);
                } else {
                    CreateDefaultRow(holder, mUtilitiesInfo);
                    setButtons(holder, Buttons.DEFAULT);
                }
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemLongClicked(position);
                    return true;
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, position);
                }
            });
        }
    }

    private void CreateTextRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo) {
        holder.isProtected = mUtilitiesInfo.isProtected();

        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.setText(context.getString(R.string.data) + ": " + mUtilitiesInfo.getData());
        holder.hardware.setText(context.getString(R.string.hardware) + ": " + mUtilitiesInfo.getHardwareName());
        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0)
            holder.data.setText(context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage());

        String text = "";
        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0) {
            try {
                int usage = Integer.parseInt(mUtilitiesInfo.getUsage().replace("Watt", "").trim());
                if (mUtilitiesInfo.getUsageDeliv() != null && mUtilitiesInfo.getUsageDeliv().length() > 0) {
                    int usagedel = Integer.parseInt(mUtilitiesInfo.getUsageDeliv().replace("Watt", "").trim());
                    text = context.getString(R.string.usage) + ": " + (usage - usagedel) + " Watt";
                    holder.data.setText(text);
                } else {
                    text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                    holder.data.setText(text);
                }
            } catch (Exception ex) {
                text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                holder.data.setText(text);
            }
        }

        if (mUtilitiesInfo.getCounterToday() != null && mUtilitiesInfo.getCounterToday().length() > 0)
            holder.data.append(" " + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 && !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());
        if (mSharedPrefs.darkThemeEnabled()) {
            holder.buttonLog.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
        }

        if (holder.likeButton != null) {
            holder.likeButton.setId(mUtilitiesInfo.getIdx());
            holder.likeButton.setLiked(mUtilitiesInfo.getFavoriteBoolean());
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

        holder.buttonLog.setId(mUtilitiesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    private void CreateDefaultRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo) {
        holder.isProtected = mUtilitiesInfo.isProtected();

        if (mSharedPrefs.darkThemeEnabled()) {
            holder.dayButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.monthButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.yearButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.weekButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
        }

        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.setText(context.getString(R.string.data) + ": " + mUtilitiesInfo.getData());
        holder.hardware.setText(context.getString(R.string.hardware) + ": " + mUtilitiesInfo.getHardwareName());

        String text = "";
        if (mUtilitiesInfo.getUsage() != null && mUtilitiesInfo.getUsage().length() > 0) {
            try {
                int usage = Integer.parseInt(mUtilitiesInfo.getUsage().replace("Watt", "").trim());
                if (mUtilitiesInfo.getUsageDeliv() != null && mUtilitiesInfo.getUsageDeliv().length() > 0) {
                    int usagedel = Integer.parseInt(mUtilitiesInfo.getUsageDeliv().replace("Watt", "").trim());
                    text = context.getString(R.string.usage) + ": " + (usage - usagedel) + " Watt";
                    holder.data.setText(text);
                } else {
                    text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                    holder.data.setText(text);
                }
            } catch (Exception ex) {
                text = context.getString(R.string.usage) + ": " + mUtilitiesInfo.getUsage();
                holder.data.setText(text);
            }
        }
        if (mUtilitiesInfo.getCounterToday() != null && mUtilitiesInfo.getCounterToday().length() > 0)
            holder.data.append(" " + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 &&
                !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                }
            }
        });

        holder.weekButton.setVisibility(View.GONE);
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
                }
            }
        });

        holder.yearButton.setId(mUtilitiesInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                }
            }
        });

        if (holder.likeButton != null) {
            holder.likeButton.setId(mUtilitiesInfo.getIdx());
            holder.likeButton.setLiked(mUtilitiesInfo.getFavoriteBoolean());
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

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    private void CreateThermostatRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo, final double setPoint) {
        int layoutResourceId;
        holder.isProtected = mUtilitiesInfo.isProtected();
        if (holder.isProtected)
            holder.on_button.setEnabled(false);

        if (mSharedPrefs.darkThemeEnabled()) {
            holder.dayButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.monthButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.yearButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            holder.weekButton.setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
        }

        holder.on_button.setText(context.getString(R.string.set_temperature));
        holder.on_button.setId(mUtilitiesInfo.getIdx());
        holder.on_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleThermostatClick(v.getId());
            }
        });
        if (mSharedPrefs.darkThemeEnabled()) {
            holder.on_button.setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
        }

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                }
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                }
            }
        });

        holder.weekButton.setVisibility(View.GONE);
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
                }
            }
        });

        if (mUtilitiesInfo.getSubType()
                .replace("Electric", "counter")
                .replace("kWh", "counter")
                .replace("Gas", "counter")
                .replace("Energy", "counter")
                .replace("Voltcraft", "counter")
                .replace("SetPoint", "temp")
                .replace("YouLess counter", "counter").contains("counter"))
            holder.weekButton.setVisibility(View.VISIBLE);

        holder.yearButton.setId(mUtilitiesInfo.getIdx());
        holder.yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (UtilitiesInfo t : filteredData) {
                    if (t.getIdx() == v.getId())
                        listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                }
            }
        });
        holder.name.setText(mUtilitiesInfo.getName());
        holder.data.setText(mUtilitiesInfo.getLastUpdate());
        holder.hardware.setText(context.getString(R.string.set_point) + ": " + String.valueOf(setPoint));
        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(), mUtilitiesInfo.getType(), mUtilitiesInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    public void handleThermostatClick(int idx) {
        listener.onThermostatClick(idx);
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void setButtons(DataObjectHolder holder, int button) {

        if (holder.buttonLog != null) {
            holder.buttonLog.setVisibility(View.GONE);
        }
        if (holder.dayButton != null) {
            holder.dayButton.setVisibility(View.GONE);
        }
        if (holder.monthButton != null) {
            holder.monthButton.setVisibility(View.GONE);
        }
        if (holder.yearButton != null) {
            holder.yearButton.setVisibility(View.GONE);
        }
        if (holder.weekButton != null) {
            holder.weekButton.setVisibility(View.GONE);
        }
        if (holder.on_button != null) {
            holder.on_button.setVisibility(View.GONE);
        }

        switch (button) {
            case Buttons.DEFAULT:
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                break;
            case Buttons.TEXT:
                holder.buttonLog.setVisibility(View.VISIBLE);
                break;
            case Buttons.THERMOSTAT:
                holder.on_button.setVisibility(View.VISIBLE);
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
    }

    interface Buttons {
        int DEFAULT = 0;
        int TEXT = 1;
        int THERMOSTAT = 2;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        ImageView iconRow;
        Boolean isProtected;

        Button dayButton;
        Button monthButton;
        Button yearButton;
        Button weekButton;
        Button buttonLog;
        Button on_button;

        LikeButton likeButton;
        LinearLayout extraPanel;

        public DataObjectHolder(View itemView) {
            super(itemView);

            dayButton = (Button) itemView.findViewById(R.id.day_button);
            monthButton = (Button) itemView.findViewById(R.id.month_button);
            yearButton = (Button) itemView.findViewById(R.id.year_button);
            weekButton = (Button) itemView.findViewById(R.id.week_button);
            likeButton = (LikeButton) itemView.findViewById(R.id.fav_button);

            on_button = (Button) itemView.findViewById(R.id.on_button);
            name = (TextView) itemView.findViewById(R.id.utilities_name);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);
            buttonLog = (Button) itemView.findViewById(R.id.log_button);
            data = (TextView) itemView.findViewById(R.id.utilities_data);
            hardware = (TextView) itemView.findViewById(R.id.utilities_hardware);

            extraPanel = (LinearLayout) itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
        }
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<UtilitiesInfo> list = data;

            int count = list.size();
            final ArrayList<UtilitiesInfo> nlist = new ArrayList<UtilitiesInfo>(count);

            UtilitiesInfo filterableObject;

            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
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
            filteredData = (ArrayList<UtilitiesInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}