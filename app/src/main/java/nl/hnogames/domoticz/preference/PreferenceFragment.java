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
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.legacy.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.HashSet;

import hugo.weaving.DebugLog;
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
import nl.hnogames.domoticz.ui.SimpleTextDialog;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

import static android.content.Context.KEYGUARD_SERVICE;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = PreferenceFragment.class.getSimpleName();

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG_IMPORT = "Import Settings";
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG_EXPORT = "Export Settings";
    private SharedPrefUtil mSharedPrefs;
    private File SettingsFile;
    private Context mContext;
    private Domoticz mDomoticz;
    private ConfigInfo mConfigInfo;
    private ServerUtil mServerUtil;
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

        mServerUtil = new ServerUtil(mContext);
        mSharedPrefs = new SharedPrefUtil(mContext);
        mDomoticz = new Domoticz(mContext, AppController.getInstance().getRequestQueue());
        mConfigInfo = mServerUtil.getActiveServer().getConfigInfo(mContext);

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
        androidx.preference.Preference GeoSettings = findPreference("geo_settings");
        androidx.preference.SwitchPreference WearPreference = findPreference("enableWearItems");
        androidx.preference.SwitchPreference WidgetsEnablePreference = findPreference("enableWidgets");
        androidx.preference.Preference NFCPreference = findPreference("nfc_settings");
        androidx.preference.Preference QRCodePreference = findPreference("qrcode_settings");
        androidx.preference.Preference BluetoothPreference = findPreference("bluetooth_settings");
        androidx.preference.Preference SpeechPreference = findPreference("speech_settings");
        androidx.preference.SwitchPreference EnableNFCPreference = findPreference("enableNFC");
        androidx.preference.SwitchPreference EnableQRCodePreference = findPreference("enableQRCode");
        androidx.preference.SwitchPreference EnableBluetoothPreference = findPreference("enableBluetooth");
        androidx.preference.SwitchPreference EnableSpeechPreference = findPreference("enableSpeech");
        androidx.preference.SwitchPreference EnableTalkBackPreference = findPreference("talkBack");
        MultiSelectListPreference drawerItems = findPreference("show_nav_items");
        @SuppressWarnings("SpellCheckingInspection") androidx.preference.SwitchPreference AlwaysOnPreference = findPreference("alwayson");
        @SuppressWarnings("SpellCheckingInspection") androidx.preference.SwitchPreference RefreshScreenPreference = findPreference("autorefresh");
        @SuppressWarnings("SpellCheckingInspection") androidx.preference.PreferenceScreen preferenceScreen = findPreference("settingsscreen");
        androidx.preference.PreferenceCategory premiumCategory = findPreference("premium_category");
        androidx.preference.Preference premiumPreference = findPreference("premium_settings");
        androidx.preference.Preference ThemePreference = findPreference("darkTheme");
        androidx.preference.SwitchPreference ClockPreference = findPreference("dashboardShowClock");
        androidx.preference.Preference FingerPrintSettingsPreference = findPreference("SecuritySettings");
        androidx.preference.SwitchPreference FingerPrintPreference = findPreference("enableSecurity");
        androidx.preference.SwitchPreference customSortProperty = findPreference("sortCustom");
        androidx.preference.Preference openNotificationSettings = findPreference("openNotificationSettings");

        if (mConfigInfo == null) {
            UsefulBits.getServerConfigForActiveServer(mContext, new ConfigReceiver() {
                @Override
                @DebugLog
                public void onReceiveConfig(ConfigInfo settings) {
                    mConfigInfo = settings;
                    setupDefaultValues();
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                }
            }, mServerUtil.getActiveServer().getConfigInfo(mContext));
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
                    UsefulBits.getServerConfigForActiveServer(mContext, new ConfigReceiver() {
                        @Override
                        public void onReceiveConfig(ConfigInfo settings) {
                            showSnackbar(mContext.getString(R.string.fetched_server_config_success));
                        }

                        @Override
                        public void onError(Exception error) {
                            showSnackbar(mContext.getString(R.string.fetched_server_config_failed));
                        }
                    }, null);
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
                            FingerprintManager fingerprintManager = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                                return false;
                            }
                            if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                                return false;
                            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                                UsefulBits.showSimpleToast(mContext, getString(R.string.fingerprint_not_setup_in_android), Toast.LENGTH_LONG);
                                return false;
                            } else {
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
        SettingsFile = new File(Environment.getExternalStorageDirectory(),
                "/Domoticz/DomoticzSettings.txt");

        final String sPath = SettingsFile.getPath().
                substring(0, SettingsFile.getPath().lastIndexOf("/"));
        //noinspection unused
        boolean mkdirsResultIsOk = new File(sPath + "/").mkdirs();

        androidx.preference.Preference exportButton = findPreference("export_settings");
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
        Log.v(TAG_IMPORT, "Importing settings from: " + SettingsFile.getPath());
        if (mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile))
            showSnackbar(mContext.getString(R.string.settings_imported));
        else
            showSnackbar(mContext.getString(R.string.settings_import_failed));
    }

    private void exportSettings() {
        Log.v(TAG_EXPORT, "Exporting settings to: " + SettingsFile.getPath());
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
            showSnackbar(mContext.getString(R.string.settings_exported));
        else
            showSnackbar(mContext.getString(R.string.settings_export_failed));
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