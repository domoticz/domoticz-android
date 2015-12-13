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

package nl.hnogames.domoticz.Preference;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class Preference extends PreferenceFragment {

    private SharedPrefUtil mSharedPrefs;
    private File SettingsFile;
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = Preference.class.getSimpleName();
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG_IMPORT = "Import Settings";
    private final String TAG_EXPORT = "Export Settings";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mSharedPrefs = new SharedPrefUtil(getActivity());

        setStartUpScreenDefaultValue();
        setVersionInfo();
        handleImportExportButtons();
    }


    private void handleImportExportButtons() {
        SettingsFile = new File(Environment.getExternalStorageDirectory(),
                "/Domoticz/DomoticzSettings.txt");
        final String sPath = SettingsFile.getPath().
                substring(0, SettingsFile.getPath().lastIndexOf("/"));
        boolean mkdirsResultIsOk = new File(sPath).mkdirs();
        if (!mkdirsResultIsOk) {
            Toast.makeText(getActivity(),
                    R.string.unable_to_create_directories,
                    Toast.LENGTH_SHORT).show();
        } else {
            android.preference.Preference exportButton = findPreference("export_settings");
            exportButton.setOnPreferenceClickListener(
                    new android.preference.Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(android.preference.Preference preference) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!PermissionsUtil.canAccessStorage(getActivity())) {
                                    requestPermissions(PermissionsUtil.INITIAL_STORAGE_PERMS,
                                            PermissionsUtil.INITIAL_EXPORT_SETTINGS_REQUEST);
                                } else
                                    exportSettings();
                            } else {
                                exportSettings();
                            }
                            return false;
                        }
                    });

            android.preference.Preference importButton = findPreference("import_settings");
            importButton.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!PermissionsUtil.canAccessStorage(getActivity())) {
                            requestPermissions(PermissionsUtil.INITIAL_STORAGE_PERMS,
                                    PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST);
                        } else
                            importSettings();
                    } else {
                        importSettings();
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(getActivity())) {
                    importSettings();
                }
                break;
            case PermissionsUtil.INITIAL_EXPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(getActivity())) {
                    exportSettings();
                }
                break;
        }
    }

    private void importSettings() {
        Log.v(TAG_IMPORT, "Importing settings from: " + SettingsFile.getPath());
        mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile);
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            Toast.makeText(getActivity(),
                    R.string.settings_imported,
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(),
                    R.string.settings_import_failed,
                    Toast.LENGTH_SHORT).show();
    }

    private void exportSettings() {
        Log.v(TAG_EXPORT, "Exporting settings to: " + SettingsFile.getPath());
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            Toast.makeText(getActivity(), "Settings Exported.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getActivity(), "Failed to Export Settings.", Toast.LENGTH_SHORT).show();
    }

    private void setVersionInfo() {
        PackageInfo pInfo = null;
        try {
            pInfo = getActivity()
                    .getPackageManager()
                    .getPackageInfo(getActivity()
                            .getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersion = "";
        if (pInfo != null) appVersion = pInfo.versionName;

        final EditTextPreference version = (EditTextPreference) findPreference("version");
        final EditTextPreference domoticzVersion =
                (EditTextPreference) findPreference("version_domoticz");
        version.setSummary(appVersion);

        Domoticz domoticz = new Domoticz(getActivity());
        domoticz.getVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(String version) {
                try {
                    String sVersion = version;
                    String sUpdateVersion = mSharedPrefs.getUpdateAvailable();
                    if (sUpdateVersion != null && sUpdateVersion.length() > 0)
                        sVersion += "  " + getString(R.string.update_available) + ": " + sUpdateVersion;

                    domoticzVersion.setSummary(sVersion);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onError(Exception error) {
            }
        });
    }

    private void setStartUpScreenDefaultValue() {
        int defaultValue = mSharedPrefs.getStartupScreenIndex();
        ListPreference startup_screen = (ListPreference) findPreference("startup_screen");
        startup_screen.setValueIndex(defaultValue);
    }
}