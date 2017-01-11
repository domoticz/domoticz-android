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
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class SecurityWidgetProvider extends AppWidgetProvider {

    private static SharedPrefUtil mSharedPrefs;
    private static String packageName;
    private static Domoticz domoticz;
    private static Context context;

    public static PendingIntent buildButtonPendingIntent(Context context, int widget_id, int idx, int action) {
        Intent intent = new Intent();
        intent.setAction("nl.hnogames.domoticz.Service.WIDGET_SECURITY_ACTION");
        intent.putExtra("IDX", idx);
        intent.putExtra("WIDGETID", widget_id);
        intent.putExtra("WIDGETACTION", action);

        return PendingIntent.getBroadcast(context, widget_id, intent, 0);
    }

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
                Intent intent = new Intent(context, UpdateSecurityWidgetService.class);
                intent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                intent.setAction("FROM WIDGET PROVIDER");
                context.startService(intent);
            }
        }
    }

    public static class UpdateSecurityWidgetService extends IntentService {
        private static final int INVALID_IDX = 999999;
        private RemoteViews views;

        public UpdateSecurityWidgetService() {
            super("UpdateSecurityWidgetService");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(UpdateSecurityWidgetService.this);

            int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                    INVALID_APPWIDGET_ID);
            if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
                try {
                    updateAppWidget(appWidgetManager, incomingAppWidgetId);
                } catch (NullPointerException e) {
                    Log.e(SecurityWidgetProvider.class.getSimpleName(), e.getMessage());
                }
            }
        }

        public void updateAppWidget(final AppWidgetManager appWidgetManager,
                                    final int appWidgetId) {
            final int idx = mSharedPrefs.getSecurityWidgetIDX(appWidgetId);
            if (appWidgetId == INVALID_APPWIDGET_ID || idx == INVALID_IDX) {
                Log.i("WIDGET", "I am invalid");
                return;
            }

            views = new RemoteViews(packageName, R.layout.widget_security_layout);
            domoticz.getDevice(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                }

                @Override
                public void onReceiveDevice(DevicesInfo s) {
                    if (s != null) {
                        views = new RemoteViews(packageName, R.layout.widget_security_layout);
                        views.setTextViewText(R.id.title, s.getName());
                        views.setTextViewText(R.id.status, context.getString(R.string.status) + ": " +
                                String.valueOf(s.getData()));

                        views.setOnClickPendingIntent(R.id.armhome, buildButtonPendingIntent(
                                UpdateSecurityWidgetService.this,
                                appWidgetId,
                                s.getIdx(), DomoticzValues.Security.Status.ARMHOME));
                        views.setViewVisibility(R.id.armhome, View.VISIBLE);

                        views.setOnClickPendingIntent(R.id.armaway, buildButtonPendingIntent(
                                UpdateSecurityWidgetService.this,
                                appWidgetId,
                                s.getIdx(), DomoticzValues.Security.Status.ARMAWAY));
                        views.setViewVisibility(R.id.armaway, View.VISIBLE);

                        views.setOnClickPendingIntent(R.id.disarm, buildButtonPendingIntent(
                                UpdateSecurityWidgetService.this,
                                appWidgetId,
                                s.getIdx(), DomoticzValues.Security.Status.DISARM));
                        views.setViewVisibility(R.id.disarm, View.VISIBLE);

                        views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
                        appWidgetManager.updateAppWidget(appWidgetId, views);
                    }
                }

                @Override
                public void onError(Exception error) {
                }
            }, idx, false);
        }
    }
}
