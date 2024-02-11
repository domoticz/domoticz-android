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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.ItemTouchHelper;

import com.afollestad.materialdialogs.DialogAction;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.UtilityAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.SimpleItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.UtilityClickListener;
import nl.hnogames.domoticz.ui.PasswordDialog;
import nl.hnogames.domoticz.ui.SwitchLogInfoDialog;
import nl.hnogames.domoticz.ui.TemperatureDialog;
import nl.hnogames.domoticz.ui.UtilitiesInfoDialog;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.SwitchLogInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticzapi.Interfaces.UtilitiesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class Utilities extends DomoticzRecyclerFragment implements DomoticzFragmentListener,
        UtilityClickListener {

    private ArrayList<UtilitiesInfo> mUtilitiesInfos;
    private double thermostatSetPointValue;
    private UtilityAdapter adapter;
    private Context mContext;
    private boolean itemDecorationAdded = false;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onConnectionFailed() {
        processUtilities();
    }

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUtilities();
    }

    @Override
    public void onDestroyView() {
        if (adapter != null)
            adapter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        setActionbar(getString(R.string.title_utilities));
        setSortFab(false);
        initAnimation();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    private ArrayList<UtilitiesInfo> AddAdsDevice(ArrayList<UtilitiesInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                ArrayList<UtilitiesInfo> filteredList = new ArrayList<>();
                for (UtilitiesInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                UtilitiesInfo adView = new UtilitiesInfo();
                adView.setIdx(MainActivity.ADS_IDX);
                adView.setName("Ads");
                adView.setType("advertisement");
                adView.setFavoriteBoolean(true);
                filteredList.add(1, adView);
                return filteredList;
            }
        } catch (Exception ex) {
        }
        return supportedSwitches;
    }

    @Override
    public void Filter(String text) {
        filter = text;
        try {
            if (adapter != null) {
                if (UsefulBits.isEmpty(text) &&
                        (UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) &&
                        mSharedPrefs.enableCustomSorting() && !mSharedPrefs.isCustomSortingLocked()) {
                    if (mItemTouchHelper == null) {
                        mItemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(adapter, false));
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
        processUtilities();
    }

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        animHide = AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    private void processUtilities() {
        try {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);

            GetUtilities();
        } catch (Exception ex) {
        }
    }

    private void createListView() {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new UtilityAdapter(mContext, StaticHelper.getDomoticz(mContext), AddAdsDevice(mUtilitiesInfos), this);
                gridView.setAdapter(adapter);
            } else {
                adapter.setData(AddAdsDevice(mUtilitiesInfos));
                adapter.notifyDataSetChanged();
            }

            if (mItemTouchHelper == null) {
                mItemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(adapter, isTablet));
            }
            if ((UsefulBits.isEmpty(super.getSort()) || super.getSort().equals(mContext.getString(R.string.filterOn_all))) &&
                    mSharedPrefs.enableCustomSorting() && !mSharedPrefs.isCustomSortingLocked()) {
                mItemTouchHelper.attachToRecyclerView(gridView);
            } else {
                if (mItemTouchHelper != null)
                    mItemTouchHelper.attachToRecyclerView(null);
            }
            if (!isTablet && !itemDecorationAdded) {
                gridView.addItemDecoration(new MarginItemDecoration(20));
                itemDecorationAdded = true;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(() -> processUtilities());

            super.showSpinner(false);
            this.Filter(filter);
        }
    }

    private void showInfoDialog(final UtilitiesInfo mUtilitiesInfo) {
        UtilitiesInfoDialog infoDialog = new UtilitiesInfoDialog(
                mContext,
                mUtilitiesInfo,
                R.layout.dialog_utilities_info);
        infoDialog.setIdx(String.valueOf(mUtilitiesInfo.getIdx()));
        infoDialog.setLastUpdate(mUtilitiesInfo.getLastUpdate());
        infoDialog.setIsFavorite(mUtilitiesInfo.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener((isChanged, isFavorite) -> {
            if (isChanged) changeFavorite(mUtilitiesInfo, isFavorite);
        });
    }

    private void changeFavorite(final UtilitiesInfo mUtilitiesInfo, final boolean isFavorite) {
        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
        if (user != null && user.getRights() <= 1) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            refreshFragment();
            return;
        }
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mUtilitiesInfo.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
            UsefulBits.showSnackbar(mContext, frameLayout, mUtilitiesInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, frameLayout, mUtilitiesInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        StaticHelper.getDomoticz(mContext).setAction(mUtilitiesInfo.getIdx(),
                jsonUrl,
                jsonAction,
                0,
                null,
                new setCommandReceiver() {
                    @Override

                    public void onReceiveResult(String result) {
                        successHandling(result, false);
                        mUtilitiesInfo.setFavoriteBoolean(isFavorite);
                    }

                    @Override

                    public void onError(Exception error) {
                        errorHandling(error);
                    }
                });
    }

    /**
     * Updates the set point in the Utilities container
     *
     * @param idx         ID of the utility to be changed
     * @param newSetPoint The new set point value
     */
    private void updateThermostatSetPointValue(int idx, double newSetPoint) {
        addDebugText("updateThermostatSetPointValue");

        for (UtilitiesInfo info : mUtilitiesInfos) {
            if (info.getIdx() == idx) {
                info.setSetPoint(newSetPoint);
                break;
            }
        }

        notifyDataSetChanged();
    }

    private void updateThermostatModeValue(int idx, int newMode) {
        addDebugText("updateThermostatModeValue");

        for (UtilitiesInfo info : mUtilitiesInfos) {
            if (info.getIdx() == idx) {
                info.setModeId(newMode);
                break;
            }
        }

        notifyDataSetChanged();
    }

    /**
     * Notifies the list view adapter the data has changed and refreshes the list view
     */
    private void notifyDataSetChanged() {
        addDebugText("notifyDataSetChanged");
        adapter.notifyDataSetChanged();
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

    public void onPause() {
        super.onPause();
    }

    private UtilitiesInfo getUtility(int idx) {
        for (UtilitiesInfo info : mUtilitiesInfos) {
            if (info.getIdx() == idx) {
                return info;
            }
        }
        return null;
    }

    @Override

    public void onClick(UtilitiesInfo utility) {
    }

    @Override
    public void OnModeChanged(UtilitiesInfo utility, int mode, String modeName) {
        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
        if (user != null && user.getRights() <= 0) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            refreshFragment();
            return;
        }

        addDebugText("OnModeChanged");
        addDebugText("Set idx " + utility.getIdx() + " to " + modeName);

        if (utility.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    mContext, StaticHelper.getDomoticz(mContext));
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                public void onDismiss(String password) {
                    SetThermostatMode(utility, mode, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            SetThermostatMode(utility, mode, null);
        }
    }

    private void SetThermostatMode(UtilitiesInfo utility, int mode, String password) {
        StaticHelper.getDomoticz(mContext).setAction(utility.getIdx(),
                DomoticzValues.Json.Url.Set.THERMOSTAT,
                DomoticzValues.Device.Thermostat.Action.TMODE,
                mode,
                password,
                new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        if (result.contains("WRONG CODE")) {
                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                        } else {
                            updateThermostatModeValue(utility.getIdx(), mode);
                            successHandling(result, false);
                        }
                    }

                    @Override

                    public void onError(Exception error) {
                        errorHandling(error);
                    }
                });
    }

    @Override
    public void onLogClick(final UtilitiesInfo utility, final String range) {
        int steps = 2;
        String graphType = utility.getSubType()
                .replace("Electric", "counter")
                .replace("kWh", "counter")
                .replace("Gas", "counter")
                .replace("Energy", "counter")
                .replace("Voltcraft", "counter")
                .replace("Voltage", "counter")
                .replace("SetPoint", "temp")
                .replace("Lux", "counter")
                .replace("BWR102", "counter")
                .replace("Sound Level", "counter")
                .replace("Moisture", "counter")
                .replace("Managed Counter", "counter")
                .replace("Pressure", "counter")
                .replace("Custom Sensor", "Percentage")
                .replace("YouLess counter", "counter");

        if (graphType.toLowerCase().contains("counter"))
            graphType = "counter";
        if (utility.getSubType().equals("Gas"))
            steps = 1;

        Intent intent = new Intent(mContext, GraphActivity.class);
        intent.putExtra("IDX", utility.getIdx());
        intent.putExtra("RANGE", range);
        intent.putExtra("TYPE", graphType);
        intent.putExtra("TITLE", utility.getSubType().toUpperCase());
        intent.putExtra("STEPS", steps);
        startActivity(intent);
    }

    @Override
    public void onThermostatClick(final int idx) {
        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
        if (user != null && user.getRights() <= 0) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            refreshFragment();
            return;
        }

        addDebugText("onThermostatClick");
        final UtilitiesInfo tempUtil = getUtility(idx);

        TemperatureDialog tempDialog = new TemperatureDialog(mContext, tempUtil.getSetPoint(), tempUtil.hasStep(),
                tempUtil.getStep(), tempUtil.hasMax(), tempUtil.getMax(), tempUtil.hasMin(), tempUtil.getMin(), tempUtil.getVUnit());
        tempDialog.onDismissListener((newSetPoint, dialogAction) -> {
            if (dialogAction == DialogAction.POSITIVE) {
                addDebugText("Set idx " + idx + " to " + newSetPoint);
                if (tempUtil != null) {
                    if (tempUtil.isProtected()) {
                        PasswordDialog passwordDialog = new PasswordDialog(
                                mContext, StaticHelper.getDomoticz(mContext));
                        passwordDialog.show();
                        passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                            @Override
                            public void onDismiss(String password) {
                                setThermostatAction(tempUtil, newSetPoint, password);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                    } else {
                        setThermostatAction(tempUtil, newSetPoint, null);
                    }
                }
            } else {
                addDebugText("Not updating idx " + idx);
            }
        });
        tempDialog.show();
    }

    public void setThermostatAction(final UtilitiesInfo tempUtil,
                                    double newSetPoint,
                                    String password) {
        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
        if (user != null && user.getRights() <= 0) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            refreshFragment();
            return;
        }

        thermostatSetPointValue = newSetPoint;
        int jsonUrl = DomoticzValues.Json.Url.Set.TEMP;

        int action = DomoticzValues.Device.Thermostat.Action.PLUS;
        if (newSetPoint < tempUtil.getSetPoint())
            action = DomoticzValues.Device.Thermostat.Action.MIN;

        StaticHelper.getDomoticz(mContext).setAction(tempUtil.getIdx(),
                jsonUrl,
                action,
                newSetPoint,
                password,
                new setCommandReceiver() {
                    @Override

                    public void onReceiveResult(String result) {
                        if (result.contains("WRONG CODE")) {
                            UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                        } else {
                            updateThermostatSetPointValue(tempUtil.getIdx(), thermostatSetPointValue);
                            successHandling(result, false);
                        }
                    }

                    @Override

                    public void onError(Exception error) {
                        errorHandling(error);
                    }
                });
    }


    @Override

    public void onLogButtonClick(int idx) {


        StaticHelper.getDomoticz(mContext).getTextLogs(idx, new SwitchLogReceiver() {
            @Override

            public void onReceiveSwitches(ArrayList<SwitchLogInfo> switchesLogs) {
                showLogDialog(switchesLogs);
            }

            @Override

            public void onError(Exception error) {
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_logs);
                UsefulBits.showSnackbar(mContext, frameLayout, R.string.error_logs, Snackbar.LENGTH_SHORT);
            }
        });
    }

    @Override

    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getUtility(idx), checked);
    }

    @Override

    public void onItemClicked(View v, int position) {
        LinearLayout extra_panel = v.findViewById(R.id.extra_panel);
        if (extra_panel != null) {
            if (extra_panel.getVisibility() == View.VISIBLE) {
                extra_panel.startAnimation(animHide);
                extra_panel.setVisibility(View.GONE);
            } else {
                extra_panel.setVisibility(View.VISIBLE);
                extra_panel.startAnimation(animShow);
            }

            if (extra_panel != lExtraPanel) {
                if (lExtraPanel != null) {
                    if (lExtraPanel.getVisibility() == View.VISIBLE) {
                        lExtraPanel.startAnimation(animHide);
                        lExtraPanel.setVisibility(View.GONE);
                    }
                }
            }

            lExtraPanel = extra_panel;
        }
    }

    @Override

    public boolean onItemLongClicked(int idx) {
        showInfoDialog(getUtility(idx));
        return true;
    }

    private void showLogDialog(ArrayList<SwitchLogInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(mContext, "No logs found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchLogInfoDialog infoDialog = new SwitchLogInfoDialog(
                    mContext,
                    switchLogs,
                    R.layout.dialog_switch_logs);
            infoDialog.show();
        }
    }

    public void GetUtilities() {
        StaticHelper.getDomoticz(mContext).getUtilities(new UtilitiesReceiver() {
            @Override

            public void onReceiveUtilities(ArrayList<UtilitiesInfo> mUtilitiesInfos) {
                successHandling(mUtilitiesInfos.toString(), false);
                SerializableManager.saveSerializable(mContext, mUtilitiesInfos, "Utilities");
                Utilities.this.mUtilitiesInfos = mUtilitiesInfos;

                createListView();
            }

            @Override

            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }
}