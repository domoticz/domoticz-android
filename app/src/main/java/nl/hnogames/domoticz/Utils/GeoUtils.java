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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Service.GeofenceTransitionsIntentService;

public class GeoUtils {
    public static boolean geofencesAlreadyRegistered = false;
    private Context mContext;

    private SharedPrefUtil mSharedPrefs;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    public GeoUtils(Context mContext) {
        this.mContext = mContext;
        this.mSharedPrefs = new SharedPrefUtil(mContext);
        mGeofencePendingIntent = null;
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
    }

    /**
     * Gets an address from string
     *
     * @param strAddress String address
     * @return Address
     */
    public Address getAddressFromString(String strAddress) {
        Geocoder mGeocoder;
        mGeocoder = new Geocoder(mContext);
        List<Address> addressList;
        Address mAddress = null;

        try {
            addressList = mGeocoder.getFromLocationName(strAddress, 5);
            if (addressList == null) {
                return null;
            }
            if (addressList.size() >= 1) mAddress = addressList.get(0);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return mAddress;
    }

    /**
     * Gets an address from location
     *
     * @param mLocation Location
     * @return Address
     */
    public Address getAddressFromLocation(Location mLocation) {
        Geocoder mGeocoder;
        mGeocoder = new Geocoder(mContext);
        List<Address> addressList;
        Address mAddress = null;

        try {
            addressList = mGeocoder.getFromLocation(mLocation.getLatitude(),
                    mLocation.getLongitude(), 5);

            if (addressList == null) {
                return null;
            }
            mAddress = addressList.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mAddress;
    }

    /**
     * @param locationInfo The location information
     * @return first address which matches latitude and longitude in the given location info
     */
    public Address getAddressFromLocationInfo(LocationInfo locationInfo) {
        Geocoder mGeocoder;
        mGeocoder = new Geocoder(mContext);
        List<Address> addressList;
        Address mAddress = null;

        try {
            addressList =
                    mGeocoder.getFromLocation(
                            locationInfo.getLocation().latitude,
                            locationInfo.getLocation().longitude, 5);

            if (addressList == null) {
                return null;
            }
            mAddress = addressList.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mAddress;
    }

    /**
     * Gets an address from latitude and longitude
     *
     * @param mLatLong LatLong
     * @return Address
     */
    public Address getAddressFromLatLng(LatLng mLatLong) {
        Geocoder mGeocoder;
        mGeocoder = new Geocoder(mContext);
        List<Address> addressList;
        Address mAddress = null;

        try {
            addressList = mGeocoder.getFromLocation(mLatLong.latitude, mLatLong.longitude, 5);
            if (addressList == null || addressList.size() <= 0) {
                return null;
            }
            mAddress = addressList.get(0);//get first
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mAddress;
    }

    public void RemoveGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    public void AddGeofences() {
        if (this.mSharedPrefs.isGeofenceEnabled()) {
            //only continue when we have the correct permissions!
            if (PermissionsUtil.canAccessLocation(mContext)) {
                final List<Geofence> mGeofenceList = this.mSharedPrefs.getEnabledGeofences();
                if (mGeofenceList != null && mGeofenceList.size() > 0) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        return;
                    mGeofencingClient.addGeofences(getGeofencingRequest(mGeofenceList), getGeofencePendingIntent());
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
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     * @param mGeofenceList
     */
    private GeofencingRequest getGeofencingRequest(List<Geofence> mGeofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    public void enableGeoFenceService() {
        AddGeofences();
    }

    public void disableGeoFenceService() {
        RemoveGeofences();
    }
}