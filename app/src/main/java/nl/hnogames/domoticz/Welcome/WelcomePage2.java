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

package nl.hnogames.domoticz.Welcome;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class WelcomePage2 extends Fragment {

    private File SettingsFile;

    public static WelcomePage2 newInstance() {
        return new WelcomePage2();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_welcome2, container, false);
        SettingsFile = new File(Environment.getExternalStorageDirectory(), "/Domoticz/DomoticzSettings.txt");

        Button importButton = (Button) v.findViewById(R.id.import_settings);
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessStorage(getActivity())) {
                        requestPermissions(PermissionsUtil.INITIAL_STORAGE_PERMS, PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST);
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
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(getActivity())) {
                    importSettings();
                }
                break;
        }
    }
}