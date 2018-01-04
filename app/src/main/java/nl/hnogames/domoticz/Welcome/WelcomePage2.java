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

package nl.hnogames.domoticz.Welcome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;

import java.io.File;
import java.util.Arrays;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class WelcomePage2 extends Fragment implements OnPermissionCallback {

    private File SettingsFile;
    private PermissionFragmentHelper permissionFragmentHelper;

    public static WelcomePage2 newInstance() {
        return new WelcomePage2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome2, container, false);
        SettingsFile = new File(Environment.getExternalStorageDirectory(), "/Domoticz/DomoticzSettings.txt");

        permissionFragmentHelper = PermissionFragmentHelper.getInstance(this);
        Button importButton = (Button) v.findViewById(R.id.import_settings);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessStorage(getActivity())) {
                        permissionFragmentHelper
                                .request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                    } else {
                        importSettings();
                    }
                } else {
                    importSettings();
                }
            }
        });
        if (!SettingsFile.exists()) {
            importButton.setVisibility(View.GONE);
        }
        return v;
    }

    private void importSettings() {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
        if (mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile)) {
            Toast.makeText(getActivity(), R.string.settings_imported, Toast.LENGTH_LONG).show();
            ((WelcomeViewActivity) getActivity()).finishWithResult(true);
        } else
            Toast.makeText(getActivity(), R.string.settings_import_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.i("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
        String[] neededPermission = PermissionFragmentHelper.declinedPermissions(this, PermissionsUtil.INITIAL_STORAGE_PERMS);
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
        if (PermissionsUtil.canAccessStorage(getActivity()))
            importSettings();
    }
}