package nl.hnogames.domoticz.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class GeofenceReceiver extends BroadcastReceiver
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "GEOFENCE";
    Intent broadcastIntent = new Intent();
    private Context context;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private String notificationTitle = "";
    private String notificationDescription = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(context);

        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            Log.e(TAG, "Error: " + geoFenceEvent.getErrorCode());
        } else {
            handleEnterExit(geoFenceEvent);
        }
    }

    private void handleEnterExit(GeofencingEvent geoFenceEvent) {
        try {
            if (geoFenceEvent.hasError()) {
                int errorCode = geoFenceEvent.getErrorCode();
                Log.e(TAG, "Location Services error: " + errorCode);
            } else {
                int transitionType = geoFenceEvent.getGeofenceTransition();
                if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                    for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                        LocationInfo locationFound =
                            mSharedPrefs.getLocation(Integer.valueOf(geofence.getRequestId()));
                        Log.d(TAG, "Triggered entering a geofence location: "
                            + locationFound.getName());

                        notificationTitle = String.format(
                            context.getString(R.string.geofence_location_entering),
                            locationFound.getName());
                        notificationDescription = context.getString(R.string.geofence_location_entering_text);
                        NotificationUtil.sendSimpleNotification(notificationTitle,
                            notificationDescription, 0, context);

                        if (locationFound.getSwitchIdx() > 0)
                            handleSwitch(locationFound.getSwitchIdx(), locationFound.getSwitchPassword(), 1, locationFound.getValue(), locationFound.isSceneOrGroup());
                    }
                } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                    for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                        LocationInfo locationFound
                            = mSharedPrefs.getLocation(Integer.valueOf(geofence.getRequestId()));
                        Log.d(TAG, "Triggered leaving a geofence location: "
                            + locationFound.getName());

                        notificationTitle = String.format(
                            context.getString(R.string.geofence_location_leaving),
                            locationFound.getName());
                        notificationDescription = context.getString(R.string.geofence_location_leaving_text);

                        if (locationFound.getSwitchIdx() > 0)
                            handleSwitch(locationFound.getSwitchIdx(), locationFound.getSwitchPassword(), 0, locationFound.getValue(), locationFound.isSceneOrGroup());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSwitch(final int idx, final String password, final int inputJSONAction, final String value, final boolean isSceneOrGroup) {
        if (domoticz == null)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());

        domoticz.getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {}

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                int jsonAction;
                int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
                int jsonValue = 0;

                if (!isSceneOrGroup) {
                    if (inputJSONAction < 0) {
                        if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                            if (!mDevicesInfo.getStatusBoolean())
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                            else {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            }
                        } else {
                            if (!mDevicesInfo.getStatusBoolean()) {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            } else
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        }
                    } else {
                        if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                            if (inputJSONAction == 1)
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                            else {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            }
                        } else {
                            if (inputJSONAction == 1) {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            } else
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        }
                    }

                    switch (mDevicesInfo.getSwitchTypeVal()) {
                        case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            break;
                        case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                            break;
                    }
                }
                else
                {
                    jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
                    if (inputJSONAction < 0) {
                        if (!mDevicesInfo.getStatusBoolean()) {
                            jsonAction = DomoticzValues.Scene.Action.ON;
                        } else
                            jsonAction = DomoticzValues.Scene.Action.OFF;
                    } else {
                        if (inputJSONAction == 1) {
                            jsonAction = DomoticzValues.Scene.Action.ON;
                        } else
                            jsonAction = DomoticzValues.Scene.Action.OFF;
                    }

                    if (mDevicesInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                        jsonAction = DomoticzValues.Scene.Action.ON;
                }

                domoticz.setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    @DebugLog
                    public void onReceiveResult(String result) {
                        if (!UsefulBits.isEmpty(result))
                            Log.d(TAG, result);
                    }

                    @Override
                    @DebugLog
                    public void onError(Exception error) {
                        if (error != null && !UsefulBits.isEmpty(error.getMessage()))
                            Log.d(TAG, error.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                if (error != null && !UsefulBits.isEmpty(error.getMessage()))
                    Log.d(TAG, error.getMessage());
            }

        }, idx, isSceneOrGroup);
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
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

    private void onErrorHandling(Exception error) {
        if (error != null) {
            Toast.makeText(
                context,
                "Domoticz: " +
                    context.getString(R.string.unable_to_get_switches),
                Toast.LENGTH_SHORT).show();

            if (domoticz != null && UsefulBits.isEmpty(domoticz.getErrorMessage(error)))
                Log.e(TAG, domoticz.getErrorMessage(error));
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
    }
}