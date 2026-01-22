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
            WidgetEntity entity = new WidgetEntity(
                widgetId, entityIdx, entityName, entityType, isScene,
                layoutStyle, themeStyle, System.currentTimeMillis(), password, null
            );
            widgetDao.insertWidget(entity);
            Log.d(TAG, "Widget config saved successfully for ID: " + widgetId);
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

            // Fetch device/scene data on main thread since it's a network call
            mainHandler.post(() -> {
                if (widget.isScene) {
                    Log.d(TAG, "Fetching scene data for idx: " + widget.entityIdx);
                    getScene(widget.entityIdx, scene -> {
                        Log.d(TAG, "Scene data received: " + (scene != null ? scene.getName() : "null"));
                        WidgetData data = new WidgetData(widget, null, scene);
                        callback.onResult(data);
                    });
                } else {
                    Log.d(TAG, "Fetching device data for idx: " + widget.entityIdx);
                    getDevice(widget.entityIdx, device -> {
                        Log.d(TAG, "Device data received: " + (device != null ? device.getName() : "null"));
                        WidgetData data = new WidgetData(widget, device, null);
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
            nl.hnogames.domoticzapi.Domoticz domoticz = StaticHelper.getDomoticz(context);
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
            }, idx, true); // Changed to true to include groups and scenes!

            Log.d(TAG, "getDevice API call initiated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception calling getDevice for idx " + idx, e);
            callback.onDevice(null);
        }
    }

    private void getScene(int idx, SceneCallback callback) {
        try {
            Log.d(TAG, "Calling API to get scene with idx: " + idx);

            // Validate that Domoticz is properly configured
            nl.hnogames.domoticzapi.Domoticz domoticz = StaticHelper.getDomoticz(context);
            if (domoticz == null) {
                Log.e(TAG, "Domoticz instance is null!");
                callback.onScene(null);
                return;
            }

            nl.hnogames.domoticzapi.Containers.ServerInfo activeServer =
                domoticz.getServerUtil().getActiveServer();
            if (activeServer == null) {
                Log.e(TAG, "No active server configured!");
                callback.onScene(null);
                return;
            }

            Log.d(TAG, "Active server: " + activeServer.getServerName() +
                  ", URL: " + activeServer.getRemoteServerUrl());

            // Track if callback was called
            final boolean[] callbackCalled = {false};

            // Set timeout - if no response in 10 seconds, assume failure
            mainHandler.postDelayed(() -> {
                if (!callbackCalled[0]) {
                    Log.e(TAG, "API timeout - no response received for scene idx " + idx);
                    callback.onScene(null);
                    callbackCalled[0] = true;
                }
            }, 10000);

            Log.d(TAG, "Executing getScene API call for idx: " + idx);
            domoticz.getScene(new ScenesReceiver() {
                @Override
                public void onReceiveScenes(ArrayList<SceneInfo> scenes) {
                    if (!callbackCalled[0]) {
                        Log.d(TAG, "API returned scenes list: " + (scenes != null ? scenes.size() : "null"));
                        if (scenes != null && !scenes.isEmpty()) {
                            callback.onScene(scenes.get(0));
                        } else {
                            callback.onScene(null);
                        }
                        callbackCalled[0] = true;
                    }
                }

                @Override
                public void onReceiveScene(SceneInfo scene) {
                    if (!callbackCalled[0]) {
                        Log.d(TAG, "API returned scene: " + (scene != null ? scene.getName() : "null"));
                        callback.onScene(scene);
                        callbackCalled[0] = true;
                    }
                }

                @Override
                public void onError(Exception error) {
                    if (!callbackCalled[0]) {
                        Log.e(TAG, "Error fetching scene idx " + idx, error);
                        callback.onScene(null);
                        callbackCalled[0] = true;
                    }
                }
            }, idx);

            Log.d(TAG, "getScene API call initiated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception calling getScene for idx " + idx, e);
            callback.onScene(null);
        }
    }

    public interface WidgetDataCallback {
        void onResult(WidgetData data);
    }

    private interface DeviceCallback {
        void onDevice(DevicesInfo device);
    }

    private interface SceneCallback {
        void onScene(SceneInfo scene);
    }

    public static class WidgetData {
        public final WidgetEntity config;
        public final DevicesInfo deviceInfo;
        public final SceneInfo sceneInfo;

        public WidgetData(WidgetEntity config, DevicesInfo deviceInfo, SceneInfo sceneInfo) {
            this.config = config;
            this.deviceInfo = deviceInfo;
            this.sceneInfo = sceneInfo;
        }
    }
}
