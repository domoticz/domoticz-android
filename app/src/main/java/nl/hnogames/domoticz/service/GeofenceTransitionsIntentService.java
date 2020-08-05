package nl.hnogames.domoticz.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.Date;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.LocationInfo;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.utils.WidgetUtils;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class GeofenceTransitionsIntentService extends JobIntentService {
    private static final int JOB_ID = 502;
    private final String TAG = "GEOFENCE";
    private Context context;
    private SharedPrefUtil mSharedPrefs;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsIntentService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        this.context = getApplicationContext();
        if (mSharedPrefs == null)
            mSharedPrefs = new SharedPrefUtil(context);

        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            Log.e(TAG, "Error: " + geoFenceEvent.getErrorCode());
        } else {
            Log.d(TAG, "Received geofence event.");
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
                String notificationTitle = "";
                String notificationDescription = "";
                if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType || Geofence.GEOFENCE_TRANSITION_DWELL == transitionType) {
                    for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                        LocationInfo locationFound =
                                mSharedPrefs.getLocation(Integer.parseInt(geofence.getRequestId()));
                        Log.d(TAG, "Triggered entering a geofence location: "
                                + locationFound.getName());

                        if (mSharedPrefs.isGeofenceNotificationsEnabled()) {
                            notificationTitle = String.format(
                                    context.getString(R.string.geofence_location_entering), locationFound.getName());
                            notificationDescription = context.getString(R.string.geofence_location_entering_text);
                            NotificationUtil.sendSimpleNotification(new NotificationInfo(-1, notificationTitle, notificationDescription, 0, new Date()), context);
                        }
                        if (locationFound.getSwitchIdx() > 0)
                            handleSwitch(context, locationFound.getSwitchIdx(), locationFound.getSwitchPassword(), true, locationFound.getValue(), locationFound.isSceneOrGroup());
                    }
                } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                    for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                        LocationInfo locationFound
                                = mSharedPrefs.getLocation(Integer.parseInt(geofence.getRequestId()));
                        Log.d(TAG, "Triggered leaving a geofence location: "
                                + locationFound.getName());

                        if (mSharedPrefs.isGeofenceNotificationsEnabled()) {
                            notificationTitle = String.format(
                                    context.getString(R.string.geofence_location_leaving),
                                    locationFound.getName());
                            notificationDescription = context.getString(R.string.geofence_location_leaving_text);
                            NotificationUtil.sendSimpleNotification(new NotificationInfo(-1, notificationTitle, notificationDescription, 0, new Date()), context);
                        }
                        if (locationFound.getSwitchIdx() > 0)
                            handleSwitch(context, locationFound.getSwitchIdx(), locationFound.getSwitchPassword(), false, locationFound.getValue(), locationFound.isSceneOrGroup());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSwitch(final Context context, final int idx, final String password, final boolean checked, final String value, final boolean isSceneOrGroup) {
        StaticHelper.getDomoticz(context).getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

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
                StaticHelper.getDomoticz(context).setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                        WidgetUtils.RefreshWidgets(context);
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