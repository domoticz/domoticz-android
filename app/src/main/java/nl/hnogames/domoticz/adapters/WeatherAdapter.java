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
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import github.nisrulz.recyclerviewhelper.RVHAdapter;
import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ads.NativeTemplateStyle;
import nl.hnogames.domoticz.ads.TemplateView;
import nl.hnogames.domoticz.helpers.ItemMoveAdapter;
import nl.hnogames.domoticz.interfaces.WeatherClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Containers.WeatherInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

@SuppressWarnings("unused")
public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.DataObjectHolder> implements ItemMoveAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = WeatherAdapter.class.getSimpleName();
    public static List<String> mCustomSorting;
    private final WeatherClickListener listener;
    private final Context context;
    private final Domoticz domoticz;
    private final ItemFilter mFilter = new ItemFilter();
    private final ConfigInfo mConfigInfo;
    private final SharedPrefUtil mSharedPrefs;
    public ArrayList<WeatherInfo> filteredData = null;
    private ArrayList<WeatherInfo> data = null;
    private boolean adLoaded = false;

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
        if (this.filteredData != null)
            SaveSorting();
        ArrayList<WeatherInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    private ArrayList<WeatherInfo> SortData(ArrayList<WeatherInfo> dat) {
        ArrayList<WeatherInfo> data = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            data = dat;
        } else {
            for (WeatherInfo d : dat) {
                if (d.getIdx() != MainActivity.ADS_IDX)
                    data.add(d);
            }
        }
        ArrayList<WeatherInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            WeatherInfo adView = null;
            for (String s : mCustomSorting) {
                for (WeatherInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())) && d.getIdx() != MainActivity.ADS_IDX)
                        customdata.add(d);
                    if (d.getIdx() == MainActivity.ADS_IDX)
                        adView = d;
                }
            }
            for (WeatherInfo d : data) {
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
        mSharedPrefs.saveSortingList("weather", mCustomSorting);
    }

    public void onDestroy() {
        SaveSorting();
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (Build.VERSION.SDK_INT >= 21) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.weather_row_default, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.weather_row_default_noads, parent, false);
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
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final WeatherInfo mWeatherInfo = filteredData.get(position);

            if (holder.contentWrapper != null)
                holder.contentWrapper.setVisibility(View.VISIBLE);
            if (holder.adview != null)
                holder.adview.setVisibility(View.GONE);
            holder.itemView.setVisibility(View.VISIBLE);

            if (mWeatherInfo.getIdx() == MainActivity.ADS_IDX) {
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.GONE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.VISIBLE);
                setAdsLayout(holder);
            } else {
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
                holder.infoIcon.setOnClickListener(v -> listener.onItemLongClicked((int) v.getTag()));

                TypedValue pieBackgroundValue = new TypedValue();
                TypedValue temperatureValue = new TypedValue();
                Resources.Theme theme = context.getTheme();
                theme.resolveAttribute(R.attr.listviewRowBackground, pieBackgroundValue, true);
                theme.resolveAttribute(R.attr.temperatureTextColor, temperatureValue, true);
                holder.pieView.setInnerBackgroundColor(pieBackgroundValue.data);
                holder.pieView.setTextColor(temperatureValue.data);

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
                holder.dayButton.setOnClickListener(v -> {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
                    }
                });

                holder.monthButton.setId(mWeatherInfo.getIdx());
                holder.monthButton.setOnClickListener(v -> {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
                    }
                });

                holder.yearButton.setId(mWeatherInfo.getIdx());
                holder.yearButton.setOnClickListener(v -> {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
                    }
                });

                holder.weekButton.setVisibility(View.GONE);
                holder.weekButton.setId(mWeatherInfo.getIdx());
                holder.weekButton.setOnClickListener(v -> {
                    for (WeatherInfo t : filteredData) {
                        if (t.getIdx() == v.getId())
                            listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
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

                holder.itemView.setOnClickListener(v -> listener.onItemClicked(v, position));

                Picasso.get().load(DomoticzIcons.getDrawableIcon(mWeatherInfo.getTypeImg(), mWeatherInfo.getType(), null, false, false, null)).into(holder.iconRow);
            }
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
        for (WeatherInfo d : filteredData) {
            if (d.getIdx() != -9998)
                ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
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
        TemplateView adview;
        RelativeLayout contentWrapper;

        public DataObjectHolder(View itemView) {
            super(itemView);
            contentWrapper = itemView.findViewById(R.id.contentWrapper);
            adview = itemView.findViewById(R.id.adview);
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
            final ArrayList<WeatherInfo> devicesInfos = new ArrayList<>(count);

            WeatherInfo filterableObject;
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
            filteredData = (ArrayList<WeatherInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}