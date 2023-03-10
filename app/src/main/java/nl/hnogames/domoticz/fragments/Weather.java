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
import android.widget.LinearLayout;

import androidx.recyclerview.widget.ItemTouchHelper;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.GraphActivity;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.WeatherAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.SimpleItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.WeatherClickListener;
import nl.hnogames.domoticz.ui.WeatherInfoDialog;
import nl.hnogames.domoticz.utils.AnimationUtil;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Containers.WeatherInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.WeatherReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class Weather extends DomoticzRecyclerFragment implements DomoticzFragmentListener, WeatherClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = Weather.class.getSimpleName();
    private Context mContext;
    private WeatherAdapter adapter;
    private String filter = "";
    private LinearLayout lExtraPanel = null;
    private Animation animShow, animHide;
    private ArrayList<WeatherInfo> mWeatherInfoList;
    private boolean itemDecorationAdded = false;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }


    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processWeather();
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
        setActionbar(getString(R.string.title_weather));
        setSortFab(false);
    }

    private ArrayList<WeatherInfo> AddAdsDevice(ArrayList<WeatherInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                ArrayList<WeatherInfo> filteredList = new ArrayList<>();
                for (WeatherInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                WeatherInfo adView = new WeatherInfo();
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
        //if (getActionBar() != null)
        //   getActionBar().setTitle(R.string.title_weather);
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
        processWeather();
    }

    private void processWeather() {
        try {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);

            new GetCachedDataTask().execute();
        } catch (Exception ex) {
        }
    }

    private void initAnimation() {
        animShow = AnimationUtil.getLogRowAnimationOpen(mContext);
        animHide = AnimationUtil.getLogRowAnimationClose(mContext);
    }

    private void createListView(ArrayList<WeatherInfo> mWeatherInfos) {
        if (adapter == null) {
            adapter = new WeatherAdapter(mContext, StaticHelper.getDomoticz(mContext), getServerUtil(), AddAdsDevice(mWeatherInfos), this);
            gridView.setAdapter(adapter);
        } else {
            adapter.setData(AddAdsDevice(mWeatherInfos));
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
        mSwipeRefreshLayout.setOnRefreshListener(() -> processWeather());
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
        infoDialog.onDismissListener((isChanged, isFavorite) -> {
            if (isChanged)
                changeFavorite(mWeatherInfo, isFavorite);
        });
    }

    private void changeFavorite(final WeatherInfo mWeatherInfo, final boolean isFavorite) {
        addDebugText("changeFavorite");
        addDebugText("Set idx " + mWeatherInfo.getIdx() + " favorite to " + isFavorite);

        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
        if (user != null && user.getRights() <= 1) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            refreshFragment();
            return;
        }
        if (isFavorite) {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_added);
            UsefulBits.showSnackbar(mContext, frameLayout, mWeatherInfo.getName() + " " + mContext.getString(R.string.favorite_added), Snackbar.LENGTH_SHORT);
        } else {
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.favorite_removed);
            UsefulBits.showSnackbar(mContext, frameLayout, mWeatherInfo.getName() + " " + mContext.getString(R.string.favorite_removed), Snackbar.LENGTH_SHORT);
        }

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.FAVORITE;

        if (isFavorite) jsonAction = DomoticzValues.Device.Favorite.ON;
        else jsonAction = DomoticzValues.Device.Favorite.OFF;

        StaticHelper.getDomoticz(mContext).setAction(mWeatherInfo.getIdx(),
                jsonUrl,
                jsonAction,
                0,
                null,
                new setCommandReceiver() {
                    @Override

                    public void onReceiveResult(String result) {
                        successHandling(result, false);
                        mWeatherInfo.setFavoriteBoolean(isFavorite);
                    }

                    @Override

                    public void onError(Exception error) {
                        errorHandling(error);
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

    public void onLogClick(final WeatherInfo weather, final String range) {
        String graphType = weather.getTypeImg()
                .toLowerCase()
                .replace("temperature", "temp")
                .replace("visibility", "counter");
        if (weather.getSubType().equals("Barometer"))
            graphType = "temp";

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

    public void onLikeButtonClick(int idx, boolean checked) {
        changeFavorite(getWeather(idx), checked);
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
        showInfoDialog(getWeather(idx));
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
            if (mContext == null)
                return false;
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(mContext);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheWeathers = (ArrayList<WeatherInfo>) SerializableManager.readSerializedObject(mContext, "Weathers");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (mContext == null)
                return;
            if (cacheWeathers != null)
                createListView(cacheWeathers);

            StaticHelper.getDomoticz(mContext).getWeathers(new WeatherReceiver() {
                @Override

                public void onReceiveWeather(ArrayList<WeatherInfo> mWeatherInfos) {
                    mWeatherInfoList = mWeatherInfos;
                    if (getView() != null) {
                        successHandling(mWeatherInfos.toString(), false);
                        SerializableManager.saveSerializable(mContext, mWeatherInfos, "Weathers");
                        createListView(mWeatherInfos);
                    }
                }

                @Override

                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }
}