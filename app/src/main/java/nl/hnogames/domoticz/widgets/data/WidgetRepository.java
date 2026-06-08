package nl.hnogames.domoticz.widgets.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;

/**
 * Repository for widget data management
 * Handles data persistence and API calls
 */
public class WidgetRepository {
    private static final String TAG = "WidgetRepository";

    private final Context context;
    private final WidgetDao widgetDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public WidgetRepository(Context context, WidgetDao widgetDao) {
        this.context = context.getApplicationContext();
        this.widgetDao = widgetDao;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void saveWidgetConfig(int widgetId, int entityIdx, String entityName,
                                 String entityType, boolean isScene, String layoutStyle,
                                 String themeStyle, String password) {
        executorService.execute(() -> {
            WidgetEntity entity = new WidgetEntity(
                widgetId, entityIdx, entityName, entityType, isScene,
                layoutStyle, themeStyle, System.currentTimeMillis(), password, null
            );
            widgetDao.insertWidget(entity);
        });
    }

    public void saveWidgetConfig(int widgetId, int entityIdx, String entityName,
                                 String entityType, boolean isScene, String layoutStyle,
                                 String themeStyle, String password, Runnable onComplete) {
        Log.d(TAG, "Saving widget config - ID: " + widgetId + ", entityIdx: " + entityIdx +
              ", name: " + entityName + ", isScene: " + isScene);
        executorService.execute(() -> {
            try {
                WidgetEntity entity = new WidgetEntity(
                    widgetId, entityIdx, entityName, entityType, isScene,
                    layoutStyle, themeStyle, System.currentTimeMillis(), password, null
                );
                widgetDao.insertWidget(entity);
                Log.d(TAG, "Widget config saved successfully for ID: " + widgetId);
            } catch (Exception e) {
                Log.e(TAG, "Error saving widget config for ID: " + widgetId + " - " + e.getMessage(), e);
            }
            // Always invoke onComplete so the config activity can finish and place the widget
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
        });
    }

    public void getWidgetData(int widgetId, WidgetDataCallback callback) {
        Log.d(TAG, "Getting widget data for ID: " + widgetId);
        executorService.execute(() -> {
            WidgetEntity widget = widgetDao.getWidget(widgetId);
            if (widget == null) {
                Log.w(TAG, "No widget entity found in database for ID: " + widgetId);
                mainHandler.post(() -> callback.onResult(null));
                return;
            }

            Log.d(TAG, "Widget entity found - ID: " + widgetId + ", entityIdx: " + widget.entityIdx +
                  ", isScene: " + widget.isScene);

            // Fetch data on main thread since it's a network call
            mainHandler.post(() -> {
                if (widget.isScene) {
                    // Scenes/Groups live on a separate Domoticz endpoint (/json.htm?type=scenes)
                    // getDevice() only covers regular devices; using it for scenes returns null.
                    Log.d(TAG, "Fetching scene/group data for idx: " + widget.entityIdx);
                    getSceneAsDevice(widget.entityIdx, device -> {
                        Log.d(TAG, "Scene data received: " + (device != null ? device.getName() : "null"));
                        WidgetData data = new WidgetData(widget, device);
                        callback.onResult(data);
                    });
                } else {
                    Log.d(TAG, "Fetching device data for idx: " + widget.entityIdx);
                    getDevice(widget.entityIdx, device -> {
                        Log.d(TAG, "Device data received: " + (device != null ? device.getName() : "null"));
                        WidgetData data = new WidgetData(widget, device);
                        callback.onResult(data);
                    });
                }
            });
        });
    }

    public LiveData<WidgetEntity> getWidgetLiveData(int widgetId) {
        return widgetDao.getWidgetLiveData(widgetId);
    }

    public void deleteWidget(int widgetId) {
        executorService.execute(() -> widgetDao.deleteWidget(widgetId));
    }

    public void updateTimestamp(int widgetId) {
        executorService.execute(() ->
            widgetDao.updateTimestamp(widgetId, System.currentTimeMillis())
        );
    }

    public LiveData<List<WidgetEntity>> getAllWidgets() {
        return widgetDao.getAllWidgets();
    }

    private void getDevice(int idx, DeviceCallback callback) {
        try {
            Log.d(TAG, "Calling API to get device with idx: " + idx);

            // Validate that Domoticz is properly configured
            // Use refresh=true to ensure we pick up current network (local/remote) settings
            nl.hnogames.domoticzapi.Domoticz domoticz = StaticHelper.getDomoticz(context, true);
            if (domoticz == null) {
                Log.e(TAG, "Domoticz instance is null!");
                callback.onDevice(null);
                return;
            }

            nl.hnogames.domoticzapi.Containers.ServerInfo activeServer =
                domoticz.getServerUtil().getActiveServer();
            if (activeServer == null) {
                Log.e(TAG, "No active server configured!");
                callback.onDevice(null);
                return;
            }

            Log.d(TAG, "Active server: " + activeServer.getServerName() +
                  ", URL: " + activeServer.getRemoteServerUrl());

            // Track if callback was called
            final boolean[] callbackCalled = {false};

            // Set timeout - if no response in 10 seconds, assume failure
            mainHandler.postDelayed(() -> {
                if (!callbackCalled[0]) {
                    Log.e(TAG, "API timeout - no response received for device idx " + idx);
                    callback.onDevice(null);
                    callbackCalled[0] = true;
                }
            }, 10000);

            Log.d(TAG, "Executing getDevice API call for idx: " + idx);
            domoticz.getDevice(new DevicesReceiver() {
                @Override
                public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                    // Not used
                    Log.d(TAG, "onReceiveDevices called (not used)");
                }

                @Override
                public void onReceiveDevice(DevicesInfo device) {
                    if (!callbackCalled[0]) {
                        if (device != null) {
                            Log.d(TAG, "API returned device: " + device.getName() + " (idx: " + device.getIdx() + ", type: " + device.getType() + ")");
                        } else {
                            Log.e(TAG, "API returned null device for idx " + idx + " - Device may not exist or may be disabled (Used=0). Check if device exists in main app!");
                        }
                        callback.onDevice(device);
                        callbackCalled[0] = true;
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (!callbackCalled[0]) {
                        Log.e(TAG, "Error fetching device idx " + idx, error);
                        callback.onDevice(null);
                        callbackCalled[0] = true;
                    }
                }
            }, idx, false);

            Log.d(TAG, "getDevice API call initiated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception calling getDevice for idx " + idx, e);
            callback.onDevice(null);
        }
    }

    /**
     * Fetches a scene or group by idx via the /json.htm?type=scenes endpoint and converts it
     * to a DevicesInfo object so the rest of the widget code can handle it uniformly.
     */
    private void getSceneAsDevice(int idx, DeviceCallback callback) {
        try {
            nl.hnogames.domoticzapi.Domoticz domoticz = StaticHelper.getDomoticz(context, true);
            if (domoticz == null) {
                Log.e(TAG, "Domoticz instance is null for getSceneAsDevice!");
                callback.onDevice(null);
                return;
            }

            final boolean[] callbackCalled = {false};

            // Timeout safety – mirror the same guard used in getDevice()
            mainHandler.postDelayed(() -> {
                if (!callbackCalled[0]) {
                    Log.e(TAG, "Scene API timeout for idx " + idx);
                    callback.onDevice(null);
                    callbackCalled[0] = true;
                }
            }, 10000);

            Log.d(TAG, "Calling getScene API for idx: " + idx);
            domoticz.getScene(new ScenesReceiver() {
                @Override
                public void onReceiveScenes(java.util.ArrayList<SceneInfo> scenes) {
                    // Not used – we requested a single scene via idx
                }

                @Override
                public void onReceiveScene(SceneInfo scene) {
                    if (!callbackCalled[0]) {
                        callbackCalled[0] = true;
                        if (scene == null) {
                            Log.e(TAG, "getScene returned null for idx " + idx);
                            callback.onDevice(null);
                            return;
                        }
                        Log.d(TAG, "Scene data received: " + scene.getName()
                                + " (idx: " + scene.getIdx() + ", type: " + scene.getType()
                                + ", status: " + scene.getStatusInString() + ")");

                        // Convert SceneInfo → DevicesInfo so widget code is uniform
                        DevicesInfo deviceInfo = new DevicesInfo();
                        deviceInfo.setIdx(scene.getIdx());
                        deviceInfo.setName(scene.getName());
                        deviceInfo.setStatus(scene.getStatusInString()); // "On" / "Off"
                        deviceInfo.setType(scene.getType());             // "Scene" or "Group"
                        deviceInfo.setLastUpdate(scene.getLastUpdate());
                        // TypeImg drives the icon lookup in DomoticzIcons ("scene" / "group")
                        deviceInfo.setTypeImg(scene.getType() != null ? scene.getType().toLowerCase() : "scene");
                        callback.onDevice(deviceInfo);
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (!callbackCalled[0]) {
                        callbackCalled[0] = true;
                        Log.e(TAG, "Error fetching scene idx " + idx, error);
                        callback.onDevice(null);
                    }
                }
            }, idx);

            Log.d(TAG, "getScene API call initiated for idx: " + idx);
        } catch (Exception e) {
            Log.e(TAG, "Exception calling getScene for idx " + idx, e);
            callback.onDevice(null);
        }
    }

    public interface WidgetDataCallback {
        void onResult(WidgetData data);
    }

    private interface DeviceCallback {
        void onDevice(DevicesInfo device);
    }

    public static class WidgetData {
        public final WidgetEntity config;
        public final DevicesInfo deviceInfo;

        public WidgetData(WidgetEntity config, DevicesInfo deviceInfo) {
            this.config = config;
            this.deviceInfo = deviceInfo;
        }
    }
}
