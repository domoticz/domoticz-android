package nl.hnogames.domoticz.widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper to trigger widget reloads from the app (e.g. on open or on preference change)
 */
public class WidgetUpdateHelper {
    private static final String TAG = "WidgetUpdateHelper";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void requestImmediateReload(Context context) {
        Log.d(TAG, "Requesting immediate widget reload");
        // Run off the UI thread
        executor.execute(() -> reloadWidgetsNow(context));
    }

    public static void reloadWidgetsNow(Context context) {
        try {
            AppWidgetManager awm = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, DomoticzWidget.class);
            int[] ids = awm.getAppWidgetIds(cn);
            Log.d(TAG, "Found " + ids.length + " widget(s) to update");
            for (int id : ids) {
                try {
                    DomoticzWidget.updateAppWidget(context, awm, id);
                } catch (Exception e) {
                    Log.e(TAG, "Failed updating widget " + id, e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while reloading widgets", e);
        }
    }
}

