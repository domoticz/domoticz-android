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
import android.support.design.chip.Chip;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.Interfaces.WeatherClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Containers.WeatherInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

@SuppressWarnings("unused")
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.DataObjectHolder> implements RVHAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = WeatherAdapter.class.getSimpleName();
    public static List<String> mCustomSorting;
    private final WeatherClickListener listener;
    public ArrayList<WeatherInfo> filteredData = null;
    private Context context;
    private ArrayList<WeatherInfo> data = null;
    private Domoticz domoticz;
    private ItemFilter mFilter = new ItemFilter();
    private ConfigInfo mConfigInfo;
    private SharedPrefUtil mSharedPrefs;

    public WeatherAdapter(Context context,
                          Domoticz mDomoticz,
                          ServerUtil serverUtil,
                          ArrayList<WeatherInfo> data,
                          WeatherClickListener listener) {
        super();
        this.context = context;
        domoticz = mDomoticz;
        mSharedPrefs = new SharedPrefUtil(context);
        mConfigInfo = serverUtil.getActiveServer().getConfigInfo(context);
        this.listener = listener;
        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("weather");
        setData(data);
    }

    public void setData(ArrayList<WeatherInfo> data) {
        ArrayList<WeatherInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    private ArrayList<WeatherInfo> SortData(ArrayList<WeatherInfo> data) {
        ArrayList<WeatherInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            for (String s : mCustomSorting) {
                for (WeatherInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())))
                        customdata.add(d);
                }
            }
            for (WeatherInfo d : data) {
                if (!customdata.contains(d))
                    customdata.add(d);
            }
        } else
            customdata = data;
        return customdata;
    }

    private void SaveSorting() {
        List<String> ids = new ArrayList<>();
        for (WeatherInfo d : filteredData) {
            ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
        mSharedPrefs.saveSortingList("weather", ids);
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.weather_row_default, parent, false);

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
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }


    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final WeatherInfo mWeatherInfo = filteredData.get(position);

            JSONObject language = null;
            Language languageObj = new SharedPrefUtil(context).getSavedLanguage();
            if (languageObj != null) language = languageObj.getJsonObject();

            String tempSign = "";
            String windSign = "";
            if (mConfigInfo != null) {
                tempSign = mConfigInfo.getTempSign();
                windSign = mConfigInfo.getWindSign();
            }

            holder.infoIcon.setTag(mWeatherInfo.getIdx());
            holder.infoIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemLongClicked((int) v.getTag());
                }
            });

            holder.isProtected = mWeatherInfo.isProtected();
            holder.name.setText(mWeatherInfo.getName());
            holder.data.setText("");
            holder.hardware.setText("");
            if (language != null) {
                String hardware = language.optString(mWeatherInfo.getHardwareName(), mWeatherInfo.getHardwareName());
                holder.hardware.setText(hardware);
            } else holder.hardware.setText(mWeatherInfo.getHardwareName());

            holder.data.setEllipsize(TextUtils.TruncateAt.END);
            holder.data.setMaxLines(3);
            if (mWeatherInfo.getType().equals("Wind")) {
                holder.data.append(context.getString(R.string.direction) + " " + mWeatherInfo.getDirection() + " " + mWeatherInfo.getDirectionStr());
            } else {
                holder.data.append(mWeatherInfo.getData());
            }
            String text;

            if (!UsefulBits.isEmpty(mWeatherInfo.getRain())) {
                text = context.getString(R.string.rain) + ": " + mWeatherInfo.getRain();
                holder.data.setText(text);
            }
            if (!UsefulBits.isEmpty(mWeatherInfo.getRainRate()))
                holder.data.append(", " + context.getString(R.string.rainrate) + ": " + mWeatherInfo.getRainRate());
            if (!UsefulBits.isEmpty(mWeatherInfo.getForecastStr()))
                holder.data.append(", " + mWeatherInfo.getForecastStr());
            if (!UsefulBits.isEmpty(mWeatherInfo.getSpeed()))
                holder.data.append(", " + context.getString(R.string.speed) + ": " + mWeatherInfo.getSpeed() + " " + windSign);
            if (mWeatherInfo.getDewPoint() > 0)
                holder.data.append(", " + context.getString(R.string.dewPoint) + ": " + mWeatherInfo.getDewPoint() + " " + tempSign);
            if (mWeatherInfo.getTemp() > 0) {
                holder.data.append(", " + context.getString(R.string.temp) + ": " + mWeatherInfo.getTemp() + " " + tempSign);

                holder.pieView.setVisibility(View.VISIBLE);

                if (!this.mSharedPrefs.darkThemeEnabled()) {
                    holder.pieView.setInnerBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    holder.pieView.setTextColor(ContextCompat.getColor(context, R.color.black));
                }
                holder.pieView.setPercentageTextSize(16);
                holder.pieView.setPercentageBackgroundColor(ContextCompat.getColor(context, R.color.material_orange_600));

                double temp = mWeatherInfo.getTemp();
                if (!tempSign.equals("C"))
                    temp = temp / 2;
                holder.pieView.setPercentage(Float.valueOf(temp + ""));
                holder.pieView.setInnerText(mWeatherInfo.getTemp() + " " + tempSign);

                PieAngleAnimation animation = new PieAngleAnimation(holder.pieView);
                animation.setDuration(2000);
                holder.pieView.startAnimation(animation);
            } else {
                holder.pieView.setVisibility(View.GONE);
            }
            if (mWeatherInfo.getBarometer() > 0)
                holder.data.append(", " + context.getString(R.string.pressure) + ": " + mWeatherInfo.getBarometer());
            if (!UsefulBits.isEmpty(mWeatherInfo.getChill()))
                holder.data.append(", " + context.getString(R.string.chill) + ": " + mWeatherInfo.getChill() + " " + tempSign);
            if (!UsefulBits.isEmpty(mWeatherInfo.getHumidityStatus()))
                holder.data.append(", " + context.getString(R.string.humidity) + ": " + mWeatherInfo.getHumidityStatus());

            holder.dayButton.setId(mWeatherInfo.getIdx());
            holder.dayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                    }
                }
            });

            holder.monthButton.setId(mWeatherInfo.getIdx());
            holder.monthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                    }
                }
            });

            holder.yearButton.setId(mWeatherInfo.getIdx());
            holder.yearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                    }
                }
            });

            holder.weekButton.setVisibility(View.GONE);
            holder.weekButton.setId(mWeatherInfo.getIdx());
            holder.weekButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
                    }
                }
            });

            if (holder.likeButton != null) {
                holder.likeButton.setId(mWeatherInfo.getIdx());
                holder.likeButton.setLiked(mWeatherInfo.getFavoriteBoolean());
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

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, position);
                }
            });

            Picasso.get().load(DomoticzIcons.getDrawableIcon(mWeatherInfo.getTypeImg(), mWeatherInfo.getType(), null, false, false, null)).into(holder.iconRow);
        }
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
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

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        ImageView iconRow;
        Boolean isProtected;
        Chip dayButton;
        Chip monthButton;
        Chip yearButton;
        Chip weekButton;
        LikeButton likeButton;
        LinearLayout extraPanel;
        PieView pieView;
        ImageView infoIcon;

        public DataObjectHolder(View itemView) {
            super(itemView);
            pieView = itemView.findViewById(R.id.pieView);
            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            pieView.setVisibility(View.GONE);
            dayButton = itemView.findViewById(R.id.day_button);
            monthButton = itemView.findViewById(R.id.month_button);
            yearButton = itemView.findViewById(R.id.year_button);
            weekButton = itemView.findViewById(R.id.week_button);
            likeButton = itemView.findViewById(R.id.fav_button);
            name = itemView.findViewById(R.id.weather_name);
            iconRow = itemView.findViewById(R.id.rowIcon);
            data = itemView.findViewById(R.id.weather_data);
            hardware = itemView.findViewById(R.id.weather_hardware);
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

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final ArrayList<WeatherInfo> list = data;

            int count = list.size();
            final ArrayList<WeatherInfo> weatherInfos = new ArrayList<>(count);

            WeatherInfo filterableObject;
            for (int i = 0; i < count; i++) {
                filterableObject = list.get(i);
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
                    weatherInfos.add(filterableObject);
                }
            }

            results.values = weatherInfos;
            results.count = weatherInfos.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<WeatherInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}