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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

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
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
        } else {
            int transitionType = geoFenceEvent.getGeofenceTransition();
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                    LocationInfo locationFound = mSharedPrefs.getLocation( Integer.valueOf(geofence.getRequestId()));
                    Log.d(TAG, "Triggered geofence location: " + locationFound.getName());

                    if (mSharedPrefs.isGeofenceNotificationsEnabled())
                        sendNotification("Entering " + locationFound.getName(), "Entering one of the locations");

                    if (locationFound.getSwitchidx() > 0) {
                        handleSwitch(locationFound.getSwitchidx(), true);
                    }
                }

            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                for (Geofence geofence : geoFenceEvent.getTriggeringGeofences()) {
                    LocationInfo locationFound = mSharedPrefs.getLocation( Integer.valueOf(geofence.getRequestId()));
                    Log.d(TAG, "Triggered geofence location: " + locationFound.getName());

                    if (mSharedPrefs.isGeofenceNotificationsEnabled())
                        sendNotification("Leaving " + locationFound.getName(), "Leaving one of the locations");

                    if (locationFound.getSwitchidx() > 0) {
                        handleSwitch(locationFound.getSwitchidx(), false);
                    }
                }
            }
        }
    }

    private void handleSwitch(final int idx, final boolean checked) {
        domoticz = new Domoticz(this);
        domoticz.getSwitches(new SwitchesReceiver() {
                                 @Override
                                 public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                                     for (SwitchInfo s : switches) {
                                         if (s.getIdx() == idx) {
                                             domoticz.getStatus(idx, new StatusReceiver() {
                                                 @Override
                                                 public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {

                                                     int jsonAction;
                                                     int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;

                                                     if (extendedStatusInfo.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                                                             extendedStatusInfo.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                                                         if (checked)
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                         else
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                     } else {
                                                         if (checked)
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                         else
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                     }

                                                     domoticz.setAction(idx, jsonUrl, jsonAction, 0, new setCommandReceiver() {
                                                         @Override
                                                         public void onReceiveResult(String result) {
                                                             Log.d(TAG, result);
                                                         }

                                                         @Override
                                                         public void onError(Exception error) {
                                                             Log.e(TAG, error.getMessage());
                                                         }
                                                     });
                                                 }

                                                 @Override
                                                 public void onError(Exception error) {
                                                 }
                                             });
                                         }
                                     }

                                 }

                                 @Override
                                 public void onError(Exception error) {

                                 }
                             }
        );
    }

    private void sendNotification(String title, String text) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(text);
        int NOTIFICATION_ID = 12345;

        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }


    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

}
