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
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

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
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Scenes extends DomoticzFragment implements DomoticzFragmentListener,
        ScenesClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Scenes.class.getSimpleName();
    private ProgressDialog progressDialog;
    private Context mContext;
    private Domoticz mDomoticz;
    private SceneAdapter adapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CoordinatorLayout coordinatorLayout;
    private ArrayList<SceneInfo> mScenes;
    private Parcelable state;
    private ListView listView;
    private String filter = "";


    @Override
    public void Filter(String text) {
        filter=text;
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
        super.showSpinner(true);
        mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
        listView = (ListView) getView().findViewById(R.id.listView);
        coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id
                .coordinatorLayout);
        mDomoticz = new Domoticz(mContext);
        processScenes();
    }

    private void processScenes() {
        mSwipeRefreshLayout.setRefreshing(true);

        state = listView.onSaveInstanceState();
        WidgetUtils.RefreshWidgets(mContext);


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

            @Override
            public void onReceiveScene(SceneInfo scene) {
            }
        });
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
                    Snackbar.make(coordinatorLayout, "Filter on :" + super.getSort(), Snackbar.LENGTH_SHORT).show();
                    if ((super.getSort().equals(getContext().getString(R.string.filterOn_on)) && s.getStatusInBoolean()) && isOnOffScene(s))
                        supportedScenes.add(s);
                    if ((super.getSort().equals(getContext().getString(R.string.filterOn_off)) && !s.getStatusInBoolean()) && isOnOffScene(s))
                        supportedScenes.add(s);
                    if ((super.getSort().equals(getContext().getString(R.string.filterOn_static))) && !isOnOffScene(s))
                        supportedScenes.add(s);
                }
            }

            adapter = new SceneAdapter(mContext, supportedScenes, listener);
            SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
            animationAdapter.setAbsListView(listView);
            listView.setAdapter(animationAdapter);

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
                    processScenes();
                }
            });

            if (state != null) {
                // Restore previous state (including selected item index and scroll position)
                listView.onRestoreInstanceState(state);
            }
        }
        super.showSpinner(false);
        this.Filter(filter);

    }

    private boolean isOnOffScene(SceneInfo testSwitch) {
        switch (testSwitch.getType()) {
            case Domoticz.Scene.Type.GROUP:
                return true;
        }

        return false;
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
                processScenes();
            }

            @Override
            public void onError(Exception error) {
                // Domoticz always gives an error: ignore
                errorHandling(error);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
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
                processScenes();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });
    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void errorHandling(Exception error) {
        // Let's check if were still attached to an activity
        if (isAdded()) {
            super.errorHandling(error);
        }
    }
}