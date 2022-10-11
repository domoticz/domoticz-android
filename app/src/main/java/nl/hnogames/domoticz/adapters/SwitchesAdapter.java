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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
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

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ads.NativeTemplateStyle;
import nl.hnogames.domoticz.ads.TemplateView;
import nl.hnogames.domoticz.helpers.ItemMoveAdapter;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.switchesClickListener;
import nl.hnogames.domoticz.utils.CameraUtil;
import nl.hnogames.domoticz.utils.PicassoUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

@SuppressWarnings("unused")
public class SwitchesAdapter extends RecyclerView.Adapter<SwitchesAdapter.DataObjectHolder> implements ItemMoveAdapter {
    public static final int ID_SCENE_SWITCH = 2000;
    public static List<String> mCustomSorting;
    private final int ID_TEXTVIEW = 1000;
    private final int ID_SWITCH = 0;
    private final int[] EVOHOME_STATE_IDS = {
            DomoticzValues.Device.ModalSwitch.Action.AUTO,
            DomoticzValues.Device.ModalSwitch.Action.ECONOMY,
            DomoticzValues.Device.ModalSwitch.Action.AWAY,
            DomoticzValues.Device.ModalSwitch.Action.AWAY,
            DomoticzValues.Device.ModalSwitch.Action.CUSTOM,
            DomoticzValues.Device.ModalSwitch.Action.HEATING_OFF
    };
    private final Context context;
    private final switchesClickListener listener;
    private final SharedPrefUtil mSharedPrefs;
    private final ConfigInfo mConfigInfo;
    private final int lastPosition = -1;
    private final Picasso picasso;
    private final ItemFilter mFilter = new ItemFilter();
    private final Domoticz domoticz;
    @ColorInt
    private final int listviewRowBackground;
    public ArrayList<DevicesInfo> data = null;
    public ArrayList<DevicesInfo> filteredData = null;
    private int layoutResourceId;
    private int previousDimmerValue;
    private boolean adLoaded = false;

    public SwitchesAdapter(Context context,
                           ServerUtil serverUtil,
                           ArrayList<DevicesInfo> data,
                           switchesClickListener listener) {
        super();
        this.domoticz = StaticHelper.getDomoticz(context);

        mSharedPrefs = new SharedPrefUtil(context);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.listviewRowBackground, typedValue, true);
        listviewRowBackground = typedValue.data;

        picasso = new PicassoUtil().getPicasso(context,
                domoticz.getSessionUtil().getSessionCookie(),
                domoticz.getUserCredentials(Domoticz.Authentication.USERNAME),
                domoticz.getUserCredentials(Domoticz.Authentication.PASSWORD));

        this.context = context;
        mConfigInfo = serverUtil.getActiveServer().getConfigInfo(context);
        this.listener = listener;

        if (mCustomSorting == null)
            mCustomSorting = mSharedPrefs.getSortingList("switches");
        setData(data);
    }

    public void setData(ArrayList<DevicesInfo> data) {
        if (this.filteredData != null)
            SaveSorting();
        ArrayList<DevicesInfo> sortedData = SortData(data);
        this.data = sortedData;
        this.filteredData = sortedData;
    }

    private ArrayList<DevicesInfo> SortData(ArrayList<DevicesInfo> dat) {
        ArrayList<DevicesInfo> data = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            data = dat;
        } else {
            for (DevicesInfo d : dat) {
                if (d.getIdx() != MainActivity.ADS_IDX)
                    data.add(d);
            }
        }

        ArrayList<DevicesInfo> customdata = new ArrayList<>();
        if (mSharedPrefs.enableCustomSorting() && mCustomSorting != null) {
            DevicesInfo adView = null;
            for (String s : mCustomSorting) {
                for (DevicesInfo d : data) {
                    if (s.equals(String.valueOf(d.getIdx())) && d.getIdx() != MainActivity.ADS_IDX)
                        customdata.add(d);
                    if (d.getIdx() == MainActivity.ADS_IDX)
                        adView = d;
                }
            }
            for (DevicesInfo d : data) {
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
        mSharedPrefs.saveSortingList("switches", mCustomSorting);
    }

    public void onDestroy() {
        SaveSorting();
    }

    private void handleLikeButtonClick(int idx, boolean checked) {
        listener.onLikeButtonClick(idx, checked);
    }

    /**
     * Get's the filter
     *
     * @return Returns the filter
     */
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row = null;
        // Check if we're running on Android 5.0 or higher
        if (Build.VERSION.SDK_INT >= 21) {
            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.switch_row_list, parent, false);
        } else {
            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.switch_row_list_noads, parent, false);
        }
        return new DataObjectHolder(row);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() >= position) {
            DevicesInfo extendedStatusInfo = filteredData.get(position);
            setSwitchRowData(extendedStatusInfo, holder);

            if (extendedStatusInfo.getType() == null || !extendedStatusInfo.getType().equals("advertisement")) {
                holder.buttonLog.setId(extendedStatusInfo.getIdx());
                holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
                holder.infoIcon.setTag(extendedStatusInfo.getIdx());
                holder.infoIcon.setOnClickListener(v -> listener.onItemLongClicked((int) v.getTag()));
                if (holder.likeButton != null) {
                    holder.likeButton.setId(extendedStatusInfo.getIdx());
                    holder.likeButton.setLiked(extendedStatusInfo.getFavoriteBoolean());
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

                holder.buttonTimer.setId(extendedStatusInfo.getIdx());
                holder.buttonTimer.setOnClickListener(v -> handleTimerButtonClick(v.getId()));
                if (!UsefulBits.isEmpty(extendedStatusInfo.getTimers())) {
                    if (extendedStatusInfo.getTimers().equalsIgnoreCase("false"))
                        holder.buttonTimer.setVisibility(View.GONE);
                }

                holder.buttonNotifications.setId(extendedStatusInfo.getIdx());
                holder.buttonNotifications.setOnClickListener(v -> handleNotificationButtonClick(v.getId()));

                if (!extendedStatusInfo.hasNotifications())
                    holder.buttonNotifications.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v -> listener.onItemClicked(v, position));
            }
        }
    }

    /**
     * Set the data for switches
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setSwitchRowData(DevicesInfo mDeviceInfo,
                                  DataObjectHolder holder) {
        holder.itemView.setVisibility(View.VISIBLE);
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            if (mDeviceInfo.getIdx() == MainActivity.ADS_IDX) {
                setButtons(holder, Buttons.ADS);
                setAdsLayout(holder);
            } else {
                switch (mDeviceInfo.getType()) {
                    case DomoticzValues.Scene.Type.GROUP:
                        setButtons(holder, Buttons.BUTTONS);
                        setOnOffButtonRowData(mDeviceInfo, holder);
                        break;
                    case DomoticzValues.Scene.Type.SCENE:
                        setButtons(holder, Buttons.BUTTON_ON);
                        setPushOnOffSwitchRowData(mDeviceInfo, holder, true);
                        break;
                    case DomoticzValues.Device.Utility.Type.THERMOSTAT:
                        setButtons(holder, Buttons.BUTTON_ON);
                        setThermostatRowData(mDeviceInfo, holder);
                        break;
                    case DomoticzValues.Device.Utility.Type.HEATING:
                        setButtons(holder, Buttons.SET);
                        setTemperatureRowData(mDeviceInfo, holder);
                        break;
                    default:
                        setButtons(holder, Buttons.NOTHING);
                        setDefaultRowData(mDeviceInfo, holder);
                        break;
                }
            }
        } else if ((mDeviceInfo.getSwitchType() == null)) {
            setButtons(holder, Buttons.NOTHING);
            setDefaultRowData(mDeviceInfo, holder);
        } else {
            switch (mDeviceInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                case DomoticzValues.Device.Type.Value.DOORLOCK:
                case DomoticzValues.Device.Type.Value.DOORLOCKINVERTED:
                    switch (mDeviceInfo.getSwitchType()) {
                        case DomoticzValues.Device.Type.Name.SECURITY:
                            if (mDeviceInfo.getSubType().equals(DomoticzValues.Device.SubType.Name.SECURITYPANEL)) {
                                setButtons(holder, Buttons.BUTTON_ON);
                                setSecurityPanelSwitchRowData(mDeviceInfo, holder);
                            } else {
                                setButtons(holder, Buttons.NOTHING);
                                setDefaultRowData(mDeviceInfo, holder);
                            }
                            break;
                        case DomoticzValues.Device.Type.Name.EVOHOME:
                            if (mDeviceInfo.getSubType().equals(DomoticzValues.Device.SubType.Name.EVOHOME)) {
                                setButtons(holder, Buttons.MODAL);
                                setModalSwitchRowData(mDeviceInfo, holder, R.array.evohome_states, R.array.evohome_state_names, EVOHOME_STATE_IDS);
                            } else {
                                setButtons(holder, Buttons.NOTHING);
                                setDefaultRowData(mDeviceInfo, holder);
                            }
                            break;
                        default:
                            setButtons(holder, Buttons.SWITCH);
                            setOnOffSwitchRowData(mDeviceInfo, holder);
                            break;
                    }
                    break;

                case DomoticzValues.Device.Type.Value.X10SIREN:
                case DomoticzValues.Device.Type.Value.MOTION:
                case DomoticzValues.Device.Type.Value.CONTACT:
                case DomoticzValues.Device.Type.Value.DUSKSENSOR:
                case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                case DomoticzValues.Device.Type.Value.DOORBELL:
                    setButtons(holder, Buttons.BUTTON_ON);
                    setContactSwitchRowData(mDeviceInfo, holder, false);
                    break;
                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                    setButtons(holder, Buttons.BUTTON_ON);
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, true);
                    break;

                case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    setButtons(holder, Buttons.NOTHING);
                    setDefaultRowData(mDeviceInfo, holder);
                    break;

                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    setButtons(holder, Buttons.BUTTON_ON);
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, false);
                    break;

                case DomoticzValues.Device.Type.Value.DIMMER:
                    if (mDeviceInfo.getSubType().startsWith(DomoticzValues.Device.SubType.Name.RGB) ||
                            mDeviceInfo.getSubType().startsWith(DomoticzValues.Device.SubType.Name.WW)) {
                        setButtons(holder, Buttons.DIMMER_RGB);
                        setDimmerRowData(mDeviceInfo, holder, true);
                    } else {
                        setButtons(holder, Buttons.DIMMER);
                        setDimmerRowData(mDeviceInfo, holder, false);
                    }
                    break;

                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTEDSTOP:
                    if (DomoticzValues.canHandleStopButton(mDeviceInfo) ||
                            (mDeviceInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTEDSTOP || mDeviceInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP)) {
                        setButtons(holder, Buttons.BLINDS_DIMMER);
                        setBlindsRowData(mDeviceInfo, holder);
                    } else {
                        setButtons(holder, Buttons.BLINDS_DIMMER_NOSTOP);
                        setBlindsRowData(mDeviceInfo, holder);
                    }
                    break;

                case DomoticzValues.Device.Type.Value.SELECTOR:
                    setButtons(holder, Buttons.SELECTOR);
                    setSelectorRowData(mDeviceInfo, holder);
                    break;

                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                    if (DomoticzValues.canHandleStopButton(mDeviceInfo)) {
                        setButtons(holder, Buttons.BLINDS);
                    } else {
                        setButtons(holder, Buttons.BLINDS_NOSTOP);
                    }
                    setBlindsRowData(mDeviceInfo, holder);
                    break;

                case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                    setButtons(holder, Buttons.BLINDS);
                    setBlindsRowData(mDeviceInfo, holder);
                    break;

                default:
                    throw new NullPointerException(
                            "No supported switch type defined in the adapter (setSwitchRowData)");
            }
        }
        SetCameraBackGround(mDeviceInfo, holder);
    }

    private void SetCameraBackGround(DevicesInfo mDeviceInfo, final SwitchesAdapter.DataObjectHolder holder) {
        if (mSharedPrefs.addCameraToDashboard() && mDeviceInfo.getUsedByCamera() && mDeviceInfo.getCameraIdx() >= 0) {
            holder.full_screen_icon.setVisibility(View.VISIBLE);
            holder.full_screen_icon.setTag(mDeviceInfo.getCameraIdx());
            holder.full_screen_icon.setOnClickListener(v -> {
                if (v.getTag() != null)
                    listener.onCameraFullScreenClick((int) v.getTag(), "Snapshot");
            });

            final String imageUrl = domoticz.getSnapshotUrl(mDeviceInfo.getCameraIdx());
            holder.dummyImg.setVisibility(View.VISIBLE);
            holder.row_wrapper.setBackground(null);

            Drawable cache = CameraUtil.getDrawable(imageUrl);
            if (cache == null) {
                picasso.load(imageUrl)
                        .noPlaceholder()
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .into(holder.dummyImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                CameraUtil.setDrawable(imageUrl, holder.dummyImg.getDrawable());
                            }

                            @Override
                            public void onError(Exception e) {
                                if (holder.dummyImg.getDrawable() == null)
                                    holder.dummyImg.setVisibility(View.GONE);
                                holder.row_wrapper.setBackgroundColor(listviewRowBackground);
                            }
                        });
            } else {
                picasso.load(imageUrl)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .noFade()
                        .placeholder(cache)
                        .networkPolicy(NetworkPolicy.NO_CACHE, NetworkPolicy.NO_STORE)
                        .into(holder.dummyImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                CameraUtil.setDrawable(imageUrl, holder.dummyImg.getDrawable());
                            }

                            @Override
                            public void onError(Exception e) {
                                if (holder.dummyImg.getDrawable() == null)
                                    holder.dummyImg.setVisibility(View.GONE);
                                holder.row_wrapper.setBackgroundColor(listviewRowBackground);
                            }
                        });
            }
        } else {
            holder.full_screen_icon.setVisibility(View.GONE);
            holder.dummyImg.setVisibility(View.GONE);
            holder.row_wrapper.setBackgroundColor(listviewRowBackground);
        }
    }

    /**
     * Sets the data for a default device
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setDefaultRowData(DevicesInfo mDeviceInfo,
                                   DataObjectHolder holder) {

        String text;

        holder.switch_battery_level.setMaxLines(3);
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null) {
            holder.switch_name.setText(mDeviceInfo.getName());
        }

        String tempSign = "";
        String windSign = "";
        if (mConfigInfo != null) {
            tempSign = mConfigInfo.getTempSign();
            windSign = mConfigInfo.getWindSign();
        }

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": ";
            if (mDeviceInfo.getLastUpdateDateTime() != null) {
                text += UsefulBits.getFormattedDate(context,
                        mDeviceInfo.getLastUpdateDateTime().getTime());
            }
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status)
                    + ": "
                    + mDeviceInfo.getData();
            holder.switch_battery_level.setText(text);

            if (mDeviceInfo.getUsage() != null && mDeviceInfo.getUsage().length() > 0) {
                text = context.getString(R.string.usage) + ": " + mDeviceInfo.getUsage();
                holder.switch_battery_level.setText(text);
            }
            if (mDeviceInfo.getCounterToday() != null && mDeviceInfo.getCounterToday().length() > 0)
                holder.switch_battery_level.append(" " + context.getString(R.string.today) + ": " + mDeviceInfo.getCounterToday());
            if (mDeviceInfo.getCounter() != null && mDeviceInfo.getCounter().length() > 0 &&
                    !mDeviceInfo.getCounter().equals(mDeviceInfo.getData()))
                holder.switch_battery_level.append(" " + context.getString(R.string.total) + ": " + mDeviceInfo.getCounter());
            if (mDeviceInfo.getType().equals("Wind")) {
                text = context.getString(R.string.direction) + " " + mDeviceInfo.getDirection() + " " + mDeviceInfo.getDirectionStr();
                holder.switch_battery_level.setText(text);
            }
            if (!UsefulBits.isEmpty(mDeviceInfo.getForecastStr()))
                holder.switch_battery_level.setText(mDeviceInfo.getForecastStr());
            if (!UsefulBits.isEmpty(mDeviceInfo.getSpeed()))
                holder.switch_battery_level.append(", " + context.getString(R.string.speed) + ": " + mDeviceInfo.getSpeed() + " " + windSign);
            if (mDeviceInfo.getDewPoint() > 0)
                holder.switch_battery_level.append(", " + context.getString(R.string.dewPoint) + ": " + mDeviceInfo.getDewPoint() + " " + tempSign);
            if (mDeviceInfo.getTemp() > 0)
                holder.switch_battery_level.append(", " + context.getString(R.string.temp) + ": " + mDeviceInfo.getTemp() + " " + tempSign);
            if (mDeviceInfo.getBarometer() > 0)
                holder.switch_battery_level.append(", " + context.getString(R.string.pressure) + ": " + mDeviceInfo.getBarometer());
            if (!UsefulBits.isEmpty(mDeviceInfo.getChill()))
                holder.switch_battery_level.append(", " + context.getString(R.string.chill) + ": " + mDeviceInfo.getChill() + " " + tempSign);
            if (!UsefulBits.isEmpty(mDeviceInfo.getHumidityStatus()))
                holder.switch_battery_level.append(", " + context.getString(R.string.humidity) + ": " + mDeviceInfo.getHumidityStatus());
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        holder.iconRow.setAlpha(1f);
        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
    }

    /**
     * Set the data for the security panel
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setSecurityPanelSwitchRowData(DevicesInfo mDeviceInfo, DataObjectHolder holder) {
        holder.isProtected = mDeviceInfo.isProtected();
        holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());

        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                mDeviceInfo.getData();
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDeviceInfo.getIdx());
            if (mDeviceInfo.getData().startsWith("Arm"))
                holder.buttonOn.setText(context.getString(R.string.button_disarm));
            else
                holder.buttonOn.setText(context.getString(R.string.button_arm));

            holder.buttonOn.setOnClickListener(v -> {
                //open security panel
                handleSecurityPanel(v.getId());
            });
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSwitchType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
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
                    .addTestDevice("2C114D01992840EC6BF853D44CB96754")
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

    /**
     * Set the data for the on/off buttons
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setOnOffButtonRowData(final DevicesInfo mDeviceInfo,
                                       final DataObjectHolder holder) {
        String text;

        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }
        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    mDeviceInfo.getData();
            holder.switch_battery_level.setText(text);
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (holder.buttonOn != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonOn.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonOn.setId(mDeviceInfo.getIdx());

            holder.buttonOn.setOnClickListener(v -> handleOnOffSwitchClick(v.getId(), true));
        }
        if (holder.buttonOff != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonOff.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonOff.setId(mDeviceInfo.getIdx());
            holder.buttonOff.setOnClickListener(v -> handleOnOffSwitchClick(v.getId(), false));
        }

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        if (holder.buttonLog != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonLog.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonLog.setId(mDeviceInfo.getIdx());

            holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
        }
    }

    /**
     * Set the data for the on/off switch
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setOnOffSwitchRowData(final DevicesInfo mDeviceInfo,
                                       final DataObjectHolder holder) {
        String text;

        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        text = context.getString(R.string.status) + ": " +
                mDeviceInfo.getData();
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        if (holder.onOffSwitch != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.onOffSwitch.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.onOffSwitch.setId(mDeviceInfo.getIdx());

            holder.onOffSwitch.setOnCheckedChangeListener(null);
            holder.onOffSwitch.setChecked(mDeviceInfo.getStatusBoolean());
            holder.onOffSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
                handleOnOffSwitchClick(compoundButton.getId(), checked);
                mDeviceInfo.setStatusBoolean(checked);
                if (!checked)
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);
            });
        }

        if (holder.buttonLog != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonLog.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonLog.setId(mDeviceInfo.getIdx());

            holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
        }
    }

    /**
     * Set the data for the thermostat devices
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setThermostatRowData(DevicesInfo mDeviceInfo, DataObjectHolder holder) {
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        final double setPoint = mDeviceInfo.getSetPoint();
        if (holder.isProtected)
            holder.buttonOn.setEnabled(false);
        holder.buttonOn.setText(context.getString(R.string.set_temperature));
        holder.buttonOn.setOnClickListener(v -> handleThermostatClick(v.getId()));
        holder.buttonOn.setId(mDeviceInfo.getIdx());

        holder.switch_name.setText(mDeviceInfo.getName());

        String text;
        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            String setPointText =
                    context.getString(R.string.set_point) + ": " + setPoint;
            holder.switch_battery_level.setText(setPointText);
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(
                mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                false,
                false,
                null)).into(holder.iconRow);
    }

    /**
     * Set the data for temperature devices
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setTemperatureRowData(DevicesInfo mDeviceInfo, DataObjectHolder holder) {
        final double temperature = mDeviceInfo.getTemperature();
        final double setPoint = mDeviceInfo.getSetPoint();
        int modeIconRes = 0;
        holder.isProtected = mDeviceInfo.isProtected();

        holder.switch_name.setText(mDeviceInfo.getName());
        if (Double.isNaN(temperature) || Double.isNaN(setPoint)) {
            if (holder.signal_level != null)
                holder.signal_level.setVisibility(View.GONE);

            if (holder.switch_battery_level != null) {
                String batteryText = context.getString(R.string.temperature)
                        + ": "
                        + mDeviceInfo.getData();
                holder.switch_battery_level.setText(batteryText);
            }
        } else {
            if (holder.signal_level != null)
                holder.signal_level.setVisibility(View.VISIBLE);
            if (holder.switch_battery_level != null) {
                String batteryLevelText = context.getString(R.string.temperature)
                        + ": "
                        + temperature
                        + " C";
                holder.switch_battery_level.setText(batteryLevelText);
            }

            if (holder.signal_level != null) {
                String signalText = context.getString(R.string.set_point)
                        + ": "
                        + mDeviceInfo.getSetPoint()
                        + " C";
                holder.signal_level.setText(signalText);
            }
        }

        if (holder.isProtected)
            holder.buttonSet.setEnabled(false);

        if ("evohome".equals(mDeviceInfo.getHardwareName())) {
            holder.buttonSet.setText(context.getString(R.string.set_temperature));
            holder.buttonSet.setOnClickListener(v -> handleSetTemperatureClick(v.getId()));
            holder.buttonSet.setId(mDeviceInfo.getIdx());
            holder.buttonSet.setVisibility(View.VISIBLE);

            modeIconRes = getEvohomeStateIconResource(mDeviceInfo.getStatus());
        } else {
            holder.buttonSet.setVisibility(View.GONE);
        }

        if (holder.iconMode != null) {
            if (0 == modeIconRes) {
                holder.iconMode.setVisibility(View.GONE);
            } else {
                holder.iconMode.setImageResource(modeIconRes);
                holder.iconMode.setVisibility(View.VISIBLE);
            }
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(), mDeviceInfo.getType(), mDeviceInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    /**
     * Set the data for the contact switch
     *
     * @param mDevicesInfo  Device info class
     * @param holder        Holder to use
     * @param noButtonShown Should the button be shown?
     */
    private void setContactSwitchRowData(DevicesInfo mDevicesInfo,
                                         DataObjectHolder holder,
                                         boolean noButtonShown) {
        if (mDevicesInfo == null || holder == null)
            return;

        ArrayList<String> statusOpen = new ArrayList<>();
        statusOpen.add("open");

        ArrayList<String> statusClosed = new ArrayList<>();
        statusClosed.add("closed");

        holder.isProtected = mDevicesInfo.isProtected();
        if (holder.switch_name != null) {
            holder.switch_name.setText(mDevicesInfo.getName());
        }

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context, mDevicesInfo.getLastUpdateDateTime().getTime());
        if (holder.signal_level != null) {
            holder.signal_level.setText(text);
        }
        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " + mDevicesInfo.getData();
            holder.switch_battery_level.setText(text);
        }

        if (holder.buttonOn != null) {
            if (!noButtonShown) {
                holder.buttonOn.setVisibility(View.GONE);
            } else {
                holder.buttonOn.setId(mDevicesInfo.getIdx());
                if (!UsefulBits.isEmpty(mDevicesInfo.getData())) {
                    String status = mDevicesInfo.getData().toLowerCase();
                    if (statusOpen.contains(status)) {
                        holder.buttonOn.setText(context.getString(R.string.button_state_open));
                    } else if (statusClosed.contains(status)) {
                        holder.buttonOn.setText(context.getString(R.string.button_state_closed));
                    } else {
                        if (status.startsWith("off")) status = "off";
                        holder.buttonOn.setText(status.toUpperCase());
                    }
                }
                holder.buttonOn.setOnClickListener(v -> {
                    String text1 = (String) ((Button) v).getText();
                    handleOnButtonClick(v.getId(), text1.equals(context.getString(R.string.button_state_on)));
                });
            }
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDevicesInfo.getTypeImg(),
                mDevicesInfo.getType(),
                mDevicesInfo.getSwitchType(),
                mDevicesInfo.getStatusBoolean(),
                mDevicesInfo.getUseCustomImage(),
                mDevicesInfo.getImage())).into(holder.iconRow);

        if (!mDevicesInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
    }


    /**
     * Set the data for a push on/off device
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setPushOnOffSwitchRowData(DevicesInfo mDeviceInfo, DataObjectHolder holder, boolean action) {
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                mDeviceInfo.getData();
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
            holder.buttonOn.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
        else
            holder.buttonOn.setId(mDeviceInfo.getIdx());

        if (action) {
            holder.buttonOn.setText(context.getString(R.string.button_state_on));
            //holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_on));
        } else {
            holder.buttonOn.setText(context.getString(R.string.button_state_off));
            //holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_off));
        }

        holder.buttonOn.setOnClickListener(v -> {
            try {
                String text1 = (String) ((Button) v).getText();
                handleOnButtonClick(v.getId(), text1.equals(context.getString(R.string.button_state_on)));
            } catch (Exception ignore) {
            }
        });

        if (holder.buttonLog != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonLog.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonLog.setId(mDeviceInfo.getIdx());

            holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
        }
    }

    /**
     * Set the data for blinds
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setBlindsRowData(final DevicesInfo mDeviceInfo,
                                  DataObjectHolder holder) {

        String text;
        holder.isProtected = mDeviceInfo.isProtected();
        holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(
                    context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    mDeviceInfo.getData();
            holder.switch_battery_level.setText(text);
        }

        holder.buttonUp.setId(mDeviceInfo.getIdx());
        holder.buttonUp.setOnClickListener(view -> {
            for (DevicesInfo e : data) {
                if (e.getIdx() == view.getId()) {
                    if (e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED || e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED || e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTEDSTOP)
                        handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.ON);
                    else
                        handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.OFF);
                }
            }
        });

        holder.buttonStop.setId(mDeviceInfo.getIdx());
        holder.buttonStop.setOnClickListener(view -> {
            for (DevicesInfo e : data) {
                if (e.getIdx() == view.getId()) {
                    handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.STOP);
                }
            }
        });

        holder.buttonDown.setId(mDeviceInfo.getIdx());
        holder.buttonDown.setOnClickListener(view -> {
            for (DevicesInfo e : data) {
                if (e.getIdx() == view.getId()) {
                    if (e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED || e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED || e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTEDSTOP)
                        handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.OFF);
                    else
                        handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.ON);
                }
            }
        });

        if (holder.dimmer.getVisibility() == View.VISIBLE) {
            holder.dimmer.setTag(mDeviceInfo.getIdx());
            holder.dimmer.setValueTo(mDeviceInfo.getMaxDimLevel() <= 0 ? 100 : mDeviceInfo.getMaxDimLevel());
            holder.dimmer.setValue(mDeviceInfo.getLevel() > holder.dimmer.getValueTo() ? holder.dimmer.getValueTo() : mDeviceInfo.getLevel());
            holder.dimmer.setLabelFormatter(value -> (Math.round(value)) + "%");
            holder.dimmer.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
                @Override
                public void onStartTrackingTouch(@NonNull Slider slider) {
                    previousDimmerValue = (Math.round(slider.getValue()));
                }

                @Override
                public void onStopTrackingTouch(@NonNull Slider slider) {
                    int progress = (Math.round(slider.getValue()));
                    handleDimmerChange((int) slider.getTag(), progress, false);
                    mDeviceInfo.setLevel(progress);
                }
            });
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
    }

    /**
     * Set the data for a selector switch
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setSelectorRowData(final DevicesInfo mDeviceInfo,
                                    final DataObjectHolder holder) {
        String text;

        holder.isProtected = mDeviceInfo.isProtected();
        holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    mDeviceInfo.getStatus();
            holder.switch_battery_level.setText(text);
        }

        int loadLevel = !mDeviceInfo.isLevelOffHidden() ? mDeviceInfo.getLevel() / 10 : (mDeviceInfo.getLevel() - 1) / 10;
        final ArrayList<String> levelNames = mDeviceInfo.getLevelNames();
        if (mDeviceInfo.isLevelOffHidden())
            levelNames.remove(0);

        holder.spSelector.setTag(mDeviceInfo.getIdx());
        if (levelNames != null && levelNames.size() > loadLevel) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, levelNames);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.spSelector.setAdapter(dataAdapter);
            holder.spSelector.setSelection(loadLevel);
        }
        holder.spSelector.setVisibility(View.VISIBLE);

        holder.spSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if (((int) holder.spSelector.getTag()) == mDeviceInfo.getIdx()) {
                    holder.spSelector.setTag(mDeviceInfo.getIdx() * 3);
                } else {
                    String selValue = holder.spSelector.getItemAtPosition(arg2).toString();
                    handleSelectorChange(mDeviceInfo, selValue, levelNames);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSwitchType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);
    }

    /**
     * Set the data for a dimmer
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setDimmerRowData(final DevicesInfo mDeviceInfo,
                                  final DataObjectHolder holder,
                                  final boolean isRGB) {
        String text;

        holder.isProtected = mDeviceInfo.isProtected();

        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    mDeviceInfo.getStatus();
            holder.switch_battery_level.setText(text);
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        holder.dimmerOnOffSwitch.setId(mDeviceInfo.getIdx() + ID_SWITCH);

        holder.dimmerOnOffSwitch.setOnCheckedChangeListener(null);
        holder.dimmerOnOffSwitch.setChecked(mDeviceInfo.getStatusBoolean());
        holder.dimmerOnOffSwitch.setOnCheckedChangeListener((compoundButton, checked) -> {
            handleOnOffSwitchClick(compoundButton.getId(), checked);
            mDeviceInfo.setStatusBoolean(checked);
            if (checked) {
                holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.dimmer.getValue() <= 10) {
                    //dimmer turned on with default progress value
                    holder.dimmer.setValue(20 > holder.dimmer.getValueTo() ? holder.dimmer.getValueTo() : 20);
                }
                if (isRGB)
                    holder.buttonColor.setVisibility(View.VISIBLE);
            } else {
                holder.dimmer.setVisibility(View.GONE);
                if (isRGB)
                    holder.buttonColor.setVisibility(View.GONE);
            }
            if (!checked)
                holder.iconRow.setAlpha(0.5f);
            else
                holder.iconRow.setAlpha(1f);
        });

        holder.dimmer.setTag(mDeviceInfo.getIdx());
        holder.dimmer.setValueTo(mDeviceInfo.getMaxDimLevel() <= 0 ? 100 : mDeviceInfo.getMaxDimLevel());
        holder.dimmer.setValue(mDeviceInfo.getLevel() > holder.dimmer.getValueTo() ? holder.dimmer.getValueTo() : mDeviceInfo.getLevel());
        holder.dimmer.setLabelFormatter(value -> (Math.round(value)) + "%");
        holder.dimmer.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                previousDimmerValue = (Math.round(slider.getValue()));
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int progress = (Math.round(slider.getValue()));
                SwitchMaterial dimmerOnOffSwitch = null;
                try {
                    dimmerOnOffSwitch = slider.getRootView()
                            .findViewById(mDeviceInfo.getIdx() + ID_SWITCH);
                    if (progress == 0 && dimmerOnOffSwitch.isChecked()) {
                        dimmerOnOffSwitch.setChecked(false);
                        slider.setValue(previousDimmerValue);
                    } else if (progress > 0 && !dimmerOnOffSwitch.isChecked()) {
                        dimmerOnOffSwitch.setChecked(true);
                    }
                } catch (Exception ex) {/*else we don't use a switch, but buttons */}

                handleDimmerChange((int) slider.getTag(), progress, false);
                mDeviceInfo.setLevel(progress);
            }
        });

        if (!mDeviceInfo.getStatusBoolean()) {
            holder.dimmer.setVisibility(View.GONE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.GONE);
        } else {
            holder.dimmer.setVisibility(View.VISIBLE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.VISIBLE);
        }

        if (holder.buttonLog != null) {
            holder.buttonLog.setId(mDeviceInfo.getIdx());
            holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
        }

        if (isRGB && holder.buttonColor != null) {
            holder.buttonColor.setId(mDeviceInfo.getIdx());
            holder.buttonColor.setOnClickListener(v -> handleColorButtonClick(v.getId()));
        }
    }

    /**
     * Set the data for a dimmer
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setDimmerOnOffButtonRowData(final DevicesInfo mDeviceInfo,
                                             final DataObjectHolder holder,
                                             final boolean isRGB) {
        String text;
        holder.isProtected = mDeviceInfo.isProtected();

        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.signal_level != null && mDeviceInfo.getLastUpdateDateTime() != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    mDeviceInfo.getStatus();
            holder.switch_battery_level.setText(text);
        }

        String percentage = calculateDimPercentage(
                mDeviceInfo.getMaxDimLevel(), mDeviceInfo.getLevel());
        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDeviceInfo.getIdx());
            holder.buttonOn.setOnClickListener(v -> {
                handleOnOffSwitchClick(v.getId(), true);
                holder.iconRow.setAlpha(1f);
                holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.dimmer.getValue() <= 10) {
                    //dimmer turned on with default progress value
                    holder.dimmer.setValue(20 > holder.dimmer.getValueTo() ? holder.dimmer.getValueTo() : 20);
                }
                if (isRGB)
                    holder.buttonColor.setVisibility(View.VISIBLE);

            });
        }
        if (holder.buttonOff != null) {
            holder.buttonOff.setId(mDeviceInfo.getIdx());
            holder.buttonOff.setOnClickListener(v -> {
                handleOnOffSwitchClick(v.getId(), false);
                holder.iconRow.setAlpha(0.5f);
                holder.dimmer.setVisibility(View.GONE);
                if (isRGB)
                    holder.buttonColor.setVisibility(View.GONE);
            });
        }

        holder.dimmer.setTag(mDeviceInfo.getIdx());
        holder.dimmer.setValueTo(mDeviceInfo.getMaxDimLevel() <= 0 ? 100 : mDeviceInfo.getMaxDimLevel());
        holder.dimmer.setValue(mDeviceInfo.getLevel() > holder.dimmer.getValueTo() ? holder.dimmer.getValueTo() : mDeviceInfo.getLevel());
        holder.dimmer.setLabelFormatter(value -> (Math.round(value)) + "%");
        holder.dimmer.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                previousDimmerValue = (Math.round(slider.getValue()));
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                int progress = (Math.round(slider.getValue()));
                handleDimmerChange((int) slider.getTag(), progress, false);
                mDeviceInfo.setLevel(progress);
            }
        });

        if (!mDeviceInfo.getStatusBoolean()) {
            holder.dimmer.setVisibility(View.GONE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.GONE);
        } else {
            holder.dimmer.setVisibility(View.VISIBLE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.VISIBLE);
        }

        if (holder.buttonLog != null) {
            holder.buttonLog.setId(mDeviceInfo.getIdx());
            holder.buttonLog.setOnClickListener(v -> handleLogButtonClick(v.getId()));
        }

        if (isRGB && holder.buttonColor != null) {
            holder.buttonColor.setId(mDeviceInfo.getIdx());
            holder.buttonColor.setOnClickListener(v -> handleColorButtonClick(v.getId()));
        }
    }

    /**
     * Set the data for temperature devices
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setModalSwitchRowData(DevicesInfo mDeviceInfo,
                                       DataObjectHolder holder,
                                       final int stateArrayRes,
                                       final int stateNamesArrayRes,
                                       final int[] stateIds) {

        holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                UsefulBits.getFormattedDate(context,
                        mDeviceInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                getStatus(stateArrayRes, stateNamesArrayRes, mDeviceInfo.getStatus());
        holder.switch_battery_level.setText(text);

        if (holder.buttonSetStatus != null) {
            holder.buttonSetStatus.setId(mDeviceInfo.getIdx());
            holder.buttonSetStatus.setOnClickListener(v -> {
                //open state dialog
                handleStateButtonClick(v.getId(), stateNamesArrayRes, stateIds);
            });
        }

        Picasso.get().load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSwitchType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);
    }

    /**
     * Gets the status text
     *
     * @param statusArrayRes      Status array to use
     * @param statusNamesArrayRes Status array of names to use
     * @param text                Text to find
     * @return Returns the status text
     */
    private String getStatus(int statusArrayRes, int statusNamesArrayRes, String text) {
        Resources res = context.getResources();
        String[] states = res.getStringArray(statusArrayRes);
        String[] stateNames = res.getStringArray(statusNamesArrayRes);

        int length = states.length;
        for (int i = 0; i < length; i++) {
            if (states[i].equals(text))
                return stateNames[i];
        }
        return text;
    }


    /**
     * Handles the color button
     *
     * @param idx IDX of the device to change
     */
    private void handleColorButtonClick(int idx) {
        listener.onColorButtonClick(idx);
    }

    /**
     * Interface which handles the clicks of the thermostat set button
     *
     * @param idx IDX of the device to change
     */
    public void handleThermostatClick(int idx) {
        listener.onThermostatClick(idx);
    }

    /**
     * Handles the temperature click
     *
     * @param idx IDX of the device to change
     */
    public void handleSetTemperatureClick(int idx) {
        listener.onSetTemperatureClick(idx);
    }

    /**
     * Handles the on/off switch click
     *
     * @param idx    IDX of the device to change
     * @param action Action to take
     */
    private void handleOnOffSwitchClick(int idx, boolean action) {
        listener.onSwitchClick(idx, action);
    }

    /**
     * Handles the security panel
     *
     * @param idx IDX of the device to change
     */
    private void handleSecurityPanel(int idx) {
        listener.onSecurityPanelButtonClick(idx);
    }

    /**
     * Handles the on button click
     *
     * @param idx    IDX of the device to change
     * @param action Action to take
     */
    private void handleOnButtonClick(int idx, boolean action) {
        listener.onButtonClick(idx, action);
    }

    /**
     * Handles the blicks click
     *
     * @param idx    IDX of the device to change
     * @param action Action to take
     */
    private void handleBlindsClick(int idx, int action) {
        listener.onBlindClick(idx, action);
    }

    /**
     * Handles the dimmer change
     *
     * @param idx      IDX of the device to change
     * @param value    Value to change the device to
     * @param selector True if it's a selector device
     */
    private void handleDimmerChange(final int idx, final int value, boolean selector) {
        listener.onDimmerChange(idx, value > 100 ? 100 : value, selector);
    }

    /**
     * Handles the state button click
     *
     * @param idx      IDX of the device to change
     * @param itemsRes Resource ID of the items
     * @param itemIds  State ID's
     */
    private void handleStateButtonClick(final int idx, int itemsRes, int[] itemIds) {
        listener.onStateButtonClick(idx, itemsRes, itemIds);
    }

    /**
     * Handles the selector dimmer click
     */
    private void handleSelectorChange(DevicesInfo device, String levelName, ArrayList<String> levelNames) {
        for (int i = 0; i < levelNames.size(); i++) {
            if (levelNames.get(i).equals(levelName)) {
                listener.onSelectorChange(device.getIdx(), device.isLevelOffHidden() ? (i * 10) : (i * 10) - 10);
            }
        }
    }

    /**
     * Handles the log button click
     *
     * @param idx IDX of the device to change
     */
    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    /**
     * Calculates the dim percentage
     *
     * @param maxDimLevel Max dim level
     * @param level       Current level
     * @return Calculated percentage
     */
    private String calculateDimPercentage(float maxDimLevel, float level) {
        float percentage = (level / maxDimLevel) * 100;
        return String.format("%.0f", percentage) + "%";
    }

    /**
     * Get's the icon of the Evo home state
     *
     * @param stateName The current state to return the icon for
     * @return Returns resource ID for the icon
     */
    private int getEvohomeStateIconResource(String stateName) {
        if (stateName == null) return 0;
        TypedArray icons = context.getResources().obtainTypedArray(R.array.evohome_zone_state_icons);
        String[] states = context.getResources().getStringArray(R.array.evohome_state_names);
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

    public void setButtons(DataObjectHolder holder, int button) {
        holder.itemView.setVisibility(View.VISIBLE);
        if (holder.contentWrapper != null)
            holder.contentWrapper.setVisibility(View.VISIBLE);
        if (holder.adview != null)
            holder.adview.setVisibility(View.GONE);
        if (holder.dimmerOnOffSwitch != null) {
            holder.dimmerOnOffSwitch.setVisibility(View.GONE);
        }
        if (holder.dimmer != null) {
            holder.dimmer.setVisibility(View.GONE);
        }
        if (holder.adview != null) {
            holder.adview.setVisibility(View.GONE);
        }
        if (holder.buttonColor != null) {
            holder.buttonColor.setVisibility(View.GONE);
        }
        if (holder.spSelector != null) {
            holder.spSelector.setVisibility(View.GONE);
        }
        if (holder.buttonLog != null) {
            holder.buttonLog.setVisibility(View.VISIBLE);
        }
        if (holder.buttonNotifications != null) {
            holder.buttonNotifications.setVisibility(View.VISIBLE);
        }
        if (holder.buttonTimer != null) {
            holder.buttonTimer.setVisibility(View.VISIBLE);
        }
        if (holder.buttonUp != null) {
            holder.buttonUp.setVisibility(View.GONE);
        }
        if (holder.buttonStop != null) {
            holder.buttonStop.setVisibility(View.GONE);
        }
        if (holder.buttonDown != null) {
            holder.buttonDown.setVisibility(View.GONE);
        }
        if (holder.buttonSet != null) {
            holder.buttonSet.setVisibility(View.GONE);
        }
        if (holder.buttonSetStatus != null) {
            holder.buttonSetStatus.setVisibility(View.GONE);
        }
        if (holder.buttonOff != null) {
            holder.buttonOff.setText(context.getString(R.string.button_state_off));
            holder.buttonOff.setVisibility(View.GONE);
        }
        if (holder.buttonOn != null) {
            holder.buttonOn.setText(context.getString(R.string.button_state_on));
            holder.buttonOn.setVisibility(View.GONE);
        }
        if (holder.onOffSwitch != null) {
            holder.onOffSwitch.setVisibility(View.GONE);
        }

        switch (button) {
            case Buttons.SWITCH:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.onOffSwitch != null)
                    holder.onOffSwitch.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.ADS:
                if (holder.adview != null)
                    holder.adview.setVisibility(View.VISIBLE);
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.GONE);
                break;
            case Buttons.BUTTONS:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.VISIBLE);
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.SET:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonSet != null)
                    holder.buttonSet.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.MODAL:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonSetStatus != null)
                    holder.buttonSetStatus.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.BUTTON_ON:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.BUTTON_OFF:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.BLINDS:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.buttonDown.setVisibility(View.VISIBLE);
                if (holder.buttonUp != null)
                    holder.buttonUp.setVisibility(View.VISIBLE);
                if (holder.buttonStop != null)
                    holder.buttonStop.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.GONE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.BLINDS_NOSTOP:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.buttonDown.setVisibility(View.VISIBLE);
                if (holder.buttonUp != null)
                    holder.buttonUp.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.GONE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.BLINDS_DIMMER:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.buttonDown.setVisibility(View.VISIBLE);
                if (holder.buttonUp != null)
                    holder.buttonUp.setVisibility(View.VISIBLE);
                if (holder.buttonStop != null)
                    holder.buttonStop.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.BLINDS_DIMMER_NOSTOP:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.buttonDown.setVisibility(View.VISIBLE);
                if (holder.buttonUp != null)
                    holder.buttonUp.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.DIMMER_RGB:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.dimmerOnOffSwitch != null)
                    holder.dimmerOnOffSwitch.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.buttonColor != null)
                    holder.buttonColor.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.DIMMER:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.dimmerOnOffSwitch != null)
                    holder.dimmerOnOffSwitch.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.DIMMER_BUTTONS:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.VISIBLE);
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            case Buttons.SELECTOR:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.spSelector != null)
                    holder.spSelector.setVisibility(View.VISIBLE);
                if (holder.dimmerOnOffSwitch != null)
                    holder.dimmerOnOffSwitch.setVisibility(View.GONE);
                break;
            case Buttons.SELECTOR_BUTTONS:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.GONE);
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.GONE);
                if (holder.spSelector != null)
                    holder.spSelector.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
            default:
                if (holder.contentWrapper != null)
                    holder.contentWrapper.setVisibility(View.VISIBLE);
                holder.switch_battery_level.setVisibility(View.VISIBLE);
                if (holder.adview != null)
                    holder.adview.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    private void handleTimerButtonClick(int idx) {
        listener.onTimerButtonClick(idx);
    }

    private void handleNotificationButtonClick(int idx) {
        listener.onNotificationButtonClick(idx);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        swap(fromPosition, toPosition);
        return false;
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
        for (DevicesInfo d : filteredData) {
            if (d.getIdx() != -9998)
                ids.add(String.valueOf(d.getIdx()));
        }
        mCustomSorting = ids;
    }

    @Override
    public void onItemDismiss(int position, int direction) {
        remove(position);
    }

    interface Buttons {
        int NOTHING = 0;
        int SWITCH = 1;
        int SET = 2;
        int BUTTONS = 3;
        int BLINDS = 4;
        int BLINDS_NOSTOP = 9;
        int BLINDS_DIMMER = 15;
        int BLINDS_DIMMER_NOSTOP = 16;
        int DIMMER = 5;
        int DIMMER_RGB = 6;
        int BUTTON_ON = 7;
        int BUTTON_OFF = 8;
        int MODAL = 10;
        int DIMMER_BUTTONS = 11;
        int SELECTOR = 12;
        int SELECTOR_BUTTONS = 13;
        int CLOCK = 14;
        int ADS = 17;
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level;
        SwitchMaterial onOffSwitch, dimmerOnOffSwitch;
        ImageView buttonUp, buttonDown, buttonStop;
        Button buttonOn, buttonColor, buttonSetStatus, buttonSet, buttonOff;
        Chip buttonLog, buttonTimer, buttonNotifications;
        Boolean isProtected;
        LikeButton likeButton;
        ImageView iconRow, iconMode, full_screen_icon;
        Slider dimmer;
        Spinner spSelector;
        LinearLayout extraPanel, row_wrapper;
        ImageView infoIcon;
        ImageView dummyImg;
        TemplateView adview;
        RelativeLayout contentWrapper;

        public DataObjectHolder(View itemView) {
            super(itemView);

            contentWrapper = itemView.findViewById(R.id.contentWrapper);
            adview = itemView.findViewById(R.id.adview);
            buttonOn = itemView.findViewById(R.id.on_button);
            buttonOff = itemView.findViewById(R.id.off_button);
            buttonSetStatus = itemView.findViewById(R.id.set_button);
            dummyImg = itemView.findViewById(R.id.dummyImg);

            row_wrapper = itemView.findViewById(R.id.row_wrapper);
            onOffSwitch = itemView.findViewById(R.id.switch_button);
            signal_level = itemView.findViewById(R.id.switch_signal_level);
            iconRow = itemView.findViewById(R.id.rowIcon);
            switch_name = itemView.findViewById(R.id.switch_name);
            switch_battery_level = itemView.findViewById(R.id.switch_battery_level);
            full_screen_icon = itemView.findViewById(R.id.full_screen_icon);

            dimmerOnOffSwitch = itemView.findViewById(R.id.switch_dimmer_switch);
            dimmer = itemView.findViewById(R.id.switch_dimmer);
            spSelector = itemView.findViewById(R.id.spSelector);
            buttonColor = itemView.findViewById(R.id.color_button);
            buttonLog = itemView.findViewById(R.id.log_button);
            buttonTimer = itemView.findViewById(R.id.timer_button);
            buttonUp = itemView.findViewById(R.id.switch_button_up);
            buttonNotifications = itemView.findViewById(R.id.notifications_button);
            buttonStop = itemView.findViewById(R.id.switch_button_stop);
            buttonDown = itemView.findViewById(R.id.switch_button_down);
            buttonSet = itemView.findViewById(R.id.set_button);
            infoIcon = itemView.findViewById(R.id.widget_info_icon);
            likeButton = itemView.findViewById(R.id.fav_button);
            if (buttonLog != null)
                buttonLog.setVisibility(View.GONE);
            if (buttonTimer != null)
                buttonTimer.setVisibility(View.GONE);
            extraPanel = itemView.findViewById(R.id.extra_panel);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);
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

            final ArrayList<DevicesInfo> list = data;

            int count = list.size();
            final ArrayList<DevicesInfo> devicesInfos = new ArrayList<>(count);

            DevicesInfo filterableObject;
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
            filteredData = (ArrayList<DevicesInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}