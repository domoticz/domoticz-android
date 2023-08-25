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

import static android.content.Context.KEYGUARD_SERVICE;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.biometric.BiometricManager;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
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
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashSet;

import nl.hnogames.domoticz.BeaconSettingsActivity;
import nl.hnogames.domoticz.BluetoothSettingsActivity;
import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.EventsActivity;
import nl.hnogames.domoticz.GeoSettingsActivity;
import nl.hnogames.domoticz.LogsActivity;
import nl.hnogames.domoticz.NFCSettingsActivity;
import nl.hnogames.domoticz.NotificationSettingsActivity;
import nl.hnogames.domoticz.QRCodeSettingsActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.ServerListSettingsActivity;
import nl.hnogames.domoticz.ServerSettingsActivity;
import nl.hnogames.domoticz.SettingsActivity;
import nl.hnogames.domoticz.SpeechSettingsActivity;
import nl.hnogames.domoticz.UserVariablesActivity;
import nl.hnogames.domoticz.WifiSettingsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.SubscriptionsListener;
import nl.hnogames.domoticz.service.WifiReceiver;
import nl.hnogames.domoticz.service.WifiReceiverManager;
import nl.hnogames.domoticz.ui.SimpleTextDialog;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.LoginInfo;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.LoginReceiver;

public class PreferenceFragment extends PreferenceFragmentCompat implements SubscriptionsListener {
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
    private String filter;
    private SearchView searchViewAction;

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
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.preferences);
        permissionHelper = PermissionHelper.getInstance(getActivity());

        mContext = getActivity();
        mSharedPrefs = new SharedPrefUtil(mContext);
        mConfigInfo = StaticHelper.getServerUtil(getActivity()).getActiveServer().getConfigInfo(mContext);

        setIconColor();
        setPreferences();
        setStartUpScreenDefaultValue();
        handleImportExportButtons();
        handleInfoAndAbout();
        handleAdvanceButtons();
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
        // Screen
        PreferenceScreen preferenceScreen = findPreference("settingsscreen");

        // Categories
        PreferenceCategory old_version_category = findPreference("old_version_category");
        PreferenceCategory premiumCategory = findPreference("premium_category");
        PreferenceCategory dashboard_category = findPreference("dashboard_category");
        PreferenceCategory generic_category = findPreference("generic_category");
        PreferenceCategory defaultValues = findPreference("defaultValues");
        PreferenceCategory sorting_category = findPreference("sorting_category");
        PreferenceCategory notifications_category = findPreference("notifications_category");
        PreferenceCategory server_category = findPreference("server_category");
        PreferenceCategory geofencing_category = findPreference("geofencing_category");
        PreferenceCategory nfc_category = findPreference("nfc_category");
        PreferenceCategory qrcode_category = findPreference("qrcode_category");
        PreferenceCategory wifi_category = findPreference("wifi_category");
        PreferenceCategory bluetooth_category = findPreference("bluetooth_category");
        PreferenceCategory beacon_category = findPreference("beacon_category");
        PreferenceCategory security_category = findPreference("security_category");
        PreferenceCategory widgets_category = findPreference("widgets_category");
        PreferenceCategory language_category = findPreference("language_category");
        PreferenceCategory theme_category = findPreference("theme_category");
        PreferenceCategory wear_category = findPreference("wear_category");
        PreferenceCategory Auto_category = findPreference("auto_category");
        PreferenceCategory advanced_category = findPreference("advanced_category");
        PreferenceCategory other_category = findPreference("other_category");
        PreferenceCategory about_category = findPreference("about_category");
        PreferenceCategory speech_category = findPreference("speech_category");

        // Generic settings
        ListPreference startup_screen = findPreference("startup_nav");
        MultiSelectListPreference drawerItems = findPreference("show_nav_items");
        SwitchPreference CameraPreference = findPreference("dashboardShowCamera");
        SwitchPreference AlwaysOnPreference = findPreference("alwayson");
        SwitchPreference RefreshScreenPreference = findPreference("autorefresh");
        EditTextIntegerPreference RefreshScreenTimerPreference = findPreference("autorefreshTimer");

        // Default values settings
        EditTextIntegerPreference oTemperatureMin = findPreference("tempMinValue");
        EditTextIntegerPreference oTemperatureMax = findPreference("tempMaxValue");

        // Sorting settings
        SwitchPreference customSortProperty = findPreference("sortCustom");
        SwitchPreference customSortLockProperty = findPreference("lockSortCustom");

        // Dashboard settings
        SwitchPreference ClockPreference = findPreference("dashboardShowClock");
        SwitchPreference PlansPreference = findPreference("dashboardShowPlans");
        SwitchPreference dashboardAsListProperty = findPreference("dashboardAsList2");

        // Notification settings
        Preference openNotificationSettings = findPreference("openNotificationSettings");

        // Server settings
        final SwitchPreference MultiServerPreference = findPreference("enableMultiServers");
        Preference ServerSettings = findPreference("server_settings");
        Preference fetchServerConfig = findPreference("server_force_fetch_config");

        // Geofence settings
        Preference GeoSettings = findPreference("geo_settings");

        // NFC settings
        SwitchPreference EnableNFCPreference = findPreference("enableNFC");
        SwitchPreference AutoPreference = findPreference("enableAutoItems");
        AutoMultiSelectListPreference AutoItems = findPreference("autolistItems");
        Preference NFCPreference = findPreference("nfc_settings");

        // Speech settings
        SwitchPreference EnableSpeechPreference = findPreference("enableSpeech");
        Preference SpeechPreference = findPreference("speech_settings");

        // QR Code settings
        SwitchPreference EnableQRCodePreference = findPreference("enableQRCode");
        Preference QRCodePreference = findPreference("qrcode_settings");

        // Wifi settings
        SwitchPreference EnableWifiPreference = findPreference("enableWifi");
        Preference WifiPreference = findPreference("wifi_settings");

        // Bluetooth settings
        Preference BluetoothPreference = findPreference("bluetooth_settings");
        SwitchPreference EnableBluetoothPreference = findPreference("enableBluetooth");

        // Beacon settings
        SwitchPreference EnableBeaconPreference = findPreference("enableBeacon");
        SwitchPreference EnableBeaconNotificationsPreference = findPreference("beacon_notifications_enabled");
        Preference BeaconPreference = findPreference("beacon_settings");

        // Security settings
        Preference FingerPrintSettingsPreference = findPreference("SecuritySettings");
        SwitchPreference FingerPrintPreference = findPreference("enableSecurity");

        // Widgets settings
        SwitchPreference WidgetsEnablePreference = findPreference("enableWidgets");

        // Language settings
        ListPreference displayLanguage = findPreference("displayLanguage");
        SwitchPreference EnableTalkBackPreference = findPreference("talkBack");
        Preference translateApplication = findPreference("translate");

        // Theme settings
        Preference ThemePreference = findPreference("darkTheme");

        // Android Wear settings
        SwitchPreference WearPreference = findPreference("enableWearItems");
        WearMultiSelectListPreference WearItems = findPreference("wearItems");

        // Advanced settings
        androidx.preference.Preference exportButton = findPreference("export_settings");
        androidx.preference.Preference importButton = findPreference("import_settings");
        androidx.preference.Preference logsButton = findPreference("logs_settings");
        androidx.preference.Preference eventsButton = findPreference("events_settings");
        androidx.preference.Preference varsButton = findPreference("vars_settings");
        Preference PermissionsSettings = findPreference("permissionssettings");
        Preference resetApplication = findPreference("reset_settings");

        // Other settings
        Preference taskerPreference = findPreference("tasker_settings");

        // Premium settings
        Preference premiumPreference = findPreference("premium_settings");
        Preference restorePreference = findPreference("premium_restore");
        Preference oldVersionPreference = findPreference("old_version");

        // About settings
        Preference ReportErrorSettings = findPreference("report");
        Preference TermsPreferences = findPreference("info_terms");
        Preference PrivacyPreferences = findPreference("info_privacy");
        Preference versionPreferences = findPreference("version");
        Preference about = findPreference("info_about");

        if (premiumPreference != null) {
            premiumCategory.setVisible(UsefulBits.isEmpty(filter));
            premiumPreference.setVisible(UsefulBits.isEmpty(filter));
            restorePreference.setVisible(UsefulBits.isEmpty(filter));
        }

        if (oldVersionPreference != null) {
            old_version_category.setVisible(UsefulBits.isEmpty(filter));
            oldVersionPreference.setVisible(UsefulBits.isEmpty(filter));
        }

        startup_screen.setVisible(UsefulBits.isEmpty(filter) || startup_screen.getTitle().toString().toLowerCase().contains(filter) || (CameraPreference.getSummary() != null && startup_screen.getSummary().toString().toLowerCase().contains(filter)));
        drawerItems.setVisible(UsefulBits.isEmpty(filter) || drawerItems.getTitle().toString().toLowerCase().contains(filter) || (drawerItems.getSummary() != null && drawerItems.getSummary().toString().toLowerCase().contains(filter)));
        AlwaysOnPreference.setVisible(UsefulBits.isEmpty(filter) || AlwaysOnPreference.getTitle().toString().toLowerCase().contains(filter) || (AlwaysOnPreference.getSummary() != null && AlwaysOnPreference.getSummary().toString().toLowerCase().contains(filter)));
        RefreshScreenPreference.setVisible(UsefulBits.isEmpty(filter) || RefreshScreenPreference.getTitle().toString().toLowerCase().contains(filter) || (RefreshScreenPreference.getSummary() != null && RefreshScreenPreference.getSummary().toString().toLowerCase().contains(filter)));
        RefreshScreenTimerPreference.setVisible(UsefulBits.isEmpty(filter) || RefreshScreenTimerPreference.getTitle().toString().toLowerCase().contains(filter) || (RefreshScreenTimerPreference.getSummary() != null && RefreshScreenTimerPreference.getSummary().toString().toLowerCase().contains(filter)));
        generic_category.setVisible(startup_screen.isVisible() || drawerItems.isVisible() || CameraPreference.isVisible() || AlwaysOnPreference.isVisible() || RefreshScreenPreference.isVisible() || RefreshScreenTimerPreference.isVisible());

        // Default values settings
        oTemperatureMin.setVisible(UsefulBits.isEmpty(filter) || oTemperatureMin.getTitle().toString().toLowerCase().contains(filter) || (oTemperatureMin.getSummary() != null && oTemperatureMin.getSummary().toString().toLowerCase().contains(filter)));
        oTemperatureMax.setVisible(UsefulBits.isEmpty(filter) || oTemperatureMax.getTitle().toString().toLowerCase().contains(filter) || (oTemperatureMax.getSummary() != null && oTemperatureMax.getSummary().toString().toLowerCase().contains(filter)));
        defaultValues.setVisible(oTemperatureMax.isVisible() || oTemperatureMin.isVisible());

        // Sorting settings
        customSortProperty.setVisible(UsefulBits.isEmpty(filter) || customSortProperty.getTitle().toString().toLowerCase().contains(filter) || (customSortProperty.getSummary() != null && customSortProperty.getSummary().toString().toLowerCase().contains(filter)));
        customSortLockProperty.setVisible(UsefulBits.isEmpty(filter) || customSortLockProperty.getTitle().toString().toLowerCase().contains(filter) || (customSortLockProperty.getSummary() != null && customSortLockProperty.getSummary().toString().toLowerCase().contains(filter)));
        sorting_category.setVisible(customSortProperty.isVisible() || customSortLockProperty.isVisible());

        // Dashboard settings
        ClockPreference.setVisible(UsefulBits.isEmpty(filter) || ClockPreference.getTitle().toString().toLowerCase().contains(filter) || (ClockPreference.getSummary() != null && ClockPreference.getSummary().toString().toLowerCase().contains(filter)));
        dashboardAsListProperty.setVisible(UsefulBits.isEmpty(filter) || dashboardAsListProperty.getTitle().toString().toLowerCase().contains(filter) || (dashboardAsListProperty.getSummary() != null && dashboardAsListProperty.getSummary().toString().toLowerCase().contains(filter)));
        PlansPreference.setVisible(UsefulBits.isEmpty(filter) || PlansPreference.getTitle().toString().toLowerCase().contains(filter) || (PlansPreference.getSummary() != null && PlansPreference.getSummary().toString().toLowerCase().contains(filter)));
        CameraPreference.setVisible(UsefulBits.isEmpty(filter) || CameraPreference.getTitle().toString().toLowerCase().contains(filter) || (CameraPreference.getSummary() != null && CameraPreference.getSummary().toString().toLowerCase().contains(filter)));
        dashboard_category.setVisible(ClockPreference.isVisible() || dashboardAsListProperty.isVisible() || CameraPreference.isVisible() || PlansPreference.isVisible());

        // Notification settings
        openNotificationSettings.setVisible(UsefulBits.isEmpty(filter) || openNotificationSettings.getTitle().toString().toLowerCase().contains(filter) || (openNotificationSettings.getSummary() != null && openNotificationSettings.getSummary().toString().toLowerCase().contains(filter)));
        notifications_category.setVisible(openNotificationSettings.isVisible());

        // Server settings
        MultiServerPreference.setVisible(UsefulBits.isEmpty(filter) || MultiServerPreference.getTitle().toString().toLowerCase().contains(filter) || (MultiServerPreference.getSummary() != null && MultiServerPreference.getSummary().toString().toLowerCase().contains(filter)));
        ServerSettings.setVisible(UsefulBits.isEmpty(filter) || ServerSettings.getTitle().toString().toLowerCase().contains(filter) || (ServerSettings.getSummary() != null && ServerSettings.getSummary().toString().toLowerCase().contains(filter)));
        fetchServerConfig.setVisible(UsefulBits.isEmpty(filter) || fetchServerConfig.getTitle().toString().toLowerCase().contains(filter) || (fetchServerConfig.getSummary() != null && fetchServerConfig.getSummary().toString().toLowerCase().contains(filter)));
        server_category.setVisible(MultiServerPreference.isVisible() || ServerSettings.isVisible() || fetchServerConfig.isVisible());

        // Geofence settings
        GeoSettings.setVisible(UsefulBits.isEmpty(filter) || GeoSettings.getTitle().toString().toLowerCase().contains(filter) || (GeoSettings.getSummary() != null && GeoSettings.getSummary().toString().toLowerCase().contains(filter)));
        geofencing_category.setVisible(GeoSettings.isVisible());

        // NFC settings
        EnableNFCPreference.setVisible(UsefulBits.isEmpty(filter) || EnableNFCPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableNFCPreference.getSummary() != null && EnableNFCPreference.getSummary().toString().toLowerCase().contains(filter)));
        NFCPreference.setVisible(UsefulBits.isEmpty(filter) || NFCPreference.getTitle().toString().toLowerCase().contains(filter) || (NFCPreference.getSummary() != null && NFCPreference.getSummary().toString().toLowerCase().contains(filter)));
        nfc_category.setVisible(EnableNFCPreference.isVisible() || NFCPreference.isVisible());

        // Speech settings
        EnableSpeechPreference.setVisible(UsefulBits.isEmpty(filter) || EnableSpeechPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableSpeechPreference.getSummary() != null && EnableSpeechPreference.getSummary().toString().toLowerCase().contains(filter)));
        SpeechPreference.setVisible(UsefulBits.isEmpty(filter) || SpeechPreference.getTitle().toString().toLowerCase().contains(filter) || (SpeechPreference.getSummary() != null && SpeechPreference.getSummary().toString().toLowerCase().contains(filter)));
        speech_category.setVisible(EnableSpeechPreference.isVisible() || SpeechPreference.isVisible());

        // QR Code settings
        EnableQRCodePreference.setVisible(UsefulBits.isEmpty(filter) || EnableQRCodePreference.getTitle().toString().toLowerCase().contains(filter) || (EnableQRCodePreference.getSummary() != null && EnableQRCodePreference.getSummary().toString().toLowerCase().contains(filter)));
        QRCodePreference.setVisible(UsefulBits.isEmpty(filter) || QRCodePreference.getTitle().toString().toLowerCase().contains(filter) || (QRCodePreference.getSummary() != null && QRCodePreference.getSummary().toString().toLowerCase().contains(filter)));
        qrcode_category.setVisible(EnableQRCodePreference.isVisible() || QRCodePreference.isVisible());

        // Wifi settings
        EnableWifiPreference.setVisible(UsefulBits.isEmpty(filter) || EnableWifiPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableWifiPreference.getSummary() != null && EnableWifiPreference.getSummary().toString().toLowerCase().contains(filter)));
        WifiPreference.setVisible(UsefulBits.isEmpty(filter) || WifiPreference.getTitle().toString().toLowerCase().contains(filter) || (WifiPreference.getSummary() != null && WifiPreference.getSummary().toString().toLowerCase().contains(filter)));
        wifi_category.setVisible(EnableWifiPreference.isVisible() || WifiPreference.isVisible());

        // Bluetooth settings
        BluetoothPreference.setVisible(UsefulBits.isEmpty(filter) || BluetoothPreference.getTitle().toString().toLowerCase().contains(filter) || (BluetoothPreference.getSummary() != null && BluetoothPreference.getSummary().toString().toLowerCase().contains(filter)));
        EnableBluetoothPreference.setVisible(UsefulBits.isEmpty(filter) || EnableBluetoothPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableBluetoothPreference.getSummary() != null && EnableBluetoothPreference.getSummary().toString().toLowerCase().contains(filter)));
        bluetooth_category.setVisible(BluetoothPreference.isVisible() || EnableBluetoothPreference.isVisible());

        // Beacon settings
        EnableBeaconPreference.setVisible(UsefulBits.isEmpty(filter) || EnableBeaconPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableBeaconPreference.getSummary() != null && EnableBeaconPreference.getSummary().toString().toLowerCase().contains(filter)));
        EnableBeaconNotificationsPreference.setVisible(UsefulBits.isEmpty(filter) || EnableBeaconNotificationsPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableBeaconNotificationsPreference.getSummary() != null && EnableBeaconNotificationsPreference.getSummary().toString().toLowerCase().contains(filter)));
        BeaconPreference.setVisible(UsefulBits.isEmpty(filter) || BeaconPreference.getTitle().toString().toLowerCase().contains(filter) || (BeaconPreference.getSummary() != null && BeaconPreference.getSummary().toString().toLowerCase().contains(filter)));
        beacon_category.setVisible(EnableBeaconPreference.isVisible() || EnableBeaconNotificationsPreference.isVisible() || BeaconPreference.isVisible());

        // Security settings
        FingerPrintSettingsPreference.setVisible(UsefulBits.isEmpty(filter) || FingerPrintSettingsPreference.getTitle().toString().toLowerCase().contains(filter) || (FingerPrintSettingsPreference.getSummary() != null && FingerPrintSettingsPreference.getSummary().toString().toLowerCase().contains(filter)));
        FingerPrintPreference.setVisible(UsefulBits.isEmpty(filter) || FingerPrintPreference.getTitle().toString().toLowerCase().contains(filter) || (FingerPrintPreference.getSummary() != null && FingerPrintPreference.getSummary().toString().toLowerCase().contains(filter)));
        security_category.setVisible(FingerPrintSettingsPreference.isVisible() || FingerPrintPreference.isVisible());

        // Widgets settings
        WidgetsEnablePreference.setVisible(UsefulBits.isEmpty(filter) || WidgetsEnablePreference.getTitle().toString().toLowerCase().contains(filter) || (WidgetsEnablePreference.getSummary() != null && WidgetsEnablePreference.getSummary().toString().toLowerCase().contains(filter)));
        widgets_category.setVisible(WidgetsEnablePreference.isVisible());

        // Language settings
        displayLanguage.setVisible(UsefulBits.isEmpty(filter) || displayLanguage.getTitle().toString().toLowerCase().contains(filter) || (displayLanguage.getSummary() != null && displayLanguage.getSummary().toString().toLowerCase().contains(filter)));
        EnableTalkBackPreference.setVisible(UsefulBits.isEmpty(filter) || EnableTalkBackPreference.getTitle().toString().toLowerCase().contains(filter) || (EnableTalkBackPreference.getSummary() != null && EnableTalkBackPreference.getSummary().toString().toLowerCase().contains(filter)));
        translateApplication.setVisible(UsefulBits.isEmpty(filter) || translateApplication.getTitle().toString().toLowerCase().contains(filter) || (translateApplication.getSummary() != null && translateApplication.getSummary().toString().toLowerCase().contains(filter)));
        language_category.setVisible(displayLanguage.isVisible() || EnableTalkBackPreference.isVisible() || translateApplication.isVisible());

        // Theme settings
        ThemePreference.setVisible(UsefulBits.isEmpty(filter) || ThemePreference.getTitle().toString().toLowerCase().contains(filter) || (ThemePreference.getSummary() != null && ThemePreference.getSummary().toString().toLowerCase().contains(filter)));
        theme_category.setVisible(ThemePreference.isVisible());

        // Android Wear settings
        WearPreference.setVisible(UsefulBits.isEmpty(filter) || WearPreference.getTitle().toString().toLowerCase().contains(filter) || (WearPreference.getSummary() != null && WearPreference.getSummary().toString().toLowerCase().contains(filter)));
        WearItems.setVisible(UsefulBits.isEmpty(filter) || WearItems.getTitle().toString().toLowerCase().contains(filter) || (WearItems.getSummary() != null && WearItems.getSummary().toString().toLowerCase().contains(filter)));
        wear_category.setVisible(WearPreference.isVisible() || WearItems.isVisible());

        // Android Auto settings
        AutoPreference.setVisible(UsefulBits.isEmpty(filter) || AutoPreference.getTitle().toString().toLowerCase().contains(filter) || (AutoPreference.getSummary() != null && AutoPreference.getSummary().toString().toLowerCase().contains(filter)));
        AutoItems.setVisible(UsefulBits.isEmpty(filter) || AutoItems.getTitle().toString().toLowerCase().contains(filter) || (AutoItems.getSummary() != null && AutoItems.getSummary().toString().toLowerCase().contains(filter)));
        Auto_category.setVisible(AutoPreference.isVisible() || AutoItems.isVisible());

        // Advanced settings
        exportButton.setVisible(UsefulBits.isEmpty(filter) || exportButton.getTitle().toString().toLowerCase().contains(filter) || (exportButton.getSummary() != null && exportButton.getSummary().toString().toLowerCase().contains(filter)));
        importButton.setVisible(UsefulBits.isEmpty(filter) || importButton.getTitle().toString().toLowerCase().contains(filter) || (importButton.getSummary() != null && importButton.getSummary().toString().toLowerCase().contains(filter)));
        PermissionsSettings.setVisible(UsefulBits.isEmpty(filter) || PermissionsSettings.getTitle().toString().toLowerCase().contains(filter) || (PermissionsSettings.getSummary() != null && PermissionsSettings.getSummary().toString().toLowerCase().contains(filter)));
        resetApplication.setVisible(UsefulBits.isEmpty(filter) || resetApplication.getTitle().toString().toLowerCase().contains(filter) || (resetApplication.getSummary() != null && resetApplication.getSummary().toString().toLowerCase().contains(filter)));
        advanced_category.setVisible(logsButton.isVisible() || eventsButton.isVisible() || varsButton.isVisible() || exportButton.isVisible() || importButton.isVisible() || PermissionsSettings.isVisible() || resetApplication.isVisible());
        logsButton.setVisible(UsefulBits.isEmpty(filter) || logsButton.getTitle().toString().toLowerCase().contains(filter) || (logsButton.getSummary() != null && logsButton.getSummary().toString().toLowerCase().contains(filter)));
        eventsButton.setVisible(UsefulBits.isEmpty(filter) || eventsButton.getTitle().toString().toLowerCase().contains(filter) || (eventsButton.getSummary() != null && eventsButton.getSummary().toString().toLowerCase().contains(filter)));
        varsButton.setVisible(UsefulBits.isEmpty(filter) || varsButton.getTitle().toString().toLowerCase().contains(filter) || (varsButton.getSummary() != null && varsButton.getSummary().toString().toLowerCase().contains(filter)));

        // Other settings
        taskerPreference.setVisible(UsefulBits.isEmpty(filter) || taskerPreference.getTitle().toString().toLowerCase().contains(filter) || (taskerPreference.getSummary() != null && taskerPreference.getSummary().toString().toLowerCase().contains(filter)));
        other_category.setVisible(taskerPreference.isVisible());

        // About settings
        ReportErrorSettings.setVisible(UsefulBits.isEmpty(filter) || ReportErrorSettings.getTitle().toString().toLowerCase().contains(filter) || (ReportErrorSettings.getSummary() != null && ReportErrorSettings.getSummary().toString().toLowerCase().contains(filter)));
        TermsPreferences.setVisible(UsefulBits.isEmpty(filter) || TermsPreferences.getTitle().toString().toLowerCase().contains(filter) || (TermsPreferences.getSummary() != null && TermsPreferences.getSummary().toString().toLowerCase().contains(filter)));
        PrivacyPreferences.setVisible(UsefulBits.isEmpty(filter) || PrivacyPreferences.getTitle().toString().toLowerCase().contains(filter) || (PrivacyPreferences.getSummary() != null && PrivacyPreferences.getSummary().toString().toLowerCase().contains(filter)));
        versionPreferences.setVisible(UsefulBits.isEmpty(filter) || versionPreferences.getTitle().toString().toLowerCase().contains(filter) || (versionPreferences.getSummary() != null && versionPreferences.getSummary().toString().toLowerCase().contains(filter)));
        about.setVisible(UsefulBits.isEmpty(filter) || about.getTitle().toString().toLowerCase().contains(filter) || (about.getSummary() != null && about.getSummary().toString().toLowerCase().contains(filter)));
        about_category.setVisible(ReportErrorSettings.isVisible() || TermsPreferences.isVisible() || PrivacyPreferences.isVisible() || versionPreferences.isVisible() || about.isVisible());

        int defaultValue = mSharedPrefs.getActualStartupScreenIndex();
        startup_screen.setValueIndex(defaultValue);

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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.sort_custom_on));
                    return false;
                } else {
                    return true;
                }
            });


        if (ThemePreference != null)
            ThemePreference.setOnPreferenceClickListener(preference -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_theme));
                    return false;
                } else {
                    ((SettingsActivity) getActivity()).openThemePicker();
                    return true;
                }
            });

        if (ClockPreference != null)
            ClockPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_clock));
                    return false;
                }
                return true;
            });

        if (CameraPreference != null)
            CameraPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.dashboard_camera));
                    return false;
                }
                return true;
            });

        if (MultiServerPreference != null)
            MultiServerPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_bluetooth));
                    return false;
                }
                return true;
            });

        if (EnableWifiPreference != null)
            EnableWifiPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_wifi));
                    return false;
                }

                WorkManager.getInstance(mContext).cancelAllWorkByTag(WifiReceiver.workTag);
                WorkManager.getInstance(mContext).cancelAllWorkByTag(WifiReceiverManager.workTag);

                if (((boolean) newValue)) {
                    WorkManager.getInstance(mContext).enqueue(new OneTimeWorkRequest
                            .Builder(WifiReceiverManager.class)
                            .addTag(WifiReceiverManager.workTag)
                            .build());
                }
                return true;
            });

        if (EnableBeaconPreference != null)
            EnableBeaconPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_QRCode));
                    return false;
                }

                return true;
            });

        if (EnableSpeechPreference != null)
            EnableSpeechPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_Speech));
                    return false;
                }
                return true;
            });

        if (EnableTalkBackPreference != null)
            EnableTalkBackPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_talk_back));
                    return false;
                }
                return true;
            });

        if (NFCPreference != null)
            NFCPreference.setOnPreferenceClickListener(preference -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_QRCode));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, QRCodeSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (WifiPreference != null)
            WifiPreference.setOnPreferenceClickListener(preference -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_wifi));
                    return false;
                } else {
                    Intent intent = new Intent(mContext, WifiSettingsActivity.class);
                    startActivity(intent);
                    return true;
                }
            });

        if (BluetoothPreference != null)
            BluetoothPreference.setOnPreferenceClickListener(preference -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_wear));
                    return false;
                }
                return true;
            });

        if (AutoPreference != null)
            AutoPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.category_auto));
                    return false;
                }
                return true;
            });

        if (AlwaysOnPreference != null)
            AlwaysOnPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.always_on_title));
                    return false;
                }
                return true;
            });

        if (RefreshScreenPreference != null)
            RefreshScreenPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
                    showPremiumSnackbar(getString(R.string.always_auto_refresh));
                    return false;
                }
                return true;
            });

        if (AppController.IsPremiumEnabled) {
            if (preferenceScreen != null && premiumCategory != null) {
                preferenceScreen.removePreference(premiumCategory);
            }
        } else {
            if (premiumPreference != null)
                premiumPreference.setOnPreferenceClickListener(preference -> {
                    UsefulBits.openPremiumAppStore(mContext, this);
                    return true;
                });
            if (restorePreference != null)
                restorePreference.setOnPreferenceClickListener(preference -> {
                    showSnackbar("Restoring subscriptions");
                    UsefulBits.RestoreSubscriptions(mContext, this);
                    return true;
                });
        }

        if (BuildConfig.NEW_VERSION || BuildConfig.PAID_OOTT) {
            if (preferenceScreen != null && old_version_category != null) {
                preferenceScreen.removePreference(old_version_category);
            }
        } else {
            if (oldVersionPreference != null)
                oldVersionPreference.setOnPreferenceClickListener(preference -> {
                    UsefulBits.ShowOldVersionDialog(mContext);
                    return true;
                });
        }

        if (TermsPreferences != null)
            TermsPreferences.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.PAID_OOTT)
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://hnogames.nl/oott_terms.html")));
                else
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://hnogames.nl/domoticz_terms.html")));
                return true;
            });

        if (PrivacyPreferences != null)
            PrivacyPreferences.setOnPreferenceClickListener(preference -> {
                if (BuildConfig.PAID_OOTT)
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://hnogames.nl/oott_policy.html")));
                else
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
                if (!AppController.IsPremiumEnabled || !mSharedPrefs.isAPKValidated()) {
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

    private void handleAdvanceButtons() {
        androidx.preference.Preference logsButton = findPreference("logs_settings");
        androidx.preference.Preference eventsButton = findPreference("events_settings");
        androidx.preference.Preference varsButton = findPreference("vars_settings");

        if (logsButton != null) {
            logsButton.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(mContext, LogsActivity.class);
                startActivity(intent);
                return true;
            });
        }
        if (eventsButton != null) {
            eventsButton.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(mContext, EventsActivity.class);
                startActivity(intent);
                return true;
            });
        }
        if (varsButton != null) {
            varsButton.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(mContext, UserVariablesActivity.class);
                startActivity(intent);
                return true;
            });
        }
    }

    private void handleImportExportButtons() {
        androidx.preference.Preference exportButton = findPreference("export_settings");
        androidx.preference.Preference importButton = findPreference("import_settings");

        if (exportButton != null)
            exportButton.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ((SettingsActivity) getActivity()).exportSettings();
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!PermissionsUtil.canAccessStorage(mContext)) {
                            permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                        } else
                            ((SettingsActivity) getActivity()).exportSettings();
                    } else
                        ((SettingsActivity) getActivity()).exportSettings();
                }
                return false;
            });

        if (importButton != null)
            importButton.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ((SettingsActivity) getActivity()).importSettings();
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!PermissionsUtil.canAccessStorage(mContext)) {
                            permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                        } else
                            ((SettingsActivity) getActivity()).importSettings();
                    } else
                        ((SettingsActivity) getActivity()).importSettings();
                }
                return false;
            });
    }

    private void setStartUpScreenDefaultValue() {
    }

    private void showPremiumSnackbar(final String category) {
        try {
            new Handler().postDelayed(() -> {
                if (getView() != null) {
                    Snackbar.make(getView(), category + " " + getString(R.string.premium_feature), Snackbar.LENGTH_LONG)
                            .setAction(R.string.upgrade, view -> UsefulBits.openPremiumAppStore(mContext, this))
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_preferences, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter on preferences
                filter = newText;
                if (!UsefulBits.isEmpty(filter))
                    filter = filter.toLowerCase();
                setPreferences();
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void OnDone(boolean IsPremiumEnabled) {
        try {
            ((SettingsActivity) getActivity()).recreate();
        } catch (Exception ex) {
        }
    }
}