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

package nl.hnogames.domoticz.widgets;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

public class WidgetProviderSmallTemp extends AppWidgetProvider {
    private static String packageName;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            SharedPrefUtil mSharedPrefs = new SharedPrefUtil(context);
            mSharedPrefs.deleteSmallTempWidget(widgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        packageName = context.getPackageName();
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                WidgetProviderSmallTemp.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        if (allWidgetIds != null) {
            for (int mAppWidgetId : allWidgetIds) {
                Intent intent = new Intent(context, UpdateWidgetService.class);
                intent.putExtra(EXTRA_APPWIDGET_ID, mAppWidgetId);
                intent.setAction("FROM WIDGET PROVIDER");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent);
                } else
                    context.startService(intent);
            }
        }
    }

    public static class UpdateWidgetService extends Service {
        private RemoteViews views;
        private SharedPrefUtil mSharedPrefs;

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

            AppWidgetManager appWidgetManager = AppWidgetManager
                    .getInstance(UpdateWidgetService.this);

            try {
                int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                        INVALID_APPWIDGET_ID);
                if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
                    try {
                        updateAppWidget(appWidgetManager, incomingAppWidgetId);
                    } catch (NullPointerException e) {
                        if (!UsefulBits.isEmpty(e.getMessage()))
                            Log.e(WidgetProviderSmallTemp.class.getSimpleName() + "@onStartCommand", e.getMessage());
                    }
                }

            } catch (Exception ex) {
                Log.e("UpdateWidget", ex.toString());
            }

            stopSelf();
            return START_NOT_STICKY;
        }

        public void updateAppWidget(final AppWidgetManager appWidgetManager,
                                    final int appWidgetId) {
            if (appWidgetId == INVALID_APPWIDGET_ID) {
                Log.i("WIDGET", "I am invalid");
                return;
            }

            if (mSharedPrefs == null)
                mSharedPrefs = new SharedPrefUtil(this.getApplicationContext());

            final int idx = mSharedPrefs.getSmallTempWidgetIDX(appWidgetId);
            views = new RemoteViews(packageName, mSharedPrefs.getSmallTempWidgetLayout(appWidgetId));
            appWidgetManager.updateAppWidget(appWidgetId, views);
            StaticHelper.getDomoticz(getApplicationContext()).getDevice(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                }

                @Override
                public void onReceiveDevice(DevicesInfo s) {
                    if (s != null) {
                        views = new RemoteViews(packageName, mSharedPrefs.getSmallTempWidgetLayout(appWidgetId));
                        final double temperature = s.getTemperature();
                        String sign = StaticHelper.getDomoticz(getApplicationContext()).getServerUtil() != null && StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer() != null
                                && StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()) != null ?
                                StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()).getTempSign() : "C";

                        String text = s.getData();
                        if (!Double.isNaN(temperature)) {
                            text = temperature
                                    + " " + sign;
                        }
                        views.setTextViewText(R.id.title, text);
                        views.setTextViewText(R.id.desc, s.getName());

                        int icon;
                        if ((!UsefulBits.isEmpty(sign) && sign.equals("C") && s.getTemperature() < 0) ||
                                (!UsefulBits.isEmpty(sign) && sign.equals("F") && s.getTemperature() < 30)) {
                            icon = (DomoticzIcons.getDrawableIcon(s.getTypeImg(),
                                    s.getType(),
                                    null,
                                    StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()) != null && s.getTemperature() > StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()).getDegreeDaysBaseTemperature(),
                                    true,
                                    "Freezing"));
                        } else {
                            icon = (DomoticzIcons.getDrawableIcon(s.getTypeImg(),
                                    s.getType(),
                                    null,
                                    StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()) != null && s.getTemperature() > StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()).getDegreeDaysBaseTemperature(),
                                    false,
                                    null));
                        }

                        views.setImageViewResource(R.id.rowIcon, icon);
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
