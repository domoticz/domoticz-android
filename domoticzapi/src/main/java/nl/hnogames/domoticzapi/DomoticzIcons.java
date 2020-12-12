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

import android.os.Build;
import android.service.controls.DeviceTypes;

import androidx.annotation.RequiresApi;

/**
 * Created by m.heinis on 8/5/2016.
 */
public class DomoticzIcons {

    @SuppressWarnings("SpellCheckingInspection")
    public static int getDrawableIcon(String imgType, String Type, String switchType, boolean State, boolean useCustomImage, String CustomImage) {
        int standardImage = getDrawableIcon(imgType, Type, switchType, State);

        if (useCustomImage && CustomImage != null && CustomImage.length() > 0) {
            switch (CustomImage) {
                case "Alarm":
                    return R.drawable.alarm;
                case "Freezing":
                    return R.drawable.freezing;
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
                case "GoogleDevsHomeMini":
                    return R.drawable.ghome;
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
                if (switchType != null && switchType.length() > 0 && switchType.equals(DomoticzValues.Device.Type.Name.DUSKSENSOR))
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
                if (switchType != null && switchType.length() > 0 && switchType.startsWith(DomoticzValues.Device.SubType.Name.RGB))
                    return R.drawable.rgb;
                else
                    return R.drawable.dimmer;
            case "motion":
                return R.drawable.motion;
            case "security":
                return R.drawable.security;
            case "temperature":
            case "override_mini":
                if (State)
                    return R.drawable.heating;
                else
                    return R.drawable.cooling;
            case "counter":
                if (Type != null && Type.length() > 0 && Type.contains("Smart Meter")) {
                    if(switchType != null && switchType.length() > 0 && switchType.contains("Gas"))
                        return R.drawable.gas;
                    else
                        return R.drawable.meter;
                }
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
            case "scale":
                return R.drawable.scale;
            case "lux":
                return R.drawable.uvsunny;
        }
        switch (Type.toLowerCase()) {
            case "heating":
                return R.drawable.heating;
            case "thermostat":
                return R.drawable.flame;
        }
        return test;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressWarnings("SpellCheckingInspection")
    public static int getDrawableIconForGoogle(String imgType, String Type, String switchType, boolean State, boolean useCustomImage, String CustomImage) {
        int standardImage = getDrawableIconForGoogle(imgType, Type, switchType, State);
        if (useCustomImage && CustomImage != null && CustomImage.length() > 0) {
            switch (CustomImage) {
                case "Alarm":
                    return DeviceTypes.TYPE_GENERIC_ARM_DISARM;
                case "Freezing":
                    return DeviceTypes.TYPE_GENERIC_TEMP_SETTING;
                case "Amplifier":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "Computer":
                case "ComputerPC":
                    return DeviceTypes.TYPE_DISPLAY;
                case "Cooling":
                    return DeviceTypes.TYPE_GENERIC_TEMP_SETTING;
                case "ChristmasTree":
                    return DeviceTypes.TYPE_LIGHT;
                case "Door":
                    return DeviceTypes.TYPE_DOOR;
                case "Fan":
                    return DeviceTypes.TYPE_FAN;
                case "Fireplace":
                    return DeviceTypes.TYPE_HEATER;
                case "Generic":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "Harddisk":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "Heating":
                    return DeviceTypes.TYPE_HEATER;
                case "Light":
                    return DeviceTypes.TYPE_LIGHT;
                case "Media":
                    return DeviceTypes.TYPE_TV;
                case "Phone":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "Speaker":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "Printer":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "TV":
                    return DeviceTypes.TYPE_TV;
                case "WallSocket":
                    return DeviceTypes.TYPE_OUTLET;
                case "Water":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "GoogleDevsHomeMini":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
            }
        }

        return standardImage;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @SuppressWarnings("SpellCheckingInspection")
    public static int getDrawableIconForGoogle(String imgType, String Type, String switchType, boolean State) {
        int test = R.drawable.defaultimage;
        switch (imgType.toLowerCase()) {
            case "scene":
                return DeviceTypes.TYPE_LIGHT;
            case "group":
                return DeviceTypes.TYPE_LIGHT;
            case "wind":
                return DeviceTypes.TYPE_GENERIC_TEMP_SETTING;
            case "doorbell":
                return DeviceTypes.TYPE_DOORBELL;
            case "door":
                return DeviceTypes.TYPE_DOOR;
            case "lightbulb":
                return DeviceTypes.TYPE_LIGHT;
            case "push":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "pushoff":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "siren":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "smoke":
            return DeviceTypes.TYPE_UNKNOWN;
            case "uv":
                return DeviceTypes.TYPE_UNKNOWN;
            case "contact":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "logitechMediaServer":
                return DeviceTypes.TYPE_TV;
            case "media":
                return DeviceTypes.TYPE_TV;
            case "blinds":
                return DeviceTypes.TYPE_BLINDS;
            case "dimmer":
                return DeviceTypes.TYPE_LIGHT;
            case "motion":
                return DeviceTypes.TYPE_UNKNOWN;
            case "security":
                return DeviceTypes.TYPE_SECURITY_SYSTEM;
            case "temperature":
            case "override_mini":
                return DeviceTypes.TYPE_HEATER;
            case "counter":
                return DeviceTypes.TYPE_UNKNOWN;
            case "visibility":
                return DeviceTypes.TYPE_UNKNOWN;
            case "radiation":
                return DeviceTypes.TYPE_UNKNOWN;
            case "moisture":
            case "rain":
                return DeviceTypes.TYPE_UNKNOWN;
            case "leaf":
                return DeviceTypes.TYPE_UNKNOWN;
            case "hardware":
                return DeviceTypes.TYPE_DISPLAY;
            case "fan":
                return DeviceTypes.TYPE_FAN;
            case "speaker":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "current":
                return DeviceTypes.TYPE_OUTLET;
            case "text":
                return DeviceTypes.TYPE_UNKNOWN;
            case "alert":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "gauge":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "clock":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "mode":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "utility":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "scale":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "lux":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
        }
        switch (Type.toLowerCase()) {
            case "heating":
                return DeviceTypes.TYPE_HEATER;
            case "thermostat":
                return DeviceTypes.TYPE_THERMOSTAT;
        }
        return test;
    }
}
