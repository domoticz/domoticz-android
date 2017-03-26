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

package nl.hnogames.domoticz.Domoticz;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.R;

public class Domoticz {

    @SuppressWarnings("SpellCheckingInspection")
    public static int getDrawableIcon(String imgType, String Type, String switchType, boolean State, boolean useCustomImage, String CustomImage) {
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
                case "Door":
                    return R.drawable.door;
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
    public static int getDrawableIcon(String imgType, String Type, String switchType, boolean State) {
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
                if (switchType != null && switchType.length() > 0 && switchType.equals(Device.Type.Value.DUSKSENSOR))
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
            case "Freezing":
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

    public List<Integer> getWearSupportedSwitchesValues() {
        List<Integer> switchesSupported = new ArrayList<>();
        switchesSupported.add(Device.Type.Value.ON_OFF);
        switchesSupported.add(Device.Type.Value.DIMMER);
        switchesSupported.add(Device.Type.Value.PUSH_ON_BUTTON);
        switchesSupported.add(Device.Type.Value.PUSH_OFF_BUTTON);
        switchesSupported.add(Device.Type.Value.MEDIAPLAYER);
        switchesSupported.add(Device.Type.Value.SMOKE_DETECTOR);
        switchesSupported.add(Device.Type.Value.X10SIREN);
        switchesSupported.add(Device.Type.Value.DOORCONTACT);
        switchesSupported.add(Device.Type.Value.DOORLOCK);
        switchesSupported.add(Device.Type.Value.DOORBELL);
        switchesSupported.add(Device.Type.Value.BLINDINVERTED);
        switchesSupported.add(Device.Type.Value.BLINDPERCENTAGE);
        switchesSupported.add(Device.Type.Value.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(Device.Type.Value.BLINDS);
        switchesSupported.add(Device.Type.Value.BLINDVENETIAN);
        switchesSupported.add(Device.Type.Value.BLINDVENETIANUS);
        switchesSupported.add(Device.Type.Value.MOTION);
        switchesSupported.add(Device.Type.Value.CONTACT);
        switchesSupported.add(Device.Type.Value.DUSKSENSOR);
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
        switchesSupported.add(Device.Type.Name.DOORCONTACT);
        switchesSupported.add(Device.Type.Name.DOORBELL);
        switchesSupported.add(Device.Type.Name.BLINDINVERTED);
        switchesSupported.add(Device.Type.Name.BLINDPERCENTAGE);
        switchesSupported.add(Device.Type.Name.BLINDPERCENTAGEINVERTED);
        switchesSupported.add(Device.Type.Name.BLINDS);
        switchesSupported.add(Device.Type.Name.BLINDVENETIAN);
        switchesSupported.add(Device.Type.Name.BLINDVENETIANUS);
        switchesSupported.add(Device.Type.Name.MOTION);
        switchesSupported.add(Device.Type.Name.CONTACT);
        switchesSupported.add(Device.Type.Name.DUSKSENSOR);
        return switchesSupported;
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
}
