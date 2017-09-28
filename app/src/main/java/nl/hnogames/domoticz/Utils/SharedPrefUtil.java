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

package nl.hnogames.domoticz.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Containers.NFCInfo;
import nl.hnogames.domoticz.Containers.QRCodeInfo;
import nl.hnogames.domoticz.Containers.SpeechInfo;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.LanguageReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;

@SuppressWarnings("SpellCheckingInspection")
public class SharedPrefUtil {

    private static final String PREF_MULTI_SERVER = "enableMultiServers";
    private static final String PREF_STARTUP_PROTECTION_ENABLED = "enableSecurity";
    private static final String PREF_CUSTOM_WEAR = "enableWearItems";
    private static final String PREF_CUSTOM_AUTO = "enableAutoNotifications";
    private static final String PREF_ENABLE_NFC = "enableNFC";
    private static final String PREF_CUSTOM_WEAR_ITEMS = "wearItems";
    private static final String PREF_ALWAYS_ON = "alwayson";
    private static final String PREF_NOTIFICATION_VIBRATE = "notification_vibrate";
    private static final String PREF_NOTIFICATION_SOUND = "notification_sound";
    private static final String PREF_DISPLAY_LANGUAGE = "displayLanguage";
    private static final String PREF_SAVED_LANGUAGE = "savedLanguage";
    private static final String PREF_SAVED_LANGUAGE_DATE = "savedLanguageDate";
    private static final String PREF_UPDATE_SERVER_AVAILABLE = "updateserveravailable";
    private static final String PREF_UPDATE_SERVER_SHOWN = "updateservershown";
    private static final String PREF_EXTRA_DATA = "extradata";
    private static final String PREF_STARTUP_SCREEN = "startup_screen";
    private static final String PREF_TASK_SCHEDULED = "task_scheduled";
    private static final String PREF_NAVIGATION_ITEMS = "enable_menu_items";
    private static final String PREF_NFC_TAGS = "nfc_tags";
    private static final String PREF_QR_CODES = "qr_codes";
    private static final String PREF_SPEECH_COMMANDS = "speech_commands";
    private static final String PREF_GEOFENCE_LOCATIONS = "geofence_locations";
    private static final String PREF_GEOFENCE_ENABLED = "geofence_enabled";
    private static final String PREF_GEOFENCE_NOTIFICATIONS_ENABLED = "geofence_notifications_enabled";
    private static final String PREF_SPEECH_ENABLED = "enableSpeech";
    private static final String PREF_QRCODE_ENABLED = "enableQRCode";
    private static final String PREF_GEOFENCE_STARTED = "geofence_started";
    private static final String PREF_ADVANCED_SETTINGS_ENABLED = "advanced_settings_enabled";
    private static final String PREF_DEBUGGING = "debugging";
    private static final int INVALID_IDX = 999999;
    private static final String PREF_SAVED_LANGUAGE_STRING = "savedLanguageString";
    private static final String PREF_FIRST_START = "isFirstStart";
    private static final String PREF_WELCOME_SUCCESS = "welcomeSuccess";
    private static final String PREF_ENABLE_NOTIFICATIONS = "enableNotifications";
    private static final String PREF_OVERWRITE_NOTIFICATIONS = "overwriteNotifications";
    private static final String PREF_SUPPRESS_NOTIFICATIONS = "suppressNotifications";
    private static final String PREF_ALARM_NOTIFICATIONS = "alarmNotifications";
    private static final String PREF_RECEIVED_NOTIFICATIONS = "receivedNotifications";
    private static final String PREF_RECEIVED_NOTIFICATIONS_LOG = "receivedNotificationsLog";
    private static final String PREF_CHECK_UPDATES = "checkForSystemUpdates";
    private static final String PREF_LAST_VERSION = "lastappversion";
    private static final String PREF_APK_VALIDATED = "apkvalidated";
    private static final String PREF_TALK_BACK = "talkBack";
    private static final String PREF_ALARM_TIMER = "alarmNotificationTimer";
    private static final String PREF_TEMP_MIN = "tempMinValue";
    private static final String PREF_TEMP_MAX = "tempMaxValue";
    private static final String PREF_WIDGET_ENABLED = "enableWidgets";


    private final String TAG = "Shared Pref util";
    @SuppressWarnings("FieldCanBeLocal")
    private final String PREF_SORT_LIKESERVER = "sort_dashboardLikeServer";
    @SuppressWarnings("FieldCanBeLocal")
    private final String PREF_DARK_THEME = "darkTheme";
    private final String PREF_SWITCH_BUTTONS = "switchButtons";
    @SuppressWarnings("FieldCanBeLocal")
    private final String PREF_DASHBOARD_LIST = "dashboardAsList";

    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @SuppressLint("CommitPrefEdits")
    public SharedPrefUtil(Context mContext) {
        this.mContext = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    public boolean darkThemeEnabled() {
        return prefs.getBoolean(PREF_DARK_THEME, false);
    }

    public boolean showDashboardAsList() {
        return prefs.getBoolean(PREF_DASHBOARD_LIST, true);
    }

    public boolean showSwitchesAsButtons() {
        return prefs.getBoolean(PREF_SWITCH_BUTTONS, false);
    }

    public boolean checkForUpdatesEnabled() {
        return prefs.getBoolean(PREF_CHECK_UPDATES, false);
    }

    public boolean IsWidgetsEnabled() {
        return prefs.getBoolean(PREF_WIDGET_ENABLED, false);
    }

    public void SetWidgetsEnabled(Boolean set) {
        editor.putBoolean(PREF_WIDGET_ENABLED, set).apply();
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

    public boolean isDashboardSortedLikeServer() {
        return prefs.getBoolean(PREF_SORT_LIKESERVER, true);
    }

    public boolean getAlwaysOn() {
        return prefs.getBoolean(PREF_ALWAYS_ON, false);
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
        if(!isScene)
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

    public void deleteSecurityWidget(int widgetID) {
        editor.remove("WIDGETSECURITY" + widgetID);
        editor.remove("SMALLWIDGETIDX" + widgetID);
        editor.remove("WIDGETSECURITYPIN" + widgetID);
        editor.remove("WIDGETSECURITYPINLAYOUT" + widgetID);
        editor.remove("WIDGETSECURITYVALUE" + widgetID);
        editor.commit();
    }

    public void setSecurityWidgetIDX(int widgetID, int idx, String value, String pin, int layout) {
        editor.putInt("WIDGETSECURITY" + widgetID, idx).apply();
        editor.putString("WIDGETSECURITYVALUE" + widgetID, value).apply();
        editor.putString("WIDGETSECURITYPIN" + widgetID, pin).apply();
        editor.putInt("WIDGETSECURITYPINLAYOUT" + widgetID, layout).apply();
        editor.commit();
    }

    public int getSecurityWidgetIDX(int widgetID) {
        return prefs.getInt("WIDGETSECURITY" + widgetID, INVALID_IDX);
    }

    public int getSecurityWidgetLayout(int widgetID) {
        return prefs.getInt("WIDGETSECURITYPINLAYOUT" + widgetID, -1);
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
        if(!isScene)
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
    public List<String> getLoggedNotifications() {
        if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS_LOG)) return null;

        Set<String> notifications = prefs.getStringSet(PREF_RECEIVED_NOTIFICATIONS_LOG, null);
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

    /**
     * Adds the notification to the list of received notifications (only unique ones are stored)
     *
     * @param notification Notification string to add
     */
    public void addLoggedNotification(String notification) {
        if (UsefulBits.isEmpty(notification))
            return;

        try {
            Set<String> notifications;
            if (!prefs.contains(PREF_RECEIVED_NOTIFICATIONS_LOG)) {
                notifications = new HashSet<>();
                notifications.add(notification);
                editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS_LOG, notifications).apply();
            } else {
                notifications = prefs.getStringSet(PREF_RECEIVED_NOTIFICATIONS_LOG, null);
                if (notifications == null)
                    notifications = new HashSet<>();
                notifications.add(notification);

                if (notifications.size() > 20) {
                    List<String> notificationsValues = new ArrayList<>();
                    for (String s : notifications)
                        notificationsValues.add(s);
                    Collections.sort(notificationsValues);
                    notificationsValues.remove(0);
                    Collections.reverse(notificationsValues);
                    editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS_LOG, new HashSet<String>(notificationsValues)).apply();
                } else
                    editor.putStringSet(PREF_RECEIVED_NOTIFICATIONS_LOG, notifications).apply();
            }
        } catch (Exception ex) {
            Log.e(TAG, "Failed to save received notification: " + ex.getMessage());
        }
    }

    public void removeWizard() {
        // 1 if start up screen is 0 (wizard) change to dashboard
        if (getStartupScreenIndex() == 0) setStartupScreenIndex(1);

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

    public int getStartupScreenIndex() {
        String startupScreenSelectedValue = prefs.getString(PREF_STARTUP_SCREEN, null);
        if (startupScreenSelectedValue == null) return 0;
        else {
            String[] startupScreenValues =
                    mContext.getResources().getStringArray(R.array.drawer_actions);
            int i = 0;

            for (String screen : startupScreenValues) {
                if (screen.equalsIgnoreCase(startupScreenSelectedValue)) {
                    return i;
                }
                i++;
            }

            //fix, could not find startup screen
            setStartupScreenIndex(0);
            return 0;
        }
    }

    public void setStartupScreenIndex(int position) {
        String[] startupScreenValues =
                mContext.getResources().getStringArray(R.array.drawer_actions);
        String startupScreenValue;

        try {
            startupScreenValue = startupScreenValues[position];
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            startupScreenValue = startupScreenValues[0];
        }

        editor.putString(PREF_STARTUP_SCREEN, startupScreenValue).apply();
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
        return prefs.getBoolean(PREF_DEBUGGING, false);
    }

    public boolean isAdvancedSettingsEnabled() {
        return prefs.getBoolean(PREF_ADVANCED_SETTINGS_ENABLED, false);
    }

    public void setAdvancedSettingsEnabled(boolean enabled) {
        editor.putBoolean(PREF_ADVANCED_SETTINGS_ENABLED, enabled).apply();
    }

    public boolean showExtraData() {
        return prefs.getBoolean(PREF_EXTRA_DATA, true);
    }

    public boolean showCustomWear() {
        return prefs.getBoolean(PREF_CUSTOM_WEAR, false);
    }

    public boolean showAutoNotifications() {
        return prefs.getBoolean(PREF_CUSTOM_AUTO, false);
    }

    public boolean isNFCEnabled() {
        return prefs.getBoolean(PREF_ENABLE_NFC, false);
    }

    public boolean isServerUpdateAvailable() {
        return prefs.getBoolean(PREF_UPDATE_SERVER_AVAILABLE, false);
    }

    public String getPreviousVersionNumber() {
        return prefs.getString(PREF_LAST_VERSION, "");
    }

    public void setVersionNumber(String version) {
        editor.putString(PREF_LAST_VERSION, version);
        editor.commit();
    }

    public String getLastUpdateShown() {
        return prefs.getString(PREF_UPDATE_SERVER_SHOWN, "");
    }

    public void setLastUpdateShown(String revisionNb) {
        editor.putString(PREF_UPDATE_SERVER_SHOWN, revisionNb);
        editor.commit();
    }

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

    public boolean isSpeechEnabled() {
        return prefs.getBoolean(PREF_SPEECH_ENABLED, false);
    }

    public void saveNFCList(List<NFCInfo> list) {
        Gson gson = new Gson();
        editor.putString(PREF_NFC_TAGS, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<NFCInfo> getNFCList() {
        ArrayList<NFCInfo> oReturnValue = new ArrayList<>();
        List<NFCInfo> nfcs;
        if (prefs.contains(PREF_NFC_TAGS)) {
            String jsonNFCs = prefs.getString(PREF_NFC_TAGS, null);
            Gson gson = new Gson();
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

    public void saveQRCodeList(List<QRCodeInfo> list) {
        Gson gson = new Gson();
        editor.putString(PREF_QR_CODES, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<QRCodeInfo> getQRCodeList() {
        ArrayList<QRCodeInfo> oReturnValue = new ArrayList<>();
        List<QRCodeInfo> qrs;
        if (prefs.contains(PREF_QR_CODES)) {
            String jsonNFCs = prefs.getString(PREF_QR_CODES, null);
            Gson gson = new Gson();
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
        Gson gson = new Gson();
        editor.putString(PREF_SPEECH_COMMANDS, gson.toJson(list));
        editor.commit();
    }

    public ArrayList<SpeechInfo> getSpeechList() {
        ArrayList<SpeechInfo> oReturnValue = new ArrayList<>();
        List<SpeechInfo> qrs;
        if (prefs.contains(PREF_SPEECH_COMMANDS)) {
            String jsonNFCs = prefs.getString(PREF_SPEECH_COMMANDS, null);
            Gson gson = new Gson();
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
        Gson gson = new Gson();
        String jsonLocations = gson.toJson(locations);
        editor.putString(PREF_GEOFENCE_LOCATIONS, jsonLocations);
        editor.commit();
    }

    public ArrayList<LocationInfo> getLocations() {
        List<LocationInfo> returnValue = new ArrayList<>();
        List<LocationInfo> locations;
        boolean incorrectDetected = false;

        if (prefs.contains(PREF_GEOFENCE_LOCATIONS)) {
            String jsonLocations = prefs.getString(PREF_GEOFENCE_LOCATIONS, null);
            Gson gson = new Gson();
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

        return (ArrayList<LocationInfo>) returnValue;
    }

    public LocationInfo getLocation(int id) {
        List<LocationInfo> locations = getLocations();
        for (LocationInfo l : locations) {
            if (l.getID() == id)
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

    public boolean saveSharedPreferencesToFile(File dst) {
        try {
            boolean isServerUpdateAvailableValue = false;
            ServerUpdateInfo mServerUpdateInfo = new ServerUtil(mContext).getActiveServer().getServerUpdateInfo(mContext);

            // Before saving to file set server update available preference to false
            if (isServerUpdateAvailable()) {
                isServerUpdateAvailableValue = true;
                mServerUpdateInfo.setUpdateAvailable(false);
            }

            Map<String, ?> oAllPrefs = this.prefs.getAll();
            HashMap<String, Object> oSavePrefs = new HashMap<String, Object>();
            for (Map.Entry<String, ?> entry : oAllPrefs.entrySet()) {
                //Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                if (entry.getKey().startsWith("WIDGET") || entry.getKey().startsWith("SMALLWIDGET"))
                    Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                else if (entry.getKey().equals("receivedNotifications") || entry.getKey().equals("receivedNotificationsLog"))
                    Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                else {
                    Log.i("PREFS", "Exported: " + entry.getKey() + ": " + entry.getValue().toString());
                    oSavePrefs.put(entry.getKey(), entry.getValue());
                }
            }

            boolean result = true;
            if (dst.exists())
                result = dst.delete();

            if (result) {
                ObjectOutputStream output = null;

                //noinspection TryWithIdenticalCatches
                try {
                    output = new ObjectOutputStream(new FileOutputStream(dst));
                    output.writeObject(oSavePrefs);
                    result = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (output != null) {
                            output.flush();
                            output.close();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            // Write original settings to preferences
            if (isServerUpdateAvailableValue) mServerUpdateInfo.setUpdateAvailable(true);
            return result;
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressWarnings({"UnnecessaryUnboxing", "unchecked"})
    public boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        //noinspection TryWithIdenticalCatches
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            //editor.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();
                if (v != null && !UsefulBits.isEmpty(key)) {
                    if (entry.getKey().startsWith("WIDGET") || entry.getKey().startsWith("SMALLWIDGET"))
                        Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                    else if (entry.getKey().equals("receivedNotifications") || entry.getKey().equals("receivedNotificationsLog"))
                        Log.i("PREFS", "Skipped: " + entry.getKey() + ": " + entry.getValue().toString());
                    else {
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
            Gson gson = new Gson();
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
                Gson gson = new Gson();
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
            new Domoticz(mContext, AppController.getInstance().getRequestQueue()).getLanguageStringsFromServer(langToDownload, new LanguageReceiver() {
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