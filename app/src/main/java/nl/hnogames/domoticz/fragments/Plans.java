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
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.PlanActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.PlansAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.helpers.SimpleItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.utils.ViewUtils;
import nl.hnogames.domoticzapi.Containers.PlanInfo;
import nl.hnogames.domoticzapi.Interfaces.PlansReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class Plans extends DomoticzCardFragment implements DomoticzFragmentListener {

    @SuppressWarnings("unused")
    private static final String TAG = Plans.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecyclerView;
    private SharedPrefUtil mSharedPrefs;
    private PlansAdapter mAdapter;
    private ArrayList<PlanInfo> mPlans;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    public void onConnectionFailed() {
        processPlans();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null)
            mAdapter.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processPlans();
    }

    public void processPlans() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        new GetCachedDataTask().execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        mSharedPrefs = new SharedPrefUtil(mContext);
        setActionbar(getString(R.string.title_plans));
        setSortFab(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void errorHandling(Exception error, View frameLayout) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error, frameLayout);
            }
        }
    }

    @Override
    public void onConnectionOk() {
        processPlans();
    }

    private void createListView() {
        if (getView() == null)
            return;

        Collections.sort(this.mPlans, (left, right) -> left.getOrder() - right.getOrder());

        if (mRecyclerView == null) {
            mRecyclerView = getView().findViewById(R.id.my_recycler_view);
            mSwipeRefreshLayout = getView().findViewById(R.id.swipe_refresh_layout);

            StaggeredGridLayoutManager mLayoutManager;
            boolean isPortrait = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            if (ViewUtils.isTablet(getContext())) {
                if (isPortrait) {
                    mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                } else {
                    mLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
                }
            } else {
                if (isPortrait) {
                    mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                } else {
                    mLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
                }
            }

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setHasFixedSize(false);
        }

        if (mAdapter == null) {
            mAdapter = new PlansAdapter(AddAdsDevice(this.mPlans), mContext);
            mAdapter.setOnItemClickListener((position, v) -> {
                try {
                    if (mPhoneConnectionUtil != null && mPhoneConnectionUtil.isNetworkAvailable()) {
                        Intent intent = new Intent(mContext, PlanActivity.class);
                        intent.putExtra("PLANNAME", mPlans.get(position).getName());
                        intent.putExtra("PLANID", mPlans.get(position).getIdx());
                        startActivity(intent);
                    } else {
                        if (frameLayout != null) {
                            UsefulBits.showSnackbar(getContext(), frameLayout, R.string.error_notConnected, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.error_notConnected);
                        }
                    }
                } catch (Exception ignored) {
                }
            });
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setData(AddAdsDevice(this.mPlans));
            mAdapter.notifyDataSetChanged();
        }

        if (mItemTouchHelper == null) {
            mItemTouchHelper = new ItemTouchHelper(new SimpleItemTouchHelperCallback(mAdapter, true));
        }
        if (mSharedPrefs.enableCustomSorting() && !mSharedPrefs.isCustomSortingLocked()) {
            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        } else {
            if (mItemTouchHelper != null)
                mItemTouchHelper.attachToRecyclerView(null);
        }

        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(() -> processPlans());
    }

    private ArrayList<PlanInfo> AddAdsDevice(ArrayList<PlanInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                ArrayList<PlanInfo> filteredList = new ArrayList<>();
                for (PlanInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                PlanInfo adView = new PlanInfo();
                adView.setIdx(MainActivity.ADS_IDX);
                adView.setName("Ads");
                filteredList.add(1, adView);
                this.mPlans = filteredList;
                return filteredList;
            }
        } catch (Exception ex) {
        }
        return supportedSwitches;
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<PlanInfo> cachePlans = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(mContext);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cachePlans = (ArrayList<PlanInfo>) SerializableManager.readSerializedObject(mContext, "Plans");
                    Plans.this.mPlans = cachePlans;

                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cachePlans != null)
                createListView();

            StaticHelper.getDomoticz(mContext).getPlans(new PlansReceiver() {
                @Override
                public void OnReceivePlans(ArrayList<PlanInfo> plans) {
                    successHandling(plans.toString(), false);
                    SerializableManager.saveSerializable(mContext, plans, "Plans");
                    Plans.this.mPlans = plans;
                    createListView();
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error, frameLayout);
                }
            });
        }
    }
}