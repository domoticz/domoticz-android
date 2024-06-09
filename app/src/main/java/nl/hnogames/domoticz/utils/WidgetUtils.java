package nl.hnogames.domoticz.utils;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import nl.hnogames.domoticz.widgets.WidgetProviderLarge;
import nl.hnogames.domoticz.widgets.WidgetProviderSmallTemp;

public class WidgetUtils {
    public static void RefreshWidgets(Context context) {
        if (context == null)
            return;
        try {
            AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

            ComponentName widgetComponentLarge = new ComponentName(context, WidgetProviderLarge.class);
            int[] appWidgetLargeIds = widgetManager.getAppWidgetIds(widgetComponentLarge);
            for (int appWidgetId : appWidgetLargeIds) {
                Intent updateIntent = new Intent(context, WidgetProviderLarge.UpdateWidgetService.class);
                updateIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
                updateIntent.setAction("FROM WIDGET PROVIDER");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(updateIntent);
                else
                    context.startService(updateIntent);
            }

            ComponentName widgetComponent = new ComponentName(context, WidgetProviderSmallTemp.class);
            int[] appWidgetIds = widgetManager.getAppWidgetIds(widgetComponent);
            for (int appWidgetId : appWidgetIds) {
                Intent updateIntent = new Intent(context, WidgetProviderSmallTemp.UpdateWidgetService.class);
                updateIntent.putExtra(EXTRA_APPWIDGET_ID, appWidgetId);
                updateIntent.setAction("FROM WIDGET PROVIDER");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    context.startForegroundService(updateIntent);
                else
                    context.startService(updateIntent);
            }
        } catch (Exception ex) {
            Log.e("WidgetUtils", "Error refreshing widgets", ex);
        }
    }
}