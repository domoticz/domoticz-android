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
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.SceneAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.SimpleItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.ScenesClickListener;
import nl.hnogames.domoticz.ui.PasswordDialog;
import nl.hnogames.domoticz.ui.SceneInfoDialog;
import nl.hnogames.domoticz.ui.SwitchLogInfoDialog;
import nl.hnogames.domoticz.ui.SwitchTimerInfoDialog;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.utils.WidgetUtils;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Containers.SwitchLogInfo;
import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class Scenes extends DomoticzRecyclerFragment implements DomoticzFragmentListener,
        ScenesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Scenes.class.getSimpleName();
    private Context mContext;
    private SceneAdapter adapter;
    private ArrayList<SceneInfo> mScenes;
    private Parcelable state;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private boolean itemDecorationAdded = false;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onConnectionFailed() {
        GetScenes();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        initAnimation();
        setActionbar(getString(R.string.title_scenes));
        setSortFab(true);
    }

    @Override
    public void onDestroyView() {
        if (adapter != null)
            adapter.onDestroy();
        super.onDestroyView();
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

    private ArrayList<SceneInfo> AddAdsDevice(ArrayList<SceneInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                ArrayList<SceneInfo> filteredList = new ArrayList<>();
                for (SceneInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                SceneInfo adView = new SceneInfo();
                adView.setIdx(MainActivity.ADS_IDX);
                adView.setName("Ads");
                adView.setType("advertisement");
                adView.setDescription("Advertisement");
                adView.setFavoriteBoolean(true);
                filteredList.add(1, adView);
                return filteredList;
            }
        } catch (Exception ex) {
        }
        return supportedSwitches;
    }

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        animHide = AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    @Override

    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processScenes();
    }

    @Override

    public void onConnectionOk() {
        super.showSpinner(true);
        processScenes();
    }

    private void processScenes() {
        try {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);

            state = gridView.getLayoutManager().onSaveInstanceState();
            WidgetUtils.RefreshWidgets(mContext);
            GetScenes();
        } catch (Exception ignored) {
        }
    }

    public void createListView(final ArrayList<SceneInfo> scenes) {
        ArrayList<SceneInfo> supportedScenes = new ArrayList<>();
        if (getView() != null) {
            mScenes = scenes;

            final ScenesClickListener listener = this;

            for (SceneInfo s : scenes) {
                if (super.getSort().equals(null) || super.getSort().length() <= 0 || super.getSort().equals(getContext().getString(R.string.filterOn_all))) {
                    supportedScenes.add(s);
                } else {
                    if (mContext != null) {
                        //UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.filter_on) + ": " + super.getSort(), Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.filter_on);
                        if ((super.getSort().equals(getContext().getString(R.string.filterOn_on)) && s.getStatusInBoolean()) && StaticHelper.getDomoticz(mContext).isOnOffScene(s))
                            supportedScenes.add(s);
                        if ((super.getSort().equals(getContext().getString(R.string.filterOn_off)) && !s.getStatusInBoolean()) && StaticHelper.getDomoticz(mContext).isOnOffScene(s))
                            supportedScenes.add(s);
                        if ((super.getSort().equals(getContext().getString(R.string.filterOn_static))) && !StaticHelper.getDomoticz(mContext).isOnOffScene(s))
                            supportedScenes.add(s);
                    }
                }
            }

            if (adapter == null) {
                adapter = new SceneAdapter(mContext, StaticHelper.getDomoticz(mContext), AddAdsDevice(supportedScenes), listener);
                gridView.setAdapter(adapter);
            } else {
                adapter.setData(AddAdsDevice(supportedScenes));
                adapter.notifyDataSetChanged();
            }
            if (!isTablet && !itemDecorationAdded) {
                gridView.addItemDecoration(new MarginItemDecoration(20));
                itemDecorationAdded = true;
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

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override

                public void onRefresh() {
                    processScenes();
                }
            });

            if (state != null) {
                // Restore previous state (including selected item index and scroll position)
                gridView.getLayoutManager().onRestoreInstanceState(state);
            }
        }
        super.showSpinner(false);
        this.Filter(filter);

    }

    private SceneInfo getScene(int idx) {
        SceneInfo clickedScene = null;
        for (SceneInfo s : mScenes) {
            if (s.getIdx() == idx) {
                clickedScene = s;
            }
        }
        return clickedScene;
    }

    private void showInfoDialog(final SceneInfo mSceneInfo) {
        SceneInfoDialog infoDialog = new SceneInfoDialog(
                getActivity(),
                mSceneInfo,
                R.layout.dialog_scene_info);
        infoDialog.setIdx(String.valueOf(mSceneInfo.getIdx()));
        infoDialog.setLastUpdate(mSceneInfo.getLastUpdate());
        infoDialog.setIsFavorite(mSceneInfo.getFavoriteBoolean());
        infoDialog.show();
        infoDialog.onDismissListener(new SceneInfoDialog.DismissListener() {

            @Override

            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged) changeFavorite(mSceneInfo, isFavorite);
            }
        });
    }

    private void changeFavorite(final SceneInfo mSceneInfo, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mSceneInfo.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
            UsefulBits.showSnackbar(mContext, frameLayout, mSceneInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, frameLayout, mSceneInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SCENEFAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        StaticHelper.getDomoticz(mContext).setAction(mSceneInfo.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSceneInfo.setFavoriteBoolean(isFavorite);
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
    public void onSceneClick(int idx, final boolean action) {
        addDebugText("onSceneClick");
        addDebugText("Set " + idx + " to " + action);
        final SceneInfo clickedScene = getScene(idx);
        if (clickedScene.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    getActivity(), StaticHelper.getDomoticz(mContext));
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override

                public void onDismiss(String password) {
                    setScene(clickedScene, action, password);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            setScene(clickedScene, action, null);
        }
    }

    @Override
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getScene(idx), checked);
    }

    @Override

    public void onLogButtonClick(int idx) {
        StaticHelper.getDomoticz(mContext).getSceneLogs(idx, new SwitchLogReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchLogInfo> switchesLogs) {
                showLogDialog(switchesLogs);
            }

            @Override
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, frameLayout, R.string.error_logs, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_logs);
            }
        });
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
        showInfoDialog(getScene(idx));
        return true;
    }

    @Override
    public void onTimerButtonClick(int idx) {
        StaticHelper.getDomoticz(mContext).getSwitchTimers(idx, new SwitchTimerReceiver() {
            @Override

            public void onReceiveSwitchTimers(ArrayList<SwitchTimerInfo> switchTimers) {
                if (switchTimers != null)
                    showTimerDialog(switchTimers);
            }

            @Override
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, frameLayout, R.string.error_timer, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_timer);
            }
        }, true);
    }

    private void showTimerDialog(ArrayList<SwitchTimerInfo> switchLogs) {
        if (switchLogs.size() <= 0) {
            Toast.makeText(mContext, "No timer found.", Toast.LENGTH_LONG).show();
        } else {
            SwitchTimerInfoDialog infoDialog = new SwitchTimerInfoDialog(
                    mContext,
                    switchLogs,
                    R.layout.dialog_switch_logs);
            infoDialog.show();
        }
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

    public void setScene(SceneInfo clickedScene, boolean action, String password) {
        if (action) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_on);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.switch_on) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_off);
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.switch_off) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SCENES;

        if (action) jsonAction = DomoticzValues.Scene.Action.ON;
        else jsonAction = DomoticzValues.Scene.Action.OFF;

        StaticHelper.getDomoticz(mContext).setAction(clickedScene.getIdx(), jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override

            public void onReceiveResult(String result) {
                if (result.contains("WRONG CODE")) {
                    UsefulBits.showSnackbar(mContext, frameLayout, R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    if (getActivity() instanceof MainActivity)
                        ((MainActivity) getActivity()).Talk(R.string.security_wrong_code);
                } else {
                    processScenes();
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

    public void GetScenes() {
        StaticHelper.getDomoticz(mContext).getScenes(new ScenesReceiver() {
            @Override

            public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                SerializableManager.saveSerializable(mContext, scenes, "Scenes");
                successHandling(scenes.toString(), false);
                createListView(scenes);
            }

            @Override

            public void onError(Exception error) {
                errorHandling(error);
            }

            @Override

            public void onReceiveScene(SceneInfo scene) {
            }
        });
    }
}