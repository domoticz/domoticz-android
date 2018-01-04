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
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.SceneAdapter;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.ScenesClickListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.PasswordDialog;
import nl.hnogames.domoticz.UI.SceneInfoDialog;
import nl.hnogames.domoticz.UI.SwitchLogInfoDialog;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Containers.SwitchLogInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchLogReceiver;
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
    private SlideInBottomAnimationAdapter alphaSlideIn;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_scenes);
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

    private void initAnimation() {
        animShow = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        animHide = AnimationUtils.loadAnimation(mContext, R.anim.exit_to_right);
    }

    @Override
    @DebugLog
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processScenes();
    }

    @Override
    @DebugLog
    public void onConnectionOk() {
        super.showSpinner(true);
        processScenes();
    }

    private void processScenes() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        state = gridView.getLayoutManager().onSaveInstanceState();
        WidgetUtils.RefreshWidgets(mContext);

        new GetCachedDataTask().execute();
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
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.filter_on) + ": " + super.getSort(), Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.filter_on);
                        if ((super.getSort().equals(getContext().getString(R.string.filterOn_on)) && s.getStatusInBoolean()) && mDomoticz.isOnOffScene(s))
                            supportedScenes.add(s);
                        if ((super.getSort().equals(getContext().getString(R.string.filterOn_off)) && !s.getStatusInBoolean()) && mDomoticz.isOnOffScene(s))
                            supportedScenes.add(s);
                        if ((super.getSort().equals(getContext().getString(R.string.filterOn_static))) && !mDomoticz.isOnOffScene(s))
                            supportedScenes.add(s);
                    }
                }
            }

            if (adapter == null) {
                adapter = new SceneAdapter(mContext, mDomoticz, supportedScenes, listener);
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
            } else {
                adapter.setData(supportedScenes);
                adapter.notifyDataSetChanged();
                alphaSlideIn.notifyDataSetChanged();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                @DebugLog
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
            @DebugLog
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
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mSceneInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mSceneInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SCENEFAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        mDomoticz.setAction(mSceneInfo.getIdx(), jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                successHandling(result, false);
                mSceneInfo.setFavoriteBoolean(isFavorite);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                // Domoticz always gives an error: ignore
                errorHandling(error);
            }
        });
    }


    @Override
    @DebugLog
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
                @DebugLog
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
    @DebugLog
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getScene(idx), checked);
    }

    @Override
    @DebugLog
    public void onLogButtonClick(int idx) {
        mDomoticz.getSceneLogs(idx, new SwitchLogReceiver() {
            @Override
            @DebugLog
            public void onReceiveSwitches(ArrayList<SwitchLogInfo> switchesLogs) {
                showLogDialog(switchesLogs);
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.error_logs, Snackbar.LENGTH_SHORT);
                if (getActivity() instanceof MainActivity)
                    ((MainActivity) getActivity()).Talk(R.string.error_logs);
            }
        });
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
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_on) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.switch_off);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.switch_off) + ": " + clickedScene.getName(), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SCENES;

        if (action) jsonAction = DomoticzValues.Scene.Action.ON;
        else jsonAction = DomoticzValues.Scene.Action.OFF;

        mDomoticz.setAction(clickedScene.getIdx(), jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            @DebugLog
            public void onReceiveResult(String result) {
                processScenes();
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
    public void onPause() {
        super.onPause();
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

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<SceneInfo> cacheSwitches = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheSwitches = (ArrayList<SceneInfo>) SerializableManager.readSerializedObject(mContext, "Scenes");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheSwitches != null)
                createListView(cacheSwitches);

            mDomoticz.getScenes(new ScenesReceiver() {
                @Override
                @DebugLog
                public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                    SerializableManager.saveSerializable(mContext, scenes, "Scenes");
                    successHandling(scenes.toString(), false);
                    createListView(scenes);
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    errorHandling(error);
                }

                @Override
                @DebugLog
                public void onReceiveScene(SceneInfo scene) {
                }
            });
        }
    }
}