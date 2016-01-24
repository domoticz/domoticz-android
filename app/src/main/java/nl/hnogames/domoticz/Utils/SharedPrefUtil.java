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

package nl.hnogames.domoticz.Utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticz.Containers.ConfigInfo;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Service.GeofenceTransitionsIntentService;

@SuppressWarnings("SpellCheckingInspection")
public class SharedPrefUtil {

    public static final String PREF_CUSTOM_WEAR = "enableWearItems";
    public static final String PREF_CUSTOM_WEAR_ITEMS = "wearItems";
    public static final String PREF_ALWAYS_ON = "alwayson";
    public static final String PREF_NOTIFICATION_ID = "notification_id";
    public static final String PREF_NOTIFICATION_VIBRATE = "notification_vibrate";
    public static final String PREF_NOTIFICATION_SOUND = "notification_sound";
    public static final String PREF_LANGUAGE = "displayLanguage";
    public static final String PREF_UPDATE_VERSION = "updateversion";
    public static final String PREF_UPDATE_SERVER_AVAILABLE = "updateserveravailable";
    public static final String PREF_SERVER_VERSION = "serverversion";
    public static final String PREF_EXTRA_DATA = "extradata";
    public static final String PREF_STARTUP_SCREEN = "startup_screen";
    public static final String PREF_NAVIGATION_ITEMS = "enable_menu_items";
    public static final String PREF_GEOFENCE_LOCATIONS = "geofence_locations";
    public static final String PREF_CONFIG = "domoticz_config";
    public static final String PREF_GEOFENCE_ENABLED = "geofence_enabled";
    public static final String PREF_GEOFENCE_STARTED = "geofence_started";
    public static final String PREF_ADVANCED_SETTINGS_ENABLED = "advanced_settings_enabled";
    public static final String PREF_GEOFENCE_NOTIFICATIONS_ENABLED = "geofence_notifications_enabled";
    public static final String PREF_DEBUGGING = "debugging";
    public static final int INVALID_IDX = 999999;
    private static final String PREF_FIRST_START = "isFirstStart";
    private static final String PREF_WELCOME_SUCCESS = "welcomeSuccess";
    private static final String REMOTE_SERVER_USERNAME = "remote_server_username";
    private static final String REMOTE_SERVER_PASSWORD = "remote_server_password";
    private static final String REMOTE_SERVER_URL = "remote_server_url";
    private static final String REMOTE_SERVER_PORT = "remote_server_port";
    private static final String REMOTE_SERVER_DIRECTORY = "remote_server_directory";
    private static final String REMOTE_SERVER_SECURE = "remote_server_secure";
    private static final String REMOTE_SERVER_AUTHENTICATION_METHOD =
            "remote_server_authentication_method";
    private static final String IS_LOCAL_SERVER_ADDRESS_DIFFERENT = "local_server_different_address";
    private static final String LOCAL_SERVER_USERNAME = "local_server_username";
    private static final String LOCAL_SERVER_PASSWORD = "local_server_password";
    private static final String LOCAL_SERVER_URL = "local_server_url";
    private static final String LOCAL_SERVER_PORT = "local_server_port";
    private static final String LOCAL_SERVER_DIRECTORY = "local_server_directory";
    private static final String LOCAL_SERVER_SECURE = "local_server_secure";
    private static final String LOCAL_SERVER_AUTHENTICATION_METHOD =
            "local_server_authentication_method";
    private static final String LOCAL_SERVER_SSID = "local_server_ssid";

    private Context mContext;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private GoogleApiClient mApiClient = null;

    public SharedPrefUtil(Context mContext) {
        this.mContext = mContext;
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();
    }

    public void completeCard(String cardTag) {
        editor.putBoolean("CARD" + cardTag, true).apply();
    }

    public boolean getAwaysOn() {
        return prefs.getBoolean(PREF_ALWAYS_ON, false);
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

    public void setWidgetIDX(int widgetID, int idx, boolean isScene, String password) {
        editor.putInt("WIDGET" + widgetID, idx).apply();
        editor.putBoolean("WIDGETSCENE" + widgetID, isScene).apply();
        editor.putString("WIDGETPASSWORD" + widgetID, password).apply();
        editor.commit();
    }

    public int getWidgetIDX(int widgetID) {
        return prefs.getInt("WIDGET" + widgetID, INVALID_IDX);
    }

    public String getWidgetPassword(int widgetID) {
        return prefs.getString("WIDGETPASSWORD" + widgetID, null);
    }

    public boolean getWidgetisScene(int widgetID) {
        return prefs.getBoolean("WIDGETSCENE" + widgetID, false);
    }

    public void setWidgetIDforIDX(int widgetID, int idx) {
        editor.putInt("WIDGETIDX" + idx, widgetID).apply();
    }

    public int getWidgetIDforIDX(int idx) {
        return prefs.getInt("WIDGETIDX" + idx, INVALID_IDX);
    }

    /*
     *      Generic settings
     */
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

    public String getNotificationRegistrationID() {
        return prefs.getString(PREF_NOTIFICATION_ID, "");
    }

    public void setNotificationRegistrationID(String id) {
        editor.putString(PREF_NOTIFICATION_ID, id).apply();
    }

    public boolean getNotificationVibrate() {
        return prefs.getBoolean(PREF_NOTIFICATION_VIBRATE, true);
    }

    public String getNotificationSound() {
        return prefs.getString(PREF_NOTIFICATION_SOUND, null);
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

        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections == null) //default
            return allNames;
        else {
            String[] selectionValues = new String[selections.size()];
            int i = 0;
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
    }

    public void setNavigationDefaults() {
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);
        Set<String> selections = new HashSet<>(Arrays.asList(allNames));
        editor.putStringSet(PREF_NAVIGATION_ITEMS, selections).apply();
    }

    public int[] getNavigationIcons() {
        if (!prefs.contains(PREF_NAVIGATION_ITEMS)) setNavigationDefaults();

        TypedArray icons = mContext.getResources().obtainTypedArray(R.array.drawer_icons);
        Set<String> selections = prefs.getStringSet(PREF_NAVIGATION_ITEMS, null);
        String[] allNames = mContext.getResources().getStringArray(R.array.drawer_actions);

        if (selections != null) {

            int[] selectedICONS = new int[selections.size()];
            int iconIndex = 0;
            int index = 0;
            for (String v : allNames) {
                for (String s : selections) {
                    if (s.equals(v)) {
                        selectedICONS[iconIndex] = icons.getResourceId(index, 0);
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

    /*
     *      Remote server settings
     */
    public String getDomoticzRemoteUsername() {
        return prefs.getString(REMOTE_SERVER_USERNAME, "");
    }

    public void setDomoticzRemoteUsername(String username) {
        editor.putString(REMOTE_SERVER_USERNAME, username).apply();
    }

    public String getDomoticzRemotePassword() {
        return prefs.getString(REMOTE_SERVER_PASSWORD, "");
    }

    public void setDomoticzRemotePassword(String password) {
        editor.putString(REMOTE_SERVER_PASSWORD, password).apply();
    }

    public boolean isServerUpdateAvailable() {
        return prefs.getBoolean(PREF_UPDATE_SERVER_AVAILABLE, false);
    }

    public void setServerUpdateAvailable(boolean haveUpdate) {
        editor.putBoolean(PREF_UPDATE_SERVER_AVAILABLE, haveUpdate).apply();
    }

    public String getUpdateVersionAvailable() {
        return prefs.getString(PREF_UPDATE_VERSION, "");
    }

    public void setUpdateVersionAvailable(String version) {
        editor.putString(PREF_UPDATE_VERSION, version).apply();
    }

    public void setServerVersion(String version) {
        editor.putString(PREF_SERVER_VERSION, version).apply();
    }

    public String getServerVersion() {
        return prefs.getString(PREF_SERVER_VERSION, "");
    }

    public String getDomoticzRemoteUrl() {
        return prefs.getString(REMOTE_SERVER_URL, "");
    }

    public void setDomoticzRemoteUrl(String url) {
        editor.putString(REMOTE_SERVER_URL, url).apply();
    }

    public String getDomoticzRemotePort() {
        return prefs.getString(REMOTE_SERVER_PORT, "");
    }

    public void setDomoticzRemotePort(String port) {
        editor.putString(REMOTE_SERVER_PORT, port).apply();
    }

    public String getDomoticzRemoteDirectory() {
        return prefs.getString(REMOTE_SERVER_DIRECTORY, "");
    }

    public void setDomoticzRemoteDirectory(String directory) {
        editor.putString(REMOTE_SERVER_DIRECTORY, directory).apply();
    }

    public boolean isDomoticzRemoteSecure() {
        return prefs.getBoolean(REMOTE_SERVER_SECURE, true);
    }

    public void setDomoticzRemoteSecure(boolean secure) {
        editor.putBoolean(REMOTE_SERVER_SECURE, secure).apply();
    }

    public String getDomoticzRemoteAuthenticationMethod() {
        boolean remoteServerAuthenticationMethodIsLoginForm =
                prefs.getBoolean(REMOTE_SERVER_AUTHENTICATION_METHOD, true);
        String method;

        if (remoteServerAuthenticationMethodIsLoginForm)
            method = Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM;
        else method = Domoticz.Authentication.Method.AUTH_METHOD_BASIC_AUTHENTICATION;

        return method;
    }

    /*
     *      Local server settings
     */
    public boolean isLocalServerAddressDifferent() {
        return prefs.getBoolean(IS_LOCAL_SERVER_ADDRESS_DIFFERENT, false);
    }

    public void setLocalServerUsesSameAddress(boolean b) {
        editor.putBoolean(IS_LOCAL_SERVER_ADDRESS_DIFFERENT, b).apply();
    }

    public String getDomoticzLocalUsername() {
        return prefs.getString(LOCAL_SERVER_USERNAME, "");
    }

    public void setDomoticzLocalUsername(String username) {
        editor.putString(LOCAL_SERVER_USERNAME, username).apply();
    }

    public String getDomoticzLocalPassword() {
        return prefs.getString(LOCAL_SERVER_PASSWORD, "");
    }

    public void setDomoticzLocalPassword(String password) {
        editor.putString(LOCAL_SERVER_PASSWORD, password).apply();
    }

    public String getDomoticzLocalUrl() {
        return prefs.getString(LOCAL_SERVER_URL, "");
    }

    public void setDomoticzLocalUrl(String url) {
        editor.putString(LOCAL_SERVER_URL, url).apply();
    }

    public String getDomoticzLocalPort() {
        return prefs.getString(LOCAL_SERVER_PORT, "");
    }

    public void setDomoticzLocalPort(String port) {
        editor.putString(LOCAL_SERVER_PORT, port).apply();
    }

    public String getDomoticzLocalDirectory() {
        return prefs.getString(LOCAL_SERVER_DIRECTORY, "");
    }

    public void setDomoticzLocalDirectory(String directory) {
        editor.putString(LOCAL_SERVER_DIRECTORY, directory).apply();
    }

    public boolean isDomoticzLocalSecure() {
        return prefs.getBoolean(LOCAL_SERVER_SECURE, true);
    }

    public void setDomoticzLocalSecure(boolean secure) {
        editor.putBoolean(LOCAL_SERVER_SECURE, secure).apply();
    }

    public boolean isGeofenceEnabled() {
        return prefs.getBoolean(PREF_GEOFENCE_ENABLED, false);
    }

    public void setGeofenceEnabled(boolean enabled) {
        editor.putBoolean(PREF_GEOFENCE_ENABLED, enabled).apply();
    }

    public boolean isGeofenceNotificationsEnabled() {
        return prefs.getBoolean(PREF_GEOFENCE_NOTIFICATIONS_ENABLED, false);
    }

    public void setGeofenceNotificationsEnabled(boolean enabled) {
        editor.putBoolean(PREF_GEOFENCE_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    @SuppressWarnings("unused")
    public String getDomoticzLocalAuthenticationMethod() {
        boolean localServerAuthenticationMethodIsLoginForm =
                prefs.getBoolean(LOCAL_SERVER_AUTHENTICATION_METHOD, true);
        String method;

        if (localServerAuthenticationMethodIsLoginForm)
            method = Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM;
        else method = Domoticz.Authentication.Method.AUTH_METHOD_BASIC_AUTHENTICATION;

        return method;
    }

    public void setDomoticzLocalAuthenticationMethod(String method) {
        boolean methodIsLoginForm;
        methodIsLoginForm =
                method.equalsIgnoreCase(Domoticz.Authentication.Method.AUTH_METHOD_LOGIN_FORM);
        editor.putBoolean(LOCAL_SERVER_AUTHENTICATION_METHOD, methodIsLoginForm).apply();
    }

    public Set<String> getLocalSsid() {
        return prefs.getStringSet(LOCAL_SERVER_SSID, null);
    }

    public void setLocalSsid(List<String> ssids) {
        if (ssids != null) {
            Set<String> set = new HashSet<>();
            for (String ssid : ssids) {
                set.add(ssid);
            }
            editor.putStringSet(LOCAL_SERVER_SSID, set).apply();
        }
    }

    /**
     * Method for setting local server addresses the same as the remote server addresses
     */
    public void setLocalSameAddressAsRemote() {
        setDomoticzLocalUsername(getDomoticzRemoteUsername());
        setDomoticzLocalPassword(getDomoticzRemotePassword());
        setDomoticzLocalUrl(getDomoticzRemoteUrl());
        setDomoticzLocalPort(getDomoticzRemotePort());
        setDomoticzLocalDirectory(getDomoticzRemoteDirectory());
        setDomoticzLocalSecure(isDomoticzRemoteSecure());
        setDomoticzLocalAuthenticationMethod(getDomoticzRemoteAuthenticationMethod());
    }

    public void saveConfig(ConfigInfo config) {
        editor.putString(PREF_CONFIG, config.getJsonObject());
        editor.commit();
    }

    public ConfigInfo getConfig() {
        ConfigInfo config;
        if (prefs.contains(PREF_CONFIG)) {
            String jsonConfig = prefs.getString(PREF_CONFIG, null);
            config = new ConfigInfo(jsonConfig);
        } else
            return null;

        return config;
    }

    // This four methods are used for maintaining locations.
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
        boolean isServerUpdateAvailableValue = false;

        // Before saving to file set server update available preference to false
        if (isServerUpdateAvailable()) {
            isServerUpdateAvailableValue = true;
            setServerUpdateAvailable(false);
        }

        boolean result = false;

        if (dst.exists()) result = dst.delete();

        if (result) {
            ObjectOutputStream output = null;

            //noinspection TryWithIdenticalCatches
            try {
                output = new ObjectOutputStream(new FileOutputStream(dst));
                output.writeObject(this.prefs.getAll());
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
        if (isServerUpdateAvailableValue) setServerUpdateAvailable(true);
        return result;
    }

    @SuppressWarnings({"UnnecessaryUnboxing", "unchecked"})
    public boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        //noinspection TryWithIdenticalCatches
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            editor.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

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
                    Log.v("Settings", "Could not load pref: " + key + " | " + v.getClass());
            }
            editor.commit();
            res = true;

            setGeoFenceService();
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

    public String getLanguage() {
        return prefs.getString(PREF_LANGUAGE, "");
    }

    public boolean isGeofencingStarted() {
        return prefs.getBoolean(PREF_GEOFENCE_STARTED, false);
    }

    public void setGeofencingStarted(boolean started) {
        editor.putBoolean(PREF_GEOFENCE_STARTED, started).apply();
    }

    public void setGeoFenceService() {

        if (isGeofenceEnabled()) {
            final List<Geofence> mGeofenceList = new ArrayList<>();
            final ArrayList<LocationInfo> locations = getLocations();
            if (locations != null)
                for (LocationInfo locationInfo : locations)
                    if (locationInfo.getEnabled())
                        mGeofenceList.add(locationInfo.toGeofence());

            if (locations != null && mGeofenceList.size() > 0) {
                mApiClient = new GoogleApiClient.Builder(mContext)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                PendingIntent mGeofenceRequestIntent =
                                        getGeofenceTransitionPendingIntent();

                                // First remove all GeoFences
                                try {
                                    LocationServices.GeofencingApi.removeGeofences(mApiClient,
                                            mGeofenceRequestIntent);
                                } catch (Exception ignored) {
                                }

                                //noinspection ResourceType
                                LocationServices
                                        .GeofencingApi
                                        .addGeofences(mApiClient,
                                                mGeofenceList,
                                                mGeofenceRequestIntent);
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        })
                        .build();
                mApiClient.connect();
            }
        }
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    public PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}