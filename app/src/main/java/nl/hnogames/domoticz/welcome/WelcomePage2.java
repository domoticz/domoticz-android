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

package nl.hnogames.domoticz.welcome;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;
import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class WelcomePage2 extends Fragment implements OnPermissionCallback {
    private PermissionFragmentHelper permissionFragmentHelper;
    private StorageChooser.Theme theme;

    public static WelcomePage2 newInstance() {
        return new WelcomePage2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome2, container, false);
        SetStorageTheme();

        permissionFragmentHelper = PermissionFragmentHelper.getInstance(this);
        MaterialButton importButton = v.findViewById(R.id.import_settings);
        MaterialButton demoSetup = v.findViewById(R.id.demo_settings);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessStorage(getActivity()))
                        permissionFragmentHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                    else
                        importSettings();
                } else
                    importSettings();
            }
        });

        demoSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((WelcomeViewActivity) getActivity()).setDemoAccount();
            }
        });
        return v;
    }

    private void SetStorageTheme() {
        theme = new StorageChooser.Theme(getContext());
        int[] scheme = new int[16];
        TypedValue typedValue = new TypedValue();
        Resources.Theme currentTheme = getContext().getTheme();
        currentTheme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        scheme[0] = typedValue.data;// header background
        currentTheme.resolveAttribute(R.attr.temperatureTextColor, typedValue, true);
        scheme[1] = typedValue.data;// header text
        currentTheme.resolveAttribute(R.attr.md_background_color, typedValue, true);
        scheme[2] = typedValue.data;//list bg
        currentTheme.resolveAttribute(R.attr.temperatureTextColor, typedValue, true);
        scheme[3] = typedValue.data;//storage list name text
        currentTheme.resolveAttribute(R.attr.temperatureTextColor, typedValue, true);
        scheme[4] = typedValue.data;//free space text
        currentTheme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        scheme[5] = typedValue.data;//memory bar
        currentTheme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        scheme[6] = typedValue.data;//folder tint
        currentTheme.resolveAttribute(R.attr.md_background_color, typedValue, true);
        scheme[7] = typedValue.data;// list bg
        currentTheme.resolveAttribute(R.attr.temperatureTextColor, typedValue, true);
        scheme[8] = typedValue.data;//list text
        currentTheme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        scheme[9] = typedValue.data;//address bar tint
        currentTheme.resolveAttribute(R.attr.temperatureTextColor, typedValue, true);
        scheme[10] = typedValue.data;//folder hint tint
        currentTheme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        scheme[11] = typedValue.data;//elect button color
        currentTheme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        scheme[12] = typedValue.data;//select button color
        currentTheme.resolveAttribute(R.attr.md_background_color, typedValue, true);
        scheme[13] = typedValue.data;//new folder layour bg
        currentTheme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        scheme[14] = typedValue.data;//fab multiselect color
        currentTheme.resolveAttribute(R.attr.md_background_color, typedValue, true);
        scheme[15] = typedValue.data;//address bar bg
        theme.setScheme(scheme);
    }

    private void importSettings() {
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getActivity().getFragmentManager())
                .withMemoryBar(false)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .setTheme(theme)
                .build();
        chooser.show();
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Log.v("Import", "Importing settings from: " + path);
                SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getActivity());
                if (mSharedPrefs.loadSharedPreferencesFromFile(new File(path))) {
                    Toast.makeText(getActivity(), R.string.settings_imported, Toast.LENGTH_LONG).show();
                    ((WelcomeViewActivity) getActivity()).finishWithResult(true);
                } else
                    Toast.makeText(getActivity(), R.string.settings_import_failed, Toast.LENGTH_SHORT).show();
            }
        });
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