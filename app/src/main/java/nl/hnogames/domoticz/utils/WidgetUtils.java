package nl.hnogames.domoticz.utils;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import nl.hnogames.domoticz.widgets.DomoticzWidget;

public class WidgetUtils {
    public static void RefreshWidgets(Context context) {
        if (context == null)
            return;
        try {
            Log.d("WidgetUtils", "Refreshing widgets...");
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

            // Refresh the new flexible widget
            refreshWidgetType(context, widgetManager, DomoticzWidget.class, "DomoticzWidget");

        } catch (Exception ex) {
            Log.e("WidgetUtils", "Error refreshing widgets", ex);
        }
    }

    private static void refreshWidgetType(Context context, AppWidgetManager widgetManager, Class<?> widgetClass, String widgetName) {
        try {
            ComponentName widgetComponent = new ComponentName(context, widgetClass);
            int[] appWidgetIds = widgetManager.getAppWidgetIds(widgetComponent);
            Log.d("WidgetUtils", "Refreshing " + widgetName + " with " + appWidgetIds.length + " instances.");

            // Trigger widget updates
            Intent intent = new Intent(context, widgetClass);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(intent);

        } catch (Exception ex) {
            Log.e("WidgetUtils", "Error refreshing " + widgetName, ex);
        }
    }
}