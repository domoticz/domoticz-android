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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import java.util.Calendar;
import java.util.List;

import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.service.NotificationActionReceiver;
import nl.hnogames.domoticz.service.RingtonePlayingService;
import nl.hnogames.domoticz.service.StopAlarmButtonListener;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

import java.util.ArrayList;

public class NotificationUtil {
    private static final String MESSAGE_CONVERSATION_ID_KEY = "conversaton_key";
    private static final String VOICE_REPLY_KEY = "voice_reply_key";
    private static final String MESSAGE_READ_ACTION = "nl.hnogames.domoticz.Service.ACTION_MESSAGE_READ";
    private static final String MESSAGE_REPLY_ACTION = "nl.hnogames.domoticz.Service.ACTION_MESSAGE_REPLY";
    private static final String UNREAD_CONVERSATION_BUILDER_NAME = "Domoticz -";
    public static String CHANNEL_ID = "Domoticz";
    public static String CHANNEL_ID_SWITCHES = "Domoticz_Switches";
    public static String CHANNEL_ID_SENSORS = "Domoticz_Sensors";
    public static String CHANNEL_ID_WEATHER = "Domoticz_Weather";
    public static String CHANNEL_ID_UTILITY = "Domoticz_Utility";
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

                // Create all notification channels
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CreateChannel(CHANNEL_ID, notification.getPriority(), false, context);
                    CreateChannel(CHANNEL_ID_SWITCHES, notification.getPriority(), false, context);
                    CreateChannel(CHANNEL_ID_SENSORS, notification.getPriority(), false, context);
                    CreateChannel(CHANNEL_ID_WEATHER, notification.getPriority(), false, context);
                    CreateChannel(CHANNEL_ID_UTILITY, notification.getPriority(), false, context);
                }

                if (!prefUtil.OverWriteNotifications())
                    NOTIFICATION_ID = notification.getText().hashCode();

                // Check if we have a device idx - if so, fetch device info first for actionable notification
                if (notification.getIdx() > 0) {
                    buildActionableNotification(context, notification, prefUtil, loggedNotification, alarmNot, mNotificationManager, NOTIFICATION_ID);
                } else {
                    // No device idx - show standard notification immediately
                    showStandardNotification(context, notification, prefUtil, loggedNotification, alarmNot, mNotificationManager, NOTIFICATION_ID);
                }
            }
        } catch (Exception ex) {
            if (ex != null)
                Log.i("NOTIFY", ex.getMessage());
        }
    }

    /**
     * Build and show standard notification without action buttons
     */
    private static void showStandardNotification(Context context, NotificationInfo notification,
                                                SharedPrefUtil prefUtil, String loggedNotification,
                                                List<String> alarmNot, NotificationManager mNotificationManager,
                                                int notificationId) {
        NotificationCompat.Builder builder = createBaseNotificationBuilder(context, notification, prefUtil,
                                                                          loggedNotification, alarmNot, notificationId);

        if (mNotificationManager != null)
            mNotificationManager.notify(notificationId, builder.build());

        try {
            HandleAlarmSounds(context, loggedNotification, alarmNot);
        } catch (InterruptedException e) {
            Log.e("NotificationUtil", "Error handling alarm sounds", e);
        }
    }

    /**
     * Build and show actionable notification with device toggle buttons
     */
    private static void buildActionableNotification(Context context, NotificationInfo notification,
                                                   SharedPrefUtil prefUtil, String loggedNotification,
                                                   List<String> alarmNot, NotificationManager mNotificationManager,
                                                   int notificationId) {
        Log.d("NotificationUtil", "Building actionable notification for device idx: " + notification.getIdx());

        // Fetch device info first, then build notification with action buttons
        StaticHelper.getDomoticz(context).getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevice(DevicesInfo device) {
                if (device == null) {
                    Log.w("NotificationUtil", "Device not found, showing standard notification");
                    showStandardNotification(context, notification, prefUtil, loggedNotification,
                                           alarmNot, mNotificationManager, notificationId);
                    return;
                }

                Log.d("NotificationUtil", "Device found: " + device.getName() +
                        ", type: " + device.getType() + ", switchType: " + device.getSwitchTypeVal());

                // Build base notification with device info
                NotificationCompat.Builder builder = createEnhancedNotificationBuilder(context, notification,
                                                                                       prefUtil, loggedNotification,
                                                                                       alarmNot, notificationId, device);

                // Determine button type
                int buttonType = getDeviceButtonType(device);
                Log.d("NotificationUtil", "Button type: " + buttonType);

                // Add action buttons if device is toggleable
                if (buttonType > 0) {
                    addActionButtons(context, builder, device, buttonType, notification.getIdx(), notificationId);
                } else {
                    Log.d("NotificationUtil", "Device is not toggleable, showing enhanced notification");
                }

                // Show the notification
                if (mNotificationManager != null)
                    mNotificationManager.notify(notificationId, builder.build());

                try {
                    HandleAlarmSounds(context, loggedNotification, alarmNot);
                } catch (InterruptedException e) {
                    Log.e("NotificationUtil", "Error handling alarm sounds", e);
                }
            }

            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> devices) {
                // Not used
            }

            @Override
            public void onError(Exception error) {
                Log.e("NotificationUtil", "Error fetching device, showing standard notification", error);
                showStandardNotification(context, notification, prefUtil, loggedNotification,
                                       alarmNot, mNotificationManager, notificationId);
            }
        }, notification.getIdx(), false);
    }

    /**
     * Create base notification builder with common settings
     */
    private static NotificationCompat.Builder createBaseNotificationBuilder(Context context,
                                                                           NotificationInfo notification,
                                                                           SharedPrefUtil prefUtil,
                                                                           String loggedNotification,
                                                                           List<String> alarmNot,
                                                                           int notificationId) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.domoticz_white)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(alarmNot != null && alarmNot.contains(loggedNotification) ?
                                        context.getString(R.string.alarm) + ": " + notification.getTitle() :
                                        notification.getTitle())
                        .setContentText(alarmNot != null && alarmNot.contains(loggedNotification) ?
                                       context.getString(R.string.alarm) + ": " + notification.getText() :
                                       notification.getText())
                        .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(notification.getText()))
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int nrOfNotifications = 1;
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                StatusBarNotification[] activeNotifications = mNotificationManager.getActiveNotifications();
                if (activeNotifications != null) {
                    nrOfNotifications = activeNotifications.length;
                }
            }
            builder.setNumber(nrOfNotifications);
        }

        if (!UsefulBits.isEmpty(prefUtil.getNotificationSound()))
            builder.setSound(Uri.parse(prefUtil.getNotificationSound()));

        Intent targetIntent = new Intent(context, MainActivity.class);
        if (notification.getIdx() > -1)
            targetIntent.putExtra("TARGETIDX", notification.getIdx());
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        if (prefUtil.isNotificationsEnabled() && alarmNot != null && alarmNot.contains(loggedNotification)) {
            Intent stopAlarmIntent = new Intent(context, StopAlarmButtonListener.class);
            PendingIntent pendingAlarmIntent = PendingIntent.getService(context, 78578, stopAlarmIntent, PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(android.R.drawable.ic_delete, "Stop", pendingAlarmIntent);
        }

        if (prefUtil.showAutoNotifications()) {
            builder.extend(new NotificationCompat.CarExtender()
                    .setUnreadConversation(getUnreadConversation(context, notification.getText())));
        }

        return builder;
    }

    /**
     * Create enhanced notification builder with device-specific icon and styling
     */
    private static NotificationCompat.Builder createEnhancedNotificationBuilder(Context context,
                                                                                NotificationInfo notification,
                                                                                SharedPrefUtil prefUtil,
                                                                                String loggedNotification,
                                                                                List<String> alarmNot,
                                                                                int notificationId,
                                                                                DevicesInfo device) {
        // Get device-specific icon
        int deviceIconRes = nl.hnogames.domoticzapi.DomoticzIcons.getDrawableIcon(
                device.getTypeImg(),
                device.getType(),
                device.getSwitchType(),
                device.getStatusBoolean(),
                device.getUseCustomImage(),
                device.getImage()
        );

        // Create large icon from device icon (scaled to 70% with padding for visual balance)
        android.graphics.Bitmap deviceIconBitmap = null;
        try {
            android.graphics.drawable.Drawable drawable = context.getResources().getDrawable(deviceIconRes, null);

            int originalWidth = drawable.getIntrinsicWidth();
            int originalHeight = drawable.getIntrinsicHeight();

            // Calculate scaled dimensions (70% of original)
            int scaledWidth = (int) (originalWidth * 0.7f);
            int scaledHeight = (int) (originalHeight * 0.7f);

            // Create bitmap at original size to maintain notification icon space
            // but draw the icon scaled down and centered
            deviceIconBitmap = android.graphics.Bitmap.createBitmap(
                    originalWidth,
                    originalHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
            );

            android.graphics.Canvas canvas = new android.graphics.Canvas(deviceIconBitmap);

            // Calculate position to center the scaled icon
            int left = (originalWidth - scaledWidth) / 2;
            int top = (originalHeight - scaledHeight) / 2;

            // Draw the icon scaled and centered
            drawable.setBounds(left, top, left + scaledWidth, top + scaledHeight);
            drawable.draw(canvas);
        } catch (Exception e) {
            Log.e("NotificationUtil", "Error creating device icon bitmap", e);
        }

        // Build enhanced notification with device info
        String title = alarmNot != null && alarmNot.contains(loggedNotification) ?
                context.getString(R.string.alarm) + ": " + notification.getTitle() :
                notification.getTitle();

        String contentText = alarmNot != null && alarmNot.contains(loggedNotification) ?
                context.getString(R.string.alarm) + ": " + notification.getText() :
                notification.getText();

        // Add device status to content text
        String deviceStatus = getDeviceStatusText(device);
        String fullContentText = contentText;
        if (!UsefulBits.isEmpty(deviceStatus)) {
            fullContentText = contentText + "\n" + device.getName() + ": " + deviceStatus;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.domoticz_white)  // Keep Domoticz logo as small icon (status bar)
                .setContentTitle(title)
                .setContentText(fullContentText)
                .setAutoCancel(true)
                .setGroup("domoticz_device_notifications")
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_ALL);

        // Set device-specific icon as large icon (notification drawer)
        if (deviceIconBitmap != null) {
            builder.setLargeIcon(deviceIconBitmap);
        } else {
            // Fallback to app icon if device icon creation failed
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher));
        }

        // Use InboxStyle for better multi-line display
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle(title)
                .addLine(contentText)
                .addLine(device.getName() + ": " + deviceStatus);

        builder.setStyle(inboxStyle);

        // Add notification count
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int nrOfNotifications = 1;
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                StatusBarNotification[] activeNotifications = mNotificationManager.getActiveNotifications();
                if (activeNotifications != null) {
                    nrOfNotifications = activeNotifications.length;
                }
            }
            builder.setNumber(nrOfNotifications);
        }

        // Set color based on device state
        if (device.getStatusBoolean()) {
            builder.setColor(0xFF4CAF50);  // Green for ON
        } else {
            builder.setColor(0xFF9E9E9E);  // Grey for OFF
        }

        // Update channel to device-specific channel
        String channelId = getChannelForDeviceType(device);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelId);
        }

        // Sound
        if (!UsefulBits.isEmpty(prefUtil.getNotificationSound()))
            builder.setSound(Uri.parse(prefUtil.getNotificationSound()));

        // Content intent
        Intent targetIntent = new Intent(context, MainActivity.class);
        targetIntent.putExtra("TARGETIDX", notification.getIdx());
        PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, targetIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        // Alarm action
        if (prefUtil.isNotificationsEnabled() && alarmNot != null && alarmNot.contains(loggedNotification)) {
            Intent stopAlarmIntent = new Intent(context, StopAlarmButtonListener.class);
            PendingIntent pendingAlarmIntent = PendingIntent.getService(context, 78578, stopAlarmIntent,
                    PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(android.R.drawable.ic_delete, "Stop Alarm", pendingAlarmIntent);
        }

        // Car extender
        if (prefUtil.showAutoNotifications()) {
            builder.extend(new NotificationCompat.CarExtender()
                    .setUnreadConversation(getUnreadConversation(context, notification.getText())));
        }

        return builder;
    }

    /**
     * Get device status as readable text
     */
    private static String getDeviceStatusText(DevicesInfo device) {
        String status = device.getData();
        if (UsefulBits.isEmpty(status)) {
            status = device.getStatus();
        }

        // Clean up status text
        if (!UsefulBits.isEmpty(status)) {
            status = status.replace(" Watt", " W");
        }

        return status != null ? status : "Unknown";
    }

    /**
     * Get appropriate notification channel based on device type
     */
    private static String getChannelForDeviceType(DevicesInfo device) {
        String type = device.getType();
        String subType = device.getSubType();
        int switchType = device.getSwitchTypeVal();

        if (type == null) {
            return CHANNEL_ID;
        }

        // Temperature/Humidity sensors
        if (type.contains("Temp") || type.contains("Humidity") ||
                (subType != null && (subType.contains("Temp") || subType.contains("Humidity")))) {
            return CHANNEL_ID_SENSORS;
        }

        // Weather devices
        if (type.equals("Wind") || type.equals("Rain") || type.equals("UV") ||
                (subType != null && (subType.contains("Weather") || subType.equals("Solar Radiation")))) {
            return CHANNEL_ID_WEATHER;
        }

        // Utility devices (power, gas, water, etc.)
        if (type.equals("P1 Smart Meter") || type.equals("YouLess Meter") ||
                type.equals("General") && (subType != null &&
                        (subType.contains("kWh") || subType.contains("Gas") ||
                                subType.contains("Water") || subType.contains("Counter") ||
                                subType.contains("Energy") || subType.contains("Percentage") ||
                                subType.equals("Custom Sensor") || subType.equals("Managed Counter")))) {
            return CHANNEL_ID_UTILITY;
        }

        // Switches, lights, and other toggleable devices
        if (switchType > 0 || type.equals("Lighting 1") || type.equals("Lighting 2") ||
                type.equals("Lighting 3") || type.equals("Lighting 4") ||
                type.equals("Lighting 5") || type.equals("Lighting 6") ||
                type.equals("Light/Switch") || type.equals("Color Switch") ||
                type.contains("RFY") || type.contains("ASA") ||
                type.equals(nl.hnogames.domoticzapi.DomoticzValues.Scene.Type.SCENE) ||
                type.equals(nl.hnogames.domoticzapi.DomoticzValues.Scene.Type.GROUP)) {
            return CHANNEL_ID_SWITCHES;
        }

        // Default channel
        return CHANNEL_ID;
    }

    /**
     * Add action buttons to notification based on device type
     */
    private static void addActionButtons(Context context, NotificationCompat.Builder builder,
                                        DevicesInfo device, int buttonType, int deviceIdx, int notificationId) {
        // Create toggle action
        Intent toggleIntent = new Intent(context, NotificationActionReceiver.class);
        toggleIntent.setAction(NotificationActionReceiver.ACTION_TOGGLE_DEVICE);
        toggleIntent.putExtra(NotificationActionReceiver.EXTRA_DEVICE_IDX, deviceIdx);
        toggleIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);

        PendingIntent togglePendingIntent = PendingIntent.getBroadcast(
                context,
                deviceIdx * 1000, // Unique request code
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String toggleLabel = getToggleLabel(device, buttonType);
        builder.addAction(android.R.drawable.ic_menu_send, toggleLabel, togglePendingIntent);
        Log.d("NotificationUtil", "Added toggle action: " + toggleLabel);

        // Add stop button for blinds
        if (buttonType == 3 && canHandleStopButton(device)) {
            Intent stopIntent = new Intent(context, NotificationActionReceiver.class);
            stopIntent.setAction(NotificationActionReceiver.ACTION_BLIND_STOP);
            stopIntent.putExtra(NotificationActionReceiver.EXTRA_DEVICE_IDX, deviceIdx);
            stopIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);

            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                    context,
                    deviceIdx * 1000 + 1, // Unique request code
                    stopIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            builder.addAction(android.R.drawable.ic_media_pause, "Stop", stopPendingIntent);
            Log.d("NotificationUtil", "Added stop action for blind");
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
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, targetIntent, PendingIntent.FLAG_IMMUTABLE);

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
                    Thread.sleep(prefUtil.getAlarmTimer() * 1000L);
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
                PendingIntent.FLAG_IMMUTABLE);
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
                PendingIntent.FLAG_IMMUTABLE);
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

    /**
     * Determine button type for device (same logic as widget)
     * @return 0 = no buttons, 1 = single toggle, 2 = on/off buttons, 3 = blinds (up/stop/down)
     */
    private static int getDeviceButtonType(DevicesInfo device) {
        if (device.getSwitchTypeVal() == 0 && android.text.TextUtils.isEmpty(device.getSwitchType())) {
            switch (device.getType()) {
                case DomoticzValues.Scene.Type.SCENE:
                    return 1;
                case DomoticzValues.Scene.Type.GROUP:
                    return 2;
            }
            return 0;
        }

        switch (device.getSwitchTypeVal()) {
            case DomoticzValues.Device.Type.Value.ON_OFF:
            case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
            case DomoticzValues.Device.Type.Value.DOORCONTACT:
            case DomoticzValues.Device.Type.Value.DIMMER:
            case DomoticzValues.Device.Type.Value.SELECTOR:
                return 2;

            case DomoticzValues.Device.Type.Value.X10SIREN:
            case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
            case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
            case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
            case DomoticzValues.Device.Type.Value.DOORBELL:
            case DomoticzValues.Device.Type.Value.DOORLOCK:
            case DomoticzValues.Device.Type.Value.DOORLOCKINVERTED:
                return 1;

            case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
            case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
            case DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP:
            case DomoticzValues.Device.Type.Value.BLINDSTOP:
                return 3;

            case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                return 2;

            case DomoticzValues.Device.Type.Value.BLINDS:
                if (canHandleStopButton(device)) {
                    return 3;
                }
                return 2;

            default:
                return 0;
        }
    }

    /**
     * Get appropriate toggle label based on device type and state
     */
    private static String getToggleLabel(DevicesInfo device, int buttonType) {
        if (buttonType == 3) {
            // Blind device
            String status = device.getStatus();
            if (status != null && (status.equals(DomoticzValues.Device.Blind.State.OPEN) ||
                    status.equals(DomoticzValues.Device.Blind.State.ON) ||
                    status.contains("Open"))) {
                return "Close";
            } else {
                return "Open";
            }
        } else {
            // Standard toggle device
            boolean currentState = device.getStatusBoolean();
            return currentState ? "Turn Off" : "Turn On";
        }
    }

    /**
     * Check if blind device supports stop button
     */
    private static boolean canHandleStopButton(DevicesInfo device) {
        return DomoticzValues.canHandleStopButton(device);
    }
}


