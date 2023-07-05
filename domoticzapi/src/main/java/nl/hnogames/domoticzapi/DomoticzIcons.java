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
                case "Irrigation":
                    return R.drawable.drop;
                case "Alarm":
                    return R.drawable.alarm;
                case "Freezing":
                    return R.drawable.cold;
                case "Amplifier":
                    return R.drawable.loudspeakers;
                case "Computer":
                case "ComputerPC":
                    return R.drawable.computer;
                case "Cooling":
                    return R.drawable.air_conditioning_indoor;
                case "Door":
                    return R.drawable.door_handle;
                case "Fan":
                    return R.drawable.ventilation;
                case "Generic":
                    return R.drawable.generic;
                case "Heating":
                    return R.drawable.heater;
                case "Light":
                    return R.drawable.bulb;
                case "Media":
                    return R.drawable.media;
                case "Phone":
                    return R.drawable.smartphone;
                case "Speaker":
                    return R.drawable.loudspeakers;
                case "TV":
                    return R.drawable.tv;
                case "WallSocket":
                    return R.drawable.socket_f;
                case "Water":
                    return R.drawable.drop;
                case "Printer":
                    return R.drawable.printer;
                case "GoogleDevsHomeMini":
                    return R.drawable.google_home;
                case "Harddisk":
                    return R.drawable.harddisk;
                case "Fireplace":
                    return R.drawable.flame;
                case "ChristmasTree":
                    return R.drawable.christmas;
            }

            if (CustomImage.contains("robot-vacuum"))
                return R.drawable.vacuum_robot;
        }

        return standardImage;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static int getDrawableIcon(String imgType, String Type, String switchType, boolean State) {
        int test = R.drawable.defaultimage;
        switch (imgType.toLowerCase()) {
            case "scene":
            case "push":
            case "pushoff":
            case "group":
                return R.drawable.power_button;
            case "wind":
                return R.drawable.weather;
            case "doorbell":
                return R.drawable.doorbell;
            case "irrigation":
                return R.drawable.drop;
            case "door":
                return R.drawable.door_handle;
            case "lightbulb":
                if (switchType != null && switchType.length() > 0 && switchType.equals(DomoticzValues.Device.Type.Name.DUSKSENSOR))
                    if (State)
                        return R.drawable.sun;
                    else
                        return R.drawable.sun;
                else
                    return R.drawable.bulb;
            case "siren":
                return R.drawable.loudspeakers;
            case "smoke":
                return R.drawable.smoke;
            case "uv":
                return R.drawable.sun;
            case "contact":
                return R.drawable.socket_f;
            case "current":
                return R.drawable.power;
            case "logitechMediaServer":
            case "media":
                return R.drawable.media;
            case "blinds":
                return R.drawable.down;
            case "dimmer":
                if (switchType != null && switchType.length() > 0 && switchType.startsWith(DomoticzValues.Device.SubType.Name.RGB))
                    return R.drawable.bulb;
                else
                    return R.drawable.bulb;
            case "motion":
                return R.drawable.eye;
            case "security":
                return R.drawable.key;
            case "override_mini":
                if (State)
                    return R.drawable.heater;
                else
                    return R.drawable.air_conditioning_indoor;
            case "temperature":
                return R.drawable.temperature;
            case "counter":
                if (Type != null && Type.length() > 0 && Type.contains("Smart Meter")) {
                    if (switchType != null && switchType.length() > 0 && switchType.contains("Gas"))
                        return R.drawable.electric_range;
                    else
                        return R.drawable.power;
                } else
                    return R.drawable.up;
            case "visibility":
                return R.drawable.eye;
            case "radiation":
                return R.drawable.power;
            case "moisture":
            case "rain":
                return R.drawable.drop;
            case "leaf":
                return R.drawable.houseplant;
            case "hardware":
                return R.drawable.computer;
            case "fan":
                return R.drawable.ventilation;
            case "speaker":
                return R.drawable.loudspeakers;
            case "text":
                return R.drawable.computer;
            case "alert":
                return R.drawable.loudspeakers;
            case "gauge":
            case "mode":
                return R.drawable.thermostat;
            case "clock":
                return R.drawable.clock_b;
            case "utility":
            case "scale":
                return R.drawable.solar_panel;
            case "lux":
                return R.drawable.sun;
        }
        switch (Type.toLowerCase()) {
            case "heating":
                return R.drawable.heater;
            case "thermostat":
                return R.drawable.thermostat;
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
                case "Cooling":
                    return DeviceTypes.TYPE_GENERIC_TEMP_SETTING;
                case "Amplifier":
                case "GoogleDevsHomeMini":
                case "Water":
                case "Printer":
                case "Speaker":
                case "Phone":
                case "Harddisk":
                case "Generic":
                    return DeviceTypes.TYPE_GENERIC_ON_OFF;
                case "Computer":
                case "ComputerPC":
                    return DeviceTypes.TYPE_DISPLAY;
                case "ChristmasTree":
                case "Light":
                    return DeviceTypes.TYPE_LIGHT;
                case "Door":
                    return DeviceTypes.TYPE_DOOR;
                case "Fan":
                    return DeviceTypes.TYPE_FAN;
                case "Fireplace":
                case "Heating":
                    return DeviceTypes.TYPE_HEATER;
                case "Media":
                case "TV":
                    return DeviceTypes.TYPE_TV;
                case "WallSocket":
                    return DeviceTypes.TYPE_OUTLET;
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
            case "dimmer":
            case "lightbulb":
            case "group":
                return DeviceTypes.TYPE_LIGHT;
            case "wind":
                return DeviceTypes.TYPE_GENERIC_TEMP_SETTING;
            case "doorbell":
                return DeviceTypes.TYPE_DOORBELL;
            case "door":
                return DeviceTypes.TYPE_DOOR;
            case "push":
            case "lux":
            case "scale":
            case "utility":
            case "mode":
            case "clock":
            case "gauge":
            case "alert":
            case "speaker":
            case "contact":
            case "siren":
            case "pushoff":
                return DeviceTypes.TYPE_GENERIC_ON_OFF;
            case "smoke":
            case "text":
            case "leaf":
            case "moisture":
            case "rain":
            case "radiation":
            case "visibility":
            case "counter":
            case "motion":
            case "uv":
                return DeviceTypes.TYPE_UNKNOWN;
            case "logitechMediaServer":
            case "media":
                return DeviceTypes.TYPE_TV;
            case "blinds":
                return DeviceTypes.TYPE_BLINDS;
            case "security":
                return DeviceTypes.TYPE_SECURITY_SYSTEM;
            case "temperature":
            case "override_mini":
                return DeviceTypes.TYPE_HEATER;
            case "hardware":
                return DeviceTypes.TYPE_DISPLAY;
            case "fan":
                return DeviceTypes.TYPE_FAN;
            case "current":
                return DeviceTypes.TYPE_OUTLET;
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
