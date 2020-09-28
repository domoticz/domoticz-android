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

package nl.hnogames.domoticz.preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codekidlabs.storagechooser.StorageChooser;
import com.fastaccess.permission.base.PermissionHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.HashSet;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.legacy.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import nl.hnogames.domoticz.BeaconSettingsActivity;
import nl.hnogames.domoticz.BluetoothSettingsActivity;
import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.GeoSettingsActivity;
import nl.hnogames.domoticz.NFCSettingsActivity;
import nl.hnogames.domoticz.NotificationSettingsActivity;
import nl.hnogames.domoticz.QRCodeSettingsActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ServerListSettingsActivity;
import nl.hnogames.domoticz.ServerSettingsActivity;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.SpeechSettingsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.ui.SimpleTextDialog;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.LoginInfo;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.LoginReceiver;

import static android.content.Context.KEYGUARD_SERVICE;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = PreferenceFragment.class.getSimpleName();

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG_IMPORT = "Import Settings";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG_EXPORT = "Export Settings";
    private SharedPrefUtil mSharedPrefs;
    private Context mContext;
    private ConfigInfo mConfigInfo;
    private PermissionHelper permissionHelper;
    private StorageChooser.Theme theme;

    private static void tintIcons(Preference preference, int color) {
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup group = ((PreferenceGroup) preference);
            Drawable icon = group.getIcon();
            if (icon != null) {
                icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
            for (int i = 0; i < group.getPreferenceCount(); i++) {
                tintIcons(group.getPreference(i), color);
            }
        } else {
            Drawable icon = preference.getIcon();
            if (icon != null) {
                icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        permissionHelper = PermissionHelper.getInstance(getActivity());

        mContext = getActivity();

        mSharedPrefs = new SharedPrefUtil(mContext);
        mConfigInfo = StaticHelper.getServerUtil(getActivity()).getActiveServer().getConfigInfo(mContext);

        UsefulBits.checkAPK(mContext, mSharedPrefs);

        SetStorageTheme();
        setIconColor();
        setPreferences();
        setStartUpScreenDefaultValue();
        handleImportExportButtons();
        handleInfoAndAbout();
        GetVersion();
    }

    private void SetStorageTheme() {
        theme = new StorageChooser.Theme(mContext);
        int[] scheme = new int[16];
        TypedValue typedValue = new TypedValue();
        Resources.Theme currentTheme = mContext.getTheme();
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

    private void setIconColor() {
        int colorAttr = R.attr.preferenceIconColor;
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(new int[]{colorAttr});
        int color = ta.getColor(0, 0);
        ta.recycle();
        tintIcons(getPreferenceScreen(), color);
    }

    private void setupDefaultValues() {
        nl.hnogames.domoticz.preference.EditTextIntegerPreference oTemperatureMin = findPreference("tempMinValue");
        nl.hnogames.domoticz.preference.EditTextIntegerPreference oTemperatureMax = findPreference("tempMaxValue");
        oTemperatureMin.setText(mSharedPrefs.getTemperatureSetMin(mConfigInfo.getTempSign()) + "");
        oTemperatureMax.setText(mSharedPrefs.getTemperatureSetMax(mConfigInfo.getTempSign()) + "");
        oTemperatureMax.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(androidx.preference.Preference preference, Object o) {
                int newMaxValue = Integer.valueOf(o + "");
                int existingMinValue = mSharedPrefs.getTemperatureSetMin(mConfigInfo.getTempSign());

                if (newMaxValue > existingMinValue)
                    return true;
                else
                    Toast.makeText(mContext, mContext.getString(R.string.default_values_max_error), Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        oTemperatureMin.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(androidx.preference.Preference preference, Object o) {
                int newMinValue = Integer.valueOf(o + "");
                int existingMaxValue = mSharedPrefs.getTemperatureSetMax(mConfigInfo.getTempSign());
                if (newMinValue < existingMaxValue)
                    return true;
                else
                    Toast.makeText(mContext, mContext.getString(R.string.default_values_min_error), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void setPreferences() {
        final androidx.preference.SwitchPreference MultiServerPreference = findPreference("enableMultiServers");
        androidx.preference.Preference ServerSettings = findPreference("server_settings");
        androidx.preference.Preference PermissionsSettings = findPreference("permissionssettings");
        androidx.preference.Preference fetchServerConfig = findPreference("server_force_fetch_config");
        androidx.preference.Preference resetApplication = findPreference("reset_settings");
        androidx.preference.Preference translateApplication = findPreference("translate");
        ListPreference displayLanguage = findPreference("displayLanguage");
        androidx.preference.Preference ReportErrorSettings = findPreference("report");
        androidx.preference.Preference GeoSettings = findPreference("geo_settings");
        androidx.preference.SwitchPreference WearPreference = findPreference("enableWearItems");
        androidx.preference.SwitchPreference WidgetsEnablePreference = findPreference("enableWidgets");
        androidx.preference.Preference NFCPreference = findPreference("nfc_settings");
        androidx.preference.Preference QRCodePreference = findPreference("qrcode_settings");
        androidx.preference.Preference BluetoothPreference = findPreference("bluetooth_settings");
        androidx.preference.Preference BeaconPreference = findPreference("beacon_settings");
        androidx.preference.Preference SpeechPreference = findPreference("speech_settings");
        androidx.preference.SwitchPreference EnableNFCPreference = findPreference("enableNFC");
        androidx.preference.SwitchPreference EnableQRCodePreference = findPreference("enableQRCode");
        androidx.preference.SwitchPreference EnableBluetoothPreference = findPreference("enableBluetooth");
        androidx.preference.SwitchPreference EnableBeaconPreference = findPreference("enableBeacon");
        androidx.preference.SwitchPreference EnableSpeechPreference = findPreference("enableSpeech");
        androidx.preference.SwitchPreference EnableTalkBackPreference = findPreference("talkBack");
        MultiSelectListPreference drawerItems = findPreference("show_nav_items");
        @SuppressWarnings("SpellCheckingInspection") androidx.preference.SwitchPreference AlwaysOnPreference = findPreference("alwayson");
        @SuppressWarnings("SpellCheckingInspection") androidx.preference.SwitchPreference RefreshScreenPreference = findPreference("autorefresh");
        @SuppressWarnings("SpellCheckingInspection") androidx.preference.PreferenceScreen preferenceScreen = findPreference("settingsscreen");
        androidx.preference.PreferenceCategory premiumCategory = findPreference("premium_category");
        androidx.preference.Preference premiumPreference = findPreference("premium_settings");
        androidx.preference.Preference taskerPreference = findPreference("tasker_settings");
        androidx.preference.Preference ThemePreference = findPreference("darkTheme");
        androidx.preference.SwitchPreference ClockPreference = findPreference("dashboardShowClock");
        androidx.preference.SwitchPreference CameraPreference = findPreference("dashboardShowCamera");

        androidx.preference.Preference FingerPrintSettingsPreference = findPreference("SecuritySettings");
        androidx.preference.SwitchPreference FingerPrintPreference = findPreference("enableSecurity");
        androidx.preference.SwitchPreference customSortProperty = findPreference("sortCustom");
        androidx.preference.Preference openNotificationSettings = findPreference("openNotificationSettings");

        if (mConfigInfo == null) {
            StaticHelper.getDomoticz(mContext).checkLogin(new LoginReceiver() {
                @Override
                public void OnReceive(LoginInfo mLoginInfo) {
                    UsefulBits.getServerConfigForActiveServer(mContext, mLoginInfo, new ConfigReceiver() {
                        @Override
                        public void onReceiveConfig(ConfigInfo settings) {
                            mConfigInfo = settings;
                            setupDefaultValues();
                        }

                        @Override
                        public void onError(Exception error) {
                        }
                    }, StaticHelper.getServerUtil(mContext).getActiveServer().getConfigInfo(mContext));
                }

                @Override
                public void onError(Exception error) {
                }
            });

        } else {
            setupDefaultValues();
        }

        if (PermissionsSettings != null)
            PermissionsSettings.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    permissionHelper.openSettingsScreen();
                    return true;
                }
            });

        if (drawerItems != null)
            drawerItems.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    try {
                        final HashSet selectedDrawerItems = (HashSet) newValue;
                        if (selectedDrawerItems.size() < 1) {
                            showSnackbar(mContext.getString(R.string.error_atLeastOneItemInDrawer));
                            return false;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return false;
                    }
                    return true;
                }
            });


        if (openNotificationSettings != null)
            openNotificationSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(mContext, NotificationSettingsActivity.class));
                    return true;
                }
            });

        if (customSortProperty != null)
            customSortProperty.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.sort_custom_on));
                        return false;
                    } else {
                        return true;
                    }
                }
            });


        if (ThemePreference != null)
            ThemePreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_theme));
                        return false;
                    } else {
                        ((SettingsActivity) getActivity()).openThemePicker();
                        return true;
                    }
                }
            });

        if (ClockPreference != null)
            ClockPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_clock));
                        return false;
                    }
                    return true;
                }
            });

        if (CameraPreference != null)
            CameraPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.dashboard_camera));
                        return false;
                    }
                    return true;
                }
            });

        if (MultiServerPreference != null)
            MultiServerPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.multi_server));
                        return false;
                    }
                    return true;
                }
            });

        if (ServerSettings != null)
            ServerSettings.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (!MultiServerPreference.isChecked()) {
                        Intent intent = new Intent(mContext, ServerSettingsActivity.class);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, ServerListSettingsActivity.class);
                        startActivity(intent);
                    }
                    return true;
                }
            });


        if (FingerPrintSettingsPreference != null)
            FingerPrintSettingsPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    mContext.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                    return true;
                }
            });

        if (fetchServerConfig != null)
            fetchServerConfig.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    StaticHelper.getDomoticz(mContext).checkLogin(new LoginReceiver() {
                        @Override
                        public void OnReceive(LoginInfo mLoginInfo) {
                            UsefulBits.getServerConfigForActiveServer(mContext, mLoginInfo, new ConfigReceiver() {
                                @Override
                                public void onReceiveConfig(ConfigInfo settings) {
                                    showSnackbar(mContext.getString(R.string.fetched_server_config_success));
                                }

                                @Override
                                public void onError(Exception error) {
                                    showSnackbar(mContext.getString(R.string.fetched_server_config_failed));
                                }
                            }, null);
                        }

                        @Override
                        public void onError(Exception error) {
                        }
                    });
                    return true;
                }
            });

        if (displayLanguage != null)
            displayLanguage.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    showRestartMessage();
                    return true;
                }
            });

        if (GeoSettings != null)
            GeoSettings.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.geofence));
                        return false;
                    } else {
                        Intent intent = new Intent(mContext, GeoSettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            });

        if (EnableNFCPreference != null)
            EnableNFCPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_nfc));
                        return false;
                    }

                    if (NfcAdapter.getDefaultAdapter(mContext) == null) {
                        showSnackbar(mContext.getString(R.string.nfc_not_supported));
                        return false;
                    }
                    return true;
                }
            });

        if (EnableBluetoothPreference != null)
            EnableBluetoothPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_bluetooth));
                        return false;
                    }
                    return true;
                }
            });

        if (EnableBeaconPreference != null)
            EnableBeaconPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.beacon));
                        return false;
                    }

                    if (!((boolean) newValue))
                        AppController.getInstance().StopBeaconScanning();
                    else {
                        try {
                            AppController.getInstance().StartBeaconScanning();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            });

        if (EnableQRCodePreference != null)
            EnableQRCodePreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_QRCode));
                        return false;
                    }

                    return true;
                }
            });

        if (EnableSpeechPreference != null)
            EnableSpeechPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_Speech));
                        return false;
                    }
                    return true;
                }
            });

        if (EnableTalkBackPreference != null)
            EnableTalkBackPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_talk_back));
                        return false;
                    }
                    return true;
                }
            });

        if (NFCPreference != null)
            NFCPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_nfc));
                        return false;
                    } else {
                        Intent intent = new Intent(mContext, NFCSettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            });

        if (QRCodePreference != null)
            QRCodePreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_QRCode));
                        return false;
                    } else {
                        Intent intent = new Intent(mContext, QRCodeSettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            });

        if (BluetoothPreference != null)
            BluetoothPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_bluetooth));
                        return false;
                    } else {
                        Intent intent = new Intent(mContext, BluetoothSettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            });

        if (BeaconPreference != null)
            BeaconPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.beacon));
                        return false;
                    } else {
                        Intent intent = new Intent(mContext, BeaconSettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            });

        if (SpeechPreference != null)
            SpeechPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_Speech));
                        return false;
                    } else {
                        Intent intent = new Intent(mContext, SpeechSettingsActivity.class);
                        startActivity(intent);
                        return true;
                    }
                }
            });

        if (WidgetsEnablePreference != null)
            WidgetsEnablePreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_widgets));
                        return false;
                    } else {
                        if ((boolean) newValue) {
                            new MaterialDialog.Builder(mContext)
                                    .title(R.string.wizard_widgets)
                                    .content(R.string.widget_warning)
                                    .positiveText(R.string.ok)
                                    .negativeText(R.string.cancel)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mSharedPrefs.SetWidgetsEnabled(true);
                                            ((SettingsActivity) getActivity()).reloadSettings();
                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            mSharedPrefs.SetWidgetsEnabled(false);
                                        }
                                    })
                                    .show();
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            });

        if (WearPreference != null)
            WearPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_wear));
                        return false;
                    }
                    return true;
                }
            });

        if (AlwaysOnPreference != null)
            AlwaysOnPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.always_on_title));
                        return false;
                    }
                    return true;
                }
            });

        if (RefreshScreenPreference != null)
            RefreshScreenPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.always_auto_refresh));
                        return false;
                    }
                    return true;
                }
            });

        if (!BuildConfig.LITE_VERSION) {
            if (preferenceScreen != null && premiumCategory != null)
                preferenceScreen.removePreference(premiumCategory);
        } else {
            if (premiumPreference != null)
                premiumPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(androidx.preference.Preference preference) {
                        String packageID = mContext.getPackageName() + ".premium";
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageID)));
                        } catch (android.content.ActivityNotFoundException ignored) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageID)));
                        }
                        return true;
                    }
                });
        }

        if (ReportErrorSettings != null)
            ReportErrorSettings.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (BuildConfig.PAID_OOTT)
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.oott.hu/ereport")));
                    else
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.domoticz.com/forum/")));
                    return true;
                }
            });

        if (taskerPreference != null)
            taskerPreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=nl.hnogames.domoticz.tasker")));
                    } catch (android.content.ActivityNotFoundException ignored) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=nl.hnogames.domoticz.tasker")));
                    }
                    return true;
                }
            });

        if (translateApplication != null)
            translateApplication.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("https://crowdin.com/project/domoticz-for-android"));
                    startActivity(i);
                    return true;
                }
            });

        if (resetApplication != null)
            resetApplication.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        new MaterialDialog.Builder(mContext)
                                .title(R.string.category_Reset)
                                .content(R.string.are_you_sure_clear_settings)
                                .positiveText(R.string.ok)
                                .negativeText(R.string.cancel)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @SuppressLint("NewApi")
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE))
                                                .clearApplicationUserData();
                                    }
                                })
                                .show();
                    } else {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                    }
                    return true;
                }
            });

        if (FingerPrintPreference != null)
            FingerPrintPreference.setOnPreferenceChangeListener(new androidx.preference.Preference.OnPreferenceChangeListener() {
                @SuppressLint("NewApi")
                @Override
                public boolean onPreferenceChange(androidx.preference.Preference preference, Object newValue) {
                    if (mSharedPrefs.isStartupSecurityEnabled())
                        return true;
                    if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                        showPremiumSnackbar(getString(R.string.category_startup_security));
                        return false;
                    } else {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !checkBiometricSupport()) {
                            UsefulBits.showSimpleToast(mContext, getString(R.string.fingerprint_not_supported), Toast.LENGTH_LONG);
                            return false;
                        }
                        if (!PermissionsUtil.canAccessFingerprint(mContext)) {
                            permissionHelper.request(PermissionsUtil.INITIAL_FINGERPRINT_PERMS);
                        } else {
                            BiometricManager biometricManager = BiometricManager.from(mContext);
                            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                                new MaterialDialog.Builder(mContext)
                                        .title(R.string.category_startup_security)
                                        .content(R.string.fingerprint_sure)
                                        .positiveText(R.string.ok)
                                        .negativeText(R.string.cancel)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                mSharedPrefs.setStartupSecurityEnabled(true);
                                                if (getActivity() != null)
                                                    ((SettingsActivity) getActivity()).reloadSettings();
                                            }
                                        })
                                        .show();
                                return false;
                            } else {

                                UsefulBits.showSimpleToast(mContext, getString(R.string.fingerprint_not_setup_in_android), Toast.LENGTH_LONG);
                            }
                        }
                    }
                    return false;
                }
            });
    }

    private void GetVersion() {
        androidx.preference.Preference appVersion = findPreference("version");
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
        if (appVersion != null && !UsefulBits.isEmpty(appVersionStr))
            appVersion.setSummary(appVersionStr);
    }

    private Boolean checkBiometricSupport() {
        KeyguardManager keyguardManager = (KeyguardManager) mContext.getSystemService(KEYGUARD_SERVICE);
        PackageManager packageManager = mContext.getPackageManager();
        if (keyguardManager == null || !keyguardManager.isKeyguardSecure()) {
            UsefulBits.showSimpleToast(mContext, "Lock screen security not enabled in Settings", Toast.LENGTH_LONG);
            return false;
        }
        if (ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.USE_BIOMETRIC) !=
                PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT))
            return true;
        return true;
    }

    private void showRestartMessage() {
        new MaterialDialog.Builder(mContext)
                .title(R.string.restart_required_title)
                .content(mContext.getString(R.string.restart_required_msg)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + mContext.getString(R.string.restart_now))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UsefulBits.restartApplication(getActivity());
                    }
                })
                .show();
    }

    private void handleInfoAndAbout() {
        androidx.preference.Preference about = findPreference("info_about");

        if (about != null)
            about.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    SimpleTextDialog td = new SimpleTextDialog(mContext);
                    td.setTitle(R.string.info_about);
                    td.setText(R.string.welcome_info_domoticz);
                    td.show();
                    return true;
                }
            });
        androidx.preference.Preference credits = findPreference("info_credits");

        if (credits != null)
            credits.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
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
        androidx.preference.Preference exportButton = findPreference("export_settings");
        if (exportButton != null)
            exportButton.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!PermissionsUtil.canAccessStorage(mContext)) {
                            permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                        } else
                            exportSettings();
                    } else
                        exportSettings();

                    return false;
                }
            });
        androidx.preference.Preference importButton = findPreference("import_settings");
        if (importButton != null)
            importButton.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!PermissionsUtil.canAccessStorage(mContext)) {
                            permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                        } else
                            importSettings();
                    } else
                        importSettings();

                    return false;
                }
            });
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
                Log.v(TAG_IMPORT, "Importing settings from: " + path);
                if (mSharedPrefs.loadSharedPreferencesFromFile(new File(path))) {
                    ((SettingsActivity) getActivity()).reloadSettings();
                    showSnackbar(mContext.getString(R.string.settings_imported));
                } else
                    showSnackbar(mContext.getString(R.string.settings_import_failed));
            }
        });
    }

    private void exportSettings() {
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(getActivity())
                .withFragmentManager(getActivity().getFragmentManager())
                .allowAddFolder(true)
                .withMemoryBar(false)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .setTheme(theme)
                .build();
        chooser.show();
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(final String path) {
                new MaterialDialog.Builder(mContext)
                        .title(R.string.save_as)
                        .content(R.string.filename)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("settings.txt", "settings.txt", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                File newFile = new File(path, "/" + input);
                                if (mSharedPrefs.saveSharedPreferencesToFile(newFile))
                                    showSnackbar(mContext.getString(R.string.settings_exported));
                                else
                                    showSnackbar(mContext.getString(R.string.settings_export_failed));
                            }
                        }).show();
            }
        });
    }

    private void setStartUpScreenDefaultValue() {
        int defaultValue = mSharedPrefs.getActualStartupScreenIndex();
        ListPreference startup_screen = findPreference("startup_nav");
        startup_screen.setValueIndex(defaultValue);
    }

    private void showPremiumSnackbar(final String category) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null) {
                        Snackbar.make(getView(), category + " " + getString(R.string.premium_feature), Snackbar.LENGTH_LONG)
                                .setAction(R.string.upgrade, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        UsefulBits.openPremiumAppStore(mContext);
                                    }
                                })
                                .setActionTextColor(ContextCompat.getColor(mContext, R.color.material_blue_600))
                                .show();
                    }
                }
            }, (300));
        } catch (Exception ex) {
            Log.e(TAG, "No Snackbar shown: " + ex.getMessage());
        }
    }

    private void showSnackbar(final String text) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null) {
                        Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
                    }
                }
            }, (300));
        } catch (Exception ex) {
            Log.e(TAG, "No Snackbar shown: " + ex.getMessage());
        }
    }
}