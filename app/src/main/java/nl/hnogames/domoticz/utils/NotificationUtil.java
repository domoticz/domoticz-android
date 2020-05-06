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

package nl.hnogames.domoticz.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.NotificationHistoryActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.service.RingtonePlayingService;
import nl.hnogames.domoticz.service.StopAlarmButtonListener;

public class NotificationUtil {
    private static final String MESSAGE_CONVERSATION_ID_KEY = "conversaton_key";
    private static final String VOICE_REPLY_KEY = "voice_reply_key";
    private static final String MESSAGE_READ_ACTION = "nl.hnogames.domoticz.Service.ACTION_MESSAGE_READ";
    private static final String MESSAGE_REPLY_ACTION = "nl.hnogames.domoticz.Service.ACTION_MESSAGE_REPLY";
    private static final String UNREAD_CONVERSATION_BUILDER_NAME = "Domoticz -";
    public static String CHANNEL_ID = "Domoticz";
    private static int NOTIFICATION_ID = 12345;

    public static void sendSimpleNotification(NotificationInfo notification, final Context context) {
        if (notification == null || UsefulBits.isEmpty(notification.getTitle()) || UsefulBits.isEmpty(notification.getText()) || context == null)
            return;

        SharedPrefUtil prefUtil = new SharedPrefUtil(context);
        String loggedNotification = notification.getTitle();
        if (notification.getTitle().equals(context.getString(R.string.app_name_domoticz)))
            loggedNotification = notification.getText();

        prefUtil.addUniqueReceivedNotification(loggedNotification);
        prefUtil.addLoggedNotification(notification);
        List<String> suppressedNot = prefUtil.getSuppressedNotifications();
        List<String> alarmNot = prefUtil.getAlarmNotifications();

        try {
            if (prefUtil.isNotificationsEnabled() && suppressedNot != null && !suppressedNot.contains(notification.getText())) {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    CreateChannel(CHANNEL_ID, notification.getPriority(), false, context);

                Intent historyIntent = new Intent(context, NotificationHistoryActivity.class);
                PendingIntent historyPendingIntent = PendingIntent.getActivity(context, 0, historyIntent, 0);
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(context, CHANNEL_ID)
                                .setSmallIcon(R.drawable.domoticz_white)
                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                                .setContentTitle(alarmNot != null && alarmNot.contains(loggedNotification) ? context.getString(R.string.alarm) + ": " + notification.getTitle() : notification.getTitle())
                                .setContentText(alarmNot != null && alarmNot.contains(loggedNotification) ? context.getString(R.string.alarm) + ": " + notification.getText() : notification.getText())
                                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(notification.getText()))
                                .addAction(R.drawable.baseline_notification_important_white_24, context.getString(R.string.notification_inbox), historyPendingIntent)
                                .setAutoCancel(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int nrOfNotifications = 1;
                    if (mNotificationManager != null) {
                        StatusBarNotification[] activeNotifications = mNotificationManager.getActiveNotifications();
                        if (activeNotifications != null) {
                            nrOfNotifications = activeNotifications.length;
                        }
                    }
                    builder.setNumber(nrOfNotifications);
                }

                if (!prefUtil.OverWriteNotifications())
                    NOTIFICATION_ID = notification.getText().hashCode();

                if (!UsefulBits.isEmpty(prefUtil.getNotificationSound()))
                    builder.setSound(Uri.parse(prefUtil.getNotificationSound()));

                Intent targetIntent = new Intent(context, MainActivity.class);
                if (notification.getIdx() > -1)
                    targetIntent.putExtra("TARGETIDX", notification.getIdx());
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                if (prefUtil.isNotificationsEnabled() && alarmNot != null && alarmNot.contains(loggedNotification)) {
                    Intent stopAlarmIntent = new Intent(context, StopAlarmButtonListener.class);
                    PendingIntent pendingAlarmIntent = PendingIntent.getService(context, 78578, stopAlarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.addAction(android.R.drawable.ic_delete, "Stop", pendingAlarmIntent);
                }
                if (prefUtil.showAutoNotifications()) {
                    builder.extend(new NotificationCompat.CarExtender()
                            .setUnreadConversation(getUnreadConversation(context, notification.getText())));
                }

                if (mNotificationManager != null)
                    mNotificationManager.notify(NOTIFICATION_ID, builder.build());
                HandleAlarmSounds(context, loggedNotification, alarmNot);
            }
        } catch (Exception ex) {
            if (ex != null)
                Log.i("NOTIFY", ex.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void CreateChannel(String channelId, int priority, boolean backgroundProcess, Context context) {
        SharedPrefUtil prefUtil = new SharedPrefUtil(context);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelId,
                channelId,
                GetPriority(priority));

        if (!backgroundProcess) {
            channel.setShowBadge(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.setLightColor(Color.BLUE);
        }
        if (!backgroundProcess && prefUtil.getNotificationVibrate()) {
            channel.enableVibration(true);
        }
        if (mNotificationManager != null)
            mNotificationManager.createNotificationChannel(channel);
    }

    public static Notification getForegroundServiceNotification(Context context, String channelId) {
        Intent targetIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            CreateChannel(channelId, NotificationManager.IMPORTANCE_DEFAULT, true, context);

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.domoticz_white)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Domoticz")
                .setContentText("Processing widget request..")
                .setContentIntent(contentIntent).build();
    }

    private static int GetPriority(int priority) {
        int prio = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            prio = NotificationManager.IMPORTANCE_DEFAULT;
            switch (priority) {
                case 1:
                    prio = NotificationManager.IMPORTANCE_HIGH;
                    break;
                case 2:
                    prio = NotificationManager.IMPORTANCE_MAX;
                    break;
                case -1:
                    prio = NotificationManager.IMPORTANCE_LOW;
                    break;
                case -2:
                    prio = NotificationManager.IMPORTANCE_MIN;
                    break;
            }
        }
        return prio;
    }

    private static void HandleAlarmSounds(Context context, String loggedNotification, List<String> alarmNot) throws InterruptedException {
        SharedPrefUtil prefUtil = new SharedPrefUtil(context);
        if (prefUtil.isNotificationsEnabled() && alarmNot != null && alarmNot.contains(loggedNotification)) {
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

                if (prefUtil.getAlarmTimer() > 0) {
                    Thread.sleep(prefUtil.getAlarmTimer() * 1000);
                    Intent stopIntent = new Intent(context, RingtonePlayingService.class);
                    context.stopService(stopIntent);
                }
            }
        }
    }

    private static Intent getMessageReadIntent() {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(MESSAGE_READ_ACTION)
                .putExtra(MESSAGE_CONVERSATION_ID_KEY, NOTIFICATION_ID);
    }

    private static PendingIntent getMessageReadPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context,
                NOTIFICATION_ID,
                getMessageReadIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Intent getMessageReplyIntent() {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(MESSAGE_REPLY_ACTION)
                .putExtra(MESSAGE_CONVERSATION_ID_KEY, NOTIFICATION_ID);
    }

    private static PendingIntent getMessageReplyPendingIntent(Context context) {
        return PendingIntent.getBroadcast(context,
                NOTIFICATION_ID,
                getMessageReplyIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static RemoteInput getVoiceReplyRemoteInput() {
        return new RemoteInput.Builder(VOICE_REPLY_KEY)
                .setLabel("Reply")
                .build();
    }

    private static NotificationCompat.CarExtender.UnreadConversation getUnreadConversation(Context context, String text) {
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConversationBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder(UNREAD_CONVERSATION_BUILDER_NAME + text);
        unreadConversationBuilder.setReadPendingIntent(getMessageReadPendingIntent(context));
        unreadConversationBuilder.setReplyAction(getMessageReplyPendingIntent(context), getVoiceReplyRemoteInput());
        unreadConversationBuilder.addMessage(text);
        unreadConversationBuilder.setLatestTimestamp(Calendar.getInstance().get(Calendar.SECOND));
        return unreadConversationBuilder.build();
    }
}
