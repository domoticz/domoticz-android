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
import android.text.InputType;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.UserVariablesAdapter;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.helpers.MarginItemDecoration;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.interfaces.UserVariablesClickListener;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Containers.UserVariableInfo;
import nl.hnogames.domoticzapi.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class UserVariables extends DomoticzRecyclerFragment implements DomoticzFragmentListener, UserVariablesClickListener {
    private ArrayList<UserVariableInfo> mUserVariableInfos;
    private UserVariablesAdapter adapter;
    private Context mContext;
    private String filter = "";
    private boolean itemDecorationAdded = false;
    private SlideInBottomAnimationAdapter alphaSlideIn;

    @Override
    public void onConnectionFailed() {
        new GetCachedDataTask().execute();
    }

    @Override

    public void refreshFragment() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        processUserVariables();
    }

    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        mContext = context;
        setActionbar(getString(R.string.title_vars));
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
                adapter = new UserVariablesAdapter(mContext, StaticHelper.getDomoticz(mContext), mUserVariableInfos, this);
                alphaSlideIn = new SlideInBottomAnimationAdapter(adapter);
                gridView.setAdapter(alphaSlideIn);
            } else {
                adapter.setData(mUserVariableInfos);
                adapter.notifyDataSetChanged();
                alphaSlideIn.notifyDataSetChanged();
            }
            if (!isTablet && !itemDecorationAdded) {
                gridView.addItemDecoration(new MarginItemDecoration(20));
                itemDecorationAdded = true;
            }
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
        UserInfo user = getCurrentUser(mContext, StaticHelper.getDomoticz(mContext));
        if (user != null && user.getRights() <= 1) {
            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.security_no_rights), Snackbar.LENGTH_SHORT);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).Talk(R.string.security_no_rights);
            refreshFragment();
            return;
        }
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
                            UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.var_input), Snackbar.LENGTH_SHORT);
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
        StaticHelper.getDomoticz(mContext).setUserVariableValue(input, clickedVar, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                processUserVariables();
            }

            @Override
            public void onError(Exception error) {
                UsefulBits.showSnackbar(mContext, frameLayout, mContext.getString(R.string.var_input_error), Snackbar.LENGTH_SHORT);
            }
        });
        return true;
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<UserVariableInfo> cacheUserVariables = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (mContext == null)
                return false;
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(mContext);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheUserVariables = (ArrayList<UserVariableInfo>) SerializableManager.readSerializedObject(mContext, "UserVariables");
                    UserVariables.this.mUserVariableInfos = cacheUserVariables;
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (mContext == null)
                return;
            if (cacheUserVariables != null)
                createListView();

            StaticHelper.getDomoticz(mContext).getUserVariables(new UserVariablesReceiver() {
                @Override

                public void onReceiveUserVariables(ArrayList<UserVariableInfo> mVarInfos) {
                    UserVariables.this.mUserVariableInfos = mVarInfos;
                    SerializableManager.saveSerializable(mContext, mVarInfos, "UserVariables");
                    successHandling(mUserVariableInfos.toString(), false);
                    createListView();
                }

                @Override

                public void onError(Exception error) {
                    errorHandling(error);
                }
            });
        }
    }
}