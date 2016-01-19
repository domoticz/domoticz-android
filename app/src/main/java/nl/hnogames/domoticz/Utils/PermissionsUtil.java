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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class PermissionsUtil {

    //these permissions are needed for Wifi scanning
    public static final String[] INITIAL_LOCATION_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    //these permissions are needed for storing camera images
    public static final String[] INITIAL_STORAGE_PERMS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //This range is from 0 to 255!!
    public static final int INITIAL_LOCATION_REQUEST = 111;
    public static final int INITIAL_IMPORT_SETTINGS_REQUEST = 122;
    public static final int INITIAL_EXPORT_SETTINGS_REQUEST = 133;
    public static final int INITIAL_CAMERA_REQUEST = 144;


    @SuppressWarnings("unused")
    private static final String TAG = PermissionsUtil.class.getSimpleName();

    public static boolean canAccessLocation(Context context) {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION, context));
    }

    public static boolean canAccessStorage(Context context) {
        return (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context));
    }

    private static boolean hasPermission(String permission, Context context) {

        // Using ContextCompat.checkSelfPermission will work on all API versions
        return (PackageManager.PERMISSION_GRANTED
                == ContextCompat.checkSelfPermission(context, permission));
    }

}