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

import android.app.ProgressDialog;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Adapters.DevicesAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.DeviceInfoDialog;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Dashboard extends DomoticzFragment implements DomoticzFragmentListener,
        switchesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Switches.class.getSimpleName();
    private ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Domoticz mDomoticz;
    private Context mContext;
    private DevicesAdapter adapter;
    private ArrayList<DevicesInfo> extendedStatusSwitches;

    private int planID = 0;
    private String planName = "";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void Filter(String text) {
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onConnectionOk() {
        showProgressDialog();
        getActionBar().setTitle(R.string.title_dashboard);

        if (planName != null && planName.length() > 0)
            getActionBar().setTitle(planName + "");

        processDashboard();
    }

    private void processDashboard() {
        mDomoticz = new Domoticz(mContext);
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
        }, planID);
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
        createListView(extendedStatusSwitches);
    }

    private boolean isOnOffSwitch(DevicesInfo testSwitch)
    {
        if(testSwitch.getSwitchTypeVal()<=0 && testSwitch.getSwitchType()==null)
            return false;

        switch (testSwitch.getSwitchTypeVal()) {
            case Domoticz.Device.Type.Value.ON_OFF:
            case Domoticz.Device.Type.Value.MEDIAPLAYER:
            case Domoticz.Device.Type.Value.X10SIREN:
            case Domoticz.Device.Type.Value.DOORLOCK:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGE:
            case Domoticz.Device.Type.Value.BLINDINVERTED:
            case Domoticz.Device.Type.Value.BLINDPERCENTAGEINVERTED:
            case Domoticz.Device.Type.Value.BLINDVENETIAN:
            case Domoticz.Device.Type.Value.BLINDS:
            case Domoticz.Device.Type.Value.DIMMER:
                return true;
        }
        switch (testSwitch.getType()) {
            case Domoticz.Scene.Type.GROUP:
                return true;
        }

        return false;
    }

    // add dynamic list view
    // https://github.com/nhaarman/ListViewAnimations
    private void createListView(ArrayList<DevicesInfo> switches) {
        if (switches == null)
            return;

        try {
            coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
            supportedSwitches = new ArrayList<>();
            final List<Integer> appSupportedSwitchesValues = mDomoticz.getSupportedSwitchesValues();
            final List<String> appSupportedSwitchesNames = mDomoticz.getSupportedSwitchesNames();

            for (DevicesInfo mExtendedStatusInfo : switches) {
                String name = mExtendedStatusInfo.getName();
                int switchTypeVal = mExtendedStatusInfo.getSwitchTypeVal();
                String switchType = mExtendedStatusInfo.getSwitchType();

                if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                        (appSupportedSwitchesValues.contains(switchTypeVal) && appSupportedSwitchesNames.contains(switchType)) ||
                        (switchType == null || switchType.equals(null) || switchType.length() <= 0)) {
                    if (super.getSort().equals(null) || super.getSort().length() <= 0 || super.getSort().equals(getContext().getString(R.string.sort_all))) {
                        supportedSwitches.add(mExtendedStatusInfo);
                    } else {
                        Snackbar.make(coordinatorLayout, "Filter on :" + super.getSort(), Snackbar.LENGTH_SHORT).show();
                        if ((super.getSort().equals(getContext().getString(R.string.sort_on)) && mExtendedStatusInfo.getStatusBoolean()) &&
                                isOnOffSwitch(mExtendedStatusInfo)) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        }
                        if ((super.getSort().equals(getContext().getString(R.string.sort_off)) && !mExtendedStatusInfo.getStatusBoolean()) &&
                                isOnOffSwitch(mExtendedStatusInfo)) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        }
                        if (super.getSort().equals(getContext().getString(R.string.sort_static)) &&
                                !isOnOffSwitch(mExtendedStatusInfo)) {
                            supportedSwitches.add(mExtendedStatusInfo);
                        }
                    }
                }
            }

            final switchesClickListener listener = this;
            adapter = new DevicesAdapter(mContext, supportedSwitches, listener);
            ListView listView = (ListView) getView().findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long id) {
                    showInfoDialog(adapter.filteredData.get(index));
                    return true;
                }
            });

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

        hideProgressDialog();
    }

    private void showInfoDialog(final DevicesInfo mSwitch) {
        DeviceInfoDialog infoDialog = new DeviceInfoDialog(
                getActivity(),
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
            Snackbar.make(coordinatorLayout, mSwitch.getName() + " " + getActivity().getString(R.string.favorite_added), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, mSwitch.getName() + " " + getActivity().getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = Domoticz.Device.Favorite.ON;
        else jsonAction = Domoticz.Device.Favorite.OFF;

        if (mSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || mSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
            jsonUrl = Domoticz.Json.Url.Set.SCENEFAVORITE;
        }

        mDomoticz.setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
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
    public void onSwitchClick(int idx, boolean checked) {
        addDebugText("onSwitchClick");
        addDebugText("Set idx " + idx + " to " + checked);

        DevicesInfo clickedSwitch = getDevice(idx);
        if (checked)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        if (clickedSwitch != null) {
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
                idx = idx - adapter.ID_SCENE_SWITCH;
            }

            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                }

                @Override
                public void onError(Exception error) {
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
                    if (mExtendedStatusInfo.getIdx() == (idx - adapter.ID_SCENE_SWITCH)) {
                        clickedSwitch = mExtendedStatusInfo;
                    }
                }
            }
        }
        return clickedSwitch;
    }

    @Override
    public void onButtonClick(int idx, boolean checked) {
        addDebugText("onButtonClick");
        addDebugText("Set idx " + idx + " to ON");

        DevicesInfo clickedSwitch = getDevice(idx);

        if (checked)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction = Domoticz.Device.Switch.Action.OFF;

        if (clickedSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || clickedSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
            jsonUrl = Domoticz.Json.Url.Set.SCENES;
            if (checked) jsonAction = Domoticz.Scene.Action.ON;
            else jsonAction = Domoticz.Scene.Action.OFF;
            idx = idx - adapter.ID_SCENE_SWITCH;
        }
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onLogButtonClick(int idx) {
    }

    @Override
    public void onColorButtonClick(int idx) {
    }

    @Override
    public void onTimerButtonClick(int idx) {
    }


    @Override
    public void onBlindClick(int idx, int jsonAction) {
        addDebugText("onBlindClick");
        addDebugText("Set idx " + idx + " to " + String.valueOf(jsonAction));
        DevicesInfo clickedSwitch = getDevice(idx);

        if ((jsonAction == Domoticz.Device.Blind.Action.UP || jsonAction == Domoticz.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() != Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else if ((jsonAction == Domoticz.Device.Blind.Action.DOWN || jsonAction == Domoticz.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() != Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else if ((jsonAction == Domoticz.Device.Blind.Action.UP || jsonAction == Domoticz.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else if ((jsonAction == Domoticz.Device.Blind.Action.DOWN || jsonAction == Domoticz.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDINVERTED))
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.blind_stop) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onDimmerChange(int idx, int value) {
        addDebugText("onDimmerChange");
        DevicesInfo clickedSwitch = getDevice(idx);
        Snackbar.make(coordinatorLayout, "Setting level for switch: " + clickedSwitch.getName() + " to " + value, Snackbar.LENGTH_SHORT).show();

        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        int jsonAction = Domoticz.Device.Dimmer.Action.DIM_LEVEL;

        mDomoticz.setAction(idx, jsonUrl, jsonAction, value, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    /**
     * Initializes the progress dialog
     */
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage(getString(R.string.msg_please_wait));
        progressDialog.setCancelable(false);
    }

    /**
     * Shows the progress dialog if isn't already showing
     */
    private void showProgressDialog() {
        if (progressDialog == null)
            initProgressDialog();
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**
     * Hides the progress dialog if it is showing
     */
    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void errorHandling(Exception error) {
        super.errorHandling(error);
        hideProgressDialog();
    }
}