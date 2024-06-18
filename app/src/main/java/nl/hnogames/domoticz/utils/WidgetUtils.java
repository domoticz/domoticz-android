package nl.hnogames.domoticz.utils;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import nl.hnogames.domoticz.widgets.WidgetProviderLarge;
import nl.hnogames.domoticz.widgets.WidgetProviderSmall;
import nl.hnogames.domoticz.widgets.WidgetProviderSmallTemp;

public class WidgetUtils {
    public static void RefreshWidgets(Context context) {
        if (context == null)
            return;
        try {
            Log.d("WidgetUtils", "Refreshing widgets...");
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

            // Add logging for each widget type
            refreshWidgetType(context, widgetManager, WidgetProviderSmall.class, "WidgetProviderSmall");
            refreshWidgetType(context, widgetManager, WidgetProviderLarge.class, "WidgetProviderLarge");
            refreshWidgetType(context, widgetManager, WidgetProviderSmallTemp.class, "WidgetProviderSmallTemp");

        } catch (Exception ex) {
            Log.e("WidgetUtils", "Error refreshing widgets", ex);
        }
    }

    private static void refreshWidgetType(Context context, AppWidgetManager widgetManager, Class<?> widgetClass, String widgetName) {
        try {
            ComponentName widgetComponent = new ComponentName(context, widgetClass);
            int[] appWidgetIds = widgetManager.getAppWidgetIds(widgetComponent);
            Log.d("WidgetUtils", "Refreshing " + widgetName + " with " + appWidgetIds.length + " instances.");
            for (int appWidgetId : appWidgetIds) {
                Intent updateIntent = new Intent(context, widgetClass);
                updateIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
                updateIntent.setAction("FROM WIDGET PROVIDER");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(updateIntent);
                else
                    context.startService(updateIntent);
            }
        } catch (Exception ex) {
            Log.e("WidgetUtils", "Error refreshing " + widgetName, ex);
        }
    }
}