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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.hnogames.domoticz.Containers.ConfigInfo;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.ServerUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class DevicesAdapter extends BaseAdapter implements Filterable {

    public static final int ID_SCENE_SWITCH = 2000;
    private final int ID_TEXTVIEW = 1000;
    private final int ID_SWITCH = 0;

    private final int[] EVOHOME_STATE_IDS = {
            Domoticz.Device.ModalSwitch.Action.AUTO,
            Domoticz.Device.ModalSwitch.Action.ECONOMY,
            Domoticz.Device.ModalSwitch.Action.AWAY,
            Domoticz.Device.ModalSwitch.Action.AWAY,
            Domoticz.Device.ModalSwitch.Action.CUSTOM,
            Domoticz.Device.ModalSwitch.Action.HEATING_OFF
    };

    public ArrayList<DevicesInfo> filteredData = null;
    private Domoticz domoticz;
    private Context context;
    private ArrayList<DevicesInfo> data = null;
    private switchesClickListener listener;
    private int layoutResourceId;
    private int previousDimmerValue;
    private ItemFilter mFilter = new ItemFilter();
    private SharedPrefUtil mSharedPrefs;
    private ConfigInfo mConfigInfo;

    public DevicesAdapter(Context context,
                          ServerUtil serverUtil,
                          ArrayList<DevicesInfo> data,
                          switchesClickListener listener) {
        super();

        mSharedPrefs = new SharedPrefUtil(context);

        this.context = context;
        domoticz = new Domoticz(context, serverUtil);

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

        mConfigInfo = serverUtil.getActiveServer().getConfigInfo();
        this.filteredData = data;
        this.data = data;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        DevicesInfo extendedStatusInfo = filteredData.get(position);

        holder = new ViewHolder();
        convertView = setSwitchRowId(extendedStatusInfo, holder);
        convertView.setTag(holder);

        setSwitchRowData(extendedStatusInfo, holder);

        return convertView;
    }

    /**
     * Get's the filter
     *
     * @return Returns the filter
     */
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * Sets the right layout ID's based on the info of the device
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     * @return Returns the right view
     */
    private View setSwitchRowId(DevicesInfo mDeviceInfo, ViewHolder holder) {
        View row = setDefaultRowId(holder);
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            switch (mDeviceInfo.getType()) {
                case Domoticz.Scene.Type.GROUP:
                    row = setOnOffButtonRowId(holder);
                    break;
                case Domoticz.Scene.Type.SCENE:
                    row = setPushOnOffSwitchRowId(holder);
                    break;
                case Domoticz.UTILITIES_TYPE_THERMOSTAT:
                    row = setThermostatRowId(holder);
                    break;
                case Domoticz.UTILITIES_TYPE_HEATING:
                    row = setTemperatureRowId(holder);
                    break;
                default:
                    row = setDefaultRowId(holder);
                    break;
            }
        } else {
            if ((mDeviceInfo.getSwitchType() == null)) {
                row = setDefaultRowId(holder);
            } else {
                switch (mDeviceInfo.getSwitchTypeVal()) {
                    case Domoticz.Device.Type.Value.ON_OFF:
                    case Domoticz.Device.Type.Value.MEDIAPLAYER:
                    case Domoticz.Device.Type.Value.X10SIREN:
                    case Domoticz.Device.Type.Value.DOORLOCK:

                        switch (mDeviceInfo.getSwitchType()) {
                            case Domoticz.Device.Type.Name.SECURITY:
                                if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.SECURITYPANEL))
                                    row = setSecurityPanelSwitchRowId(holder);
                                else
                                    row = setDefaultRowId(holder);
                                break;
                            case Domoticz.Device.Type.Name.EVOHOME:
                                if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.EVOHOME))
                                    row = setModalSwitchRowId(holder);
                                else
                                    row = setDefaultRowId(holder);
                                break;
                            default:
                                if (mSharedPrefs.showSwitchesAsButtons())
                                    row = setOnOffButtonRowId(holder);
                                else {
                                    if (mSharedPrefs.showSwitchesAsButtons())
                                        row = setOnOffButtonRowId(holder);
                                    else
                                        row = setOnOffSwitchRowId(holder);
                                }
                                break;
                        }
                        break;

                    case Domoticz.Device.Type.Value.MOTION:
                    case Domoticz.Device.Type.Value.CONTACT:
                    case Domoticz.Device.Type.Value.DUSKSENSOR:
                        row = setMotionSwitchRowId(holder);
                        break;

                    case Domoticz.Device.Type.Value.BLINDVENETIAN:
                        row = setBlindsRowId(holder);
                        break;

                    case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                    case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                    case Domoticz.Device.Type.Value.DOORBELL:
                    case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                        row = setPushOnOffSwitchRowId(holder);
                        break;

                    case Domoticz.Device.Type.Value.DIMMER:
                    case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
                    case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                    case Domoticz.Device.Type.Value.SELECTOR:
                        if (mDeviceInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB)) {
                            if (mSharedPrefs.showSwitchesAsButtons())
                                row = setDimmerOnOffButtonRowId(holder, true);
                            else
                                row = setDimmerRowId(holder, true);
                        } else {
                            if (mSharedPrefs.showSwitchesAsButtons())
                                row = setDimmerOnOffButtonRowId(holder, false);
                            else
                                row = setDimmerRowId(holder, false);
                        }
                        break;

                    case Domoticz.Device.Type.Value.BLINDS:
                    case Domoticz.Device.Type.Value.BLINDINVERTED:
                        if (canHandleStopButton(mDeviceInfo))
                            row = setBlindsRowId(holder);
                        else {
                            if (mSharedPrefs.showSwitchesAsButtons())
                                row = setOnOffButtonRowId(holder);
                            else
                                row = setOnOffSwitchRowId(holder);
                        }
                        break;
                }
            }
        }

        if (mSharedPrefs.darkThemeEnabled()) {
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

        return row;
    }

    /**
     * Sets the layout ID's on/off button
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setOnOffButtonRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData())
            layoutResourceId = R.layout.scene_row_group;
        else layoutResourceId = R.layout.scene_row_group_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.extraPanel = (LinearLayout) row.findViewById(R.id.extra_panel);
        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonOff = (Button) row.findViewById(R.id.off_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Sets the layout ID's for a on/off switch
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setOnOffSwitchRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData())
            layoutResourceId = R.layout.switch_row_on_off;
        else layoutResourceId = R.layout.switch_row_on_off_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.extraPanel = (LinearLayout) row.findViewById(R.id.extra_panel);
        holder.onOffSwitch = (Switch) row.findViewById(R.id.switch_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Sets the layout ID's for the security panel
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setSecurityPanelSwitchRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_securitypanel;
        else layoutResourceId = R.layout.switch_row_securitypanel_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Sets the layout ID's for a motion type device
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setMotionSwitchRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_motion;
        else layoutResourceId = R.layout.switch_row_motion_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.onOffSwitch = (Switch) row.findViewById(R.id.switch_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);
        return row;
    }

    /**
     * Sets the layout for default devices
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setDefaultRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.dashboard_row_default;
        else layoutResourceId = R.layout.dashboard_row_default_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);

        return row;
    }

    /**
     * Sets the layout ID's for push on/push off switch
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setPushOnOffSwitchRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_pushon;
        else layoutResourceId = R.layout.switch_row_pushon_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Sets the layout ID's for a thermostat device
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setThermostatRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.utilities_row_thermostat;
        else layoutResourceId = R.layout.utilities_row_thermostat_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View convertView = inflater.inflate(layoutResourceId, null);

        holder.switch_name = (TextView) convertView.findViewById(R.id.thermostat_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);

        holder.switch_battery_level = (TextView) convertView.findViewById(R.id.thermostat_lastSeen);
        holder.signal_level = (TextView) convertView.findViewById(R.id.thermostat_set_point);
        holder.buttonOn = (Button) convertView.findViewById(R.id.on_button);
        return convertView;
    }

    /**
     * Sets the layout ID's for a temperature device
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setTemperatureRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.temperature_row_default;
        else layoutResourceId = R.layout.temperature_row_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View convertView = inflater.inflate(layoutResourceId, null);

        holder.switch_name = (TextView) convertView.findViewById(R.id.temperature_name);
        holder.iconRow = (ImageView) convertView.findViewById(R.id.rowIcon);
        holder.iconMode = (ImageView) convertView.findViewById(R.id.mode_icon);

        holder.switch_battery_level = (TextView) convertView.findViewById(R.id.temperature_data);
        holder.signal_level = (TextView) convertView.findViewById(R.id.temperature_data2);
        holder.buttonSet = (Button) convertView.findViewById(R.id.set_button);

        holder.extraPanel = (LinearLayout) convertView.findViewById(R.id.extra_panel);
        if (holder.extraPanel != null)
            holder.extraPanel.setVisibility(View.GONE);

        return convertView;
    }

    /**
     * Sets the layout ID's for blinds
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setBlindsRowId(ViewHolder holder) {

        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_blinds;
        else layoutResourceId = R.layout.switch_row_blinds_small;


        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_status = (TextView) row.findViewById(R.id.switch_status);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.buttonUp = (ImageButton) row.findViewById(R.id.switch_button_up);
        holder.buttonStop = (ImageButton) row.findViewById(R.id.switch_button_stop);
        holder.buttonDown = (ImageButton) row.findViewById(R.id.switch_button_down);

        return row;
    }

    /**
     * Sets the layout ID's for the dimmer
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setDimmerRowId(ViewHolder holder, boolean isRGB) {
        if (isRGB) {
            if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_rgb_dimmer;
            else layoutResourceId = R.layout.switch_row_rgb_dimmer_small;
        } else {
            if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_dimmer;
            else layoutResourceId = R.layout.switch_row_dimmer_small;
        }

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.switch_dimmer_level = (TextView) row.findViewById(R.id.switch_dimmer_level);
        holder.dimmerOnOffSwitch = (Switch) row.findViewById(R.id.switch_dimmer_switch);
        holder.dimmer = (SeekBar) row.findViewById(R.id.switch_dimmer);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (isRGB)
            holder.buttonColor = (Button) row.findViewById(R.id.color_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Sets the layout ID's for the dimmer with the individual buttons for on/off
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setDimmerOnOffButtonRowId(ViewHolder holder, boolean isRGB) {
        if (isRGB) {
            if (mSharedPrefs.showExtraData())
                layoutResourceId = R.layout.switch_row_rgb_dimmer_button;
            else layoutResourceId = R.layout.switch_row_rgb_dimmer_button_small;
        } else {
            if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_dimmer_button;
            else layoutResourceId = R.layout.switch_row_dimmer_button_small;
        }

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonOff = (Button) row.findViewById(R.id.off_button);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.switch_dimmer_level = (TextView) row.findViewById(R.id.switch_dimmer_level);
        holder.dimmer = (SeekBar) row.findViewById(R.id.switch_dimmer);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (isRGB)
            holder.buttonColor = (Button) row.findViewById(R.id.color_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Sets the layout ID's for the modal switch
     *
     * @param holder Holder to use
     * @return Returns a view
     */
    private View setModalSwitchRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData()) layoutResourceId = R.layout.switch_row_modal;
        else layoutResourceId = R.layout.switch_row_modal_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);

        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.buttonSetStatus = (Button) row.findViewById(R.id.set_status_button);

        holder.extraPanel = (LinearLayout) row.findViewById(R.id.extra_panel);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.extraPanel != null)
            holder.extraPanel.setVisibility(View.GONE);
        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

    /**
     * Set the data for switches
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setSwitchRowData(DevicesInfo mDeviceInfo,
                                  ViewHolder holder) {
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            switch (mDeviceInfo.getType()) {
                case Domoticz.Scene.Type.GROUP:
                    setOnOffButtonRowData(mDeviceInfo, holder);
                    break;
                case Domoticz.Scene.Type.SCENE:
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, true);
                    break;
                case Domoticz.UTILITIES_TYPE_THERMOSTAT:
                    setThermostatRowData(mDeviceInfo, holder);
                    break;
                case Domoticz.UTILITIES_TYPE_HEATING:
                    setTemperatureRowData(mDeviceInfo, holder);
                    break;
                default:
                    setDefaultRowData(mDeviceInfo, holder);
                    break;
            }
        } else if ((mDeviceInfo.getSwitchType() == null)) {
            setDefaultRowData(mDeviceInfo, holder);
        } else {
            switch (mDeviceInfo.getSwitchTypeVal()) {
                case Domoticz.Device.Type.Value.ON_OFF:
                case Domoticz.Device.Type.Value.MEDIAPLAYER:
                case Domoticz.Device.Type.Value.DOORLOCK:
                    switch (mDeviceInfo.getSwitchType()) {
                        case Domoticz.Device.Type.Name.SECURITY:
                            if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.SECURITYPANEL))
                                setSecurityPanelSwitchRowData(mDeviceInfo, holder);
                            else
                                setDefaultRowData(mDeviceInfo, holder);
                            break;
                        case Domoticz.Device.Type.Name.EVOHOME:
                            if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.EVOHOME))
                                setModalSwitchRowData(mDeviceInfo, holder, R.array.evohome_states, R.array.evohome_state_names, EVOHOME_STATE_IDS);
                            else
                                setDefaultRowData(mDeviceInfo, holder);
                            break;
                        default:
                            if (mSharedPrefs.showSwitchesAsButtons())
                                setOnOffButtonRowData(mDeviceInfo, holder);
                            else
                                setOnOffSwitchRowData(mDeviceInfo, holder);
                            break;
                    }
                    break;

                case Domoticz.Device.Type.Value.X10SIREN:
                case Domoticz.Device.Type.Value.MOTION:
                case Domoticz.Device.Type.Value.CONTACT:
                case Domoticz.Device.Type.Value.DUSKSENSOR:
                case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                case Domoticz.Device.Type.Value.DOORBELL:
                    setContactSwitchRowData(mDeviceInfo, holder, false);
                    break;
                case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, true);
                    break;

                case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, false);
                    break;

                case Domoticz.Device.Type.Value.BLINDVENETIAN:
                    setBlindsRowData(mDeviceInfo, holder);
                    break;

                case Domoticz.Device.Type.Value.DIMMER:
                case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
                case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                    if (mDeviceInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB)) {
                        if (mSharedPrefs.showSwitchesAsButtons())
                            setDimmerOnOffButtonRowData(mDeviceInfo, holder, true);
                        else
                            setDimmerRowData(mDeviceInfo, holder, true);
                    } else {
                        if (mSharedPrefs.showSwitchesAsButtons())
                            setDimmerOnOffButtonRowData(mDeviceInfo, holder, false);
                        else
                            setDimmerRowData(mDeviceInfo, holder, false);
                    }
                    break;

                case Domoticz.Device.Type.Value.SELECTOR:
                    setSelectorRowData(mDeviceInfo, holder);
                    break;

                case Domoticz.Device.Type.Value.BLINDS:
                case Domoticz.Device.Type.Value.BLINDINVERTED:
                    if (canHandleStopButton(mDeviceInfo))
                        setBlindsRowData(mDeviceInfo, holder);
                    else {
                        if (mSharedPrefs.showSwitchesAsButtons())
                            setOnOffButtonRowData(mDeviceInfo, holder);
                        else
                            setOnOffSwitchRowData(mDeviceInfo, holder);
                    }
                    break;

                default:
                    throw new NullPointerException(
                            "No supported switch type defined in the adapter (setSwitchRowData)");
            }
        }
    }

    /**
     * Checks if the device has a stop button
     *
     * @param mDeviceInfo Device to check
     * @return Returns true if the device has a stop button
     */
    private boolean canHandleStopButton(DevicesInfo mDeviceInfo) {
        //noinspection SpellCheckingInspection
        return (mDeviceInfo.getSubType().contains("RAEX")) ||
                (mDeviceInfo.getSubType().contains("A-OK")) ||
                (mDeviceInfo.getSubType().contains("RollerTrol")) ||
                (mDeviceInfo.getSubType().contains("RFY")) ||
                (mDeviceInfo.getSubType().contains("ASA")) ||
                (mDeviceInfo.getSubType().contains("T6 DC"));
    }

    /**
     * Sets the data for a default device
     *
     * @param mDeviceInfo Device info
     * @param holder      Holder to use
     */
    private void setDefaultRowData(DevicesInfo mDeviceInfo,
                                   ViewHolder holder) {

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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
    private void setSecurityPanelSwitchRowData(DevicesInfo mDeviceInfo, ViewHolder holder) {
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                                       final ViewHolder holder) {
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (holder.buttonOn != null) {
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
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
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
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
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
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
                                       final ViewHolder holder) {
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
                holder.onOffSwitch.setId(mDeviceInfo.getIdx() + ID_SCENE_SWITCH);
            else
                holder.onOffSwitch.setId(mDeviceInfo.getIdx());

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
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
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
    private void setThermostatRowData(DevicesInfo mDeviceInfo, ViewHolder holder) {
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

        Picasso.with(context).load(domoticz.getDrawableIcon(
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
    private void setTemperatureRowData(DevicesInfo mDeviceInfo, ViewHolder holder) {
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
                        + String.valueOf(temperature)
                        + " C";
                holder.switch_battery_level.setText(batteryLevelText);
            }

            if (holder.signal_level != null) {
                String signalText = context.getString(R.string.set_point)
                        + ": "
                        + String.valueOf(mDeviceInfo.getSetPoint()
                        + " C");
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
        }

        if (holder.iconMode != null) {
            if (0 == modeIconRes) {
                holder.iconMode.setVisibility(View.GONE);
            } else {
                holder.iconMode.setImageResource(modeIconRes);
                holder.iconMode.setVisibility(View.VISIBLE);
            }
        }

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(), mDeviceInfo.getType(), mDeviceInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    /**
     * Set the data for the contact switch
     *
     * @param mDevicesInfo  Device info class
     * @param holder        Holder to use
     * @param noButtonShown Should the button be shown?
     */
    private void setContactSwitchRowData(DevicesInfo mDevicesInfo,
                                         ViewHolder holder,
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDevicesInfo.getTypeImg(),
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
    private void setPushOnOffSwitchRowData(DevicesInfo mDeviceInfo, ViewHolder holder, boolean action) {
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
                mDeviceInfo.getType(),
                mDeviceInfo.getSubType(),
                mDeviceInfo.getStatusBoolean(),
                mDeviceInfo.getUseCustomImage(),
                mDeviceInfo.getImage())).into(holder.iconRow);

        if (!mDeviceInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

        if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
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
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
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
                                  ViewHolder holder) {

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
                for (DevicesInfo e : filteredData) {
                    if (e.getIdx() == view.getId()) {
                        if (e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDVENETIAN)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.OFF);
                        else if (e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.DOWN);
                        else
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.UP);
                    }
                }
            }
        });

        holder.buttonStop.setId(mDeviceInfo.getIdx());
        holder.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (DevicesInfo e : filteredData) {
                    if (e.getIdx() == view.getId()) {
                        handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.STOP);
                    }
                }
            }
        });

        holder.buttonDown.setId(mDeviceInfo.getIdx());
        holder.buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (DevicesInfo e : filteredData) {
                    if (e.getIdx() == view.getId()) {
                        if (e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDVENETIAN)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.ON);
                        else if (e.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED)
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.UP);
                        else
                            handleBlindsClick(e.getIdx(), Domoticz.Device.Blind.Action.DOWN);
                    }
                }
            }
        });

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                                    final ViewHolder holder) {
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

        int loadLevel = mDeviceInfo.getLevel() / 10;
        final String[] levelNames = mDeviceInfo.getLevelNames();
        String statusText = context.getString(R.string.unknown);

        if (levelNames.length > loadLevel)
            statusText = levelNames[loadLevel];

        holder.switch_dimmer_level.setId(mDeviceInfo.getIdx() + ID_TEXTVIEW);
        holder.switch_dimmer_level.setText(statusText);

        if (holder.dimmerOnOffSwitch != null) {
            holder.dimmerOnOffSwitch.setId(mDeviceInfo.getIdx() + ID_SWITCH);
            holder.dimmerOnOffSwitch.setChecked(mDeviceInfo.getStatusBoolean());
            holder.dimmerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    handleOnOffSwitchClick(compoundButton.getId(), checked);
                    mDeviceInfo.setStatusBoolean(checked);
                    if (checked) {
                        holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                        holder.dimmer.setVisibility(View.VISIBLE);
                        holder.dimmer.setProgress(0);
                    } else {
                        holder.switch_dimmer_level.setVisibility(View.GONE);
                        holder.dimmer.setVisibility(View.GONE);
                    }
                    if (!checked)
                        holder.iconRow.setAlpha(0.5f);
                    else
                        holder.iconRow.setAlpha(1f);
                }
            });
        }
        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDeviceInfo.getIdx());
            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), true);

                    holder.switch_dimmer_level.setVisibility(View.VISIBLE);
                    holder.dimmer.setVisibility(View.VISIBLE);
                    holder.dimmer.setProgress(0);
                    holder.iconRow.setAlpha(1f);
                }
            });
        }
        if (holder.buttonOff != null) {
            holder.buttonOff.setId(mDeviceInfo.getIdx());
            holder.buttonOff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), false);

                    holder.switch_dimmer_level.setVisibility(View.GONE);
                    holder.dimmer.setVisibility(View.GONE);
                    holder.iconRow.setAlpha(0.5f);
                }
            });
        }

        holder.dimmer.incrementProgressBy(1);
        holder.dimmer.setProgress(loadLevel);
        holder.dimmer.setMax(levelNames.length - 1);

        holder.dimmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSelectorDimmerClick(mDeviceInfo.getIdx(), levelNames);
            }
        });

        holder.dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handleSelectorDimmerClick(mDeviceInfo.getIdx(), levelNames);
            }
        });

        if (!mDeviceInfo.getStatusBoolean()) {
            holder.switch_dimmer_level.setVisibility(View.GONE);
            holder.dimmer.setVisibility(View.GONE);
        } else {
            holder.switch_dimmer_level.setVisibility(View.VISIBLE);
            holder.dimmer.setVisibility(View.VISIBLE);
        }

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                                  final ViewHolder holder,
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                switch_dimmer_level.setText(percentage);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previousDimmerValue = seekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                Switch dimmerOnOffSwitch = (Switch) seekBar.getRootView()
                        .findViewById(mDeviceInfo.getIdx() + ID_SWITCH);

                if (progress == 0 && dimmerOnOffSwitch.isChecked()) {
                    dimmerOnOffSwitch.setChecked(false);
                    seekBar.setProgress(previousDimmerValue);
                } else if (progress > 0 && !dimmerOnOffSwitch.isChecked())
                    dimmerOnOffSwitch.setChecked(true);
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
                                             final ViewHolder holder,
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
                                       ViewHolder holder,
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

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(),
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
     *
     * @param idx        IDX of the device to change
     * @param levelNames Names om the levels
     */
    private void handleSelectorDimmerClick(int idx, String[] levelNames) {
        listener.onSelectorDimmerClick(idx, levelNames);
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

    /**
     * View holder for caching resource ID's
     */
    static class ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        ImageButton buttonUp, buttonDown, buttonStop;
        Button buttonOn, buttonLog, buttonTimer, buttonColor, buttonSetStatus, buttonSet, buttonOff;
        Boolean isProtected;
        ImageView iconRow, iconMode;
        SeekBar dimmer;
        LinearLayout extraPanel;
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