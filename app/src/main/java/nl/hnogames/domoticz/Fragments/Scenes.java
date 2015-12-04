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

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.SceneAdapter;
import nl.hnogames.domoticz.Containers.SceneInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.ScenesClickListener;
import nl.hnogames.domoticz.Interfaces.ScenesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.SceneInfoDialog;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Scenes extends DomoticzFragment implements DomoticzFragmentListener,
        ScenesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Scenes.class.getSimpleName();
    private ProgressDialog progressDialog;
    private Activity mActivity;
    private Domoticz mDomoticz;
    private SceneAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CoordinatorLayout coordinatorLayout;
    private ArrayList<SceneInfo> mScenes;

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
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        processScenes();
    }

    @Override
    public void onConnectionOk() {
        showProgressDialog();

        mDomoticz = new Domoticz(mActivity);
        processScenes();
    }

    private void processScenes() {
        mDomoticz.getScenes(new ScenesReceiver() {

            @Override
            public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                successHandling(scenes.toString(), false);
                createListView(scenes);
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }

    public void createListView(final ArrayList<SceneInfo> scenes) {

        if (getView() != null) {
            mScenes = scenes;
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);

            coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id
                    .coordinatorLayout);
            final ScenesClickListener listener = this;

            adapter = new SceneAdapter(mActivity, scenes, listener);
            ListView listView = (ListView) getView().findViewById(R.id.listView);
            listView.setAdapter(adapter);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view,
                                               int index, long id) {
                    showInfoDialog(scenes.get(index));
                    return true;
                }
            });

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    processScenes();
                }
            });

            hideProgressDialog();
        }
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

        if (isFavorite)
            Snackbar.make(coordinatorLayout, mSceneInfo.getName() + " " + getActivity().getString(R.string.favorite_added), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, mSceneInfo.getName() + " " + getActivity().getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SCENEFAVORITE;

        if (isFavorite) jsonAction = Domoticz.Device.Favorite.ON;
        else jsonAction = Domoticz.Device.Favorite.OFF;

        mDomoticz.setAction(mSceneInfo.getIdx(), jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSceneInfo.setFavoriteBoolean(isFavorite);
            }

            @Override
            public void onError(Exception error) {
                // Domoticz always gives an error: ignore
                errorHandling(error);
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        getActionBar().setTitle(R.string.title_scenes);
    }

    @Override
    public void onSceneClick(int idx, boolean action) {
        addDebugText("onSceneClick");
        addDebugText("Set " + idx + " to " + action);
        SceneInfo clickedScene = getScene(idx);

        if (action)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SCENES;

        if (action) jsonAction = Domoticz.Scene.Action.ON;
        else jsonAction = Domoticz.Scene.Action.OFF;

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