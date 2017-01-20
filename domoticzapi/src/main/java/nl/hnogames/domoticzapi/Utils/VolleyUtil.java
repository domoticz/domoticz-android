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

package nl.hnogames.domoticzapi.Utils;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import nl.hnogames.domoticzapi.R;

public class VolleyUtil {

    @SuppressWarnings("unused")
    public static final String VOLLEY_ERROR_HANDSHAKE_FAILED = "Handshake failed";
    @SuppressWarnings("unused")
    public static final String VOLLEY_ERROR_TIMEOUT_ERROR = "com.android.volley.TimeoutError";
    private static final String TAG = VolleyUtil.class.getSimpleName();
    private Context mContext;

    public VolleyUtil(Context mContext) {
        this.mContext = mContext;
    }

    public String getVolleyErrorMessage(VolleyError volleyError) {

        String errorMessage = "Unhandled error";

        if (volleyError instanceof AuthFailureError) {
            Log.e(TAG, "Authentication failure");
            errorMessage = mContext.getString(R.string.error_authentication);

        } else if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
            Log.e(TAG, "Timeout or no connection");
            String detail;

            if (volleyError.getCause() != null) detail = volleyError.getCause().getMessage();
            else {
                detail = volleyError.toString();
            }
            errorMessage = mContext.getString(R.string.error_timeout) + "\n" + detail;

        } else if (volleyError instanceof ServerError) {
            Log.e(TAG, "Server error");
            errorMessage = mContext.getString(R.string.error_server);

        } else if (volleyError instanceof NetworkError) {
            Log.e(TAG, "Network error");

            NetworkResponse networkResponse = volleyError.networkResponse;
            if (networkResponse != null) {
                Log.e("Status code", String.valueOf(networkResponse.statusCode));
                errorMessage = String.format(mContext.getString(R.string.error_network), networkResponse.statusCode);
            }
        } else if (volleyError instanceof ParseError) {
            Log.e(TAG, "Parse failure");
            errorMessage = mContext.getString(R.string.error_parse);
        }

        return errorMessage;
    }
}