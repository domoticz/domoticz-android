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
import java.util.concurrent.atomic.AtomicBoolean;

import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

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

            // Fetch data on main thread since it's a network call.
            // Use the saved scene/group flag so widgets for scenes and groups resolve correctly.
            mainHandler.post(() -> {
                Log.d(TAG, "Fetching device data for idx: " + widget.entityIdx +
                      (widget.isScene ? " (Scene/Group)" : " (Device)"));
                getDevice(widget.entityIdx, widget.isScene, device -> {
                    Log.d(TAG, "Device data received: " + (device != null ? device.getName() : "null"));
                    WidgetData data = new WidgetData(widget, device);
                    callback.onResult(data);
                });
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

    private void getDevice(int idx, boolean sceneOrGroup, DeviceCallback callback) {
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

            AtomicBoolean callbackCalled = new AtomicBoolean(false);

            // Set timeout - if no response in 10 seconds, assume failure
            mainHandler.postDelayed(() -> {
                if (callbackCalled.compareAndSet(false, true)) {
                    Log.e(TAG, "API timeout - no response received for device idx " + idx);
                    callback.onDevice(null);
                }
            }, 10000);

            fetchDeviceByIdx(
                domoticz,
                idx,
                sceneOrGroup,
                callbackCalled,
                callback
            );

            Log.d(TAG, "getDevice API call initiated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception calling getDevice for idx " + idx, e);
            callback.onDevice(null);
        }
    }

    private void fetchDeviceByIdx(nl.hnogames.domoticzapi.Domoticz domoticz,
                                  int idx,
                                  boolean sceneOrGroup,
                                  AtomicBoolean callbackCalled,
                                  DeviceCallback callback) {
        Log.d(TAG, "Executing fast getDevice API call for idx: " + idx +
            (sceneOrGroup ? " (Scene/Group)" : " (Device)"));
        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                // Not used for single-device request
            }

            @Override
            public void onReceiveDevice(DevicesInfo device) {
                if (device != null) {
                    Log.d(TAG, "Fast lookup returned: " + device.getName() + " (idx: " +
                        device.getIdx() + ", type: " + device.getType() + ")");
                    deliverDevice(callbackCalled, callback, device);
                    return;
                }

                Log.w(TAG, "Fast lookup returned null for idx " + idx +
                    ". Falling back to full device list lookup.");
                fetchDeviceFromAll(domoticz, idx, sceneOrGroup, callbackCalled, callback);
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Fast lookup failed for idx " + idx + ". Trying full list fallback.", error);
                fetchDeviceFromAll(domoticz, idx, sceneOrGroup, callbackCalled, callback);
            }
        }, idx, sceneOrGroup);
    }

    private void fetchDeviceFromAll(nl.hnogames.domoticzapi.Domoticz domoticz,
                                    int idx,
                                    boolean sceneOrGroup,
                                    AtomicBoolean callbackCalled,
                                    DeviceCallback callback) {
        domoticz.getDevices(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
                DevicesInfo matchedDevice = findDeviceByIdx(mDevicesInfo, idx, sceneOrGroup);
                if (matchedDevice != null) {
                    Log.d(TAG, "Fallback lookup returned: " + matchedDevice.getName() +
                        " (idx: " + matchedDevice.getIdx() + ", type: " + matchedDevice.getType() + ")");
                } else {
                    Log.e(TAG, "Fallback lookup could not find idx " + idx + " in device list");
                }
                deliverDevice(callbackCalled, callback, matchedDevice);
            }

            @Override
            public void onReceiveDevice(DevicesInfo device) {
                // Not used for list request
            }

            @Override
            public void onError(Exception error) {
                Log.e(TAG, "Fallback full list lookup failed for idx " + idx, error);
                deliverDevice(callbackCalled, callback, null);
            }
        }, 0, null);
    }

    private DevicesInfo findDeviceByIdx(ArrayList<DevicesInfo> devices, int idx, boolean sceneOrGroup) {
        if (devices == null || devices.isEmpty()) {
            return null;
        }

        DevicesInfo anyTypeMatch = null;
        for (DevicesInfo device : devices) {
            if (device == null || device.getIdx() != idx) {
                continue;
            }

            if (anyTypeMatch == null) {
                anyTypeMatch = device;
            }

            boolean isSceneType = DomoticzValues.Scene.Type.GROUP.equals(device.getType()) ||
                DomoticzValues.Scene.Type.SCENE.equals(device.getType());
            if (sceneOrGroup == isSceneType) {
                return device;
            }
        }

        // Keep a best-effort fallback for legacy servers returning unexpected type values.
        return anyTypeMatch;
    }

    private void deliverDevice(AtomicBoolean callbackCalled, DeviceCallback callback, DevicesInfo device) {
        if (callbackCalled.compareAndSet(false, true)) {
            callback.onDevice(device);
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
