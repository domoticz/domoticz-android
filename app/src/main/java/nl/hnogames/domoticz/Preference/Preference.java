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

package nl.hnogames.domoticz.Preference;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.GeoSettingsActivity;
import nl.hnogames.domoticz.NFCSettingsActivity;
import nl.hnogames.domoticz.QRCodeSettingsActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ServerListSettingsActivity;
import nl.hnogames.domoticz.ServerSettingsActivity;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.SpeechSettingsActivity;
import nl.hnogames.domoticz.UI.SimpleTextDialog;
import nl.hnogames.domoticz.UpdateActivity;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

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
    private Domoticz mDomoticz;
    private ConfigInfo mConfigInfo;
    private ServerUtil mServerUtil;
    private PermissionHelper permissionHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        permissionHelper = PermissionHelper.getInstance(getActivity());

        mContext = getActivity();

        mServerUtil = new ServerUtil(mContext);
        mSharedPrefs = new SharedPrefUtil(mContext);
        mDomoticz = new Domoticz(mContext, AppController.getInstance().getRequestQueue());
        mConfigInfo = mServerUtil.getActiveServer().getConfigInfo(mContext);

        UsefulBits.checkAPK(mContext, mSharedPrefs);

        setPreferences();
        setStartUpScreenDefaultValue();
        setVersionInfo();
        handleImportExportButtons();
        handleInfoAndAbout();
    }

    private void setupDefaultValues() {
        nl.hnogames.domoticz.Preference.EditTextIntegerPreference oTemperatureMin = (nl.hnogames.domoticz.Preference.EditTextIntegerPreference) findPreference("tempMinValue");
        nl.hnogames.domoticz.Preference.EditTextIntegerPreference oTemperatureMax = (nl.hnogames.domoticz.Preference.EditTextIntegerPreference) findPreference("tempMaxValue");
        oTemperatureMin.setText(mSharedPrefs.getTemperatureSetMin(mConfigInfo.getTempSign()) + "");
        oTemperatureMax.setText(mSharedPrefs.getTemperatureSetMax(mConfigInfo.getTempSign()) + "");
        oTemperatureMax.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object o) {
                int newMaxValue = Integer.valueOf(o + "");
                int existingMinValue = mSharedPrefs.getTemperatureSetMin(mConfigInfo.getTempSign());

                if (newMaxValue > existingMinValue)
                    return true;
                else
                    Toast.makeText(mContext, mContext.getString(R.string.default_values_max_error), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        oTemperatureMin.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object o) {
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
        final android.preference.SwitchPreference MultiServerPreference = (android.preference.SwitchPreference) findPreference("enableMultiServers");
        android.preference.Preference ServerSettings = findPreference("server_settings");
        android.preference.Preference ClearNotifications = findPreference("clear_notifications");
        android.preference.Preference PermissionsSettings = findPreference("permissionssettings");
        android.preference.Preference NotificationLogged = findPreference("notification_show_logs");
        android.preference.Preference fetchServerConfig = findPreference("server_force_fetch_config");
        android.preference.Preference resetApplication = findPreference("reset_settings");
        android.preference.ListPreference displayLanguage = (ListPreference) findPreference("displayLanguage");
        final android.preference.Preference registrationId = findPreference("notification_registration_id");
        android.preference.Preference GeoSettings = findPreference("geo_settings");
        android.preference.SwitchPreference WearPreference = (android.preference.SwitchPreference) findPreference("enableWearItems");
        android.preference.SwitchPreference WidgetsEnablePreference = (android.preference.SwitchPreference) findPreference("enableWidgets");
        android.preference.Preference NFCPreference = findPreference("nfc_settings");
        android.preference.Preference QRCodePreference = findPreference("qrcode_settings");
        android.preference.Preference SpeechPreference = findPreference("speech_settings");
        android.preference.SwitchPreference EnableNFCPreference = (android.preference.SwitchPreference) findPreference("enableNFC");
        android.preference.SwitchPreference EnableQRCodePreference = (android.preference.SwitchPreference) findPreference("enableQRCode");
        android.preference.SwitchPreference EnableSpeechPreference = (android.preference.SwitchPreference) findPreference("enableSpeech");
        android.preference.SwitchPreference EnableTalkBackPreference = (android.preference.SwitchPreference) findPreference("talkBack");
        MultiSelectListPreference drawerItems = (MultiSelectListPreference) findPreference("enable_menu_items");
        @SuppressWarnings("SpellCheckingInspection") android.preference.SwitchPreference AlwaysOnPreference = (android.preference.SwitchPreference) findPreference("alwayson");
        @SuppressWarnings("SpellCheckingInspection") android.preference.PreferenceScreen preferenceScreen = (android.preference.PreferenceScreen) findPreference("settingsscreen");
        android.preference.PreferenceCategory premiumCategory = (android.preference.PreferenceCategory) findPreference("premium_category");
        android.preference.Preference premiumPreference = findPreference("premium_settings");
        NotificationsMultiSelectListPreference notificationsMultiSelectListPreference = (NotificationsMultiSelectListPreference) findPreference("suppressNotifications");
        NotificationsMultiSelectListPreference alarmMultiSelectListPreference = (NotificationsMultiSelectListPreference) findPreference("alarmNotifications");
        android.preference.SwitchPreference ThemePreference = (android.preference.SwitchPreference) findPreference("darkTheme");
        android.preference.Preference FingerPrintSettingsPreference = findPreference("SecuritySettings");
        android.preference.SwitchPreference FingerPrintPreference = (android.preference.SwitchPreference) findPreference("enableSecurity");
        android.preference.PreferenceScreen notificationScreen = (android.preference.PreferenceScreen) findPreference("notificationscreen");
        android.preference.Preference noticiationSettings = findPreference("noticiationSettings");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.preference.PreferenceCategory notificationSound = (android.preference.PreferenceCategory) findPreference("notificationSound");
            notificationScreen.removePreference(notificationSound);
        }
        else{
            notificationScreen.removePreference(noticiationSettings);
        }

        noticiationSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationUtil.CHANNEL_ID);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName());
                startActivity(intent);
                return true;
            }
        });

        if (mConfigInfo == null) {
            UsefulBits.getServerConfigForActiveServer(mContext, false, new ConfigReceiver() {
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

        List<String> notifications = mSharedPrefs.getReceivedNotifications();
        if (notifications == null || notifications.size() <= 0) {
            notificationsMultiSelectListPreference.setEnabled(false);
            alarmMultiSelectListPreference.setEnabled(false);
        } else {
            notificationsMultiSelectListPreference.setEnabled(true);
            alarmMultiSelectListPreference.setEnabled(true);
        }

        PermissionsSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                permissionHelper.openSettingsScreen();
                return true;
            }
        });

        drawerItems.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
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

        ThemePreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_theme));
                    return false;
                } else {
                    ((SettingsActivity) getActivity()).reloadSettings();
                    return true;
                }
            }
        });

        MultiServerPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.multi_server));
                    return false;
                }
                return true;
            }
        });

        ServerSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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

        FingerPrintSettingsPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                mContext.startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
                return true;
            }
        });

        fetchServerConfig.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                UsefulBits.getServerConfigForActiveServer(mContext, true, new ConfigReceiver() {
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

        displayLanguage.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                showRestartMessage();
                return true;
            }
        });

        registrationId.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!PermissionsUtil.canAccessDeviceState(mContext)) {
                        permissionHelper.request(PermissionsUtil.INITIAL_DEVICE_PERMS);
                    } else {
                        pushGCMRegistrationIds();
                    }
                } else {
                    pushGCMRegistrationIds();
                }
                return true;
            }
        });

        ClearNotifications.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                mSharedPrefs.clearPreviousNotification();
                return true;
            }
        });

        GeoSettings.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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

        EnableNFCPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
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

        EnableQRCodePreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_QRCode));
                    return false;
                }

                return true;
            }
        });

        EnableSpeechPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_Speech));
                    return false;
                }
                return true;
            }
        });

        EnableTalkBackPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_talk_back));
                    return false;
                }
                return true;
            }
        });

        NFCPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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

        QRCodePreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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

        SpeechPreference.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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

        WidgetsEnablePreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
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

        WearPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_wear));
                    return false;
                }
                return true;
            }
        });

        AlwaysOnPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object newValue) {
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.always_on_title));
                    return false;
                }
                return true;
            }
        });

        NotificationLogged.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                //show dialog
                List<String> logs = mSharedPrefs.getLoggedNotifications();
                if (logs != null && logs.size() > 0) {
                    Collections.reverse(logs);
                    new MaterialDialog.Builder(mContext)
                            .title(mContext.getString(R.string.notification_show_title))
                            .items((CharSequence[]) logs.toArray(new String[0]))
                            .show();
                } else
                    UsefulBits.showSimpleToast(mContext, getString(R.string.notification_show_nothing), Toast.LENGTH_LONG);
                return true;
            }
        });

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
                    } catch (android.content.ActivityNotFoundException ignored) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageID)));
                    }

                    return true;
                }
            });
        }

        resetApplication.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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

        FingerPrintPreference.setOnPreferenceChangeListener(new android.preference.Preference.OnPreferenceChangeListener() {
            @SuppressLint("NewApi")
            @Override
            public boolean onPreferenceChange(android.preference.Preference preference, Object o) {
                if (mSharedPrefs.isStartupSecurityEnabled())
                    return true;
                if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_startup_security));
                    return false;
                } else {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!fingerprintManager.isHardwareDetected()) {
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
                                                ((SettingsActivity) getActivity()).reloadSettings();
                                            }
                                        })
                                        .show();

                                return false;
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    private void pushGCMRegistrationIds() {
        final String UUID = DeviceUtils.getUniqueID(mContext);
        final String senderId = FirebaseInstanceId.getInstance().getToken();
        mDomoticz.CleanMobileDevice(UUID, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                //previous id cleaned
                mDomoticz.AddMobileDevice(UUID, senderId, new MobileDeviceReceiver() {
                    @Override
                    public void onSuccess() {
                        if (isAdded())
                            showSnackbar(mContext.getString(R.string.notification_settings_pushed));
                    }

                    @Override
                    public void onError(Exception error) {
                        if (isAdded())
                            showSnackbar(mContext.getString(R.string.notification_settings_push_failed));
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                //nothing to clean..
                mDomoticz.AddMobileDevice(UUID, senderId, new MobileDeviceReceiver() {
                    @Override
                    public void onSuccess() {
                        if (isAdded())
                            showSnackbar(mContext.getString(R.string.notification_settings_pushed));
                    }

                    @Override
                    public void onError(Exception error) {
                        if (isAdded())
                            showSnackbar(mContext.getString(R.string.notification_settings_push_failed));
                    }
                });
            }
        });
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
        boolean mkdirsResultIsOk = new File(sPath + "/").mkdirs();

        android.preference.Preference exportButton = findPreference("export_settings");
        exportButton.setOnPreferenceClickListener(
                new android.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(android.preference.Preference preference) {
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
        android.preference.Preference importButton = findPreference("import_settings");
        importButton.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
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
        mSharedPrefs.loadSharedPreferencesFromFile(SettingsFile);
        if (mSharedPrefs.saveSharedPreferencesToFile(SettingsFile))
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

    private void setVersionInfo() {
        ServerUtil serverUtil = new ServerUtil(mContext);
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
        appVersion.setSummary(appVersionStr);

        final android.preference.Preference domoticzVersion = findPreference("version_domoticz");
        String message;

        try {

            if (serverUtil.getActiveServer() != null) {
                if ((serverUtil.getActiveServer().getServerUpdateInfo(mContext) != null
                        && serverUtil.getActiveServer().getServerUpdateInfo(mContext).isUpdateAvailable()
                        && !UsefulBits.isEmpty(serverUtil.getActiveServer().getServerUpdateInfo(mContext).getCurrentServerVersion())) ||
                        mSharedPrefs.isDebugEnabled()) {

                    // Update is available or debugging is enabled
                    String version;
                    if (mSharedPrefs.isDebugEnabled())
                        version = mContext.getString(R.string.debug_test_text);
                    else
                        version = (serverUtil.getActiveServer().getServerUpdateInfo(mContext) != null)
                                ? serverUtil.getActiveServer().getServerUpdateInfo(mContext).getUpdateRevisionNumber() : "";

                    message = String.format(getString(R.string.update_available_enhanced),
                            serverUtil.getActiveServer().getServerUpdateInfo(mContext).getCurrentServerVersion(),
                            version);
                    if (serverUtil.getActiveServer().getServerUpdateInfo(mContext) != null &&
                            serverUtil.getActiveServer().getServerUpdateInfo(mContext).getSystemName() != null &&
                            serverUtil.getActiveServer().getServerUpdateInfo(mContext).getSystemName().equalsIgnoreCase("linux")) {
                        // Only offer remote/auto update on Linux systems
                        message += UsefulBits.newLine() + mContext.getString(R.string.click_to_update_server);
                        domoticzVersion.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
                            @Override
                            public boolean onPreferenceClick(android.preference.Preference preference) {
                                Intent intent = new Intent(mContext, UpdateActivity.class);
                                startActivity(intent);
                                return false;
                            }
                        });
                    }
                } else {
                    message = (serverUtil.getActiveServer().getServerUpdateInfo(mContext) != null &&
                            !UsefulBits.isEmpty(serverUtil.getActiveServer().getServerUpdateInfo(mContext).getUpdateRevisionNumber()))
                            ? serverUtil.getActiveServer().getServerUpdateInfo(mContext).getUpdateRevisionNumber() : "";
                }
                domoticzVersion.setSummary(message);
            }
        } catch (Exception ex) {
            String ex_message = mDomoticz.getErrorMessage(ex);
            if (!UsefulBits.isEmpty(ex_message))
                Log.e(TAG, mDomoticz.getErrorMessage(ex));
        }
    }

    private void setStartUpScreenDefaultValue() {
        int defaultValue = mSharedPrefs.getStartupScreenIndex();
        ListPreference startup_screen = (ListPreference) findPreference("startup_screen");
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