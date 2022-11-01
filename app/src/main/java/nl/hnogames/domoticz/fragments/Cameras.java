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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.adapters.CamerasAdapter;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.helpers.SimpleItemTouchHelperCallback;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.utils.CameraUtil;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.ViewUtils;
import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Containers.LoginInfo;
import nl.hnogames.domoticzapi.Interfaces.CameraReceiver;
import nl.hnogames.domoticzapi.Interfaces.LoginReceiver;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;

public class Cameras extends DomoticzCardFragment implements DomoticzFragmentListener, OnPermissionCallback {

    @SuppressWarnings("unused")
    private static final String TAG = Cameras.class.getSimpleName();

    private Context context;
    private RecyclerView mRecyclerView;
    private CamerasAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean refreshTimer = false;
    private SharedPrefUtil mSharedPrefs;
    private PermissionFragmentHelper permissionFragmentHelper;
    private ItemTouchHelper mItemTouchHelper;
    private ArrayList<CameraInfo> Cameras;

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
        refreshTimer = true;
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        getCameras();
    }

    private ArrayList<CameraInfo> AddAdsDevice(ArrayList<CameraInfo> supportedSwitches) {
        try {
            if (supportedSwitches == null || supportedSwitches.size() <= 0)
                return supportedSwitches;

            if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                ArrayList<CameraInfo> filteredList = new ArrayList<>();
                for (CameraInfo d : supportedSwitches) {
                    if (d.getIdx() != MainActivity.ADS_IDX)
                        filteredList.add(d);
                }
                CameraInfo adView = new CameraInfo();
                adView.setIdx(MainActivity.ADS_IDX);
                adView.setName("Ads");
                filteredList.add(1, adView);
                Cameras = filteredList;
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
    }

    public void getCameras() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        permissionFragmentHelper = PermissionFragmentHelper.getInstance(this);
        new GetCachedDataTask().execute();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFragment(this);
        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        setActionbar(getString(R.string.title_cameras));
        setSortFab(false);
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

    public ActionBar getActionBar() {
        return ((AppCompatActivity) context).getSupportActionBar();
    }

    @Override
    public void onConnectionOk() {
        if (getView() != null) {
            getCameras();
        }
    }

    @Override
    public void onConnectionFailed() {
        getCameras();
    }

    private void createListView() {
        if (getView() == null)
            return;

        if (mRecyclerView == null) {
            mRecyclerView = getView().findViewById(R.id.my_recycler_view);
            mSwipeRefreshLayout = getView().findViewById(R.id.swipe_refresh_layout);

            StaggeredGridLayoutManager mLayoutManager;
            boolean isPortrait = getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
            if (ViewUtils.isTablet(context)) {
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
            mAdapter = new CamerasAdapter(AddAdsDevice(Cameras), context, StaticHelper.getDomoticz(context), refreshTimer);
            mAdapter.setOnItemClickListener((position, v) -> {
                CameraInfo camera = Cameras.get(position);
                CameraUtil.ProcessImage(context, camera.getIdx(), camera.getName());
            });
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setRefreshTimer(refreshTimer);
            mAdapter.setData(AddAdsDevice(Cameras));
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
        mSwipeRefreshLayout.setOnRefreshListener(() -> getCameras());
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.i("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
        String[] neededPermission = PermissionFragmentHelper.declinedPermissions(this, PermissionsUtil.INITIAL_STORAGE_PERMS);
        StringBuilder builder = new StringBuilder(neededPermission.length);
        if (neededPermission.length > 0) {
            for (String permission : neededPermission) {
                builder.append(permission).append("\n");
            }
        }
        AlertDialog alert = PermissionsUtil.getAlertDialog(getActivity(), permissionFragmentHelper, getActivity().getString(R.string.permission_title),
                getActivity().getString(R.string.permission_desc_storage), neededPermission);
        if (!alert.isShowing()) {
            alert.show();
        }
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.i("onPermissionPreGranted", "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        Log.i("NeedExplanation", "Permission( " + permissionName + " ) needs Explanation");
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.i("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.i("onNoPermissionNeeded", "Permission(s) not needed");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionFragmentHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionFragmentHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
    }

    private class GetCachedDataTask extends AsyncTask<Boolean, Boolean, Boolean> {
        ArrayList<CameraInfo> cacheCameras = null;

        protected Boolean doInBackground(Boolean... geto) {
            if (context == null) return false;
            if (mPhoneConnectionUtil == null)
                mPhoneConnectionUtil = new PhoneConnectionUtil(context);
            if (mPhoneConnectionUtil != null && !mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheCameras = (ArrayList<CameraInfo>) SerializableManager.readSerializedObject(context, "Cameras");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheCameras != null) {
                Cameras = cacheCameras;
                createListView();
            }
            StaticHelper.getDomoticz(context).checkLogin(new LoginReceiver() {
                @Override
                public void OnReceive(LoginInfo mLoginInfo) {
                    StaticHelper.getDomoticz(context).getCameras(new CameraReceiver() {
                        @Override
                        public void OnReceiveCameras(ArrayList<CameraInfo> c) {
                            successHandling(c.toString(), false);
                            SerializableManager.saveSerializable(context, c, "Cameras");
                            Cameras = c;
                            createListView();
                        }

                        @Override
                        public void onError(Exception error) {
                            errorHandling(error, frameLayout);
                        }
                    });
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error, frameLayout);
                }
            });
        }
    }
}