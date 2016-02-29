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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
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

public class SwitchesAdapter extends BaseAdapter implements Filterable {

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
    Domoticz domoticz;
    private SharedPrefUtil mSharedPrefs;
    private Context context;
    private ArrayList<DevicesInfo> data = null;
    private switchesClickListener listener;
    private int layoutResourceId;
    private int previousDimmerValue;
    private ItemFilter mFilter = new ItemFilter();

    public SwitchesAdapter(Context context,
                           Domoticz mDomoticz,
                           ArrayList<DevicesInfo> data,
                           switchesClickListener listener) {
        super();
        this.context = context;
        domoticz = mDomoticz;
        mSharedPrefs = new SharedPrefUtil(context);

        Collections.sort(data, new Comparator<DevicesInfo>() {
            @Override
            public int compare(DevicesInfo left, DevicesInfo right) {
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
        ViewHolder holder;
        DevicesInfo DevicesInfo = filteredData.get(position);

        //if (convertView == null) {
        holder = new ViewHolder();
        convertView = setSwitchRowId(DevicesInfo, holder);
        convertView.setTag(holder);
        //} else holder = (ViewHolder) convertView.getTag();

        setSwitchRowData(DevicesInfo, holder);

        return convertView;
    }

    private View setSwitchRowId(DevicesInfo mDevicesInfo, ViewHolder holder) {
        View row;

        switch (mDevicesInfo.getSwitchTypeVal()) {
            case Domoticz.Device.Type.Value.ON_OFF:
            case Domoticz.Device.Type.Value.MEDIAPLAYER:
            case Domoticz.Device.Type.Value.X10SIREN:
            case Domoticz.Device.Type.Value.DOORLOCK:
                switch (mDevicesInfo.getSwitchType()) {
                    case Domoticz.Device.Type.Name.SECURITY:
                        if (mDevicesInfo.getSubType().equals(Domoticz.Device.SubType.Name.SECURITYPANEL))
                            row = setSecurityPanelSwitchRowId(holder);
                        else
                            row = setDefaultRowId(holder);
                        break;
                    case Domoticz.Device.Type.Name.EVOHOME:
                        if (mDevicesInfo.getSubType().equals(Domoticz.Device.SubType.Name.EVOHOME))
                            row = setModalSwitchRowId(holder);
                        else
                            row = setDefaultRowId(holder);
                        break;
                    default:
                        row = setOnOffSwitchRowId(holder);
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
                row = setPushOnOffSwitchRowId(holder, true);
                break;
            case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                row = setPushOnOffSwitchRowId(holder, true);
                break;
            case Domoticz.Device.Type.Value.DOORBELL:
                row = setPushOnOffSwitchRowId(holder, true);
                break;
            case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                row = setPushOnOffSwitchRowId(holder, true);
                break;

            case Domoticz.Device.Type.Value.DIMMER:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
            case Domoticz.Device.Type.Value.SELECTOR:
                if (mDevicesInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB))
                    row = setDimmerRowId(holder, true);
                else
                    row = setDimmerRowId(holder, false);
                break;

            case Domoticz.Device.Type.Value.BLINDS:
            case Domoticz.Device.Type.Value.BLINDINVERTED:
                if (canHandleStopButton(mDevicesInfo))
                    row = setBlindsRowId(holder);
                else
                    row = setOnOffSwitchRowId(holder);
                break;

            default:
                throw new NullPointerException(
                        "Switch type not supported in the adapter for: \n"
                                + mDevicesInfo.toString());
        }

        if (mSharedPrefs.darkThemeEnabled()) {
            (row.findViewById(R.id.row_wrapper)).setBackground(context.getResources().getDrawable(R.drawable.bordershadowdark));
            (row.findViewById(R.id.row_global_wrapper)).setBackgroundColor(context.getResources().getColor(R.color.background_dark));

            if ((row.findViewById(R.id.day_button)) != null)
                (row.findViewById(R.id.day_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
            if ((row.findViewById(R.id.year_button)) != null)
                (row.findViewById(R.id.year_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
            if ((row.findViewById(R.id.month_button)) != null)
                (row.findViewById(R.id.month_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
            if ((row.findViewById(R.id.week_button)) != null)
                (row.findViewById(R.id.week_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
            if ((row.findViewById(R.id.log_button)) != null)
                (row.findViewById(R.id.log_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
            if ((row.findViewById(R.id.timer_button)) != null)
                (row.findViewById(R.id.timer_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
            if ((row.findViewById(R.id.notifications_button)) != null)
                (row.findViewById(R.id.notifications_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));

            if ((row.findViewById(R.id.on_button)) != null)
                (row.findViewById(R.id.on_button)).setBackground(context.getResources().getDrawable(R.drawable.button_status_dark));
            if ((row.findViewById(R.id.off_button)) != null)
                (row.findViewById(R.id.off_button)).setBackground(context.getResources().getDrawable(R.drawable.button_status_dark));
            if ((row.findViewById(R.id.color_button)) != null)
                (row.findViewById(R.id.color_button)).setBackground(context.getResources().getDrawable(R.drawable.button_dark_status));
        }
        return row;
    }

    private View setDefaultRowId(ViewHolder holder) {
        layoutResourceId = R.layout.dashboard_row_default;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);

        return row;
    }

    private View setOnOffSwitchRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_on_off;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.onOffSwitch = (Switch) row.findViewById(R.id.switch_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);
        holder.buttonNotifications = (Button) row.findViewById(R.id.notifications_button);

        return row;
    }

    private View setMotionSwitchRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_motion;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);
        holder.buttonNotifications = (Button) row.findViewById(R.id.notifications_button);

        return row;
    }

    /**
     * Get's the layout items and returns the appropriate row
     *
     * @param holder     Holder to use
     * @param toggleable Should the switch be enabled
     * @return Returns the layout
     */
    private View setPushOnOffSwitchRowId(ViewHolder holder, boolean toggleable) {
        layoutResourceId = R.layout.switch_row_pushon;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonOn.setEnabled(toggleable);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);
        holder.buttonNotifications = (Button) row.findViewById(R.id.notifications_button);

        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);

        return row;
    }

    private View setSecurityPanelSwitchRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_securitypanel;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.buttonOn = (Button) row.findViewById(R.id.on_button);
        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);
        holder.buttonNotifications = (Button) row.findViewById(R.id.notifications_button);

        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);

        return row;
    }


    private View setBlindsRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_blinds;
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);
        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_status = (TextView) row.findViewById(R.id.switch_status);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.buttonUp = (ImageButton) row.findViewById(R.id.switch_button_up);
        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);
        holder.buttonStop = (ImageButton) row.findViewById(R.id.switch_button_stop);
        holder.buttonDown = (ImageButton) row.findViewById(R.id.switch_button_down);
        return row;
    }

    private View setDimmerRowId(ViewHolder holder, boolean isRGB) {
        if (isRGB)
            layoutResourceId = R.layout.switch_row_rgb_dimmer;
        else
            layoutResourceId = R.layout.switch_row_dimmer;

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
        holder.buttonNotifications = (Button) row.findViewById(R.id.notifications_button);

        if (isRGB)
            holder.buttonColor = (Button) row.findViewById(R.id.color_button);
        return row;
    }

    private View setModalSwitchRowId(ViewHolder holder) {
        layoutResourceId = R.layout.switch_row_modal;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(layoutResourceId, null);

        holder.iconRow = (ImageView) row.findViewById(R.id.rowIcon);

        holder.switch_name = (TextView) row.findViewById(R.id.switch_name);
        holder.switch_battery_level = (TextView) row.findViewById(R.id.switch_battery_level);
        holder.signal_level = (TextView) row.findViewById(R.id.switch_signal_level);
        holder.buttonSetState = (Button) row.findViewById(R.id.set_status_button);

        holder.buttonLog = (Button) row.findViewById(R.id.log_button);
        holder.buttonTimer = (Button) row.findViewById(R.id.timer_button);
        holder.buttonNotifications = (Button) row.findViewById(R.id.notifications_button);

        return row;
    }

    private void setSwitchRowData(DevicesInfo mDevicesInfo,
                                  ViewHolder holder) {

        switch (mDevicesInfo.getSwitchTypeVal()) {
            case Domoticz.Device.Type.Value.ON_OFF:
            case Domoticz.Device.Type.Value.MEDIAPLAYER:
            case Domoticz.Device.Type.Value.DOORLOCK:
                switch (mDevicesInfo.getSwitchType()) {
                    case Domoticz.Device.Type.Name.SECURITY:
                        if (mDevicesInfo.getSubType().equals(Domoticz.Device.SubType.Name.SECURITYPANEL))
                            setSecurityPanelSwitchRowData(mDevicesInfo, holder);
                        else
                            setDefaultRowData(mDevicesInfo, holder);
                        break;
                    case Domoticz.Device.Type.Name.EVOHOME:
                        if (mDevicesInfo.getSubType().equals(Domoticz.Device.SubType.Name.EVOHOME))
                            setModalSwitchRowData(mDevicesInfo, holder, R.array.evohome_states, R.array.evohome_state_names, EVOHOME_STATE_IDS);
                        else
                            setDefaultRowData(mDevicesInfo, holder);
                        break;
                    default:
                        setOnOffSwitchRowData(mDevicesInfo, holder);
                        break;
                }
                break;

            case Domoticz.Device.Type.Value.X10SIREN:
            case Domoticz.Device.Type.Value.MOTION:
            case Domoticz.Device.Type.Value.CONTACT:
            case Domoticz.Device.Type.Value.DUSKSENSOR:
            case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
            case Domoticz.Device.Type.Value.DOORBELL:
                setContactSwitchRowData(mDevicesInfo, holder, false);
                break;
            case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                setPushOnOffSwitchRowData(mDevicesInfo, holder, true);
                break;

            case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                setPushOnOffSwitchRowData(mDevicesInfo, holder, false);
                break;

            case Domoticz.Device.Type.Value.BLINDVENETIAN:
                setBlindsRowData(mDevicesInfo, holder);
                break;

            case Domoticz.Device.Type.Value.DIMMER:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                if (mDevicesInfo.getSubType().startsWith(Domoticz.Device.SubType.Name.RGB))
                    setDimmerRowData(mDevicesInfo, holder, true);
                else
                    setDimmerRowData(mDevicesInfo, holder, false);
                break;

            case Domoticz.Device.Type.Value.SELECTOR:
                setSelectorRowData(mDevicesInfo, holder);
                break;

            case Domoticz.Device.Type.Value.BLINDS:
            case Domoticz.Device.Type.Value.BLINDINVERTED:
                if (canHandleStopButton(mDevicesInfo))
                    setBlindsRowData(mDevicesInfo, holder);
                else
                    setOnOffSwitchRowData(mDevicesInfo, holder);
                break;

            default:
                throw new NullPointerException(
                        "No supported switch type defined in the adapter (setSwitchRowData)");
        }
    }

    private void setDefaultRowData(DevicesInfo mDeviceInfo,
                                   ViewHolder holder) {

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
            text = context.getString(R.string.status) + ": " + String.valueOf(mDeviceInfo.getData());
            holder.switch_battery_level.setText(text);
        }

        if (mDeviceInfo.getUsage() != null && mDeviceInfo.getUsage().length() > 0) {
            text = context.getString(R.string.usage) + ": " + mDeviceInfo.getUsage();
            holder.switch_battery_level.setText(text);
        }
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

    private boolean canHandleStopButton(DevicesInfo mDevicesInfo) {
        return (mDevicesInfo.getSubType().contains("RAEX")) ||
                (mDevicesInfo.getSubType().contains("A-OK")) ||
                (mDevicesInfo.getSubType().contains("RollerTrol")) ||
                (mDevicesInfo.getSubType().contains("RFY")) ||
                (mDevicesInfo.getSubType().contains("ASA")) ||
                (mDevicesInfo.getSubType().contains("T6 DC"));
    }

    private void setOnOffSwitchRowData(final DevicesInfo mDevicesInfo,
                                       final ViewHolder holder) {

        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context,
                mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getData());
        holder.switch_battery_level.setText(text);

        holder.onOffSwitch.setId(mDevicesInfo.getIdx());
        holder.onOffSwitch.setChecked(mDevicesInfo.getStatusBoolean());

        holder.onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                handleOnOffSwitchClick(compoundButton.getId(), checked);
                mDevicesInfo.setStatusBoolean(checked);

                if (!checked)
                    holder.iconRow.setAlpha(0.5f);
                else
                    holder.iconRow.setAlpha(1f);
            }
        });

        holder.buttonLog.setId(mDevicesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        holder.buttonTimer.setId(mDevicesInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });
        if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.GONE);

        holder.buttonNotifications.setId(mDevicesInfo.getIdx());
        holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationButtonClick(v.getId());
            }
        });
        if (!mDevicesInfo.hasNotifications())
            holder.buttonNotifications.setVisibility(View.GONE);

        if (!mDevicesInfo.getStatusBoolean())
            holder.iconRow.setAlpha(0.5f);
        else
            holder.iconRow.setAlpha(1f);

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
     * Set the data for the contact switch
     *
     * @param mDevicesInfo  Device info class
     * @param holder        Holder to use
     * @param buttonEnabled Should the button be enabled?
     */
    private void setContactSwitchRowData(DevicesInfo mDevicesInfo,
                                         ViewHolder holder,
                                         boolean buttonEnabled) {
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
            holder.buttonOn.setId(mDevicesInfo.getIdx());
            holder.buttonOn.setEnabled(buttonEnabled);

            String status = String.valueOf(mDevicesInfo.getData().toLowerCase());
            if (statusOpen.contains(status)) {
                holder.buttonOn.setText(context.getString(R.string.button_state_open));
                holder.buttonOn.setTextColor(ContextCompat.getColor(context, R.color.black));
                // holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_on));
            } else if (statusClosed.contains(status)) {
                holder.buttonOn.setText(context.getString(R.string.button_state_closed));
                holder.buttonOn.setTextColor(ContextCompat.getColor(context, R.color.black));
                // holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_off));
            } else {
                if (status.startsWith("off")) status = "off";
                holder.buttonOn.setText(status.toUpperCase());
                holder.buttonOn.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
            if (buttonEnabled) {
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

        if (holder.buttonLog != null) {
            holder.buttonLog.setId(mDevicesInfo.getIdx());
            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
        }

        if (holder.buttonTimer != null) {
            holder.buttonTimer.setId(mDevicesInfo.getIdx());
            holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleTimerButtonClick(v.getId());
                }
            });

            if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
                holder.buttonTimer.setVisibility(View.GONE);
        }

        if (holder.buttonNotifications != null) {
            holder.buttonNotifications.setId(mDevicesInfo.getIdx());
            holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleNotificationButtonClick(v.getId());
                }
            });
            if (!mDevicesInfo.hasNotifications())
                holder.buttonNotifications.setVisibility(View.GONE);
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
     * Set the data for the push on/of button data
     *
     * @param mDevicesInfo The device info
     * @param holder       Holder to use
     * @param action       Action to set the state of the button (e.g. on or off)
     */
    private void setPushOnOffSwitchRowData(DevicesInfo mDevicesInfo, ViewHolder holder, boolean action) {
        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context, mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getData());
        holder.switch_battery_level.setText(text);

        holder.buttonOn.setId(mDevicesInfo.getIdx());
        if (action) {
            holder.buttonOn.setText(context.getString(R.string.button_state_on));
            holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_on));
        } else {
            holder.buttonOn.setText(context.getString(R.string.button_state_off));
            holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_off));
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

        holder.buttonLog.setId(mDevicesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });
        holder.buttonTimer.setId(mDevicesInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.GONE);


        holder.buttonNotifications.setId(mDevicesInfo.getIdx());
        holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationButtonClick(v.getId());
            }
        });
        if (!mDevicesInfo.hasNotifications())
            holder.buttonNotifications.setVisibility(View.GONE);

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

    private void setSecurityPanelSwitchRowData(DevicesInfo mDevicesInfo, ViewHolder holder) {
        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context,
                mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getData());
        holder.switch_battery_level.setText(text);

        if (holder.buttonOn != null) {
            holder.buttonOn.setId(mDevicesInfo.getIdx());
            if (mDevicesInfo.getData().startsWith("Arm"))
                holder.buttonOn.setText(context.getString(R.string.button_disarm));
            else
                holder.buttonOn.setText(context.getString(R.string.button_arm));

            holder.buttonOn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_on));
            holder.buttonOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open security panel
                    handleSecurityPanel(v.getId());
                }
            });
        }

        if (holder.buttonLog != null) {
            holder.buttonLog.setId(mDevicesInfo.getIdx());
            holder.buttonLog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogButtonClick(v.getId());
                }
            });
        }

        if (holder.buttonTimer != null) {
            holder.buttonTimer.setId(mDevicesInfo.getIdx());
            holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleTimerButtonClick(v.getId());
                }
            });

            if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
                holder.buttonTimer.setVisibility(View.GONE);
        }


        holder.buttonNotifications.setId(mDevicesInfo.getIdx());
        holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationButtonClick(v.getId());
            }
        });
        if (!mDevicesInfo.hasNotifications())
            holder.buttonNotifications.setVisibility(View.GONE);

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

    private void setBlindsRowData(DevicesInfo mDevicesInfo,
                                  ViewHolder holder) {
        holder.isProtected = mDevicesInfo.isProtected();

        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context,
                mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.switch_status.setText(text);

        text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getData());
        holder.switch_battery_level.setText(text);

        holder.buttonUp.setId(mDevicesInfo.getIdx());
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

        holder.buttonStop.setId(mDevicesInfo.getIdx());
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


        holder.buttonDown.setId(mDevicesInfo.getIdx());
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

    private void setDimmerRowData(final DevicesInfo mDevicesInfo,
                                  final ViewHolder holder,
                                  final boolean isRGB) {

        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context,
                mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getStatus());
        holder.switch_battery_level.setText(text);

        holder.switch_dimmer_level.setId(mDevicesInfo.getIdx() + ID_TEXTVIEW);
        String percentage = calculateDimPercentage(
                mDevicesInfo.getMaxDimLevel(), mDevicesInfo.getLevel());
        holder.switch_dimmer_level.setText(percentage);

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


        holder.dimmer.setProgress(mDevicesInfo.getLevel());
        holder.dimmer.setMax(mDevicesInfo.getMaxDimLevel());
        holder.dimmer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String percentage = calculateDimPercentage(seekBar.getMax(), progress);
                TextView switch_dimmer_level = (TextView) seekBar.getRootView()
                        .findViewById(mDevicesInfo.getIdx() + ID_TEXTVIEW);
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
                        .findViewById(mDevicesInfo.getIdx() + ID_SWITCH);

                if (progress == 0 && dimmerOnOffSwitch.isChecked()) {
                    dimmerOnOffSwitch.setChecked(false);
                    seekBar.setProgress(previousDimmerValue);
                } else if (progress > 0 && !dimmerOnOffSwitch.isChecked()) {
                    dimmerOnOffSwitch.setChecked(true);
                }

                handleDimmerChange(mDevicesInfo.getIdx(), progress + 1, false);
                mDevicesInfo.setLevel(progress);
            }
        });

        if (!mDevicesInfo.getStatusBoolean()) {
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

        holder.buttonLog.setId(mDevicesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        holder.buttonTimer.setId(mDevicesInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.GONE);

        if (isRGB) {
            holder.buttonColor.setId(mDevicesInfo.getIdx());
            holder.buttonColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorButtonClick(v.getId());
                }
            });
        }


        holder.buttonNotifications.setId(mDevicesInfo.getIdx());
        holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationButtonClick(v.getId());
            }
        });
        if (!mDevicesInfo.hasNotifications())
            holder.buttonNotifications.setVisibility(View.GONE);


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

    private void setSelectorRowData(final DevicesInfo mDevicesInfo,
                                    final ViewHolder holder) {
        holder.isProtected = mDevicesInfo.isProtected();
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context,
                mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": " + String.valueOf(mDevicesInfo.getStatus());
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

        holder.buttonLog.setId(mDevicesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        holder.buttonTimer.setId(mDevicesInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.GONE);


        holder.buttonNotifications.setId(mDevicesInfo.getIdx());
        holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationButtonClick(v.getId());
            }
        });
        if (!mDevicesInfo.hasNotifications())
            holder.buttonNotifications.setVisibility(View.GONE);


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

    private void setModalSwitchRowData(DevicesInfo mDevicesInfo, ViewHolder holder, final int stateArrayRes, final int stateNamesArrayRes, final int[] stateIds) {
        holder.switch_name.setText(mDevicesInfo.getName());

        String text = context.getString(R.string.last_update)
                + ": "
                + UsefulBits.getFormattedDate(context,
                mDevicesInfo.getLastUpdateDateTime().getTime());
        holder.signal_level.setText(text);

        text = context.getString(R.string.status) + ": "
                + getStatus(stateArrayRes, stateNamesArrayRes, mDevicesInfo.getStatus());
        holder.switch_battery_level.setText(text);

        holder.buttonSetState.setId(mDevicesInfo.getIdx());
        holder.buttonSetState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open state dialog
                handleStateButtonClick(v.getId(), stateNamesArrayRes, stateIds);
            }
        });

        holder.buttonLog.setId(mDevicesInfo.getIdx());
        holder.buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogButtonClick(v.getId());
            }
        });

        holder.buttonTimer.setId(mDevicesInfo.getIdx());
        holder.buttonTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleTimerButtonClick(v.getId());
            }
        });

        if (mDevicesInfo.getTimers().toLowerCase().equals("false"))
            holder.buttonTimer.setVisibility(View.GONE);

        holder.buttonNotifications.setId(mDevicesInfo.getIdx());
        holder.buttonNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleNotificationButtonClick(v.getId());
            }
        });
        if (!mDevicesInfo.hasNotifications())
            holder.buttonNotifications.setVisibility(View.GONE);

        Picasso.with(context).load(domoticz.getDrawableIcon(mDevicesInfo.getTypeImg(),
                mDevicesInfo.getType(),
                mDevicesInfo.getSwitchType(),
                mDevicesInfo.getStatusBoolean(),
                mDevicesInfo.getUseCustomImage(),
                mDevicesInfo.getImage())).into(holder.iconRow);
    }

    @SuppressLint("DefaultLocale")
    private String calculateDimPercentage(int maxDimLevel, int level) {
        float percentage = ((float) level / (float) maxDimLevel) * 100;
        return String.format("%.0f", percentage) + "%";
    }

    private void handleOnOffSwitchClick(int idx, boolean action) {
        listener.onSwitchClick(idx, action);
    }

    private void handleOnButtonClick(int idx, boolean action) {
        listener.onButtonClick(idx, action);
    }

    private void handleSecurityPanel(int idx) {
        listener.onSecurityPanelButtonClick(idx);
    }

    private void handleStateButtonClick(final int idx, int itemsRes, int[] itemIds) {
        listener.onStateButtonClick(idx, itemsRes, itemIds);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }

    private void handleColorButtonClick(int idx) {
        listener.onColorButtonClick(idx);
    }

    private void handleTimerButtonClick(int idx) {
        listener.onTimerButtonClick(idx);
    }

    private void handleNotificationButtonClick(int idx) {
        listener.onNotificationButtonClick(idx);
    }

    private void handleBlindsClick(int idx, int action) {
        listener.onBlindClick(idx, action);
    }

    private void handleDimmerChange(final int idx, final int value, boolean selector) {
        listener.onDimmerChange(idx, value, selector);
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

    private void handleSelectorDimmerClick(int idx, String[] levelNames) {
        listener.onSelectorDimmerClick(idx, levelNames);
    }

    static class ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        ImageButton buttonUp, buttonDown, buttonStop;
        Button buttonOn, buttonLog, buttonTimer, buttonColor, buttonSetState, buttonNotifications;
        Boolean isProtected;
        ImageView iconRow;
        SeekBar dimmer;
    }

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