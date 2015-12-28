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
import android.graphics.Color;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Adapters.SwitchesAdapter;
import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Containers.SwitchLogInfo;
import nl.hnogames.domoticz.Containers.SwitchTimerInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Interfaces.switchesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.ColorPickerDialog;
import nl.hnogames.domoticz.UI.SwitchInfoDialog;
import nl.hnogames.domoticz.UI.SwitchLogInfoDialog;
import nl.hnogames.domoticz.UI.SwitchTimerInfoDialog;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Switches extends DomoticzFragment implements DomoticzFragmentListener,
        switchesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Switches.class.getSimpleName();
    private ProgressDialog progressDialog;
    private Domoticz mDomoticz;
    private Context mContext;
    private int currentSwitch = 1;
    private SwitchesAdapter adapter;

    private CoordinatorLayout coordinatorLayout;
    private ArrayList<DevicesInfo> extendedStatusSwitches;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Parcelable state;
    private ListView listView;

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        getSwitchesData();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        getActionBar().setTitle(R.string.title_switches);
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

        mDomoticz = new Domoticz(mContext);
        getSwitchesData();
    }

    private void getSwitchesData() {
        if (listView != null) {
            //switch toggled, refresh listview
            state = listView.onSaveInstanceState();
            WidgetUtils.RefreshWidgets(mContext);
        }

        mDomoticz.getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                extendedStatusSwitches = mDevicesInfo;
                successHandling(mDevicesInfo.toString(), false);
                createListView(mDevicesInfo);
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        }, 0, "light");
    }

    // add dynamic list view
    // https://github.com/nhaarman/ListViewAnimations
    private void createListView(ArrayList<DevicesInfo> switches) {
        try {
            coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id.coordinatorLayout);

            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);

            ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
            final List<Integer> appSupportedSwitchesValues = mDomoticz.getSupportedSwitchesValues();
            final List<String> appSupportedSwitchesNames = mDomoticz.getSupportedSwitchesNames();

            for (DevicesInfo mDevicesInfo : switches) {
                String name = mDevicesInfo.getName();
                int switchTypeVal = mDevicesInfo.getSwitchTypeVal();
                String switchType = mDevicesInfo.getSwitchType();

                if (!name.startsWith(Domoticz.HIDDEN_CHARACTER) &&
                        appSupportedSwitchesValues.contains(switchTypeVal) &&
                        appSupportedSwitchesNames.contains(switchType)) {
                    if (super.getSort().equals(null) || super.getSort().length() <= 0 || super.getSort().equals(getContext().getString(R.string.sort_all))) {
                        supportedSwitches.add(mDevicesInfo);
                    } else {
                        Snackbar.make(coordinatorLayout, "Filter on :" + super.getSort(), Snackbar.LENGTH_SHORT).show();
                        if ((super.getSort().equals(getContext().getString(R.string.sort_on)) && mDevicesInfo.getStatusBoolean()) && isOnOffSwitch(mDevicesInfo)) {
                            supportedSwitches.add(mDevicesInfo);
                        }
                        if ((super.getSort().equals(getContext().getString(R.string.sort_off)) && !mDevicesInfo.getStatusBoolean()) && isOnOffSwitch(mDevicesInfo)) {
                            supportedSwitches.add(mDevicesInfo);
                        }
                        if ((super.getSort().equals(getContext().getString(R.string.sort_static))) && !isOnOffSwitch(mDevicesInfo)) {
                            supportedSwitches.add(mDevicesInfo);
                        }
                    }
                }
            }

            final switchesClickListener listener = this;
            adapter = new SwitchesAdapter(mContext, supportedSwitches, listener);
            listView = (ListView) getView().findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view,
                                               int index, long id) {
                    showInfoDialog(adapter.filteredData.get(index));
                    return true;
                }
            });

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    getSwitchesData();
                }
            });
            if (state != null) {
                // Restore previous state (including selected item index and scroll position)
                listView.onRestoreInstanceState(state);
            }
        } catch (Exception ex) {
            errorHandling(ex);
        }
        hideProgressDialog();
    }

    private boolean isOnOffSwitch(DevicesInfo testSwitch) {
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

    private void showInfoDialog(final DevicesInfo mSwitch) {

        SwitchInfoDialog infoDialog = new SwitchInfoDialog(
                getActivity(),
                mSwitch,
                R.layout.dialog_switch_info);
        infoDialog.setIdx(String.valueOf(mSwitch.getIdx()));
        infoDialog.setLastUpdate(mSwitch.getLastUpdate());
        infoDialog.setSignalLevel(String.valueOf(mSwitch.getSignalLevel()));
        infoDialog.setBatteryLevel(String.valueOf(mSwitch.getBatteryLevel()));
        infoDialog.setIsFavorite(mSwitch.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener(new SwitchInfoDialog.DismissListener() {
            @Override
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged) changeFavorite(mSwitch, isFavorite);
            }
        });
    }

    private void showLogDialog(ArrayList<SwitchLogInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(getContext(), "No logs found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchLogInfoDialog infoDialog = new SwitchLogInfoDialog(
                    getActivity(),
                    switchLogs,
                    R.layout.dialog_switch_logs);
            infoDialog.show();
        }
    }

    private void showTimerDialog(ArrayList<SwitchTimerInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(getContext(), "No timer found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchTimerInfoDialog infoDialog = new SwitchTimerInfoDialog(
                    getActivity(),
                    switchLogs,
                    R.layout.dialog_switch_logs);
            infoDialog.show();
        }
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

        mDomoticz.setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSwitch.setFavoriteBoolean(isFavorite);
                getSwitchesData();
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(coordinatorLayout, getContext().getString(R.string.error_favorite), Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onLogButtonClick(int idx) {
        mDomoticz.getSwitchLogs(idx, new SwitchLogReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchLogInfo> switchesLogs) {
                showLogDialog(switchesLogs);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(coordinatorLayout, getContext().getString(R.string.error_logs), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onColorButtonClick(final int idx) {
        ColorPickerDialog colorDialog = new ColorPickerDialog(
                getActivity());
        colorDialog.show();
        colorDialog.onDismissListener(new ColorPickerDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedColor) {
                float[] hsv = new float[3];
                Color.RGBToHSV(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor), hsv);
                Log.v(TAG, "Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2]);

                mDomoticz.setRGBColorAction(idx,
                        Domoticz.Json.Url.Set.RGBCOLOR,
                        Math.round(hsv[0]),
                        100,
                        new setCommandReceiver() {
                            @Override
                            public void onReceiveResult(String result) {
                                Snackbar.make(coordinatorLayout, getContext().getString(R.string.color_set) + ": " + getSwitch(idx).getName(), Snackbar.LENGTH_SHORT).show();
                                getSwitchesData();
                            }

                            @Override
                            public void onError(Exception error) {
                                Snackbar.make(coordinatorLayout, getContext().getString(R.string.error_color), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    public void onTimerButtonClick(int idx) {
        mDomoticz.getSwitchTimers(idx, new SwitchTimerReceiver() {
            @Override
            public void onReceiveSwitchTimers(ArrayList<SwitchTimerInfo> switchTimers) {
                if (switchTimers != null)
                    showTimerDialog(switchTimers);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(coordinatorLayout, getContext().getString(R.string.error_timer), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onThermostatClick(int idx, int action, double newSetPoint) {
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
    public void onSwitchClick(int idx, boolean checked) {
        try {
            addDebugText("onSwitchClick");
            addDebugText("Set idx " + idx + " to " + checked);
            DevicesInfo clickedSwitch = getSwitch(idx);

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

                mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        successHandling(result, false);
                        getSwitchesData();
                    }

                    @Override
                    public void onError(Exception error) {
                        errorHandling(error);
                    }
                });
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public void onButtonClick(int idx, boolean checked) {
        addDebugText("onButtonClick");
        addDebugText("Set idx " + idx + " to ON");
        DevicesInfo clickedSwitch = getSwitch(idx);

        if (checked)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked)
            jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction =
                Domoticz.Device.Switch.Action.OFF;

        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                getSwitchesData();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    public void onBlindClick(int idx, int jsonAction) {
        addDebugText("onBlindClick");
        addDebugText("Set idx " + idx + " to " + String.valueOf(jsonAction));
        DevicesInfo clickedSwitch = getSwitch(idx);

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
                getSwitchesData();
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
        DevicesInfo clickedSwitch = getSwitch(idx);
        if(clickedSwitch!=null) {
            Snackbar.make(coordinatorLayout, getContext().getString(R.string.error_level) + ": " + clickedSwitch.getName() + " to " + value, Snackbar.LENGTH_SHORT).show();

            int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
            int jsonAction = Domoticz.Device.Dimmer.Action.DIM_LEVEL;
            mDomoticz.setAction(idx, jsonUrl, jsonAction, value, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    //getSwitchesData();
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
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
        if (progressDialog == null) initProgressDialog();
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    /**
     * Hides the progress dialog if it is showing
     */
    private void hideProgressDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void errorHandling(Exception error) {
        super.errorHandling(error);
        hideProgressDialog();
    }
}