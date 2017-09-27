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

package nl.hnogames.domoticz.Containers;

import android.location.Address;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import nl.hnogames.domoticz.Utils.UsefulBits;

public class LocationInfo {
    private String name;
    private LatLng latLng;
    private int id = 0;
    private int switchIdx = 0;
    private String switchPassword = "";
    private int radius = 400;           //meters
    private boolean enabled = false;
    private Address address;
    private String switchName;
    private String value;

    private boolean isSceneOrGroup = false;

    public LocationInfo(int id, String name, LatLng latLng, int radius) {
        this.name = name;
        this.latLng = latLng;
        this.id = id;
        this.radius = radius;
    }

    public boolean isSceneOrGroup() {
        return isSceneOrGroup;
    }

    public void setSceneOrGroup(boolean sceneOrGroup) {
        isSceneOrGroup = sceneOrGroup;
    }

    public String getName() {
        if (UsefulBits.isEmpty(name))
            return "";
        else
            return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        enabled = e;
    }

    public int getID() {
        return id;
    }

    public int getSwitchIdx() {
        return switchIdx;
    }

    public void setSwitchIdx(int idx) {
        switchIdx = idx;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int e) {
        radius = e;
    }

    public LatLng getLocation() {
        return latLng;
    }

    public void setLocation(LatLng latLng) {
        this.latLng = latLng;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getSwitchPassword() {
        return switchPassword;
    }

    public void setSwitchPassword(String switchPassword) {
        this.switchPassword = switchPassword;
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        if (radius <= 0)
            radius = 400;//default
        try {
            // Build a new Geofence object.
            return  new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(String.valueOf(id))
                // Set the circular region of this geofence.
                .setCircularRegion(
                    latLng.latitude, latLng.longitude, radius
                )
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL |
                    Geofence.GEOFENCE_TRANSITION_EXIT)
                    .setLoiteringDelay(3000)
                // Create the geofence.
                .build();
        } catch (Exception ex) {
            // Wrong LocationInfo data detected
            return null;
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName(String switchName) {
        this.switchName = switchName;
    }
}