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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.GeoSettingsActivity;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ServerSettingsActivity;
import nl.hnogames.domoticz.UI.SimpleTextDialog;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class Preference extends PreferenceFragment {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = Preference.class.getSimpleName();
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG_IMPORT = "Import Settings";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG_EXPORT = "Export Settings";
    private SharedPrefUtil mSharedPrefs;
    private File SettingsFile;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mContext = getActivity();
        mSharedPrefs = new SharedPrefUtil(mContext);

        setPreferences();
        setStartUpScreenDefaultValue();
        setVersionInfo();
        handleImportExportButtons();
        handleInfoAndAbout();
    }

    private void setPreferences() {

        android.preference.Preference ServerSettings = findPreference("server_settings");
        ServerSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Intent intent = new Intent(mContext, ServerSettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });

        android.preference.Preference registrationId = findPreference("notification_registration_id");
        registrationId.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                String id = mSharedPrefs.getNotificationRegistrationID();
                Toast.makeText(mContext, mContext.getString(R.string.notification_settings_copied) + ": " + id, Toast.LENGTH_SHORT).show();

                UsefulBits.copyToClipboard(mContext, "id", id);
                return true;
            }
        });

        android.preference.Preference GeoSettings = findPreference("geo_settings");
        GeoSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.geofence) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    Intent intent = new Intent(mContext, GeoSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            }
        });

        android.preference.SwitchPreference WearPreference = (android.preference.SwitchPreference) findPreference("enableWearItems");
        WearPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        android.preference.SwitchPreference AlwaysOnPreference = (android.preference.SwitchPreference) findPreference("alwayson");
        AlwaysOnPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION) {
                    Toast.makeText(mContext, getString(R.string.category_wear) + " " + getString(R.string.premium_feature), Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }
        });

        android.preference.PreferenceScreen preferenceScreen = (android.preference.PreferenceScreen) findPreference("settingsscreen");
        android.preference.PreferenceCategory premiumCategory = (android.preference.PreferenceCategory) findPreference("premium_category");
        android.preference.Preference premiumPreference = findPreference("premium_settings");
        //noinspection PointlessBooleanExpression
        if (!BuildConfig.LITE_VERSION) {
            preferenceScreen.removePreference(premiumCategory);
        } else {
            premiumPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(android.preference.Preference preference) {
                    String packageID = mContext.getPackageName() + ".premium";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageID)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageID)));
                    }

                    return true;
                }
            });
        }
    }

    private void handleInfoAndAbout() {
        android.preference.Preference about = findPreference("info_about");
        about.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                SimpleTextDialog td = new SimpleTextDialog(mContext);
                td.setTitle(R.string.info_about);
                td.setText(R.string.welcome_info_domoticz);
                td.show();
                return true;
            }
        });
        android.preference.Preference credits = findPreference("info_credits");
        credits.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                String text = getString(R.string.info_credits_text);
                text = text + ":\n\n" + getString(R.string.info_credits_text_urls);

                SimpleTextDialog td = new SimpleTextDialog(mContext);
                td.setTitle(R.string.info_credits);
                td.setText(text);
                td.show();
                return false;
            }
        });
    }

    private void handleImportExportButtons() {
        SettingsFile = new File(Environment.getExternalStorageDirectory(),
                "/Domoticz/DomoticzSettings.txt");
        final String sPath = SettingsFile.getPath().
                substring(0, SettingsFile.getPath().lastIndexOf("/"));
        //noinspection unused
        boolean mkdirsResultIsOk = new File(sPath).mkdirs();

        android.preference.Preference exportButton = findPreference("export_settings");
        exportButton.setOnPreferenceClickListener(
                new android.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(android.preference.Preference preference) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!PermissionsUtil.canAccessStorage(mContext)) {
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
                    if (!PermissionsUtil.canAccessStorage(mContext)) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_IMPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(mContext)) {
                    importSettings();
                }
                break;
            case PermissionsUtil.INITIAL_EXPORT_SETTINGS_REQUEST:
                if (PermissionsUtil.canAccessStorage(mContext)) {
                    exportSettings();
                }
                break;
        }
    }

    private void importSettings() {
        Log.v(TAG_IMPORT, "Importing settings from: " + SettingsFile.getPath());
        mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile);
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            Toast.makeText(mContext,
                    R.string.settings_imported,
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext,
                    R.string.settings_import_failed,
                    Toast.LENGTH_SHORT).show();
    }

    private void exportSettings() {
        Log.v(TAG_EXPORT, "Exporting settings to: " + SettingsFile.getPath());
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            Toast.makeText(mContext, "Settings Exported.", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(mContext, "Failed to Export Settings.", Toast.LENGTH_SHORT).show();
    }

    private void setVersionInfo() {
        PackageInfo pInfo = null;
        try {
            pInfo = mContext
                    .getPackageManager()
                    .getPackageInfo(mContext
                            .getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String appVersionStr = mContext.getString(R.string.unknown);
        if (pInfo != null) appVersionStr = pInfo.versionName;

        final android.preference.Preference appVersion = findPreference("version");
        final android.preference.Preference domoticzVersion = findPreference("version_domoticz");
        appVersion.setSummary(appVersionStr);

        Domoticz domoticz = new Domoticz(mContext);
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