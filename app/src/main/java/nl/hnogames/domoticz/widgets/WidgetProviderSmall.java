package nl.hnogames.domoticz.widgets;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.widgets.database.WidgetContract;
import nl.hnogames.domoticz.widgets.database.WidgetDbHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;

public class WidgetProviderSmall extends AppWidgetProvider {
    private static final int iVoiceAction = -55;
    private static final int iQRCodeAction = -66;
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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        packageName = context.getPackageName();
        Log.d("WidgetProviderSmall", "onUpdate called with widgetIds: " + Arrays.toString(appWidgetIds));
        WidgetDbHelper dbHelper = new WidgetDbHelper(context);

        try {
            if (appWidgetIds != null) {
                for (int widgetId : appWidgetIds) {
                    ContentValues values = dbHelper.getWidgetConfiguration(widgetId);
                    int idx = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX);
                    int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);

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
                        Log.e("WidgetProviderLarge", "Error starting service: " + ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("WIDGET", ex.getMessage());
        }
    }

    public static class UpdateWidgetService extends Service {
        private static final int WITHBUTTON = 1;

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

        public void updateAppWidget(final AppWidgetManager appWidgetManager,
                                    final int appWidgetId) {
            if (appWidgetId == INVALID_APPWIDGET_ID) {
                Log.i("WIDGET", "I am invalid");
                return;
            }

            WidgetDbHelper dbHelper = new WidgetDbHelper(getApplicationContext());
            ContentValues values = dbHelper.getWidgetConfiguration(appWidgetId);

            int idx = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX);
            if (idx == iVoiceAction) {
                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                RemoteViews views = new RemoteViews(packageName, layoutId);
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.Speech_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_speech));
                views.setImageViewResource(R.id.rowIcon, nl.hnogames.domoticzapi.R.drawable.mic);
                views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else if (idx == iQRCodeAction) {
                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                RemoteViews views = new RemoteViews(packageName, layoutId);
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.qrcode_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_qrcode_scan));
                views.setImageViewResource(R.id.rowIcon, nl.hnogames.domoticzapi.R.drawable.qrcode);
                views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else {
                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                RemoteViews views = new RemoteViews(packageName, layoutId);
                appWidgetManager.updateAppWidget(appWidgetId, views);
                final boolean isScene = values.getAsBoolean(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE);
                if (!isScene) {
                    StaticHelper.getDomoticz(getApplicationContext()).getDevice(new DevicesReceiver() {
                        @Override
                        public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                        }

                        @Override
                        public void onReceiveDevice(DevicesInfo s) {
                            if (s != null) {
                                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                                RemoteViews views = new RemoteViews(packageName, layoutId);

                                int withButtons = withButtons(s);
                                String text = s.getData();
                                views.setTextViewText(R.id.title, s.getName());
                                if (s.getUsage() != null && s.getUsage().length() > 0)
                                    text = s.getUsage();
                                if (s.getCounterToday() != null && s.getCounterToday().length() > 0)
                                    text += " Today: " + s.getCounterToday();
                                if (s.getCounter() != null && s.getCounter().length() > 0 &&
                                        !s.getCounter().equals(s.getData()))
                                    text += " Total: " + s.getCounter();

                                views.setTextViewText(R.id.desc, text);
                                if (withButtons == WITHBUTTON && s.getStatus() != null) {
                                    boolean newStatus = !s.getStatusBoolean();//toggle

                                    views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            newStatus,
                                            true));
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
                                if (!s.getStatusBoolean())
                                    views.setInt(R.id.rowIcon, "setAlpha", 100);
                                else
                                    views.setInt(R.id.rowIcon, "setAlpha", 255);

                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                        }
                    }, idx, false);
                } else {
                    StaticHelper.getDomoticz(getApplicationContext()).getScene(new ScenesReceiver() {
                        @Override
                        public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                        }

                        @Override
                        public void onError(Exception error) {
                        }

                        @Override
                        public void onReceiveScene(SceneInfo s) {
                            if (s != null) {
                                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                                RemoteViews views = new RemoteViews(packageName, layoutId);

                                if (s.getStatusInString() != null) {
                                    views.setTextViewText(R.id.title, s.getName());
                                    views.setTextViewText(R.id.desc, s.getStatusInString());
                                    views.setOnClickPendingIntent(R.id.rowIcon, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            idx,
                                            !s.getStatusInBoolean(),
                                            true));
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getType(), null, null, false, false, null));
                                if (!s.getStatusInBoolean())
                                    views.setInt(R.id.rowIcon, "setAlpha", 100);
                                else
                                    views.setInt(R.id.rowIcon, "setAlpha", 255);

                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                        }
                    }, idx);
                }
            }
        }

        public PendingIntent buildButtonPendingIntent(Context context, int widget_id, int idx, boolean action, boolean toggle) {
            Intent intent = new Intent(this, WidgetIntentService.class);
            intent.setAction("nl.hnogames.domoticz.Service.WIDGET_TOGGLE_ACTION");
            intent.putExtra("IDX", idx);
            intent.putExtra("WIDGETID", widget_id);
            intent.putExtra("WIDGETACTION", action);
            intent.putExtra("WIDGETTOGGLE", toggle);
            intent.putExtra("WIDGETSMALL", true);

            if (toggle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return PendingIntent.getForegroundService(context, widget_id, intent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    return PendingIntent.getService(context, widget_id, intent, PendingIntent.FLAG_IMMUTABLE);
                }
            else if (action)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return PendingIntent.getForegroundService(context, widget_id + 8888, intent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    return PendingIntent.getService(context, widget_id + 8888, intent, PendingIntent.FLAG_IMMUTABLE);
                }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return PendingIntent.getForegroundService(context, widget_id + 9999, intent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    return PendingIntent.getService(context, widget_id + 9999, intent, PendingIntent.FLAG_IMMUTABLE);
                }
            }
        }

        private int withButtons(DevicesInfo s) {
            int withButton = 0;
            if (s != null) {
                if (s.getSwitchTypeVal() == 0 &&
                        (UsefulBits.isEmpty(s.getSwitchType()))) {
                    switch (s.getType()) {
                        case DomoticzValues.Scene.Type.SCENE:
                            withButton = WITHBUTTON;
                            break;
                        case DomoticzValues.Scene.Type.GROUP:
                            withButton = WITHBUTTON;
                            break;
                    }
                } else {
                    switch (s.getSwitchTypeVal()) {
                        case DomoticzValues.Device.Type.Value.ON_OFF:
                        case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                        case DomoticzValues.Device.Type.Value.DOORCONTACT:
                            withButton = WITHBUTTON;
                            break;

                        case DomoticzValues.Device.Type.Value.X10SIREN:
                        case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                        case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                        case DomoticzValues.Device.Type.Value.DOORBELL:
                        case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        case DomoticzValues.Device.Type.Value.DIMMER:
                        case DomoticzValues.Device.Type.Value.SELECTOR:
                            withButton = WITHBUTTON;
                            break;

                    }
                }
            }
            return withButton;
        }
    }
}