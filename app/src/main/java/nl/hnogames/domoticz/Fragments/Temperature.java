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

package nl.hnogames.domoticz.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.TemperatureAdapter;
import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.TemperatureClickListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.ScheduledTemperatureDialog;
import nl.hnogames.domoticz.UI.TemperatureDialog;
import nl.hnogames.domoticz.UI.TemperatureInfoDialog;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
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
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private ArrayList<TemperatureInfo> mTempInfos;
    private SlideInBottomAnimationAdapter alphaSlideIn;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

    @Override
    @DebugLog
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        processTemperature();
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_temperature);
        initAnimation();
    }

    @Override
    @DebugLog
    public void Filter(String text) {
        filter = text;
        try {
            if (adapter != null)
                adapter.getFilter().filter(text);
            super.Filter(text);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    @DebugLog
    public void onConnectionOk() {
        super.showSpinner(true);
        processTemperature();
    }

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        animHide = AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    private void processTemperature() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        new GetCachedDataTask().execute();
    }

    private void createListView(ArrayList<TemperatureInfo> mTemperatureInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new TemperatureAdapter(mContext, mDomoticz, getServerUtil(), mTemperatureInfos, this);
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
            } else {
                adapter.setData(mTemperatureInfos);
                adapter.notifyDataSetChanged();
                alphaSlideIn.notifyDataSetChanged();
            }

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                @DebugLog
                public void onRefresh() {
                    processTemperature();
                }
            });
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
        infoDialog.onDismissListener(new TemperatureInfoDialog.DismissListener() {
            @Override
            @DebugLog
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged)
                    changeFavorite(mTemperatureInfo, isFavorite);
            }
        });
    }

    private void changeFavorite(final TemperatureInfo mTemperatureInfo, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mTemperatureInfo.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mTemperatureInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mTemperatureInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        mDomoticz.setAction(mTemperatureInfo.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mTemperatureInfo.setFavoriteBoolean(isFavorite);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    @Override
    @DebugLog
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
    @DebugLog
    public void onPause() {
        super.onPause();
    }

    @Override
    @DebugLog
    public void onLogClick(final TemperatureInfo temp, final String range) {
        Intent intent = new Intent(mContext, GraphActivity.class);
        intent.putExtra("IDX", temp.getIdx());
        intent.putExtra("RANGE", range);
        intent.putExtra("TYPE", "temp");
        intent.putExtra("STEPS", 3);
        startActivity(intent);
    }

    @Override
    @DebugLog
    public void onSetClick(final TemperatureInfo t) {
        addDebugText("onSetClick");
        final int idx = t.getIdx();

        final setCommandReceiver commandReceiver = new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                successHandling(result, false);
                processTemperature();
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                errorHandling(error);
            }
        };

        final boolean evohomeZone = "evohome".equals(t.getHardwareName());

        TemperatureDialog tempDialog;
        if (evohomeZone) {
            tempDialog = new ScheduledTemperatureDialog(
                mContext,
                t.getSetPoint(),
                !"auto".equalsIgnoreCase(t.getStatus()));
        } else {
            tempDialog = new TemperatureDialog(
                mContext,
                t.getSetPoint());
        }

        tempDialog.onDismissListener(new TemperatureDialog.DialogActionListener() {
            @Override
            @DebugLog
            public void onDialogAction(double newSetPoint, DialogAction dialogAction) {
                if (dialogAction == DialogAction.POSITIVE) {
                    addDebugText("Set idx " + idx + " to " + String.valueOf(newSetPoint));

                    String params = "&setpoint=" + String.valueOf(newSetPoint) +
                        "&mode=" + PERMANENT_OVERRIDE;

                    // add query parameters
                    mDomoticz.setDeviceUsed(idx, t.getName(), t.getDescription(), params, commandReceiver);
                } else if (dialogAction == DialogAction.NEUTRAL && evohomeZone) {
                    addDebugText("Set idx " + idx + " to Auto");

                    String params = "&setpoint=" + String.valueOf(newSetPoint) +
                        "&mode=" + AUTO;

                    // add query parameters
                    mDomoticz.setDeviceUsed(idx, t.getName(), t.getDescription(), params, commandReceiver);
                } else {
                    addDebugText("Not updating idx " + idx);
                }
            }
        });

        tempDialog.show();
    }

    @Override
    @DebugLog
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getTemperature(idx), checked);
    }

    @Override
    @DebugLog
    public void onItemClicked(View v, int position) {
        LinearLayout extra_panel = (LinearLayout) v.findViewById(R.id.extra_panel);
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
    @DebugLog
    public boolean onItemLongClicked(int position) {
        showInfoDialog(adapter.filteredData.get(position));
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
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheTemperatures = (ArrayList<TemperatureInfo>) SerializableManager.readSerializedObject(mContext, "Temperatures");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheTemperatures != null)
                createListView(cacheTemperatures);

            mDomoticz.getTemperatures(new TemperatureReceiver() {
                @Override
                @DebugLog
                public void onReceiveTemperatures(ArrayList<TemperatureInfo> mTemperatureInfos) {
                    mTempInfos = mTemperatureInfos;
                    successHandling(mTemperatureInfos.toString(), false);
                    SerializableManager.saveSerializable(mContext, mTemperatureInfos, "Temperatures");
                    createListView(mTemperatureInfos);
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }
}