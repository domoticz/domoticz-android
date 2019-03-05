package nl.hnogames.domoticz.Service;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Containers.BluetoothInfo;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class BluetoothConnectionReceiver extends BroadcastReceiver {
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;

    public BluetoothConnectionReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(context);

        if (!mSharedPrefs.isBluetoothEnabled())
            return;

        List<BluetoothInfo> connectedDevices = mSharedPrefs.getBluetoothList();
        if (connectedDevices == null)
            return;

        BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        BluetoothInfo connectedDevice = null;
        for (BluetoothInfo b : connectedDevices) {
            if (b.getName().equals(bluetoothDevice.getName()))
                connectedDevice = b;
        }

        if (connectedDevice != null && connectedDevice.isEnabled()) {
            handleSwitch(context, connectedDevice.getSwitchIdx(), connectedDevice.getSwitchPassword(), (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())),
                connectedDevice.getValue(), connectedDevice.isSceneOrGroup());
            Toast.makeText(context, context.getString(R.string.bluetooth) + " " + connectedDevice.getName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSwitch(final Context context, final int idx, final String password, final boolean checked, final String value, final boolean isSceneOrGroup) {
        if (domoticz == null)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());

        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {}

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
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
                            if(mDevicesInfo.getStatus() != value)//before turning stuff off check if the value is still the same as the on value (else something else took over)
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
                    }
                    else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON )
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON )
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
                domoticz.setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        WidgetUtils.RefreshWidgets(context);
                    }

                    @Override
                    public void onError(Exception error) {}
                });
            }

            @Override
            public void onError(Exception error) {}

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