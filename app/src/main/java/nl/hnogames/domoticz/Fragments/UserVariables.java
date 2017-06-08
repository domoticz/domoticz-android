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
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import hugo.weaving.DebugLog;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.UserVariablesAdapter;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.Interfaces.UserVariablesClickListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.UserVariableInfo;
import nl.hnogames.domoticzapi.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class UserVariables extends DomoticzRecyclerFragment implements DomoticzFragmentListener, UserVariablesClickListener {

    private ArrayList<UserVariableInfo> mUserVariableInfos;
    private UserVariablesAdapter adapter;
    private Context mContext;
    private String filter = "";
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
        processUserVariables();
    }

    @Override
    @DebugLog
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_vars);
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

    private void createListView() {
        if (getView() != null) {
            if (adapter == null) {
                adapter = new UserVariablesAdapter(mContext, mDomoticz, mUserVariableInfos, this);
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
            } else {
                adapter.setData(mUserVariableInfos);
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
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);

                super.errorHandling(error);
            }
        }
    }

    @Override
    public void onUserVariableClick(final UserVariableInfo clickedVar) {
        new MaterialDialog.Builder(mContext)
            .title(R.string.title_vars)
            .content(clickedVar.getName() + " -> " + clickedVar.getTypeValue())
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(null, clickedVar.getValue(), new MaterialDialog.InputCallback() {
                @Override
                public void onInput(MaterialDialog dialog, CharSequence input) {
                    if (validateInput(String.valueOf(input), clickedVar.getType())) {
                        updateUserVariable(String.valueOf(input), clickedVar);
                    } else {
                        UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.var_input), Snackbar.LENGTH_SHORT);
                    }
                }
            }).show();
    }

    private boolean validateInput(String input, String type) {
        try {
            switch (type) {
                case "0":
                    Integer.parseInt(input);
                    break;
                case "1":
                    Float.parseFloat(input);
                    break;
                case "3":
                    new SimpleDateFormat("dd/MM/yyyy").parse(input);
                    break;
                case "4":
                    new SimpleDateFormat("HH:mm").parse(input);
                    break;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean updateUserVariable(String input, UserVariableInfo clickedVar) {
        mDomoticz.setUserVariableValue(input, clickedVar, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                processUserVariables();
            }

            @Override
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, coordinatorLayout, mContext.getString(R.string.var_input_error), Snackbar.LENGTH_SHORT);
            }
        });
        return true;
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<UserVariableInfo> cacheUserVariables = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheUserVariables = (ArrayList<UserVariableInfo>) SerializableManager.readSerializedObject(mContext, "UserVariables");
                    UserVariables.this.mUserVariableInfos = cacheUserVariables;
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheUserVariables != null)
                createListView();

            mDomoticz.getUserVariables(new UserVariablesReceiver() {
                @Override
                @DebugLog
                public void onReceiveUserVariables(ArrayList<UserVariableInfo> mVarInfos) {
                    UserVariables.this.mUserVariableInfos = mVarInfos;
                    SerializableManager.saveSerializable(mContext, mVarInfos, "UserVariables");
                    successHandling(mUserVariableInfos.toString(), false);
                    createListView();
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