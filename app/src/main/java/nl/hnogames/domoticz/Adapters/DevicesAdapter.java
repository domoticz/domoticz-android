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

public class DevicesAdapter extends BaseAdapter implements Filterable {

    public final int ID_SCENE_SWITCH = 2000;
    private final int ID_TEXTVIEW = 1000;
    private final int ID_SWITCH = 0;

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

        setSwitchRowData(extendedStatusInfo, holder, convertView);

        return convertView;
    }

    private View setSwitchRowId(DevicesInfo mDeviceInfo, ViewHolder holder) {
        View row = setDefaultRowId(holder);
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null || mDeviceInfo.getSwitchType().equals(null))) {
            switch (mDeviceInfo.getType()) {
                case Domoticz.Scene.Type.GROUP:
                    row = setOnOffSwitchRowId(holder);
                    break;
                case Domoticz.Scene.Type.SCENE:
                    row = setPushOnOffSwitchRowId(holder);
                    break;
                case Domoticz.UTILITIES_TYPE_THERMOSTAT:
                    row = setThermostatRowId(holder);
                    break;
                default:
                    row = setDefaultRowId(holder);
                    break;
            }
        } else {
            if ((mDeviceInfo.getSwitchType() == null || mDeviceInfo.getSwitchType().equals(null))) {
                row = setDefaultRowId(holder);
            } else {
                boolean switchFound = true;
                switch (mDeviceInfo.getSwitchTypeVal()) {
                    case Domoticz.Device.Type.Value.ON_OFF:
                    case Domoticz.Device.Type.Value.MEDIAPLAYER:
                    case Domoticz.Device.Type.Value.X10SIREN:
                    case Domoticz.Device.Type.Value.DOORLOCK:
                        row = setOnOffSwitchRowId(holder);
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
                    default:
                        switchFound = false;
                        break;
                }
            }
        }
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
        holder.buttonPlus = (ImageButton) convertView.findViewById(R.id.utilities_plus);
        holder.buttonMinus = (ImageButton) convertView.findViewById(R.id.utilities_minus);

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
        }else {
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

    private void setSwitchRowData(DevicesInfo mDeviceInfo,
                                  ViewHolder holder,
                                  View convertView) {
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null || mDeviceInfo.getSwitchType().equals(null))) {
            switch (mDeviceInfo.getType()) {
                case Domoticz.Scene.Type.GROUP:
                    setOnOffSwitchRowData(mDeviceInfo, holder);
                    break;
                case Domoticz.Scene.Type.SCENE:
                    setPushOnOffSwitchRowData(mDeviceInfo, holder, true);
                    break;
                case Domoticz.UTILITIES_TYPE_THERMOSTAT:
                    setThermostatRowData(mDeviceInfo, holder);
                    break;
                default:
                    setDefaultRowData(mDeviceInfo, holder);
                    break;
            }
        } else if ((mDeviceInfo.getSwitchType() == null || mDeviceInfo.getSwitchType().equals(null))) {
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
                String.valueOf(mDeviceInfo.getLastUpdate().substring(mDeviceInfo.getLastUpdate().indexOf(" ") + 1));
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
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

    private void setOnOffSwitchRowData(final DevicesInfo mDeviceInfo,
                                       final ViewHolder holder) {

        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mDeviceInfo.getLastUpdate().substring(mDeviceInfo.getLastUpdate().indexOf(" ") + 1));
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (holder.isProtected)
            holder.onOffSwitch.setEnabled(false);

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

        if (holder.isProtected) holder.buttonPlus.setEnabled(false);
        if (holder.isProtected) holder.buttonMinus.setEnabled(false);

        holder.buttonMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double newValue = setPoint - 0.5;
                handleThermostatClick(view.getId(),
                        Domoticz.Device.Thermostat.Action.MIN,
                        newValue);
            }
        });
        holder.buttonPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double newValue = setPoint + 0.5;
                handleThermostatClick(view.getId(),
                        Domoticz.Device.Thermostat.Action.PLUS,
                        newValue);
            }
        });
        holder.buttonPlus.setId(mDeviceInfo.getIdx());
        holder.buttonMinus.setId(mDeviceInfo.getIdx());

        holder.switch_name.setText(mDeviceInfo.getName());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(mDeviceInfo.getLastUpdate());

        if (holder.signal_level != null)
            holder.signal_level.setText(context.getString(R.string.set_point) + ": " + String.valueOf(setPoint));

        Picasso.with(context).load(domoticz.getDrawableIcon(mDeviceInfo.getTypeImg(), mDeviceInfo.getType(), mDeviceInfo.getSubType(), false, false, null)).into(holder.iconRow);
    }

    public void handleThermostatClick(int idx, int action, double newSetPoint) {
        listener.onThermostatClick(idx, action, newSetPoint);
    }

    private void setPushOnOffSwitchRowData(DevicesInfo mDeviceInfo, ViewHolder holder, boolean action) {
        holder.isProtected = mDeviceInfo.isProtected();
        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mDeviceInfo.getLastUpdate().substring(mDeviceInfo.getLastUpdate().indexOf(" ") + 1));
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (holder.isProtected)
            holder.buttonOn.setEnabled(false);

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

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mDeviceInfo.getLastUpdate().substring(mDeviceInfo.getLastUpdate().indexOf(" ") + 1));
        if (holder.switch_status != null)
            holder.switch_status.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mDeviceInfo.getData());
        if (holder.switch_battery_level != null)
            holder.switch_battery_level.setText(text);

        if (holder.isProtected) holder.buttonUp.setEnabled(false);
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

        if (holder.isProtected) holder.buttonStop.setEnabled(false);
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

        if (holder.isProtected) holder.buttonDown.setEnabled(false);
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

    private void setDimmerRowData(final DevicesInfo mDeviceInfo,
                                  final ViewHolder holder,
                                  final boolean isRGB) {

        holder.isProtected = mDeviceInfo.isProtected();

        if (holder.switch_name != null)
            holder.switch_name.setText(mDeviceInfo.getName());

        String text = context.getString(R.string.last_update) + ": " +
                String.valueOf(mDeviceInfo.getLastUpdate().substring(mDeviceInfo.getLastUpdate().indexOf(" ") + 1));
        if (holder.signal_level != null)
            holder.signal_level.setText(text);

        text = context.getString(R.string.data) + ": " +
                String.valueOf(mDeviceInfo.getData());
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

        if (holder.isProtected)
            holder.dimmerOnOffSwitch.setEnabled(false);

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
                handleDimmerChange(mDeviceInfo.getIdx(), progress + 1);
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

        if (isRGB && holder.buttonColor!=null) {
            holder.buttonColor.setId(mDeviceInfo.getIdx());
            holder.buttonColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleColorButtonClick(v.getId());
                }
            });
        }
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

    private void handleOnButtonClick(int idx, boolean action) {
        listener.onButtonClick(idx, action);
    }

    private void handleBlindsClick(int idx, int action) {
        listener.onBlindClick(idx, action);
    }

    private void handleDimmerChange(final int idx, final int value) {
        listener.onDimmerChange(idx, value);
    }

    private void handleLogButtonClick(int idx) {
        listener.onLogButtonClick(idx);
    }


    static class ViewHolder {
        TextView switch_name, signal_level, switch_status, switch_battery_level, switch_dimmer_level;
        Switch onOffSwitch, dimmerOnOffSwitch;
        ImageButton buttonUp, buttonDown, buttonStop, buttonPlus, buttonMinus;
        Button buttonOn, buttonLog, buttonTimer, buttonColor;
        Boolean isProtected;
        ImageView iconRow;
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