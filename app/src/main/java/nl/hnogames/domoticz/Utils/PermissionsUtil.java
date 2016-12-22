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
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.fastaccess.permission.base.PermissionFragmentHelper;
import com.fastaccess.permission.base.PermissionHelper;

import nl.hnogames.domoticz.R;

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
    //these permissions are needed for getting device id
    public static final String[] INITIAL_DEVICE_PERMS = {
            Manifest.permission.READ_PHONE_STATE
    };
    //these permissions are needed for scanning qrcodes
    public static final String[] INITIAL_CAMERA_PERMS = {
            Manifest.permission.CAMERA
    };
    //these permissions are needed for recording audio
    public static final String[] INITIAL_AUDIO_PERMS = {
            Manifest.permission.RECORD_AUDIO
    };
    //these permissions are needed for fingerprint
    public static final String[] INITIAL_FINGERPRINT_PERMS = {
            Manifest.permission.USE_FINGERPRINT
    };

    //This range is from 0 to 255!!
    public static final int INITIAL_LOCATION_REQUEST = 111;
    public static final int INITIAL_IMPORT_SETTINGS_REQUEST = 122;
    public static final int INITIAL_EXPORT_SETTINGS_REQUEST = 133;
    public static final int INITIAL_CAMERA_REQUEST = 144;
    public static final int INITIAL_DEVICE_REQUEST = 155;
    public static final int INITIAL_AUDIO_REQUEST = 166;
    public static final int INITIAL_FINGERPRINT_REQUEST = 177;

    @SuppressWarnings("unused")
    private static final String TAG = PermissionsUtil.class.getSimpleName();

    public static boolean canAccessLocation(Context context) {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION, context));
    }

    public static boolean canAccessStorage(Context context) {
        return (hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context));
    }

    public static boolean canAccessFingerprint(Context context) {
        return (hasPermission(Manifest.permission.USE_FINGERPRINT, context));
    }

    public static boolean canAccessCamera(Context context) {
        return (hasPermission(Manifest.permission.CAMERA, context));
    }

    public static boolean canAccessDeviceState(Context context) {
        return (hasPermission(Manifest.permission.READ_PHONE_STATE, context));
    }

    public static boolean canAccessAudioState(Context context) {
        return (hasPermission(Manifest.permission.RECORD_AUDIO, context));
    }

    private static boolean hasPermission(String permission, Context context) {
        // Using ContextCompat.checkSelfPermission will work on all API versions
        return (PackageManager.PERMISSION_GRANTED
                == ContextCompat.checkSelfPermission(context, permission));
    }

    public static AlertDialog getAlertDialog(Context context, final PermissionFragmentHelper permissionFragmentHelper, String title, String description, final String[] permissions) {
        return getAlertDialog(context, permissionFragmentHelper, title, description, permissions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    public static AlertDialog getAlertDialog(Context context, final PermissionFragmentHelper permissionFragmentHelper, String title, String description, final String[] permissions, DialogInterface.OnClickListener oncancel) {
        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .create();
        builder.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.request_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionFragmentHelper.requestAfterExplanation(permissions);
            }
        });
        builder.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setMessage(description);
        return builder;
    }

    public static AlertDialog getAlertDialog(Context context, final PermissionFragmentHelper permissionFragmentHelper, String title, String description, final String permission) {
        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .create();
        builder.setButton(DialogInterface.BUTTON_POSITIVE, "Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionFragmentHelper.requestAfterExplanation(permission);
            }
        });
        builder.setMessage(description);
        return builder;
    }


    public static AlertDialog getAlertDialog(Context context, final PermissionHelper permissionFragmentHelper, String title, String description, final String[] permissions) {
        return getAlertDialog(context, permissionFragmentHelper, title, description, permissions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    public static AlertDialog getAlertDialog(Context context, final PermissionHelper permissionFragmentHelper, String title, String description, final String[] permissions, DialogInterface.OnClickListener oncancel) {
        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .create();
        builder.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.request_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionFragmentHelper.requestAfterExplanation(permissions);
            }
        });
        builder.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setMessage(description);
        return builder;
    }

    public static AlertDialog getAlertDialog(Context context, final PermissionHelper permissionFragmentHelper, String title, String description, final String permission) {
        AlertDialog builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .create();
        builder.setButton(DialogInterface.BUTTON_POSITIVE, "Request", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionFragmentHelper.requestAfterExplanation(permission);
            }
        });
        builder.setMessage(description);
        return builder;
    }
}