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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import nl.hnogames.domoticz.Adapters.CamerasAdapter;
import nl.hnogames.domoticz.CameraActivity;
import nl.hnogames.domoticz.Interfaces.DomoticzFragmentListener;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Interfaces.CameraReceiver;

public class Cameras extends DomoticzCardFragment implements DomoticzFragmentListener, OnPermissionCallback {

    @SuppressWarnings("unused")
    private static final String TAG = Cameras.class.getSimpleName();

    private Context context;
    private RecyclerView mRecyclerView;
    private CamerasAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean refreshTimer = false;
    private SharedPrefUtil mSharedPrefs;
    private SlideInBottomAnimationAdapter alphaSlideIn;
    private PermissionFragmentHelper permissionFragmentHelper;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void refreshFragment() {
        refreshTimer = true;
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

        getCameras();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void getCameras() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
        permissionFragmentHelper = PermissionFragmentHelper.getInstance(this);
        new GetCachedDataTask().execute();
    }

    private void processImage(Bitmap savePic, String title) {
        File dir = mDomoticz.saveSnapShot(savePic, title);
        if (dir != null) {
            Intent intent = new Intent(context, CameraActivity.class);
            //noinspection SpellCheckingInspection
            intent.putExtra("IMAGETITLE", title);
            //noinspection SpellCheckingInspection
            intent.putExtra("IMAGEURL", dir.getPath());
            startActivity(intent);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        mSharedPrefs = new SharedPrefUtil(context);
        if (getActionBar() != null)
            getActionBar().setTitle(R.string.title_cameras);
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

    private void createListView(ArrayList<CameraInfo> Cameras) {
        if (getView() == null)
            return;

        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) getView().findViewById(R.id.my_recycler_view);
            mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh_layout);
            mRecyclerView.setHasFixedSize(true);
            GridLayoutManager mLayoutManager = new GridLayoutManager(context, 2);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }

        if (mAdapter == null) {
            mAdapter = new CamerasAdapter(Cameras, context, mDomoticz, refreshTimer);
            mAdapter.setOnItemClickListener(new CamerasAdapter.onClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    if (mPhoneConnectionUtil.isNetworkAvailable()) {
                        try {
                            ImageView cameraImage = (ImageView) v.findViewById(R.id.image);
                            TextView cameraTitle = (TextView) v.findViewById(R.id.name);
                            Bitmap savePic = ((BitmapDrawable) cameraImage.getDrawable()).getBitmap();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!PermissionsUtil.canAccessStorage(context)) {
                                    permissionFragmentHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                                } else
                                    processImage(savePic, cameraTitle.getText().toString());
                            } else {
                                processImage(savePic, cameraTitle.getText().toString());
                            }
                        } catch (Exception ex) {
                            errorHandling(ex, coordinatorLayout);
                        }
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
            mAdapter.setData(Cameras);
            mAdapter.notifyDataSetChanged();
            alphaSlideIn.notifyDataSetChanged();
        }

        mSwipeRefreshLayout.setRefreshing(false);
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
            if (!mPhoneConnectionUtil.isNetworkAvailable()) {
                try {
                    cacheCameras = (ArrayList<CameraInfo>) SerializableManager.readSerializedObject(context, "Cameras");
                } catch (Exception ex) {
                }
            }
            return true;
        }

        protected void onPostExecute(Boolean result) {
            if (cacheCameras != null)
                createListView(cacheCameras);

            mDomoticz.getCameras(new CameraReceiver() {
                @Override
                public void OnReceiveCameras(ArrayList<CameraInfo> Cameras) {
                    successHandling(Cameras.toString(), false);
                    SerializableManager.saveSerializable(context, Cameras, "Cameras");
                    createListView(Cameras);
                }

                @Override
                public void onError(Exception error) {
                    errorHandling(error, coordinatorLayout);
                }
            });
        }
    }
}