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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.hnogames.domoticz.Interfaces.CameraReceiver;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.EventReceiver;
import nl.hnogames.domoticz.Interfaces.EventXmlReceiver;
import nl.hnogames.domoticz.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticz.Interfaces.LogsReceiver;
import nl.hnogames.domoticz.Interfaces.PlansReceiver;
import nl.hnogames.domoticz.Interfaces.ScenesReceiver;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchLogReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchTimerReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.TemperatureReceiver;
import nl.hnogames.domoticz.Interfaces.UpdateReceiver;
import nl.hnogames.domoticz.Interfaces.UserVariablesReceiver;
import nl.hnogames.domoticz.Interfaces.UtilitiesReceiver;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.Interfaces.WeatherReceiver;
import nl.hnogames.domoticz.Interfaces.WifiSSIDListener;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.PhoneConnectionUtil;
import nl.hnogames.domoticz.Utils.RequestUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.VolleyUtil;

public class Domoticz {

    public static final int batteryLevelMax = 100;
    public static final int signalLevelMax = 12;
    public static final int DOMOTICZ_FAKE_ID = 99999;
    public static final String HIDDEN_CHARACTER = "$";

    public static final String UTILITIES_TYPE_THERMOSTAT = "Thermostat";
    /*
    *  Log tag
    */
    private static final String TAG = Domoticz.class.getSimpleName();
    public static boolean debug;
    private final SharedPrefUtil mSharedPrefUtil;
    private final PhoneConnectionUtil mPhoneConnectionUtil;
    Context mContext;

    public Domoticz(Context mContext) {
        this.mContext = mContext;
        mSharedPrefUtil = new SharedPrefUtil(mContext);
        mPhoneConnectionUtil = new PhoneConnectionUtil(mContext, new WifiSSIDListener() {
            @Override
            public void ReceiveSSIDs(CharSequence[] entries) { }
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

        if (mSharedPrefUtil.isLocalServerAddressDifferent()) {
            Set<String> localSsid = mSharedPrefUtil.getLocalSsid();

            if (mPhoneConnectionUtil.isWifiConnected() && localSsid != null) {

                String currentSsid = mPhoneConnectionUtil.getCurrentSsid();

                // Remove quotes from current SSID read out
                currentSsid = currentSsid.substring(1, currentSsid.length() - 1);

                for (String ssid : localSsid) {
                    if (ssid.equals(currentSsid)) userIsLocal = true;
                }
            }
        }

        return userIsLocal;
    }

    public boolean isConnectionDataComplete() {
        boolean result = true;
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", mSharedPrefUtil.getDomoticzLocalUrl());
        stringHashMap.put("Domoticz local port", mSharedPrefUtil.getDomoticzLocalPort());
        stringHashMap.put("Domoticz remote URL", mSharedPrefUtil.getDomoticzRemoteUrl());
        stringHashMap.put("Domoticz remote port", mSharedPrefUtil.getDomoticzRemotePort());

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

    public boolean isUrlValid() {
        boolean result = true;
        HashMap<String, String> stringHashMap = new HashMap<>();
        stringHashMap.put("Domoticz local URL", mSharedPrefUtil.getDomoticzLocalUrl());
        stringHashMap.put("Domoticz remote URL", mSharedPrefUtil.getDomoticzRemoteUrl());

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

        ClipboardManager clipboard =
                (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Domoticz debug data", message);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(mContext, R.string.msg_copiedToClipboard, Toast.LENGTH_SHORT).show();
    }

    private String getJsonGetUrl(int jsonGetUrl) {
        String url;

        switch (jsonGetUrl) {
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

            case Json.Url.Request.SWITCHTIMER:
                url = Url.Category.SWITCHTIMER;
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

            case Json.Url.Request.GRAPH:
                url = Url.Log.GRAPH;
                break;

            default:
                throw new NullPointerException("getJsonGetUrl: No known JSON URL specified");
        }
        return url;
    }

    private String constructGetUrl(int jsonGetUrl) {
        String protocol, url, port, jsonUrl;
        StringBuilder buildUrl = new StringBuilder();
        SharedPrefUtil mSharedPrefUtil = new SharedPrefUtil(mContext);

        if (isUserOnLocalWifi()) {
            if (mSharedPrefUtil.isDomoticzLocalSecure()) protocol = Url.Protocol.HTTPS;
            else protocol = Url.Protocol.HTTP;

            url = mSharedPrefUtil.getDomoticzLocalUrl();
            port = mSharedPrefUtil.getDomoticzLocalPort();

        } else {
            if (mSharedPrefUtil.isDomoticzRemoteSecure()) protocol = Url.Protocol.HTTPS;
            else protocol = Url.Protocol.HTTP;

            url = mSharedPrefUtil.getDomoticzRemoteUrl();
            port = mSharedPrefUtil.getDomoticzRemotePort();

        }
        jsonUrl = getJsonGetUrl(jsonGetUrl);

        String fullString = buildUrl.append(protocol)
                .append(url).append(":")
                .append(port)
                .append(jsonUrl).toString();

        logger("Constructed url: " + fullString);

        return fullString;
    }

    public String constructSetUrl(int jsonSetUrl, int idx, int action, double value) {
        String protocol, baseUrl, url, port, jsonUrl = null, actionUrl;
        StringBuilder buildUrl = new StringBuilder();
        SharedPrefUtil mSharedPrefUtil = new SharedPrefUtil(mContext);
        if (isUserOnLocalWifi()) {
            if (mSharedPrefUtil.isDomoticzLocalSecure()) {
                protocol = Url.Protocol.HTTPS;
            } else protocol = Url.Protocol.HTTP;

            baseUrl = mSharedPrefUtil.getDomoticzLocalUrl();
            port = mSharedPrefUtil.getDomoticzLocalPort();

        } else {
            if (mSharedPrefUtil.isDomoticzRemoteSecure()) {
                protocol = Url.Protocol.HTTPS;
            } else protocol = Url.Protocol.HTTP;
            baseUrl = mSharedPrefUtil.getDomoticzRemoteUrl();
            port = mSharedPrefUtil.getDomoticzRemotePort();
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
                .append(jsonUrl).toString();
        logger("Constructed url: " + fullString);

        return fullString;
    }

    public String getUserCredentials(String credential) {

        if (credential.equals(Authentication.USERNAME)
                || credential.equals(Authentication.PASSWORD)) {

            SharedPrefUtil mSharedPrefUtil = new SharedPrefUtil(mContext);
            String username, password;

            if (isUserOnLocalWifi()) {
                logger("On local wifi");
                username = mSharedPrefUtil.getDomoticzLocalUsername();
                password = mSharedPrefUtil.getDomoticzLocalPassword();
            } else {
                logger("Not on local wifi");
                username = mSharedPrefUtil.getDomoticzRemoteUsername();
                password = mSharedPrefUtil.getDomoticzRemotePassword();
            }
            HashMap<String, String> credentials = new HashMap<>();
            credentials.put(Authentication.USERNAME, username);
            credentials.put(Authentication.PASSWORD, password);

            return credentials.get(credential);
        } else return "";
    }

    public void getVersion(VersionReceiver receiver) {
        VersionParser parser = new VersionParser(receiver);
        String url = constructGetUrl(Json.Url.Request.VERSION);
        RequestUtil.makeJsonVersionRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getUpdate(UpdateReceiver receiver) {
        UpdateParser parser = new UpdateParser(receiver);
        String url = constructGetUrl(Json.Url.Request.UPDATE);
        RequestUtil.makeJsonGetRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getScenes(ScenesReceiver receiver) {
        ScenesParser parser = new ScenesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.SCENES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getPlans(PlansReceiver receiver) {
        PlanParser parser = new PlanParser(receiver);
        String url = constructGetUrl(Json.Url.Request.PLANS);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getCameras(CameraReceiver receiver) {
        CameraParser parser = new CameraParser(receiver);
        String url = constructGetUrl(Json.Url.Request.CAMERAS);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getSwitches(SwitchesReceiver switchesReceiver) {
        SwitchesParser parser = new SwitchesParser(switchesReceiver);
        String url = constructGetUrl(Json.Url.Request.SWITCHES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getSwitchLogs(int idx, SwitchLogReceiver switchesReceiver) {
        SwitchLogParser parser = new SwitchLogParser(switchesReceiver);
        logger("for idx: " + String.valueOf(idx));
        String url = constructGetUrl(Json.Url.Request.SWITCHLOG) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getSwitchTimers(int idx, SwitchTimerReceiver switchesReceiver) {
        SwitchTimerParser parser = new SwitchTimerParser(switchesReceiver);
        logger("for idx: " + String.valueOf(idx));
        String url = constructGetUrl(Json.Url.Request.SWITCHTIMER) + String.valueOf(idx);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void setAction(int idx,
                          int jsonUrl,
                          int jsonAction,
                          double value,
                          setCommandReceiver receiver) {

        setCommandParser parser = new setCommandParser(receiver);
        String url = constructSetUrl(jsonUrl, idx, jsonAction, value);
        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void setRGBColorAction(int idx,
                                  int jsonUrl,
                                  int hue,
                                  int brightness,
                                  setCommandReceiver receiver) {
        setCommandParser parser = new setCommandParser(receiver);
        String url = constructSetUrl(jsonUrl, idx, Device.Dimmer.Action.COLOR, 0);
        url = url.replace("%hue%", String.valueOf(hue)).replace("%bright%", String.valueOf(brightness));
        Log.v(TAG, "Action: " + url);
        RequestUtil.makeJsonPutRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

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
                url);
    }

    public void getStatus(int idx, StatusReceiver receiver) {
        StatusInfoParser parser = new StatusInfoParser(receiver);
        String url = constructGetUrl(Json.Get.STATUS) + String.valueOf(idx);
        logger("for idx: " + String.valueOf(idx));

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getUtilities(UtilitiesReceiver receiver) {
        UtilitiesParser parser = new UtilitiesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.UTILITIES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getTemperatures(TemperatureReceiver receiver) {
        TemperaturesParser parser = new TemperaturesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.TEMPERATURE);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getWeathers(WeatherReceiver receiver) {
        WeatherParser parser = new WeatherParser(receiver);
        String url = constructGetUrl(Json.Url.Request.WEATHER);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getDevices(DevicesReceiver receiver, int plan, String filter) {
        DevicesParser parser = new DevicesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.DEVICES);

        if(filter!=null && filter.length()>0)
            url = url.replace("filter=all", "filter="+filter);
        if (plan > 0)
            url += "&plan=" + plan;

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getDevice(DevicesReceiver receiver, int idx) {
        DevicesParser parser = new DevicesParser(receiver, idx);
        String url = constructGetUrl(Json.Url.Request.DEVICES);

        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }


    public void getLogs(LogsReceiver receiver) {
        LogsParser parser = new LogsParser(receiver);
        String url = constructGetUrl(Json.Url.Request.LOG);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getUserVariables(UserVariablesReceiver receiver) {
        UserVariablesParser parser = new UserVariablesParser(receiver);
        String url = constructGetUrl(Json.Url.Request.USERVARIABLES);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getEvents(EventReceiver receiver) {
        EventsParser parser = new EventsParser(receiver);
        String url = constructGetUrl(Json.Url.Request.EVENTS);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }

    public void getEventXml(int id, EventXmlReceiver receiver) {
        EventsXmlParser parser = new EventsXmlParser(receiver);
        String url = constructGetUrl(Json.Url.Request.EVENTXML);
        url = url.replace("%id%", id + "");
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }


    public void getGraphData(int idx, String range, String type, GraphDataReceiver receiver) {
        GraphDataParser parser = new GraphDataParser(receiver);
        String url = constructGetUrl(Json.Url.Request.GRAPH) + String.valueOf(idx);
        url = url + Url.Log.GRAPH_RANGE + range;
        url = url + Url.Log.GRAPH_TYPE + type;

        Log.i("GRAPH", "url: " + url);
        RequestUtil.makeJsonGetResultRequest(parser,
                getUserCredentials(Authentication.USERNAME),
                getUserCredentials(Authentication.PASSWORD),
                url);
    }


    public int getDrawableIcon(String imgType, String Type , String switchType, boolean State, boolean useCustomImage, String CustomImage) {
        int standardImage = getDrawableIcon(imgType, Type, switchType, State);

        if(useCustomImage && CustomImage!=null && CustomImage.length()>0)
        {
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

    private int getDrawableIcon(String imgType, String Type , String switchType, boolean State) {
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

        interface Type {
            @SuppressWarnings("unused")
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
            }
        }

        interface SubType {
            @SuppressWarnings("unused")
            interface Value {
                int RGB = 1;
            }

            @SuppressWarnings("unused")
            interface Name {
                String RGB = "RGB";
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
            @SuppressWarnings("unused")
            interface Request {
                int DASHBOARD = 1;
                int SCENES = 2;
                int SWITCHES = 3;
                int UTILITIES = 4;
                int TEMPERATURE = 5;
                int WEATHER = 6;
                int CAMERAS = 7;
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
            }

            interface Set {
                int SCENES = 101;
                int SWITCHES = 102;
                int TEMP = 103;
                int FAVORITE = 104;
                int SCENEFAVORITE = 106;
                int EVENT = 105;
                int RGBCOLOR = 107;
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
        }
    }

    public interface Event {
        interface Type {
            String EVENT = "Event";
        }

        interface Action {
            int ON = 55;
            int OFF = 56;
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
            String UTILITIES = ALLDEVICES + "&filter=utility&used=true";
            String PLANS = "/json.htm?type=plans";
            String TEMPERATURE = ALLDEVICES + "&filter=temp&used=true";
            String SWITCHLOG = "/json.htm?type=lightlog&idx=";
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
        }
    }

    private interface FavoriteAction {
        String ON = "1";
        String OFF = "0";
    }
}