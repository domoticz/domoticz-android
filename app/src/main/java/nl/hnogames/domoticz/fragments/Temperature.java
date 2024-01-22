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
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.ItemTouchHelper;

import com.afollestad.materialdialogs.DialogAction;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.TemperatureAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.SimpleItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.TemperatureClickListener;
import nl.hnogames.domoticz.ui.ScheduledTemperatureDialog;
import nl.hnogames.domoticz.ui.TemperatureDialog;
import nl.hnogames.domoticz.ui.TemperatureInfoDialog;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.TemperatureInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.TemperatureReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class Temperature extends DomoticzRecyclerFragment implements DomoticzFragmentListener, TemperatureClickListener {

    public static final String AUTO = "Auto";
    public static final String PERMANENT_OVERRIDE = "PermanentOverride";
    public static final String TEMPORARY_OVERRIDE = "TemporaryOverride";

    @SuppressWarnings("unused")
    private static final String TAG = Temperature.class.getSimpleName();
    private Context mContext;
    private TemperatureAdapter adapter;
    private boolean itemDecorationAdded = false;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private ArrayList<TemperatureInfo> mTempInfos;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processTemperature();
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
        initAnimation();
        setActionbar(getString(R.string.title_temperature));
        setSortFab(false);
    }

    private ArrayList<TemperatureInfo> AddAdsDevice(ArrayList<TemperatureInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                ArrayList<TemperatureInfo> filteredList = new ArrayList<>();
                for (TemperatureInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                TemperatureInfo adView = new TemperatureInfo();
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
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
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
        processTemperature();
    }

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        animHide = AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    private void processTemperature() {
        try {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);

            new GetCachedDataTask().execute();
        } catch (Exception ex) {
        }
    }

    private void createListView(ArrayList<TemperatureInfo> mTemperatureInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new TemperatureAdapter(mContext, StaticHelper.getDomoticz(mContext), getServerUtil(), AddAdsDevice(mTemperatureInfos), this);
                gridView.setAdapter(adapter);
            } else {
                adapter.setData(AddAdsDevice(mTemperatureInfos));
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
            mSwipeRefreshLayout.setOnRefreshListener(() -> processTemperature());
            this.Filter(filter);
        }
        super.showSpinner(false);
    }

    private void showInfoDialog(final TemperatureInfo mTemperatureInfo) {
        TemperatureInfoDialog infoDialog = new TemperatureInfoDialog(
                mContext,
                mTemperatureInfo,
                R.layout.dialog_utilities_info);
        infoDialog.setIdx(String.valueOf(mTemperatureInfo.getIdx()));
        infoDialog.setLastUpdate(mTemperatureInfo.getLastUpdate());
        infoDialog.setIsFavorite(mTemperatureInfo.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener((isChanged, isFavorite) -> {
            if (isChanged)
                changeFavorite(mTemperatureInfo, isFavorite);
        });
    }

    private void changeFavorite(final TemperatureInfo mTemperatureInfo, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mTemperatureInfo.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
            UsefulBits.showSnackbar(mContext, frameLayout, mTemperatureInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, frameLayout, mTemperatureInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        StaticHelper.getDomoticz(mContext).setAction(mTemperatureInfo.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                successHandling(result, false);
                mTemperatureInfo.setFavoriteBoolean(isFavorite);
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

    @Override
    public void onLogClick(final TemperatureInfo temp, final String range) {
        Intent intent = new Intent(mContext, GraphActivity.class);
        intent.putExtra("IDX", temp.getIdx());
        intent.putExtra("RANGE", range);
        intent.putExtra("TYPE", "temp");
        intent.putExtra("STEPS", 3);
        startActivity(intent);
    }

    @Override
    public void onSetClick(final TemperatureInfo t) {
        addDebugText("onSetClick");
        final int idx = t.getIdx();

        final setCommandReceiver commandReceiver = new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                processTemperature();
            }

            @Override
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_no_rights, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            }
        };

        final boolean evohomeZone = "evohome".equals(t.getHardwareName());
        TemperatureDialog tempDialog;
        if (evohomeZone) {
            tempDialog = new ScheduledTemperatureDialog(
                    mContext,
                    t.getSetPoint(), t.hasStep(),
                    t.getStep(), t.hasMax(), t.getMax(), t.hasMin(), t.getMin(),
                    !AUTO.equalsIgnoreCase(t.getStatus()), t.getVUnit());
        } else {
            tempDialog = new TemperatureDialog(mContext, t.getSetPoint(), t.hasStep(),
                    t.getStep(), t.hasMax(), t.getMax(), t.hasMin(), t.getMin(), t.getVUnit());
        }

        tempDialog.onDismissListener((newSetPoint, dialogAction) -> {
            if (dialogAction == DialogAction.POSITIVE) {
                addDebugText("Set idx " + idx + " to " + newSetPoint);
                String params = "&setpoint=" + newSetPoint +
                        "&mode=" + PERMANENT_OVERRIDE;
                // add query parameters
                StaticHelper.getDomoticz(mContext).setDeviceUsed(idx, t.getName(), t.getDescription(), params, commandReceiver);
            } else if (dialogAction == DialogAction.NEUTRAL && evohomeZone) {
                addDebugText("Set idx " + idx + " to Auto");
                String params = "&setpoint=" + newSetPoint +
                        "&mode=" + AUTO;
                // add query parameters
                StaticHelper.getDomoticz(mContext).setDeviceUsed(idx, t.getName(), t.getDescription(), params, commandReceiver);
            } else {
                addDebugText("Not updating idx " + idx);
            }
        });

        tempDialog.show();
    }

    @Override
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getTemperature(idx), checked);
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
        showInfoDialog(getTemperature(idx));
        return true;
    }

    private TemperatureInfo getTemperature(int idx) {
        TemperatureInfo clickedTemp = null;
        for (TemperatureInfo mTempInfo : mTempInfos) {
            if (mTempInfo.getIdx() == idx) {
                clickedTemp = mTempInfo;
            }
        }
        return clickedTemp;
    }


    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<TemperatureInfo> cacheTemperatures = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (mContext == null)
                return false;
            try {
                cacheTemperatures = (ArrayList<TemperatureInfo>) SerializableManager.readSerializedObject(mContext, "Temperatures");
            } catch (Exception ignored) {
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (mContext == null)
                return;
            if (cacheTemperatures != null)
                createListView(cacheTemperatures);

            StaticHelper.getDomoticz(mContext).getTemperatures(new TemperatureReceiver() {
                @Override

                public void onReceiveTemperatures(ArrayList<TemperatureInfo> mTemperatureInfos) {
                    mTempInfos = mTemperatureInfos;
                    successHandling(mTemperatureInfos.toString(), false);
                    SerializableManager.saveSerializable(mContext, mTemperatureInfos, "Temperatures");
                    createListView(mTemperatureInfos);
                }

                @Override

                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }
}