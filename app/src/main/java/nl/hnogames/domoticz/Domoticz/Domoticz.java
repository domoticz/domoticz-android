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

package nl.hnogames.domoticz.Domoticz;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticz.Containers.CameraInfo;
import nl.hnogames.domoticz.Containers.ServerInfo;
import nl.hnogames.domoticz.Interfaces.CameraReceiver;
import nl.hnogames.domoticz.Interfaces.ConfigReceiver;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.EventReceiver;
import nl.hnogames.domoticz.Interfaces.EventXmlReceiver;
import nl.hnogames.domoticz.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticz.Interfaces.LogsReceiver;
import nl.hnogames.domoticz.Interfaces.MobileDeviceReceiver;
import nl.hnogames.domoticz.Interfaces.NotificationReceiver;
import nl.hnogames.domoticz.Interfaces.PlansReceiver;
import nl.hnogames.domoticz.Interfaces.ScenesReceiver;
import nl.hnogames.domoticz.Interfaces.SettingsReceiver;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.TemperatureReceiver;
import nl.hnogames.domoticz.Interfaces.UpdateDomoticzServerReceiver;
import nl.hnogames.domoticz.Interfaces.UpdateDownloadReadyReceiver;
import nl.hnogames.domoticz.Interfaces.UpdateVersionReceiver;
import nl.hnogames.domoticz.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticz.Interfaces.UtilitiesReceiver;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.Interfaces.WeatherReceiver;
import nl.hnogames.domoticz.Interfaces.WifiSSIDListener;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PhoneConnectionUtil;
import nl.hnogames.domoticz.Utils.RequestUtil;
import nl.hnogames.domoticz.Utils.ServerUtil;
import nl.hnogames.domoticz.Utils.SessionUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.VolleyUtil;


public class Domoticz {

    public static final int batteryLevelMax = 100;
    public static final int signalLevelMax = 12;
    public static final int DOMOTICZ_FAKE_ID = 99999;
    public static final String HIDDEN_CHARACTER = "$";

    public static final String UTILITIES_TYPE_THERMOSTAT = "Thermostat";
    public static final String UTILITIES_TYPE_HEATING = "Heating";
    public static final String UTILITIES_SUBTYPE_TEXT = "Text";
    public static final String DOMOTICZ_DEFAULT_SERVER = "DEFAULT";

    /*
    *  Log tag
    */
    private static final String TAG = Domoticz.class.getSimpleName();
    public static boolean debug;
    private final SharedPrefUtil mSharedPrefUtil;
    private final ServerUtil mServerUtil;
    private final SessionUtil mSessionUtil;
    private final PhoneConnectionUtil mPhoneConnectionUtil;
    private Context mContext;

    @SuppressWarnings("FieldCanBeLocal")
    private String snapshot_file_path = "/Domoticz/SnapShot";


    public Domoticz(Context mContext) {
        this.mContext = mContext;

        mSharedPrefUtil = new SharedPrefUtil(mContext);
        mServerUtil = new ServerUtil(mContext);

        mSessionUtil = new SessionUtil(mContext);
        mPhoneConnectionUtil = new PhoneConnectionUtil(mContext, new WifiSSIDListener() {
            @Override
            public void ReceiveSSIDs(CharSequence[] entries) {
            }
        });
        debug = mSharedPrefUtil.isDebugEnabled();
    }

    public boolean isDebugEnabled() {
        return mSharedPrefUtil.isDebugEnabled();
    }

    public void logger(String text) {
        if (debug) Log.d(TAG, text);
    }

    public boolean isUserOnLocalWifi() {
        boolean userIsLocal = false;
        if (mServerUtil.getActiveServer().getIsLocalServerAddressDifferent()) {
            Set<String> localSsid = mServerUtil.getActiveServer().getLocalServerSsid();

            if (mPhoneConnectionUtil.isWifiConnected() && localSsid != null && localSsid.size() > 0) {
                String currentSsid = mPhoneConnectionUtil.getCurrentSsid();
                if (!UsefulBits.isEmpty(currentSsid)) {
                    // Remove quotes from current SSID read out
                    currentSsid = currentSsid.substring(1, currentSsid.length() - 1);
                    for (String ssid : localSsid) {
                        if (ssid.equals(currentSsid)) userIsLocal = true;
                    }
                }
            }
        }

        return userIsLocal;
    }

    public boolean isConnectionDataComplete(ServerInfo server) {
        boolean result = true;
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        stringHashMap.put("Domoticz local port", server.getLocalServerPort());
        stringHashMap.put("Domoticz remote URL", server.getRemoteServerUrl());
        stringHashMap.put("Domoticz remote port", server.getRemoteServerPort());

        for (Map.Entry<String, String> entry : stringHashMap.entrySet()) {

            if (UsefulBits.isEmpty(entry.getValue())) {
                logger(entry.getKey() + " is empty");
                result = false;
                break;
            }

        }
        logger("isConnectionDataComplete = " + result);
        return result;
    }

    public String isConnectionDataComplete(ServerInfo server, boolean validatePorts) {
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        stringHashMap.put("Domoticz remote URL", server.getRemoteServerUrl());

        if(validatePorts) {
            stringHashMap.put("Domoticz local port", server.getLocalServerPort());
            stringHashMap.put("Domoticz remote port", server.getRemoteServerPort());
        }
        for (Map.Entry<String, String> entry : stringHashMap.entrySet()) {

            if (UsefulBits.isEmpty(entry.getValue())) {
                return (entry.getKey() + " is empty");
            }

        }
        return null;
    }

    public boolean isUrlValid(ServerInfo server) {
        boolean result = true;
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        stringHashMap.put("Domoticz remote URL", server.getRemoteServerUrl());

        for (Map.Entry<String, String> entry : stringHashMap.entrySet()) {

            if (entry.getValue().toLowerCase().startsWith("http")) {
                logger(entry.getKey() + " starts with http");
                result = false;
                break;
            }

        }
        return result;
    }

    public String getErrorMessage(Exception error) {

        String errorMessage;

        if (error instanceof VolleyError) {

            VolleyUtil mVolleyUtil = new VolleyUtil(mContext);
            VolleyError volleyError = (VolleyError) error;

            errorMessage = mVolleyUtil.getVolleyErrorMessage(volleyError);
        } else {
            errorMessage = error.getMessage();
        }

        return errorMessage;
    }

    public List<Integer> getSupportedSwitchesValues() {
        List<Integer> switchesSupported = new ArrayList<>();
        switchesSupported.add(Device.Type.Value.ON_OFF);
        switchesSupported.add(Device.Type.Value.DIMMER);
        switchesSupported.add(Device.Type.Value.BLINDPERCENTAGE);
        switchesSupported.add(Device.Type.Value.BLINDVENETIAN);
        switchesSupported.add(Device.Type.Value.BLINDINVERTED);
        switchesSupported.add(Device.Type.Value.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(Device.Type.Value.BLINDS);
        switchesSupported.add(Device.Type.Value.MOTION);
        switchesSupported.add(Device.Type.Value.CONTACT);
        switchesSupported.add(Device.Type.Value.PUSH_ON_BUTTON);
        switchesSupported.add(Device.Type.Value.PUSH_OFF_BUTTON);
        switchesSupported.add(Device.Type.Value.MEDIAPLAYER);
        switchesSupported.add(Device.Type.Value.SMOKE_DETECTOR);
        switchesSupported.add(Device.Type.Value.X10SIREN);
        switchesSupported.add(Device.Type.Value.DUSKSENSOR);
        switchesSupported.add(Device.Type.Value.DOORLOCK);
        switchesSupported.add(Device.Type.Value.DOORBELL);
        switchesSupported.add(Device.Type.Value.SECURITY);
        switchesSupported.add(Device.Type.Value.SELECTOR);
        return switchesSupported;
    }

    public List<String> getSupportedSwitchesNames() {
        List<String> switchesSupported = new ArrayList<>();
        switchesSupported.add(Device.Type.Name.ON_OFF);
        switchesSupported.add(Device.Type.Name.DIMMER);
        switchesSupported.add(Device.Type.Name.BLINDPERCENTAGE);
        switchesSupported.add(Device.Type.Name.BLINDVENETIAN);
        switchesSupported.add(Device.Type.Name.BLINDINVERTED);
        switchesSupported.add(Device.Type.Name.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(Device.Type.Name.BLINDS);
        switchesSupported.add(Device.Type.Name.PUSH_ON_BUTTON);
        switchesSupported.add(Device.Type.Name.PUSH_OFF_BUTTON);
        switchesSupported.add(Device.Type.Name.CONTACT);
        switchesSupported.add(Device.Type.Name.MOTION);
        switchesSupported.add(Device.Type.Name.MEDIAPLAYER);
        switchesSupported.add(Device.Type.Name.SMOKE_DETECTOR);
        switchesSupported.add(Device.Type.Name.X10SIREN);
        switchesSupported.add(Device.Type.Name.DUSKSENSOR);
        switchesSupported.add(Device.Type.Name.DOORLOCK);
        switchesSupported.add(Device.Type.Name.DOORBELL);
        switchesSupported.add(Device.Type.Name.SECURITY);
        switchesSupported.add(Device.Type.Name.SELECTOR);
        switchesSupported.add(Device.Type.Name.EVOHOME);
        return switchesSupported;
    }

    public List<Integer> getWearSupportedSwitchesValues() {
        List<Integer> switchesSupported = new ArrayList<>();
        switchesSupported.add(Device.Type.Value.ON_OFF);
        switchesSupported.add(Device.Type.Value.PUSH_ON_BUTTON);
        switchesSupported.add(Device.Type.Value.DIMMER);
        switchesSupported.add(Device.Type.Value.PUSH_OFF_BUTTON);
        switchesSupported.add(Device.Type.Value.MEDIAPLAYER);
        switchesSupported.add(Device.Type.Value.SMOKE_DETECTOR);
        switchesSupported.add(Device.Type.Value.X10SIREN);
        switchesSupported.add(Device.Type.Value.DOORLOCK);
        switchesSupported.add(Device.Type.Value.DOORBELL);
        return switchesSupported;
    }

    public List<String> getWearSupportedSwitchesNames() {
        List<String> switchesSupported = new ArrayList<>();
        switchesSupported.add(Device.Type.Name.ON_OFF);
        switchesSupported.add(Device.Type.Name.PUSH_ON_BUTTON);
        switchesSupported.add(Device.Type.Name.PUSH_OFF_BUTTON);
        switchesSupported.add(Device.Type.Name.DIMMER);
        switchesSupported.add(Device.Type.Name.MEDIAPLAYER);
        switchesSupported.add(Device.Type.Name.SMOKE_DETECTOR);
        switchesSupported.add(Device.Type.Name.X10SIREN);
        switchesSupported.add(Device.Type.Name.DOORLOCK);
        switchesSupported.add(Device.Type.Name.DOORBELL);
        return switchesSupported;
    }

    public void debugTextToClipboard(TextView debugText) {
        String message = debugText.getText().toString();
        UsefulBits.copyToClipboard(mContext, "Domoticz debug data", message);
        Toast.makeText(mContext, R.string.msg_copiedToClipboard, Toast.LENGTH_SHORT).show();
    }

    private String getJsonGetUrl(int jsonGetUrl) {
        String url;

        switch (jsonGetUrl) {
            case Json.Url.Request.UPDATE_DOMOTICZ_SERVER:
                url = Url.System.UPDATE_DOMOTICZ_SERVER;
                break;
            case Json.Url.Request.UPDATE_DOWNLOAD_READY:
                url = Url.System.DOWNLOAD_READY;
                break;
            case Json.Url.Request.VERSION:
                url = Url.Category.VERSION;
                break;
            case Json.Url.Request.LOG:
                url = Url.Log.GET_LOG;
                break;

            case Json.Url.Request.DASHBOARD:
                url = Url.Category.DASHBOARD;
                break;

            case Json.Url.Request.SCENES:
                url = Url.Category.SCENES;
                break;

            case Json.Url.Request.SWITCHES:
                url = Url.Category.SWITCHES;
                break;

            case Json.Url.Request.UTILITIES:
                url = Url.Category.UTILITIES;
                break;

            case Json.Url.Request.TEMPERATURE:
                url = Url.Category.TEMPERATURE;
                break;

            case Json.Url.Request.WEATHER:
                url = Url.Category.WEATHER;
                break;

            case Json.Url.Request.CAMERAS:
                url = Url.Category.CAMERAS;
                break;

            case Json.Url.Request.CAMERA:
                url = Url.Category.CAMERA;
                break;

            case Json.Url.Request.DEVICES:
                url = Url.Category.DEVICES;
                break;

            case Json.Get.STATUS:
                url = Url.Device.STATUS;
                break;

            case Json.Url.Request.PLANS:
                url = Url.Category.PLANS;
                break;

            case Json.Url.Request.SWITCHLOG:
                url = Url.Category.SWITCHLOG;
                break;

            case Json.Url.Request.TEXTLOG:
                url = Url.Category.TEXTLOG;
                break;

            case Json.Url.Request.SWITCHTIMER:
                url = Url.Category.SWITCHTIMER;
                break;

            case Json.Url.Request.SETSECURITY:
                url = Url.System.SETSECURITY;
                break;

            case Json.Url.Request.UPDATE:
                url = Url.System.UPDATE;
                break;

            case Json.Url.Request.USERVARIABLES:
                url = Url.System.USERVARIABLES;
                break;

            case Json.Url.Request.EVENTS:
                url = Url.System.EVENTS;
                break;

            case Json.Url.Request.EVENTXML:
                url = Url.System.EVENTXML;
                break;

            case Json.Url.Request.SETTINGS:
                url = Url.System.SETTINGS;
                break;

            case Json.Url.Request.CONFIG:
                url = Url.System.CONFIG;
                break;

            case Json.Url.Request.GRAPH:
                url = Url.Log.GRAPH;
                break;

            case Json.Url.Request.ADD_MOBILE_DEVICE:
                url = Url.System.ADD_MOBILE_DEVICE;
                break;

            case Json.Url.Request.CLEAN_MOBILE_DEVICE:
                url = Url.System.CLEAN_MOBILE_DEVICE;
                break;

            case Json.Url.Request.SET_DEVICE_USED:
                url = Url.Device.SET_USED;
                break;

            case Json.Url.Request.NOTIFICATIONS:
                url = Url.Notification.NOTIFICATION;
                break;

            default:
                throw new NullPointerException("getJsonGetUrl: No known JSON URL specified");
        }
        return url;
    }

    private String constructGetUrl(int jsonGetUrl) {
        String protocol, url, port, directory, jsonUrl;
        StringBuilder buildUrl = new StringBuilder();

        if (isUserOnLocalWifi()) {
            if (mServerUtil.getActiveServer().getLocalServerSecure()) protocol = Url.Protocol.HTTPS;
            else protocol = Url.Protocol.HTTP;

            url = mServerUtil.getActiveServer().getLocalServerUrl();
            port = mServerUtil.getActiveServer().getLocalServerPort();
            directory = mServerUtil.getActiveServer().getLocalServerDirectory();

        } else {
            if (mServerUtil.getActiveServer().getRemoteServerSecure())
                protocol = Url.Protocol.HTTPS;
            else protocol = Url.Protocol.HTTP;

            url = mServerUtil.getActiveServer().getRemoteServerUrl();
            port = mServerUtil.getActiveServer().getRemoteServerPort();
            directory = mServerUtil.getActiveServer().getRemoteServerDirectory();

        }
        jsonUrl = getJsonGetUrl(jsonGetUrl);

        String fullString = buildUrl.append(protocol)
                .append(url).append(":")
                .append(port)
                .append(directory.isEmpty() ? "" : "/" + directory)
                .append(jsonUrl).toString();

        logger("Constructed url: " + fullString);

        return fullString;
    }

    public String constructSetUrl(int jsonSetUrl, int idx, int action, double value) {
        String protocol, baseUrl, url, port, directory, jsonUrl = null, actionUrl;
        StringBuilder buildUrl = new StringBuilder();

        if (isUserOnLocalWifi()) {
            if (mServerUtil.getActiveServer().getLocalServerSecure()) {
                protocol = Url.Protocol.HTTPS;
            } else protocol = Url.Protocol.HTTP;

            baseUrl = mServerUtil.getActiveServer().getLocalServerUrl();
            port = mServerUtil.getActiveServer().getLocalServerPort();
            directory = mServerUtil.getActiveServer().getLocalServerDirectory();
        } else {
            if (mServerUtil.getActiveServer().getRemoteServerSecure()) {
                protocol = Url.Protocol.HTTPS;
            } else protocol = Url.Protocol.HTTP;
            baseUrl = mServerUtil.getActiveServer().getRemoteServerUrl();
            port = mServerUtil.getActiveServer().getRemoteServerPort();
            directory = mServerUtil.getActiveServer().getRemoteServerDirectory();
        }

        switch (action) {
            case Scene.Action.ON:
                actionUrl = Url.Action.ON;
                break;

            case Scene.Action.OFF:
                actionUrl = Url.Action.OFF;
                break;

            case Device.Switch.Action.ON:
                actionUrl = Url.Action.ON;
                break;

            case Device.Switch.Action.OFF:
                actionUrl = Url.Action.OFF;
                break;

            case Device.Blind.Action.UP:
                actionUrl = Url.Action.UP;
                break;

            case Device.Blind.Action.STOP:
                actionUrl = Url.Action.STOP;
                break;

            case Device.Blind.Action.DOWN:
                actionUrl = Url.Action.DOWN;
                break;

            case Device.Blind.Action.ON:
                actionUrl = Url.Action.ON;
                break;

            case Device.Blind.Action.OFF:
                actionUrl = Url.Action.OFF;
                break;

            case Device.Thermostat.Action.MIN:
                actionUrl = String.valueOf(value);
                break;

            case Device.Thermostat.Action.PLUS:
                actionUrl = String.valueOf(value);
                break;

            case Device.Favorite.ON:
                actionUrl = FavoriteAction.ON;
                break;

            case Device.Favorite.OFF:
                actionUrl = FavoriteAction.OFF;
                break;

            case Event.Action.OFF:
            case Event.Action.ON:
                actionUrl = Url.System.EVENTSTATUS + String.valueOf(value);
                break;

            case Device.Dimmer.Action.DIM_LEVEL:
                actionUrl = Url.Switch.DIM_LEVEL + String.valueOf(value);
                break;

            case Device.Dimmer.Action.COLOR:
                actionUrl = Url.Switch.COLOR;
                break;

            case Device.ModalSwitch.Action.AUTO:
                actionUrl = Url.ModalAction.AUTO;
                break;

            case Device.ModalSwitch.Action.ECONOMY:
                actionUrl = Url.ModalAction.ECONOMY;
                break;

            case Device.ModalSwitch.Action.AWAY:
                actionUrl = Url.ModalAction.AWAY;
                break;

            case Device.ModalSwitch.Action.DAY_OFF:
                actionUrl = Url.ModalAction.DAY_OFF;
                break;

            case Device.ModalSwitch.Action.CUSTOM:
                actionUrl = Url.ModalAction.CUSTOM;
                break;

            case Device.ModalSwitch.Action.HEATING_OFF:
                actionUrl = Url.ModalAction.HEATING_OFF;
                break;

            default:
                throw new NullPointerException(
                        "Action not found in method Domoticz.constructSetUrl");
        }

        switch (jsonSetUrl) {
            case Json.Url.Set.SCENES:
                url = Url.Scene.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + Url.Switch.CMD + actionUrl;
                break;

            case Json.Url.Set.SWITCHES:
                url = Url.Switch.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + Url.Switch.CMD + actionUrl;
                break;

            case Json.Url.Set.MODAL_SWITCHES:
                url = Url.ModalSwitch.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + Url.ModalSwitch.STATUS + actionUrl;
                break;
            case Json.Url.Set.TEMP:
                url = Url.Temp.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + Url.Temp.VALUE + actionUrl;
                break;

            case Json.Url.Set.SCENEFAVORITE:
                url = Url.Favorite.SCENE;
                jsonUrl = url
                        + String.valueOf(idx)
                        + Url.Favorite.VALUE + actionUrl;
                break;

            case Json.Url.Set.FAVORITE:
                url = Url.Favorite.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + Url.Favorite.VALUE + actionUrl;
                break;

            case Json.Url.Set.EVENT:
                url = Url.System.EVENTACTION;
                jsonUrl = url
                        + String.valueOf(idx)
                        + actionUrl
                        + "&xml=";
                break;

            case Json.Url.Set.RGBCOLOR:
                url = Url.System.RGBCOLOR;
                jsonUrl = url
                        + String.valueOf(idx)
                        + actionUrl;
                break;
        }

        String fullString = buildUrl.append(protocol)
                .append(baseUrl).append(":")
                .append(port)
                .append(directory.isEmpty() ? "" : "/" + directory)
                .append(jsonUrl).toString();
        logger("Constructed url: " + fullString);

        return fullString;
    }

    public String getUserCredentials(String credential) {

        if (credential.equals(Authentication.USERNAME)
                || credential.equals(Authentication.PASSWORD)) {

            String username, password;

            if (isUserOnLocalWifi()) {
                logger("On local wifi");
                username = mServerUtil.getActiveServer().getLocalServerUsername();
                password = mServerUtil.getActiveServer().getLocalServerPassword();
            } else {
                logger("Not on local wifi");
                username = mServerUtil.getActiveServer().getRemoteServerUsername();
                password = mServerUtil.getActiveServer().getRemoteServerPassword();
            }
            HashMap<String, String> credentials = new HashMap<>();
            credentials.put(Authentication.USERNAME, username);
            credentials.put(Authentication.PASSWORD, password);

            return credentials.get(credential);
        } else return "";
    }

    /**
     * Register you device on Domoticz
     *
     * @param DeviceId UUID of the device
     * @param SenderId sender id from the Google services
     * @param receiver to get the callback on
     */
    public void AddMobileDevice(String DeviceId, String SenderId, MobileDeviceReceiver receiver) {
        MobileDeviceParser parser = new MobileDeviceParser(receiver);
        String url = constructGetUrl(Json.Url.Request.ADD_MOBILE_DEVICE);
        url += "&uuid=" + DeviceId;
        url += "&senderid=" + SenderId;

        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1);
    }


    /**
     * Clean previous sender id's on Domoticz
     *
     * @param DeviceId UUID of the device
     * @param receiver to get the callback on
     */
    public void CleanMobileDevice(String DeviceId, MobileDeviceReceiver receiver) {
        MobileDeviceParser parser = new MobileDeviceParser(receiver);
        String url = constructGetUrl(Json.Url.Request.CLEAN_MOBILE_DEVICE);
        url += "&uuid=" + DeviceId;

        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1);
    }

    /**
     * Get's version of the Domoticz server
     *
     * @param receiver to get the callback on
     */
    public void getServerVersion(VersionReceiver receiver) {
        VersionParser parser = new VersionParser(receiver);
        String url = constructGetUrl(Json.Url.Request.VERSION);
        RequestUtil.makeJsonVersionRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    /**
     * Get's the version of the update (if available)
     *
     * @param receiver to get the callback on
     */
    public void getUpdate(UpdateVersionReceiver receiver) {
        UpdateVersionParser parser = new UpdateVersionParser(receiver);
        String url = constructGetUrl(Json.Url.Request.UPDATE);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1);
    }

    /**
     * Get's if the update is downloaded and ready
     *
     * @param receiver to get the callback on
     */
    public void getUpdateDownloadReady(UpdateDownloadReadyReceiver receiver) {
        UpdateDownloadReadyParser parser = new UpdateDownloadReadyParser(receiver);
        String url = constructGetUrl(Json.Url.Request.UPDATE_DOWNLOAD_READY);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1);
    }

    /**
     * Gives the Domoticz server the command to install the latest update (if downloaded)
     *
     * @param receiver to get the callback on
     */
    public void updateDomoticzServer(@Nullable UpdateDomoticzServerReceiver receiver) {
        UpdateDomoticzServerParser parser = new UpdateDomoticzServerParser(receiver);
        String url = constructGetUrl(Json.Url.Request.UPDATE_DOMOTICZ_SERVER);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getScenes(ScenesReceiver receiver) {
        ScenesParser parser = new ScenesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.SCENES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getScene(ScenesReceiver receiver, int idx) {
        ScenesParser parser = new ScenesParser(receiver, idx);
        String url = constructGetUrl(Json.Url.Request.SCENES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getPlans(PlansReceiver receiver) {
        PlanParser parser = new PlanParser(receiver);
        String url = constructGetUrl(Json.Url.Request.PLANS);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getCameras(CameraReceiver receiver) {
        CameraParser parser = new CameraParser(receiver, this);
        String url = constructGetUrl(Json.Url.Request.CAMERAS);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public String getSnapshotUrl(CameraInfo camera) {
        return constructGetUrl(Json.Url.Request.CAMERA) + camera.getIdx();
    }

    public void getSwitches(SwitchesReceiver switchesReceiver) {
        SwitchesParser parser = new SwitchesParser(switchesReceiver);
        String url = constructGetUrl(Json.Url.Request.SWITCHES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getSwitchLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        logger("for idx: " + String.valueOf(idx));
        String url = constructGetUrl(Json.Url.Request.SWITCHLOG) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getTextLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        logger("for idx: " + String.valueOf(idx));
        String url = constructGetUrl(Json.Url.Request.TEXTLOG) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getSwitchTimers(int idx, SwitchTimerReceiver switchesReceiver) {
        SwitchTimerParser parser = new SwitchTimerParser(switchesReceiver);
        logger("for idx: " + String.valueOf(idx));
        String url = constructGetUrl(Json.Url.Request.SWITCHTIMER) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getNotifications(int idx, NotificationReceiver notificationReceiver) {
        NotificationsParser parser = new NotificationsParser(notificationReceiver);
        logger("for idx: " + String.valueOf(idx));
        String url = constructGetUrl(Json.Url.Request.NOTIFICATIONS) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setSecurityPanelAction(int secStatus,
                                       String seccode,
                                       setCommandReceiver receiver) {

        setCommandParser parser = new setCommandParser(receiver);
        String url = constructGetUrl(Json.Url.Request.SETSECURITY);
        url += "&secstatus=" + secStatus;
        url += "&seccode=" + seccode;

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setAction(int idx,
                          int jsonUrl,
                          int jsonAction,
                          double value,
                          String password,
                          setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = constructSetUrl(jsonUrl, idx, jsonAction, value);

        if (!UsefulBits.isEmpty(password)) {
            url += "&passcode=" + password;
        }

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setRGBColorAction(int idx,
                                  int jsonUrl,
                                  long hue,
                                  int brightness,
                                  boolean isWhite,
                                  String password,
                                  setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);

        String url = constructSetUrl(jsonUrl, idx, Device.Dimmer.Action.COLOR, 0);
        url = url.replace("%hue%", String.valueOf(hue)).replace("%bright%", String.valueOf(brightness));

        if (isWhite)
            url = url.replace("&iswhite=false", "&iswhite=true");

        if (!UsefulBits.isEmpty(password)) {
            url += "&passcode=" + password;
        }
        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setModalAction(int id,
                               int status, // one of Domoticz.Device.ModalSwitch.Action
                               int action, // behaves like this action == 1 ? 1 : 0
                               String password,
                               setCommandReceiver receiver) {
        String url = constructSetUrl(Domoticz.Json.Url.Set.MODAL_SWITCHES, id, status, 0);
        url += "&action=" + action;

        if (!UsefulBits.isEmpty(password)) {
            url += "&passcode=" + password;
        }
        Log.v(TAG, "Action: " + url);
        setCommandParser parser = new setCommandParser(receiver);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    /*
    public void setEventAction(int id,
                               String xmlStatement,
                               int jsonUrl,
                               int jsonAction,
                               long value,
                               setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = constructSetUrl(jsonUrl, id, jsonAction, value);
        url += Uri.encode(xmlStatement);
        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }*/

    public void setDeviceUsed(int id,
                              String name,
                              String description,
                              String extraParams,
                              setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = constructGetUrl(Json.Url.Request.SET_DEVICE_USED);
        url += id;
        url += "&name=" + name;
        url += "&description=" + description;
        if (extraParams != null) {
            url += extraParams;
        }
        url += "&used=true";

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getStatus(int idx, StatusReceiver receiver) {
        StatusInfoParser parser = new StatusInfoParser(receiver);
        String url = constructGetUrl(Json.Get.STATUS) + String.valueOf(idx);
        logger("for idx: " + String.valueOf(idx));

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getUtilities(UtilitiesReceiver receiver) {
        UtilitiesParser parser = new UtilitiesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.UTILITIES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getSettings(SettingsReceiver receiver) {
        SettingsParser parser = new SettingsParser(receiver);
        String url = constructGetUrl(Json.Url.Request.SETTINGS);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getConfig(ConfigReceiver receiver) {
        ConfigParser parser = new ConfigParser(receiver);
        String url = constructGetUrl(Json.Url.Request.CONFIG);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getTemperatures(TemperatureReceiver receiver) {
        TemperaturesParser parser = new TemperaturesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.TEMPERATURE);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getWeathers(WeatherReceiver receiver) {
        WeatherParser parser = new WeatherParser(receiver);
        String url = constructGetUrl(Json.Url.Request.WEATHER);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getDevices(DevicesReceiver receiver, int plan, String filter) {
        DevicesParser parser = new DevicesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.DEVICES);

        if (filter != null && filter.length() > 0)
            url = url.replace("filter=all", "filter=" + filter);
        if (plan > 0)
            url += "&plan=" + plan;

        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getDevice(DevicesReceiver receiver, int idx, boolean scene_or_group) {
        DevicesParser parser = new DevicesParser(receiver, idx, scene_or_group);
        String url = constructGetUrl(Json.Url.Request.DEVICES);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getLogs(LogsReceiver receiver) {
        LogsParser parser = new LogsParser(receiver);
        String url = constructGetUrl(Json.Url.Request.LOG);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getUserVariables(UserVariablesReceiver receiver) {
        UserVariablesParser parser = new UserVariablesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.USERVARIABLES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getEvents(EventReceiver receiver) {
        EventsParser parser = new EventsParser(receiver);
        String url = constructGetUrl(Json.Url.Request.EVENTS);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public void getEventXml(int id, EventXmlReceiver receiver) {
        EventsXmlParser parser = new EventsXmlParser(receiver);
        String url = constructGetUrl(Json.Url.Request.EVENTXML);
        url = url.replace("%id%", id + "");
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }


    public void getGraphData(int idx, String range, String type, GraphDataReceiver receiver) {
        GraphDataParser parser = new GraphDataParser(receiver);
        String url = constructGetUrl(Json.Url.Request.GRAPH) + String.valueOf(idx);
        url += Url.Log.GRAPH_RANGE + range;
        url += Url.Log.GRAPH_TYPE + type;

        Log.i("GRAPH", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3);
    }

    public File saveSnapShot(Bitmap bitmap, String name) {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                this.snapshot_file_path;
        File dir = new File(file_path);
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, "snapshot" + name + ".jpg");
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
            fOut.flush();
            fOut.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public int getDrawableIcon(String imgType, String Type, String switchType, boolean State, boolean useCustomImage, String CustomImage) {
        int standardImage = getDrawableIcon(imgType, Type, switchType, State);

        if (useCustomImage && CustomImage != null && CustomImage.length() > 0) {
            switch (CustomImage) {
                case "Alarm":
                    return R.drawable.alarm;
                case "Amplifier":
                    return R.drawable.volume;
                case "Computer":
                case "ComputerPC":
                    return R.drawable.computer;
                case "Cooling":
                    return R.drawable.cooling;
                case "ChristmasTree":
                    return R.drawable.christmastree;
                case "Fan":
                    return R.drawable.wind;
                case "Fireplace":
                    return R.drawable.flame;
                case "Generic":
                    return R.drawable.generic;
                case "Harddisk":
                    return R.drawable.harddisk;
                case "Heating":
                    return R.drawable.heating;
                case "Light":
                    return R.drawable.lights;
                case "Media":
                    return R.drawable.video;
                case "Phone":
                    return R.drawable.phone;
                case "Speaker":
                    return R.drawable.sub;
                case "Printer":
                    return R.drawable.printer;
                case "TV":
                    return R.drawable.tv;
                case "WallSocket":
                    return R.drawable.wall;
                case "Water":
                    return R.drawable.water;
            }
        }

        return standardImage;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private int getDrawableIcon(String imgType, String Type, String switchType, boolean State) {
        int test = R.drawable.defaultimage;
        switch (imgType.toLowerCase()) {
            case "scene":
                return R.drawable.generic;
            case "group":
                return R.drawable.generic;
            case "wind":
                return R.drawable.wind;
            case "doorbell":
                return R.drawable.door;
            case "door":
                return R.drawable.door;
            case "lightbulb":
                if (switchType != null && switchType.length() > 0 && switchType.equals(Device.Type.Name.DUSKSENSOR))
                    if (State)
                        return R.drawable.uvdark;
                    else
                        return R.drawable.uvsunny;
                else
                    return R.drawable.lights;
            case "push":
                return R.drawable.pushoff;
            case "pushoff":
                return R.drawable.pushoff;
            case "siren":
                return R.drawable.siren;
            case "smoke":
                return R.drawable.smoke;
            case "uv":
                return R.drawable.uv;
            case "contact":
                return R.drawable.contact;
            case "logitechMediaServer":
                return R.drawable.media;
            case "media":
                return R.drawable.media;
            case "blinds":
                return R.drawable.down;
            case "dimmer":
                if (switchType != null && switchType.length() > 0 && switchType.startsWith(Device.SubType.Name.RGB))
                    return R.drawable.rgb;
                else
                    return R.drawable.dimmer;
            case "motion":
                return R.drawable.motion;
            case "security":
                return R.drawable.security;
            case "temperature":
                if (State)
                    return R.drawable.heating;
                else
                    return R.drawable.cooling;
            case "counter":
                if (Type != null && Type.length() > 0 && Type.equals("P1 Smart Meter"))
                    return R.drawable.wall;
                else
                    return R.drawable.up;
            case "visibility":
                return R.drawable.visibility;
            case "radiation":
                return R.drawable.radiation;
            case "moisture":
            case "rain":
                return R.drawable.rain;
            case "leaf":
                return R.drawable.leaf;
            case "hardware":
                return R.drawable.computer;
            case "fan":
                return R.drawable.fan;
            case "speaker":
                return R.drawable.speaker;
            case "current":
                return R.drawable.wall;
            case "text":
                return R.drawable.text;
            case "alert":
                return R.drawable.siren;
            case "gauge":
                return R.drawable.gauge;
            case "clock":
                return R.drawable.clock48;
            case "mode":
                return R.drawable.defaultimage;
            case "utility":
                return R.drawable.scale;
        }

        switch (Type.toLowerCase()) {
            case "heating":
                return R.drawable.heating;
            case "thermostat":
                return R.drawable.flame;
        }
        return test;
    }

    public interface Authentication {
        String USERNAME = "username";
        String PASSWORD = "password";

        interface Method {
            String AUTH_METHOD_LOGIN_FORM = "Login form";
            String AUTH_METHOD_BASIC_AUTHENTICATION = "Basic authentication";
        }
    }

    public interface Result {
        String ERROR = "ERR";
        String OK = "OK";
    }

    public interface Protocol {
        String SECURE = "HTTPS";
        String INSECURE = "HTTP";
    }

    public interface Device {
        interface Switch {
            interface Action {
                int ON = 10;
                int OFF = 11;
            }
        }

        interface Dimmer {
            interface Action {
                int DIM_LEVEL = 20;
                int COLOR = 21;
            }
        }

        interface Blind {
            interface Action {
                int UP = 30;
                int STOP = 31;
                int ON = 33;
                int OFF = 34;
                int DOWN = 32;
            }
        }

        interface Thermostat {
            interface Action {
                int MIN = 50;
                int PLUS = 51;
            }
        }

        interface ModalSwitch {
            interface Action {
                int AUTO = 60;
                int ECONOMY = 61;
                int AWAY = 62;
                int DAY_OFF = 63;
                int CUSTOM = 64;
                int HEATING_OFF = 65;
            }
        }

        interface Type {
            @SuppressWarnings({"unused", "SpellCheckingInspection"})
            interface Value {
                int DOORBELL = 1;
                int CONTACT = 2;
                int BLINDS = 3;
                int SMOKE_DETECTOR = 5;
                int DIMMER = 7;
                int MOTION = 8;
                int PUSH_ON_BUTTON = 9;
                int PUSH_OFF_BUTTON = 10;
                int ON_OFF = 0;
                int SECURITY = 0;
                int X10SIREN = 4;
                int MEDIAPLAYER = 17;
                int DUSKSENSOR = 12;
                int DOORLOCK = 11;
                int BLINDPERCENTAGE = 13;
                int BLINDVENETIAN = 15;
                int BLINDINVERTED = 6;
                int BLINDPERCENTAGEINVERTED = 16;
                int SELECTOR = 18;
            }

            @SuppressWarnings({"unused", "SpellCheckingInspection"})
            interface Name {
                String DOORBELL = "Doorbell";
                String CONTACT = "Contact";
                String BLINDS = "Blinds";
                String SMOKE_DETECTOR = "Smoke Detector";
                String DIMMER = "Dimmer";
                String MOTION = "Motion Sensor";
                String PUSH_ON_BUTTON = "Push On Button";
                String PUSH_OFF_BUTTON = "Push Off Button";
                String ON_OFF = "On/Off";
                String SECURITY = "Security";
                String X10SIREN = "X10 Siren";
                String MEDIAPLAYER = "Media Player";
                String DUSKSENSOR = "Dusk Sensor";
                String DOORLOCK = "Door Lock";
                String BLINDPERCENTAGE = "Blinds Percentage";
                String BLINDVENETIAN = "Venetian Blinds EU";
                String BLINDINVERTED = "Blinds Inverted";
                String BLINDPERCENTAGEINVERTED = "Blinds Percentage Inverted";
                String TEMPHUMIDITYBARO = "Temp + Humidity + Baro";
                String WIND = "Wind";
                String SELECTOR = "Selector";
                String EVOHOME = "evohome";
            }
        }

        interface SubType {
            @SuppressWarnings({"unused", "SpellCheckingInspection"})
            interface Value {
                int RGB = 1;
                int SECURITYPANEL = 2;
                int EVOHOME = 3;
            }

            @SuppressWarnings({"unused", "SpellCheckingInspection"})
            interface Name {
                String RGB = "RGB";
                String SECURITYPANEL = "Security Panel";
                String EVOHOME = "Evohome";
            }
        }

        interface Favorite {
            int ON = 208;
            int OFF = 209;
        }
    }

    public interface Json {
        interface Field {
            String RESULT = "result";
            String STATUS = "status";
            String VERSION = "version";
        }

        interface Url {
            @SuppressWarnings({"unused", "SpellCheckingInspection"})
            interface Request {
                int DASHBOARD = 1;
                int SCENES = 2;
                int SWITCHES = 3;
                int UTILITIES = 4;
                int TEMPERATURE = 5;
                int WEATHER = 6;
                int CAMERAS = 7;
                int CAMERA = 21;
                int SUNRISE_SUNSET = 8;
                int VERSION = 9;
                int DEVICES = 10;
                int PLANS = 11;
                int PLAN_DEVICES = 12;
                int LOG = 13;
                int SWITCHLOG = 14;
                int SWITCHTIMER = 15;
                int UPDATE = 16;
                int USERVARIABLES = 17;
                int EVENTS = 18;
                int EVENTXML = 19;
                int GRAPH = 20;
                int SETTINGS = 22;
                int SETSECURITY = 23;
                int TEXTLOG = 24;
                int CONFIG = 25;
                int SET_DEVICE_USED = 26;
                int UPDATE_DOWNLOAD_READY = 27;
                int UPDATE_DOMOTICZ_SERVER = 28;
                int ADD_MOBILE_DEVICE = 29;
                int CLEAN_MOBILE_DEVICE = 30;
                int NOTIFICATIONS = 31;
            }

            @SuppressWarnings("SpellCheckingInspection")
            interface Set {
                int SCENES = 101;
                int SWITCHES = 102;
                int TEMP = 103;
                int FAVORITE = 104;
                int SCENEFAVORITE = 106;
                int EVENT = 105;
                int RGBCOLOR = 107;
                int MODAL_SWITCHES = 108;
            }
        }

        interface Get {
            int STATUS = 301;
        }
    }

    public interface Scene {
        interface Type {
            String GROUP = "Group";
            String SCENE = "Scene";
        }

        interface Action {
            int ON = 40;
            int OFF = 41;
        }
    }

    public interface Graph {
        interface Range {
            String DAY = "day";
            String MONTH = "month";
            String YEAR = "year";
            String WEEK = "week";
        }
    }

    public interface Temperature {
        @SuppressWarnings("unused")
        interface Sign {
            String CELSIUS = "C";
            String FAHRENHEIT = "F";
        }
    }

    public interface Event {
        @SuppressWarnings("unused")
        interface Type {
            String EVENT = "Event";
        }

        interface Action {
            int ON = 55;
            int OFF = 56;
        }
    }

    public interface Security {
        @SuppressWarnings("SpellCheckingInspection")
        interface Status {
            int ARMHOME = 1;
            int ARMAWAY = 2;
            int DISARM = 0;
        }
    }

    private interface Url {
        @SuppressWarnings("unused")
        interface Action {
            String ON = "On";
            String OFF = "Off";
            String UP = "Up";
            String STOP = "Stop";
            String DOWN = "Down";
            String PLUS = "Plus";
            String MIN = "Min";
        }

        interface ModalAction {
            String AUTO = "Auto";
            String ECONOMY = "AutoWithEco";
            String AWAY = "Away";
            String DAY_OFF = "DayOff";
            String CUSTOM = "Custom";
            String HEATING_OFF = "HeatingOff";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface Category {
            String ALLDEVICES = "/json.htm?type=devices";
            String DEVICES = "/json.htm?type=devices&filter=all&used=true&order=Name";
            String VERSION = "/json.htm?type=command&param=getversion";
            String DASHBOARD = ALLDEVICES + "&filter=all";
            String SCENES = "/json.htm?type=scenes";
            String SWITCHES = "/json.htm?type=command&param=getlightswitches";
            String WEATHER = ALLDEVICES + "&filter=weather&used=true";
            String CAMERAS = "/json.htm?type=cameras";
            String CAMERA = "/camsnapshot.jpg?idx=";
            String UTILITIES = ALLDEVICES + "&filter=utility&used=true";
            String PLANS = "/json.htm?type=plans";
            String TEMPERATURE = ALLDEVICES + "&filter=temp&used=true";
            String SWITCHLOG = "/json.htm?type=lightlog&idx=";
            String TEXTLOG = "/json.htm?type=textlog&idx=";
            String SWITCHTIMER = "/json.htm?type=timers&idx=";
        }

        @SuppressWarnings({"SpellCheckingInspection", "unused"})
        interface Switch {
            String DIM_LEVEL = "Set%20Level&level=";
            String COLOR = "&hue=%hue%&brightness=%bright%&iswhite=false";
            String GET = "/json.htm?type=command&param=switchlight&idx=";
            String CMD = "&switchcmd=";
            String LEVEL = "&level=";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface ModalSwitch {
            String GET = "/json.htm?type=command&param=switchmodal&idx=";
            String STATUS = "&status=";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface Scene {
            String GET = "/json.htm?type=command&param=switchscene&idx=";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface Temp {
            String GET = "/json.htm?type=command&param=udevice&idx=";
            String VALUE = "&nvalue=0&svalue=";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface Favorite {
            String GET = "/json.htm?type=command&param=makefavorite&idx=";
            String SCENE = "/json.htm?type=command&param=makescenefavorite&idx=";
            String VALUE = "&isfavorite=";
        }

        interface Protocol {
            String HTTP = "http://";
            String HTTPS = "https://";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface Device {
            String STATUS = "/json.htm?type=devices&rid=";
            String SET_USED = "/json.htm?type=setused&idx=";
        }

        @SuppressWarnings("unused")
        interface Sunrise {
            String GET = "/json.htm?type=command&param=getSunRiseSet";
        }

        @SuppressWarnings({"unused", "SpellCheckingInspection"})
        interface Plan {
            String GET = "/json.htm?type=plans";
            String DEVICES = "/json.htm?type=command&param=getplandevices&idx=";
        }

        @SuppressWarnings({"unused", "SpellCheckingInspection"})
        interface Log {
            String GRAPH = "/json.htm?type=graph&idx=";
            String GRAPH_RANGE = "&range=";
            String GRAPH_TYPE = "&sensor=";

            String GET_LOG = "/json.htm?type=command&param=getlog";
            String GET_FROMLASTLOGTIME = "/json.htm?type=command&param=getlog&lastlogtime=";
        }

        @SuppressWarnings({"unused", "SpellCheckingInspection"})
        interface Notification {
            String NOTIFICATION = "/json.htm?type=notifications&idx=";
        }

        @SuppressWarnings({"unused", "SpellCheckingInspection"})
        interface Security {
            String GET = "/json.htm?type=command&param=getsecstatus";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface System {
            String UPDATE = "/json.htm?type=command&param=checkforupdate&forced=true";
            String USERVARIABLES = "/json.htm?type=command&param=getuservariables";
            String EVENTS = "/json.htm?type=events&param=list";
            String EVENTXML = "/json.htm?type=events&param=load&event=%id%";
            String EVENTACTION = "/json.htm?type=events&param=create&name=LichtenAan&eventid=";
            String EVENTSTATUS = "&eventstatus=";
            String RGBCOLOR = "/json.htm?type=command&param=setcolbrightnessvalue&idx=";
            String SETTINGS = "/json.htm?type=settings";
            String CONFIG = "/json.htm?type=command&param=getconfig";
            String SETSECURITY = "/json.htm?type=command&param=setsecstatus";
            String DOWNLOAD_READY = "/json.htm?type=command&param=downloadready";
            String UPDATE_DOMOTICZ_SERVER = "/json.htm?type=command&param=execute_script&scriptname=update_domoticz&direct=true";
            String ADD_MOBILE_DEVICE = "/json.htm?type=command&param=addmobiledevice";
            String CLEAN_MOBILE_DEVICE = "/json.htm?type=command&param=deletemobiledevice";
        }
    }

    private interface FavoriteAction {
        String ON = "1";
        String OFF = "0";
    }
}
