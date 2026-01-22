package nl.hnogames.domoticz.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.widgets.data.WidgetRepository;
import nl.hnogames.domoticz.widgets.database.WidgetDatabase;

/**
 * Flexible widget provider that adapts to all sizes
 * Provides beautiful Material Design 3 UI for Domoticz devices
 */
public class DomoticzWidget extends AppWidgetProvider {
    private static final String TAG = "DomoticzWidget";
    private static final String ACTION_TOGGLE = "nl.hnogames.domoticz.widgets.ACTION_TOGGLE";
    private static final String ACTION_BLIND_STOP = "nl.hnogames.domoticz.widgets.ACTION_BLIND_STOP";
    private static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_TOGGLE.equals(intent.getAction())) {
            int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d(TAG, "Toggle action received for widget ID: " + widgetId);
                toggleDevice(context, widgetId);
            }
        } else if (ACTION_BLIND_STOP.equals(intent.getAction())) {
            int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                Log.d(TAG, "Blind stop action received for widget ID: " + widgetId);
                blindStopAction(context, widgetId);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Log.d(TAG, "onUpdate called for " + appWidgetIds.length + " widgets");
        for (int widgetId : appWidgetIds) {
            Log.d(TAG, "Updating widget ID: " + widgetId);
            updateAppWidget(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        Log.d(TAG, "onDeleted called for " + appWidgetIds.length + " widgets");
        WidgetRepository repository = getRepository(context);
        for (int widgetId : appWidgetIds) {
            Log.d(TAG, "Deleting widget ID: " + widgetId);
            repository.deleteWidget(widgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG, "Widget enabled - first widget added to home screen");
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG, "Widget disabled - last widget removed from home screen");
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                         int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        Log.d(TAG, "Widget " + appWidgetId + " size changed to: " + minWidth + "x" + minHeight);

        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        Log.d(TAG, "updateAppWidget called for widget ID: " + widgetId);
        WidgetRepository repository = getRepository(context);

        repository.getWidgetData(widgetId, data -> {
            Log.d(TAG, "Widget data received for ID " + widgetId + ", data is null: " + (data == null));

            if (data == null) {
                Log.w(TAG, "No widget configuration found for ID " + widgetId + ", showing empty view");
                // Show empty/configuration needed view
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_empty);

                // Set click to open configuration
                Intent configIntent = new Intent(context, WidgetConfigActivity.class);
                configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
                configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent configPendingIntent = PendingIntent.getActivity(context, widgetId, configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent);

                appWidgetManager.updateAppWidget(widgetId, views);
                return;
            }

            Log.d(TAG, "Creating widget view for ID " + widgetId);
            // Determine layout based on widget size
            RemoteViews views = createWidgetView(context, widgetId, data, appWidgetManager);
            appWidgetManager.updateAppWidget(widgetId, views);
            Log.d(TAG, "Widget updated successfully for ID " + widgetId);
        });
    }

    private static RemoteViews createWidgetView(Context context, int widgetId,
                                               WidgetRepository.WidgetData data,
                                               AppWidgetManager appWidgetManager) {
        // Get widget dimensions
        Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Choose layout based on aspect ratio and size
        int layoutId;
        String layoutName;

        // Calculate aspect ratio to determine orientation
        float aspectRatio = minHeight > 0 ? (float) minHeight / (float) minWidth : 1.0f;

        // Use compact layout ONLY for very small 1-cell widgets (1x2 or 2x1)
        // But still respect orientation even for these
        if ((minWidth < 150 && minHeight < 220) || (minHeight < 150 && minWidth < 220)) {
            // Very small 1x2 or 2x1 widget
            if (aspectRatio > 1.2f) {
                // 1x2 vertical - use detailed/vertical for better display
                layoutId = R.layout.widget_detailed;
                layoutName = "detailed (1x2 vertical)";
            } else {
                // 2x1 horizontal - use compact
                layoutId = R.layout.widget_compact;
                layoutName = "compact (2x1 horizontal)";
            }
        } else if (minWidth < 250 && minHeight < 250 && aspectRatio >= 0.8f && aspectRatio <= 1.2f) {
            // 2x2 widget (nearly square, medium size) - use compact vertical medium layout
            layoutId = R.layout.widget_medium;
            layoutName = "medium (2x2 compact vertical)";
        } else if (aspectRatio > 1.2f) {
            // Clearly vertical (height is at least 20% more than width) - use detailed/vertical layout
            layoutId = R.layout.widget_detailed;
            layoutName = "detailed (vertical)";
        } else if (aspectRatio < 0.8f) {
            // Clearly horizontal (width is at least 20% more than height) - use standard/horizontal layout
            layoutId = R.layout.widget_standard;
            layoutName = "standard (horizontal)";
        } else {
            // Nearly square but larger - use detailed/vertical layout
            layoutId = R.layout.widget_detailed;
            layoutName = "detailed (square)";
        }

        Log.d(TAG, "Using " + layoutName + " layout for widget " + widgetId +
            " (size: " + minWidth + "x" + minHeight + ", aspect ratio: " +
            String.format("%.2f", aspectRatio) + ")");

        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);

        // Populate widget data
        populateWidgetData(context, views, data, widgetId);

        return views;
    }

    private static void populateWidgetData(Context context, RemoteViews views,
                                          WidgetRepository.WidgetData data, int widgetId) {
        Log.d(TAG, "Populating widget data - Device: " + (data.deviceInfo != null) +
            ", IsScene: " + (data.config != null && data.config.isScene) +
            ", Config: " + (data.config != null ? data.config.entityName : "null"));

        if (data.deviceInfo != null) {
            // Device widget
            String deviceName = data.deviceInfo.getName();
            String deviceStatus = getDeviceStatus(data.deviceInfo);
            int deviceIcon = getDeviceIcon(data.deviceInfo);

            Log.d(TAG, "Device widget - Name: " + deviceName + ", Status: " + deviceStatus +
                ", Icon: " + deviceIcon + ", Type: " + data.deviceInfo.getType());

            views.setTextViewText(R.id.widget_title, deviceName);
            views.setTextViewText(R.id.widget_status, deviceStatus);
            views.setImageViewResource(R.id.widget_icon, deviceIcon);

            // Populate extra info for larger widgets
            populateExtraInfo(views, data.deviceInfo);

            // Dim icon when device is off (like DashboardAdapter does)
            if (!data.deviceInfo.getStatusBoolean()) {
                views.setInt(R.id.widget_icon, "setImageAlpha", 128); // 0.5 alpha = 128/255
            } else {
                views.setInt(R.id.widget_icon, "setImageAlpha", 255); // Full alpha
            }

            int buttonType = getButtonType(data.deviceInfo);

            // Hide all buttons by default
            views.setViewVisibility(R.id.widget_toggle_button, android.view.View.GONE);

            if (buttonType == 1) {
                // Single toggle button - make icon clickable for toggle
                Intent toggleIntent = new Intent(context, DomoticzWidget.class);
                toggleIntent.setAction(ACTION_TOGGLE);
                toggleIntent.putExtra(EXTRA_WIDGET_ID, widgetId);
                PendingIntent togglePendingIntent = PendingIntent.getBroadcast(context, widgetId,
                    toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_icon, togglePendingIntent);
                Log.d(TAG, "Made icon clickable for single toggle: " + deviceName);
            } else if (buttonType == 2) {
                // On/Off buttons - make icon clickable for toggle
                Intent toggleIntent = new Intent(context, DomoticzWidget.class);
                toggleIntent.setAction(ACTION_TOGGLE);
                toggleIntent.putExtra(EXTRA_WIDGET_ID, widgetId);
                PendingIntent togglePendingIntent = PendingIntent.getBroadcast(context, widgetId,
                    toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_icon, togglePendingIntent);
                Log.d(TAG, "Made icon clickable for on/off toggle: " + deviceName);
            } else if (buttonType == 3) {
                // Blinds with 3 buttons (up/stop/down)
                // Make icon toggle up/down based on current state
                Intent toggleIntent = new Intent(context, DomoticzWidget.class);
                toggleIntent.setAction(ACTION_TOGGLE);
                toggleIntent.putExtra(EXTRA_WIDGET_ID, widgetId);
                PendingIntent togglePendingIntent = PendingIntent.getBroadcast(context, widgetId,
                    toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_icon, togglePendingIntent);

                // Show stop button for blinds
                views.setViewVisibility(R.id.widget_toggle_button, android.view.View.VISIBLE);
                views.setImageViewResource(R.id.widget_toggle_button, android.R.drawable.ic_media_pause);
                views.setInt(R.id.widget_toggle_button, "setColorFilter", 0xFFFF9800); // Orange for stop

                Intent stopIntent = new Intent(context, DomoticzWidget.class);
                stopIntent.setAction(ACTION_BLIND_STOP);
                stopIntent.putExtra(EXTRA_WIDGET_ID, widgetId);
                PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, widgetId + 7777,
                    stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_toggle_button, stopPendingIntent);
                Log.d(TAG, "Made icon clickable for blind toggle and added stop button: " + deviceName);
            } else {
                // Not toggleable - icon click opens app (no button)
                Intent intent = new Intent(context, nl.hnogames.domoticz.MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                views.setOnClickPendingIntent(R.id.widget_icon, pendingIntent);
            }

            // Container always opens app
            Intent intent = new Intent(context, nl.hnogames.domoticz.MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        } else {
            // No device data - show config name as fallback
            Log.w(TAG, "No device or scene info available! Using fallback data from config.");

            if (data.config != null && data.config.entityName != null) {
                views.setTextViewText(R.id.widget_title, data.config.entityName);
                views.setTextViewText(R.id.widget_status, "Loading...");
                views.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher);
                Log.d(TAG, "Showing loading state with entity name: " + data.config.entityName);
            } else {
                views.setTextViewText(R.id.widget_title, "No Data");
                views.setTextViewText(R.id.widget_status, "Tap to configure");
                views.setImageViewResource(R.id.widget_icon, R.mipmap.ic_launcher);
            }

            // Hide toggle button
            views.setViewVisibility(R.id.widget_toggle_button, android.view.View.GONE);

            // Set click to open app
            Intent intent = new Intent(context, nl.hnogames.domoticz.MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        }
    }

    /**
     * Populate extra information fields for larger widgets
     * Only shows ADDITIONAL data that's not already in the main status
     */
    private static void populateExtraInfo(RemoteViews views, nl.hnogames.domoticzapi.Containers.DevicesInfo device) {
        StringBuilder extraInfo = new StringBuilder();

        // Get main status to check for duplicates
        String mainStatus = getDeviceStatus(device);

        // Battery level - always additional info
        if (device.getBatteryLevel() > 0 && device.getBatteryLevel() <= 100) {
            extraInfo.append("Battery ").append(device.getBatteryLevel()).append("%");
        }

        // Signal strength - always additional info
        int signalLevel = device.getSignalLevel();
        if (signalLevel > 0) {
            if (extraInfo.length() > 0) extraInfo.append(" • ");
            extraInfo.append("Signal ").append(signalLevel);
        }

        // Temperature - only if NOT a temp sensor (additional context for other sensors)
        long temp = device.getTemp();
        if (temp != 0 && device.getType() != null && !device.getType().contains("Temp") &&
            !device.getType().contains("Humidity")) {  // Not main sensor type
            String tempStr = temp + "°C";
            if (!mainStatus.contains(tempStr)) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(tempStr);
            }
        }

        // Dew Point - only if NOT a temp sensor (additional data)
        long dewPoint = device.getDewPoint();
        if (dewPoint != 0 && device.getType() != null && !device.getType().contains("Temp")) {
            String dewStr = "Dew " + dewPoint + "°C";
            if (!mainStatus.contains(String.valueOf(dewPoint))) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(dewStr);
            }
        }

        // Humidity status - only if NOT a humidity sensor (additional context)
        if (device.getHumidityStatus() != null && !device.getHumidityStatus().isEmpty() &&
            (device.getType() == null || (!device.getType().contains("Humidity") &&
             (device.getSubType() == null || !device.getSubType().contains("Humidity"))))) {
            if (!mainStatus.contains(device.getHumidityStatus())) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(device.getHumidityStatus());
            }
        }

        // Barometer / Pressure - always additional info
        if (device.getBarometer() > 0) {
            String baroStr = device.getBarometer() + " hPa";
            if (!mainStatus.contains(baroStr)) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(baroStr);
            }
        }

        // Wind speed - only if NOT a wind device (additional context)
        if (device.getSpeed() != null && !device.getSpeed().isEmpty() &&
            (device.getType() == null || !device.getType().equals("Wind"))) {
            if (!mainStatus.contains(device.getSpeed())) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(device.getSpeed());
            }
        }

        // Wind chill - only if NOT a wind device (additional context)
        if (device.getChill() != null && !device.getChill().isEmpty() &&
            (device.getType() == null || !device.getType().equals("Wind"))) {
            if (!mainStatus.contains(device.getChill())) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append("Chill " + device.getChill());
            }
        }

        // Rain - only if NOT a rain device (additional context)
        if (device.getRain() != null && !device.getRain().isEmpty() &&
            (device.getType() == null || !device.getType().equals("Rain"))) {
            if (!mainStatus.contains(device.getRain())) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(device.getRain());
            }
        }

        // Power/Usage - ONLY show if NOT already in main status (not a power device)
        if (device.getUsage() != null && !device.getUsage().isEmpty()) {
            String usage = device.getUsage().replace(" Watt", "").trim();
            if (!usage.contains("W") && !usage.contains("kW")) {
                usage = usage + " W";
            }

            // Don't show if already in main status
            if (!mainStatus.contains(usage) && !mainStatus.contains(device.getUsage())) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                extraInfo.append(usage);
            }
        }

        // Counter Today - ONLY if NOT already shown in main status
        if (device.getCounterToday() != null && !device.getCounterToday().isEmpty()) {
            String counter = device.getCounterToday();

            // Don't show if already in main status
            if (!mainStatus.contains(counter)) {
                if (extraInfo.length() > 0) extraInfo.append(" • ");
                // Ensure unit is present
                if (!counter.contains("kWh") && !counter.contains("Wh") && !counter.contains("W") && !counter.contains("L") && !counter.contains("m")) {
                    try {
                        Double.parseDouble(counter.trim());
                        counter = counter + " kWh";
                    } catch (Exception e) {
                        // Already has unit
                    }
                }
                extraInfo.append(counter);
            }
        }

        // Show extra info if we have any
        try {
            if (extraInfo.length() > 0) {
                views.setTextViewText(R.id.widget_extra_info, extraInfo.toString());
                views.setViewVisibility(R.id.widget_extra_info, android.view.View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.widget_extra_info, android.view.View.GONE);
            }
        } catch (Exception e) {
            // widget_extra_info doesn't exist in this layout (compact widget)
            Log.d(TAG, "widget_extra_info not available in this layout");
        }

        // Last update time (only for detailed layout)
        try {
            if (device.getLastUpdate() != null && !device.getLastUpdate().isEmpty()) {
                String lastUpdate = "Updated: " + device.getLastUpdate();
                views.setTextViewText(R.id.widget_last_update, lastUpdate);
                views.setViewVisibility(R.id.widget_last_update, android.view.View.VISIBLE);
            } else {
                views.setViewVisibility(R.id.widget_last_update, android.view.View.GONE);
            }
        } catch (Exception e) {
            // widget_last_update doesn't exist in this layout
            Log.d(TAG, "widget_last_update not available in this layout");
        }
    }

    private static String getDeviceStatus(nl.hnogames.domoticzapi.Containers.DevicesInfo device) {
        String status = "";

        // Handle usage/power devices (like P1 Smart Meter, power monitoring)
        if (device.getUsage() != null && !device.getUsage().isEmpty()) {
            try {
                int usage = Integer.parseInt(device.getUsage().replace("Watt", "").trim());
                if (device.getUsageDeliv() != null && device.getUsageDeliv().length() > 0) {
                    int usageDel = Integer.parseInt(device.getUsageDeliv().replace("Watt", "").trim());
                    status = (usage - usageDel) + " W";
                } else {
                    status = usage + " W";
                }
            } catch (Exception ex) {
                status = device.getUsage().replace(" Watt", " W");
            }

            // Add today's counter if available
            if (device.getCounterToday() != null && !device.getCounterToday().isEmpty()) {
                String counter = device.getCounterToday();
                // Ensure unit is present
                if (!counter.contains("kWh") && !counter.contains("Wh") && !counter.contains("L") && !counter.contains("m³")) {
                    try {
                        Double.parseDouble(counter.trim());
                        counter = counter + " kWh";
                    } catch (Exception e) {
                        // Already has unit
                    }
                }
                status += " • " + counter;
            }
            return status;
        }

        // Handle temperature devices
        if (device.getType() != null && device.getType().contains("Temp")) {
            if (device.getData() != null && !device.getData().isEmpty()) {
                return device.getData();
            }
        }

        // Handle humidity devices
        if (device.getType() != null && (device.getType().contains("Humidity") ||
            device.getSubType() != null && device.getSubType().contains("Humidity"))) {
            if (device.getData() != null && !device.getData().isEmpty()) {
                return device.getData();
            }
        }

        // Handle wind devices
        if (device.getType() != null && device.getType().equals("Wind")) {
            if (device.getDirection() != null && device.getDirectionStr() != null) {
                return device.getDirection() + " " + device.getDirectionStr();
            }
        }

        // Handle rain devices
        if (!android.text.TextUtils.isEmpty(device.getRain())) {
            status = device.getRain();
            if (!android.text.TextUtils.isEmpty(device.getRainRate())) {
                status += " • " + device.getRainRate();
            }
            return status;
        }

        // Handle forecast devices
        if (!android.text.TextUtils.isEmpty(device.getForecastStr())) {
            return device.getForecastStr();
        }

        // Handle counter devices (water, gas, etc.)
        if (device.getCounterToday() != null && !device.getCounterToday().isEmpty()) {
            String counter = device.getCounterToday();
            // Ensure unit is present if it's just a number
            if (!counter.contains("kWh") && !counter.contains("Wh") && !counter.contains("W") &&
                !counter.contains("L") && !counter.contains("m³") && !counter.contains("m3")) {
                try {
                    Double.parseDouble(counter.trim());
                    counter = counter + " kWh";
                } catch (Exception e) {
                    // Already has unit
                }
            }
            status = counter;
        }

        // Default: use Data field, then Status field
        if (status.isEmpty()) {
            status = device.getData();
        }
        if (status == null || status.isEmpty()) {
            status = device.getStatus();
        }

        // Clean up common patterns and ensure proper units
        if (status != null) {
            status = status.replace(" Watt", " W");
        }

        return status != null && !status.isEmpty() ? status : "Unknown";
    }

    private static int getDeviceIcon(nl.hnogames.domoticzapi.Containers.DevicesInfo device) {
        return nl.hnogames.domoticzapi.DomoticzIcons.getDrawableIcon(
            device.getTypeImg(),
            device.getType(),
            device.getSwitchType(),
            true,
            device.getUseCustomImage(),
            device.getImage()
        );
    }

    /**
     * Determines button type for device
     * @return 0 = no buttons, 1 = single toggle, 2 = on/off buttons, 3 = blinds (up/stop/down)
     */
    private static int getButtonType(nl.hnogames.domoticzapi.Containers.DevicesInfo device) {
        if (device.getSwitchTypeVal() == 0 && android.text.TextUtils.isEmpty(device.getSwitchType())) {
            switch (device.getType()) {
                case nl.hnogames.domoticzapi.DomoticzValues.Scene.Type.SCENE:
                    return 1;
                case nl.hnogames.domoticzapi.DomoticzValues.Scene.Type.GROUP:
                    return 2;
            }
            return 0;
        }

        switch (device.getSwitchTypeVal()) {
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.ON_OFF:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.MEDIAPLAYER:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.DOORCONTACT:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.DIMMER:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.SELECTOR:
                return 2;

            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.X10SIREN:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.DOORBELL:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.DOORLOCK:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.DOORLOCKINVERTED:
                return 1;

            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDVENETIAN:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP:
            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDSTOP:
                // These always have stop button
                return 3;

            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                return 2;

            case nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDS:
                // BLINDS type may or may not have stop button depending on subtype
                if (nl.hnogames.domoticzapi.DomoticzValues.canHandleStopButton(device)) {
                    return 3;
                }
                return 2;

            default:
                return 0;
        }
    }

    /**
     * Toggle a device on/off
     */
    private static void toggleDevice(Context context, int widgetId) {
        Log.d(TAG, "Toggling device for widget ID: " + widgetId);
        WidgetRepository repository = getRepository(context);

        repository.getWidgetData(widgetId, data -> {
            if (data == null || data.deviceInfo == null) {
                Log.w(TAG, "Cannot toggle - no device data for widget " + widgetId);
                return;
            }

            nl.hnogames.domoticzapi.Containers.DevicesInfo device = data.deviceInfo;
            int switchType = device.getSwitchTypeVal();

            Log.d(TAG, "Toggle device: " + device.getName() + ", switchType=" + switchType);

            // Check if this is a blind device - use blind-specific logic
            boolean isBlind = isBlindDevice(switchType);
            Log.d(TAG, "Is blind device: " + isBlind);

            if (isBlind) {
                Log.d(TAG, "Using blind-specific toggle logic");
                toggleBlindDevice(context, widgetId, device);
                return;
            }

            boolean currentState = device.getStatusBoolean();
            boolean newState = !currentState;

            Log.d(TAG, "Toggling device " + device.getName() + " from " + currentState + " to " + newState +
                  ", switchType=" + switchType);

            int idx = device.getIdx();
            int jsonAction;
            int jsonUrl = nl.hnogames.domoticzapi.DomoticzValues.Json.Url.Set.SWITCHES;

            // Determine the correct action based on device type (from Dashboard.java logic)
            if (device.getType().equals(nl.hnogames.domoticzapi.DomoticzValues.Scene.Type.GROUP) ||
                device.getType().equals(nl.hnogames.domoticzapi.DomoticzValues.Scene.Type.SCENE)) {
                // Groups and Scenes
                jsonUrl = nl.hnogames.domoticzapi.DomoticzValues.Json.Url.Set.SCENES;
                jsonAction = newState ? nl.hnogames.domoticzapi.DomoticzValues.Scene.Action.ON
                                     : nl.hnogames.domoticzapi.DomoticzValues.Scene.Action.OFF;
            } else if (switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
                // Inverted logic for inverted door locks
                jsonAction = newState ? nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.OFF
                                     : nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.ON;
                Log.d(TAG, "Using inverted logic for inverted lock");
            } else if (switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.SELECTOR) {
                // Selector switches - toggle between off (0) and first level (10)
                if (currentState) {
                    // Currently ON - turn OFF (level 0)
                    jsonAction = nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.OFF;
                } else {
                    // Currently OFF - turn to first level (level 10)
                    jsonAction = nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.ON;
                }
                Log.d(TAG, "Selector switch toggle: " + (currentState ? "OFF" : "ON to first level"));
            } else {
                // Standard switches, dimmers, etc.
                jsonAction = newState ? nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.ON
                                     : nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.OFF;
            }

            Log.d(TAG, "Executing action: idx=" + idx + ", url=" + jsonUrl + ", action=" + jsonAction);

            // Perform the action
            nl.hnogames.domoticz.helpers.StaticHelper.getDomoticz(context).setAction(
                idx, jsonUrl, jsonAction, 0, null,
                new nl.hnogames.domoticzapi.Interfaces.setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        Log.d(TAG, "Toggle successful for device: " + device.getName() + ", result: " + result);
                        // Update the widget to reflect the new state
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        updateAppWidget(context, appWidgetManager, widgetId);
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Toggle failed for device: " + device.getName(), error);
                        // Still update the widget in case server state changed
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        updateAppWidget(context, appWidgetManager, widgetId);
                    }
                });
        });
    }

    /**
     * Check if device is a blind type
     */
    private static boolean isBlindDevice(int switchType) {
        return switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDS ||
               switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDSTOP ||
               switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
               switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP ||
               switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDVENETIAN ||
               switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDVENETIANUS;
    }

    /**
     * Toggle a blind device - uses UP/DOWN actions instead of ON/OFF
     */
    private static void toggleBlindDevice(Context context, int widgetId, nl.hnogames.domoticzapi.Containers.DevicesInfo device) {
        String status = device.getStatus();
        int idx = device.getIdx();
        int switchType = device.getSwitchTypeVal();
        int jsonAction;

        Log.d(TAG, "==== BLIND TOGGLE DEBUG ====");
        Log.d(TAG, "Device: " + device.getName());
        Log.d(TAG, "Idx: " + idx);
        Log.d(TAG, "SwitchType: " + switchType + " (" + device.getSwitchType() + ")");
        Log.d(TAG, "Status: " + status);
        Log.d(TAG, "StatusBoolean: " + device.getStatusBoolean());
        Log.d(TAG, "Data: " + device.getData());

        // Different blind types use different action values!
        // BLINDS and BLINDPERCENTAGE use Switch.Action.ON/OFF (10/11) - inverted logic
        // BLINDVENETIAN, BLINDVENETIANUS, BLINDSTOP, BLINDPERCENTAGESTOP use Blind.Action.UP/DOWN (30/32)

        boolean useBlindActions = (switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDVENETIAN ||
                                  switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDVENETIANUS ||
                                  switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDSTOP ||
                                  switchType == nl.hnogames.domoticzapi.DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP);

        if (useBlindActions) {
            // Use Blind.Action.OPEN/CLOSE (35/36) for these types
            if (status != null && (status.equals(nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.State.OPEN) ||
                                   status.equals(nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.State.ON) ||
                                   status.contains("Open"))) {
                // Currently open - close it with CLOSE action
                jsonAction = nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.Action.CLOSE;
                Log.d(TAG, "Blind currently OPEN, sending Blind.Action.CLOSE (36) command");
            } else {
                // Currently closed or stopped - open it with OPEN action
                jsonAction = nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.Action.OPEN;
                Log.d(TAG, "Blind currently CLOSED/STOPPED (status=" + status + "), sending Blind.Action.OPEN (35) command");
            }
        } else {
            // Use Switch.Action.ON/OFF (10/11) for BLINDS and BLINDPERCENTAGE - INVERTED LOGIC
            // When blind is closed (OFF state), send ON to open it
            // When blind is open (ON state), send OFF to close it
            boolean currentState = device.getStatusBoolean();
            if (currentState) {
                // Currently ON (open) - close it with OFF action
                jsonAction = nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.OFF;
                Log.d(TAG, "Blind currently ON/OPEN, sending Switch.Action.OFF (11) command to close");
            } else {
                // Currently OFF (closed) - open it with ON action
                jsonAction = nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.ON;
                Log.d(TAG, "Blind currently OFF/CLOSED, sending Switch.Action.ON (10) command to open");
            }
        }

        int jsonUrl = nl.hnogames.domoticzapi.DomoticzValues.Json.Url.Set.SWITCHES;

        Log.d(TAG, "API Call - URL type: " + jsonUrl + ", Action: " + jsonAction + ", UseBlindActions: " + useBlindActions);
        Log.d(TAG, "Action constants - Switch.ON:" + nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.ON +
                   ", Switch.OFF:" + nl.hnogames.domoticzapi.DomoticzValues.Device.Switch.Action.OFF);
        Log.d(TAG, "Action constants - Blind.OPEN:" + nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.Action.OPEN +
                   ", Blind.CLOSE:" + nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.Action.CLOSE +
                   ", Blind.STOP:" + nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.Action.STOP);
        Log.d(TAG, "===========================");

        // Perform the action
        nl.hnogames.domoticz.helpers.StaticHelper.getDomoticz(context).setAction(
            idx, jsonUrl, jsonAction, 0, null,
            new nl.hnogames.domoticzapi.Interfaces.setCommandReceiver() {
                @Override
                public void onReceiveResult(String result) {
                    Log.d(TAG, "Blind toggle result: " + result);
                    if (result.contains("ERROR")) {
                        Log.e(TAG, "Blind toggle failed for device: " + device.getName() + ", error in result: " + result);
                    } else {
                        Log.d(TAG, "Blind toggle successful for device: " + device.getName());
                    }
                    // Update the widget to reflect the new state
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    updateAppWidget(context, appWidgetManager, widgetId);
                }

                @Override
                public void onError(Exception error) {
                    Log.e(TAG, "Blind toggle failed for device: " + device.getName(), error);
                    // Still update the widget in case server state changed
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    updateAppWidget(context, appWidgetManager, widgetId);
                }
            });
    }

    /**
     * Stop a blind device
     */
    private static void blindStopAction(Context context, int widgetId) {
        Log.d(TAG, "Blind stop action for widget ID: " + widgetId);
        WidgetRepository repository = getRepository(context);

        repository.getWidgetData(widgetId, data -> {
            if (data == null || data.deviceInfo == null) {
                Log.w(TAG, "Cannot stop blind - no device data for widget " + widgetId);
                return;
            }

            nl.hnogames.domoticzapi.Containers.DevicesInfo device = data.deviceInfo;
            int idx = device.getIdx();

            Log.d(TAG, "Stopping blind device: " + device.getName() + ", idx=" + idx);

            // Perform the stop action
            nl.hnogames.domoticz.helpers.StaticHelper.getDomoticz(context).setAction(
                idx,
                nl.hnogames.domoticzapi.DomoticzValues.Json.Url.Set.SWITCHES,
                nl.hnogames.domoticzapi.DomoticzValues.Device.Blind.Action.STOP,
                0,
                null,
                new nl.hnogames.domoticzapi.Interfaces.setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        Log.d(TAG, "Blind stop successful for device: " + device.getName() + ", result: " + result);
                        // Update the widget to reflect the new state
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        updateAppWidget(context, appWidgetManager, widgetId);
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e(TAG, "Blind stop failed for device: " + device.getName(), error);
                        // Still update the widget in case server state changed
                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                        updateAppWidget(context, appWidgetManager, widgetId);
                    }
                });
        });
    }

    private static WidgetRepository getRepository(Context context) {
        WidgetDatabase db = WidgetDatabase.getInstance(context);
        return new WidgetRepository(context, db.widgetDao());
    }
}
