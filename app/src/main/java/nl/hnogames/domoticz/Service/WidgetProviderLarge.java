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
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetProviderLarge extends AppWidgetProvider {
    Context context;

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
                    updateAppWidget(appWidgetManager, incomingAppWidgetId, intent);
                } catch (NullPointerException e) {
                }
            }
        }

        public void updateAppWidget(final AppWidgetManager appWidgetManager,
                                    final int appWidgetId, Intent intent) {

            final SharedPrefUtil mSharedPrefs = new SharedPrefUtil(getApplicationContext());

            final String packageName = this.getPackageName();
            views = new RemoteViews(packageName, R.layout.widget_layout);//default
            final Domoticz domoticz = new Domoticz(getApplicationContext());

            int idx = mSharedPrefs.getWidgetIDX(appWidgetId);
            domoticz.getDevice(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                }

                @Override
                public void onReceiveDevice(DevicesInfo s) {
                    if (s != null) {
                        boolean withButtons = withButtons(s);
                        if (withButtons)
                            views = new RemoteViews(packageName, R.layout.widget_layout);//default

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

                        if (withButtons && s.getStatus() != null) {
                            if (s.getStatusBoolean())
                                views.setTextViewText(R.id.on_button, "off");
                            else
                                views.setTextViewText(R.id.on_button, "on");

                            views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                    UpdateWidgetService.this,
                                    mSharedPrefs.getWidgetIDforIDX(s.getIdx()),
                                    s.getIdx()));
                            views.setViewVisibility(R.id.on_button, View.VISIBLE);
                        } else {
                            views.setViewVisibility(R.id.on_button, View.GONE);
                        }

                        views.setImageViewResource(R.id.rowIcon, domoticz.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
                        appWidgetManager.updateAppWidget(mSharedPrefs.getWidgetIDforIDX(s.getIdx()), views);
                    }
                }

                @Override
                public void onError(Exception error) {
                }

            }, idx);
        }

        public PendingIntent buildButtonPendingIntent(Context context, int widgetid, int idx) {
            Intent intent = new Intent();
            intent.setAction("nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION");
            intent.putExtra("IDX", idx);
            intent.putExtra("WIDGETID", widgetid);
            return PendingIntent.getBroadcast(context, widgetid, intent, 0);
        }

        private boolean withButtons(DevicesInfo s) {
            boolean withButton = false;
            if (s != null && !s.isProtected()) {
                if (s.getSwitchTypeVal() == 0 &&
                        (s.getSwitchType() == null || s.getSwitchType().equals(null))) {
                    switch (s.getType()) {
                        case Domoticz.Scene.Type.GROUP:
                        case Domoticz.Scene.Type.SCENE:
                            withButton = true;
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
                            withButton = true;
                            break;
                    }
                }
            }
            return withButton;
        }
    }
}