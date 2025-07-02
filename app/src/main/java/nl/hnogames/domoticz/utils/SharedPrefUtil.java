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

package nl.hnogames.domoticz.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.BeaconInfo;
import nl.hnogames.domoticz.containers.BluetoothInfo;
import nl.hnogames.domoticz.containers.LocationInfo;
import nl.hnogames.domoticz.containers.NFCInfo;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.containers.QRCodeInfo;
import nl.hnogames.domoticz.containers.SpeechInfo;
import nl.hnogames.domoticz.containers.WifiInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;
import nl.hnogames.domoticzapi.Interfaces.LanguageReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

@SuppressWarnings("SpellCheckingInspection")
public class SharedPrefUtil {
    private static final String PREF_ADS_COUNTER = "adsCounter";

    private static final String PREF_DASHBOARD_LIST = "dashboardAsList2";
    private static final int NR_OF_HISTORY = 100;
    private static final String PREF_MULTI_SERVER = "enableMultiServers";
    private static final String PREF_STARTUP_PROTECTION_ENABLED = "enableSecurity";
    private static final String PREF_CUSTOM_WEAR = "enableWearItems";
    private static final String PREF_CUSTOM_AUTOLIST = "enableAutoItems";
    private static final String PREF_CUSTOM_AUTO = "enableAutoNotifications";
    private static final String PREF_ENABLE_NFC = "enableNFC";
    private static final String PREF_ENABLE_Bluetooth = "enableBluetooth";
    private static final String PREF_ENABLE_WIFI = "enableWifi";
    private static final String PREF_ENABLE_BEACON = "enableBeacon";
    private static final String PREF_CUSTOM_WEAR_ITEMS = "wearItems";
    private static final String PREF_CUSTOM_AUTO_ITEMS = "autolistItems";
    private static final String PREF_ALWAYS_ON = "alwayson";
    private static final String PREF_AUTO_REFRESH = "autorefresh";
    private static final String PREF_AUTO_REFRESH_TIMER = "autorefreshTimer";
    private static final String PREF_NOTIFICATION_VIBRATE = "notification_vibrate";
    private static final String PREF_NOTIFICATION_SOUND = "notification_sound";
    private static final String PREF_DISPLAY_LANGUAGE = "displayLanguage";
    private static final String PREF_SAVED_LANGUAGE = "savedLanguage";
    private static final String PREF_SAVED_LANGUAGE_DATE = "savedLanguageDate";
    private static final String PREF_STARTUP_SCREEN = "startup_nav";
    private static final String PREF_TASK_SCHEDULED = "task_scheduled";
    private static final String PREF_NAVIGATION_ITEMS = "show_nav_items";
    private static final String PREF_NFC_TAGS = "nfc_tags";
    private static final String PREF_BLUETOOTH = "bluetooth";
    private static final String PREF_WIFI = "wifi";
    private static final String PREF_LAST_WIFI = "lastWifiConnection";
    private static final String PREF_BEACON = "beacon";
    private static final String PREF_QR_CODES = "qr_codes";
    private static final String PREF_SPEECH_COMMANDS = "speech_commands";
    private static final String PREF_GEOFENCE_LOCATIONS = "geofence_locations";
    private static final String PREF_GEOFENCE_ENABLED = "geofence_enabled";
    private static final String PREF_GEOFENCE_NOTIFICATIONS_ENABLED = "geofence_notifications_enabled";
    private static final String PREF_BLUETOOTH_NOTIFICATIONS_ENABLED = "bluetooth_notifications_enabled";
    private static final String PREF_WIFI_NOTIFICATIONS_ENABLED = "wifi_notifications_enabled";
    private static final String PREF_BEACON_NOTIFICATIONS_ENABLED = "beacon_notifications_enabled";
    private static final String PREF_SPEECH_ENABLED = "enableSpeech";
    private static final String PREF_QRCODE_ENABLED = "enableQRCode";
    private static final String PREF_GEOFENCE_STARTED = "geofence_started";
    private static final String PREF_ADVANCED_SETTINGS_ENABLED = "advanced_settings_enabled";
    private static final int INVALID_IDX = 999999;
    private static final String PREF_SAVED_LANGUAGE_STRING = "savedLanguageString";
    private static final String PREF_FIRST_START = "isFirstStart";
    private static final String PREF_WELCOME_SUCCESS = "welcomeSuccess";
    private static final String PREF_ENABLE_NOTIFICATIONS = "enableNotifications";
    private static final String PREF_OVERWRITE_NOTIFICATIONS = "overwriteNotifications";
    private static final String PREF_SUPPRESS_NOTIFICATIONS = "suppressNotifications";
    private static final String PREF_ALARM_NOTIFICATIONS = "alarmNotifications";
    private static final String PREF_RECEIVED_NOTIFICATIONS = "receivedNotifications";
    private static final String PREF_RECEIVED_NOTIFICATIONS_LOG = "receivedNotificationsLogv3";
    private static final String PREF_APK_VALIDATED = "apkvalidated";
    private static final String PREF_TALK_BACK = "talkBack";
    private static final String PREF_ALARM_TIMER = "alarmNotificationTimer";
    private static final String PREF_TEMP_MIN = "tempMinValue";
    private static final String PREF_TEMP_MAX = "tempMaxValue";
    private static final String PREF_SORT_LIST = "customSortList";
    private static final String PREF_DASHBOARD_CLOCK = "dashboardShowClock";
    private static final String PREF_DASHBOARD_PLANS = "dashboardShowPlans";
    private static final String PREF_DASHBOARD_CAMERA = "dashboardShowCamera";
    private static final String PREF_OLD_VERIONS_DIALOG_SHOWN = "oldVersion";

    private static final int DEFAULT_STARTUP_SCREEN = 1;
    private final String TAG = "Shared Pref util";
    @SuppressWarnings("FieldCanBeLocal")
    private final String PREF_SORT_CUSTOM = "sortCustom";
    @SuppressWarnings("FieldCanBeLocal")
    private final String PREF_LOCK_SORT_CUSTOM = "lockSortCustom";
    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Gson gson;

    @SuppressLint("CommitPrefEdits")
    public SharedPrefUtil(Context mContext) {
        this.mContext = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
        gson = new Gson();
    }

    public boolean addPlansToDashboard() {
        return prefs.getBoolean(PREF_DASHBOARD_PLANS, true);
    }

    public boolean addClockToDashboard() {
        return prefs.getBoolean(PREF_DASHBOARD_CLOCK, false);
    }

    public boolean addCameraToDashboard() {
        return prefs.getBoolean(PREF_DASHBOARD_CAMERA, false);
    }

    public boolean showDashboardAsList() {
        return prefs.getBoolean(PREF_DASHBOARD_LIST, false);
    }

    public int getAdsCounter() {
        return prefs.getInt(PREF_ADS_COUNTER, 0);
    }

    public void setAdsCounter(int id) {
        editor.putInt(PREF_ADS_COUNTER, id).apply();
        editor.commit();
    }

    public boolean showOldVersionDialog() {
        if (BuildConfig.NEW_VERSION)
            return false;

        return prefs.getBoolean(PREF_OLD_VERIONS_DIALOG_SHOWN, true);
    }

    public void OldVersionDialogShown() {
        editor.putBoolean(PREF_OLD_VERIONS_DIALOG_SHOWN, false).apply();
        editor.commit();
    }

    public boolean isMultiServerEnabled() {
        return prefs.getBoolean(PREF_MULTI_SERVER, false);
    }

    public boolean isStartupSecurityEnabled() {
        return prefs.getBoolean(PREF_STARTUP_PROTECTION_ENABLED, false);
    }

    public void setStartupSecurityEnabled(Boolean set) {
        editor.putBoolean(PREF_STARTUP_PROTECTION_ENABLED, set).apply();
        editor.commit();
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(PREF_ENABLE_NOTIFICATIONS, true);
    }

    public boolean OverWriteNotifications() {
        return prefs.getBoolean(PREF_OVERWRITE_NOTIFICATIONS, false);
    }

    public boolean enableCustomSorting() {
        return prefs.getBoolean(PREF_SORT_CUSTOM, false);
    }

    public boolean isCustomSortingLocked() {
        return prefs.getBoolean(PREF_LOCK_SORT_CUSTOM, false);
    }

    public boolean getAlwaysOn() {
        return prefs.getBoolean(PREF_ALWAYS_ON, false);
    }

    public boolean getAutoRefresh() {
        return prefs.getBoolean(PREF_AUTO_REFRESH, false);
    }

    public int getAutoRefreshTimer() {
        try {
            int value = Integer.valueOf(prefs.getString(PREF_AUTO_REFRESH_TIMER, "5"));
            if (value == -1) {
                editor.putString(PREF_AUTO_REFRESH_TIMER, "5").apply();
                return 5;
            }
            return value;
        } catch (Exception ex) {
            editor.putString(PREF_AUTO_REFRESH_TIMER, "5").apply();
            return 5;
        }
    }

    public int getAlarmTimer() {
        try {
            String timer = prefs.getString(PREF_ALARM_TIMER, "5");
            return Integer.valueOf(timer);
        } catch (Exception ex) {
            editor.putString(PREF_ALARM_TIMER, "5").apply();
            return 5;
        }
    }

    public int getTemperatureSetMin(String tempType) {
        if (UsefulBits.isEmpty(tempType) || tempType.equals("C")) {
            try {
                int value = Integer.valueOf(prefs.getString(PREF_TEMP_MIN, "10"));
                if (value == -1) {
                    editor.putString(PREF_TEMP_MIN, "10").apply();
                    return 10;
                }
                return value;
            } catch (Exception ex) {
                editor.putString(PREF_TEMP_MIN, "10").apply();
                return 10;
            }
        } else {
            try {
                int value = Integer.valueOf(prefs.getString(PREF_TEMP_MIN, "60"));
                if (value == -1) {
                    editor.putString(PREF_TEMP_MIN, "60").apply();
                    return 60;
                }
                return value;
            } catch (Exception ex) {
                editor.putString(PREF_TEMP_MIN, "60").apply();
                return 60;
            }
        }
    }

    public int getTemperatureSetMax(String tempType) {
        if (UsefulBits.isEmpty(tempType) || tempType.equals("C")) {
            try {
                int value = Integer.valueOf(prefs.getString(PREF_TEMP_MAX, "30"));
                if (value == -1) {
                    editor.putString(PREF_TEMP_MAX, "30").apply();
                    return 30;
                }
                return value;
            } catch (Exception ex) {
                editor.putString(PREF_TEMP_MAX, "30").apply();
                return 30;
            }
        } else {
            try {
                int value = Integer.valueOf(prefs.getString(PREF_TEMP_MAX, "90"));
                if (value == -1) {
                    editor.putString(PREF_TEMP_MAX, "90").apply();
                    return 90;
                }
                return value;
            } catch (Exception ex) {
                editor.putString(PREF_TEMP_MAX, "90").apply();
                return 90;
            }
        }
    }

    public void completeCard(String cardTag) {
        editor.putBoolean("CARD" + cardTag, true).apply();
    }

    public boolean isCardCompleted(String cardTag) {
        return prefs.getBoolean("CARD" + cardTag, false);
    }

    public void savePreviousColor(int idx, int color, int position) {
        editor.putInt("COLOR" + idx, color).apply();
        editor.putInt("COLORPOSITION" + idx, position).apply();
        editor.commit();
    }

    public int getPreviousColor(int idx) {
        return prefs.getInt("COLOR" + idx, 0);
    }

    public int getPreviousColorPosition(int idx) {
        return prefs.getInt("COLORPOSITION" + idx, 0);
    }

    public void setWidgetIDX(int widgetID, int idx, boolean isScene, String password, String value, int layout) {
        editor.putInt("WIDGET" + widgetID, idx).apply();
        editor.putBoolean("WIDGETSCENE" + widgetID, isScene).apply();
        editor.putString("WIDGETPASSWORD" + widgetID, password).apply();
        editor.putString("WIDGETVALUE" + widgetID, value).apply();
        editor.putInt("WIDGETLAYOUT" + widgetID, layout).apply();
        editor.commit();
    }

    public void deleteSmallWidget(int widgetID, boolean isScene) {
        editor.remove("SMALLWIDGET" + widgetID);
        if (!isScene)
            editor.remove("SMALLWIDGETIDX" + widgetID);
        else
            editor.remove("WIDGETIDXSCENE" + widgetID);
        editor.remove("SMALLWIDGETPASSWORD" + widgetID);
        editor.remove("SMALLWIDGETLAYOUT" + widgetID);
        editor.remove("SMALLWIDGETVALUE" + widgetID);
        editor.remove("SMALLWIDGETSCENE" + widgetID);
        editor.commit();
    }

    public void setSmallWidgetIDX(int widgetID, int idx, boolean isScene, String password, String value, int layout) {
        editor.putInt("SMALLWIDGET" + widgetID, idx).apply();
        editor.putBoolean("SMALLWIDGETSCENE" + widgetID, isScene).apply();
        editor.putString("SMALLWIDGETPASSWORD" + widgetID, password).apply();
        editor.putString("SMALLWIDGETVALUE" + widgetID, value).apply();
        editor.putInt("SMALLWIDGETLAYOUT" + widgetID, layout).apply();
        editor.commit();
    }

    public int getSmallWidgetIDX(int widgetID) {
        return prefs.getInt("SMALLWIDGET" + widgetID, INVALID_IDX);
    }

    public String getSmallWidgetPassword(int widgetID) {
        return prefs.getString("SMALLWIDGETPASSWORD" + widgetID, null);
    }

    public int getSmallWidgetLayout(int widgetID) {
        return prefs.getInt("SMALLWIDGETLAYOUT" + widgetID, -1);
    }

    public String getSmallWidgetValue(int widgetID) {
        return prefs.getString("SMALLWIDGETVALUE" + widgetID, null);
    }

    public boolean getSmallWidgetisScene(int widgetID) {
        return prefs.getBoolean("SMALLWIDGETSCENE" + widgetID, false);
    }

    public void deleteSmallTempWidget(int widgetID) {
        editor.remove("SMALLTEMPWIDGET" + widgetID);
        editor.remove("SMALLTEMPWIDGETIDX" + widgetID);
        editor.remove("SMALLTEMPWIDGETPASSWORD" + widgetID);
        editor.remove("SMALLTEMPWIDGETLAYOUT" + widgetID);
        editor.remove("SMALLTEMPWIDGETVALUE" + widgetID);
        editor.remove("SMALLTEMPWIDGETSCENE" + widgetID);
        editor.commit();
    }

    public void setSmallTempWidgetIDX(int widgetID, int idx, boolean isScene, String password, String value, int layout) {
        editor.putInt("SMALLTEMPWIDGET" + widgetID, idx).apply();
        editor.putBoolean("SMALLTEMPWIDGETSCENE" + widgetID, isScene).apply();
        editor.putString("SMALLTEMPWIDGETPASSWORD" + widgetID, password).apply();
        editor.putString("SMALLTEMPWIDGETVALUE" + widgetID, value).apply();
        editor.putInt("SMALLTEMPWIDGETLAYOUT" + widgetID, layout).apply();
        editor.commit();
    }

    public int getSmallTempWidgetIDX(int widgetID) {
        return prefs.getInt("SMALLTEMPWIDGET" + widgetID, INVALID_IDX);
    }

    public String getSmallTempWidgetPassword(int widgetID) {
        return prefs.getString("SMALLTEMPWIDGETPASSWORD" + widgetID, null);
    }

    public int getSmallTempWidgetLayout(int widgetID) {
        return prefs.getInt("SMALLTEMPWIDGETLAYOUT" + widgetID, -1);
    }

    public String getSmallTempWidgetValue(int widgetID) {
        return prefs.getString("SMALLTEMPWIDGETVALUE" + widgetID, null);
    }

    public void deleteSecurityWidget(int widgetID) {
        editor.remove("WIDGETSECURITY" + widgetID);
        editor.remove("WIDGETSECURITYPIN" + widgetID);
        editor.remove("WIDGETSECURITYLAYOUT" + widgetID);
        editor.remove("WIDGETSECURITYVALUE" + widgetID);
        editor.commit();
    }

    public void setSecurityWidgetIDX(int widgetID, int idx, String value, String pin, int layout) {
        editor.putInt("WIDGETSECURITY" + widgetID, idx).apply();
        editor.putString("WIDGETSECURITYVALUE" + widgetID, value).apply();
        editor.putString("WIDGETSECURITYPIN" + widgetID, pin).apply();
        editor.putInt("WIDGETSECURITYLAYOUT" + widgetID, layout).apply();
        editor.commit();
    }

    public int getSecurityWidgetIDX(int widgetID) {
        return prefs.getInt("WIDGETSECURITY" + widgetID, INVALID_IDX);
    }

    public int getSecurityWidgetLayout(int widgetID) {
        return prefs.getInt("WIDGETSECURITYLAYOUT" + widgetID, -1);
    }

    public String getSecurityWidgetValue(int widgetID) {
        return prefs.getString("WIDGETSECURITYVALUE" + widgetID, null);
    }

    public String getSecurityWidgetPin(int widgetID) {
        return prefs.getString("WIDGETSECURITYPIN" + widgetID, null);
    }

    public int getWidgetIDX(int widgetID) {
        return prefs.getInt("WIDGET" + widgetID, INVALID_IDX);
    }

    public String getWidgetPassword(int widgetID) {
        return prefs.getString("WIDGETPASSWORD" + widgetID, null);
    }

    public void deleteWidget(int widgetID, boolean isScene) {
        editor.remove("WIDGET" + widgetID);
        if (!isScene)
            editor.remove("WIDGETIDX" + widgetID);
        else
            editor.remove("WIDGETIDXSCENE" + widgetID);
        editor.remove("WIDGETPASSWORD" + widgetID);
        editor.remove("WIDGETLAYOUT" + widgetID);
        editor.remove("WIDGETVALUE" + widgetID);
        editor.remove("WIDGETSCENE" + widgetID);
        editor.commit();
    }

    public int getWidgetLayout(int widgetID) {
        return prefs.getInt("WIDGETLAYOUT" + widgetID, -1);
    }

    public String getWidgetValue(int widgetID) {
        return prefs.getString("WIDGETVALUE" + widgetID, null);
    }

    public boolean getWidgetisScene(int widgetID) {
        return prefs.getBoolean("WIDGETSCENE" + widgetID, false);
    }

    private void setWidgetIDforIDX(int widgetID, int idx, boolean isScene) {
        if (!isScene)
            editor.putInt("WIDGETIDX" + idx, widgetID).apply();
        else
            editor.putInt("WIDGETIDXSCENE" + idx, widgetID).apply();
    }

    private int getWidgetIDforIDX(int idx, boolean isScene) {
        if (!isScene)
            return prefs.getInt("WIDGETIDX" + idx, INVALID_IDX);
        else
            return prefs.getInt("WIDGETIDXSCENE" + idx, INVALID_IDX);
    }

    public boolean isFirstStart() {
        return prefs.getBoolean(PREF_FIRST_START, true);
    }

    public void setFirstStart(boolean firstStart) {
        editor.putBoolean(PREF_FIRST_START, firstStart).apply();
    }

    public boolean isWelcomeWizardSuccess() {
        return prefs.getBoolean(PREF_WELCOME_SUCCESS, false);
    }

    public void setWelcomeWizardSuccess(boolean success) {
        editor.putBoolean(PREF_WELCOME_SUCCESS, success).apply();
    }

    public List<String> getSortingList(String listName) {
        if (!prefs.contains(listName + PREF_SORT_LIST))
            return null;
        String sortString = prefs.getString(listName + PREF_SORT_LIST, null);
        if (!UsefulBits.isEmpty(sortString))
            return Arrays.asList(sortString.split(","));
        return null;
    }

    public void saveSortingList(String listName, List<String> ids) {
        if (ids != null)
            editor.putString(listName + PREF_SORT_LIST, TextUtils.join(",", ids)).apply();
        else
            editor.putString(listName + PREF_SORT_LIST, null).apply();
        editor.commit();
    }

    /**
     * Get's the users preference to vibrate on notifications
     *
     * @return true to vibrate
     */
    public boolean getNotificationVibrate() {
        return prefs.getBoolean(PREF_NOTIFICATION_VIBRATE, true);
    }

    /**
     * Get's the URL for the notification sound
     *
     * @return Notification sound URL
     */
    public String getNotificationSound() {
        return prefs.getString(PREF_NOTIFICATION_SOUND, null);
    }

    /**
     * Get's a list of suppressed notifications
     *
     * @return list of suppressed notifications
     */
    public List<String> getSuppressedNotifications() {
        if (!prefs.contains(PREF_SUPPRESS_NOTIFICATIONS)) return null;

        Set<String> notifications = prefs.getStringSet(PREF_SUPPRESS_NOTIFICATIONS, null);
        if (notifications != null) {
            List<String> notificationsValues = new ArrayList<>();
            for (String s : notifications) {
                notificationsValues.add(s);
            }
            return notificationsValues;
        } else return null;
    }

    /**
     * Get's a list of alarm notifications
     *
     * @return list of alarm notifications
     */
    public List<String> getAlarmNotifications() {
        if (!prefs.contains(PREF_ALARM_NOTIFICATIONS)) return null;

        Set<String> notifications = prefs.getStringSet(PREF_ALARM_NOTIFICATIONS, null);
        if (notifications != null) {
            List<String> notificationsValues = new ArrayList<>();

            for (String s : notifications) {
                notificationsValues.add(s);
            }
            return notificationsValues;
        } else return null;
    }

    /**
     * Get's a list of received notifications
     *
     * @return List of received notifications
     */
    public List<String> getReceivedNotifications() {
        if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS)) return null;

        Set<String> notifications = prefs.getStringSet(PREF_RECEIVED_NOTIFICATIONS, null);
        if (notifications != null) {
            List<String> notificationsValues = new ArrayList<>();

            for (String s : notifications) {
                notificationsValues.add(s);
            }
            java.util.Collections.sort(notificationsValues);
            return notificationsValues;
        } else return null;
    }

    /**
     * Get's a list of received notifications
     *
     * @return List of received notifications
     */
    public List<NotificationInfo> getLoggedNotifications() {
        if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS_LOG))
            return null;
        String json = prefs.getString(PREF_RECEIVED_NOTIFICATIONS_LOG, null);
        if (!UsefulBits.isEmpty(json)) {
            List<NotificationInfo> notifications = gson.fromJson(json, new TypeToken<List<NotificationInfo>>() {
            }.getType());
            if (notifications != null)
                Collections.sort(notifications);
            return notifications;
        } else
            return null;
    }

    /**
     * Adds the notification to the list of received notifications (only unique ones are stored)
     *
     * @param notification Notification string to add
     */
    public void addLoggedNotification(NotificationInfo notification) {
        if (notification == null)
            return;

        try {
            List<NotificationInfo> notifications = getLoggedNotifications();
            if (notifications == null)
                notifications = new ArrayList<>();

            notifications.add(notification);
            if (notifications.size() > NR_OF_HISTORY) {
                Collections.sort(notifications);
                notifications.remove(0);
            }
            editor.putString(PREF_RECEIVED_NOTIFICATIONS_LOG, gson.toJson(notifications)).apply();
        } catch (Exception ex) {
            Log.e(TAG, "Failed to save received notification: " + ex.getMessage());
        }
    }

    /**
     * Adds the notification to the list of received notifications (only unique ones are stored)
     *
     * @param notification Notification string to add
     */
    public void addUniqueReceivedNotification(String notification) {
        if (UsefulBits.isEmpty(notification))
            return;

        try {
            Set<String> notifications;
            if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS)) {
                notifications = new HashSet<>();
                notifications.add(notification);
                editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS, notifications).apply();
            } else {
                notifications = prefs.getStringSet(PREF_RECEIVED_NOTIFICATIONS, null);
                if (notifications == null)
                    notifications = new HashSet<>();
                if (!notifications.contains(notification)) {
                    notifications.add(notification);
                    editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS, notifications).apply();
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to save new type of notification: " + ex.getMessage());
        }
    }

    public void clearPreviousNotification() {
        editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS_LOG, null).apply();
        editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS, null).apply();
    }

    public void removeWizard() {
        // 1 if start up screen is 0 (wizard) change to dashboard
        if (getStartupScreenIndex() == 0) setStartupScreenIndex(DEFAULT_STARTUP_SCREEN);

        //2 remove wizard from navigation
        String removeWizard = "";
        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections != null) {
            for (String s : selections) {
                if (s.equals(allNames[0])) {
                    removeWizard = allNames[0];
                    break;
                }
            }
            if (removeWizard.length() > 0) {
                selections.remove(removeWizard);
                editor.putStringSet(PREF_NAVIGATION_ITEMS, selections).apply();
                editor.commit();
            }
        }
    }

    public int getActualStartupScreenIndex() {
        String startupScreenSelectedValue = prefs.getString(PREF_STARTUP_SCREEN, null);
        if (startupScreenSelectedValue == null)
            return DEFAULT_STARTUP_SCREEN;
        else {
            String[] startupScreenValues = mContext.getResources().getStringArray(R.array.startup_actions);
            int i = 0;
            for (String screen : startupScreenValues) {
                if (screen.equalsIgnoreCase(startupScreenSelectedValue)) {
                    return i;
                }
                i++;
            }
            //else, could not find startup screen
            setStartupScreenIndex(DEFAULT_STARTUP_SCREEN);
            return DEFAULT_STARTUP_SCREEN;
        }
    }

    public int getStartupScreenIndex() {
        String startupScreenSelectedValue = prefs.getString(PREF_STARTUP_SCREEN, null);
        if (startupScreenSelectedValue == null)
            return DEFAULT_STARTUP_SCREEN;
        else {
            String[] startupScreenValues = mContext.getResources().getStringArray(R.array.drawer_actions);
            int i = 0;
            for (String screen : startupScreenValues) {
                if (screen.equalsIgnoreCase(startupScreenSelectedValue)) {
                    return i;
                }
                i++;
            }

            // Check for dashboard actions
            startupScreenValues = mContext.getResources().getStringArray(R.array.dashboard_actions);
            for (String screen : startupScreenValues) {
                if (screen.equalsIgnoreCase(startupScreenSelectedValue)) {
                    return 1;
                }
            }

            // Check for temperature actions
            startupScreenValues = mContext.getResources().getStringArray(R.array.temperature_actions);
            for (String screen : startupScreenValues) {
                if (screen.equalsIgnoreCase(startupScreenSelectedValue)) {
                    return 2;
                }
            }

            //else, could not find startup screen
            setStartupScreenIndex(DEFAULT_STARTUP_SCREEN);
            return DEFAULT_STARTUP_SCREEN;
        }
    }

    public void setStartupScreenIndex(int position) {
        String[] startupScreenValues =
                mContext.getResources().getStringArray(R.array.startup_actions);
        String startupScreenValue;

        try {
            startupScreenValue = startupScreenValues[position];
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            startupScreenValue = startupScreenValues[DEFAULT_STARTUP_SCREEN];
        }

        editor.putString(PREF_STARTUP_SCREEN, startupScreenValue).apply();
    }

    public String getStartupScreen() {
        return prefs.getString(PREF_STARTUP_SCREEN, null);
    }

    public String[] getAutoSwitches() {
        if (!prefs.contains(PREF_CUSTOM_AUTO_ITEMS)) return null;
        Set<String> selections = prefs.getStringSet(PREF_CUSTOM_AUTO_ITEMS, null);
        if (selections != null) {
            String[] selectionValues = new String[selections.size()];

            int i = 0;
            for (String s : selections) {
                selectionValues[i] = s;
                i++;
            }
            return selectionValues;
        } else return null;
    }

    public String[] getWearSwitches() {
        if (!prefs.contains(PREF_CUSTOM_WEAR_ITEMS)) return null;
        Set<String> selections = prefs.getStringSet(PREF_CUSTOM_WEAR_ITEMS, null);
        if (selections != null) {
            String[] selectionValues = new String[selections.size()];

            int i = 0;
            for (String s : selections) {
                selectionValues[i] = s;
                i++;
            }
            return selectionValues;
        } else return null;
    }

    public String[] getNavigationFragments() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS))
            setNavigationDefaults();

        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allValues = mContext.getResources().getStringArray(R.array.drawer_fragments);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections == null)
            return allValues;
        else {
            String[] selectionValues = new String[selections.size()];
            int i = 0;
            int index = 0;

            for (String v : allNames) {
                for (String s : selections) {
                    if (s.equals(v)) {
                        selectionValues[i] = allValues[index];
                        i++;
                    }
                }
                index++;
            }

            return selectionValues;
        }
    }

    public String[] getNavigationActions() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS))
            setNavigationDefaults();

        try {
            Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
            String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

            if (selections == null) //default
                return allNames;
            else {
                int i = 0;
                String[] selectionValues = new String[selections.size()];
                for (String v : allNames) {
                    for (String s : selections) {
                        if (s.equals(v)) {
                            selectionValues[i] = v;
                            i++;
                        }
                    }
                }

                if (i < selections.size()) {
                    setNavigationDefaults();
                    return getNavigationActions();
                } else
                    return selectionValues;
            }
        } catch (Exception ex) {
            if (!UsefulBits.isEmpty(ex.getMessage()))
                Log.e(TAG, ex.getMessage());
            setNavigationDefaults();//try to correct the issue
        }

        return null; //failed, can't show the menu (can be a translation issue if this happens!!)
    }

    public void setNavigationDefaults() {
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);
        Set<String> selections = new HashSet<>(Arrays.asList(allNames));
        editor.putStringSet(PREF_NAVIGATION_ITEMS, selections).apply();
    }

    public String[] getNavigationIcons() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS)) setNavigationDefaults();
        TypedArray icons = mContext.getResources().obtainTypedArray(R.array.drawer_icons);
        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections != null) {
            String[] selectedICONS = new String[selections.size()];
            int iconIndex = 0;
            int index = 0;
            for (String v : allNames) {
                for (String s : selections) {
                    if (s.equals(v)) {
                        selectedICONS[iconIndex] = icons.getString(index);
                        iconIndex++;
                    }
                }
                index++;
            }
            icons.recycle();
            return selectedICONS;
        } else {
            icons.recycle();
            return null;
        }
    }

    public boolean isDebugEnabled() {
        return false;
    }

    public boolean isAdvancedSettingsEnabled() {
        return prefs.getBoolean(PREF_ADVANCED_SETTINGS_ENABLED, false);
    }

    public void setAdvancedSettingsEnabled(boolean enabled) {
        editor.putBoolean(PREF_ADVANCED_SETTINGS_ENABLED, enabled).apply();
    }

    public boolean showCustomWear() {
        return prefs.getBoolean(PREF_CUSTOM_WEAR, false);
    }

    public boolean showCustomAndroidAuto() {
        return prefs.getBoolean(PREF_CUSTOM_AUTOLIST, false);
    }

    public boolean showAutoNotifications() {
        return prefs.getBoolean(PREF_CUSTOM_AUTO, false);
    }

    public boolean isNFCEnabled() {
        return prefs.getBoolean(PREF_ENABLE_NFC, false);
    }

    public void setNFCEnabled(boolean enabled) {
        editor.putBoolean(PREF_ENABLE_NFC, enabled).apply();
    }

    public boolean isBluetoothEnabled() {
        return prefs.getBoolean(PREF_ENABLE_Bluetooth, false);
    }

    public void setBluetoothEnabled(boolean enabled) {
        editor.putBoolean(PREF_ENABLE_Bluetooth, enabled).apply();
    }

    public boolean isWifiEnabled() {
        return prefs.getBoolean(PREF_ENABLE_WIFI, false);
    }

    public void setWifiEnabled(boolean enabled) {
        editor.putBoolean(PREF_ENABLE_WIFI, enabled).apply();
    }

    public boolean isBeaconEnabled() {
        return prefs.getBoolean(PREF_ENABLE_BEACON, false);
    }

    public void setBeaconEnabled(boolean enabled) {
        editor.putBoolean(PREF_ENABLE_BEACON, enabled).apply();
    }

    //public boolean isServerUpdateAvailable() {
    //    return prefs.getBoolean(PREF_UPDATE_SERVER_AVAILABLE, false);
    //}

    //public String getPreviousVersionNumber() {
    //    return prefs.getString(PREF_LAST_VERSION, "");
    //}

    //public void setVersionNumber(String version) {
    //    editor.putString(PREF_LAST_VERSION, version);
    //    editor.commit();
    //}

    //public String getLastUpdateShown() {
    //    return prefs.getString(PREF_UPDATE_SERVER_SHOWN, "");
    //}

    //public void setLastUpdateShown(String revisionNb) {
    //   editor.putString(PREF_UPDATE_SERVER_SHOWN, revisionNb);
    //   editor.commit();
    //}

    public boolean isGeofenceEnabled() {
        return prefs.getBoolean(PREF_GEOFENCE_ENABLED, false);
    }

    public void setGeofenceEnabled(boolean enabled) {
        editor.putBoolean(PREF_GEOFENCE_ENABLED, enabled).apply();
        editor.commit();
    }

    public boolean isGeofenceNotificationsEnabled() {
        return prefs.getBoolean(PREF_GEOFENCE_NOTIFICATIONS_ENABLED, false);
    }

    public void setGeofenceNotificationsEnabled(boolean enabled) {
        editor.putBoolean(PREF_GEOFENCE_NOTIFICATIONS_ENABLED, enabled).apply();
        editor.commit();
    }

    public boolean isBluetoothNotificationsEnabled() {
        return prefs.getBoolean(PREF_BLUETOOTH_NOTIFICATIONS_ENABLED, false);
    }

    public void setBluetoothNotificationsEnabled(boolean enabled) {
        editor.putBoolean(PREF_BLUETOOTH_NOTIFICATIONS_ENABLED, enabled).apply();
        editor.commit();
    }

    public boolean isWifiNotificationsEnabled() {
        return prefs.getBoolean(PREF_WIFI_NOTIFICATIONS_ENABLED, false);
    }

    public void setWifiNotificationsEnabled(boolean enabled) {
        editor.putBoolean(PREF_WIFI_NOTIFICATIONS_ENABLED, enabled).apply();
        editor.commit();
    }

    public boolean isBeaconNotificationsEnabled() {
        return prefs.getBoolean(PREF_BEACON_NOTIFICATIONS_ENABLED, false);
    }

    public boolean isTalkBackEnabled() {
        return prefs.getBoolean(PREF_TALK_BACK, false);
    }

    public boolean isAPKValidated() {
        return true;
    }

    public void setAPKValidated(boolean valid) {
        editor.putBoolean(PREF_APK_VALIDATED, valid).apply();
    }

    public boolean isQRCodeEnabled() {
        return prefs.getBoolean(PREF_QRCODE_ENABLED, false);
    }

    public void setQRCodeEnabled(boolean enabled) {
        editor.putBoolean(PREF_QRCODE_ENABLED, enabled).apply();
    }

    public boolean isSpeechEnabled() {
        return prefs.getBoolean(PREF_SPEECH_ENABLED, false);
    }

    public void setSpeechEnabled(boolean speechEnabled) {
        editor.putBoolean(PREF_SPEECH_ENABLED, speechEnabled).apply();
    }

    public void saveNFCList(List<NFCInfo> list) {
        editor.putString(PREF_NFC_TAGS, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<NFCInfo> getNFCList() {
        ArrayList<NFCInfo> oReturnValue = new ArrayList<>();
        List<NFCInfo> nfcs;
        if (prefs.contains(PREF_NFC_TAGS)) {
            String jsonNFCs = prefs.getString(PREF_NFC_TAGS, null);
            NFCInfo[] item = gson.fromJson(jsonNFCs,
                    NFCInfo[].class);
            nfcs = Arrays.asList(item);
            for (NFCInfo n : nfcs) {
                oReturnValue.add(n);
            }
        } else
            return null;

        return oReturnValue;
    }

    public void saveWifiList(List<WifiInfo> list) {
        editor.putString(PREF_WIFI, gson.toJson(list));
        editor.commit();
    }

    public void saveLastWifi(WifiInfo wifi) {
        editor.putString(PREF_LAST_WIFI, gson.toJson(wifi));
        editor.commit();
    }

    public WifiInfo getLastWifi() {
        List<WifiInfo> Wifis;
        if (prefs.contains(PREF_LAST_WIFI)) {
            String jsonWifi = prefs.getString(PREF_LAST_WIFI, null);
            return gson.fromJson(jsonWifi, WifiInfo.class);
        } else
            return null;
    }

    public ArrayList<WifiInfo> getWifiList() {
        ArrayList<WifiInfo> oReturnValue;
        List<WifiInfo> Wifis;
        if (prefs.contains(PREF_WIFI)) {
            String jsonWifis = prefs.getString(PREF_WIFI, null);
            WifiInfo[] item = gson.fromJson(jsonWifis,
                    WifiInfo[].class);
            Wifis = Arrays.asList(item);
            oReturnValue = new ArrayList<>(Wifis);
        } else
            return null;
        return oReturnValue;
    }

    public void saveBluetoothList(List<BluetoothInfo> list) {
        editor.putString(PREF_BLUETOOTH, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<BluetoothInfo> getBluetoothList() {
        ArrayList<BluetoothInfo> oReturnValue;
        List<BluetoothInfo> Bluetooths;
        if (prefs.contains(PREF_BLUETOOTH)) {
            String jsonBluetooths = prefs.getString(PREF_BLUETOOTH, null);
            BluetoothInfo[] item = gson.fromJson(jsonBluetooths,
                    BluetoothInfo[].class);
            Bluetooths = Arrays.asList(item);
            oReturnValue = new ArrayList<>(Bluetooths);
        } else
            return null;
        return oReturnValue;
    }

    public void saveBeaconList(List<BeaconInfo> list) {
        editor.putString(PREF_BEACON, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<BeaconInfo> getBeaconList() {
        ArrayList<BeaconInfo> oReturnValue;
        List<BeaconInfo> beacons;
        if (prefs.contains(PREF_BEACON)) {
            String jsonBeacons = prefs.getString(PREF_BEACON, null);
            BeaconInfo[] item = gson.fromJson(jsonBeacons, BeaconInfo[].class);
            beacons = Arrays.asList(item);
            oReturnValue = new ArrayList<>(beacons);
        } else
            return null;
        return oReturnValue;
    }

    public void saveQRCodeList(List<QRCodeInfo> list) {
        editor.putString(PREF_QR_CODES, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<QRCodeInfo> getQRCodeList() {
        ArrayList<QRCodeInfo> oReturnValue = new ArrayList<>();
        List<QRCodeInfo> qrs;
        if (prefs.contains(PREF_QR_CODES)) {
            String jsonNFCs = prefs.getString(PREF_QR_CODES, null);
            QRCodeInfo[] item = gson.fromJson(jsonNFCs,
                    QRCodeInfo[].class);
            qrs = Arrays.asList(item);
            for (QRCodeInfo n : qrs) {
                oReturnValue.add(n);
            }
        } else
            return null;

        return oReturnValue;
    }

    public void saveSpeechList(List<SpeechInfo> list) {
        editor.putString(PREF_SPEECH_COMMANDS, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<SpeechInfo> getSpeechList() {
        ArrayList<SpeechInfo> oReturnValue = new ArrayList<>();
        List<SpeechInfo> qrs;
        if (prefs.contains(PREF_SPEECH_COMMANDS)) {
            String jsonNFCs = prefs.getString(PREF_SPEECH_COMMANDS, null);
            SpeechInfo[] item = gson.fromJson(jsonNFCs,
                    SpeechInfo[].class);
            qrs = Arrays.asList(item);
            for (SpeechInfo n : qrs) {
                oReturnValue.add(n);
            }
        } else
            return null;

        return oReturnValue;
    }

    public void saveLocations(List<LocationInfo> locations) {
        String jsonLocations = gson.toJson(locations);
        editor.putString(PREF_GEOFENCE_LOCATIONS, jsonLocations);
        editor.commit();
    }

    public ArrayList<LocationInfo> getLocations() {
        ArrayList<LocationInfo> returnValue = new ArrayList<>();
        List<LocationInfo> locations;
        boolean incorrectDetected = false;

        if (prefs.contains(PREF_GEOFENCE_LOCATIONS)) {
            String jsonLocations = prefs.getString(PREF_GEOFENCE_LOCATIONS, null);
            LocationInfo[] locationItem = gson.fromJson(jsonLocations,
                    LocationInfo[].class);
            locations = Arrays.asList(locationItem);
            for (LocationInfo l : locations) {
                if (l.toGeofence() != null) {
                    returnValue.add(l);
                } else {
                    incorrectDetected = true;
                }
            }
            if (incorrectDetected) {
                saveLocations(returnValue);
                Toast.makeText(mContext,
                        R.string.geofence_error_recreateLocations,
                        Toast.LENGTH_LONG).show();
            }
        } else
            return null;
        return returnValue;
    }

    public LocationInfo getLocation(int id) {
        List<LocationInfo> locations = getLocations();
        for (LocationInfo l : locations) {
            if (l.getID() == id)
                return l;
        }
        return null;
    }

    public BeaconInfo getBeacon(String name) {
        List<BeaconInfo> beacons = getBeaconList();
        for (BeaconInfo l : beacons) {
            if (l.getName().equals(name))
                return l;
        }
        return null;
    }

    public void addLocation(LocationInfo location) {
        List<LocationInfo> locations = getLocations();
        if (locations == null)
            locations = new ArrayList<>();
        locations.add(location);
        saveLocations(locations);
    }

    public void updateLocation(LocationInfo location) {
        List<LocationInfo> locations = getLocations();
        if (locations == null)
            locations = new ArrayList<>();
        int i = 0;
        for (LocationInfo l : locations) {
            if (l.getID() == location.getID()) {
                locations.set(i, location);
            }
            i++;
        }
        saveLocations(locations);
    }

    public void removeLocation(LocationInfo location) {
        ArrayList<LocationInfo> locations = getLocations();
        ArrayList<LocationInfo> removeLocations = new ArrayList<>();
        if (locations != null) {
            for (LocationInfo l : locations) {
                if (l.getID() == location.getID())
                    removeLocations.add(l);
            }
            for (LocationInfo l : removeLocations) {
                locations.remove(l);
            }
            saveLocations(locations);
        }
    }

    public boolean saveSharedPreferencesToFile(Uri file) {
        try {
            boolean isServerUpdateAvailableValue = false;
            ServerUpdateInfo mServerUpdateInfo = StaticHelper.getServerUtil(mContext).getActiveServer().getServerUpdateInfo(mContext);

            Map<String, ?> oAllPrefs = this.prefs.getAll();
            HashMap<String, Object> oSavePrefs = new HashMap<String, Object>();
            for (Map.Entry<String, ?> entry : oAllPrefs.entrySet()) {
                if (entry.getValue() != null) {
                    //Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                    if (entry.getKey().startsWith("WIDGET") || entry.getKey().startsWith("SMALLWIDGET") || entry.getKey().startsWith("SMALLTEMPWIDGET") || entry.getKey().startsWith("WIDGETSECURITY"))
                        Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                    else if (entry.getKey().equals("receivedNotifications") || entry.getKey().equals("receivedNotificationsLog"))
                        Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                    else {
                        Log.i("PREFS", "Exported: " + entry.getKey() + ": " + entry.getValue().toString());
                        oSavePrefs.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            OutputStream outputStream;
            ObjectOutputStream output = null;
            try {
                outputStream = mContext.getContentResolver().openOutputStream(file);
                output = new ObjectOutputStream(outputStream);
                output.writeObject(oSavePrefs);
                output.flush();
                output.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Write original settings to preferences
            if (isServerUpdateAvailableValue)
                mServerUpdateInfo.setUpdateAvailable(true);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressWarnings({"UnnecessaryUnboxing", "unchecked"})
    public boolean loadSharedPreferencesFromFile(Uri src) {
        boolean res = false;
        ObjectInputStream input = null;
        //noinspection TryWithIdenticalCatches
        try {
            InputStream iputStream = mContext.getContentResolver().openInputStream(src);
            input = new ObjectInputStream(iputStream);
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                if (v != null && !UsefulBits.isEmpty(key)) {
                    if (entry.getKey().startsWith("WIDGET") || entry.getKey().startsWith("SMALLWIDGET") || entry.getKey().startsWith("SMALLTEMPWIDGET") || entry.getKey().startsWith("WIDGETSECURITY"))
                        Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                    else if (entry.getKey().equals("receivedNotifications") || entry.getKey().equals("receivedNotificationsLog"))
                        Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                    else {
                        try {
                            if (v instanceof Boolean)
                                editor.putBoolean(key, ((Boolean) v).booleanValue());
                            else if (v instanceof Float)
                                editor.putFloat(key, ((Float) v).floatValue());
                            else if (v instanceof Integer)
                                editor.putInt(key, ((Integer) v).intValue());
                            else if (v instanceof Long)
                                editor.putLong(key, ((Long) v).longValue());
                            else if (v instanceof String)
                                editor.putString(key, ((String) v));
                            else if (v instanceof Set)
                                editor.putStringSet(key, ((Set<String>) v));
                            else
                                Log.v(TAG, "Could not load pref: " + key + " | " + v.getClass());
                        } catch (Exception ex) {
                        }
                    }
                }
            }
            editor.commit();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception ex) {
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Get the user prefered display language
     *
     * @return Language string
     */
    public String getDisplayLanguage() {
        return prefs.getString(PREF_DISPLAY_LANGUAGE, "");
    }

    /**
     * Get's the date (in milliseconds) when the language files where saved
     *
     * @return time in milliseconds
     */
    public long getSavedLanguageDate() {
        return prefs.getLong(PREF_SAVED_LANGUAGE_DATE, 0);
    }

    /**
     * Set's the date (in milliseconds) when the language files are saved
     *
     * @param timeInMillis time in milliseconds
     */
    public void setSavedLanguageDate(long timeInMillis) {
        editor.putLong(PREF_SAVED_LANGUAGE_DATE, timeInMillis).apply();
    }

    /**
     * Save language to shared preferences
     *
     * @param language The translated strings to save to shared preferences
     */
    public void saveLanguage(Language language) {
        if (language != null) {
            try {
                String jsonLocations = gson.toJson(language);
                editor.putString(PREF_SAVED_LANGUAGE, jsonLocations).apply();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    /**
     * Get the saved language from shared preferences
     *
     * @return Language with tranlated strings
     */
    public Language getSavedLanguage() {
        Language returnValue;

        if (prefs.contains(PREF_SAVED_LANGUAGE)) {
            String languageStr = prefs.getString(PREF_SAVED_LANGUAGE, null);
            if (languageStr != null) {
                try {
                    returnValue = gson.fromJson(languageStr, Language.class);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            } else return null;
        } else
            return null;

        return returnValue;
    }

    /**
     * Get's the translated strings from the server and saves them to shared preferences
     *
     * @param langToDownload Language to get from the server
     * @param server         ServerUtil
     */
    public boolean getLanguageStringsFromServer(final String langToDownload, ServerUtil server) {

        final boolean[] result = new boolean[1];
        if (!UsefulBits.isEmpty(langToDownload)) {
            StaticHelper.getDomoticz(mContext).getLanguageStringsFromServer(langToDownload, new LanguageReceiver() {
                @Override
                public void onReceiveLanguage(Language language) {
                    Log.d(TAG, "Language " + langToDownload + " downloaded from server");
                    Calendar now = Calendar.getInstance();
                    saveLanguage(language);
                    // Write to shared preferences so we can use it to check later
                    setDownloadedLanguage(langToDownload);
                    setSavedLanguageDate(now.getTimeInMillis());
                    result[0] = true;
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Unable to get the language from the server: " + langToDownload);
                    error.printStackTrace();
                    result[0] = false;
                }
            });
        } else {
            Log.d(TAG, "Aborting: Language to download not specified");
            result[0] = false;
        }
        return result[0];
    }

    public String getDownloadedLanguage() {
        return prefs.getString(PREF_SAVED_LANGUAGE_STRING, "");
    }

    public void setDownloadedLanguage(String language) {
        editor.putString(PREF_SAVED_LANGUAGE_STRING, language).apply();
    }

    public boolean getTaskIsScheduled() {
        return prefs.getBoolean(PREF_TASK_SCHEDULED, false);
    }

    public void setTaskIsScheduled(boolean isScheduled) {
        editor.putBoolean(PREF_TASK_SCHEDULED, isScheduled).apply();
    }

    public boolean isGeofencingStarted() {
        return prefs.getBoolean(PREF_GEOFENCE_STARTED, false);
    }

    public void setGeofencingStarted(boolean started) {
        editor.putBoolean(PREF_GEOFENCE_STARTED, started).apply();
    }

    public List<Geofence> getEnabledGeofences() {
        final List<Geofence> mGeofenceList = new ArrayList<>();
        final ArrayList<LocationInfo> locations = getLocations();

        if (locations != null) {
            for (LocationInfo locationInfo : locations)
                if (locationInfo.getEnabled())
                    mGeofenceList.add(locationInfo.toGeofence());
            return mGeofenceList;
        } else return null;
    }
}
