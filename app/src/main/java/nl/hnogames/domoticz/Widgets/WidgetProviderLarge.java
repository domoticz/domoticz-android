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

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetProviderLarge extends AppWidgetProvider {

    private static final int iVoiceAction = -55;
    private static final int iQRCodeAction = -66;

    private static SharedPrefUtil mSharedPrefs;
    private static String packageName;
    private static Domoticz domoticz;

    private static Context context;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.context = context;
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(context);
        if (domoticz == null)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        packageName = context.getPackageName();

        if (appWidgetIds != null) {
            for (int mAppWidgetId : appWidgetIds) {
                Intent intent = new Intent(context, UpdateWidgetService.class);
                intent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                intent.setAction("FROM WIDGET PROVIDER");
                context.startService(intent);
            }
        }
    }

    public static class UpdateWidgetService extends IntentService {
        private static final int BUTTON_1 = 1;
        private static final int BUTTON_2 = 2;
        private static final int BUTTON_3 = 3;
        private RemoteViews views;

        public UpdateWidgetService() {
            super("UpdateWidgetService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(UpdateWidgetService.this);

            int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
            if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
                try {
                    updateAppWidget(appWidgetManager, incomingAppWidgetId);
                } catch (NullPointerException e) {
                    Log.e(WidgetProviderLarge.class.getSimpleName(), e.getMessage());
                }
            }
        }

        public void updateAppWidget(final AppWidgetManager appWidgetManager,
                                    final int appWidgetId) {
            if (appWidgetId == INVALID_APPWIDGET_ID) {
                Log.i("WIDGET", "I am invalid");
                return;
            }
            final int idx = mSharedPrefs.getWidgetIDX(appWidgetId);
            views = new RemoteViews(packageName, mSharedPrefs.getWidgetLayout(appWidgetId));
            if (views == null)
                return;

            if (idx == iVoiceAction) {
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.Speech_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_speech));
                views.setImageViewResource(R.id.rowIcon, R.drawable.mic);
                views.setTextViewText(R.id.on_button, "GO");
                views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                views.setViewVisibility(R.id.on_button, View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else if (idx == iQRCodeAction) {
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.qrcode_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_qrcode_scan));
                views.setImageViewResource(R.id.rowIcon, R.drawable.qrcode);
                views.setTextViewText(R.id.on_button, "GO");
                views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                views.setViewVisibility(R.id.on_button, View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else {
                appWidgetManager.updateAppWidget(appWidgetId, views);
                final boolean isScene = mSharedPrefs.getWidgetisScene(appWidgetId);
                if (!isScene) {
                    domoticz.getDevice(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo s) {
                            if (s != null) {
                                views = new RemoteViews(packageName, mSharedPrefs.getWidgetLayout(appWidgetId));
                                if (views == null)
                                    return;

                                int withButtons = withButtons(s);
                                String text = s.getData();
                                views.setTextViewText(R.id.title, s.getName());
                                if (s.getUsage() != null && s.getUsage().length() > 0)
                                    text = s.getUsage();
                                if (s.getCounterToday() != null && s.getCounterToday().length() > 0)
                                    text += " Today: " + s.getCounterToday();
                                if (s.getCounter() != null && s.getCounter().length() > 0 &&
                                        !s.getCounter().equals(s.getData()))
                                    text += " Total: " + s.getCounter();

                                views.setTextViewText(R.id.desc, text);
                                if (withButtons == BUTTON_1 && s.getStatus() != null) {
                                    if(s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON)
                                        views.setTextViewText(R.id.on_button, context.getString(R.string.button_state_on));
                                    else if (s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON)
                                        views.setTextViewText(R.id.on_button, context.getString(R.string.button_state_off));
                                    else {
                                        if (s.getStatusBoolean())
                                            views.setTextViewText(R.id.on_button, context.getString(R.string.button_state_off));
                                        else
                                            views.setTextViewText(R.id.on_button, context.getString(R.string.button_state_on));
                                    }
                                    
                                    views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            !s.getStatusBoolean(),
                                            true));
                                    views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                } else if (withButtons == BUTTON_2 && s.getStatus() != null) {
                                    views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), true,
                                            false));
                                    views.setViewVisibility(R.id.on_button, View.VISIBLE);

                                    views.setOnClickPendingIntent(R.id.off_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), false,
                                            false));
                                    views.setViewVisibility(R.id.off_button, View.VISIBLE);

                                } else if (withButtons == BUTTON_3 && s.getStatus() != null) {
                                    views.setOnClickPendingIntent(R.id.switch_button_up, buildBlindPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), DomoticzValues.Device.Blind.Action.UP));
                                    views.setViewVisibility(R.id.switch_button_up, View.VISIBLE);

                                    views.setOnClickPendingIntent(R.id.switch_button_stop, buildBlindPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), DomoticzValues.Device.Blind.Action.STOP));
                                    views.setViewVisibility(R.id.switch_button_stop, View.VISIBLE);

                                    views.setOnClickPendingIntent(R.id.switch_button_down, buildBlindPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), DomoticzValues.Device.Blind.Action.DOWN));
                                    views.setViewVisibility(R.id.switch_button_down, View.VISIBLE);
                                } else {
                                    views.setViewVisibility(R.id.on_button, View.GONE);
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }

                        @Override
                        public void onError(Exception error) {
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
                        public void onReceiveScene(SceneInfo s) {
                            if (s != null) {
                                views = new RemoteViews(packageName, mSharedPrefs.getWidgetLayout(appWidgetId));
                                if (views == null)
                                    return;

                                if (s.getStatusInString() != null) {
                                    if (s.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
                                        views.setTextViewText(R.id.title, s.getName());
                                        views.setTextViewText(R.id.desc, s.getStatusInString());

                                        views.setTextViewText(R.id.on_button, context.getString(R.string.button_state_on));
                                        views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                !s.getStatusInBoolean(),
                                                true));
                                        views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                    } else {

                                        views.setTextViewText(R.id.title, s.getName());
                                        views.setTextViewText(R.id.desc, s.getStatusInString());
                                        views.setTextViewText(R.id.off_button, context.getString(R.string.button_state_off));
                                        views.setTextViewText(R.id.on_button, context.getString(R.string.button_state_on));
                                        views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                true,
                                                false));
                                        views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                        views.setOnClickPendingIntent(R.id.off_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                false,
                                                false));

                                        views.setViewVisibility(R.id.off_button, View.VISIBLE);
                                    }
                                } else {
                                    views.setViewVisibility(R.id.on_button, View.GONE);
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getType(), null, null, false, false, null));
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                    }, idx);
                }
            }
        }

        public PendingIntent buildButtonPendingIntent(Context context, int widget_id, int idx, boolean action, boolean toggle) {
            Intent intent = new Intent();
            intent.setAction("nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION");
            intent.putExtra("IDX", idx);
            intent.putExtra("WIDGETID", widget_id);
            intent.putExtra("WIDGETACTION", action);
            intent.putExtra("WIDGETTOGGLE", toggle);

            if (toggle)
                return PendingIntent.getBroadcast(context, widget_id, intent, 0);
            else if (action)
                return PendingIntent.getBroadcast(context, widget_id + 8888, intent, 0);
            else
                return PendingIntent.getBroadcast(context, widget_id + 9999, intent, 0);
        }

        public PendingIntent buildBlindPendingIntent(Context context, int widget_id, int idx, int action) {
            Intent intent = new Intent();
            intent.setAction("nl.hnogames.domoticz.Service.WIDGET_BLIND_ACTION");
            intent.putExtra("IDX", idx);
            intent.putExtra("WIDGETID", widget_id);
            intent.putExtra("WIDGETACTION", action);

            int requestCode;
            switch (action) {
                case DomoticzValues.Device.Blind.Action.UP:
                    requestCode = widget_id;
                    break;
                case DomoticzValues.Device.Blind.Action.STOP:
                    requestCode = widget_id + 8888;
                    break;
                case DomoticzValues.Device.Blind.Action.DOWN:
                    requestCode = widget_id + 9999;
                    break;
                default:
                    requestCode = widget_id + 7777;
            }

            return PendingIntent.getBroadcast(context, requestCode, intent, 0);
        }

        private int withButtons(DevicesInfo s) {
            int withButton = 0;
            if (s != null) {
                if (s.getSwitchTypeVal() == 0 &&
                        (UsefulBits.isEmpty(s.getSwitchType()))) {
                    switch (s.getType()) {
                        case DomoticzValues.Scene.Type.SCENE:
                            withButton = BUTTON_1;
                            break;
                        case DomoticzValues.Scene.Type.GROUP:
                            withButton = BUTTON_2;
                            break;
                    }
                } else {
                    switch (s.getSwitchTypeVal()) {
                        case DomoticzValues.Device.Type.Value.ON_OFF:
                        case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                        case DomoticzValues.Device.Type.Value.DOORLOCK:
                            if (mSharedPrefs.showSwitchesAsButtons())
                                withButton = BUTTON_2;
                            else
                                withButton = BUTTON_1;
                            break;

                        case DomoticzValues.Device.Type.Value.X10SIREN:
                        case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                        case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                        case DomoticzValues.Device.Type.Value.DOORBELL:
                        case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        case DomoticzValues.Device.Type.Value.DIMMER:
                        case DomoticzValues.Device.Type.Value.SELECTOR:
                            withButton = BUTTON_1;
                            break;

                        case DomoticzValues.Device.Type.Value.BLINDS:
                        case DomoticzValues.Device.Type.Value.BLINDINVERTED:
                        case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                        case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                        case DomoticzValues.Device.Type.Value.BLINDPERCENTAGEINVERTED:
                            withButton = BUTTON_3;
                            break;
                    }
                }
            }
            return withButton;
        }
    }
}