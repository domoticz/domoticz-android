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

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.fastaccess.permission.base.PermissionHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.List;

import nl.hnogames.domoticz.NotificationHistoryActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;

public class NotificationPreferenceFragment extends PreferenceFragmentCompat {
    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String TAG = NotificationPreferenceFragment.class.getSimpleName();

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
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
        addPreferencesFromResource(R.xml.notificationpreferences);

        mContext = getActivity();
        permissionHelper = PermissionHelper.getInstance(getActivity());
        mSharedPrefs = new SharedPrefUtil(mContext);
        mConfigInfo = StaticHelper.getServerUtil(getActivity()).getActiveServer().getConfigInfo(mContext);

        setIconColor();
        setPreferences();
    }

    private void setIconColor() {
        int colorAttr = R.attr.preferenceIconColor;
        TypedArray ta = mContext.getTheme().obtainStyledAttributes(new int[]{colorAttr});
        int color = ta.getColor(0, 0);
        ta.recycle();
        tintIcons(getPreferenceScreen(), color);
    }

    private void setPreferences() {
        androidx.preference.Preference NotificationLogged = findPreference("notification_show_logs");
        NotificationsMultiSelectListPreference notificationsMultiSelectListPreference = findPreference("suppressNotifications");
        NotificationsMultiSelectListPreference alarmMultiSelectListPreference = findPreference("alarmNotifications");
        final androidx.preference.Preference registrationId = findPreference("notification_registration_id");
        androidx.preference.Preference firebaseSettings = findPreference("firebase_settings");
        PreferenceCategory notificationCategory = findPreference("notificationcategory");
        androidx.preference.Preference noticiationSettings = findPreference("noticiationSettings");

        // Firebase Settings click handler
        if (firebaseSettings != null) {
            firebaseSettings.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(mContext, nl.hnogames.domoticz.FirebaseSettingsActivity.class));
                return true;
            });
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            androidx.preference.PreferenceCategory notificationSound = findPreference("notificationSound");
            if (notificationCategory != null && notificationSound != null)
                notificationCategory.removePreference(notificationSound);
        } else {
            if (notificationCategory != null && noticiationSettings != null)
                notificationCategory.removePreference(noticiationSettings);
        }

        if (noticiationSettings != null)
            noticiationSettings.setOnPreferenceClickListener(preference -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, NotificationUtil.CHANNEL_ID);
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, mContext.getPackageName());
                    startActivity(intent);
                    return true;
                }
                return false;
            });

        List<String> notifications = mSharedPrefs.getReceivedNotifications();
        if (notifications == null || notifications.size() <= 0) {
            if (notificationsMultiSelectListPreference != null)
                notificationsMultiSelectListPreference.setEnabled(false);
            if (alarmMultiSelectListPreference != null)
                alarmMultiSelectListPreference.setEnabled(false);
        } else {
            if (notificationsMultiSelectListPreference != null)
                notificationsMultiSelectListPreference.setEnabled(true);
            if (alarmMultiSelectListPreference != null)
                alarmMultiSelectListPreference.setEnabled(true);
        }

        if (registrationId != null)
            registrationId.setOnPreferenceClickListener(preference -> {
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
            });

        if (NotificationLogged != null)
            NotificationLogged.setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(mContext, NotificationHistoryActivity.class));
                return true;
            });
    }

    private void showSnack(final String text) {
        try {
            new Handler().postDelayed(() -> {
                if (getView() != null) {
                    Snackbar.make(getView(), text, Snackbar.LENGTH_LONG).show();
                }
            }, (300));
        } catch (Exception ex) {
            Log.e(TAG, "No snack shown: " + ex.getMessage());
        }
    }

    private void pushGCMRegistrationIds() {
        final String UUID = DeviceUtils.getUniqueID(mContext);
        final String senderId = FirebaseInstanceId.getInstance().getToken();
        StaticHelper.getDomoticz(mContext).CleanMobileDevice(UUID, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                //previous id cleaned
                StaticHelper.getDomoticz(mContext).AddMobileDevice(UUID, senderId, new MobileDeviceReceiver() {
                    @Override
                    public void onSuccess() {
                        if (isAdded())
                            showSnack(mContext.getString(R.string.notification_settings_pushed));
                    }

                    @Override
                    public void onError(Exception error) {
                        if (isAdded())
                            showSnack(mContext.getString(R.string.notification_settings_push_failed));
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                //nothing to clean..
                StaticHelper.getDomoticz(mContext).AddMobileDevice(UUID, senderId, new MobileDeviceReceiver() {
                    @Override
                    public void onSuccess() {
                        if (isAdded())
                            showSnack(mContext.getString(R.string.notification_settings_pushed));
                    }

                    @Override
                    public void onError(Exception error) {
                        if (isAdded())
                            showSnack(mContext.getString(R.string.notification_settings_push_failed));
                    }
                });
            }
        });
    }
}