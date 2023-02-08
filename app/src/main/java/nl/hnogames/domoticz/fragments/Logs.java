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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.LogAdapter;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticzapi.Containers.LogInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.LogsReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class Logs extends DomoticzRecyclerFragment implements DomoticzFragmentListener {
    private LogAdapter adapter;
    private Context mContext;
    private String filter = "";
    private boolean itemDecorationAdded = false;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

    @Override
    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processLogs();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        SetTitle(getString(R.string.title_logs));
        setSortFab(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        lySortLogs.setVisibility(View.VISIBLE);
        return view;
    }

    public void SetTitle(String title) {
        setActionbar(title);
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
        processLogs();
    }

    private void processLogs() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        new GetCachedDataTask().execute();
    }

    private void createListView(ArrayList<LogInfo> mLogInfos) {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new LogAdapter(mContext, StaticHelper.getDomoticz(mContext), mLogInfos);
                gridView.setAdapter(adapter);
            } else {
                adapter.setData(mLogInfos);
                adapter.notifyDataSetChanged();
            }
            if (!isTablet && !itemDecorationAdded) {
                gridView.addItemDecoration(new MarginItemDecoration(20));
                itemDecorationAdded = true;
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override

                public void onRefresh() {
                    processLogs();
                }
            });
        }
        super.showSpinner(false);
        this.Filter(filter);
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
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
                super.errorHandling(error);
            }
        }
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<LogInfo> cacheLogs = null;
        private int LogLevel;

        protected Boolean doInBackground(Boolean... geto) {
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(mContext);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheLogs = (ArrayList<LogInfo>) SerializableManager.readSerializedObject(mContext, "Logs");
                } catch (Exception ignored) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (isAdded()) {
                if (cacheLogs != null)
                    createListView(cacheLogs);

                LogLevel = DomoticzValues.Log.LOGLEVEL.ALL;
                if (getSort().equals(getString(R.string.filter_normal)))
                    LogLevel = DomoticzValues.Log.LOGLEVEL.NORMAL;
                if (getSort().equals(getString(R.string.filter_status)))
                    LogLevel = DomoticzValues.Log.LOGLEVEL.STATUS;
                if (getSort().equals(getString(R.string.filter_error)))
                    LogLevel = DomoticzValues.Log.LOGLEVEL.ERROR;

                StaticHelper.getDomoticz(mContext).getLogs(new LogsReceiver() {
                    @Override
                    public void onReceiveLogs(ArrayList<LogInfo> mLogInfos) {
                        successHandling(mLogInfos.toString(), false);
                        SerializableManager.saveSerializable(mContext, mLogInfos, "Logs");
                        createListView(mLogInfos);
                    }

                    @Override
                    public void onError(Exception error) {
                        if (LogLevel == DomoticzValues.Log.LOGLEVEL.ALL)
                            errorHandling(error);
                        else
                            createListView(new ArrayList<LogInfo>());
                    }
                }, LogLevel);
            }
        }
    }
}