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
import android.support.v4.widget.SwipeRefreshLayout;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.EventsAdapter;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.EventsClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.EventInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.EventReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

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
        processEvents();
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
        processEvents();
    }

    private void processEvents() {
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
                    public void onEventClick(final int idx, boolean action) {
                        int jsonAction = action ? DomoticzValues.Event.Action.ON : DomoticzValues.Event.Action.OFF;
                        int jsonUrl = DomoticzValues.Json.Url.Set.EVENTS_UPDATE_STATUS;

                        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
                            @Override
                            @DebugLog
                            public void onReceiveResult(String result) {
                                successHandling(result, false);
                            }

                            @Override
                            @DebugLog
                            public void onError(Exception error) {
                                errorHandling(error);
                            }
                        });
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
                    processEvents();
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
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);

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