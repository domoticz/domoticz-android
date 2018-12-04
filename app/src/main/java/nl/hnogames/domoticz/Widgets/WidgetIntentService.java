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

package nl.hnogames.domoticz.Widgets;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class WidgetIntentService extends Service {

    private final int iVoiceAction = -55;
    private final int iQRCodeAction = -66;
    private int widgetID = 0;
    private boolean action = false;
    private boolean toggle = true;
    private String password = null;
    private String value = null;
    private SharedPrefUtil mSharedPrefs;
    private int blind_action = -1;
    private boolean smallWidget = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForeground(1337, NotificationUtil.getForegroundServiceNotification(this, "Widget"));
        }

        mSharedPrefs = new SharedPrefUtil(this);
        widgetID = intent.getIntExtra("WIDGETID", 999999);
        int idx = intent.getIntExtra("IDX", 999999);
        if (idx == iVoiceAction)//voice
        {
            Intent iStart = new Intent(this, MainActivity.class);
            iStart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            iStart.putExtra("VOICE", true);
            this.startActivity(iStart);
        } else if (idx == iQRCodeAction)//qrcode
        {
            Intent iStart = new Intent(this, MainActivity.class);
            iStart.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            iStart.putExtra("QRCODE", true);
            this.startActivity(iStart);
        } else {
            loadPasswordandValue();

            if (intent.getAction().equals("nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION")) {
                Log.i("onReceive", "nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION");
                smallWidget = intent.getBooleanExtra("WIDGETSMALL", false);
                action = intent.getBooleanExtra("WIDGETACTION", false);
                toggle = intent.getBooleanExtra("WIDGETTOGGLE", true);
                Log.i("SWITCH TOGGLE", "SWITCH TOGGLE:" + idx + " | " + action);
                processSwitch(this, idx);
            } else if (intent.getAction().equals("nl.hnogames.domoticz.Service.WIDGET_BLIND_ACTION")) {
                Log.i("onReceive", "nl.hnogames.domoticz.Service.WIDGET_BLIND_ACTION");
                blind_action = intent.getIntExtra("WIDGETBLINDACTION", -1);
                Log.i("BLIND TOGGLE", "BLIND TOGGLE:" + idx + " | " + blind_action);
                processBlind(this, idx, blind_action);
            }
        }

        stopSelf();
        return START_NOT_STICKY;
    }

    private boolean isOnOffSwitch(DevicesInfo mExtendedStatusInfo) {
        if (mExtendedStatusInfo == null)
            return false;
        if (smallWidget)
            return true;

        if (mExtendedStatusInfo.getSwitchTypeVal() == 0 &&
                (mExtendedStatusInfo.getSwitchType() == null ||
                        UsefulBits.isEmpty(mExtendedStatusInfo.getSwitchType()))) {
            switch (mExtendedStatusInfo.getType()) {
                case DomoticzValues.Scene.Type.GROUP:
                    return true;
            }
        } else {
            switch (mExtendedStatusInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.ON_OFF:
                case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                case DomoticzValues.Device.Type.Value.X10SIREN:
                case DomoticzValues.Device.Type.Value.DIMMER:
                case DomoticzValues.Device.Type.Value.DOORCONTACT:
                case DomoticzValues.Device.Type.Value.SELECTOR:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                    return true;
                case DomoticzValues.Device.Type.Value.BLINDS:
                case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                    if (DomoticzValues.canHandleStopButton(mExtendedStatusInfo))
                        return false;
                    else
                        return true;
            }
        }

        return false;
    }

    private boolean isPushOnSwitch(DevicesInfo mExtendedStatusInfo) {
        if (mExtendedStatusInfo.getSwitchTypeVal() == 0 &&
                (mExtendedStatusInfo.getSwitchType() == null ||
                        UsefulBits.isEmpty(mExtendedStatusInfo.getSwitchType()))) {
            switch (mExtendedStatusInfo.getType()) {
                case DomoticzValues.Scene.Type.SCENE:
                    return true;

            }
        } else
            switch (mExtendedStatusInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                case DomoticzValues.Device.Type.Value.DOORBELL:
                    return true;

            }
        return false;
    }

    private boolean isPushOffSwitch(DevicesInfo mExtendedStatusInfo) {
        if (mExtendedStatusInfo.getSwitchTypeVal() == 0 &&
                (mExtendedStatusInfo.getSwitchType() == null ||
                        UsefulBits.isEmpty(mExtendedStatusInfo.getSwitchType()))) {
            return false;
        } else
            switch (mExtendedStatusInfo.getSwitchTypeVal()) {
                case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                    return true;
            }
        return false;
    }

    private void loadPasswordandValue() {
        password = mSharedPrefs.getWidgetPassword(widgetID);
        value = mSharedPrefs.getWidgetValue(widgetID);
        if (UsefulBits.isEmpty(value) && UsefulBits.isEmpty(password)) {
            password = mSharedPrefs.getSmallWidgetPassword(widgetID);
            value = mSharedPrefs.getSmallWidgetValue(widgetID);
        }
    }

    private void processSwitch(final Context context, int idx) {

        final Domoticz domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        boolean isScene = mSharedPrefs.getWidgetisScene(widgetID);
        if (smallWidget)
            isScene = mSharedPrefs.getSmallWidgetisScene(widgetID);
        Log.i("PROCESS SWITCH", "Device: " + idx + " " + isScene);

        if (!isScene) {
            domoticz.getDevice(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                }

                @Override
                public void onReceiveDevice(final DevicesInfo s) {
                    if (s != null) {
                        Log.i("SWITCH TOGGLE", "Device: " + s.getName() + " | " + s.getSwitchType());

                        if (isOnOffSwitch(s)) {
                            if (toggle)
                                onSwitchClick(s, !s.getStatusBoolean(), domoticz, context, value);
                            else
                                onSwitchClick(s, action, domoticz, context, value);
                        }

                        if (isPushOffSwitch(s))
                            onButtonClick(s, false, domoticz, context);
                        if (isPushOnSwitch(s))
                            onButtonClick(s, true, domoticz, context);
                    }
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(context, R.string.failed_toggle_switch, Toast.LENGTH_SHORT).show();
                }
            }, idx, false);
        } else {
            domoticz.getScene(new ScenesReceiver() {
                @Override
                public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                }

                @Override
                public void onError(Exception error) {
                }

                @Override
                public void onReceiveScene(final SceneInfo scene) {
                    if (scene != null) {
                        Log.i("SCENE TOGGLE", "Device: " + scene.getName());
                        if (DomoticzValues.Scene.Type.SCENE.equalsIgnoreCase(scene.getType())) {
                            onButtonClick(scene, true, domoticz, context);//push on scene
                        } else {//switch
                            if (toggle)
                                onSwitchClick(scene, !scene.getStatusInBoolean(), domoticz, context);
                            else
                                onSwitchClick(scene, action, domoticz, context);
                        }
                    }
                }
            }, idx);
        }
    }

    private void processBlind(final Context context, int idx, final int action) {
        final Domoticz domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());

        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

            @Override
            public void onReceiveDevice(final DevicesInfo s) {
                if (s != null) {
                    onBlindsToggle(s, action, domoticz, context);
                }
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(context, R.string.failed_toggle_switch, Toast.LENGTH_SHORT).show();
            }
        }, idx, false);
    }

    public void onButtonClick(final SceneInfo clickedSwitch, boolean checked, Domoticz mDomoticz, final Context context) {
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
        else jsonAction = DomoticzValues.Device.Switch.Action.OFF;

        int idx = clickedSwitch.getIdx();
        if (clickedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || clickedSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
            else jsonAction = DomoticzValues.Scene.Action.OFF;
        }
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                Toast.makeText(context, context.getString(R.string.switch_toggled) + ": " + clickedSwitch.getName(), Toast.LENGTH_SHORT).show();
                WidgetUtils.RefreshWidgets(context);
            }

            @Override
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password))
                    Toast.makeText(context, context.getString(R.string.security_wrong_code), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getString(R.string.failed_toggle_switch), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onButtonClick(final DevicesInfo clickedSwitch, boolean checked, Domoticz mDomoticz, final Context context) {
        int jsonAction;
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        if (checked) jsonAction = DomoticzValues.Device.Switch.Action.ON;
        else jsonAction = DomoticzValues.Device.Switch.Action.OFF;

        int idx = clickedSwitch.getIdx();
        if (clickedSwitch.getType().equals(DomoticzValues.Scene.Type.GROUP) || clickedSwitch.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
            jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
            else jsonAction = DomoticzValues.Scene.Action.OFF;
            idx = idx - 4000;
        }
        mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                Toast.makeText(context, context.getString(R.string.switch_toggled) + ": " + clickedSwitch.getName(), Toast.LENGTH_SHORT).show();
                WidgetUtils.RefreshWidgets(context);
            }

            @Override
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password))
                    Toast.makeText(context, context.getString(R.string.security_wrong_code), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getString(R.string.failed_toggle_switch), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onSwitchClick(final DevicesInfo clickedSwitch, boolean checked, Domoticz mDomoticz, final Context context, String value) {
        if (clickedSwitch != null) {
            int jsonAction;
            int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

            int jsonValue = 0;
            if (clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                    clickedSwitch.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                if (checked) jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                else {
                    jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    if (!UsefulBits.isEmpty(value)) {
                        jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                        jsonValue = getSelectorValue(clickedSwitch, value);
                    }
                }
            } else {
                if (checked) {
                    jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    if (!UsefulBits.isEmpty(value)) {
                        jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                        jsonValue = getSelectorValue(clickedSwitch, value);
                    }
                } else jsonAction = DomoticzValues.Device.Switch.Action.OFF;
            }

            mDomoticz.setAction(clickedSwitch.getIdx(), jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    Toast.makeText(context, context.getString(R.string.switch_toggled) + ": " + clickedSwitch.getName(), Toast.LENGTH_SHORT).show();
                    WidgetUtils.RefreshWidgets(context);
                }

                @Override
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password))
                        Toast.makeText(context, context.getString(R.string.security_wrong_code), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, context.getString(R.string.failed_toggle_switch), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onBlindsToggle(final DevicesInfo blind, int action, Domoticz mDomoticz, final Context context) {
        int idx = blind.getIdx();
        int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;

        mDomoticz.setAction(idx, jsonUrl, action, 0, password, new setCommandReceiver() {
            @Override
            public void onReceiveResult(String result) {
                Toast.makeText(context, context.getString(R.string.switch_toggled) + ": " + blind.getName(), Toast.LENGTH_SHORT).show();
                WidgetUtils.RefreshWidgets(context);
            }

            @Override
            public void onError(Exception error) {
                if (!UsefulBits.isEmpty(password))
                    Toast.makeText(context, context.getString(R.string.security_wrong_code), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(context, context.getString(R.string.failed_toggle_switch), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
        int jsonValue = 0;
        if (!UsefulBits.isEmpty(value)) {
            ArrayList<String> levelNames = mDevicesInfo.getLevelNames();
            int counter = 0;
            for (String l : levelNames) {
                if (l.equals(value))
                    break;
                else
                    counter += 10;
            }
            jsonValue = counter;
        }
        return jsonValue;
    }

    public void onSwitchClick(final SceneInfo clickedSwitch, boolean checked, Domoticz mDomoticz, final Context context) {

        if (clickedSwitch != null) {
            int jsonAction;
            int jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
            int idx = clickedSwitch.getIdx();

            if (checked) jsonAction = DomoticzValues.Scene.Action.ON;
            else jsonAction = DomoticzValues.Scene.Action.OFF;

            mDomoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    Toast.makeText(context, context.getString(R.string.switch_toggled) + ": " + clickedSwitch.getName(), Toast.LENGTH_SHORT).show();
                    WidgetUtils.RefreshWidgets(context);
                }

                @Override
                public void onError(Exception error) {
                    if (!UsefulBits.isEmpty(password))
                        Toast.makeText(context, context.getString(R.string.security_wrong_code), Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, context.getString(R.string.failed_toggle_switch), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
