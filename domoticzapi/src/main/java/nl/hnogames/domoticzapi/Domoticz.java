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

package nl.hnogames.domoticzapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Containers.ServerInfo;
import nl.hnogames.domoticzapi.Containers.UserVariableInfo;
import nl.hnogames.domoticzapi.Interfaces.AuthReceiver;
import nl.hnogames.domoticzapi.Interfaces.CameraReceiver;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.EventReceiver;
import nl.hnogames.domoticzapi.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticzapi.Interfaces.LanguageReceiver;
import nl.hnogames.domoticzapi.Interfaces.LogsReceiver;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;
import nl.hnogames.domoticzapi.Interfaces.NotificationReceiver;
import nl.hnogames.domoticzapi.Interfaces.PlansReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SettingsReceiver;
import nl.hnogames.domoticzapi.Interfaces.StatusReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticzapi.Interfaces.TemperatureReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateDomoticzServerReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateDownloadReadyReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateVersionReceiver;
import nl.hnogames.domoticzapi.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticzapi.Interfaces.UsersReceiver;
import nl.hnogames.domoticzapi.Interfaces.UtilitiesReceiver;
import nl.hnogames.domoticzapi.Interfaces.VersionReceiver;
import nl.hnogames.domoticzapi.Interfaces.WeatherReceiver;
import nl.hnogames.domoticzapi.Interfaces.WifiSSIDListener;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Parsers.AuthParser;
import nl.hnogames.domoticzapi.Parsers.CameraParser;
import nl.hnogames.domoticzapi.Parsers.ConfigParser;
import nl.hnogames.domoticzapi.Parsers.DevicesParser;
import nl.hnogames.domoticzapi.Parsers.EventsParser;
import nl.hnogames.domoticzapi.Parsers.GraphDataParser;
import nl.hnogames.domoticzapi.Parsers.LanguageParser;
import nl.hnogames.domoticzapi.Parsers.LogOffParser;
import nl.hnogames.domoticzapi.Parsers.LogsParser;
import nl.hnogames.domoticzapi.Parsers.MobileDeviceParser;
import nl.hnogames.domoticzapi.Parsers.NotificationsParser;
import nl.hnogames.domoticzapi.Parsers.PlanParser;
import nl.hnogames.domoticzapi.Parsers.ScenesParser;
import nl.hnogames.domoticzapi.Parsers.SettingsParser;
import nl.hnogames.domoticzapi.Parsers.StatusInfoParser;
import nl.hnogames.domoticzapi.Parsers.SwitchLogParser;
import nl.hnogames.domoticzapi.Parsers.SwitchTimerParser;
import nl.hnogames.domoticzapi.Parsers.SwitchesParser;
import nl.hnogames.domoticzapi.Parsers.TemperaturesParser;
import nl.hnogames.domoticzapi.Parsers.UpdateDomoticzServerParser;
import nl.hnogames.domoticzapi.Parsers.UpdateDownloadReadyParser;
import nl.hnogames.domoticzapi.Parsers.UpdateVersionParser;
import nl.hnogames.domoticzapi.Parsers.UserVariablesParser;
import nl.hnogames.domoticzapi.Parsers.UsersParser;
import nl.hnogames.domoticzapi.Parsers.UtilitiesParser;
import nl.hnogames.domoticzapi.Parsers.VersionParser;
import nl.hnogames.domoticzapi.Parsers.WeatherParser;
import nl.hnogames.domoticzapi.Parsers.setCommandParser;
import nl.hnogames.domoticzapi.Utils.PhoneConnectionUtil;
import nl.hnogames.domoticzapi.Utils.RequestUtil;
import nl.hnogames.domoticzapi.Utils.ServerUtil;
import nl.hnogames.domoticzapi.Utils.SessionUtil;
import nl.hnogames.domoticzapi.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Utils.VolleyUtil;

public class Domoticz {

    public static final int batteryLevelMax = 100;
    public static final int signalLevelMax = 12;
    public static final int DOMOTICZ_FAKE_ID = 99999;
    public static final String HIDDEN_CHARACTER = "$";

    public static final String DOMOTICZ_DEFAULT_SERVER = "DEFAULT";
    private static final String TAG = "DomoticzAPI";

    private ServerUtil mServerUtil;
    private PhoneConnectionUtil mPhoneConnectionUtil;
    private final SessionUtil mSessionUtil;
    private final DomoticzUrls mDomoticzUrls;
    private final RequestQueue queue;

    private Context mContext;

    public Domoticz(Context mContext, RequestQueue queue) {
        this.mContext = mContext;
        this.queue = queue;
        this.mSessionUtil = new SessionUtil(mContext);
        this.mServerUtil = new ServerUtil(mContext);
        this.mDomoticzUrls = new DomoticzUrls(this);

        if(!getServerUtil().getActiveServer().getUseOnlyLocal()) {
            mPhoneConnectionUtil = new PhoneConnectionUtil(mContext, new WifiSSIDListener() {
                @Override
                public void ReceiveSSIDs(CharSequence[] entries) {
                }
            });
        }
    }

    public ServerUtil getServerUtil() {
        return mServerUtil;
    }
    public SessionUtil getSessionUtil() {
        return mSessionUtil;
    }

    public boolean isUserOnLocalWifi() {
        boolean userIsLocal = false;
        if(getServerUtil().getActiveServer().getUseOnlyLocal())
            return true;
        else {
            if (getServerUtil().getActiveServer().getIsLocalServerAddressDifferent()) {
                Set<String> localSsid = getServerUtil().getActiveServer().getLocalServerSsid();

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
        }
        return userIsLocal;
    }

    public boolean isConnectionDataComplete(ServerInfo server) {
        boolean result = true;
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        stringHashMap.put("Domoticz local port", server.getLocalServerPort());
        if(!getServerUtil().getActiveServer().getUseOnlyLocal()) {
            stringHashMap.put("Domoticz remote URL", server.getRemoteServerUrl());
            stringHashMap.put("Domoticz remote port", server.getRemoteServerPort());
        }
        for (Map.Entry<String, String> entry : stringHashMap.entrySet()) {
            if (UsefulBits.isEmpty(entry.getValue())) {
                result = false;
                break;
            }
        }
        return result;
    }

    public String isConnectionDataComplete(ServerInfo server, boolean validatePorts) {
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        if(!getServerUtil().getActiveServer().getUseOnlyLocal())
            stringHashMap.put("Domoticz remote URL", server.getRemoteServerUrl());
        if (validatePorts) {
            stringHashMap.put("Domoticz local port", server.getLocalServerPort());
            if(!getServerUtil().getActiveServer().getUseOnlyLocal())
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
                result = false;
                break;
            }

        }
        return result;
    }

    public boolean isOnOffSwitch(DevicesInfo testSwitch) {
        if (testSwitch.getSwitchTypeVal() <= 0 && testSwitch.getSwitchType() == null)
            return false;

        switch (testSwitch.getSwitchTypeVal()) {
            case DomoticzValues.Device.Type.Value.ON_OFF:
            case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
            case DomoticzValues.Device.Type.Value.X10SIREN:
            case DomoticzValues.Device.Type.Value.DOORCONTACT:
            case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
            case DomoticzValues.Device.Type.Value.BLINDINVERTED:
            case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
            case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
            case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
            case DomoticzValues.Device.Type.Value.BLINDS:
            case DomoticzValues.Device.Type.Value.DIMMER:
                return true;
        }
        switch (testSwitch.getType()) {
            case DomoticzValues.Scene.Type.GROUP:
                return true;
        }

        return false;
    }

    public boolean isOnOffScene(SceneInfo testSwitch) {
        switch (testSwitch.getType()) {
            case DomoticzValues.Scene.Type.GROUP:
                return true;
        }
        return false;
    }

    public List<Integer> getSupportedSwitchesValues() {
        List<Integer> switchesSupported = new ArrayList<>();
        switchesSupported.add(DomoticzValues.Device.Type.Value.ON_OFF);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DIMMER);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDPERCENTAGE);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDVENETIAN);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDVENETIANUS);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDS);
        switchesSupported.add(DomoticzValues.Device.Type.Value.MOTION);
        switchesSupported.add(DomoticzValues.Device.Type.Value.CONTACT);
        switchesSupported.add(DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON);
        switchesSupported.add(DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON);
        switchesSupported.add(DomoticzValues.Device.Type.Value.MEDIAPLAYER);
        switchesSupported.add(DomoticzValues.Device.Type.Value.SMOKE_DETECTOR);
        switchesSupported.add(DomoticzValues.Device.Type.Value.X10SIREN);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DUSKSENSOR);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DOORCONTACT);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DOORLOCK);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DOORBELL);
        switchesSupported.add(DomoticzValues.Device.Type.Value.SECURITY);
        switchesSupported.add(DomoticzValues.Device.Type.Value.SELECTOR);
        return switchesSupported;
    }

    public List<String> getSupportedSwitchesNames() {
        List<String> switchesSupported = new ArrayList<>();
        switchesSupported.add(DomoticzValues.Device.Type.Name.ON_OFF);
        switchesSupported.add(DomoticzValues.Device.Type.Name.DIMMER);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDPERCENTAGE);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDVENETIAN);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDVENETIANUS);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDS);
        switchesSupported.add(DomoticzValues.Device.Type.Name.PUSH_ON_BUTTON);
        switchesSupported.add(DomoticzValues.Device.Type.Name.PUSH_OFF_BUTTON);
        switchesSupported.add(DomoticzValues.Device.Type.Name.CONTACT);
        switchesSupported.add(DomoticzValues.Device.Type.Name.MOTION);
        switchesSupported.add(DomoticzValues.Device.Type.Name.MEDIAPLAYER);
        switchesSupported.add(DomoticzValues.Device.Type.Name.SMOKE_DETECTOR);
        switchesSupported.add(DomoticzValues.Device.Type.Name.X10SIREN);
        switchesSupported.add(DomoticzValues.Device.Type.Name.DUSKSENSOR);
        switchesSupported.add(DomoticzValues.Device.Type.Name.DOORLOCK);
        switchesSupported.add(DomoticzValues.Device.Type.Name.DOORCONTACT);
        switchesSupported.add(DomoticzValues.Device.Type.Name.DOORBELL);
        switchesSupported.add(DomoticzValues.Device.Type.Name.SECURITY);
        switchesSupported.add(DomoticzValues.Device.Type.Name.SELECTOR);
        switchesSupported.add(DomoticzValues.Device.Type.Name.EVOHOME);
        return switchesSupported;
    }

    public String getErrorMessage(Exception error) {
        String errorMessage = "";
        if (error instanceof VolleyError) {
            VolleyUtil mVolleyUtil = new VolleyUtil(mContext);
            VolleyError volleyError = (VolleyError) error;
            errorMessage = mVolleyUtil.getVolleyErrorMessage(volleyError);
        }
        if (UsefulBits.isEmpty(errorMessage))
            errorMessage = error.getMessage();
        return errorMessage;
    }

    public String getUserCredentials(String credential) {
        if (credential.equals(Authentication.USERNAME)
                || credential.equals(Authentication.PASSWORD)) {

            String username, password;
            if (isUserOnLocalWifi()) {
                username = getServerUtil().getActiveServer().getLocalServerUsername();
                password = getServerUtil().getActiveServer().getLocalServerPassword();
            } else {
                username = getServerUtil().getActiveServer().getRemoteServerUsername();
                password = getServerUtil().getActiveServer().getRemoteServerPassword();
            }
            HashMap<String, String> credentials = new HashMap<>();
            credentials.put(Authentication.USERNAME, username);
            credentials.put(Authentication.PASSWORD, password);
            return credentials.get(credential);
        } else return "";
    }

    /**
     * Get's the version of the update (if available)
     *
     * @param receiver to get the callback on
     */
    public void getUserAuthenticationRights(AuthReceiver receiver) {
        AuthParser parser = new AuthParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.AUTH);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1, queue);
    }


    public interface Authentication {
        String USERNAME = "username";
        String PASSWORD = "password";

        interface Method {
            String AUTH_METHOD_LOGIN_FORM = "Login form";
            String AUTH_METHOD_BASIC_AUTHENTICATION = "Basic authentication";
        }
    }

    public void setUserCredentials(String username, String password) {
        if (!UsefulBits.isEmpty(username) && !UsefulBits.isEmpty(password)) {
            if (isUserOnLocalWifi()) {
                mServerUtil.getActiveServer().setLocalServerUsername(username);
                mServerUtil.getActiveServer().setLocalServerPassword(password);
            } else {
                mServerUtil.getActiveServer().setRemoteServerUsername(username);
                mServerUtil.getActiveServer().setRemoteServerPassword(password);
            }
            mServerUtil.saveDomoticzServers(true);
        }
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
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.ADD_MOBILE_DEVICE);
        url += "&uuid=" + DeviceId;
        url += "&senderid=" + SenderId;
        url += "&name=" + Build.MODEL.replace(" ","");
        url += "&devicetype=Android" + Build.VERSION.SDK_INT;

        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1, queue);
    }

    /**
     * Clean previous sender id's on Domoticz
     *
     * @param DeviceId UUID of the device
     * @param receiver to get the callback on
     */
    public void CleanMobileDevice(String DeviceId, MobileDeviceReceiver receiver) {
        MobileDeviceParser parser = new MobileDeviceParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CLEAN_MOBILE_DEVICE);
        url += "&uuid=" + DeviceId;

        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1, queue);
    }

    /**
     * Get's version of the Domoticz server
     *
     * @param receiver to get the callback on
     */
    public void getServerVersion(VersionReceiver receiver) {
        VersionParser parser = new VersionParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.VERSION);
        RequestUtil.makeJsonVersionRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, getSessionUtil(), true, 3, queue);
    }

    public String getSnapshotUrl(CameraInfo camera) {
        return mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CAMERA, true, getUserCredentials(Authentication.USERNAME), getUserCredentials(Authentication.PASSWORD)) + camera.getIdx();
    }

    /**
     * Get's the version of the update (if available)
     *
     * @param receiver to get the callback on
     */
    public void getUpdate(UpdateVersionReceiver receiver) {
        UpdateVersionParser parser = new UpdateVersionParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1, queue);
    }

    /**
     * Get's if the update is downloaded and ready
     *
     * @param receiver to get the callback on
     */
    public void getUpdateDownloadReady(UpdateDownloadReadyReceiver receiver) {
        UpdateDownloadReadyParser parser = new UpdateDownloadReadyParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE_DOWNLOAD_READY);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, false, 1, queue);
    }

    /**
     * Gives the Domoticz server the command to install the latest update (if downloaded)
     *
     * @param receiver to get the callback on
     */
    public void updateDomoticzServer(@Nullable UpdateDomoticzServerReceiver receiver) {
        UpdateDomoticzServerParser parser = new UpdateDomoticzServerParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE_DOMOTICZ_SERVER);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getSwitches(SwitchesReceiver switchesReceiver) {
        SwitchesParser parser = new SwitchesParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SWITCHES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getSwitchLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SWITCHLOG) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getTextLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.TEXTLOG) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getSceneLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SCENELOG) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }


    public void getScenes(ScenesReceiver receiver) {
        ScenesParser parser = new ScenesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SCENES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getScene(ScenesReceiver receiver, int idx) {
        ScenesParser parser = new ScenesParser(receiver, idx);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SCENES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getPlans(PlansReceiver receiver) {
        PlanParser parser = new PlanParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.PLANS);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getCameras(CameraReceiver receiver) {
        CameraParser parser = new CameraParser(receiver, this);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CAMERAS);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }


    public void getSwitchTimers(int idx, SwitchTimerReceiver switchesReceiver) {
        SwitchTimerParser parser = new SwitchTimerParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SWITCHTIMER) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getNotifications(int idx, NotificationReceiver notificationReceiver) {
        NotificationsParser parser = new NotificationsParser(notificationReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.NOTIFICATIONS) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setSecurityPanelAction(int secStatus,
                                       String seccode,
                                       setCommandReceiver receiver) {

        setCommandParser parser = new setCommandParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SETSECURITY);
        url += "&secstatus=" + secStatus;
        url += "&seccode=" + seccode;

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setUserVariableValue(  String newValue,
                                       UserVariableInfo var,
                                       setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATEVAR);
        url += "&idx=" + var.getIdx();
        url += "&vname=" + var.getName();
        url += "&vtype=" + var.getType();
        url += "&vvalue=" + newValue;

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setAction(int idx,
                          int jsonUrl,
                          int jsonAction,
                          double value,
                          String password,
                          setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = mDomoticzUrls.constructSetUrl(jsonUrl, idx, jsonAction, value);
        url += UsefulBits.isEmpty(password) ? "&passcode=" : "&passcode=" + password;

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
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

        String url = mDomoticzUrls.constructSetUrl(jsonUrl, idx, DomoticzValues.Device.Dimmer.Action.COLOR, 0);
        url = url.replace("%hue%", String.valueOf(hue)).replace("%bright%", String.valueOf(brightness));

        if (isWhite)
            url = url.replace("&iswhite=false", "&iswhite=true");

        if (!UsefulBits.isEmpty(password)) {
            url += "&passcode=" + password;
        }
        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setModalAction(int id,
                               int status, // one of Domoticz.Device.ModalSwitch.Action
                               int action, // behaves like this action == 1 ? 1 : 0
                               String password,
                               setCommandReceiver receiver) {
        String url = mDomoticzUrls.constructSetUrl(DomoticzValues.Json.Url.Set.MODAL_SWITCHES, id, status, 0);
        url += "&action=" + action;

        if (!UsefulBits.isEmpty(password)) {
            url += "&passcode=" + password;
        }
        Log.v(TAG, "Action: " + url);
        setCommandParser parser = new setCommandParser(receiver);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }


    /*
    @DebugLog
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
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SET_DEVICE_USED);
        url += id;
        url += "&name=" + name;
        url += "&description=" + description;
        if (extraParams != null) {
            url += extraParams;
        }
        url += "&used=true";

        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getStatus(int idx, StatusReceiver receiver) {
        StatusInfoParser parser = new StatusInfoParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Get.STATUS) + String.valueOf(idx);

        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getUtilities(UtilitiesReceiver receiver) {
        UtilitiesParser parser = new UtilitiesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UTILITIES);
        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getSettings(SettingsReceiver receiver) {
        SettingsParser parser = new SettingsParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SETTINGS);
        Log.v(TAG, url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getConfig(ConfigReceiver receiver) {
        ConfigParser parser = new ConfigParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CONFIG);
        Log.v(TAG, url);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getLanguageStringsFromServer(String language, LanguageReceiver receiver) {
        LanguageParser parser = new LanguageParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.LANGUAGE);
        Log.v(TAG, url);
        url += language + ".json";
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getTemperatures(TemperatureReceiver receiver) {
        TemperaturesParser parser = new TemperaturesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.TEMPERATURE);
        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getWeathers(WeatherReceiver receiver) {
        WeatherParser parser = new WeatherParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.WEATHER);
        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getFavorites(DevicesReceiver receiver, int plan, String filter) {
        DevicesParser parser = new DevicesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.FAVORITES);

        if (filter != null && filter.length() > 0)
            url = url.replace("filter=all", "filter=" + filter);
        if (plan > 0)
            url += "&plan=" + plan;

        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getDevices(DevicesReceiver receiver, int plan, String filter) {
        DevicesParser parser = new DevicesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.DEVICES);

        if (filter != null && filter.length() > 0)
            url = url.replace("filter=all", "filter=" + filter);
        if (plan > 0)
            url += "&plan=" + plan;

        Log.v(TAG, url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getDevice(DevicesReceiver receiver, int idx, boolean scene_or_group) {
        DevicesParser parser = new DevicesParser(receiver, idx, scene_or_group);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.DEVICES);

        Log.i("DEVICE", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getLogs(LogsReceiver receiver) {
        LogsParser parser = new LogsParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.LOG);
        Log.i("Logs", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getUserVariables(UserVariablesReceiver receiver) {
        UserVariablesParser parser = new UserVariablesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.USERVARIABLES);
        Log.i("USERVARIABLES", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getUsers(UsersReceiver receiver) {
        UsersParser parser = new UsersParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.USERS);
        Log.i("USERS", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void LogOff() {
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.LOGOFF);
        Log.i("LOGOFF", "url: " + url);
        RequestUtil.makeJsonGetRequest(new LogOffParser(),
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getEvents(EventReceiver receiver) {
        EventsParser parser = new EventsParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.EVENTS);

        Log.i("EVENTS", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    public void getGraphData(int idx, String range, String type, GraphDataReceiver receiver) {
        GraphDataParser parser = new GraphDataParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.GRAPH) + String.valueOf(idx);
        url += DomoticzValues.Url.Log.GRAPH_RANGE + range;
        url += DomoticzValues.Url.Log.GRAPH_TYPE + type;

        Log.i("GRAPH", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url, mSessionUtil, true, 3, queue);
    }

    private String snapshot_file_path = "/Domoticz/SnapShot";
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

}
