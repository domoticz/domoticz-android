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
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.PlansAdapter;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.PlanActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticzapi.Containers.PlanInfo;
import nl.hnogames.domoticzapi.Interfaces.PlansReceiver;

public class Plans extends DomoticzCardFragment implements DomoticzFragmentListener {

    @SuppressWarnings("unused")
    private static final String TAG = Plans.class.getSimpleName();

    private Context mContext;
    private RecyclerView mRecyclerView;
    private SharedPrefUtil mSharedPrefs;
    private PlansAdapter mAdapter;
    private ArrayList<PlanInfo> mPlans;
    private SlideInBottomAnimationAdapter alphaSlideIn;

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
    public void refreshFragment() {
        processPlans();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void processPlans() {
        new GetCachedDataTask().execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mSharedPrefs = new SharedPrefUtil(mContext);
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_plans);
    }

    @Override
    public void errorHandling(Exception error, CoordinatorLayout coordinatorLayout) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error, coordinatorLayout);
            }
        }
    }

    public ActionBar getActionBar() {
        return getActivity() != null ? ((AppCompatActivity) getActivity()).getSupportActionBar() : null;
    }

    @Override
    public void onConnectionOk() {
        processPlans();
    }

    private void createListView() {
        if (getView() == null)
            return;

        Collections.sort(this.mPlans, new Comparator<PlanInfo>() {
            @Override
            public int compare(PlanInfo left, PlanInfo right) {
                return left.getOrder() - right.getOrder();
            }
        });

        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
            mRecyclerView.setHasFixedSize(true);
            GridLayoutManager mLayoutManager = new GridLayoutManager(mContext, 2);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }

        if (mAdapter == null) {
            mAdapter = new PlansAdapter(this.mPlans, mContext);
            mAdapter.setOnItemClickListener(new PlansAdapter.onClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    if (mPhoneConnectionUtil.isNetworkAvailable()) {
                        Intent intent = new Intent(mContext, PlanActivity.class);
                        intent.putExtra("PLANNAME", mPlans.get(position).getName());
                        intent.putExtra("PLANID", mPlans.get(position).getIdx());
                        startActivity(intent);
                    } else {
                        if (coordinatorLayout != null) {
                            UsefulBits.showSnackbar(getContext(), coordinatorLayout, R.string.error_notConnected, Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.error_notConnected);
                        }
                    }
                }
            });
            alphaSlideIn = new SlideInBottomAnimationAdapter(mAdapter);
            mRecyclerView.setAdapter(alphaSlideIn);
        } else {
            mAdapter.setData(this.mPlans);
            mAdapter.notifyDataSetChanged();
            alphaSlideIn.notifyDataSetChanged();
        }
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<PlanInfo> cachePlans = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
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

            mDomoticz.getPlans(new PlansReceiver() {
                @Override
                public void OnReceivePlans(ArrayList<PlanInfo> plans) {
                    successHandling(plans.toString(), false);
                    SerializableManager.saveSerializable(mContext, plans, "Plans");
                    Plans.this.mPlans = plans;
                    createListView();
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error, coordinatorLayout);
                }
            });
        }
    }
}