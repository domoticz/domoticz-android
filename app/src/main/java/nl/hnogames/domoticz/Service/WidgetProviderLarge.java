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

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Containers.SceneInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.ScenesReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetProviderLarge extends AppWidgetProvider {
    private static final int iVoiceAction = -55;
    private static final int iQRCodeAction = -66;
    private Context context;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        this.context = context;
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

            final SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getApplicationContext());

            final String packageName = this.getPackageName();

            if (mSharedPrefs.darkThemeEnabled())
                views = new RemoteViews(packageName, R.layout.widget_layout_dark);
            else
                views = new RemoteViews(packageName, R.layout.widget_layout);//default

            final Domoticz domoticz = new Domoticz(getApplicationContext(), null);

            final int idx = mSharedPrefs.getWidgetIDX(appWidgetId);
            if (idx == iVoiceAction) {
                if (mSharedPrefs.darkThemeEnabled()) {
                    views = new RemoteViews(packageName, R.layout.widget_layout_dark);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_layout);
                }
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
                if (mSharedPrefs.darkThemeEnabled()) {
                    views = new RemoteViews(packageName, R.layout.widget_layout_dark);
                } else {
                    views = new RemoteViews(packageName, R.layout.widget_layout);
                }
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
                final boolean isScene = mSharedPrefs.getWidgetisScene(appWidgetId);
                if (!isScene) {
                    domoticz.getDevice(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo s) {
                            if (s != null) {
                                int withButtons = withButtons(s);
                                if (mSharedPrefs.darkThemeEnabled()) {
                                    if (withButtons == 1)
                                        views = new RemoteViews(packageName, R.layout.widget_layout_dark);
                                    if (withButtons == 2)
                                        views = new RemoteViews(packageName, R.layout.widget_layout_buttons_dark);
                                } else {
                                    if (withButtons == 1)
                                        views = new RemoteViews(packageName, R.layout.widget_layout);
                                    if (withButtons == 2)
                                        views = new RemoteViews(packageName, R.layout.widget_layout_buttons);
                                }
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
                                if (withButtons == 1 && s.getStatus() != null) {
                                    if (s.getStatusBoolean())
                                        views.setTextViewText(R.id.on_button, "off");
                                    else
                                        views.setTextViewText(R.id.on_button, "on");

                                    views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            !s.getStatusBoolean(),
                                            true));
                                    views.setViewVisibility(R.id.on_button, View.VISIBLE);

                                } else if (withButtons == 2 && s.getStatus() != null) {
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
                                } else {
                                    views.setViewVisibility(R.id.on_button, View.GONE);
                                }

                                views.setImageViewResource(R.id.rowIcon, domoticz.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
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
                                if (s.getStatusInString() != null) {
                                    if (s.getType().equals(Domoticz.Scene.Type.SCENE)) {
                                        if (mSharedPrefs.darkThemeEnabled())
                                            views = new RemoteViews(packageName, R.layout.widget_layout_dark);
                                        else
                                            views = new RemoteViews(packageName, R.layout.widget_layout);

                                        views.setTextViewText(R.id.title, s.getName());
                                        views.setTextViewText(R.id.desc, s.getStatusInString());

                                        views.setTextViewText(R.id.on_button, "on");
                                        views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                !s.getStatusInBoolean(),
                                                true));
                                        views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                    } else {
                                        if (mSharedPrefs.darkThemeEnabled())
                                            views = new RemoteViews(packageName, R.layout.widget_layout_buttons_dark);
                                        else
                                            views = new RemoteViews(packageName, R.layout.widget_layout_buttons);

                                        views.setTextViewText(R.id.title, s.getName());
                                        views.setTextViewText(R.id.desc, s.getStatusInString());
                                        views.setTextViewText(R.id.off_button, "off");
                                        views.setTextViewText(R.id.on_button, "on");

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

                                views.setImageViewResource(R.id.rowIcon, domoticz.getDrawableIcon(s.getType(), null, null, false, false, null));
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

        private int withButtons(DevicesInfo s) {
            int withButton = 0;
            if (s != null) {
                if (s.getSwitchTypeVal() == 0 &&
                        (UsefulBits.isEmpty(s.getSwitchType()))) {
                    switch (s.getType()) {
                        case Domoticz.Scene.Type.SCENE:
                            withButton = 1;
                            break;
                        case Domoticz.Scene.Type.GROUP:
                            withButton = 2;
                            break;
                    }
                } else {
                    switch (s.getSwitchTypeVal()) {
                        case Domoticz.Device.Type.Value.ON_OFF:
                        case Domoticz.Device.Type.Value.MEDIAPLAYER:
                        case Domoticz.Device.Type.Value.X10SIREN:
                        case Domoticz.Device.Type.Value.DOORLOCK:
                        case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                        case Domoticz.Device.Type.Value.SMOKE_DETECTOR:
                        case Domoticz.Device.Type.Value.DOORBELL:
                        case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                        case Domoticz.Device.Type.Value.DIMMER:
                            withButton = 1;
                            break;
                    }
                }
            }
            return withButton;
        }
    }
}