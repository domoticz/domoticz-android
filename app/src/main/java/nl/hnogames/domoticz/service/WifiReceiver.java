package nl.hnogames.domoticz.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.isupatches.wisefy.WiseFy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.containers.WifiInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class WifiReceiver extends Worker {
    private static final String TAG = "WifiReceiver";
    public static String workTag = "wifiscanner";
    private final WorkerParameters params;
    private final Context context;

    public WifiReceiver(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.params = params;
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPrefUtil mSharedPrefs = new SharedPrefUtil(context);
        if (!mSharedPrefs.isWifiEnabled()) {
            return Result.failure();
        }

        WiseFy wisefy = new WiseFy.Brains(context).logging(true).getSmarts();
        if (!mSharedPrefs.isWifiEnabled()) {
            return Result.success();
        }

        List<WifiInfo> connectedDevices = mSharedPrefs.getWifiList();
        if (connectedDevices == null) {
            return Result.success();
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        try {
            android.net.wifi.WifiInfo info = wisefy.getCurrentNetwork();
            if (wisefy.isDeviceConnectedToWifiNetwork() && info.getSSID().contains("unknown")) {
                Log.i("WiseFyReceiver", "Current SSID is unknown");
                wisefy.dump();
                return Result.success();
            }

            String connectedSSID = info.getSSID().replace("\"", "");
            boolean turnOn = true;
            WifiInfo connectedDevice = null;
            for (WifiInfo b : connectedDevices) {
                String ssid = b.getSSID() != null ? b.getSSID() : null;
                if (ssid.equals(connectedSSID))
                    connectedDevice = b;
            }

            WifiInfo lastDevice = mSharedPrefs.getLastWifi();
            if (connectedDevice != null && lastDevice != null) { // Already processed
                if (lastDevice.getSSID().equals(connectedDevice.getSSID())) {
                    Log.i("WiseFyReceiver", "Device already processed");
                    return Result.success();
                }
                // Did we turned of the switch of the previously connected access point?
                if (!connectedDevice.getSSID().equals(lastDevice.getSSID())) {
                    Log.i("WiseFyReceiver", "Other device needs to be turned off");
                    handleSwitch(context, lastDevice.getSwitchIdx(), lastDevice.getSwitchPassword(), false, lastDevice.getValue(), lastDevice.isSceneOrGroup());
                }
            }
            if (connectedDevice == null) {
                Log.i("WiseFyReceiver", "Connected device is registered as null");
                connectedDevice = mSharedPrefs.getLastWifi();
                turnOn = false;
            }

            // Toggle new device
            if (connectedDevice != null && connectedDevice.isEnabled()) {
                if (mSharedPrefs.isWifiNotificationsEnabled()) {
                    NotificationUtil.sendSimpleNotification(new NotificationInfo(connectedDevice.getSwitchIdx(),
                            String.format(context.getString(turnOn ? R.string.wifi_connected_to : R.string.wifi_disconnected_to),
                                    connectedDevice.getName()),
                            context.getString(turnOn ? R.string.wifi_connected_to_text : R.string.wifi_disconnected_to_text),
                            0, new Date()), context);
                }
                mSharedPrefs.saveLastWifi(turnOn ? connectedDevice : null);
                handleSwitch(context, connectedDevice.getSwitchIdx(), connectedDevice.getSwitchPassword(), turnOn,
                        connectedDevice.getValue(), connectedDevice.isSceneOrGroup());
            }
        } catch (Exception ignored) {
        }
        return Result.success();
    }

    private void handleSwitch(final Context context, final int idx, final String password, final boolean checked, final String value, final boolean isSceneOrGroup) {
        Log.i("WiseFyReceiver", "Requesting device to " + checked);
        StaticHelper.getDomoticz(context).getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                Log.i("WiseFyReceiver", "Received latest device status");
                if (mDevicesInfo == null)
                    return;

                int jsonAction;
                int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
                int jsonValue = 0;

                if (!isSceneOrGroup) {
                    if (checked) {
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                        if (!UsefulBits.isEmpty(value)) {
                            jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                            jsonValue = getSelectorValue(mDevicesInfo, value);
                        }
                    } else {
                        jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        if (!UsefulBits.isEmpty(value)) {
                            jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                            jsonValue = 0;
                            if (mDevicesInfo.getStatus() != value)//before turning stuff off check if the value is still the same as the on value (else something else took over)
                                return;
                        }
                    }

                    if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
                        if (checked)
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        else
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    } else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON)
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON)
                        jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                } else {
                    jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
                    if (!checked) {
                        jsonAction = DomoticzValues.Scene.Action.ON;
                    } else
                        jsonAction = DomoticzValues.Scene.Action.OFF;
                    if (mDevicesInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                        jsonAction = DomoticzValues.Scene.Action.ON;
                }

                Log.i("WiseFyReceiver", "Toggling device to " + jsonAction + "|" + jsonValue);
                StaticHelper.getDomoticz(context).setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                    }

                    @Override
                    public void onError(Exception error) {
                    }
                });
            }

            @Override
            public void onError(Exception error) {
            }

        }, idx, isSceneOrGroup);
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
        if (mDevicesInfo == null || mDevicesInfo.getLevelNames() == null)
            return 0;
        int jsonValue = 0;
        if (!UsefulBits.isEmpty(value)) {
            ArrayList<String> levelNames = mDevicesInfo.getLevelNames();
            int counter = 0;
            for (String l : levelNames) {
                if (l.equals(value))
                    break;
                else
                    counter += 10;
            }
            jsonValue = counter;
        }
        return jsonValue;
    }
}