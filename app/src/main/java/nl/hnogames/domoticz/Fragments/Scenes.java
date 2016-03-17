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
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.SceneAdapter;
import nl.hnogames.domoticz.Containers.SceneInfo;
import nl.hnogames.domoticz.Containers.SwitchLogInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.ScenesClickListener;
import nl.hnogames.domoticz.Interfaces.ScenesReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.PasswordDialog;
import nl.hnogames.domoticz.UI.SceneInfoDialog;
import nl.hnogames.domoticz.UI.SwitchLogInfoDialog;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Scenes extends DomoticzFragment implements DomoticzFragmentListener,
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        getActionBar().setTitle(R.string.title_scenes);
        initAnimation();
    }

    @Override
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
        if (mSwipeRefreshLayout != null)
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
                    Snackbar.make(coordinatorLayout, mContext.getString(R.string.filter_on) + ": " + super.getSort(), Snackbar.LENGTH_SHORT).show();
                    if ((super.getSort().equals(getContext().getString(R.string.filterOn_on)) && s.getStatusInBoolean()) && mDomoticz.isOnOffScene(s))
                        supportedScenes.add(s);
                    if ((super.getSort().equals(getContext().getString(R.string.filterOn_off)) && !s.getStatusInBoolean()) && mDomoticz.isOnOffScene(s))
                        supportedScenes.add(s);
                    if ((super.getSort().equals(getContext().getString(R.string.filterOn_static))) && !mDomoticz.isOnOffScene(s))
                        supportedScenes.add(s);
                }
            }

            adapter = new SceneAdapter(mContext, mDomoticz, supportedScenes, listener);
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
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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

        mDomoticz.setAction(mSceneInfo.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
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
    public void onSceneClick(int idx, final boolean action) {
        addDebugText("onSceneClick");
        addDebugText("Set " + idx + " to " + action);
        final SceneInfo clickedScene = getScene(idx);
        if (clickedScene.isProtected()) {
            PasswordDialog passwordDialog = new PasswordDialog(
                    getActivity(), mDomoticz);
            passwordDialog.show();
            passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                @Override
                public void onDismiss(String password) {
                    setScene(clickedScene, action, password);
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
        mDomoticz.getSceneLogs(idx, new SwitchLogReceiver() {
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
        if (action)
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_on) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT).show();
        else
            Snackbar.make(coordinatorLayout, getActivity().getString(R.string.switch_off) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT).show();

        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SCENES;

        if (action) jsonAction = Domoticz.Scene.Action.ON;
        else jsonAction = Domoticz.Scene.Action.OFF;

        mDomoticz.setAction(clickedScene.getIdx(), jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
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
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }
}