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

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.NotificationUtil;
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

public class WidgetProviderSmall extends AppWidgetProvider {

    private static final int iVoiceAction = -55;
    private static final int iQRCodeAction = -66;
    private static String packageName;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            SharedPrefUtil mSharedPrefs = new SharedPrefUtil(context);
            mSharedPrefs.deleteSmallWidget(widgetId, mSharedPrefs.getWidgetisScene(widgetId));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);

        packageName = context.getPackageName();
        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                WidgetProviderSmall.class);
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
        private static final int WITHBUTTON = 1;
        private RemoteViews views;
        private Domoticz domoticz;
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
                            Log.e(WidgetProviderSmall.class.getSimpleName() + "@onStartCommand", e.getMessage());
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
            if (domoticz == null)
                domoticz = new Domoticz(this.getApplicationContext(), AppController.getInstance().getRequestQueue());

            final int idx = mSharedPrefs.getSmallWidgetIDX(appWidgetId);
            views = new RemoteViews(packageName, mSharedPrefs.getSmallWidgetLayout(appWidgetId));
            if (idx == iVoiceAction) {
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.Speech_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_speech));
                views.setImageViewResource(R.id.rowIcon, R.drawable.mic);
                views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else if (idx == iQRCodeAction) {
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.qrcode_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_qrcode_scan));
                views.setImageViewResource(R.id.rowIcon, R.drawable.qrcode);
                views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else {
                appWidgetManager.updateAppWidget(appWidgetId, views);
                final boolean isScene = mSharedPrefs.getSmallWidgetisScene(appWidgetId);
                if (!isScene) {
                    domoticz.getDevice(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo s) {
                            if (s != null) {
                                views = new RemoteViews(packageName, mSharedPrefs.getSmallWidgetLayout(appWidgetId));
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
                                if (withButtons == WITHBUTTON && s.getStatus() != null) {
                                    boolean newStatus = !s.getStatusBoolean();//toggle

                                    views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            newStatus,
                                            true));
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
                                if (!s.getStatusBoolean())
                                    views.setInt(R.id.rowIcon, "setAlpha", 100);
                                else
                                    views.setInt(R.id.rowIcon, "setAlpha", 255);

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
                                views = new RemoteViews(packageName, mSharedPrefs.getSmallWidgetLayout(appWidgetId));
                                if (s.getStatusInString() != null) {
                                    views.setTextViewText(R.id.title, s.getName());
                                    views.setTextViewText(R.id.desc, s.getStatusInString());
                                    views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            idx,
                                            !s.getStatusInBoolean(),
                                            true));
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getType(), null, null, false, false, null));
                                if (!s.getStatusInBoolean())
                                    views.setInt(R.id.rowIcon, "setAlpha", 100);
                                else
                                    views.setInt(R.id.rowIcon, "setAlpha", 255);

                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                    }, idx);
                }
            }
        }

        public PendingIntent buildButtonPendingIntent(Context context, int widget_id, int idx, boolean action, boolean toggle) {
            Intent intent = new Intent(this, WidgetIntentService.class);
            intent.setAction("nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION");
            intent.putExtra("IDX", idx);
            intent.putExtra("WIDGETID", widget_id);
            intent.putExtra("WIDGETACTION", action);
            intent.putExtra("WIDGETTOGGLE", toggle);
            intent.putExtra("WIDGETSMALL", true);

            if (toggle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return PendingIntent.getForegroundService(context, widget_id, intent, 0);
                } else {
                    return PendingIntent.getService(context, widget_id, intent, 0);
                }
            else if (action)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return PendingIntent.getForegroundService(context, widget_id + 8888, intent, 0);
                } else {
                    return PendingIntent.getService(context, widget_id + 8888, intent, 0);
                }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return PendingIntent.getForegroundService(context, widget_id + 9999, intent, 0);
                } else {
                    return PendingIntent.getService(context, widget_id + 9999, intent, 0);
                }
            }
        }

        private int withButtons(DevicesInfo s) {
            int withButton = 0;
            if (s != null) {
                if (s.getSwitchTypeVal() == 0 &&
                        (UsefulBits.isEmpty(s.getSwitchType()))) {
                    switch (s.getType()) {
                        case DomoticzValues.Scene.Type.SCENE:
                            withButton = WITHBUTTON;
                            break;
                        case DomoticzValues.Scene.Type.GROUP:
                            withButton = WITHBUTTON;
                            break;
                    }
                } else {
                    switch (s.getSwitchTypeVal()) {
                        case DomoticzValues.Device.Type.Value.ON_OFF:
                        case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                        case DomoticzValues.Device.Type.Value.DOORCONTACT:
                            if (mSharedPrefs.showSwitchesAsButtons())
                                withButton = WITHBUTTON;
                            else
                                withButton = WITHBUTTON;
                            break;

                        case DomoticzValues.Device.Type.Value.X10SIREN:
                        case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                        case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                        case DomoticzValues.Device.Type.Value.DOORBELL:
                        case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        case DomoticzValues.Device.Type.Value.DIMMER:
                        case DomoticzValues.Device.Type.Value.SELECTOR:
                            withButton = WITHBUTTON;
                            break;

                    }
                }
            }
            return withButton;
        }
    }
}