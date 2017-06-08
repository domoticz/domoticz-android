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
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.WeatherAdapter;
import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.WeatherClickListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.UI.WeatherInfoDialog;
import nl.hnogames.domoticz.Utils.AnimationUtil;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Containers.WeatherInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.WeatherReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class Weather extends DomoticzRecyclerFragment implements DomoticzFragmentListener, WeatherClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Weather.class.getSimpleName();
    private Context mContext;
    private WeatherAdapter adapter;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private ArrayList<WeatherInfo> mWeatherInfoList;
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
        processWeather();
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_weather);
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
        processWeather();
    }

    private void processWeather() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        new GetCachedDataTask().execute();
    }

    private void initAnimation() {
        animShow = AnimationUtil.getLogRowAnimationOpen(mContext);
        animHide = AnimationUtil.getLogRowAnimationClose(mContext);
    }

    private void createListView(ArrayList<WeatherInfo> mWeatherInfos) {
        if (adapter == null) {
            adapter = new WeatherAdapter(mContext, mDomoticz, getServerUtil(), mWeatherInfos, this);
            alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
            gridView.setAdapter(alphaSlideIn);
        } else {
            adapter.setData(mWeatherInfos);
            adapter.notifyDataSetChanged();
            alphaSlideIn.notifyDataSetChanged();
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            @DebugLog
            public void onRefresh() {
                processWeather();
            }
        });
        super.showSpinner(false);
        this.Filter(filter);
    }

    private void showInfoDialog(final WeatherInfo mWeatherInfo) {
        WeatherInfoDialog infoDialog = new WeatherInfoDialog(
            mContext,
            mWeatherInfo,
            R.layout.dialog_weather);
        infoDialog.setWeatherInfo(mWeatherInfo);
        infoDialog.show();
        infoDialog.onDismissListener(new WeatherInfoDialog.DismissListener() {
            @Override
            @DebugLog
            public void onDismiss(boolean isChanged, boolean isFavorite) {
                if (isChanged)
                    changeFavorite(mWeatherInfo, isFavorite);
            }
        });
    }

    private void changeFavorite(final WeatherInfo mWeatherInfo, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mWeatherInfo.getIdx() + " favorite to " + isFavorite);

        if (isFavorite) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mWeatherInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, coordinatorLayout, mWeatherInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        mDomoticz.setAction(mWeatherInfo.getIdx(),
            jsonUrl,
            jsonAction,
            0,
            null,
            new setCommandReceiver() {
                @Override
                @DebugLog
                public void onReceiveResult(String result) {
                    successHandling(result, false);
                    mWeatherInfo.setFavoriteBoolean(isFavorite);
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
    public void onLogClick(final WeatherInfo weather, final String range) {
        final String graphType = weather.getTypeImg()
            .toLowerCase()
            .replace("temperature", "temp")
            .replace("visibility", "counter");

        JSONObject language = null;
        Language languageObj = new SharedPrefUtil(mContext).getSavedLanguage();
        if (languageObj != null) language = languageObj.getJsonObject();
        String graphDialogTitle;
        if (language != null) {
            graphDialogTitle = language.optString(weather.getType(), graphType);
        } else {
            graphDialogTitle = weather.getType();
        }

        Intent intent = new Intent(mContext, GraphActivity.class);
        intent.putExtra("IDX", weather.getIdx());
        intent.putExtra("RANGE", range);
        intent.putExtra("TYPE", graphType);
        intent.putExtra("TITLE", graphDialogTitle.toUpperCase());
        intent.putExtra("STEPS", 4);
        startActivity(intent);
    }

    @Override
    @DebugLog
    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getWeather(idx), checked);
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

    private WeatherInfo getWeather(int idx) {
        WeatherInfo clickedWeather = null;
        for (WeatherInfo mWeatherInfo : mWeatherInfoList) {
            if (mWeatherInfo.getIdx() == idx) {
                clickedWeather = mWeatherInfo;
            }
        }
        return clickedWeather;
    }


    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<WeatherInfo> cacheWeathers = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheWeathers = (ArrayList<WeatherInfo>) SerializableManager.readSerializedObject(mContext, "Weathers");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheWeathers != null)
                createListView(cacheWeathers);

            mDomoticz.getWeathers(new WeatherReceiver() {
                @Override
                @DebugLog
                public void onReceiveWeather(ArrayList<WeatherInfo> mWeatherInfos) {
                    mWeatherInfoList = mWeatherInfos;
                    if (getView() != null) {
                        successHandling(mWeatherInfos.toString(), false);
                        SerializableManager.saveSerializable(mContext, mWeatherInfos, "Weathers");
                        createListView(mWeatherInfos);
                    }
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