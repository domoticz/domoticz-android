package nl.hnogames.domoticz.widgets;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.widgets.database.WidgetContract;
import nl.hnogames.domoticz.widgets.database.WidgetDbHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class WidgetProviderSmallTemp extends AppWidgetProvider {
    private static String packageName;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        WidgetDbHelper dbHelper = new WidgetDbHelper(context);
        for (int widgetId : appWidgetIds) {
            dbHelper.deleteWidgetConfiguration(widgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        packageName = context.getPackageName();
        WidgetDbHelper dbHelper = new WidgetDbHelper(context);
        for (int widgetId : appWidgetIds) {
            // Retrieve widget configuration from the database
            ContentValues values = dbHelper.getWidgetConfiguration(widgetId);
            try {
                int idx = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX);
                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);

                // Start the update service with retrieved configuration
                Intent intent = new Intent(context, UpdateWidgetService.class);
                intent.putExtra(EXTRA_APPWIDGET_ID, widgetId);
                intent.putExtra("IDX", idx); // Pass IDX to UpdateWidgetService
                intent.putExtra("LAYOUT_ID", layoutId); // Pass layout ID to UpdateWidgetService
                intent.setAction("FROM WIDGET PROVIDER");

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent);
                    } else {
                        context.startService(intent);
                    }
                } catch (Exception ex) {
                    Log.e("WidgetProviderSmallTemp", "Error starting service: " + ex.getMessage(), ex);
                }
            } catch (Exception e) {
                Log.e("WidgetProviderSmallTemp", "Widget configuration not found for ID: " + widgetId);
            }
        }
    }

    public static class UpdateWidgetService extends Service {
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

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int appWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID);

            if (appWidgetId != INVALID_APPWIDGET_ID) {
                updateAppWidget(appWidgetManager, appWidgetId);
            } else {
                Log.e("UpdateWidgetService", "Invalid AppWidget ID");
            }

            stopSelf();
            return START_NOT_STICKY;
        }

        private void updateAppWidget(AppWidgetManager appWidgetManager, int appWidgetId) {
            WidgetDbHelper dbHelper = new WidgetDbHelper(getApplicationContext());
            ContentValues values = dbHelper.getWidgetConfiguration(appWidgetId);
            try {
                int idx = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX);
                StaticHelper.getDomoticz(getApplicationContext()).getDevice(new DevicesReceiver() {
                    @Override
                    public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                    }

                    @Override
                    public void onReceiveDevice(DevicesInfo s) {
                        if (s != null) {
                            int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                            updateViewsWithDeviceInfo(appWidgetManager, appWidgetId, s, layoutId);
                        }
                    }

                    @Override
                    public void onError(Exception error) {
                        Log.e("UpdateWidgetService", "Error fetching device info: " + error.getMessage(), error);
                    }
                }, idx, false);
            } catch (Exception e) {
                Log.e("WidgetProviderSmallTemp", "Widget configuration not found for ID: " + appWidgetId);
            }
        }

        private void updateViewsWithDeviceInfo(AppWidgetManager appWidgetManager, int appWidgetId, DevicesInfo s, int layoutId) {
            double temperature = s.getTemperature();
            String sign = "C"; // Default to Celsius

            if (StaticHelper.getDomoticz(getApplicationContext()).getServerUtil() != null
                    && StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer() != null
                    && StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()) != null) {
                sign = StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()).getTempSign();
            }

            String text = s.getData();
            if (!Double.isNaN(temperature)) {
                text = temperature + " " + sign;
            }

            packageName = getPackageName();
            RemoteViews views = new RemoteViews(packageName, layoutId);
            appWidgetManager.updateAppWidget(appWidgetId, views);

            views.setTextViewText(R.id.title, text);
            views.setTextViewText(R.id.desc, s.getName());

            int icon = DomoticzIcons.getDrawableIcon(
                    s.getTypeImg(),
                    s.getType(),
                    null,
                    s.getTemperature() > StaticHelper.getDomoticz(getApplicationContext()).getServerUtil().getActiveServer().getConfigInfo(getApplicationContext()).getDegreeDaysBaseTemperature(),
                    sign.equals("C") && s.getTemperature() < 0 || sign.equals("F") && s.getTemperature() < 30,
                    "Freezing"
            );

            views.setImageViewResource(R.id.rowIcon, icon);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
