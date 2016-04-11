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
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;

public class NotificationUtil {

    final static String GROUP_KEY_NOTIFICATIONS = "domoticz_notifications";

    public static void sendSimpleNotification(String title, String text, Context context) {
        if (UsefulBits.isEmpty(title) || UsefulBits.isEmpty(text) || context == null)
            return;

        try {
            int NOTIFICATION_ID = 12345;
            SharedPrefUtil prefUtil = new SharedPrefUtil(context);
            if (prefUtil.isNotificationsEnabled() &&
                    prefUtil.getSuppressedNotifications() != null &&
                    !prefUtil.getSuppressedNotifications().contains(text)) {

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.domoticz_white)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                                .setContentTitle(title)
                                .setContentText(text)
                                .setGroupSummary(true)
                                .setGroup(GROUP_KEY_NOTIFICATIONS)
                                .setAutoCancel(true);

                if (!prefUtil.OverWriteNotifications()) {
                    NOTIFICATION_ID = text.hashCode();
                }
                if (prefUtil.getNotificationVibrate())
                    builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

                if (!UsefulBits.isEmpty(prefUtil.getNotificationSound()))
                    builder.setSound(Uri.parse(prefUtil.getNotificationSound()));

                Intent targetIntent = new Intent(context, MainActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                nManager.notify(NOTIFICATION_ID, builder.build());
            }

            prefUtil.addReceivedNotification(text);
        } catch (Exception ex) {
        }
    }
}
