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

import nl.hnogames.domoticzapi.Containers.DevicesInfo;

/**
 * Created by m.heinis on 8/5/2016.
 */
public class DomoticzValues {

    public interface Authentication {
        String USERNAME = "username";
        String PASSWORD = "password";

        interface Method {
            String AUTH_METHOD_LOGIN_FORM = "Login form";
            String AUTH_METHOD_BASIC_AUTHENTICATION = "Basic authentication";
        }
    }

    /**
     * Checks if the device has a stop button
     *
     * @param mDeviceInfo Device to check
     * @return Returns true if the device has a stop button
     */
    public static boolean canHandleStopButton(DevicesInfo mDeviceInfo) {
        return (mDeviceInfo.getSubType().contains("RAEX")) ||
                (mDeviceInfo.getSubType().contains("A-OK")) ||
                (mDeviceInfo.getSubType().contains("Harrison")) ||
                (mDeviceInfo.getSubType().contains("RFY")) ||
                (mDeviceInfo.getSubType().contains("ASA")) ||
                (mDeviceInfo.getSubType().contains("Hasta")) ||
                (mDeviceInfo.getSubType().contains("Media Mount")) ||
                (mDeviceInfo.getSubType().contains("Forest")) ||
                (mDeviceInfo.getSubType().contains("Chamberlain")) ||
                (mDeviceInfo.getSubType().contains("Sunpery")) ||
                (mDeviceInfo.getSubType().contains("Dolat")) ||
                (mDeviceInfo.getSubType().contains("DC106")) ||
                (mDeviceInfo.getSubType().contains("Confexx")) ||
                (mDeviceInfo.getSubType().contains("ASP"));
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

        interface Hardware {
           String EVOHOME = "evohome";
        }

        interface Dimmer {
            interface Action {
                int DIM_LEVEL = 20;
                int COLOR = 21;
            }
        }

        interface Utility {
            interface Type {
                String HEATING = "Heating";
                String THERMOSTAT = "Thermostat";
            }

            interface SubType {
                String TEXT = "Text";
                String ALERT = "Alert";
                String PERCENTAGE = "Percentage";
                String ENERGY = "Energy";
                String KWH = "kWh";
                String GAS = "Gas";
                String ELECTRIC = "Electric";
                String VOLTCRAFT = "Voltcraft";
                String SETPOINT = "SetPoint";
                String YOULESS = "YouLess";
                String SMARTWARES = "Smartwares";
            }
        }

        interface Blind {
            interface State {
                String CLOSED = "Closed";
                String OPEN = "Open";
                String STOPPED = "Stopped";
                String ON = "On";
                String OFF = "Off";
            }

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
                int DOORCONTACT = 11;
                int BLINDPERCENTAGE = 13;
                int BLINDVENETIAN = 15;
                int BLINDVENETIANUS = 14;
                int BLINDINVERTED = 6;
                int BLINDPERCENTAGEINVERTED = 16;
                int SELECTOR = 18;
                int DOORLOCK = 19;
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
                String DOORCONTACT = "Door Contact";
                String BLINDPERCENTAGE = "Blinds Percentage";
                String BLINDVENETIAN = "Venetian Blinds EU";
                String BLINDVENETIANUS = "Venetian Blinds US";
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
            String MESSAGE = "message";
            String ERROR = "ERROR";
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
                int LANGUAGE = 32;
                int SCENELOG = 33;
                int USERS = 34;
                int LOGOFF = 35;
                int AUTH = 36;
                int FAVORITES = 37;
                int UPDATEVAR = 40;
                int IMAGE = 41;
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
                int EVENTS_UPDATE_STATUS = 109;
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

    public interface Url {
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
            String DEVICES = "/json.htm?type=devices&filter=all&used=true";
            String FAVORITES = "/json.htm?type=devices&filter=all&used=true&favorite=1";
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
            String SCENELOG = "/json.htm?type=scenelog&idx=";
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

        @SuppressWarnings({"unused", "SpellCheckingInspection"})
        interface UserVariable {
            String UPDATE = "/json.htm?type=command&param=updateuservariable";
        }

        @SuppressWarnings("SpellCheckingInspection")
        interface System {
            String UPDATE = "/json.htm?type=command&param=checkforupdate&forced=true";
            String USERVARIABLES = "/json.htm?type=command&param=getuservariables";
            String EVENTS = "/json.htm?type=events&param=list";
            String EVENTS_UPDATE_STATUS = "/json.htm?type=events&param=updatestatus&eventid=";
            String RGBCOLOR = "/json.htm?type=command&param=setcolbrightnessvalue&idx=";
            String SETTINGS = "/json.htm?type=settings";
            String CONFIG = "/json.htm?type=command&param=getconfig";
            String SETSECURITY = "/json.htm?type=command&param=setsecstatus";
            String DOWNLOAD_READY = "/json.htm?type=command&param=downloadready";
            String UPDATE_DOMOTICZ_SERVER = "/json.htm?type=command&param=execute_script&scriptname=update_domoticz&direct=true";
            String ADD_MOBILE_DEVICE = "/json.htm?type=command&param=addmobiledevice";
            String CLEAN_MOBILE_DEVICE = "/json.htm?type=command&param=deletemobiledevice";
            String LANGUAGE_TRANSLATIONS = "/i18n/domoticz-";
            String USERS = "/json.htm?type=users";
            String AUTH = "/json.htm?type=command&param=getauth";
            String LOGOFF = "/json.htm?type=command&param=dologout";
        }

        interface Event {
            String ON = "&eventstatus=1";
            String OFF = "&eventstatus=0";
        }
    }

    public interface FavoriteAction {
        String ON = "1";
        String OFF = "0";
    }
}
