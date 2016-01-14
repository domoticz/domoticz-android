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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.EventsAdapter;
import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.EventReceiver;
import nl.hnogames.domoticz.Interfaces.EventsClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class Events extends DomoticzFragment implements DomoticzFragmentListener {

    private Domoticz mDomoticz;
    private EventsAdapter adapter;
    private ProgressDialog progressDialog;
    private Context mContext;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;
    private ListView listView;
    private String filter = "";

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUserVariables();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        getActionBar().setTitle(R.string.title_events);
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

    @Override
    public void onConnectionOk() {
        super.showSpinner(true);
        coordinatorLayout = (CoordinatorLayout) getView().findViewById(R.id
                .coordinatorLayout);
        mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
        listView = (ListView) getView().findViewById(R.id.listView);

        mDomoticz = new Domoticz(mContext);
        processUserVariables();
    }

    private void processUserVariables() {
        mSwipeRefreshLayout.setRefreshing(true);
        mDomoticz.getEvents(new EventReceiver() {
            @Override
            public void onReceiveEvents(final ArrayList<EventInfo> mEventInfos) {
                successHandling(mEventInfos.toString(), false);

                adapter = new EventsAdapter(mContext, mEventInfos, new EventsClickListener() {
                    @Override
                    public void onEventClick(final int id, boolean action) {
                        Snackbar.make(coordinatorLayout, R.string.action_not_supported_yet, Snackbar.LENGTH_SHORT).show();
                    }
                });

                createListView();
            }

            @Override
            public void onError(Exception error) {
                errorHandling(error);
            }
        });

    }

    private void createListView() {
        if (getView() != null) {
            SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
            animationAdapter.setAbsListView(listView);
            listView.setAdapter(animationAdapter);

            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    processUserVariables();
                }
            });
            super.showSpinner(false);
            this.Filter(filter);
        }
    }

    @Override
    public void errorHandling(Exception error) {
        // Let's check if were still attached to an activity
        if (isAdded()) {
            super.errorHandling(error);
        }
    }
}