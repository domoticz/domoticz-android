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

package nl.hnogames.domoticz.Service;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class WidgetIntentReceiver extends BroadcastReceiver {

    private int widgetID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        widgetID = intent.getIntExtra("WIDGETID", 999999);
        int idx = intent.getIntExtra("IDX", 999999);
        if (intent.getAction().equals("nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION")) {
            toggleSwitch(context, idx);
        }
    }

    private DevicesInfo getDevice(int idx, ArrayList<DevicesInfo> mDevicesInfo) {
        for (DevicesInfo s : mDevicesInfo) {
            if (s.getIdx() == idx) {
                return s;
            }
        }
        return null;
    }

    private boolean isOnOffSwitch(DevicesInfo mExtendedStatusInfo) {
        if (mExtendedStatusInfo.getSwitchTypeVal() == 0 &&
                (mExtendedStatusInfo.getSwitchType() == null || mExtendedStatusInfo.getSwitchType().equals(null))) {
            switch (mExtendedStatusInfo.getType()) {
                case Domoticz.Scene.Type.GROUP:
                    return true;
            }
        } else {
            boolean switchFound = true;
            switch (mExtendedStatusInfo.getSwitchTypeVal()) {
                case Domoticz.Device.Type.Value.ON_OFF:
                case Domoticz.Device.Type.Value.MEDIAPLAYER:
                case Domoticz.Device.Type.Value.X10SIREN:
                case Domoticz.Device.Type.Value.DIMMER:
                case Domoticz.Device.Type.Value.DOORLOCK:
                    return true;
            }
        }
        return false;
    }


    private boolean isPushOnSwitch(DevicesInfo mExtendedStatusInfo) {
        if (mExtendedStatusInfo.getSwitchTypeVal() == 0 &&
                (mExtendedStatusInfo.getSwitchType() == null || mExtendedStatusInfo.getSwitchType().equals(null))) {
            switch (mExtendedStatusInfo.getType()) {
                case Domoticz.Scene.Type.SCENE:
                    return true;

            }
        } else
            switch (mExtendedStatusInfo.getSwitchTypeVal()) {
                case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                case Domoticz.Device.Type.Value.DOORBELL:
                    return true;

            }
        return false;
    }


    private boolean isPushOffSwitch(DevicesInfo mExtendedStatusInfo) {
        if (mExtendedStatusInfo.getSwitchTypeVal() == 0 &&
                (mExtendedStatusInfo.getSwitchType() == null || mExtendedStatusInfo.getSwitchType().equals(null))) {
            return false;
        } else
            switch (mExtendedStatusInfo.getSwitchTypeVal()) {
                case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                    return true;
            }
        return false;
    }

    private void toggleSwitch(final Context context, int idx) {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(context);
        final Domoticz domoticz = new Domoticz(context);
        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

            @Override
            public void onReceiveDevice(DevicesInfo s) {
                if (isOnOffSwitch(s))
                    onSwitchClick(s, !s.getStatusBoolean(), domoticz, context);
                if (isPushOffSwitch(s))
                    onButtonClick(s, false, domoticz, context);
                if (isPushOnSwitch(s))
                    onButtonClick(s, true, domoticz, context);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(context, "Failed to toggling switch.", Toast.LENGTH_SHORT).show();
            }
        }, idx);
    }

    public void onBlindClick(final DevicesInfo clickedSwitch, int jsonAction, Domoticz mDomoticz, final Context context) {
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
        int idx = clickedSwitch.getIdx();
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                Toast.makeText(context, "Switch toggled: " + clickedSwitch.getName() + " Widget: " + widgetID, Toast.LENGTH_SHORT).show();
                updateWidget(context);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(context, "Failed to toggle switch." + widgetID, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onButtonClick(final DevicesInfo clickedSwitch, boolean checked, Domoticz mDomoticz, final Context context) {
        int jsonAction;
        int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
        else jsonAction = Domoticz.Device.Switch.Action.OFF;

        int idx = clickedSwitch.getIdx();
        if (clickedSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || clickedSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
            jsonUrl = Domoticz.Json.Url.Set.SCENES;
            if (checked) jsonAction = Domoticz.Scene.Action.ON;
            else jsonAction = Domoticz.Scene.Action.OFF;
            idx = idx - 4000;
        }
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                Toast.makeText(context, "Switch toggled: " + clickedSwitch.getName() + " Widget: " + widgetID, Toast.LENGTH_SHORT).show();
                updateWidget(context);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(context, "Failed to toggle switch." + widgetID, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSwitchClick(final DevicesInfo clickedSwitch, boolean checked, Domoticz mDomoticz, final Context context) {

        if (clickedSwitch != null) {
            int jsonAction;
            int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
            if (clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                    clickedSwitch.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.OFF;
                else jsonAction = Domoticz.Device.Switch.Action.ON;
            } else {
                if (checked) jsonAction = Domoticz.Device.Switch.Action.ON;
                else jsonAction = Domoticz.Device.Switch.Action.OFF;
            }

            int idx = clickedSwitch.getIdx();
            if (clickedSwitch.getType().equals(Domoticz.Scene.Type.GROUP) || clickedSwitch.getType().equals(Domoticz.Scene.Type.SCENE)) {
                jsonUrl = Domoticz.Json.Url.Set.SCENES;
                if (checked) jsonAction = Domoticz.Scene.Action.ON;
                else jsonAction = Domoticz.Scene.Action.OFF;
                idx = idx - 4000;
            }

            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    Toast.makeText(context, "Switch toggled: " + clickedSwitch.getName() + " Widget: " + widgetID, Toast.LENGTH_SHORT).show();
                    updateWidget(context);
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(context, "Failed to toggle switch." + widgetID, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateWidget(Context context) {
        Intent intent = new Intent(context, WidgetProviderLarge.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = {widgetID};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
