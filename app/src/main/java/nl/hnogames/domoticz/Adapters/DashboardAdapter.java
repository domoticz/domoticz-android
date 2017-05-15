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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import az.plainpie.PieView;
import az.plainpie.animation.PieAngleAnimation;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DataObjectHolder> {
    public static final int ID_SCENE_SWITCH = 2000;
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

    public ArrayList<DevicesInfo> data = null;
    public ArrayList<DevicesInfo> filteredData = null;
    private boolean showAsList = false;
    private Domoticz domoticz;
    private Context context;
    private switchesClickListener listener;
    private int previousDimmerValue;
    private SharedPrefUtil mSharedPrefs;
    private ConfigInfo mConfigInfo;
    private ItemFilter mFilter = new ItemFilter();

    public DashboardAdapter(Context context,
                            ServerUtil serverUtil,
                            ArrayList<DevicesInfo> data,
                            switchesClickListener listener,
                            boolean showAsList) {
        super();
        this.showAsList = showAsList;
        mSharedPrefs = new SharedPrefUtil(context);

        this.context = context;
        domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());

        mConfigInfo = serverUtil.getActiveServer().getConfigInfo(context);
        this.listener = listener;

        setData(data);
    }

    public void setData(ArrayList<DevicesInfo> data) {
        // When not sorted the devices are almost like dashboard on server
        if (!mSharedPrefs.isDashboardSortedLikeServer()) {
            // Sort alphabetically
            Collections.sort(data, new Comparator<DevicesInfo>() {
                @Override
                public int compare(DevicesInfo left, DevicesInfo right) {
                    return left.getName().compareTo(right.getName());
                }
            });
        }

        this.data = data;
        this.filteredData = data;
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
    public int getItemCount() {
        if (filteredData == null)
            return 0;

        return filteredData.size();
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View row;
        if (showAsList)
            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dashboard_row_list, parent, false);
        else
            row = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dashboard_row, parent, false);

        if (mSharedPrefs.darkThemeEnabled()) {
            ((android.support.v7.widget.CardView) row.findViewById(R.id.card_global_wrapper)).setCardBackgroundColor(Color.parseColor("#3F3F3F"));
            if ((row.findViewById(R.id.row_wrapper)) != null)
                (row.findViewById(R.id.row_wrapper)).setBackground(ContextCompat.getDrawable(context, R.drawable.bordershadowdark));
            if ((row.findViewById(R.id.row_global_wrapper)) != null)
                (row.findViewById(R.id.row_global_wrapper)).setBackgroundColor(ContextCompat.getColor(context, R.color.background_dark));
            if ((row.findViewById(R.id.day_button)) != null)
                (row.findViewById(R.id.day_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.year_button)) != null)
                (row.findViewById(R.id.year_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.month_button)) != null)
                (row.findViewById(R.id.month_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.week_button)) != null)
                (row.findViewById(R.id.week_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.log_button)) != null)
                (row.findViewById(R.id.log_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.timer_button)) != null)
                (row.findViewById(R.id.timer_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.notifications_button)) != null)
                (row.findViewById(R.id.notifications_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
            if ((row.findViewById(R.id.on_button)) != null)
                (row.findViewById(R.id.on_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            if ((row.findViewById(R.id.off_button)) != null)
                (row.findViewById(R.id.off_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            if ((row.findViewById(R.id.color_button)) != null)
                (row.findViewById(R.id.color_button)).setBackground(ContextCompat.getDrawable(context, R.drawable.button_dark_status));
        }

        return new DataObjectHolder(row);
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, final int position) {
        if (filteredData != null && filteredData.size() >= position) {
            DevicesInfo extendedStatusInfo = filteredData.get(position);

            if (!this.mSharedPrefs.darkThemeEnabled()) {
                holder.pieView.setInnerBackgroundColor(ContextCompat.getColor(context, R.color.white));
                holder.pieView.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.pieView.setPercentageTextSize(17);
            }

            setSwitchRowData(extendedStatusInfo, holder);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(v, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemLongClicked(position);
                    return true;
                }
            });
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
        holder.pieView.setVisibility(View.GONE);

        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            if (mDeviceInfo.getSubType() != null && mDeviceInfo.getSubType().equals(DomoticzValues.Device.Utility.SubType.SMARTWARES)) {
                setButtons(holder, Buttons.BUTTON_ON);
                setThermostatRowData(mDeviceInfo, holder);
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
                case DomoticzValues.Device.Type.Value.DOORCONTACT:
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
                            if (mSharedPrefs.showSwitchesAsButtons()) {
                                setButtons(holder, Buttons.BUTTONS);
                                setOnOffButtonRowData(mDeviceInfo, holder);
                            } else {
                                setButtons(holder, Buttons.SWITCH);
                                setOnOffSwitchRowData(mDeviceInfo, holder);
                            }
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

                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    setButtons(holder, Buttons.BUTTON_ON);
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, false);
                    break;

                case DomoticzValues.Device.Type.Value.DIMMER:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                    if (mDeviceInfo.getSubType().startsWith(DomoticzValues.Device.SubType.Name.RGB)) {
                        if (mSharedPrefs.showSwitchesAsButtons()) {
                            setButtons(holder, Buttons.DIMMER_BUTTONS);
                            setDimmerOnOffButtonRowData(mDeviceInfo, holder, true);
                        } else {
                            setButtons(holder, Buttons.DIMMER_RGB);
                            setDimmerRowData(mDeviceInfo, holder, true);
                        }
                    } else {
                        if (mSharedPrefs.showSwitchesAsButtons()) {
                            setButtons(holder, Buttons.DIMMER_BUTTONS);
                            setDimmerOnOffButtonRowData(mDeviceInfo, holder, false);
                        } else {
                            setButtons(holder, Buttons.DIMMER);
                            setDimmerRowData(mDeviceInfo, holder, false);
                        }
                    }
                    break;

                case DomoticzValues.Device.Type.Value.SELECTOR:
                    if (mSharedPrefs.showSwitchesAsButtons()) {
                        setButtons(holder, Buttons.SELECTOR_BUTTONS);
                        setSelectorRowData(mDeviceInfo, holder);
                    } else {
                        setButtons(holder, Buttons.SELECTOR);
                        setSelectorRowData(mDeviceInfo, holder);
                    }
                    break;

                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                    if (DomoticzValues.canHandleStopButton(mDeviceInfo)) {
                        setButtons(holder, Buttons.BLINDS);
                        setBlindsRowData(mDeviceInfo, holder);
                    } else {
                        setButtons(holder, Buttons.BUTTONS);
                        setOnOffButtonRowData(mDeviceInfo, holder);
                    }
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
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status)
                    + ": "
                    + String.valueOf(mDeviceInfo.getData());
            holder.switch_battery_level.setText(text);

            if (mDeviceInfo.getUsage() != null && mDeviceInfo.getUsage().length() > 0) {
                try {
                    int usage = Integer.parseInt(mDeviceInfo.getUsage().replace("Watt", "").trim());
                    if (mDeviceInfo.getUsageDeliv() != null && mDeviceInfo.getUsageDeliv().length() > 0) {
                        int usagedel = Integer.parseInt(mDeviceInfo.getUsageDeliv().replace("Watt", "").trim());
                        text = context.getString(R.string.usage) + ": " + (usage - usagedel) + " Watt";
                        holder.switch_battery_level.setText(text);
                    } else {
                        text = context.getString(R.string.usage) + ": " + mDeviceInfo.getUsage();
                        holder.switch_battery_level.setText(text);
                    }
                } catch (Exception ex) {
                    text = context.getString(R.string.usage) + ": " + mDeviceInfo.getUsage();
                    holder.switch_battery_level.setText(text);
                }
            }

            if (mDeviceInfo.getCounterToday() != null && mDeviceInfo.getCounterToday().length() > 0)
                holder.switch_battery_level.append(" " + context.getString(R.string.today) + ": " + mDeviceInfo.getCounterToday());
            if (mDeviceInfo.getCounter() != null && mDeviceInfo.getCounter().length() > 0 &&
                    !mDeviceInfo.getCounter().equals(mDeviceInfo.getData()))
                holder.switch_battery_level.append(" " + context.getString(R.string.total) + ": " + mDeviceInfo.getCounter());
            if (mDeviceInfo.getType() != null && mDeviceInfo.getType().length() > 0 &&
                    mDeviceInfo.getType().equals("Wind")) {
                text = context.getString(R.string.direction) + " " + mDeviceInfo.getDirection() + " " + mDeviceInfo.getDirectionStr();
                holder.switch_battery_level.setText(text);
            }
            if (!UsefulBits.isEmpty(mDeviceInfo.getForecastStr()))
                holder.switch_battery_level.setText(mDeviceInfo.getForecastStr());
            if (!UsefulBits.isEmpty(mDeviceInfo.getSpeed()))
                holder.switch_battery_level.append(", " + context.getString(R.string.speed) + ": " + mDeviceInfo.getSpeed() + " " + windSign);
            if (mDeviceInfo.getDewPoint() > 0)
                holder.switch_battery_level.append(", " + context.getString(R.string.dewPoint) + ": " + mDeviceInfo.getDewPoint() + " " + tempSign);
            if (mDeviceInfo.getTemperature() > 0) {
                holder.switch_battery_level.append(", " + context.getString(R.string.temp) + ": " + mDeviceInfo.getTemperature() + " " + tempSign);

                holder.pieView.setVisibility(View.VISIBLE);
                double temp = mDeviceInfo.getTemperature();
                if (tempSign != null && !tempSign.equals("C"))
                    temp = temp / 2;

                holder.pieView.setPercentage(Float.valueOf(temp + ""));
                holder.pieView.setInnerText(mDeviceInfo.getTemperature() + " " + tempSign);

                PieAngleAnimation animation = new PieAngleAnimation(holder.pieView);
                animation.setDuration(2000);
                holder.pieView.startAnimation(animation);
            } else {
                holder.pieView.setVisibility(View.GONE);
            }

            if (mDeviceInfo.getBarometer() > 0)
                holder.switch_battery_level.append(", " + context.getString(R.string.pressure) + ": " + mDeviceInfo.getBarometer());
            if (!UsefulBits.isEmpty(mDeviceInfo.getChill()))
                holder.switch_battery_level.append(", " + context.getString(R.string.chill) + ": " + mDeviceInfo.getChill() + " " + tempSign);
            if (!UsefulBits.isEmpty(mDeviceInfo.getHumidityStatus()))
                holder.switch_battery_level.append(", " + context.getString(R.string.humidity) + ": " + mDeviceInfo.getHumidityStatus());
        }

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDeviceInfo.getIdx());
            if (mDeviceInfo.getData().startsWith("Arm"))
                holder.buttonOn.setText(context.getString(R.string.button_disarm));
            else
                holder.buttonOn.setText(context.getString(R.string.button_arm));

            if (mSharedPrefs.darkThemeEnabled())
                holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_status_dark));
            else
                holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_on));

            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open security panel
                    handleSecurityPanel(v.getId());
                }
            });
        }

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                    String.valueOf(mDeviceInfo.getData());
            holder.switch_battery_level.setText(text);
        }

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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

            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), true);
                }
            });
        }
        if (holder.buttonOff != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonOff.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonOff.setId(mDeviceInfo.getIdx());
            holder.buttonOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), false);
                }
            });
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

            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
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
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
            holder.onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    handleOnOffSwitchClick(compoundButton.getId(), checked);
                    mDeviceInfo.setStatusBoolean(checked);
                    if (!checked)
                        holder.iconRow.setAlpha(0.5f);
                    else
                        holder.iconRow.setAlpha(1f);
                }
            });
        }

        if (holder.buttonLog != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonLog.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonLog.setId(mDeviceInfo.getIdx());

            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
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
        holder.buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleThermostatClick(v.getId());
            }
        });
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
                    context.getString(R.string.set_point) + ": " + String.valueOf(setPoint);
            holder.switch_battery_level.setText(setPointText);
        }

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(
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

        String sign = mConfigInfo != null ? mConfigInfo.getTempSign() : "C";
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
                        + String.valueOf(temperature)
                        + " " + sign;
                holder.switch_battery_level.setText(batteryLevelText);
            }

            if (holder.signal_level != null) {
                String signalText = context.getString(R.string.set_point)
                        + ": "
                        + String.valueOf(mDeviceInfo.getSetPoint()
                        + " " + sign);
                holder.signal_level.setText(signalText);
            }
        }

        if (holder.isProtected)
            holder.buttonSet.setEnabled(false);

        if ("evohome".equals(mDeviceInfo.getHardwareName())) {
            holder.buttonSet.setText(context.getString(R.string.set_temperature));
            holder.buttonSet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleSetTemperatureClick(v.getId());
                }
            });
            holder.buttonSet.setId(mDeviceInfo.getIdx());
            holder.buttonSet.setVisibility(View.VISIBLE);

            modeIconRes = getEvohomeStateIconResource(mDeviceInfo.getStatus());
        } else {
            holder.buttonSet.setVisibility(View.GONE);
            holder.pieView.setVisibility(View.VISIBLE);

            double temp = temperature;
            if (!sign.equals("C"))
                temp = temp / 2;
            holder.pieView.setPercentage(Float.valueOf(temp + ""));
            holder.pieView.setInnerText(temperature + " " + sign);

            if ((sign.equals("C") && temperature < 0) || (sign.equals("F") && temperature < 30)) {
                holder.pieView.setPercentageBackgroundColor(R.color.md_red_600);
            }

            PieAngleAnimation animation = new PieAngleAnimation(holder.pieView);
            animation.setDuration(2000);
            holder.pieView.startAnimation(animation);
        }

        if (holder.iconMode != null) {
            if (0 == modeIconRes) {
                holder.iconMode.setVisibility(View.GONE);
            } else {
                holder.iconMode.setImageResource(modeIconRes);
                holder.iconMode.setVisibility(View.VISIBLE);
            }
        }

        if ((sign.equals("C") && temperature < 0) || (sign.equals("F") && temperature < 30)) {
            Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(), mDeviceInfo.getType(), mDeviceInfo.getSubType(),
                    (mConfigInfo != null && temperature > mConfigInfo.getDegreeDaysBaseTemperature()) ? true : false,
                    true, "Freezing")).into(holder.iconRow);
        } else {
            Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(), mDeviceInfo.getType(), mDeviceInfo.getSubType(),
                    (mConfigInfo != null && temperature > mConfigInfo.getDegreeDaysBaseTemperature()) ? true : false,
                    false, null)).into(holder.iconRow);
        }
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
            text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getData());
            holder.switch_battery_level.setText(text);
        }

        if (holder.buttonOn != null) {
            if (!noButtonShown) {
                holder.buttonOn.setVisibility(View.GONE);
            } else {
                holder.buttonOn.setId(mDevicesInfo.getIdx());
                String status = String.valueOf(mDevicesInfo.getData().toLowerCase());
                if (statusOpen.contains(status)) {
                    holder.buttonOn.setText(context.getString(R.string.button_state_open));
                } else if (statusClosed.contains(status)) {
                    holder.buttonOn.setText(context.getString(R.string.button_state_closed));
                } else {
                    if (status.startsWith("off")) status = "off";
                    holder.buttonOn.setText(status.toUpperCase());
                }
                holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = (String) ((Button) v).getText();
                        if (text.equals(context.getString(R.string.button_state_on)))
                            handleOnButtonClick(v.getId(), true);
                        else
                            handleOnButtonClick(v.getId(), false);
                    }
                });
            }
        }

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDevicesInfo.getTypeImg(),
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
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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

        holder.buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String text = (String) ((Button) v).getText();
                    if (text.equals(context.getString(R.string.button_state_on)))
                        handleOnButtonClick(v.getId(), true);
                    else
                        handleOnButtonClick(v.getId(), false);
                } catch (Exception ignore) {
                }
            }
        });

        if (holder.buttonLog != null) {
            if (mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mDeviceInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                holder.buttonLog.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.buttonLog.setId(mDeviceInfo.getIdx());

            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
        }
    }

    /**
     * Set the data for blinds
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setBlindsRowData(DevicesInfo mDeviceInfo,
                                  DataObjectHolder holder) {

        String text;

        holder.isProtected = mDeviceInfo.isProtected();

        holder.switch_name.setText(mDeviceInfo.getName());

        if (holder.switch_status != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(
                    context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.switch_status.setText(text);
        }


        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    String.valueOf(mDeviceInfo.getData());
            holder.switch_battery_level.setText(text);
        }

        holder.buttonUp.setId(mDeviceInfo.getIdx());
        holder.buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (DevicesInfo e : data) {
                    if (e.getIdx() == view.getId()) {
                        if (e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED)
                            handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.ON);
                        else
                            handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.OFF);
                    }
                }
            }
        });

        holder.buttonStop.setId(mDeviceInfo.getIdx());
        holder.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (DevicesInfo e : data) {
                    if (e.getIdx() == view.getId()) {
                        handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.STOP);
                    }
                }
            }
        });

        holder.buttonDown.setId(mDeviceInfo.getIdx());
        holder.buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (DevicesInfo e : data) {
                    if (e.getIdx() == view.getId()) {
                        if (e.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED)
                            handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.OFF);
                        else
                            handleBlindsClick(e.getIdx(), DomoticzValues.Device.Blind.Action.ON);
                    }
                }
            }
        });

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                    String.valueOf(mDeviceInfo.getStatus());
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

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                    String.valueOf(mDeviceInfo.getStatus());
            holder.switch_battery_level.setText(text);
        }

        holder.switch_dimmer_level.setId(mDeviceInfo.getIdx() + ID_TEXTVIEW);
        String percentage = calculateDimPercentage(
                mDeviceInfo.getMaxDimLevel(), mDeviceInfo.getLevel());
        holder.switch_dimmer_level.setText(percentage);

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
        holder.dimmerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                handleOnOffSwitchClick(compoundButton.getId(), checked);
                mDeviceInfo.setStatusBoolean(checked);
                if (checked) {
                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                    holder.dimmer.setVisibility(View.VISIBLE);
                    if (holder.dimmer.getProgress() <= 10) {
                        holder.dimmer.setProgress(20);//dimmer turned on with default progress value
                    }
                    if (isRGB)
                        holder.buttonColor.setVisibility(View.VISIBLE);
                } else {
                    holder.switch_dimmer_level.setVisibility(View.GONE);
                    holder.dimmer.setVisibility(View.GONE);
                    if (isRGB)
                        holder.buttonColor.setVisibility(View.GONE);
                }
                if (!checked)
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);
            }
        });

        holder.dimmer.setProgress(mDeviceInfo.getLevel());
        holder.dimmer.setMax(mDeviceInfo.getMaxDimLevel());
        holder.dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String percentage = calculateDimPercentage(seekBar.getMax(), progress);
                TextView switch_dimmer_level = (TextView) seekBar.getRootView()
                        .findViewById(mDeviceInfo.getIdx() + ID_TEXTVIEW);

                if (switch_dimmer_level != null)
                    switch_dimmer_level.setText(percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previousDimmerValue = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                Switch dimmerOnOffSwitch = null;
                try {
                    dimmerOnOffSwitch = (Switch) seekBar.getRootView()
                            .findViewById(mDeviceInfo.getIdx() + ID_SWITCH);
                    if (progress == 0 && dimmerOnOffSwitch.isChecked()) {
                        dimmerOnOffSwitch.setChecked(false);
                        seekBar.setProgress(previousDimmerValue);
                    } else if (progress > 0 && !dimmerOnOffSwitch.isChecked()) {
                        dimmerOnOffSwitch.setChecked(true);
                    }
                } catch (Exception ex) {/*else we don't use a switch, but buttons */}

                handleDimmerChange(mDeviceInfo.getIdx(), progress + 1, false);
                mDeviceInfo.setLevel(progress);
            }
        });

        if (!mDeviceInfo.getStatusBoolean()) {
            holder.switch_dimmer_level.setVisibility(View.GONE);
            holder.dimmer.setVisibility(View.GONE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.GONE);
        } else {
            holder.switch_dimmer_level.setVisibility(View.VISIBLE);
            holder.dimmer.setVisibility(View.VISIBLE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.VISIBLE);
        }

        if (holder.buttonLog != null) {
            holder.buttonLog.setId(mDeviceInfo.getIdx());
            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
        }

        if (isRGB && holder.buttonColor != null) {
            holder.buttonColor.setId(mDeviceInfo.getIdx());
            holder.buttonColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorButtonClick(v.getId());
                }
            });
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

        if (holder.signal_level != null) {
            text = context.getString(R.string.last_update)
                    + ": "
                    + UsefulBits.getFormattedDate(context,
                    mDeviceInfo.getLastUpdateDateTime().getTime());
            holder.signal_level.setText(text);
        }

        if (holder.switch_battery_level != null) {
            text = context.getString(R.string.status) + ": " +
                    String.valueOf(mDeviceInfo.getStatus());
            holder.switch_battery_level.setText(text);
        }

        holder.switch_dimmer_level.setId(mDeviceInfo.getIdx() + ID_TEXTVIEW);
        String percentage = calculateDimPercentage(
                mDeviceInfo.getMaxDimLevel(), mDeviceInfo.getLevel());
        holder.switch_dimmer_level.setText(percentage);

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), true);
                    holder.iconRow.setAlpha(1f);
                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                    holder.dimmer.setVisibility(View.VISIBLE);
                    if (holder.dimmer.getProgress() <= 10) {
                        holder.dimmer.setProgress(20);//dimmer turned on with default progress value
                    }
                    if (isRGB)
                        holder.buttonColor.setVisibility(View.VISIBLE);

                }
            });
        }
        if (holder.buttonOff != null) {
            holder.buttonOff.setId(mDeviceInfo.getIdx());
            holder.buttonOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), false);

                    holder.iconRow.setAlpha(0.5f);
                    holder.switch_dimmer_level.setVisibility(View.GONE);
                    holder.dimmer.setVisibility(View.GONE);
                    if (isRGB)
                        holder.buttonColor.setVisibility(View.GONE);
                }
            });
        }

        holder.dimmer.setProgress(mDeviceInfo.getLevel());
        holder.dimmer.setMax(mDeviceInfo.getMaxDimLevel());
        holder.dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String percentage = calculateDimPercentage(seekBar.getMax(), progress);
                TextView switch_dimmer_level = (TextView) seekBar.getRootView()
                        .findViewById(mDeviceInfo.getIdx() + ID_TEXTVIEW);
                if (switch_dimmer_level != null)
                    switch_dimmer_level.setText(percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previousDimmerValue = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                handleDimmerChange(mDeviceInfo.getIdx(), progress + 1, false);
                mDeviceInfo.setLevel(progress);
            }
        });

        if (!mDeviceInfo.getStatusBoolean()) {
            holder.switch_dimmer_level.setVisibility(View.GONE);
            holder.dimmer.setVisibility(View.GONE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.GONE);
        } else {
            holder.switch_dimmer_level.setVisibility(View.VISIBLE);
            holder.dimmer.setVisibility(View.VISIBLE);
            if (isRGB)
                holder.buttonColor.setVisibility(View.VISIBLE);
        }

        if (holder.buttonLog != null) {
            holder.buttonLog.setId(mDeviceInfo.getIdx());
            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
        }

        if (isRGB && holder.buttonColor != null) {
            holder.buttonColor.setId(mDeviceInfo.getIdx());
            holder.buttonColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorButtonClick(v.getId());
                }
            });
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
            holder.buttonSetStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open state dialog
                    handleStateButtonClick(v.getId(), stateNamesArrayRes, stateIds);
                }
            });
        }

        Picasso.with(context).load(DomoticzIcons.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
     * Handles the blind click
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
        listener.onDimmerChange(idx, value, selector);
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
    private String calculateDimPercentage(int maxDimLevel, int level) {
        float percentage = ((float) level / (float) maxDimLevel) * 100;
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
        //defaults
        if (holder.switch_dimmer_level != null) {
            holder.switch_dimmer_level.setText("");
            holder.switch_dimmer_level.setVisibility(View.GONE);
        }
        if (holder.dimmerOnOffSwitch != null) {
            holder.dimmerOnOffSwitch.setVisibility(View.GONE);
        }
        if (holder.dimmer != null) {
            holder.dimmer.setVisibility(View.GONE);
        }
        if (holder.buttonColor != null) {
            holder.buttonColor.setVisibility(View.GONE);
        }
        if (holder.buttonLog != null) {
            holder.buttonLog.setVisibility(View.GONE);
        }
        if (holder.buttonTimer != null) {
            holder.buttonTimer.setVisibility(View.GONE);
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
        if (holder.spSelector != null) {
            holder.spSelector.setVisibility(View.GONE);
        }
        if (!mSharedPrefs.showExtraData()) {
            holder.signal_level.setVisibility(View.GONE);
            holder.switch_battery_level.setVisibility(View.GONE);
        }

        switch (button) {
            case Buttons.SWITCH:
                if (holder.onOffSwitch != null)
                    holder.onOffSwitch.setVisibility(View.VISIBLE);
                break;
            case Buttons.BUTTONS:
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.VISIBLE);

                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.VISIBLE);
                break;
            case Buttons.SET:
                if (holder.buttonSet != null)
                    holder.buttonSet.setVisibility(View.VISIBLE);
                break;
            case Buttons.MODAL:

                if (holder.buttonSetStatus != null)
                    holder.buttonSetStatus.setVisibility(View.VISIBLE);
                break;
            case Buttons.BUTTON_ON:
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.VISIBLE);
                break;
            case Buttons.BUTTON_OFF:
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.VISIBLE);
                break;
            case Buttons.BLINDS:
                if (holder.buttonDown != null)
                    holder.buttonDown.setVisibility(View.VISIBLE);
                if (holder.buttonUp != null)
                    holder.buttonUp.setVisibility(View.VISIBLE);
                if (holder.buttonStop != null)
                    holder.buttonStop.setVisibility(View.VISIBLE);
                break;
            case Buttons.BLINDS_NOSTOP:
                if (holder.buttonDown != null)
                    holder.buttonDown.setVisibility(View.VISIBLE);
                if (holder.buttonUp != null)
                    holder.buttonUp.setVisibility(View.VISIBLE);
                break;
            case Buttons.DIMMER_RGB:
                if (holder.buttonDown != null)
                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.dimmerOnOffSwitch.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                if (holder.buttonDown != null)
                    holder.buttonColor.setVisibility(View.VISIBLE);
                break;
            case Buttons.DIMMER:
                if (holder.switch_dimmer_level != null)
                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                if (holder.dimmerOnOffSwitch != null)
                    holder.dimmerOnOffSwitch.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                break;
            case Buttons.DIMMER_BUTTONS:
                if (holder.switch_dimmer_level != null)
                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.VISIBLE);
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.VISIBLE);
                if (holder.dimmer != null)
                    holder.dimmer.setVisibility(View.VISIBLE);
                break;
            case Buttons.SELECTOR:
                if (holder.spSelector != null)
                    holder.spSelector.setVisibility(View.VISIBLE);
                if (holder.dimmerOnOffSwitch != null)
                    holder.dimmerOnOffSwitch.setVisibility(View.GONE);
                break;
            case Buttons.SELECTOR_BUTTONS:
                if (holder.buttonOn != null)
                    holder.buttonOn.setVisibility(View.GONE);
                if (holder.buttonOff != null)
                    holder.buttonOff.setVisibility(View.GONE);
                if (holder.spSelector != null)
                    holder.spSelector.setVisibility(View.VISIBLE);
                break;
            default:
                if (!mSharedPrefs.showExtraData())
                    holder.signal_level.setVisibility(View.GONE);
                holder.switch_battery_level.setVisibility(View.VISIBLE);
                break;
        }
    }

    interface Buttons {
        int NOTHING = 0;
        int SWITCH = 1;
        int SET = 2;
        int BUTTONS = 3;
        int BLINDS = 4;
        int DIMMER = 5;
        int DIMMER_RGB = 6;
        int BUTTON_ON = 7;
        int BUTTON_OFF = 8;
        int BLINDS_NOSTOP = 9;
        int MODAL = 10;
        int DIMMER_BUTTONS = 11;
        int SELECTOR = 12;
        int SELECTOR_BUTTONS = 13;
    }

    public interface OnClickListener {
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder {

        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        ImageButton buttonUp, buttonDown, buttonStop;
        Button buttonOn, buttonLog, buttonTimer, buttonColor, buttonSetStatus, buttonSet, buttonOff;
        Boolean isProtected;
        ImageView iconRow, iconMode;
        SeekBar dimmer;
        Spinner spSelector;
        LinearLayout extraPanel;
        PieView pieView;

        public DataObjectHolder(View itemView) {
            super(itemView);

            extraPanel = (LinearLayout) itemView.findViewById(R.id.extra_panel);
            pieView = (PieView) itemView.findViewById(R.id.pieView);
            buttonOn = (Button) itemView.findViewById(R.id.on_button);
            buttonOff = (Button) itemView.findViewById(R.id.off_button);
            onOffSwitch = (Switch) itemView.findViewById(R.id.switch_button);
            signal_level = (TextView) itemView.findViewById(R.id.switch_signal_level);
            iconRow = (ImageView) itemView.findViewById(R.id.rowIcon);
            switch_name = (TextView) itemView.findViewById(R.id.switch_name);
            switch_battery_level = (TextView) itemView.findViewById(R.id.switch_battery_level);

            switch_dimmer_level = (TextView) itemView.findViewById(R.id.switch_dimmer_level);
            dimmerOnOffSwitch = (Switch) itemView.findViewById(R.id.switch_dimmer_switch);
            dimmer = (SeekBar) itemView.findViewById(R.id.switch_dimmer);
            spSelector = (Spinner) itemView.findViewById(R.id.spSelector);
            buttonColor = (Button) itemView.findViewById(R.id.color_button);
            buttonLog = (Button) itemView.findViewById(R.id.log_button);
            buttonTimer = (Button) itemView.findViewById(R.id.timer_button);
            buttonUp = (ImageButton) itemView.findViewById(R.id.switch_button_up);
            buttonStop = (ImageButton) itemView.findViewById(R.id.switch_button_stop);
            buttonDown = (ImageButton) itemView.findViewById(R.id.switch_button_down);
            buttonSet = (Button) itemView.findViewById(R.id.set_button);
            buttonSetStatus = (Button) itemView.findViewById(R.id.set_button);

            if (buttonLog != null)
                buttonLog.setVisibility(View.GONE);
            if (buttonTimer != null)
                buttonTimer.setVisibility(View.GONE);
            if (extraPanel != null)
                extraPanel.setVisibility(View.GONE);

            pieView.setVisibility(View.GONE);//default
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
                if (filterableObject.getName().toLowerCase().contains(filterString)) {
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