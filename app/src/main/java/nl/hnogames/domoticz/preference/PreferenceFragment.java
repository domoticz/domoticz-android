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
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;

import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.legacy.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
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

        setIconColor();
        setPreferences();
        setStartUpScreenDefaultValue();
        handleImportExportButtons();
        handleInfoAndAbout();
        GetVersion();
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
        oTemperatureMax.setOnPreferenceChangeListener((preference, o) -> {
            int newMaxValue = Integer.valueOf(o + "");
            int existingMinValue = mSharedPrefs.getTemperatureSetMin(mConfigInfo.getTempSign());

            if (newMaxValue > existingMinValue)
                return true;
            else
                Toast.makeText(mContext, mContext.getString(R.string.default_values_max_error), Toast.LENGTH_SHORT).show();
            return false;
        });

        oTemperatureMin.setOnPreferenceChangeListener((preference, o) -> {
            int newMinValue = Integer.valueOf(o + "");
            int existingMaxValue = mSharedPrefs.getTemperatureSetMax(mConfigInfo.getTempSign());
            if (newMinValue < existingMaxValue)
                return true;
            else
                Toast.makeText(mContext, mContext.getString(R.string.default_values_min_error), Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private void setPreferences() {
        final SwitchPreference MultiServerPreference = findPreference("enableMultiServers");
        Preference ServerSettings = findPreference("server_settings");
        Preference PermissionsSettings = findPreference("permissionssettings");
        Preference fetchServerConfig = findPreference("server_force_fetch_config");
        Preference resetApplication = findPreference("reset_settings");
        Preference translateApplication = findPreference("translate");
        ListPreference displayLanguage = findPreference("displayLanguage");
        Preference ReportErrorSettings = findPreference("report");
        Preference GeoSettings = findPreference("geo_settings");
        SwitchPreference WearPreference = findPreference("enableWearItems");
        SwitchPreference WidgetsEnablePreference = findPreference("enableWidgets");
        Preference NFCPreference = findPreference("nfc_settings");
        Preference QRCodePreference = findPreference("qrcode_settings");
        Preference BluetoothPreference = findPreference("bluetooth_settings");
        Preference BeaconPreference = findPreference("beacon_settings");
        Preference SpeechPreference = findPreference("speech_settings");
        SwitchPreference EnableNFCPreference = findPreference("enableNFC");
        SwitchPreference EnableQRCodePreference = findPreference("enableQRCode");
        SwitchPreference EnableBluetoothPreference = findPreference("enableBluetooth");
        SwitchPreference EnableBeaconPreference = findPreference("enableBeacon");
        SwitchPreference EnableSpeechPreference = findPreference("enableSpeech");
        SwitchPreference EnableTalkBackPreference = findPreference("talkBack");
        MultiSelectListPreference drawerItems = findPreference("show_nav_items");
        @SuppressWarnings("SpellCheckingInspection") SwitchPreference AlwaysOnPreference = findPreference("alwayson");
        @SuppressWarnings("SpellCheckingInspection") SwitchPreference RefreshScreenPreference = findPreference("autorefresh");
        @SuppressWarnings("SpellCheckingInspection") PreferenceScreen preferenceScreen = findPreference("settingsscreen");
        PreferenceCategory premiumCategory = findPreference("premium_category");
        Preference premiumPreference = findPreference("premium_settings");
        Preference taskerPreference = findPreference("tasker_settings");
        Preference ThemePreference = findPreference("darkTheme");
        SwitchPreference ClockPreference = findPreference("dashboardShowClock");
        SwitchPreference CameraPreference = findPreference("dashboardShowCamera");
        Preference TermsPreferences = findPreference("info_terms");
        Preference PrivacyPreferences = findPreference("info_privacy");
        Preference FingerPrintSettingsPreference = findPreference("SecuritySettings");
        SwitchPreference FingerPrintPreference = findPreference("enableSecurity");
        SwitchPreference customSortProperty = findPreference("sortCustom");
        Preference openNotificationSettings = findPreference("openNotificationSettings");

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
            PermissionsSettings.setOnPreferenceClickListener(preference -> {
                permissionHelper.openSettingsScreen();
                return true;
            });

        if (drawerItems != null)
            drawerItems.setOnPreferenceChangeListener((preference, newValue) -> {
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
            });


        if (openNotificationSettings != null)
            openNotificationSettings.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(mContext, NotificationSettingsActivity.class));
                return true;
            });

        if (customSortProperty != null)
            customSortProperty.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.sort_custom_on));
                    return false;
                } else {
                    return true;
                }
            });


        if (ThemePreference != null)
            ThemePreference.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_theme));
                    return false;
                } else {
                    ((SettingsActivity) getActivity()).openThemePicker();
                    return true;
                }
            });

        if (ClockPreference != null)
            ClockPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_clock));
                    return false;
                }
                return true;
            });

        if (CameraPreference != null)
            CameraPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.dashboard_camera));
                    return false;
                }
                return true;
            });

        if (MultiServerPreference != null)
            MultiServerPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.multi_server));
                    return false;
                }
                return true;
            });

        if (ServerSettings != null)
            ServerSettings.setOnPreferenceClickListener(preference -> {
                if (!MultiServerPreference.isChecked()) {
                    Intent intent = new Intent(mContext, ServerSettingsActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, ServerListSettingsActivity.class);
                    startActivity(intent);
                }
                return true;
            });


        if (FingerPrintSettingsPreference != null)
            FingerPrintSettingsPreference.setOnPreferenceClickListener(preference -> {
                mContext.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                return true;
            });

        if (fetchServerConfig != null)
            fetchServerConfig.setOnPreferenceClickListener(preference -> {
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
            });

        if (displayLanguage != null)
            displayLanguage.setOnPreferenceChangeListener((preference, newValue) -> {
                showRestartMessage();
                return true;
            });

        if (GeoSettings != null)
            GeoSettings.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.geofence));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, GeoSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (EnableNFCPreference != null)
            EnableNFCPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_nfc));
                    return false;
                }

                if (NfcAdapter.getDefaultAdapter(mContext) == null) {
                    showSnackbar(mContext.getString(R.string.nfc_not_supported));
                    return false;
                }
                return true;
            });

        if (EnableBluetoothPreference != null)
            EnableBluetoothPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_bluetooth));
                    return false;
                }
                return true;
            });

        if (EnableBeaconPreference != null)
            EnableBeaconPreference.setOnPreferenceChangeListener((preference, newValue) -> {
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
            });

        if (EnableQRCodePreference != null)
            EnableQRCodePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_QRCode));
                    return false;
                }

                return true;
            });

        if (EnableSpeechPreference != null)
            EnableSpeechPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_Speech));
                    return false;
                }
                return true;
            });

        if (EnableTalkBackPreference != null)
            EnableTalkBackPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_talk_back));
                    return false;
                }
                return true;
            });

        if (NFCPreference != null)
            NFCPreference.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_nfc));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, NFCSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (QRCodePreference != null)
            QRCodePreference.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_QRCode));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, QRCodeSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (BluetoothPreference != null)
            BluetoothPreference.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_bluetooth));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, BluetoothSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (BeaconPreference != null)
            BeaconPreference.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.beacon));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, BeaconSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (SpeechPreference != null)
            SpeechPreference.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_Speech));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, SpeechSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (WidgetsEnablePreference != null)
            WidgetsEnablePreference.setOnPreferenceChangeListener((preference, newValue) -> {
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
                                .onPositive((dialog, which) -> {
                                    mSharedPrefs.SetWidgetsEnabled(true);
                                    ((SettingsActivity) getActivity()).reloadSettings();
                                })
                                .onNegative((dialog, which) -> mSharedPrefs.SetWidgetsEnabled(false))
                                .show();
                        return false;
                    } else {
                        return true;
                    }
                }
            });

        if (WearPreference != null)
            WearPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_wear));
                    return false;
                }
                return true;
            });

        if (AlwaysOnPreference != null)
            AlwaysOnPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.always_on_title));
                    return false;
                }
                return true;
            });

        if (RefreshScreenPreference != null)
            RefreshScreenPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.always_auto_refresh));
                    return false;
                }
                return true;
            });

        if (!BuildConfig.LITE_VERSION) {
            if (preferenceScreen != null && premiumCategory != null)
                preferenceScreen.removePreference(premiumCategory);
        } else {
            if (premiumPreference != null)
                premiumPreference.setOnPreferenceClickListener(preference -> {
                    String packageID = mContext.getPackageName() + ".premium";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageID)));
                    } catch (ActivityNotFoundException ignored) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageID)));
                    }
                    return true;
                });
        }

        if (TermsPreferences != null)
            TermsPreferences.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://hnogames.nl/domoticz_terms.html")));
                return true;
            });

        if (PrivacyPreferences != null)
            PrivacyPreferences.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://hnogames.nl/domoticz_policy.html")));
                return true;
            });

        if (ReportErrorSettings != null)
            ReportErrorSettings.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.PAID_OOTT)
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.oott.hu/ereport")));
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.domoticz.com/forum/")));
                return true;
            });

        if (taskerPreference != null)
            taskerPreference.setOnPreferenceClickListener(preference -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=nl.hnogames.domoticz.tasker")));
                } catch (ActivityNotFoundException ignored) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=nl.hnogames.domoticz.tasker")));
                }
                return true;
            });

        if (translateApplication != null)
            translateApplication.setOnPreferenceClickListener(preference -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://crowdin.com/project/domoticz-for-android"));
                startActivity(i);
                return true;
            });

        if (resetApplication != null)
            resetApplication.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    new MaterialDialog.Builder(mContext)
                            .title(R.string.category_Reset)
                            .content(R.string.are_you_sure_clear_settings)
                            .positiveText(R.string.ok)
                            .negativeText(R.string.cancel)
                            .onPositive((dialog, which) -> ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE))
                                    .clearApplicationUserData())
                            .show();
                } else {
                    startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);
                }
                return true;
            });

        if (FingerPrintPreference != null)
            FingerPrintPreference.setOnPreferenceChangeListener((preference, newValue) -> {
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
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
                                        .onPositive((dialog, which) -> {
                                            mSharedPrefs.setStartupSecurityEnabled(true);
                                            if (getActivity() != null)
                                                ((SettingsActivity) getActivity()).reloadSettings();
                                        })
                                        .show();
                                return false;
                            } else {

                                UsefulBits.showSimpleToast(mContext, getString(R.string.fingerprint_not_setup_in_android), Toast.LENGTH_LONG);
                            }
                        }
                    }
                }
                return false;
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
                .onPositive((dialog, which) -> UsefulBits.restartApplication(getActivity()))
                .show();
    }

    private void handleInfoAndAbout() {
        androidx.preference.Preference about = findPreference("info_about");
        if (about != null)
            about.setOnPreferenceClickListener(preference -> {
                SimpleTextDialog td = new SimpleTextDialog(mContext);
                td.setTitle(R.string.info_about);
                td.setText(R.string.welcome_info_domoticz);
                td.show();
                return true;
            });
    }

    private void handleImportExportButtons() {
        androidx.preference.Preference exportButton = findPreference("export_settings");
        if (exportButton != null)
            exportButton.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessStorage(mContext)) {
                        permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                    } else
                        ((SettingsActivity) getActivity()).exportSettings();
                } else
                    ((SettingsActivity) getActivity()).exportSettings();
                return false;
            });
        androidx.preference.Preference importButton = findPreference("import_settings");
        if (importButton != null)
            importButton.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessStorage(mContext)) {
                        permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                    } else
                        ((SettingsActivity) getActivity()).importSettings();
                } else
                    ((SettingsActivity) getActivity()).importSettings();
                return false;
            });
    }

    private void setStartUpScreenDefaultValue() {
        int defaultValue = mSharedPrefs.getActualStartupScreenIndex();
        ListPreference startup_screen = findPreference("startup_nav");
        startup_screen.setValueIndex(defaultValue);
    }

    private void showPremiumSnackbar(final String category) {
        try {
            new Handler().postDelayed(() -> {
                if (getView() != null) {
                    Snackbar.make(getView(), category + " " + getString(R.string.premium_feature), Snackbar.LENGTH_LONG)
                            .setAction(R.string.upgrade, view -> UsefulBits.openPremiumAppStore(mContext))
                            .setActionTextColor(ContextCompat.getColor(mContext, R.color.material_blue_600))
                            .show();
                }
            }, (300));
        } catch (Exception ex) {
            Log.e(TAG, "No Snackbar shown: " + ex.getMessage());
        }
    }

    public void showSnackbar(final String text) {
        try {
            new Handler().postDelayed(() -> {
                if (getView() != null) {
                    Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
                }
            }, (300));
        } catch (Exception ex) {
            Log.e(TAG, "No Snackbar shown: " + ex.getMessage());
        }
    }
}