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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.material.chip.Chip;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import github.nisrulz.recyclerviewhelper.RVHViewHolder;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ads.NativeTemplateStyle;
import nl.hnogames.domoticz.ads.TemplateView;
import nl.hnogames.domoticz.helpers.ItemMoveAdapter;
import nl.hnogames.domoticz.interfaces.UtilityClickListener;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;

@SuppressWarnings("unused")
public class UtilityAdapter extends RecyclerView.Adapter<UtilityAdapter.DataObjectHolder> implements ItemMoveAdapter {

    private static final String TAG = UtilityAdapter.class.getSimpleName();
    public static List<String> mCustomSorting;
    private final UtilityClickListener listener;
    private final Context context;
    private final Domoticz domoticz;
    private final ItemFilter mFilter = new ItemFilter();
    private final SharedPrefUtil mSharedPrefs;
    public ArrayList<UtilitiesInfo> filteredData = null;
    private ArrayList<UtilitiesInfo> data = null;
    private boolean adLoaded = false;

    public UtilityAdapter(Context context,
                          Domoticz mDomoticz,
                          ArrayList<UtilitiesInfo> data,
                          UtilityClickListener listener) {
        super();

        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        domoticz = mDomoticz;

        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("utilities");
        setData(data);
        this.listener = listener;
    }

    public void onDestroy() {
        SaveSorting();
    }

    public void setData(ArrayList<UtilitiesInfo> data) {
        if (this.filteredData != null)
            SaveSorting();
        ArrayList<UtilitiesInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    private ArrayList<UtilitiesInfo> SortData(ArrayList<UtilitiesInfo> dat) {
        ArrayList<UtilitiesInfo> data = new ArrayList<>();
        data = dat;
        ArrayList<UtilitiesInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            UtilitiesInfo adView = null;
            for (String s : mCustomSorting) {
                for (UtilitiesInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())) && d.getIdx() != MainActivity.ADS_IDX)
                        customdata.add(d);
                    if (d.getIdx() == MainActivity.ADS_IDX)
                        adView = d;
                }
            }
            for (UtilitiesInfo d : data) {
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
        mSharedPrefs.saveSortingList("utilities", mCustomSorting);
    }

    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.utilities_row_default, parent, false);
        return new DataObjectHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() > 0) {
            final UtilitiesInfo mUtilitiesInfo = filteredData.get(position);
            final double setPoint = mUtilitiesInfo.getSetPoint();

            if (mUtilitiesInfo.getIdx() == MainActivity.ADS_IDX) {
                setButtons(holder, Buttons.ADS);
                setAdsLayout(holder);
            } else {
                if ((mUtilitiesInfo.getType() != null && DomoticzValues.Device.Utility.Type.THERMOSTAT.equalsIgnoreCase(mUtilitiesInfo.getType())) ||
                        (mUtilitiesInfo.getSubType() != null && DomoticzValues.Device.Utility.SubType.SETPOINT.equalsIgnoreCase(mUtilitiesInfo.getSubType())) ||
                        (mUtilitiesInfo.getSubType() != null && DomoticzValues.Device.Utility.SubType.SMARTWARES.equalsIgnoreCase(mUtilitiesInfo.getSubType()))) {
                    setButtons(holder, Buttons.THERMOSTAT);
                    CreateThermostatRow(holder, mUtilitiesInfo, setPoint);
                } else if ((mUtilitiesInfo.getType() != null && DomoticzValues.Device.Utility.Type.GENERAL.equalsIgnoreCase(mUtilitiesInfo.getType())) &&
                        (mUtilitiesInfo.getSubType() != null && DomoticzValues.Device.Utility.SubType.THERMOSTAT_MODE.equalsIgnoreCase(mUtilitiesInfo.getSubType()))) {
                    setButtons(holder, Buttons.THERMOSTAT_MODE);
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
            }

            holder.itemView.setOnClickListener(v -> listener.onItemClicked(v, position));

            holder.infoIcon.setTag(mUtilitiesInfo.getIdx());
            holder.infoIcon.setOnClickListener(v -> listener.onItemLongClicked((int) v.getTag()));
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

            List<String> testDevices = new ArrayList<>();
            testDevices.add(AdRequest.DEVICE_ID_EMULATOR);
            testDevices.add("0095CAF9DD12F33E5417335E1EC5FCAD");
            RequestConfiguration requestConfiguration
                    = new RequestConfiguration.Builder()
                    .setTestDeviceIds(testDevices)
                    .build();

            MobileAds.initialize(context);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();

            AdLoader adLoader = new AdLoader.Builder(context, context.getString(R.string.ad_unit_id))
                    .forNativeAd(unifiedNativeAd -> {
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
                        public void onAdFailedToLoad(LoadAdError errorCode) {
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

    private void CreateTextRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo) {
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
        if (mUtilitiesInfo.getCounterDelivToday() != null && mUtilitiesInfo.getCounterDelivToday().length() > 0) {
            holder.data.append("\r\n" + context.getString(R.string.delivery) + ": " + mUtilitiesInfo.getCounterDelivToday());
            if (mUtilitiesInfo.getCounterDeliv() != null && mUtilitiesInfo.getCounterDeliv().length() > 0 &&
                    !mUtilitiesInfo.getCounterDeliv().equals(mUtilitiesInfo.getData()))
                holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounterDeliv());
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
        holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(),
                mUtilitiesInfo.getType(),
                mUtilitiesInfo.getSubType(),
                false,
                mUtilitiesInfo.getUseCustomImage(),
                mUtilitiesInfo.getImage())).into(holder.iconRow);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    private void CreateDefaultRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo) {
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
            holder.data.append("\r\n" + context.getString(R.string.today) + ": " + mUtilitiesInfo.getCounterToday());
        if (mUtilitiesInfo.getCounter() != null && mUtilitiesInfo.getCounter().length() > 0 &&
                !mUtilitiesInfo.getCounter().equals(mUtilitiesInfo.getData()))
            holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounter());

        if (mUtilitiesInfo.getCounterDelivToday() != null && mUtilitiesInfo.getCounterDelivToday().length() > 0) {
            holder.data.append("\r\n" + context.getString(R.string.delivery) + ": " + mUtilitiesInfo.getCounterDelivToday());
            if (mUtilitiesInfo.getCounterDeliv() != null && mUtilitiesInfo.getCounterDeliv().length() > 0 &&
                    !mUtilitiesInfo.getCounterDeliv().equals(mUtilitiesInfo.getData()))
                holder.data.append(" " + context.getString(R.string.total) + ": " + mUtilitiesInfo.getCounterDeliv());
        }

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
            }
        });

        holder.weekButton.setVisibility(View.GONE);
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
            }
        });

        holder.yearButton.setId(mUtilitiesInfo.getIdx());
        holder.yearButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
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

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(),
                mUtilitiesInfo.getType(),
                mUtilitiesInfo.getSubType(),
                false,
                mUtilitiesInfo.getUseCustomImage(),
                mUtilitiesInfo.getImage())).into(holder.iconRow);
    }

    private void CreateThermostatRow(DataObjectHolder holder, UtilitiesInfo mUtilitiesInfo, final double setPoint) {
        int layoutResourceId;
        holder.on_button.setText(context.getString(R.string.set_temperature));
        holder.on_button.setId(mUtilitiesInfo.getIdx());
        holder.on_button.setOnClickListener(v -> handleThermostatClick(v.getId()));

        holder.dayButton.setId(mUtilitiesInfo.getIdx());
        holder.dayButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.DAY);
            }
        });
        holder.monthButton.setId(mUtilitiesInfo.getIdx());
        holder.monthButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.MONTH);
            }
        });

        holder.weekButton.setVisibility(View.GONE);
        holder.weekButton.setId(mUtilitiesInfo.getIdx());
        holder.weekButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.WEEK);
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
        holder.yearButton.setOnClickListener(v -> {
            for (UtilitiesInfo t : filteredData) {
                if (t.getIdx() == v.getId())
                    listener.onLogClick(t, DomoticzValues.Graph.Range.YEAR);
            }
        });

        holder.name.setText(mUtilitiesInfo.getName());
        holder.hardware.setText(mUtilitiesInfo.getLastUpdate());

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mUtilitiesInfo.getTypeImg(),
                mUtilitiesInfo.getType(),
                mUtilitiesInfo.getSubType(),
                false,
                mUtilitiesInfo.getUseCustomImage(),
                mUtilitiesInfo.getImage())).into(holder.iconRow);

        int loadMode = mUtilitiesInfo.getModeId();
        final ArrayList<String> modes = mUtilitiesInfo.getModes();
        if (modes != null && modes.size() > 0) {
            holder.spSelector.setId(mUtilitiesInfo.getIdx());
            holder.spSelector.setVisibility(View.VISIBLE);
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, modes);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spSelector.setAdapter(dataAdapter);
            holder.spSelector.setSelection(loadMode);
            holder.spSelector.setTag(mUtilitiesInfo);

            holder.on_button.setVisibility(View.GONE);
            holder.data.setText(context.getString(R.string.set_mode) + ": " + modes.get(loadMode));

            holder.spSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if ((holder.spSelector.getId()) == mUtilitiesInfo.getIdx()) {
                        holder.spSelector.setId(mUtilitiesInfo.getIdx() * 3);
                    } else {
                        String selValue = holder.spSelector.getItemAtPosition(arg2).toString();
                        handleSelectorChange(mUtilitiesInfo, arg2, selValue);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } else {
            holder.spSelector.setVisibility(View.GONE);
            holder.data.setText(context.getString(R.string.set_point) + ": " + setPoint);
        }
    }

    private void handleSelectorChange(UtilitiesInfo device, int id, String mode) {
        if (device != null) {
            listener.OnModeChanged(device, id, mode);
        }
    }

    public void handleThermostatClick(int idx) {
        listener.onThermostatClick(idx);
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
        if (holder.spSelector != null) {
            holder.spSelector.setVisibility(View.GONE);
        }
        switch (button) {
            case Buttons.ADS:
                if (holder.adview != null)
                    holder.adview.setVisibility(View.VISIBLE);
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.GONE);
                break;
            case Buttons.DEFAULT:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.TEXT:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.buttonLog.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.THERMOSTAT:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.on_button.setVisibility(View.VISIBLE);
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.THERMOSTAT_MODE:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.dayButton.setVisibility(View.VISIBLE);
                holder.monthButton.setVisibility(View.VISIBLE);
                holder.weekButton.setVisibility(View.VISIBLE);
                holder.yearButton.setVisibility(View.VISIBLE);
                holder.spSelector.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
        }
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
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
        for (UtilitiesInfo d : filteredData) {
            if (d.getIdx() != -9998)
                ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
    }

    interface Buttons {
        int DEFAULT = 0;
        int TEXT = 1;
        int THERMOSTAT = 2;
        int ADS = 3;
        int THERMOSTAT_MODE = 4;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder implements RVHViewHolder {
        TextView name;
        TextView data;
        TextView hardware;
        ImageView iconRow;
        Chip dayButton;
        Chip monthButton;
        Chip yearButton;
        Chip weekButton;
        Chip buttonLog;
        Button on_button;
        ImageView infoIcon;
        LikeButton likeButton;
        LinearLayout extraPanel;
        TemplateView adview;
        RelativeLayout contentWrapper;
        Spinner spSelector;

        public DataObjectHolder(View itemView) {
            super(itemView);

            contentWrapper = itemView.findViewById(R.id.contentWrapper);
            adview = itemView.findViewById(R.id.adview);
            dayButton = itemView.findViewById(R.id.day_button);
            monthButton = itemView.findViewById(R.id.month_button);
            yearButton = itemView.findViewById(R.id.year_button);
            weekButton = itemView.findViewById(R.id.week_button);
            likeButton = itemView.findViewById(R.id.fav_button);

            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            on_button = itemView.findViewById(R.id.on_button);
            name = itemView.findViewById(R.id.utilities_name);
            iconRow = itemView.findViewById(R.id.rowIcon);
            buttonLog = itemView.findViewById(R.id.log_button);
            data = itemView.findViewById(R.id.utilities_data);
            hardware = itemView.findViewById(R.id.utilities_hardware);
            spSelector = itemView.findViewById(R.id.spSelector);

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

            final ArrayList<UtilitiesInfo> list = data;

            int count = list.size();
            final ArrayList<UtilitiesInfo> devicesInfos = new ArrayList<>(count);

            UtilitiesInfo filterableObject;
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
            filteredData = (ArrayList<UtilitiesInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}