package nl.hnogames.domoticz.widgets;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.widgets.database.WidgetContract;
import nl.hnogames.domoticz.widgets.database.WidgetDbHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.DomoticzIcons;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;

public class WidgetProviderLarge extends AppWidgetProvider {
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
        Log.d("WidgetProviderLarge", "onUpdate called with widgetIds: " + Arrays.toString(appWidgetIds));

        WidgetDbHelper dbHelper = new WidgetDbHelper(context);

        try {
            for (int widgetId : appWidgetIds) {
                ContentValues values = dbHelper.getWidgetConfiguration(widgetId);
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
                    Log.e("WidgetProviderLarge", "Error starting service: " + ex.getMessage(), ex);
                }
            }
        } catch (Exception ex) {
            Log.e("WIDGET", ex.getMessage());
        }
    }

    public static class UpdateWidgetService extends Service {
        private static final int BUTTON_1 = 1;
        private static final int BUTTON_2 = 2;
        private static final int BUTTON_3 = 3;

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
                views.setTextViewText(R.id.on_button, "GO");
                views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                views.setViewVisibility(R.id.on_button, View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            } else if (idx == iQRCodeAction) {
                int layoutId = values.getAsInteger(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID);
                RemoteViews views = new RemoteViews(packageName, layoutId);
                views.setTextViewText(R.id.desc, getApplicationContext().getString(R.string.qrcode_desc));
                views.setTextViewText(R.id.title, getApplicationContext().getString(R.string.action_qrcode_scan));
                views.setImageViewResource(R.id.rowIcon, nl.hnogames.domoticzapi.R.drawable.qrcode);
                views.setTextViewText(R.id.on_button, "GO");
                views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                        UpdateWidgetService.this,
                        appWidgetId,
                        idx,
                        false,
                        true));
                views.setViewVisibility(R.id.on_button, View.VISIBLE);
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
                                if (withButtons == BUTTON_1 && s.getStatus() != null) {
                                    if (s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON)
                                        views.setTextViewText(R.id.on_button, getApplicationContext().getString(R.string.button_state_on));
                                    else if (s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON)
                                        views.setTextViewText(R.id.on_button, getApplicationContext().getString(R.string.button_state_off));
                                    else {
                                        if (s.getStatusBoolean())
                                            views.setTextViewText(R.id.on_button, getApplicationContext().getString(R.string.button_state_off));
                                        else
                                            views.setTextViewText(R.id.on_button, getApplicationContext().getString(R.string.button_state_on));
                                    }

                                    views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            !s.getStatusBoolean(),
                                            true));
                                    views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                } else if (withButtons == BUTTON_2 && s.getStatus() != null) {
                                    views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), true,
                                            false));
                                    views.setViewVisibility(R.id.on_button, View.VISIBLE);

                                    views.setOnClickPendingIntent(R.id.off_button, buildButtonPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), false,
                                            false));
                                    views.setViewVisibility(R.id.off_button, View.VISIBLE);

                                } else if (withButtons == BUTTON_3 && s.getStatus() != null) {
                                    views.setOnClickPendingIntent(R.id.switch_button_up, buildBlindPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            DomoticzValues.Device.Blind.Action.OFF));
                                    views.setViewVisibility(R.id.switch_button_up, View.VISIBLE);

                                    views.setOnClickPendingIntent(R.id.switch_button_stop, buildBlindPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(), DomoticzValues.Device.Blind.Action.STOP));
                                    views.setViewVisibility(R.id.switch_button_stop, View.VISIBLE);

                                    views.setOnClickPendingIntent(R.id.switch_button_down, buildBlindPendingIntent(
                                            UpdateWidgetService.this,
                                            appWidgetId,
                                            s.getIdx(),
                                            DomoticzValues.Device.Blind.Action.ON));
                                    views.setViewVisibility(R.id.switch_button_down, View.VISIBLE);
                                } else {
                                    views.setViewVisibility(R.id.on_button, View.GONE);
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getTypeImg(), s.getType(), s.getSwitchType(), true, s.getUseCustomImage(), s.getImage()));
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
                                    if (s.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
                                        views.setTextViewText(R.id.title, s.getName());
                                        views.setTextViewText(R.id.desc, s.getStatusInString());

                                        views.setTextViewText(R.id.on_button, getApplicationContext().getString(R.string.button_state_on));
                                        views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                !s.getStatusInBoolean(),
                                                true));
                                        views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                    } else {

                                        views.setTextViewText(R.id.title, s.getName());
                                        views.setTextViewText(R.id.desc, s.getStatusInString());
                                        views.setTextViewText(R.id.off_button, getApplicationContext().getString(R.string.button_state_off));
                                        views.setTextViewText(R.id.on_button, getApplicationContext().getString(R.string.button_state_on));
                                        views.setOnClickPendingIntent(R.id.on_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                true,
                                                false));
                                        views.setViewVisibility(R.id.on_button, View.VISIBLE);
                                        views.setOnClickPendingIntent(R.id.off_button, buildButtonPendingIntent(
                                                UpdateWidgetService.this,
                                                appWidgetId,
                                                idx,
                                                false,
                                                false));

                                        views.setViewVisibility(R.id.off_button, View.VISIBLE);
                                    }
                                } else {
                                    views.setViewVisibility(R.id.on_button, View.GONE);
                                }

                                views.setImageViewResource(R.id.rowIcon, DomoticzIcons.getDrawableIcon(s.getType(), null, null, false, false, null));
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

        public PendingIntent buildBlindPendingIntent(Context context, int widget_id, int idx, int action) {
            Intent intent = new Intent(this, WidgetIntentService.class);
            intent.setAction("nl.hnogames.domoticz.Service.WIDGET_BLIND_ACTION");
            intent.putExtra("IDX", idx);
            intent.putExtra("WIDGETID", widget_id);
            intent.putExtra("WIDGETACTION", false);
            intent.putExtra("WIDGETBLINDACTION", action);

            int requestCode;
            switch (action) {
                case DomoticzValues.Device.Blind.Action.ON:
                    requestCode = widget_id;
                    break;
                case DomoticzValues.Device.Blind.Action.STOP:
                    requestCode = widget_id + 7777;
                    break;
                case DomoticzValues.Device.Blind.Action.OFF:
                    requestCode = widget_id + 8888;
                    break;
                default:
                    requestCode = widget_id + 9999;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return PendingIntent.getForegroundService(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                return PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
            }
        }

        private int withButtons(DevicesInfo s) {
            int withButton = 0;
            if (s != null) {
                if (s.getSwitchTypeVal() == 0 &&
                        (UsefulBits.isEmpty(s.getSwitchType()))) {
                    switch (s.getType()) {
                        case DomoticzValues.Scene.Type.SCENE:
                            withButton = BUTTON_1;
                            break;
                        case DomoticzValues.Scene.Type.GROUP:
                            withButton = BUTTON_2;
                            break;
                    }
                } else {
                    switch (s.getSwitchTypeVal()) {
                        case DomoticzValues.Device.Type.Value.ON_OFF:
                        case DomoticzValues.Device.Type.Value.MEDIAPLAYER:
                        case DomoticzValues.Device.Type.Value.DOORCONTACT:
                        case DomoticzValues.Device.Type.Value.DIMMER:
                        case DomoticzValues.Device.Type.Value.SELECTOR:
                            //if (mSharedPrefs.showSwitchesAsButtons())
                            withButton = BUTTON_2;
                            //else
                            //    withButton = BUTTON_2;//BUTTON_1;
                            break;

                        case DomoticzValues.Device.Type.Value.X10SIREN:
                        case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                        case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                        case DomoticzValues.Device.Type.Value.SMOKE_DETECTOR:
                        case DomoticzValues.Device.Type.Value.DOORBELL:
                            withButton = BUTTON_1;
                            break;

                        case DomoticzValues.Device.Type.Value.BLINDVENETIAN:
                        case DomoticzValues.Device.Type.Value.BLINDVENETIANUS:
                            withButton = BUTTON_3;
                            break;

                        case DomoticzValues.Device.Type.Value.BLINDPERCENTAGE:
                        case DomoticzValues.Device.Type.Value.BLINDPERCENTAGESTOP:
                            withButton = BUTTON_2;
                            break;

                        case DomoticzValues.Device.Type.Value.BLINDS:
                        case DomoticzValues.Device.Type.Value.BLINDSTOP:
                            if (DomoticzValues.canHandleStopButton(s) ||
                                    (s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDSTOP))
                                withButton = BUTTON_3;
                            else
                                withButton = BUTTON_2;
                            break;
                    }
                }
            }
            return withButton;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}