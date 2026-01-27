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

package nl.hnogames.domoticz.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

import java.util.ArrayList;

/**
 * BroadcastReceiver for handling notification action buttons
 * Allows toggling devices directly from notification actions
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionRcvr";

    public static final String ACTION_TOGGLE_DEVICE = "nl.hnogames.domoticz.NOTIFICATION_TOGGLE_DEVICE";
    public static final String ACTION_BLIND_STOP = "nl.hnogames.domoticz.NOTIFICATION_BLIND_STOP";
    public static final String EXTRA_DEVICE_IDX = "DEVICE_IDX";
    public static final String EXTRA_NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }

        String action = intent.getAction();
        int deviceIdx = intent.getIntExtra(EXTRA_DEVICE_IDX, -1);
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        Log.d(TAG, "Received action: " + action + ", deviceIdx: " + deviceIdx);

        if (deviceIdx == -1) {
            Log.w(TAG, "Invalid device idx");
            return;
        }

        if (ACTION_TOGGLE_DEVICE.equals(action)) {
            toggleDevice(context, deviceIdx, notificationId);
        } else if (ACTION_BLIND_STOP.equals(action)) {
            stopBlind(context, deviceIdx, notificationId);
        }
    }

    /**
     * Toggle a device on/off
     */
    private void toggleDevice(Context context, int deviceIdx, int notificationId) {
        Log.d(TAG, "Toggling device idx: " + deviceIdx);

        // Fetch device info first
        StaticHelper.getDomoticz(context).getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevice(DevicesInfo device) {
                if (device == null) {
                    Log.w(TAG, "Device not found for idx: " + deviceIdx);
                    showToast(context, "Device not found");
                    return;
                }

                Log.d(TAG, "Device found: " + device.getName() + ", type: " + device.getType() +
                        ", switchType: " + device.getSwitchTypeVal());

                // Check if blind device
                if (isBlindDevice(device.getSwitchTypeVal())) {
                    toggleBlindDevice(context, device, notificationId);
                } else {
                    toggleStandardDevice(context, device, notificationId);
                }
            }

            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                // Not used
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error fetching device info", error);
                showToast(context, "Error: " + error.getMessage());
            }
        }, deviceIdx, false);
    }

    /**
     * Toggle a standard device (switch, light, scene, etc.)
     */
    private void toggleStandardDevice(Context context, DevicesInfo device, int notificationId) {
        int idx = device.getIdx();
        int switchType = device.getSwitchTypeVal();
        boolean currentState = device.getStatusBoolean();
        boolean newState = !currentState;

        Log.d(TAG, "Toggling standard device: " + device.getName() +
                " from " + currentState + " to " + newState);

        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        // Determine the correct action based on device type (from widget logic)
        if (device.getType().equals(DomoticzValues.Scene.Type.GROUP) ||
                device.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            // Groups and Scenes
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            jsonAction = newState ? DomoticzValues.Scene.Action.ON : DomoticzValues.Scene.Action.OFF;
        } else if (switchType == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
            // Inverted logic for inverted door locks
            jsonAction = newState ? DomoticzValues.Device.Switch.Action.OFF
                                 : DomoticzValues.Device.Switch.Action.ON;
        } else if (switchType == DomoticzValues.Device.Type.Value.SELECTOR) {
            // Selector switches - toggle between off (0) and first level (10)
            jsonAction = currentState ? DomoticzValues.Device.Switch.Action.OFF
                                     : DomoticzValues.Device.Switch.Action.ON;
        } else {
            // Standard switches, dimmers, etc.
            jsonAction = newState ? DomoticzValues.Device.Switch.Action.ON
                                 : DomoticzValues.Device.Switch.Action.OFF;
        }

        performAction(context, idx, jsonUrl, jsonAction, device.getName(), "Toggled", notificationId);
    }

    /**
     * Toggle a blind device
     */
    private void toggleBlindDevice(Context context, DevicesInfo device, int notificationId) {
        String status = device.getStatus();
        int idx = device.getIdx();
        int switchType = device.getSwitchTypeVal();
        int jsonAction;

        Log.d(TAG, "Toggling blind device: " + device.getName() + ", status: " + status);

        // Different blind types use different action values
        boolean useBlindActions = (switchType == DomoticzValues.Device.Type.Value.BLINDVENETIAN ||
                switchType == DomoticzValues.Device.Type.Value.BLINDVENETIANUS ||
                switchType == DomoticzValues.Device.Type.Value.BLINDSTOP ||
                switchType == DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP);

        if (useBlindActions) {
            // Use Blind.Action.OPEN/CLOSE for these types
            if (status != null && (status.equals(DomoticzValues.Device.Blind.State.OPEN) ||
                    status.equals(DomoticzValues.Device.Blind.State.ON) ||
                    status.contains("Open"))) {
                jsonAction = DomoticzValues.Device.Blind.Action.CLOSE;
            } else {
                jsonAction = DomoticzValues.Device.Blind.Action.OPEN;
            }
        } else {
            // Use Switch.Action.ON/OFF for BLINDS and BLINDPERCENTAGE
            boolean currentState = device.getStatusBoolean();
            jsonAction = currentState ? DomoticzValues.Device.Switch.Action.OFF
                                     : DomoticzValues.Device.Switch.Action.ON;
        }

        performAction(context, idx, DomoticzValues.Json.Url.Set.SWITCHES, jsonAction,
                     device.getName(), "Toggled", notificationId);
    }

    /**
     * Stop a blind device
     */
    private void stopBlind(Context context, int deviceIdx, int notificationId) {
        Log.d(TAG, "Stopping blind idx: " + deviceIdx);

        // Fetch device info first
        StaticHelper.getDomoticz(context).getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevice(DevicesInfo device) {
                if (device == null) {
                    Log.w(TAG, "Device not found for idx: " + deviceIdx);
                    showToast(context, "Device not found");
                    return;
                }

                performAction(context, device.getIdx(), DomoticzValues.Json.Url.Set.SWITCHES,
                        DomoticzValues.Device.Blind.Action.STOP, device.getName(), "Stopped", notificationId);
            }

            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                // Not used
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Error fetching device info", error);
                showToast(context, "Error: " + error.getMessage());
            }
        }, deviceIdx, false);
    }

    /**
     * Perform the actual device action
     */
    private void performAction(Context context, int idx, int jsonUrl, int jsonAction,
                              String deviceName, String actionDescription, int notificationId) {
        Log.d(TAG, "Performing action on device: " + deviceName + ", idx: " + idx +
                ", url: " + jsonUrl + ", action: " + jsonAction);

        StaticHelper.getDomoticz(context).setAction(
                idx, jsonUrl, jsonAction, 0, null,
                new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        Log.d(TAG, "Action successful for device: " + deviceName + ", result: " + result);
                        showToast(context, actionDescription + " " + deviceName);

                        // Dismiss the notification after successful action
                        if (notificationId != -1) {
                            dismissNotification(context, notificationId);
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Action failed for device: " + deviceName, error);
                        showToast(context, "Failed to toggle " + deviceName);
                    }
                });
    }

    /**
     * Check if device is a blind type
     */
    private boolean isBlindDevice(int switchType) {
        return switchType == DomoticzValues.Device.Type.Value.BLINDS ||
                switchType == DomoticzValues.Device.Type.Value.BLINDSTOP ||
                switchType == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
                switchType == DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP ||
                switchType == DomoticzValues.Device.Type.Value.BLINDVENETIAN ||
                switchType == DomoticzValues.Device.Type.Value.BLINDVENETIANUS;
    }

    /**
     * Show a toast message
     */
    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Dismiss the notification
     */
    private void dismissNotification(Context context, int notificationId) {
        try {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(notificationId);
                Log.d(TAG, "Dismissed notification: " + notificationId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error dismissing notification", e);
        }
    }
}
