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

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.VolleyErrorListener;

public class RequestUtil {
    private static final String TAG = RequestUtil.class.getSimpleName();

    public static void makeJsonGetRequest(@Nullable final VolleyErrorListener listener,
                                          final String url,
                                          final SessionUtil sessionUtil,
                                          final String username,
                                          final String password,
                                          final boolean useBasicAuth,
                                          final RequestQueue queue) {

        JsonObjectRequest jsonObjReq =
                new JsonObjectRequest(Request.Method.GET,
                        url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (useBasicAuth)
                            Domoticz.BasicAuthDetected = useBasicAuth;
                        if (listener != null)
                            listener.onDone(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (useBasicAuth) {
                            errorHandling(volleyError);
                            if (listener != null)
                                listener.onError(volleyError);
                        } else {
                            //try again with basic auth
                            makeJsonGetRequest(listener,
                                    url,
                                    sessionUtil,
                                    username,
                                    password,
                                    true,
                                    queue);
                        }
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = super.getHeaders();
                        if (headers == null
                                || headers.equals(Collections.emptyMap())) {
                            headers = new HashMap<>();
                        }
                        sessionUtil.addSessionCookie(headers);
                        return useBasicAuth ? createBasicAuthHeader(username, password, headers) : headers;
                    }

                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        sessionUtil.checkSessionCookie(response.headers);
                        return super.parseNetworkResponse(response);
                    }
                };

        // Adding request to request queue
        addToRequestQueue(jsonObjReq, queue);
    }

    /**
     * Method to get json object request where json response starts with {
     */
    public static void makeJsonGetResultRequest(@Nullable final VolleyErrorListener listener,
                                                final String url,
                                                final SessionUtil sessionUtil,
                                                final String username,
                                                final String password,
                                                final boolean useBasicAuth,
                                                final RequestQueue queue) {

        JsonObjectRequest jsonObjReq =
                new JsonObjectRequest(Request.Method.GET,
                        url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (useBasicAuth)
                            Domoticz.BasicAuthDetected = useBasicAuth;
                        if (listener != null)
                            listener.onDone(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (useBasicAuth) {
                            errorHandling(volleyError);
                            if (listener != null)
                                listener.onError(volleyError);
                        } else {
                            //try again with basic auth
                            makeJsonGetResultRequest(listener,
                                    url,
                                    sessionUtil,
                                    username,
                                    password,
                                    true,
                                    queue);
                        }
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = super.getHeaders();
                        if (headers == null
                                || headers.equals(Collections.emptyMap())) {
                            headers = new HashMap<>();
                        }
                        sessionUtil.addSessionCookie(headers);
                        return useBasicAuth ? createBasicAuthHeader(username, password, headers) : headers;
                    }

                    @Override
                    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                        sessionUtil.checkSessionCookie(response.headers);
                        return super.parseNetworkResponse(response);
                    }
                };

        // Adding request to request queue
        addToRequestQueue(jsonObjReq, queue);
    }

    /**
     * Method to put a JSON object to a url
     */
    public static void makeJsonPostRequest(@Nullable final VolleyErrorListener listener,
                                           final String url,
                                           final Map<String, String> params,
                                           final SessionUtil sessionUtil,
                                           final String username,
                                           final String password,
                                           final boolean useBasicAuth,
                                           final RequestQueue queue) {

        StringRequest jsonObjReq =
                new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String jsonObject) {
                                if (useBasicAuth)
                                    Domoticz.BasicAuthDetected = useBasicAuth;
                                String jsonString;

                                try {
                                    JSONObject response = new JSONObject(jsonObject);
                                    jsonString = response.getString(DomoticzValues.Json.Field.STATUS);
                                    if (jsonString.equals(DomoticzValues.Json.Field.ERROR)) {
                                        jsonErrorHandling(response, null, listener);
                                    } else {
                                        if (listener != null)
                                            listener.onDone(response);
                                    }
                                } catch (JSONException ignored) {
                                }
                                if (listener != null)
                                    listener.onDone(null);
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        if (useBasicAuth) {
                            errorHandling(volleyError);
                            if (listener != null)
                                listener.onError(volleyError);
                        } else {
                            //try again with basic auth
                            makeJsonPostRequest(listener,
                                    url,
                                    params,
                                    sessionUtil,
                                    username,
                                    password,
                                    true,
                                    queue);
                        }
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = super.getHeaders();

                        if (headers == null
                                || headers.equals(Collections.emptyMap())) {
                            headers = new HashMap<>();
                        }

                        sessionUtil.addSessionCookie(headers);
                        return useBasicAuth ? createBasicAuthHeader(username, password, headers) : headers;
                    }

                    @Override
                    protected Response<String> parseNetworkResponse(NetworkResponse response) {
                        sessionUtil.checkSessionCookie(response.headers);
                        return super.parseNetworkResponse(response);
                    }
                };

        // Adding request to request queue
        addToRequestQueue(jsonObjReq, queue);
    }

    /**
     * Method to create a basic HTTP base64 encrypted authentication header
     *
     * @param username Username
     * @param password Password
     * @return Base64 encrypted header map
     */
    public static Map<String, String> createBasicAuthHeader(String username, String password, Map<String, String> headerMap) {

        if (headerMap == null)
            headerMap = new HashMap<>();

        if (!UsefulBits.isEmpty(username)) {
            String credentials = username + ":" + password;
            String base64EncodedCredentials =
                    Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            headerMap.put("Authorization", "Basic " + base64EncodedCredentials);
        }
        return headerMap;
    }

    /**
     * Local error handling
     *
     * @param volleyError Volley error holding the error
     */
    private static void errorHandling(VolleyError volleyError) {
        Log.e(TAG, "RequestUtil volley error");
        if (volleyError.getMessage() != null) Log.e(TAG, volleyError.getMessage());
    }

    /**
     * Handle json errors (functional errors)
     *
     * @param response Json result of the call
     * @param e        Exception that occurred during parsing
     * @param listener Parser interface
     */
    private static void jsonErrorHandling(JSONObject response, Exception e, VolleyErrorListener listener) {
        if (response == null)
            return;

        String message = "";
        if (response.has(DomoticzValues.Json.Field.MESSAGE)) {
            try {
                message = response.getString(DomoticzValues.Json.Field.MESSAGE);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        Exception exNew;
        if (e != null && !UsefulBits.isEmpty(e.getMessage())) {
            exNew = new Exception("Failed: " + message + " - " + e.getMessage());
        } else {
            exNew = new Exception("Failed: " + message);
        }

        if (exNew.getMessage() != null)
            Log.e(TAG, exNew.getMessage());
        if (listener != null)
            listener.onError(exNew);
    }

    private static <T> void addToRequestQueue(Request<T> req, RequestQueue queue) {
        req.setTag(TAG);
        int socketTimeout = 5000;
        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        req.setRetryPolicy(retryPolicy);
        queue.add(req);
    }
}