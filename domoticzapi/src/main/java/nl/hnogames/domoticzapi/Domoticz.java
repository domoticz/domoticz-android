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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;
import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.LoginInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Containers.ServerInfo;
import nl.hnogames.domoticzapi.Containers.UserVariableInfo;
import nl.hnogames.domoticzapi.Interfaces.CameraReceiver;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.DownloadUpdateServerReceiver;
import nl.hnogames.domoticzapi.Interfaces.EventReceiver;
import nl.hnogames.domoticzapi.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.LanguageReceiver;
import nl.hnogames.domoticzapi.Interfaces.LoginReceiver;
import nl.hnogames.domoticzapi.Interfaces.LogsReceiver;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;
import nl.hnogames.domoticzapi.Interfaces.NotificationReceiver;
import nl.hnogames.domoticzapi.Interfaces.NotificationTypesReceiver;
import nl.hnogames.domoticzapi.Interfaces.PlansReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;
import nl.hnogames.domoticzapi.Interfaces.SendNotificationReceiver;
import nl.hnogames.domoticzapi.Interfaces.SettingsReceiver;
import nl.hnogames.domoticzapi.Interfaces.StatusReceiver;
import nl.hnogames.domoticzapi.Interfaces.SunRiseReceiver;
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
import nl.hnogames.domoticzapi.Interfaces.VolleyErrorListener;
import nl.hnogames.domoticzapi.Interfaces.WeatherReceiver;
import nl.hnogames.domoticzapi.Interfaces.WifiSSIDListener;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Parsers.CameraParser;
import nl.hnogames.domoticzapi.Parsers.ConfigParser;
import nl.hnogames.domoticzapi.Parsers.DevicesParser;
import nl.hnogames.domoticzapi.Parsers.DownloadUpdateParser;
import nl.hnogames.domoticzapi.Parsers.EventsParser;
import nl.hnogames.domoticzapi.Parsers.GraphDataParser;
import nl.hnogames.domoticzapi.Parsers.LanguageParser;
import nl.hnogames.domoticzapi.Parsers.LogOffParser;
import nl.hnogames.domoticzapi.Parsers.LoginParser;
import nl.hnogames.domoticzapi.Parsers.LogsParser;
import nl.hnogames.domoticzapi.Parsers.MobileDeviceParser;
import nl.hnogames.domoticzapi.Parsers.NotificationTypesParser;
import nl.hnogames.domoticzapi.Parsers.NotificationsParser;
import nl.hnogames.domoticzapi.Parsers.PlanParser;
import nl.hnogames.domoticzapi.Parsers.ScenesParser;
import nl.hnogames.domoticzapi.Parsers.SendNotificationParser;
import nl.hnogames.domoticzapi.Parsers.SettingsParser;
import nl.hnogames.domoticzapi.Parsers.StatusInfoParser;
import nl.hnogames.domoticzapi.Parsers.SunRiseParser;
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

    private static final String TAG = "DomoticzAPI";
    private final SessionUtil mSessionUtil;
    private final DomoticzUrls mDomoticzUrls;
    private final RequestQueue queue;
    private ServerUtil mServerUtil;
    private PhoneConnectionUtil mPhoneConnectionUtil;
    private Context mContext;
    public static boolean BasicAuthDetected = false;

    public Domoticz(Context mContext, RequestQueue queue) {
        this.mContext = mContext;
        this.queue = queue;
        this.mSessionUtil = new SessionUtil(mContext);
        this.mServerUtil = new ServerUtil(mContext);
        this.mDomoticzUrls = new DomoticzUrls(this);

        if (!getServerUtil().getActiveServer().getUseOnlyLocal()) {
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
        if (getServerUtil().getActiveServer().getUseOnlyLocal())
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
                            if (ssid.equals(currentSsid)) {
                                userIsLocal = true;
                                break;
                            }
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
        stringHashMap.put("Server name", server.getServerName());
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        stringHashMap.put("Domoticz local port", server.getLocalServerPort());
        if (!getServerUtil().getActiveServer().getUseOnlyLocal()) {
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
        stringHashMap.put("Server name", server.getServerName());
        stringHashMap.put("Domoticz local URL", server.getLocalServerUrl());
        if (!getServerUtil().getActiveServer().getUseOnlyLocal())
            stringHashMap.put("Domoticz remote URL", server.getRemoteServerUrl());
        if (validatePorts) {
            stringHashMap.put("Domoticz local port", server.getLocalServerPort());
            if (!getServerUtil().getActiveServer().getUseOnlyLocal())
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
            case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTEDSTOP:
            case DomoticzValues.Device.Type.Value.BLINDINVERTED:
            case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
            case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
            case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
            case DomoticzValues.Device.Type.Value.BLINDS:
            case DomoticzValues.Device.Type.Value.DIMMER:
                return true;
        }

        if (DomoticzValues.Scene.Type.GROUP.equals(testSwitch.getType()))
            return true;
        return false;
    }

    public boolean isOnOffScene(SceneInfo testSwitch) {
        if (DomoticzValues.Scene.Type.GROUP.equals(testSwitch.getType())) {
            return true;
        }
        return false;
    }

    public List<Integer> getSupportedSwitchesValues() {
        List<Integer> switchesSupported = new ArrayList<>();
        switchesSupported.add(DomoticzValues.Device.Type.Value.ON_OFF);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DIMMER);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDPERCENTAGE);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTEDSTOP);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDVENETIAN);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDVENETIANUS);
        switchesSupported.add(DomoticzValues.Device.Type.Value.BLINDINVERTED);
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
        switchesSupported.add(DomoticzValues.Device.Type.Value.DOORLOCKINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Value.DOORBELL);
        switchesSupported.add(DomoticzValues.Device.Type.Value.SECURITY);
        switchesSupported.add(DomoticzValues.Device.Type.Value.SELECTOR);
        return switchesSupported;
    }

    public List<String> getSupportedSwitchesNames() {
        List<String> switchesSupported = new ArrayList<>();
        switchesSupported.add(DomoticzValues.Device.Type.Name.ON_OFF);
        switchesSupported.add(DomoticzValues.Device.Type.Name.DIMMER);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDVENETIAN);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDVENETIANUS);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDPERCENTAGE);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDPERCENTAGESTOP);
        switchesSupported.add(DomoticzValues.Device.Type.Name.BLINDPERCENTAGEINVERTEDSTOP);
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
        switchesSupported.add(DomoticzValues.Device.Type.Name.DOORLOCKINVERTED);
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
    public void getUserAuthenticationRights(LoginReceiver receiver) {
        LoginParser parser = new LoginParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.AUTH);
        GetRequest(parser, url, true);
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

    public void AddLog(String message, setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Set.LOG);
        url = url + message;
        GetRequest(parser, url, true);
    }

    public void GetNotificationSystems(NotificationTypesReceiver receiver) {
        NotificationTypesParser parser = new NotificationTypesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.NOTIFICATIONTYPES);
        GetRequest(parser, url, true);
    }

    public void SendNotification(String Subject, String Body, String SubSystem, SendNotificationReceiver receiver) {
        SendNotificationParser parser = new SendNotificationParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SEND_NOTIFICATION);
        url += "&subject=" + Subject;
        url += "&body=" + Body;
        url += "&subsystem=" + SubSystem;
        GetRequest(parser, url, true);
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
        url += "&name=" + Build.MODEL.replace(" ", "");
        url += "&devicetype=Android" + Build.VERSION.SDK_INT;
        GetRequest(parser, url, true);
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

        GetRequest(parser, url, true);
    }

    /**
     * Get the sunrise info from the server
     *
     * @param receiver to get the callback on
     */
    public void getSunRise(SunRiseReceiver receiver) {
        SunRiseParser parser = new SunRiseParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SUNRISE);
        GetRequest(parser, url, true);
    }

    /**
     * Get's version of the Domoticz server
     *
     * @param receiver to get the callback on
     */
    public void getServerVersion(VersionReceiver receiver) {
        VersionParser parser = new VersionParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.VERSION);
        GetRequest(parser, url, true);
    }

    public String getSnapshotUrl(CameraInfo camera) {
        return getSnapshotUrl(camera.getIdx());
    }

    public String getSnapshotUrl(int idx) {
        return mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CAMERA) + idx;
    }

    /**
     * Get's the version of the update (if available)
     *
     * @param receiver to get the callback on
     */
    public void getUpdate(UpdateVersionReceiver receiver) {
        UpdateVersionParser parser = new UpdateVersionParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE);
        GetRequest(parser, url, true);
    }

    /**
     * Get's if the update is downloaded and ready
     *
     * @param receiver to get the callback on
     */
    public void getDownloadUpdate(DownloadUpdateServerReceiver receiver) {
        DownloadUpdateParser parser = new DownloadUpdateParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE_DOWNLOAD_UPDATE);
        GetRequest(parser, url, true);
    }

    /**
     * Download the update file
     *
     * @param receiver to get the callback on
     */
    public void getUpdateDownloadReady(UpdateDownloadReadyReceiver receiver) {
        UpdateDownloadReadyParser parser = new UpdateDownloadReadyParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE_DOWNLOAD_READY);
        GetRequest(parser, url, true);
    }

    /**
     * Gives the Domoticz server the command to install the latest update (if downloaded)
     *
     * @param receiver to get the callback on
     */
    public void updateDomoticzServer(@Nullable UpdateDomoticzServerReceiver receiver) {
        UpdateDomoticzServerParser parser = new UpdateDomoticzServerParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATE_DOMOTICZ_SERVER);
        GetRequest(parser,
                url, true);
    }

    public void checkLogin(LoginReceiver loginReceiver) {
        mSessionUtil.clearSessionCookie();
        String baseUsername = getUserCredentials(Authentication.USERNAME);
        String basePassword = getUserCredentials(Authentication.PASSWORD);
        if(UsefulBits.isEmpty(baseUsername)||UsefulBits.isEmpty(basePassword))
            loginReceiver.OnReceive(new LoginInfo());

        String username = UsefulBits.encodeBase64(getUserCredentials(Authentication.USERNAME));
        String password = UsefulBits.getMd5String(getUserCredentials(Authentication.PASSWORD));
        LoginParser parser = new LoginParser(loginReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.NEWCHECKLOGIN);
        Log.v(TAG, "Url: " + url);

        try {
            Map<String, String> params = new HashMap<>();
            params.put("username", URLEncoder.encode(username, "UTF-8"));
            params.put("password", URLEncoder.encode(password, "UTF-8"));
            LoginPostRequest(parser, url, params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getSwitches(SwitchesReceiver switchesReceiver) {
        SwitchesParser parser = new SwitchesParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SWITCHES);
        GetResultRequest(parser, url, true);
    }

    public void getSwitchLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SWITCHLOG) + idx;

        GetResultRequest(parser,
                url, true);
    }

    public void getTextLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.TEXTLOG) + idx;

        GetResultRequest(parser,
                url, true);
    }

    public void getSceneLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SCENELOG) + idx;
        GetResultRequest(parser,
                url, true);
    }


    public void getScenes(ScenesReceiver receiver) {
        ScenesParser parser = new ScenesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SCENES);
        GetResultRequest(parser,
                url, true);
    }

    public void getScene(ScenesReceiver receiver, int idx) {
        ScenesParser parser = new ScenesParser(receiver, idx);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SCENES);
        GetResultRequest(parser,
                url, true);
    }

    public void getPlans(PlansReceiver receiver) {
        PlanParser parser = new PlanParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.PLANS);
        GetResultRequest(parser,
                url, true);
    }

    public void getCameras(CameraReceiver receiver) {
        CameraParser parser = new CameraParser(receiver, this);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CAMERAS);

        GetResultRequest(parser,
                url, true);
    }


    public void getSwitchTimers(int idx, SwitchTimerReceiver switchesReceiver) {
        SwitchTimerParser parser = new SwitchTimerParser(switchesReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SWITCHTIMER) + idx;

        GetResultRequest(parser,
                url, true);
    }

    public void getNotifications(int idx, NotificationReceiver notificationReceiver) {
        NotificationsParser parser = new NotificationsParser(notificationReceiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.NOTIFICATIONS) + idx;

        GetResultRequest(parser,
                url, true);
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
        GetRequest(parser, url, true);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setUserVariableValue(String newValue,
                                     UserVariableInfo var,
                                     setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UPDATEVAR);
        url += "&idx=" + var.getIdx();
        url += "&vname=" + var.getName();
        url += "&vtype=" + var.getType();
        url += "&vvalue=" + newValue;

        Log.v(TAG, "Action: " + url);
        GetRequest(parser, url, true);
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
        GetRequest(parser, url, true);
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
        GetRequest(parser,
                url, true);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void setWWColorAction(int idx,
                                 int cw,
                                 int ww,
                                 int brightness,
                                 String password,
                                 setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);

        String url = mDomoticzUrls.constructSetUrl(DomoticzValues.Json.Url.Set.WWCOLOR, idx, DomoticzValues.Device.Dimmer.Action.WWCOLOR, 0);
        url = url.replace("%ww%", String.valueOf(ww))
                .replace("%cw%", String.valueOf(cw))
                .replace("%bright%", String.valueOf(brightness));

        if (!UsefulBits.isEmpty(password)) {
            url += "&passcode=" + password;
        }
        Log.v(TAG, "Action: " + url);
        GetRequest(parser,
                url, true);
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
        GetRequest(parser,
                url, true);
    }

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
        GetRequest(parser,
                url, true);
    }

    public void getStatus(int idx, StatusReceiver receiver) {
        StatusInfoParser parser = new StatusInfoParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Get.STATUS) + String.valueOf(idx);
        Log.v(TAG, url);
        GetResultRequest(parser,
                url, true);
    }

    public void getUtilities(UtilitiesReceiver receiver) {
        UtilitiesParser parser = new UtilitiesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.UTILITIES);
        Log.v(TAG, url);
        GetResultRequest(parser,
                url, true);
    }

    public void getSettings(SettingsReceiver receiver) {
        SettingsParser parser = new SettingsParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.SETTINGS);
        Log.v(TAG, url);
        GetRequest(parser,
                url, true);
    }

    public void getConfig(ConfigReceiver receiver) {
        ConfigParser parser = new ConfigParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.CONFIG);
        Log.v(TAG, url);
        GetRequest(parser, url, true);
    }

    public void getLanguageStringsFromServer(String language, LanguageReceiver receiver) {
        LanguageParser parser = new LanguageParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.LANGUAGE);
        Log.v(TAG, url);
        url += language + ".json";
        GetRequest(parser,
                url, true);
    }

    public void getTemperatures(TemperatureReceiver receiver) {
        TemperaturesParser parser = new TemperaturesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.TEMPERATURE);
        Log.v(TAG, url);
        GetResultRequest(parser,
                url, true);
    }

    public void getWeathers(WeatherReceiver receiver) {
        WeatherParser parser = new WeatherParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.WEATHER);
        Log.v(TAG, url);
        GetResultRequest(parser,
                url, true);
    }

    public void getFavorites(DevicesReceiver receiver, int plan, String filter) {
        DevicesParser parser = new DevicesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.FAVORITES);

        if (filter != null && filter.length() > 0)
            url = url.replace("filter=all", "filter=" + filter);
        if (plan > 0)
            url += "&plan=" + plan;

        Log.v(TAG, url);
        GetResultRequest(parser, url, true);
    }

    public void getDevices(DevicesReceiver receiver, int plan, String filter) {
        DevicesParser parser = new DevicesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.DEVICES);

        if (filter != null && filter.length() > 0)
            url = url.replace("filter=all", "filter=" + filter);
        if (plan > 0)
            url += "&plan=" + plan;

        Log.v(TAG, url);
        GetResultRequest(parser,
                url, true);
    }

    public void getDevice(DevicesReceiver receiver, int idx, boolean scene_or_group) {
        DevicesParser parser = new DevicesParser(receiver, idx, scene_or_group);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.DEVICES);

        Log.i("DEVICE", "url: " + url);
        GetResultRequest(parser,
                url, true);
    }

    public void getLogs(LogsReceiver receiver, int logLevel) {
        LogsParser parser = new LogsParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.LOG) + logLevel;
        Log.i("Logs", "url: " + url);
        GetResultRequest(parser,
                url, true);
    }

    public void getUserVariables(UserVariablesReceiver receiver) {
        UserVariablesParser parser = new UserVariablesParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.USERVARIABLES);
        Log.i("USERVARIABLES", "url: " + url);
        GetResultRequest(parser,
                url, true);
    }

    public void getUsers(UsersReceiver receiver) {
        UsersParser parser = new UsersParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.USERS);
        Log.i("USERS", "url: " + url);
        GetResultRequest(parser,
                url, true);
    }

    public void LogOff() {
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.LOGOFF);
        mSessionUtil.clearSessionCookie();
        Log.i("LOGOFF", "url: " + url);
        GetRequest(new LogOffParser(), url, false);
    }

    public void getEvents(EventReceiver receiver) {
        EventsParser parser = new EventsParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.EVENTS);
        Log.i("EVENTS", "url: " + url);
        GetResultRequest(parser, url, true);
    }

    public void getGraphData(int idx, String range, String type, GraphDataReceiver receiver) {
        GraphDataParser parser = new GraphDataParser(receiver);
        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.GRAPH) + idx;
        url += DomoticzValues.Url.Log.GRAPH_RANGE + range;
        url += DomoticzValues.Url.Log.GRAPH_TYPE + type;
        Log.i("GRAPH", "url: " + url);
        GetResultRequest(parser, url, true);
    }

    public void getTempGraphData(int idx, String range, int graphType,
                                 boolean graphTemp, boolean graphChill, boolean graphHum, boolean graphBaro, boolean graphDew, boolean graphSet,
                                 GraphDataReceiver receiver) {
        GraphDataParser parser = new GraphDataParser(receiver);

        String url = mDomoticzUrls.constructGetUrl(DomoticzValues.Json.Url.Request.TEMPGRAPHS) + idx;
        url += "&range=" + range; //2020-07-29T2020-08-05
        url += "&graphtype=" + graphType; //1
        url += "&graphTemp=" + graphTemp; //true
        url += "&graphChill=" + graphChill; //true
        url += "&graphHum=" + graphHum; //true
        url += "&graphBaro=" + graphBaro; //true
        url += "&graphDew=" + graphDew; //true
        url += "&graphSet=" + graphSet; //true

        Log.i("GRAPHS", "url: " + url);
        GetResultRequest(parser, url, true);
    }

    private void GetRequest(@Nullable final JSONParserInterface parser,
                            final String url,
                            boolean retry) {
        final VolleyErrorListener defaultListener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                if (parser != null)
                    parser.parseResult(response.toString());
            }

            @Override
            public void onError(Exception error) {
                if (parser != null)
                    parser.onError(error);
            }
        };

        VolleyErrorListener listener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                if (parser != null)
                    parser.parseResult(response.toString());
            }

            @Override
            public void onError(Exception error) {
                //retry after login
                checkLogin(new LoginReceiver() {
                    @Override
                    public void OnReceive(LoginInfo mLoginInfo) {
                        RequestUtil.makeJsonGetRequest(defaultListener, url, mSessionUtil,
                                getUserCredentials(Authentication.USERNAME),
                                getUserCredentials(Authentication.PASSWORD),
                                BasicAuthDetected,
                                queue);
                    }

                    @Override
                    public void onError(Exception error) {
                        if (parser != null)
                            parser.onError(error);
                    }
                });
            }
        };

        RequestUtil.makeJsonGetRequest(retry ? listener : defaultListener,
                url, mSessionUtil,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                BasicAuthDetected, queue);
    }

    private void GetResultRequest(@Nullable final JSONParserInterface parser,
                                  final String url,
                                  boolean retry) {
        final VolleyErrorListener defaultListener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                try {
                    String jsonString = response.getString(DomoticzValues.Json.Field.RESULT);
                    if (parser != null)
                        parser.parseResult(jsonString);
                } catch (JSONException e) {
                    if (parser != null)
                        parser.onError(e);
                }
            }

            @Override
            public void onError(Exception error) {
                if (parser != null)
                    parser.onError(error);
            }
        };

        VolleyErrorListener listener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                try {
                    String jsonString = response.getString(DomoticzValues.Json.Field.RESULT);
                    if (parser != null)
                        parser.parseResult(jsonString);
                } catch (JSONException e) {
                    if (parser != null)
                        parser.onError(e);
                }
            }

            @Override
            public void onError(Exception error) {
                //retry after login
                checkLogin(new LoginReceiver() {
                    @Override
                    public void OnReceive(LoginInfo mLoginInfo) {
                        RequestUtil.makeJsonGetResultRequest(defaultListener,
                                url, mSessionUtil,
                                getUserCredentials(Authentication.USERNAME),
                                getUserCredentials(Authentication.PASSWORD),
                                BasicAuthDetected, queue);
                    }

                    @Override
                    public void onError(Exception error) {
                        if (parser != null)
                            parser.onError(error);
                    }
                });
            }
        };

        RequestUtil.makeJsonGetResultRequest(retry ? listener : defaultListener,
                url, mSessionUtil,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                BasicAuthDetected, queue);
    }

    private void LoginPostRequest(@Nullable final JSONParserInterface parser,
                              final String url,
                              final Map<String, String> params) {
        final VolleyErrorListener defaultListener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                if (parser != null)
                    parser.parseResult(response!= null ? response.toString() : null);
            }

            @Override
            public void onError(Exception error) {
                if (parser != null)
                    parser.parseResult(null); // Just continue.. we have basic auth failover
            }
        };

        RequestUtil.makeJsonPostRequest(defaultListener,
                url, params, mSessionUtil,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                BasicAuthDetected,
                queue);
    }

    private void PostRequest(@Nullable final JSONParserInterface parser,
                             final String url,
                             final Map<String, String> params,
    boolean retry) {
        final VolleyErrorListener defaultListener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                if (parser != null)
                    parser.parseResult(response!= null ? response.toString() : null);
            }

            @Override
            public void onError(Exception error) {
                if (parser != null)
                    parser.onError(error);
            }
        };

        VolleyErrorListener listener = new VolleyErrorListener() {
            @Override
            public void onDone(JSONObject response) {
                if (parser != null)
                    parser.parseResult(response!= null ? response.toString() : null);
            }

            @Override
            public void onError(Exception error) {
                //retry after login
                checkLogin(new LoginReceiver() {
                    @Override
                    public void OnReceive(LoginInfo mLoginInfo) {
                        RequestUtil.makeJsonPostRequest(defaultListener,
                                url, params, mSessionUtil,
                                getUserCredentials(Authentication.USERNAME),
                                getUserCredentials(Authentication.PASSWORD),
                                BasicAuthDetected, queue);
                    }

                    @Override
                    public void onError(Exception error) {
                        if (parser != null)
                            parser.onError(error);
                    }
                });
            }
        };

        RequestUtil.makeJsonPostRequest(retry ? listener : defaultListener,
                url, params, mSessionUtil,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                BasicAuthDetected,
                queue);
    }

    public interface Authentication {
        String USERNAME = "username";
        String PASSWORD = "password";

        interface Method {
            String AUTH_METHOD_LOGIN_FORM = "Login form";
            String AUTH_METHOD_BASIC_AUTHENTICATION = "Basic authentication";
        }
    }
}
