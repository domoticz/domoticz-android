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

import nl.hnogames.domoticzapi.Utils.ServerUtil;

/**
 * Created by m.heinis on 8/5/2016.
 */
public class DomoticzUrls {

    private final Domoticz domoticz;

    public DomoticzUrls(Domoticz domoticz) {
        this.domoticz = domoticz;
    }

    public String constructSetUrl(int jsonSetUrl, int idx, int action, double value) {
        String protocol, baseUrl, url, port, directory, jsonUrl = null, actionUrl;
        StringBuilder buildUrl = new StringBuilder();

        if (domoticz.isUserOnLocalWifi()) {
            if (domoticz.getServerUtil().getActiveServer().getLocalServerSecure()) {
                protocol = DomoticzValues.Url.Protocol.HTTPS;
            } else protocol = DomoticzValues.Url.Protocol.HTTP;

            baseUrl = domoticz.getServerUtil().getActiveServer().getLocalServerUrl();
            port = domoticz.getServerUtil().getActiveServer().getLocalServerPort();
            directory = domoticz.getServerUtil().getActiveServer().getLocalServerDirectory();
        } else {
            if (domoticz.getServerUtil().getActiveServer().getRemoteServerSecure()) {
                protocol = DomoticzValues.Url.Protocol.HTTPS;
            } else protocol = DomoticzValues.Url.Protocol.HTTP;
            baseUrl = domoticz.getServerUtil().getActiveServer().getRemoteServerUrl();
            port = domoticz.getServerUtil().getActiveServer().getRemoteServerPort();
            directory = domoticz.getServerUtil().getActiveServer().getRemoteServerDirectory();
        }

        switch (action) {
            case DomoticzValues.Scene.Action.ON:
                actionUrl = DomoticzValues.Url.Action.ON;
                break;

            case DomoticzValues.Scene.Action.OFF:
                actionUrl =DomoticzValues. Url.Action.OFF;
                break;

            case DomoticzValues.Device.Switch.Action.ON:
                actionUrl = DomoticzValues.Url.Action.ON;
                break;

            case DomoticzValues.Device.Switch.Action.OFF:
                actionUrl = DomoticzValues.Url.Action.OFF;
                break;

            case DomoticzValues.Device.Blind.Action.UP:
                actionUrl =DomoticzValues. Url.Action.UP;
                break;

            case DomoticzValues.Device.Blind.Action.STOP:
                actionUrl = DomoticzValues.Url.Action.STOP;
                break;

            case DomoticzValues.Device.Blind.Action.DOWN:
                actionUrl = DomoticzValues.Url.Action.DOWN;
                break;

            case DomoticzValues.Device.Blind.Action.ON:
                actionUrl = DomoticzValues.Url.Action.ON;
                break;

            case DomoticzValues.Device.Blind.Action.OFF:
                actionUrl = DomoticzValues.Url.Action.OFF;
                break;

            case DomoticzValues.Device.Thermostat.Action.MIN:
                actionUrl = String.valueOf(value);
                break;

            case DomoticzValues.Device.Thermostat.Action.PLUS:
                actionUrl = String.valueOf(value);
                break;

            case DomoticzValues.Device.Favorite.ON:
                actionUrl = DomoticzValues.FavoriteAction.ON;
                break;

            case DomoticzValues.Device.Favorite.OFF:
                actionUrl = DomoticzValues.FavoriteAction.OFF;
                break;

            case DomoticzValues.Device.Dimmer.Action.DIM_LEVEL:
                actionUrl = DomoticzValues.Url.Switch.DIM_LEVEL + String.valueOf(value);
                break;

            case DomoticzValues.Device.Dimmer.Action.COLOR:
                actionUrl = DomoticzValues.Url.Switch.COLOR;
                break;

            case DomoticzValues.Device.ModalSwitch.Action.AUTO:
                actionUrl = DomoticzValues.Url.ModalAction.AUTO;
                break;

            case DomoticzValues.Device.ModalSwitch.Action.ECONOMY:
                actionUrl = DomoticzValues.Url.ModalAction.ECONOMY;
                break;

            case DomoticzValues.Device.ModalSwitch.Action.AWAY:
                actionUrl = DomoticzValues.Url.ModalAction.AWAY;
                break;

            case DomoticzValues.Device.ModalSwitch.Action.DAY_OFF:
                actionUrl = DomoticzValues.Url.ModalAction.DAY_OFF;
                break;

            case DomoticzValues.Device.ModalSwitch.Action.CUSTOM:
                actionUrl = DomoticzValues.Url.ModalAction.CUSTOM;
                break;

            case DomoticzValues.Device.ModalSwitch.Action.HEATING_OFF:
                actionUrl = DomoticzValues.Url.ModalAction.HEATING_OFF;
                break;

            case DomoticzValues.Event.Action.ON:
                actionUrl = DomoticzValues.Url.Event.ON;
                break;

            case DomoticzValues.Event.Action.OFF:
                actionUrl = DomoticzValues.Url.Event.OFF;
                break;

            default:
                throw new NullPointerException(
                        "Action not found in method Domoticz.constructSetUrl");
        }

        switch (jsonSetUrl) {
            case DomoticzValues.Json.Url.Set.SCENES:
                url = DomoticzValues.Url.Scene.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + DomoticzValues.Url.Switch.CMD + actionUrl;
                break;

            case DomoticzValues.Json.Url.Set.SWITCHES:
                url = DomoticzValues.Url.Switch.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + DomoticzValues.Url.Switch.CMD + actionUrl;
                break;

            case DomoticzValues.Json.Url.Set.MODAL_SWITCHES:
                url = DomoticzValues.Url.ModalSwitch.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + DomoticzValues.Url.ModalSwitch.STATUS + actionUrl;
                break;
            case DomoticzValues.Json.Url.Set.TEMP:
                url = DomoticzValues.Url.Temp.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + DomoticzValues.Url.Temp.VALUE + actionUrl;
                break;

            case DomoticzValues.Json.Url.Set.SCENEFAVORITE:
                url = DomoticzValues.Url.Favorite.SCENE;
                jsonUrl = url
                        + String.valueOf(idx)
                        + DomoticzValues.Url.Favorite.VALUE + actionUrl;
                break;

            case DomoticzValues.Json.Url.Set.FAVORITE:
                url = DomoticzValues.Url.Favorite.GET;
                jsonUrl = url
                        + String.valueOf(idx)
                        + DomoticzValues.Url.Favorite.VALUE + actionUrl;
                break;

            case DomoticzValues.Json.Url.Set.RGBCOLOR:
                url = DomoticzValues.Url.System.RGBCOLOR;
                jsonUrl = url
                        + String.valueOf(idx)
                        + actionUrl;
                break;

            case DomoticzValues.Json.Url.Set.EVENTS_UPDATE_STATUS:
                url = DomoticzValues.Url.System.EVENTS_UPDATE_STATUS;
                jsonUrl = url
                        + String.valueOf(idx)
                        + actionUrl;
                break;

        }

        String fullString = buildUrl.append(protocol)
                .append(baseUrl)
                .append(!port.equals("80") ? ":" + port: "")
                .append(directory.isEmpty() ? "" : "/" + directory)
                .append(jsonUrl).toString();

        return fullString;
    }

    public String constructGetUrl(int jsonGetUrl) {
        return constructGetUrl(jsonGetUrl, false, null, null);
    }

    public String constructGetUrl(int jsonGetUrl, boolean withPass, String username, String password) {
        if (domoticz == null)
            return null;

        ServerUtil mServerUtil = domoticz.getServerUtil();
        String protocol, url, port, directory, jsonUrl;
        StringBuilder buildUrl = new StringBuilder();
        if (domoticz.isUserOnLocalWifi()) {
            if (mServerUtil.getActiveServer().getLocalServerSecure())
                protocol = DomoticzValues.Url.Protocol.HTTPS;
            else protocol = DomoticzValues.Url.Protocol.HTTP;

            url = mServerUtil.getActiveServer().getLocalServerUrl();
            port = mServerUtil.getActiveServer().getLocalServerPort();
            directory = mServerUtil.getActiveServer().getLocalServerDirectory();
        } else {
            if (mServerUtil.getActiveServer().getRemoteServerSecure())
                protocol = DomoticzValues.Url.Protocol.HTTPS;
            else protocol = DomoticzValues.Url.Protocol.HTTP;

            url = mServerUtil.getActiveServer().getRemoteServerUrl();
            port = mServerUtil.getActiveServer().getRemoteServerPort();
            directory = mServerUtil.getActiveServer().getRemoteServerDirectory();
        }
        jsonUrl = getJsonGetUrl(jsonGetUrl);

        if(!withPass) {
            return buildUrl.append(protocol)
                    .append(url)
                    .append(!port.equals("80") ? ":" + port: "")
                    .append(directory.isEmpty() ? "" : "/" + directory)
                    .append(jsonUrl).toString();
        }
        else{
            return buildUrl.append(protocol)
                    .append(username).append(":").append(password).append("@")
                    .append(url)
                    .append(!port.equals("80") ? ":" + port: "")
                    .append(directory.isEmpty() ? "" : "/" + directory)
                    .append(jsonUrl).toString();
        }
    }

    public String getJsonGetUrl(int jsonGetUrl) {
        String url;

        switch (jsonGetUrl) {
            case DomoticzValues.Json.Url.Request.LANGUAGE:
                url = DomoticzValues.Url.System.LANGUAGE_TRANSLATIONS;
                break;
            case DomoticzValues.Json.Url.Request.UPDATE_DOMOTICZ_SERVER:
                url = DomoticzValues.Url.System.UPDATE_DOMOTICZ_SERVER;
                break;
            case DomoticzValues.Json.Url.Request.UPDATE_DOWNLOAD_READY:
                url = DomoticzValues.Url.System.DOWNLOAD_READY;
                break;
            case DomoticzValues.Json.Url.Request.VERSION:
                url = DomoticzValues.Url.Category.VERSION;
                break;
            case DomoticzValues.Json.Url.Request.LOG:
                url = DomoticzValues.Url.Log.GET_LOG;
                break;

            case DomoticzValues.Json.Url.Request.DASHBOARD:
                url = DomoticzValues.Url.Category.DASHBOARD;
                break;

            case DomoticzValues.Json.Url.Request.SCENES:
                url = DomoticzValues.Url.Category.SCENES;
                break;

            case DomoticzValues.Json.Url.Request.SWITCHES:
                url = DomoticzValues.Url.Category.SWITCHES;
                break;

            case DomoticzValues.Json.Url.Request.UTILITIES:
                url = DomoticzValues.Url.Category.UTILITIES;
                break;

            case DomoticzValues.Json.Url.Request.TEMPERATURE:
                url = DomoticzValues.Url.Category.TEMPERATURE;
                break;

            case DomoticzValues.Json.Url.Request.WEATHER:
                url = DomoticzValues.Url.Category.WEATHER;
                break;

            case DomoticzValues.Json.Url.Request.CAMERAS:
                url = DomoticzValues.Url.Category.CAMERAS;
                break;

            case DomoticzValues.Json.Url.Request.CAMERA:
                url = DomoticzValues.Url.Category.CAMERA;
                break;

            case DomoticzValues.Json.Url.Request.DEVICES:
                url = DomoticzValues.Url.Category.DEVICES;
                break;

            case DomoticzValues.Json.Get.STATUS:
                url = DomoticzValues.Url.Device.STATUS;
                break;

            case DomoticzValues.Json.Url.Request.PLANS:
                url = DomoticzValues.Url.Category.PLANS;
                break;

            case DomoticzValues.Json.Url.Request.SWITCHLOG:
                url = DomoticzValues.Url.Category.SWITCHLOG;
                break;

            case DomoticzValues.Json.Url.Request.TEXTLOG:
                url = DomoticzValues.Url.Category.TEXTLOG;
                break;

            case DomoticzValues.Json.Url.Request.SCENELOG:
                url = DomoticzValues.Url.Category.SCENELOG;
                break;

            case DomoticzValues.Json.Url.Request.SWITCHTIMER:
                url = DomoticzValues.Url.Category.SWITCHTIMER;
                break;

            case DomoticzValues.Json.Url.Request.SETSECURITY:
                url = DomoticzValues.Url.System.SETSECURITY;
                break;

            case DomoticzValues.Json.Url.Request.UPDATE:
                url = DomoticzValues.Url.System.UPDATE;
                break;

            case DomoticzValues.Json.Url.Request.USERVARIABLES:
                url = DomoticzValues.Url.System.USERVARIABLES;
                break;

            case DomoticzValues.Json.Url.Request.EVENTS:
                url = DomoticzValues.Url.System.EVENTS;
                break;

            case DomoticzValues.Json.Url.Request.USERS:
                url = DomoticzValues.Url.System.USERS;
                break;

            case DomoticzValues.Json.Url.Request.AUTH:
                url = DomoticzValues.Url.System.AUTH;
                break;

            case DomoticzValues.Json.Url.Request.LOGOFF:
                url = DomoticzValues.Url.System.LOGOFF;
                break;

            case DomoticzValues.Json.Url.Request.SETTINGS:
                url = DomoticzValues.Url.System.SETTINGS;
                break;

            case DomoticzValues.Json.Url.Request.CONFIG:
                url = DomoticzValues.Url.System.CONFIG;
                break;

            case DomoticzValues.Json.Url.Request.GRAPH:
                url = DomoticzValues.Url.Log.GRAPH;
                break;

            case DomoticzValues.Json.Url.Request.ADD_MOBILE_DEVICE:
                url = DomoticzValues.Url.System.ADD_MOBILE_DEVICE;
                break;

            case DomoticzValues.Json.Url.Request.CLEAN_MOBILE_DEVICE:
                url = DomoticzValues.Url.System.CLEAN_MOBILE_DEVICE;
                break;

            case DomoticzValues.Json.Url.Request.SET_DEVICE_USED:
                url = DomoticzValues.Url.Device.SET_USED;
                break;

            case DomoticzValues.Json.Url.Request.NOTIFICATIONS:
                url = DomoticzValues.Url.Notification.NOTIFICATION;
                break;

            case DomoticzValues.Json.Url.Request.FAVORITES:
                url = DomoticzValues.Url.Category.FAVORITES;
                break;

            case DomoticzValues.Json.Url.Request.UPDATEVAR:
                url = DomoticzValues.Url.UserVariable.UPDATE;
                break;

            default:
                throw new NullPointerException("getJsonGetUrl: No known JSON URL specified");
        }
        return url;
    }
}
