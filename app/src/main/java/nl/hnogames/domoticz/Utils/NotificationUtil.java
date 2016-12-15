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

package nl.hnogames.domoticz.Utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Service.RingtonePlayingService;

public class NotificationUtil {

    private final static String GROUP_KEY_NOTIFICATIONS = "domoticz_notifications";
    private static SharedPrefUtil prefUtil;

    public static void sendSimpleNotification(String title, String text, final Context context) {
        if (UsefulBits.isEmpty(title) || UsefulBits.isEmpty(text) || context == null)
            return;
        int NOTIFICATION_ID = 12345;

        if (prefUtil == null)
            prefUtil = new SharedPrefUtil(context);

        prefUtil.addUniqueReceivedNotification(text);
        prefUtil.addLoggedNotification(new SimpleDateFormat("yyyy-MM-dd hh:mm ").format(new Date()) + text);

        List<String> suppressedNot = prefUtil.getSuppressedNotifications();
        List<String> alarmNot = prefUtil.getAlarmNotifications();
        try {
            if (prefUtil.isNotificationsEnabled() && suppressedNot != null && !suppressedNot.contains(text)) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.domoticz_white)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                                .setContentTitle(alarmNot != null && alarmNot.contains(text) ? context.getString(R.string.alarm) + ": " + title : title)
                                .setContentText(alarmNot != null && alarmNot.contains(text) ? context.getString(R.string.alarm) + ": " + text : text)
                                .setGroupSummary(true)
                                .setGroup(GROUP_KEY_NOTIFICATIONS)
                                .setAutoCancel(true);

                if (!prefUtil.OverWriteNotifications())
                    NOTIFICATION_ID = text.hashCode();

                if (prefUtil.getNotificationVibrate())
                    builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

                if (!UsefulBits.isEmpty(prefUtil.getNotificationSound()))
                    builder.setSound(Uri.parse(prefUtil.getNotificationSound()));

                Intent targetIntent = new Intent(context, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nManager.notify(NOTIFICATION_ID, builder.build());

                if (prefUtil.isNotificationsEnabled() && alarmNot != null && alarmNot.contains(text)) {
                    Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    if (alert == null) {
                        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        if (alert == null)
                            alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    }
                    if (alert != null) {
                        Intent ringtoneServiceStartIntent = new Intent(context, RingtonePlayingService.class);
                        ringtoneServiceStartIntent.putExtra("ringtone-uri", alert.toString());
                        context.startService(ringtoneServiceStartIntent);

                        Thread.sleep(prefUtil.getAlarmTimer()*1000);
                        Intent stopIntent = new Intent(context, RingtonePlayingService.class);
                        context.stopService(stopIntent);
                    }
                }
            }
        } catch (Exception ex) {
            Log.i("NOTIFY", ex.getMessage());
        }
    }
}
