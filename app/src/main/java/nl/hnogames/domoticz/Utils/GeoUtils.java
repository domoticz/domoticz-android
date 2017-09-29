/*
 * Copyright (C) 2015 Domoticz - Mark Heinis
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package nl.hnogames.domoticz.Utils;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import nl.hnogames.domoticz.Service.GeofenceTransitionsIntentService;

public class GeoUtils {
    public static boolean geofencesAlreadyRegistered = false;
    private Context mContext;
    private Activity mActivity;

    private SharedPrefUtil mSharedPrefs;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    public GeoUtils(Context mContext, Activity activity) {
        this.mContext = mContext;
        this.mActivity = activity;

        this.mSharedPrefs = new SharedPrefUtil(mContext);

        mGeofencePendingIntent = null;
        mGeofencingClient = LocationServices.getGeofencingClient(mActivity != null ? mActivity : mContext);
    }

    /**
     * Remove the active Geofences from the client
     */
    public void RemoveGeofences() {
        if(mGeofencingClient != null)
            mGeofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    /**
     * Add the Geofences to the client
     */
    public void AddGeofences() {
        if (this.mSharedPrefs.isGeofenceEnabled()) {
            //only continue when we have the correct permissions!
            if (PermissionsUtil.canAccessLocation(mContext)) {
                final List<Geofence> mGeofenceList = this.mSharedPrefs.getEnabledGeofences();
                if (mGeofenceList != null && mGeofenceList.size() > 0) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        return;
                    Log.i("GeoUtils", "Starting Geofences");
                    if(mGeofencingClient != null) {
                        RemoveGeofences();//clear existing ones
                        mGeofencingClient.addGeofences(getGeofencingRequest(mGeofenceList), getGeofencePendingIntent());
                    }
                }
            }
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        else {
            return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest(List<Geofence> mGeofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}