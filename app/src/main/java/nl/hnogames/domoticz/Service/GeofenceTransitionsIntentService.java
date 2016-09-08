/*
 * Copyright (C) 2015 Domoticz
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package nl.hnogames.domoticz.Service;

import android.app.IntentService;
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

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticzapi.Containers.LocationInfo;
import nl.hnogames.domoticzapi.Containers.SwitchInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.StatusReceiver;
import nl.hnogames.domoticzapi.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "GEOFENCE";
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = new SharedPrefUtil(this);
    }


    /**
     * Handles incoming intents.
     *
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
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
                        String text = String.format(
                                getString(R.string.geofence_location_entering),
                                locationFound.getName());
                        NotificationUtil.sendSimpleNotification(text,
                                getString(R.string.geofence_location_entering_text), this);

                        if (locationFound.getSwitchIdx() > 0) {
                            handleSwitch(locationFound.getSwitchIdx(), locationFound.getSwitchPassword(), true);
                        }
                    }
                } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                    for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                        LocationInfo locationFound
                                = mSharedPrefs.getLocation(Integer.valueOf(geofence.getRequestId()));
                        Log.d(TAG, "Triggered leaving a geofence location: "
                                + locationFound.getName());

                        String text = String.format(
                                getString(R.string.geofence_location_leaving),
                                locationFound.getName());
                        NotificationUtil.sendSimpleNotification(text,
                                getString(R.string.geofence_location_leaving_text), this);

                        if (locationFound.getSwitchIdx() > 0)
                            handleSwitch(locationFound.getSwitchIdx(), locationFound.getSwitchPassword(), false);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handleSwitch(final int idx, final String password, final boolean checked) {
        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        domoticz.getSwitches(new SwitchesReceiver() {
                                 @Override
                                 public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                                     for (SwitchInfo s : switches) {
                                         if (s.getIdx() == idx) {
                                             domoticz.getStatus(idx, new StatusReceiver() {
                                                 @Override
                                                 public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {

                                                     int jsonAction;
                                                     int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
                                                     if (extendedStatusInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                                                             extendedStatusInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                                                         if (checked)
                                                             jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                                         else
                                                             jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                                     } else {
                                                         if (checked)
                                                             jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                                         else
                                                             jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                                     }

                                                     switch (extendedStatusInfo.getSwitchTypeVal()) {
                                                         case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                                                             jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                                             break;
                                                         case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                                                             jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                                             break;
                                                     }

                                                     domoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                                                         @Override
                                                         public void onReceiveResult(String result) {
                                                             Log.d(TAG, result);
                                                         }

                                                         @Override
                                                         public void onError(Exception error) {
                                                             if (error != null)
                                                                 onErrorHandling(error);
                                                         }
                                                     });
                                                 }

                                                 @Override
                                                 public void onError(Exception error) {
                                                     if (error != null)
                                                         onErrorHandling(error);
                                                 }
                                             });
                                         }
                                     }
                                 }

                                 @Override
                                 public void onError(Exception error) {
                                     if (error != null)
                                         onErrorHandling(error);
                                 }
                             }
        );
    }

    private void onErrorHandling(Exception error) {
        if (error != null) {
            Toast.makeText(
                    GeofenceTransitionsIntentService.this,
                    "Domoticz: " +
                            getString(R.string.unable_to_get_switches),
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