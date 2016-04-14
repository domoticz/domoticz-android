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

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.DashboardAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.ColorPickerDialog;
import nl.hnogames.domoticz.UI.DeviceInfoDialog;
import nl.hnogames.domoticz.UI.PasswordDialog;
import nl.hnogames.domoticz.UI.ScheduledTemperatureDialog;
import nl.hnogames.domoticz.UI.SecurityPanelDialog;
import nl.hnogames.domoticz.UI.TemperatureDialog;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzDashboardFragment;

public class Dashboard extends DomoticzDashboardFragment implements DomoticzFragmentListener,
        switchesClickListener {

    public static final String PERMANENT_OVERRIDE = "PermanentOverride";
    public static final String AUTO = "Auto";
    private static final String TAG = Dashboard.class.getSimpleName();

    private Context mContext;
    private DashboardAdapter adapter;
    private ArrayList<DevicesInfo> extendedStatusSwitches;
    private int planID = 0;
    private String planName = "";
    private Parcelable state = null;
    private boolean busy = false;
    private String filter = "";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processDashboard();
    }

    public void selectedPlan(int plan, String name) {
        planID = plan;
        planName = name;
    }

    @Override
    public void Filter(String text) {
        filter = text;
        try {
            if (adapter != null) {
                adapter.getFilter().filter(text);
                adapter.notifyDataSetChanged();
            }
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConnectionOk() {
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_dashboard);
        super.showSpinner(true);
        if (getView() != null) {
            if (planName != null && planName.length() > 0)
                if (getActionBar() != null)
                    getActionBar().setTitle(planName + "");
            processDashboard();
        }
    }

    private void processDashboard() {
        busy = true;
        if (extendedStatusSwitches != null && extendedStatusSwitches.size() > 0) {
            state = gridView.getLayoutManager().onSaveInstanceState();
        }

        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        mDomoticz.getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                processDevices(switches);
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        }, planID, null);
    }

    private void processDevices(ArrayList<DevicesInfo> devicesInfos) {
        extendedStatusSwitches = new ArrayList<>();
        for (DevicesInfo switchInfo : devicesInfos) {
            successHandling(switchInfo.toString(), false);

            if (this.planID <= 0) {
                if (switchInfo.getFavoriteBoolean()) {//only favorites
                    extendedStatusSwitches.add(switchInfo);     // Add to array
                }
            } else {
                extendedStatusSwitches.add(switchInfo);
            }
        }
        if (extendedStatusSwitches.size() <= 0) {
            setMessage(mContext.getString(R.string.no_data_on_domoticz));
        } else
            createListView(extendedStatusSwitches);
    }

    // add dynamic list view
    private void createListView(ArrayList<DevicesInfo> switches) {
        if (switches == null)
            return;

        if (getView() != null) {
            try {
                ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
                final List<Integer> appSupportedSwitchesValues = mDomoticz.getSupportedSwitchesValues();
                final List<String> appSupportedSwitchesNames = mDomoticz.getSupportedSwitchesNames();

                for (DevicesInfo mExtendedStatusInfo : switches) {
                    String name = mExtendedStatusInfo.getName();
                    int switchTypeVal = mExtendedStatusInfo.getSwitchTypeVal();
                    String switchType = mExtendedStatusInfo.getSwitchType();

                    if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                            (appSupportedSwitchesValues.contains(switchTypeVal) && appSupportedSwitchesNames.contains(switchType)) ||
                            UsefulBits.isEmpty(switchType)) {
                        if (UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        } else {
                            if (mContext != null) {
                                UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.filter_on) + ": " + super.getSort(), Snackbar.LENGTH_SHORT);
                                if ((super.getSort().equals(mContext.getString(R.string.filterOn_on)) && mExtendedStatusInfo.getStatusBoolean()) &&
                                        mDomoticz.isOnOffSwitch(mExtendedStatusInfo)) {
                                    supportedSwitches.add(mExtendedStatusInfo);
                                }
                                if ((super.getSort().equals(mContext.getString(R.string.filterOn_off)) && !mExtendedStatusInfo.getStatusBoolean()) &&
                                        mDomoticz.isOnOffSwitch(mExtendedStatusInfo)) {
                                    supportedSwitches.add(mExtendedStatusInfo);
                                }
                                if (super.getSort().equals(mContext.getString(R.string.filterOn_static)) &&
                                        !mDomoticz.isOnOffSwitch(mExtendedStatusInfo)) {
                                    supportedSwitches.add(mExtendedStatusInfo);
                                }
                            }
                        }
                    }
                }

                final switchesClickListener listener = this;
                if (this.planID <= 0)
                    adapter = new DashboardAdapter(mContext, getServerUtil(), supportedSwitches, listener, !mSharedPrefs.showDashboardAsList());
                else {
                    gridView.setHasFixedSize(true);
                    GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
                    gridView.setLayoutManager(mLayoutManager);
                    adapter = new DashboardAdapter(mContext, getServerUtil(), supportedSwitches, listener, true);
                }

                SlideInBottomAnimationAdapter alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
                if (state != null) {
                    gridView.getLayoutManager().onRestoreInstanceState(state);
                }

                mSwipeRefreshLayout.setRefreshing(false);
                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        processDashboard();
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            this.Filter(filter);
            busy = false;
            super.showSpinner(false);
            adapter.notifyDataSetChanged();
        }
    }

    private void showInfoDialog(final DevicesInfo mSwitch) {
        DeviceInfoDialog infoDialog = new DeviceInfoDialog(
                mContext,
                mSwitch,
                R.layout.dialog_switch_info);

        infoDialog.setIdx(String.valueOf(mSwitch.getIdx()));
        infoDialog.setLastUpdate(mSwitch.getLastUpdate());
        infoDialog.setSignalLevel(String.valueOf(mSwitch.getSignalLevel()));
        infoDialog.setBatteryLevel(String.valueOf(mSwitch.getBatteryLevel()));
        infoDialog.setIsFavorite(mSwitch.getFavoriteBoolean());
        infoDialog.show();

        infoDialog.onDismissListener(new DeviceInfoDialog.DismissListener() {
            @Override
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged) {
                    changeFavorite(mSwitch, isFavorite);
                    processDashboard();
                }
            }
        });
    }

    private void changeFavorite(final DevicesInfo mSwitch, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mSwitch.getIdx() + " favorite to " + isFavorite);

        if (isFavorite)
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mSwitch.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        else
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mSwitch.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = Domoticz.Device.Favorite.ON;
        else jsonAction = Domoticz.Device.Favorite.OFF;

        if (mSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || mSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
            jsonUrl = Domoticz.Json.Url.Set.SCENEFAVORITE;
        }

        mDomoticz.setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                processDashboard();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onSwitchClick(int idx, final boolean checked) {
        if (busy)
            return;
        addDebugText("onSwitchClick");
        addDebugText("Set idx " + idx + " to " + checked);
        final DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                public void onDismiss(String password) {
                    toggleSwitch(clickedSwitch, checked, password);
                }
            });
        } else {
            toggleSwitch(clickedSwitch, checked, null);
        }
    }

    private void toggleSwitch(final DevicesInfo clickedSwitch, final boolean checked, final String password) {
        if (checked)
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        else
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);


        int idx = clickedSwitch.getIdx();
        if (clickedSwitch.getIdx() > 0) {
            int jsonAction;
            int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
            if (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                    clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.OFF;
                else jsonAction = Domoticz.Device.Switch.Action.ON;
            } else {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
                else jsonAction = Domoticz.Device.Switch.Action.OFF;
            }

            if (clickedSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || clickedSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
                jsonUrl = Domoticz.Json.Url.Set.SCENES;
                if (checked) jsonAction = Domoticz.Scene.Action.ON;
                else jsonAction = Domoticz.Scene.Action.OFF;
            }

            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    //processDashboard();
                    // Change the clicked switch status
                    clickedSwitch.setStatusBoolean(checked);
                    changeAdapterData(clickedSwitch);
                }

                @Override
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password))
                        UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    else
                        errorHandling(error);
                }
            });
        }
    }

    private DevicesInfo getDevice(int idx) {
        DevicesInfo clickedSwitch = null;
        for (DevicesInfo mExtendedStatusInfo : extendedStatusSwitches) {
            if (mExtendedStatusInfo.getIdx() == idx) {
                clickedSwitch = mExtendedStatusInfo;
            }
        }
        if (clickedSwitch == null) {
            for (DevicesInfo mExtendedStatusInfo : extendedStatusSwitches) {
                if (mExtendedStatusInfo.getType().equals(Domoticz.Scene.Type.GROUP) || mExtendedStatusInfo.getType().equals(Domoticz.Scene.Type.SCENE)) {
                    if (mExtendedStatusInfo.getIdx() == (idx - DashboardAdapter.ID_SCENE_SWITCH)) {
                        clickedSwitch = mExtendedStatusInfo;
                    }
                }
            }
        }
        return clickedSwitch;
    }

    @Override
    public void onButtonClick(int idx, final boolean checked) {
        if (busy)
            return;

        addDebugText("onButtonClick");
        addDebugText("Set idx " + idx + " to " + (checked ? "ON" : "OFF"));

        final DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                public void onDismiss(String password) {
                    toggleButton(clickedSwitch, checked, password);
                }
            });
        } else
            toggleButton(clickedSwitch, checked, null);
    }

    private void toggleButton(final DevicesInfo clickedSwitch, final boolean checked, final String password) {
        if (checked)
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        else
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);

        int idx = clickedSwitch.getIdx();
        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction = Domoticz.Device.Switch.Action.OFF;

        if (clickedSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || clickedSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
            jsonUrl = Domoticz.Json.Url.Set.SCENES;
            if (checked) jsonAction = Domoticz.Scene.Action.ON;
            else jsonAction = Domoticz.Scene.Action.OFF;
        }
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                clickedSwitch.setStatusBoolean(checked);
                changeAdapterData(clickedSwitch);
                //processDashboard();
            }

            @Override
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password))
                    UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                else
                    errorHandling(error);
            }
        });
    }

    @Override
    public void onLogButtonClick(int idx) {
    }

    @Override
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getSwitch(idx), checked);
    }

    @Override
    public void onColorButtonClick(final int idx) {
        ColorPickerDialog colorDialog = new ColorPickerDialog(
                mContext, idx);
        colorDialog.show();
        colorDialog.onDismissListener(new ColorPickerDialog.DismissListener() {
            @Override
            public void onDismiss(final int selectedColor) {
                if (getDevice(idx).isProtected()) {
                    PasswordDialog passwordDialog = new PasswordDialog(
                            mContext, mDomoticz);
                    passwordDialog.show();
                    passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                        @Override
                        public void onDismiss(String password) {
                            setColor(selectedColor, idx, password);
                        }
                    });
                } else
                    setColor(selectedColor, idx, null);
            }

            @Override
            public void onChangeColor(final int selectedColor) {
                if (!getDevice(idx).isProtected()) {
                    setColor(selectedColor, idx, null);
                }
            }
        });
    }

    private void setColor(int selectedColor, final int idx, final String password) {
        double[] hsv = UsefulBits.rgb2hsv(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
        if (hsv == null || hsv.length <= 0)
            return;

        Log.v(TAG, "Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2] + " color: " + selectedColor);
        addDebugText("Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2] + " color: " + selectedColor);

        boolean isWhite = false;
        long hue = Math.round(hsv[0]);
        if (selectedColor == -1) {
            isWhite = true;
        }
        mDomoticz.setRGBColorAction(idx,
                Domoticz.Json.Url.Set.RGBCOLOR,
                hue,
                getDevice(idx).getLevel(),
                isWhite,
                password,
                new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.color_set) + ": " + getDevice(idx).getName(), Snackbar.LENGTH_SHORT);
                        //processDashboard();
                    }

                    @Override
                    public void onError(Exception error) {
                        if (!UsefulBits.isEmpty(password))
                            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        else
                            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.error_color, Snackbar.LENGTH_SHORT);
                    }
                });
    }

    @Override
    public void onTimerButtonClick(int idx) {
    }

    @Override
    public void onNotificationButtonClick(int idx) {
    }

    @Override
    public void onThermostatClick(final int idx) {
        addDebugText("onThermostatClick");
        final DevicesInfo tempUtil = getDevice(idx);
        if (tempUtil != null) {

            TemperatureDialog tempDialog = new TemperatureDialog(
                    mContext,
                    tempUtil.getSetPoint());

            tempDialog.onDismissListener(new TemperatureDialog.DialogActionListener() {
                @Override
                public void onDialogAction(final double newSetPoint, DialogAction dialogAction) {
                    addDebugText("Set idx " + idx + " to " + String.valueOf(newSetPoint));
                    if (dialogAction == DialogAction.POSITIVE) {
                        if (tempUtil.isProtected()) {
                            PasswordDialog passwordDialog = new PasswordDialog(
                                    mContext, mDomoticz);
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(final String password) {
                                    int jsonUrl = Domoticz.Json.Url.Set.TEMP;
                                    int action = Domoticz.Device.Thermostat.Action.PLUS;
                                    if (newSetPoint < tempUtil.getSetPoint())
                                        action = Domoticz.Device.Thermostat.Action.MIN;
                                    mDomoticz.setAction(idx, jsonUrl, action, newSetPoint, password,
                                            new setCommandReceiver() {
                                                @Override
                                                public void onReceiveResult(String result) {
                                                    successHandling(result, false);
                                                    processDashboard();
                                                }

                                                @Override
                                                public void onError(Exception error) {
                                                    UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                                }
                                            });
                                }
                            });
                        } else {
                            int jsonUrl = Domoticz.Json.Url.Set.TEMP;
                            int action = Domoticz.Device.Thermostat.Action.PLUS;
                            if (newSetPoint < tempUtil.getSetPoint())
                                action = Domoticz.Device.Thermostat.Action.MIN;
                            mDomoticz.setAction(idx, jsonUrl, action, newSetPoint, null,
                                    new setCommandReceiver() {
                                        @Override
                                        public void onReceiveResult(String result) {
                                            successHandling(result, false);
                                            processDashboard();
                                        }

                                        @Override
                                        public void onError(Exception error) {
                                            errorHandling(error);
                                        }
                                    });
                        }
                    }
                }
            });
            tempDialog.show();
        }
    }

    @Override
    public void onSetTemperatureClick(final int idx) {
        addDebugText("onSetTemperatureClick");
        final DevicesInfo tempUtil = getDevice(idx);
        if (tempUtil != null) {
            final setCommandReceiver commandReceiver = new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    processDashboard();
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error);
                }
            };

            final boolean evohomeZone = "evohome".equals(tempUtil.getHardwareName());

            TemperatureDialog tempDialog;
            if (evohomeZone) {
                tempDialog = new ScheduledTemperatureDialog(
                        mContext,
                        tempUtil.getSetPoint(),
                        !AUTO.equalsIgnoreCase(tempUtil.getStatus()));
            } else {
                tempDialog = new TemperatureDialog(
                        mContext,
                        tempUtil.getSetPoint());
            }

            tempDialog.onDismissListener(new TemperatureDialog.DialogActionListener() {
                @Override
                public void onDialogAction(double newSetPoint, DialogAction dialogAction) {
                    if (dialogAction == DialogAction.POSITIVE) {
                        addDebugText("Set idx " + idx + " to " + String.valueOf(newSetPoint));

                        String params = "&setpoint=" + String.valueOf(newSetPoint) +
                                "&mode=" + PERMANENT_OVERRIDE;

                        // add query parameters
                        mDomoticz.setDeviceUsed(idx, tempUtil.getName(), tempUtil.getDescription(), params, commandReceiver);
                    } else if (dialogAction == DialogAction.NEUTRAL && evohomeZone) {
                        addDebugText("Set idx " + idx + " to Auto");

                        String params = "&setpoint=" + String.valueOf(newSetPoint) +
                                "&mode=" + AUTO;

                        // add query parameters
                        mDomoticz.setDeviceUsed(idx, tempUtil.getName(), tempUtil.getDescription(), params, commandReceiver);
                    } else {
                        addDebugText("Not updating idx " + idx);
                    }
                }
            });

            tempDialog.show();
        }
    }

    @Override
    public void onSecurityPanelButtonClick(int idx) {
        SecurityPanelDialog securityDialog = new SecurityPanelDialog(
                mContext, mDomoticz,
                getDevice(idx));
        securityDialog.show();

        securityDialog.onDismissListener(new SecurityPanelDialog.DismissListener() {
            @Override
            public void onDismiss() {
                processDashboard();//refresh
            }
        });
    }

    @Override
    public void onStateButtonClick(final int idx, int itemsRes, final int[] stateIds) {
        new MaterialDialog.Builder(mContext)
                .title(R.string.choose_status)
                .items(itemsRes)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                        if (getDevice(idx).isProtected()) {
                            PasswordDialog passwordDialog = new PasswordDialog(
                                    mContext, mDomoticz);
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(String password) {
                                    setState(idx, stateIds[which], password);
                                }
                            });
                        } else
                            setState(idx, stateIds[which], null);
                    }
                })
                .show();
    }

    @Override
    public void onSelectorDimmerClick(final int idx, final String[] levelNames) {
        new MaterialDialog.Builder(mContext)
                .title(R.string.choose_status)
                .items(levelNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        for (int i = 0; i < levelNames.length; i++) {
                            if (levelNames[i].equals(text)) {
                                onDimmerChange(idx, i * 10, true);
                            }
                        }
                    }
                })
                .show();
    }

    @Override
    public void onItemClicked(View v, int position) {

    }

    @Override
    public boolean onItemLongClicked(int position) {
        showInfoDialog(adapter.filteredData.get(position));
        return true;
    }

    private void setState(final int idx, int state, final String password) {
        mDomoticz.setModalAction(idx,
                state,
                1,
                password,
                new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.state_set) + ": " + getSwitch(idx).getName(), Snackbar.LENGTH_SHORT);
                        processDashboard();
                    }

                    @Override
                    public void onError(Exception error) {
                        if (!UsefulBits.isEmpty(password))
                            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        else
                            errorHandling(error);
                    }
                });
    }

    private DevicesInfo getSwitch(int idx) {
        DevicesInfo clickedSwitch = null;
        for (DevicesInfo mDevicesInfo : extendedStatusSwitches) {
            if (mDevicesInfo.getIdx() == idx) {
                clickedSwitch = mDevicesInfo;
            }
        }
        return clickedSwitch;
    }

    @Override
    public void onBlindClick(final int idx, final int jsonAction) {
        if (busy)
            return;

        addDebugText("onBlindClick");
        addDebugText("Set idx " + idx + " to " + String.valueOf(jsonAction));
        final DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                public void onDismiss(String password) {
                    setBlindState(clickedSwitch, jsonAction, password);
                }
            });
        } else {
            setBlindState(clickedSwitch, jsonAction, null);
        }
    }

    private void setBlindState(final DevicesInfo clickedSwitch, final int jsonAction, final String password) {
        if ((jsonAction == Domoticz.Device.Blind.Action.UP || jsonAction == Domoticz.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() != Domoticz.Device.Type.Value.BLINDINVERTED)) {
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            clickedSwitch.setStatus(Domoticz.Device.Blind.State.OPEN);
        } else if ((jsonAction == Domoticz.Device.Blind.Action.DOWN || jsonAction == Domoticz.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() != Domoticz.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(Domoticz.Device.Blind.State.CLOSED);
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        } else if ((jsonAction == Domoticz.Device.Blind.Action.UP || jsonAction == Domoticz.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(Domoticz.Device.Blind.State.CLOSED);
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        } else if ((jsonAction == Domoticz.Device.Blind.Action.DOWN || jsonAction == Domoticz.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(Domoticz.Device.Blind.State.OPEN);
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        } else {
            clickedSwitch.setStatus(Domoticz.Device.Blind.State.STOPPED);
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.blind_stop) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        }

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        mDomoticz.setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                //processDashboard();
                changeAdapterData(clickedSwitch);
            }

            @Override
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password))
                    UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                else
                    errorHandling(error);
            }
        });
    }

    private void changeAdapterData(DevicesInfo clickedSwitch) {
        // Only when not showing the extra data on dashboard
        // When extra data is on, more info has to be changed other than the status
        if (!mSharedPrefs.showExtraData()) {

            // Let's find out where the clicked switch is in the list
            int index = extendedStatusSwitches.indexOf(clickedSwitch);

            // Add it back into the array list
            extendedStatusSwitches.set(index, clickedSwitch);

            // Clear the data in the adapter and add all switches back in
            adapter.data.clear();
            adapter.data.addAll(extendedStatusSwitches);

            // Notify the adapter the data has changed
            adapter.notifyDataSetChanged();
        } else processDashboard();
    }

    @Override
    public void onDimmerChange(int idx, final int value, final boolean selector) {
        if (busy)
            return;

        addDebugText("onDimmerChange for " + idx + " to " + value);
        final DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                public void onDismiss(String password) {
                    setDimmerState(clickedSwitch, value, selector, password);
                }
            });
        } else {
            setDimmerState(clickedSwitch, value, selector, null);
        }
    }

    private void setDimmerState(DevicesInfo clickedSwitch, int value, final boolean selector, final String password) {
        if (clickedSwitch != null) {
            String text = String.format(mContext.getString(R.string.set_level_switch),
                    clickedSwitch.getName(),
                    !selector ? (value - 1) : ((value) / 10) + 1);
            UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, text, Snackbar.LENGTH_SHORT);

            int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
            int jsonAction = Domoticz.Device.Dimmer.Action.DIM_LEVEL;

            mDomoticz.setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, !selector ? (value) : (value) + 10, password, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    if (selector)
                        processDashboard();
                }

                @Override
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password))
                        UsefulBits.showSimpleSnackbar(mContext, coordinatorLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    else
                        errorHandling(error);
                }
            });
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }
}