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

package nl.hnogames.domoticz.Utils;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.util.UUID;

import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;

public class DeviceUtils {

    //Check is a Domoticz Device is toggable by NFC, Geofences or QRCodes
    public static boolean isAutomatedToggableDevice(DevicesInfo mDeviceInfo) {
        if (mDeviceInfo.equals(null))
            return false;
        if (mDeviceInfo.getSwitchTypeVal() == 0 &&
                (mDeviceInfo.getSwitchType() == null)) {
            if (mDeviceInfo.getSubType() != null && mDeviceInfo.getSubType().equals(DomoticzValues.Device.Utility.SubType.SMARTWARES)) {
                return true;
            } else {
                switch (mDeviceInfo.getType()) {
                    case DomoticzValues.Scene.Type.GROUP:
                        return true;
                    case DomoticzValues.Scene.Type.SCENE:
                        return true;
                    case DomoticzValues.Device.Utility.Type.THERMOSTAT:
                        return false;
                    case DomoticzValues.Device.Utility.Type.HEATING:
                        return false;
                    default:
                        return false;
                }
            }
        } else if ((mDeviceInfo.getSwitchType() == null)) {
            return false;
        } else {
            switch (mDeviceInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                case DomoticzValues.Device.Type.Value.DOORLOCK:
                case DomoticzValues.Device.Type.Value.DOORCONTACT:
                    switch (mDeviceInfo.getSwitchType()) {
                        case DomoticzValues.Device.Type.Name.SECURITY:
                            return false;
                        default:
                            return true;
                    }
                case DomoticzValues.Device.Type.Value.X10SIREN:
                case DomoticzValues.Device.Type.Value.MOTION:
                case DomoticzValues.Device.Type.Value.CONTACT:
                case DomoticzValues.Device.Type.Value.DUSKSENSOR:
                case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                case DomoticzValues.Device.Type.Value.DOORBELL:
                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                case DomoticzValues.Device.Type.Value.DIMMER:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                case DomoticzValues.Device.Type.Value.SELECTOR:
                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                    return true;
                default:
                    return false;
            }
        }
    }

    /**
     * get Unique ID for this device
     *
     * @param context
     * @return UUID (unique device id)
     */
    public static String getUniqueID(Context context) {
        if (PermissionsUtil.canAccessDeviceState(context)) {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            final String tmDevice, tmSerial, androidId;
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

            UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            return deviceUuid.toString();
        } else
            return null;
    }
}