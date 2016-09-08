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

package nl.hnogames.domoticz.Utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import nl.hnogames.domoticzapi.Containers.LocationInfo;

public class GeoUtil {
    Context mContext;

    public GeoUtil(Context mContext) {
        this.mContext = mContext;
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
}