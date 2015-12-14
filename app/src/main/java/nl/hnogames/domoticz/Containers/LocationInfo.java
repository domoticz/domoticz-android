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

package nl.hnogames.domoticz.Containers;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

public class LocationInfo {
    String Name;
    LatLng Location;
    int id = 0;
    int switchidx = 0;
    int radius = 400;           //meters
    boolean enabled = false;


    public LocationInfo(int i, String n, LatLng l, int radius) {
        this.Name = n;
        this.Location = l;
        this.id = i;
        this.radius = radius;
    }


    public String getName() {
        return Name;
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

    public int getSwitchidx() {
        return switchidx;
    }

    public void setSwitchidx(int idx) {
        switchidx = idx;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int e) {
        radius = e;
    }

    public LatLng getLocation() {
        return Location;
    }


    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     *
     * @return A Geofence object.
     */
    public Geofence toGeofence() {
        if(radius<=0)
            radius=400;//default

        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(String.valueOf(id))
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setCircularRegion(Location.latitude, Location.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }
}