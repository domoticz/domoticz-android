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
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.EventsAdapter;
import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.EventReceiver;
import nl.hnogames.domoticz.Interfaces.EventsClickListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;

public class Events extends DomoticzRecyclerFragment implements DomoticzFragmentListener {

    private EventsAdapter adapter;
    private Context mContext;
    private String filter = "";
    private SlideInBottomAnimationAdapter alphaSlideIn;

    @Override
    @DebugLog
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUserVariables();
    }


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
            getActionBar().setTitle(R.string.title_events);
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
        processUserVariables();
    }

    private void processUserVariables() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        new GetCachedDataTask().execute();
    }

    private void createListView(ArrayList<EventInfo> mEventInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new EventsAdapter(mContext, mDomoticz, mEventInfos, new EventsClickListener() {
                    @Override
                    @DebugLog
                    public void onEventClick(final int id, boolean action) {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, R.string.action_not_supported_yet, Snackbar.LENGTH_SHORT);
                        if (getActivity() instanceof MainActivity)
                            ((MainActivity) getActivity()).Talk(R.string.action_not_supported_yet);
                    }
                });
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
            } else {
                adapter.setData(mEventInfos);
                adapter.notifyDataSetChanged();
                alphaSlideIn.notifyDataSetChanged();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                @DebugLog
                public void onRefresh() {
                    processUserVariables();
                }
            });
            super.showSpinner(false);
            this.Filter(filter);
        }
    }

    @Override
    @DebugLog
    public void errorHandling(Exception error) {
        if (error != null) {
            // Let's check if were still attached to an activity
            if (isAdded()) {
                super.errorHandling(error);
            }
        }
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<EventInfo> cacheEventInfos = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheEventInfos = (ArrayList<EventInfo>) SerializableManager.readSerializedObject(mContext, "Events");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheEventInfos != null)
                createListView(cacheEventInfos);

            mDomoticz.getEvents(new EventReceiver() {
                @Override
                @DebugLog
                public void onReceiveEvents(final ArrayList<EventInfo> mEventInfos) {
                    successHandling(mEventInfos.toString(), false);
                    SerializableManager.saveSerializable(mContext, mEventInfos, "Events");
                    createListView(mEventInfos);
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