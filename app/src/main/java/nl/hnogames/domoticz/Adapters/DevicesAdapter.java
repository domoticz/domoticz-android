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

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class DevicesAdapter extends BaseAdapter implements Filterable {

    public static final int ID_SCENE_SWITCH = 2000;
    private static final int ID_TEXTVIEW = 1000;
    private static final int ID_SWITCH = 0;

    private static final int[] EVOHOME_STATE_IDS = {
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


    public DevicesAdapter(Context context,
                          ArrayList<DevicesInfo> data,
                          switchesClickListener listener) {
        super();

        mSharedPrefs = new SharedPrefUtil(context);

        this.context = context;
        domoticz = new Domoticz(context);

        Collections.sort(data, new Comparator<DevicesInfo>() {
            @Override
            public int compare(DevicesInfo left, DevicesInfo right) {
                return left.getName().compareTo(right.getName());
            }
        });
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

    public Filter getFilter() {
        return mFilter;
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

                        if (mDeviceInfo.getSwitchType().equals(Domoticz.Device.Type.Name.SECURITY)) {
                            if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.SECURITYPANEL))
                                row = setSecurityPanelSwitchRowId(holder);
                            else
                                row = setDefaultRowId(holder);
                        } else if (mDeviceInfo.getSwitchType().equals(Domoticz.Device.Type.Name.EVOHOME)) {
                            if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.EVOHOME))
                                row = setModalSwitchRowId(holder);
                            else
                                row = setDefaultRowId(holder);
                        } else {
                            row = setOnOffSwitchRowId(holder);
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
                        if (mDeviceInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB))
                            row = setDimmerRowId(holder, true);
                        else
                            row = setDimmerRowId(holder, false);
                        break;

                    case Domoticz.Device.Type.Value.BLINDS:
                    case Domoticz.Device.Type.Value.BLINDINVERTED:
                        if (canHandleStopButton(mDeviceInfo))
                            row = setBlindsRowId(holder);
                        else
                            row = setOnOffSwitchRowId(holder);
                        break;
                }
            }
        }
        return row;
    }

    private View setOnOffButtonRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData())
            layoutResourceId = R.layout.scene_row_group;
        else layoutResourceId = R.layout.scene_row_group_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.extrapanel = (LinearLayout) row.findViewById(R.id.extra_panel);
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

    private View setOnOffSwitchRowId(ViewHolder holder) {
        if (mSharedPrefs.showExtraData())
            layoutResourceId = R.layout.switch_row_on_off;
        else layoutResourceId = R.layout.switch_row_on_off_small;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.extrapanel = (LinearLayout) row.findViewById(R.id.extra_panel);
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
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);
        return row;
    }

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
        holder.extrapanel = (LinearLayout) convertView.findViewById(R.id.extra_panel_button);

        if (holder.extrapanel != null)
            holder.extrapanel.setVisibility(View.GONE);

        return convertView;
    }

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

        holder.extrapanel = (LinearLayout) row.findViewById(R.id.extra_panel);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);

        if (holder.extrapanel != null)
            holder.extrapanel.setVisibility(View.GONE);
        if (holder.buttonLog != null)
            holder.buttonLog.setVisibility(View.GONE);
        if (holder.buttonTimer != null)
            holder.buttonTimer.setVisibility(View.GONE);

        return row;
    }

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
                case Domoticz.Device.Type.Value.X10SIREN:
                case Domoticz.Device.Type.Value.MOTION:
                case Domoticz.Device.Type.Value.CONTACT:
                case Domoticz.Device.Type.Value.DUSKSENSOR:
                case Domoticz.Device.Type.Value.DOORLOCK:

                    if (mDeviceInfo.getSwitchType().equals(Domoticz.Device.Type.Name.SECURITY)) {
                        if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.SECURITYPANEL))
                            setSecurityPanelSwitchRowData(mDeviceInfo, holder);
                        else
                            setDefaultRowData(mDeviceInfo, holder);
                    } else if (mDeviceInfo.getSwitchType().equals(Domoticz.Device.Type.Name.EVOHOME)) {
                        if (mDeviceInfo.getSubType().equals(Domoticz.Device.SubType.Name.EVOHOME))
                            setModalSwitchRowData(mDeviceInfo, holder, R.array.evohome_states, R.array.evohome_state_names, EVOHOME_STATE_IDS);
                        else
                            setDefaultRowData(mDeviceInfo, holder);
                    } else
                        setOnOffSwitchRowData(mDeviceInfo, holder);

                    break;

                case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                case Domoticz.Device.Type.Value.DOORBELL:
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
                    if (mDeviceInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB))
                        setDimmerRowData(mDeviceInfo, holder, true);
                    else
                        setDimmerRowData(mDeviceInfo, holder, false);
                    break;

                case Domoticz.Device.Type.Value.SELECTOR:
                    setSelectorRowData(mDeviceInfo, holder);
                    break;

                case Domoticz.Device.Type.Value.BLINDS:
                case Domoticz.Device.Type.Value.BLINDINVERTED:
                    if (canHandleStopButton(mDeviceInfo))
                        setBlindsRowData(mDeviceInfo, holder);
                    else
                        setOnOffSwitchRowData(mDeviceInfo, holder);
                    break;

                default:
                    throw new NullPointerException(
                            "No supported switch type defined in the adapter (setSwitchRowData)");
            }
        }
    }

    private boolean canHandleStopButton(DevicesInfo mDeviceInfo) {
        if ((mDeviceInfo.getSubType().indexOf("RAEX") >= 0) ||
                (mDeviceInfo.getSubType().indexOf("A-OK") >= 0) ||
                (mDeviceInfo.getSubType().indexOf("RollerTrol") >= 0) ||
                (mDeviceInfo.getSubType().indexOf("RFY") >= 0) ||
                (mDeviceInfo.getSubType().indexOf("ASA") >= 0) ||
                (mDeviceInfo.getSubType().indexOf("T6 DC") >= 0))
            return true;
        else
            return false;
    }

    private void setDefaultRowData(DevicesInfo mDeviceInfo,
                                   ViewHolder holder) {
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());

        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (mDeviceInfo.getUsage() != null && mDeviceInfo.getUsage().length() > 0)
            holder.switch_battery_level.setText(context.getString(R.string.usage) + ": " + mDeviceInfo.getUsage());
        if (mDeviceInfo.getCounterToday() != null && mDeviceInfo.getCounterToday().length() > 0)
            holder.switch_battery_level.append(" " + context.getString(R.string.today) + ": " + mDeviceInfo.getCounterToday());
        if (mDeviceInfo.getCounter() != null && mDeviceInfo.getCounter().length() > 0 &&
                !mDeviceInfo.getCounter().equals(mDeviceInfo.getData()))
            holder.switch_battery_level.append(" " + context.getString(R.string.total) + ": " + mDeviceInfo.getCounter());

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


    private void setSecurityPanelSwitchRowData(DevicesInfo mDevicesInfo, ViewHolder holder) {
        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                UsefulBits.getFormattedDate(context, mDevicesInfo.getLastUpdateDateTime().getTime());

        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                String.valueOf(mDevicesInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDevicesInfo.getIdx());
            if (mDevicesInfo.getData().startsWith("Arm"))
                holder.buttonOn.setText(context.getString(R.string.button_disarm));
            else
                holder.buttonOn.setText(context.getString(R.string.button_arm));

            holder.buttonOn.setBackground(context.getResources().getDrawable(R.drawable.button));
            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open security panel
                    handleSecurityPanel(v.getId());
                }
            });
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

    private void setOnOffButtonRowData(final DevicesInfo mDeviceInfo,
                                       final ViewHolder holder) {

        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
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

        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDeviceInfo.getIdx());
            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleOnOffSwitchClick(v.getId(), true);
                }
            });
        }
        if (holder.buttonOff != null) {
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
                holder.buttonLog.setId(mDeviceInfo.getIdx() + this.ID_SCENE_SWITCH);
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

    private void setOnOffSwitchRowData(final DevicesInfo mDeviceInfo,
                                       final ViewHolder holder) {

        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
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

        if (holder.onOffSwitch != null) {
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
                holder.onOffSwitch.setId(mDeviceInfo.getIdx() + this.ID_SCENE_SWITCH);
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
                holder.buttonLog.setId(mDeviceInfo.getIdx() + this.ID_SCENE_SWITCH);
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
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime()));

        if (holder.signal_level != null)
            holder.signal_level.setText(context.getString(R.string.set_point) + ": " + String.valueOf(setPoint));

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(), mDeviceInfo.getType(), mDeviceInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    private void setTemperatureRowData(DevicesInfo mDeviceInfo, ViewHolder holder) {
        final double temperature = mDeviceInfo.getTemperature();
        final double setPoint = mDeviceInfo.getSetPoint();
        int modeIconRes = 0;
        holder.isProtected = mDeviceInfo.isProtected();

        holder.switch_name.setText(mDeviceInfo.getName());

        if (Double.isNaN(temperature) || Double.isNaN(setPoint)) {
            if (holder.signal_level != null)
                holder.signal_level.setVisibility(View.GONE);

            if (holder.switch_battery_level != null)
                holder.switch_battery_level.setText(context.getString(R.string.temperature) + ": " + mDeviceInfo.getData());
        } else {
            if (holder.signal_level != null)
                holder.signal_level.setVisibility(View.VISIBLE);

            if (holder.switch_battery_level != null)
                holder.switch_battery_level.setText(context.getString(R.string.temperature) + ": " + String.valueOf(temperature) + " C");

            if (holder.signal_level != null)
                holder.signal_level.setText(context.getString(R.string.set_point) + ": " + String.valueOf(mDeviceInfo.getSetPoint() + " C"));
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

            modeIconRes = getEvohomeStateIcon(mDeviceInfo.getStatus());
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

    public void handleThermostatClick(int idx) {
        listener.onThermostatClick(idx);
    }

    public void handleSetTemperatureClick(int idx) {
        listener.onSetTemperatureClick(idx);
    }

    private void setPushOnOffSwitchRowData(DevicesInfo mDeviceInfo, ViewHolder holder, boolean action) {
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
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
            holder.buttonOn.setId(mDeviceInfo.getIdx() + this.ID_SCENE_SWITCH);
        else
            holder.buttonOn.setId(mDeviceInfo.getIdx());

        if (action) {
            holder.buttonOn.setText(context.getString(R.string.button_state_on));
            holder.buttonOn.setBackground(context.getResources().getDrawable(R.drawable.button));
        } else {
            holder.buttonOn.setText(context.getString(R.string.button_state_off));
            holder.buttonOn.setBackground(context.getResources().getDrawable(R.drawable.button_off));
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
                } catch (Exception ex) {
                }
            }
        });

        if (holder.buttonLog != null) {
            if (mDeviceInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mDeviceInfo.getType().equals(Domoticz.Scene.Type.SCENE))
                holder.buttonLog.setId(mDeviceInfo.getIdx() + this.ID_SCENE_SWITCH);
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

    private void setBlindsRowData(DevicesInfo mDeviceInfo,
                                  ViewHolder holder) {
        holder.isProtected = mDeviceInfo.isProtected();

        holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
        if (holder.switch_status != null)
            holder.switch_status.setText(text);

        text = context.getString(R.string.status) + ": " +
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

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

    private void setSelectorRowData(final DevicesInfo mDevicesInfo,
                                    final ViewHolder holder) {
        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                UsefulBits.getFormattedDate(context, mDevicesInfo.getLastUpdateDateTime().getTime());
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                String.valueOf(mDevicesInfo.getStatus());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        int loadLevel = mDevicesInfo.getLevel() / 10;
        final String[] levelNames = mDevicesInfo.getLevelNames();
        String statusText = context.getString(R.string.unknown);
        if (levelNames.length >= loadLevel)
            statusText = levelNames[loadLevel];

        holder.switch_dimmer_level.setId(mDevicesInfo.getIdx() + ID_TEXTVIEW);
        holder.switch_dimmer_level.setText(statusText);
        holder.dimmerOnOffSwitch.setId(mDevicesInfo.getIdx() + ID_SWITCH);
        holder.dimmerOnOffSwitch.setChecked(mDevicesInfo.getStatusBoolean());
        holder.dimmerOnOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                handleOnOffSwitchClick(compoundButton.getId(), checked);
                mDevicesInfo.setStatusBoolean(checked);
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

        holder.dimmer.incrementProgressBy(1);
        holder.dimmer.setProgress(loadLevel);
        holder.dimmer.setMax(levelNames.length - 1);

        holder.dimmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSelectorDimmerClick(mDevicesInfo.getIdx(), levelNames);
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
                handleSelectorDimmerClick(mDevicesInfo.getIdx(), levelNames);
            }
        });

        if (!mDevicesInfo.getStatusBoolean()) {
            holder.switch_dimmer_level.setVisibility(View.GONE);
            holder.dimmer.setVisibility(View.GONE);
        } else {
            holder.switch_dimmer_level.setVisibility(View.VISIBLE);
            holder.dimmer.setVisibility(View.VISIBLE);
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

    private void setDimmerRowData(final DevicesInfo mDeviceInfo,
                                  final ViewHolder holder,
                                  final boolean isRGB) {

        holder.isProtected = mDeviceInfo.isProtected();

        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " + UsefulBits.getFormattedDate(context, mDeviceInfo.getLastUpdateDateTime().getTime());
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                String.valueOf(mDeviceInfo.getStatus());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

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

    private void setModalSwitchRowData(DevicesInfo mDevicesInfo, ViewHolder holder, final int stateArrayRes, final int stateNamesArrayRes, final int[] stateIds) {
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                UsefulBits.getFormattedDate(context, mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " +
                getStatus(stateArrayRes, stateNamesArrayRes, mDevicesInfo.getStatus());
        holder.switch_battery_level.setText(text);

        holder.buttonSetStatus.setId(mDevicesInfo.getIdx());
        holder.buttonSetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open state dialog
                handleStateButtonClick(v.getId(), stateNamesArrayRes, stateIds);
            }
        });

        if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.INVISIBLE);

        Picasso.with(context).load(domoticz.getDrawableIcon(mDevicesInfo.getTypeImg(),
                mDevicesInfo.getType(),
                mDevicesInfo.getSwitchType(),
                mDevicesInfo.getStatusBoolean(),
                mDevicesInfo.getUseCustomImage(),
                mDevicesInfo.getImage())).into(holder.iconRow);
    }

    private String getStatus(int statusArrayRes, int statusNamesArrayRes, String text) {
        Resources res = context.getResources();
        String[] states = res.getStringArray(statusArrayRes);
        String[] stateNames = res.getStringArray(statusNamesArrayRes);

        int length = states.length;
        for (int i = 0; i < length; i++) {
            if (states[i].equals(text)) {
                return stateNames[i];
            }
        }
        return text;
    }

    private void handleColorButtonClick(int idx) {
        listener.onColorButtonClick(idx);
    }

    private String calculateDimPercentage(int maxDimLevel, int level) {
        float percentage = ((float) level / (float) maxDimLevel) * 100;
        return String.format("%.0f", percentage) + "%";
    }

    private void handleOnOffSwitchClick(int idx, boolean action) {
        listener.onSwitchClick(idx, action);
    }

    private void handleSecurityPanel(int idx) {
        listener.onSecurityPanelButtonClick(idx);
    }

    private void handleOnButtonClick(int idx, boolean action) {
        listener.onButtonClick(idx, action);
    }

    private void handleBlindsClick(int idx, int action) {
        listener.onBlindClick(idx, action);
    }

    private void handleDimmerChange(final int idx, final int value, boolean selector) {
        listener.onDimmerChange(idx, value, selector);
    }

    private void handleStateButtonClick(final int idx, int itemsRes, int[] itemIds) {
        listener.onStateButtonClick(idx, itemsRes, itemIds);
    }

    private void handleSelectorDimmerClick(int idx, String[] levelNames) {
        listener.onSelectorDimmerClick(idx, levelNames);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    public int getEvohomeStateIcon(String stateName) {
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

    static class ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        ImageButton buttonUp, buttonDown, buttonStop;
        Button buttonOn, buttonLog, buttonTimer, buttonColor, buttonSetStatus, buttonSet, buttonOff;
        Boolean isProtected;
        ImageView iconRow, iconMode;
        SeekBar dimmer;
        LinearLayout extrapanel;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final ArrayList<DevicesInfo> list = data;

            int count = list.size();
            final ArrayList<DevicesInfo> nlist = new ArrayList<DevicesInfo>(count);

            DevicesInfo filterableObject;
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
            filteredData = (ArrayList<DevicesInfo>) results.values;
            notifyDataSetChanged();
        }
    }
}
