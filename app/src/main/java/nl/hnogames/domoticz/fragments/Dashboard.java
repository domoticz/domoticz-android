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

package nl.hnogames.domoticz.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.material.snackbar.Snackbar;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.DashboardAdapter;
import nl.hnogames.domoticz.app.DomoticzDashboardFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.RVHItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.switchesClickListener;
import nl.hnogames.domoticz.ui.DeviceInfoDialog;
import nl.hnogames.domoticz.ui.PasswordDialog;
import nl.hnogames.domoticz.ui.RGBWWColorPickerDialog;
import nl.hnogames.domoticz.ui.ScheduledTemperatureDialog;
import nl.hnogames.domoticz.ui.SecurityPanelDialog;
import nl.hnogames.domoticz.ui.SunriseInfoDialog;
import nl.hnogames.domoticz.ui.TemperatureDialog;
import nl.hnogames.domoticz.ui.WWColorPickerDialog;
import nl.hnogames.domoticz.utils.CameraUtil;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SunRiseInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SunRiseReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class Dashboard extends DomoticzDashboardFragment implements DomoticzFragmentListener,
        switchesClickListener, OnPermissionCallback {

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
    private ItemTouchHelper mItemTouchHelper;
    private SlideInBottomAnimationAdapter alphaSlideIn;
    private boolean itemDecorationAdded = false;
    private PermissionFragmentHelper permissionFragmentHelper;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        permissionFragmentHelper = PermissionFragmentHelper.getInstance(this);
        setActionbar(getString(R.string.title_dashboard));
        setSortFab(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        lySortDevices.setVisibility(View.VISIBLE);
        return view;
    }

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processDashboard();
    }

    @Override
    public void onDestroyView() {
        if (adapter != null)
            adapter.onDestroy();
        super.onDestroyView();
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
                if (UsefulBits.isEmpty(text) &&
                        (UsefulBits.isEmpty(planName) || planName.length() <= 0) &&
                        (UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) &&
                        mSharedPrefs.enableCustomSorting() && !mSharedPrefs.isCustomSortingLocked()) {
                    if (mItemTouchHelper == null) {
                        mItemTouchHelper = new ItemTouchHelper(new RVHItemTouchHelperCallback(adapter, true, false,
                                false));
                    }
                    mItemTouchHelper.attachToRecyclerView(gridView);
                } else {
                    if (mItemTouchHelper != null)
                        mItemTouchHelper.attachToRecyclerView(null);
                }
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
        super.showSpinner(true);

        if (getView() != null) {
            if (planName != null && planName.length() > 0)
                setActionbar(planName + "");
            processDashboard();
        }
    }

    @Override
    public void onConnectionFailed() {
        processDashboard(); //load from cache
    }

    private void processDashboard() {
        try {
            busy = true;
            if (extendedStatusSwitches != null && extendedStatusSwitches.size() > 0) {
                state = gridView.getLayoutManager().onSaveInstanceState();
            }
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            new GetCachedDataTask().execute();
        } catch (Exception ex) {
        }
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
        } else {
            final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
            final List<Integer> appSupportedSwitchesValues = StaticHelper.getDomoticz(mContext).getSupportedSwitchesValues();
            final List<String> appSupportedSwitchesNames = StaticHelper.getDomoticz(mContext).getSupportedSwitchesNames();

            for (DevicesInfo mExtendedStatusInfo : extendedStatusSwitches) {
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
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(mContext.getString(R.string.filter_on) + ": " + super.getSort());
                            if ((super.getSort().equals(mContext.getString(R.string.filterOn_on)) && mExtendedStatusInfo.getStatusBoolean()) &&
                                    StaticHelper.getDomoticz(mContext).isOnOffSwitch(mExtendedStatusInfo)) {
                                supportedSwitches.add(mExtendedStatusInfo);
                            }
                            if ((super.getSort().equals(mContext.getString(R.string.filterOn_off)) && !mExtendedStatusInfo.getStatusBoolean()) &&
                                    StaticHelper.getDomoticz(mContext).isOnOffSwitch(mExtendedStatusInfo)) {
                                supportedSwitches.add(mExtendedStatusInfo);
                            }
                            if (super.getSort().equals(mContext.getString(R.string.filterOn_static)) &&
                                    !StaticHelper.getDomoticz(mContext).isOnOffSwitch(mExtendedStatusInfo)) {
                                supportedSwitches.add(mExtendedStatusInfo);
                            }
                        }
                    }
                } else {
                    Log.i("Devices", "Not suported device.");
                }
            }
            if (mSharedPrefs.addClockToDashboard() && (UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) && planID <= 0) {
                StaticHelper.getDomoticz(mContext).getSunRise(new SunRiseReceiver() {
                    @Override
                    public void onReceive(SunRiseInfo mSunRiseInfo) {
                        createListView(AddAdsDevice(AddClockDevice(mSunRiseInfo, supportedSwitches)), mSunRiseInfo);
                    }

                    @Override
                    public void onError(Exception error) {
                        createListView(AddAdsDevice(supportedSwitches), null);
                    }
                });
            } else {
                createListView(AddAdsDevice(supportedSwitches), null);
            }
        }
    }

    private ArrayList<DevicesInfo> AddClockDevice(SunRiseInfo mSunRiseInfo, ArrayList<DevicesInfo> supportedSwitches) {
        if (mSunRiseInfo != null) {
            boolean alreadySpecified = false;
            for (DevicesInfo d : supportedSwitches) {
                if (d.getType().equals("sunrise"))
                    alreadySpecified = true;
            }
            if (!alreadySpecified) {
                DevicesInfo sunrise = new DevicesInfo();
                sunrise.setIdx(-9999);
                sunrise.setName("Clock");
                sunrise.setType("sunrise");
                sunrise.setDescription("Clock");
                sunrise.setFavoriteBoolean(true);
                sunrise.setIsProtected(false);
                sunrise.setStatusBoolean(false);
                supportedSwitches.add(0, sunrise);
            }
        }
        return supportedSwitches;
    }

    private ArrayList<DevicesInfo> AddAdsDevice(ArrayList<DevicesInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            ArrayList<DevicesInfo> filteredList = new ArrayList<>();
            if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                for (DevicesInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                DevicesInfo adView = new DevicesInfo();
                adView.setIdx(MainActivity.ADS_IDX);
                adView.setName("Ads");
                adView.setType("advertisement");
                adView.setDescription("Advertisement");
                adView.setFavoriteBoolean(true);
                adView.setIsProtected(false);
                adView.setStatusBoolean(false);
                filteredList.add(1, adView);
                return filteredList;
            }
        } catch (Exception ex) {
        }
        return supportedSwitches;
    }

    // add dynamic list view
    private void createListView(ArrayList<DevicesInfo> switches, SunRiseInfo sunrise) {
        if (switches == null)
            return;
        if (getView() != null) {
            try {
                final switchesClickListener listener = this;
                if (adapter == null) {
                    if (this.planID <= 0) {
                        adapter = new DashboardAdapter(mContext, getServerUtil(), switches, listener, false, sunrise);
                    } else {
                        gridView.setHasFixedSize(true);
                        GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
                        gridView.setLayoutManager(mLayoutManager);
                        adapter = new DashboardAdapter(mContext, getServerUtil(), switches, listener, true, sunrise);
                    }
                    alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                    gridView.setAdapter(adapter);

                } else {
                    adapter.setData(switches);
                    adapter.notifyDataSetChanged();
                    alphaSlideIn.notifyDataSetChanged();
                }
                if (state != null) {
                    gridView.getLayoutManager().onRestoreInstanceState(state);
                }

                if (!isTablet && !itemDecorationAdded) {
                    gridView.addItemDecoration(new MarginItemDecoration(20));
                    itemDecorationAdded = true;
                }

                if (mItemTouchHelper == null) {
                    mItemTouchHelper = new ItemTouchHelper(new RVHItemTouchHelperCallback(adapter, true, false,
                            false));
                }
                if ((UsefulBits.isEmpty(planName) || planName.length() <= 0) &&
                        (UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) &&
                        mSharedPrefs.enableCustomSorting() && !mSharedPrefs.isCustomSortingLocked()) {
                    mItemTouchHelper.attachToRecyclerView(gridView);
                } else {
                    if (mItemTouchHelper != null)
                        mItemTouchHelper.attachToRecyclerView(null);
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
        }
    }

    private void showInfoDialog(final DevicesInfo mSwitch, int idx) {
        if (mSwitch != null) {
            DeviceInfoDialog infoDialog = new DeviceInfoDialog(
                    mContext,
                    StaticHelper.getDomoticz(mContext),
                    mSwitch,
                    R.layout.dialog_switch_info);
            infoDialog.setIdx(String.valueOf(mSwitch.getIdx()));
            try {
                if (mSwitch != null && !UsefulBits.isEmpty(mSwitch.getSubType()))
                    infoDialog.setColorLight(mSwitch.getSubType().startsWith(DomoticzValues.Device.SubType.Name.RGB) || mSwitch.getSubType().startsWith(DomoticzValues.Device.SubType.Name.WW));
            } catch (Exception ex) {
            }
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
        } else if (idx == -9999) {
            StaticHelper.getDomoticz(mContext).getSunRise(new SunRiseReceiver() {
                @Override
                public void onReceive(SunRiseInfo mSunRiseInfo) {
                    (new SunriseInfoDialog(mContext, mSunRiseInfo)).show();
                }

                @Override
                public void onError(Exception error) {
                }
            });
        }
    }

    private void changeFavorite(final DevicesInfo mSwitch, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mSwitch.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            UsefulBits.showSnackbar(mContext, frameLayout, mSwitch.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
        } else {
            UsefulBits.showSnackbar(mContext, frameLayout, mSwitch.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        if (mSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || mSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENEFAVORITE;
        }

        StaticHelper.getDomoticz(mContext).setAction(mSwitch.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                successHandling(result, false);
                processDashboard();
            }

            @Override
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, frameLayout, R.string.error_favorite, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_favorite);
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
                    mContext, StaticHelper.getDomoticz(mContext));
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override

                public void onDismiss(String password) {
                    toggleSwitch(clickedSwitch, checked, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            toggleSwitch(clickedSwitch, checked, null);
        }
    }

    private void toggleSwitch(final DevicesInfo clickedSwitch, final boolean checked, final String password) {
        if (checked) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(mContext.getString(R.string.switch_on));
        } else {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(mContext.getString(R.string.switch_off));
        }

        int idx = clickedSwitch.getIdx();
        if (clickedSwitch.getIdx() > 0) {
            int jsonAction;
            int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

            if (clickedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || clickedSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
                jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
                if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
                else jsonAction = DomoticzValues.Scene.Action.OFF;
            } else if (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                    clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
                    clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
                if (checked) jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                else jsonAction = DomoticzValues.Device.Switch.Action.ON;
            } else {
                if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
                else jsonAction = DomoticzValues.Device.Switch.Action.OFF;
            }

            StaticHelper.getDomoticz(mContext).setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                @Override

                public void onReceiveResult(String result) {
                    if (result.contains("WRONG CODE")) {
                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else {
                        successHandling(result, false);
                        clickedSwitch.setStatusBoolean(checked);
                        changeAdapterData(clickedSwitch);
                    }
                }

                @Override

                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password)) {
                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else {
                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                    }
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
                if (mExtendedStatusInfo.getType().equals(DomoticzValues.Scene.Type.GROUP) || mExtendedStatusInfo.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
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
                    mContext, StaticHelper.getDomoticz(mContext));
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override

                public void onDismiss(String password) {
                    toggleButton(clickedSwitch, checked, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else
            toggleButton(clickedSwitch, checked, null);
    }

    private void toggleButton(final DevicesInfo clickedSwitch, final boolean checked, final String password) {
        if (checked) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_on);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.switch_on) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_off);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.switch_off) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
        }

        int idx = clickedSwitch.getIdx();
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
        else jsonAction = DomoticzValues.Device.Switch.Action.OFF;

        if (clickedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || clickedSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
            else jsonAction = DomoticzValues.Scene.Action.OFF;
        }

        StaticHelper.getDomoticz(mContext).setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                if (result.contains("WRONG CODE")) {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } else {
                    successHandling(result, false);
                    clickedSwitch.setStatusBoolean(checked);
                    changeAdapterData(clickedSwitch);
                    //processDashboard();
                }
            }

            @Override

            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password)) {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } else {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                }
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
        if (getDevice(idx).getSubType().contains(DomoticzValues.Device.SubType.Name.WW) && getDevice(idx).getSubType().contains(DomoticzValues.Device.SubType.Name.RGB)) {
            RGBWWColorPickerDialog colorDialog = new RGBWWColorPickerDialog(mContext,
                    getDevice(idx).getIdx());
            colorDialog.show();
            colorDialog.onDismissListener(new RGBWWColorPickerDialog.DismissListener() {
                @Override
                public void onDismiss(final int value, final boolean isRGB) {
                    if (getDevice(idx).isProtected()) {
                        PasswordDialog passwordDialog = new PasswordDialog(
                                mContext, StaticHelper.getDomoticz(mContext));
                        passwordDialog.show();
                        passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                            @Override
                            public void onDismiss(String password) {
                                if (!isRGB)
                                    setKelvinColor(value, idx, password, true);
                                else
                                    setRGBColor(value, idx, password, true);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                    } else {
                        if (!isRGB)
                            setKelvinColor(value, idx, null, true);
                        else
                            setRGBColor(value, idx, null, true);
                    }
                }

                @Override
                public void onChangeRGBColor(int color) {
                    if (!getDevice(idx).isProtected())
                        setRGBColor(color, idx, null, false);
                }

                @Override
                public void onChangeKelvinColor(int kelvin) {
                    if (!getDevice(idx).isProtected())
                        setKelvinColor(kelvin, idx, null, false);
                }
            });
        } else if (getDevice(idx).getSubType().startsWith(DomoticzValues.Device.SubType.Name.WW)) {
            WWColorPickerDialog colorDialog = new WWColorPickerDialog(mContext,
                    getDevice(idx).getIdx());
            colorDialog.show();
            colorDialog.onDismissListener(new WWColorPickerDialog.DismissListener() {
                @Override
                public void onDismiss(final int kelvin) {
                    if (getDevice(idx).isProtected()) {
                        PasswordDialog passwordDialog = new PasswordDialog(
                                mContext, StaticHelper.getDomoticz(mContext));
                        passwordDialog.show();
                        passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                            @Override
                            public void onDismiss(String password) {
                                setKelvinColor(kelvin, idx, password, true);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                    } else
                        setKelvinColor(kelvin, idx, null, true);
                }

                @Override
                public void onChangeColor(int kelvin) {
                    if (!getDevice(idx).isProtected())
                        setKelvinColor(kelvin, idx, null, false);
                }
            });
        } else {
            ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(getContext());
            builder.setTitle(getString(R.string.choose_color));
            builder.setPositiveButton(getString(R.string.ok), new ColorEnvelopeListener() {
                @Override
                public void onColorSelected(final ColorEnvelope envelope, boolean fromUser) {
                    if (getDevice(idx).isProtected()) {
                        PasswordDialog passwordDialog = new PasswordDialog(
                                mContext, StaticHelper.getDomoticz(mContext));
                        passwordDialog.show();
                        passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                            @Override
                            public void onDismiss(String password) {
                                setRGBColor(envelope.getColor(), idx, password, true);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                    } else
                        setRGBColor(envelope.getColor(), idx, null, true);
                }
            });
            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    private void setKelvinColor(int kelvin, final int idx, final String password, final boolean selected) {
        StaticHelper.getDomoticz(mContext).setAction(idx,
                DomoticzValues.Json.Url.Set.KELVIN,
                DomoticzValues.Device.Dimmer.Action.KELVIN,
                kelvin,
                password,
                new setCommandReceiver() {
                    @Override

                    public void onReceiveResult(String result) {
                        if (result.contains("WRONG CODE")) {
                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                        } else {
                            if (selected) {
                                UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.color_set) + ": " + getDevice(idx).getName(), Snackbar.LENGTH_SHORT);
                                if (getActivity() instanceof MainActivity)
                                    ((MainActivity) getActivity()).Talk(R.string.color_set);
                            }
                        }
                    }

                    @Override

                    public void onError(Exception error) {
                        if (selected) {
                            if (!UsefulBits.isEmpty(password)) {
                                UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                if (getActivity() instanceof MainActivity)
                                    ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                            } else {
                                UsefulBits.showSnackbar(mContext, frameLayout, R.string.error_color, Snackbar.LENGTH_SHORT);
                                if (getActivity() instanceof MainActivity)
                                    ((MainActivity) getActivity()).Talk(R.string.error_color);
                            }
                        }
                    }
                });
    }

    private void setRGBColor(int selectedColor, final int idx, final String password, final boolean selected) {
        double[] hsv = UsefulBits.rgb2hsv(Color.red(selectedColor), Color.green(selectedColor), Color.blue(selectedColor));
        if (hsv == null || hsv.length <= 0)
            return;

        if (selected) {
            Log.v(TAG, "Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2] + " color: " + selectedColor);
            addDebugText("Selected HVS Color: h:" + hsv[0] + " v:" + hsv[1] + " s:" + hsv[2] + " color: " + selectedColor);
        }

        boolean isWhite = false;
        long hue = Math.round(hsv[0]);
        if (selectedColor == -1) {
            isWhite = true;
        }
        StaticHelper.getDomoticz(mContext).setRGBColorAction(idx,
                DomoticzValues.Json.Url.Set.RGBCOLOR,
                hue,
                getDevice(idx).getLevel(),
                isWhite,
                password,
                new setCommandReceiver() {
                    @Override

                    public void onReceiveResult(String result) {
                        if (selected) {
                            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.color_set) + ": " + getDevice(idx).getName(), Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.color_set);
                        }
                    }

                    @Override

                    public void onError(Exception error) {
                        if (selected) {
                            if (!UsefulBits.isEmpty(password)) {
                                UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                if (getActivity() instanceof MainActivity)
                                    ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                            } else {
                                UsefulBits.showSnackbar(mContext, frameLayout, R.string.error_color, Snackbar.LENGTH_SHORT);
                                if (getActivity() instanceof MainActivity)
                                    ((MainActivity) getActivity()).Talk(R.string.error_color);
                            }
                        }
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
                    addDebugText("Set idx " + idx + " to " + newSetPoint);
                    if (dialogAction == DialogAction.POSITIVE) {
                        if (tempUtil.isProtected()) {
                            PasswordDialog passwordDialog = new PasswordDialog(
                                    mContext, StaticHelper.getDomoticz(mContext));
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(final String password) {
                                    int jsonUrl = DomoticzValues.Json.Url.Set.TEMP;
                                    int action = DomoticzValues.Device.Thermostat.Action.PLUS;
                                    if (newSetPoint < tempUtil.getSetPoint())
                                        action = DomoticzValues.Device.Thermostat.Action.MIN;
                                    StaticHelper.getDomoticz(mContext).setAction(idx, jsonUrl, action, newSetPoint, password,
                                            new setCommandReceiver() {
                                                @Override
                                                public void onReceiveResult(String result) {
                                                    if (result.contains("WRONG CODE")) {
                                                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                                        if (getActivity() instanceof MainActivity)
                                                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                                                    } else {
                                                        successHandling(result, false);
                                                        processDashboard();
                                                    }
                                                }

                                                @Override
                                                public void onError(Exception error) {
                                                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                                    if (getActivity() instanceof MainActivity)
                                                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                                                }
                                            });
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                        } else {
                            int jsonUrl = DomoticzValues.Json.Url.Set.TEMP;
                            int action = DomoticzValues.Device.Thermostat.Action.PLUS;
                            if (newSetPoint < tempUtil.getSetPoint())
                                action = DomoticzValues.Device.Thermostat.Action.MIN;
                            StaticHelper.getDomoticz(mContext).setAction(idx, jsonUrl, action, newSetPoint, null,
                                    new setCommandReceiver() {
                                        @Override
                                        public void onReceiveResult(String result) {
                                            if (result.contains("WRONG CODE")) {
                                                UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                                if (getActivity() instanceof MainActivity)
                                                    ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                                            } else {
                                                successHandling(result, false);
                                                processDashboard();
                                            }
                                        }

                                        @Override
                                        public void onError(Exception error) {
                                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                                            if (getActivity() instanceof MainActivity)
                                                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
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
                        addDebugText("Set idx " + idx + " to " + newSetPoint);

                        String params = "&setpoint=" + newSetPoint +
                                "&mode=" + PERMANENT_OVERRIDE;

                        // add query parameters
                        StaticHelper.getDomoticz(mContext).setDeviceUsed(idx, tempUtil.getName(), tempUtil.getDescription(), params, commandReceiver);
                    } else if (dialogAction == DialogAction.NEUTRAL && evohomeZone) {
                        addDebugText("Set idx " + idx + " to Auto");

                        String params = "&setpoint=" + newSetPoint +
                                "&mode=" + AUTO;

                        // add query parameters
                        StaticHelper.getDomoticz(mContext).setDeviceUsed(idx, tempUtil.getName(), tempUtil.getDescription(), params, commandReceiver);
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
                mContext, StaticHelper.getDomoticz(mContext),
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
                                    mContext, StaticHelper.getDomoticz(mContext));
                            passwordDialog.show();
                            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                @Override
                                public void onDismiss(String password) {
                                    setState(idx, stateIds[which], password);
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                        } else {
                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                        }
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
    public void onSelectorChange(int idx, int level) {
        onDimmerChange(idx, level, true);
    }

    @Override

    public void onItemClicked(View v, int position) {
    }

    @Override

    public boolean onItemLongClicked(int idx) {
        showInfoDialog(getDevice(idx), idx);
        return true;
    }

    @Override
    public void onCameraFullScreenClick(int idx, String name) {
        CameraUtil.ProcessImage(mContext, idx, name);
    }

    private void setState(final int idx, int state, final String password) {
        StaticHelper.getDomoticz(mContext).setModalAction(idx,
                state,
                1,
                password,
                new setCommandReceiver() {
                    @Override

                    public void onReceiveResult(String result) {
                        UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.state_set) + ": " + getSwitch(idx).getName(), Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.state_set);
                        processDashboard();
                    }

                    @Override

                    public void onError(Exception error) {
                        if (!UsefulBits.isEmpty(password)) {
                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                        } else {
                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                        }
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
        addDebugText("Set idx " + idx + " to " + jsonAction);
        final DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, StaticHelper.getDomoticz(mContext));
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override

                public void onDismiss(String password) {
                    setBlindState(clickedSwitch, jsonAction, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            setBlindState(clickedSwitch, jsonAction, null);
        }
    }

    private void setBlindState(final DevicesInfo clickedSwitch, final int jsonAction, final String password) {
        if ((jsonAction == DomoticzValues.Device.Blind.Action.UP || jsonAction == DomoticzValues.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() != DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_up);
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.OPEN);
        } else if ((jsonAction == DomoticzValues.Device.Blind.Action.DOWN || jsonAction == DomoticzValues.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() != DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.CLOSED);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_down);
        } else if ((jsonAction == DomoticzValues.Device.Blind.Action.UP || jsonAction == DomoticzValues.Device.Blind.Action.OFF) && (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.CLOSED);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.blind_down) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_down);
        } else if ((jsonAction == DomoticzValues.Device.Blind.Action.DOWN || jsonAction == DomoticzValues.Device.Blind.Action.ON) && (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDINVERTED)) {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.OPEN);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.blind_up) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_up);
        } else {
            clickedSwitch.setStatus(DomoticzValues.Device.Blind.State.STOPPED);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.blind_stop) + ": " + clickedSwitch.getName(), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.blind_stop);
        }

        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
        StaticHelper.getDomoticz(mContext).setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                if (result.contains("WRONG CODE")) {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } else {
                    successHandling(result, false);
                    //processDashboard();
                    changeAdapterData(clickedSwitch);
                }
            }

            @Override

            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password)) {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } else {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                }
            }
        });
    }

    private void changeAdapterData(DevicesInfo clickedSwitch) {
        // Only when not showing the extra data on dashboard
        // When extra data is on, more info has to be changed other than the status
        try {
            // Let's find out where the clicked switch is in the list
            int index = extendedStatusSwitches.indexOf(clickedSwitch);

            // Add it back into the array list
            extendedStatusSwitches.set(index, clickedSwitch);

            // Clear the data in the adapter and add all switches back in
            adapter.data.clear();
            adapter.data.addAll(extendedStatusSwitches);

            // Notify the adapter the data has changed
            adapter.notifyDataSetChanged();
        } catch (Exception ex) {
        }
    }

    @Override

    public void onDimmerChange(int idx, final int value, final boolean selector) {
        if (busy)
            return;

        addDebugText("onDimmerChange for " + idx + " to " + value);
        final DevicesInfo clickedSwitch = getDevice(idx);
        if (clickedSwitch.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, StaticHelper.getDomoticz(mContext));
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override

                public void onDismiss(String password) {
                    setDimmerState(clickedSwitch, value, selector, password);
                }

                @Override
                public void onCancel() {
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
                    !selector ? (value) : ((value) / 10) + 1);
            UsefulBits.showSnackbar(mContext, frameLayout, text, Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(text);

            int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
            int jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;

            StaticHelper.getDomoticz(mContext).setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, !selector ? (value) : (value) + 10, password, new setCommandReceiver() {
                @Override

                public void onReceiveResult(String result) {
                    if (result.contains("WRONG CODE")) {
                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else {
                        successHandling(result, false);
                        if (selector)
                            processDashboard();
                    }
                }

                @Override

                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password)) {
                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                    } else {
                        UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                    }
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
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                super.errorHandling(error);
            }
        }
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.i("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
        String[] neededPermission = PermissionFragmentHelper.declinedPermissions(this, PermissionsUtil.INITIAL_STORAGE_PERMS);
        StringBuilder builder = new StringBuilder(neededPermission.length);
        if (neededPermission.length > 0) {
            for (String permission : neededPermission) {
                builder.append(permission).append("\n");
            }
        }
        AlertDialog alert = PermissionsUtil.getAlertDialog(getActivity(), permissionFragmentHelper, getActivity().getString(R.string.permission_title),
                getActivity().getString(R.string.permission_desc_storage), neededPermission);
        if (!alert.isShowing()) {
            alert.show();
        }
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.i("onPermissionPreGranted", "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        Log.i("NeedExplanation", "Permission( " + permissionName + " ) needs Explanation");
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.i("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.i("onNoPermissionNeeded", "Permission(s) not needed");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionFragmentHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionFragmentHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<DevicesInfo> cacheSwitches = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (mContext == null)
                return false;
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(mContext);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheSwitches = (ArrayList<DevicesInfo>) SerializableManager.readSerializedObject(mContext, "Dashboard");
                } catch (Exception ignored) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (mContext == null)
                return;
            if (cacheSwitches != null)
                processDevices(cacheSwitches);

            StaticHelper.getDomoticz(mContext).getDevices(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                    SerializableManager.saveSerializable(mContext, switches, "Dashboard");
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
    }
}