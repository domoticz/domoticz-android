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
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.EventsAdapter;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.EventsClickListener;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.EventInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.EventReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class Events extends DomoticzRecyclerFragment implements DomoticzFragmentListener {

    private EventsAdapter adapter;
    private Context mContext;
    private String filter = "";
    private SlideInBottomAnimationAdapter alphaSlideIn;
    private boolean itemDecorationAdded = false;

    @Override

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

    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        setActionbar(getString(R.string.title_events));
        setSortFab(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        onAttachFragment(this);
        super.onActivityCreated(savedInstanceState);
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
        processEvents();
    }

    private void processEvents() {
        try {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(true);
            new GetCachedDataTask().execute();
        } catch (Exception ex) {
        }
    }

    private void createListView(ArrayList<EventInfo> mEventInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new EventsAdapter(mContext, StaticHelper.getDomoticz(mContext), mEventInfos, new EventsClickListener() {
                    @Override

                    public void onEventClick(final int idx, boolean action) {
                        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
                        if (user != null && user.getRights() <= 1) {
                            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
                            if (getActivity() instanceof MainActivity)
                                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
                            refreshFragment();
                            return;
                        }

                        int jsonAction = action ? DomoticzValues.Event.Action.ON : DomoticzValues.Event.Action.OFF;
                        int jsonUrl = DomoticzValues.Json.Url.Set.EVENTS_UPDATE_STATUS;
                        StaticHelper.getDomoticz(mContext).setAction(idx, jsonUrl, jsonAction, 0, null, new setCommandReceiver() {
                            @Override
                            public void onReceiveResult(String result) {
                                successHandling(result, false);
                            }

                            @Override
                            public void onError(Exception error) {
                                errorHandling(error);
                            }
                        });
                    }
                });
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
                if (!isTablet && !itemDecorationAdded) {
                    gridView.addItemDecoration(new MarginItemDecoration(20));
                    itemDecorationAdded = true;
                }
            } else {
                adapter.setData(mEventInfos);
                adapter.notifyDataSetChanged();
                alphaSlideIn.notifyDataSetChanged();
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override

                public void onRefresh() {
                    processEvents();
                }
            });
            super.showSpinner(false);
            this.Filter(filter);
        }
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

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<EventInfo> cacheEventInfos = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (mContext == null) return false;
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(mContext);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
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

            StaticHelper.getDomoticz(mContext).getEvents(new EventReceiver() {
                @Override

                public void onReceiveEvents(final ArrayList<EventInfo> mEventInfos) {
                    successHandling(mEventInfos.toString(), false);
                    SerializableManager.saveSerializable(mContext, mEventInfos, "Events");
                    createListView(mEventInfos);
                }

                @Override

                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }
}