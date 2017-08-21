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
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;


public class RequestUtil {

    private static final String TAG = RequestUtil.class.getSimpleName();
    public static void makeJsonVersionRequest(@Nullable final JSONParserInterface parser,
                                              final String username,
                                              final String password,
                                              final String url,
                                              final SessionUtil sessionUtil,
                                              final boolean usePreviousSession,
                                              final int retryCounter,
                                              final RequestQueue queue) {
        JsonObjectRequest jsonObjReq =
            new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    String jsonString;

                    try {
                        jsonString = response.getString(DomoticzValues.Json.Field.VERSION);
                        if (parser != null)
                            parser.parseResult(jsonString);
                    } catch (JSONException e) {
                        jsonErrorHandling(response, e, parser);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    int counter = retryCounter - 1;
                    if (counter <= 0) {
                        errorHandling(volleyError);
                        Log.d(TAG, "No retries left");
                        if (parser != null) parser.onError(volleyError);
                    } else {
                        //try again without session id
                        Log.d(TAG, "Trying again without session ID. Retries left: "
                            + String.valueOf(counter));
                        makeJsonVersionRequest(parser,
                            username,
                            password,
                            url,
                            sessionUtil,
                            false,
                            counter,
                            queue);
                    }
                }
            }) {

                @Override
                // HTTP basic authentication
                // Taken from: http://blog.lemberg.co.uk/volley-part-1-quickstart
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<>();
                    }

                    if (usePreviousSession)
                        sessionUtil.addSessionCookie(headers);
                    return createBasicAuthHeader(username, password, headers);
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    sessionUtil.checkSessionCookie(response.headers);           //save cookie
                    return super.parseNetworkResponse(response);
                }
            };

        // Adding request to request queue
        addToRequestQueue(jsonObjReq, queue);
    }

    public static void makeJsonGetRequest(@Nullable final JSONParserInterface parser,
                                          final String username,
                                          final String password,
                                          final String url,
                                          final SessionUtil sessionUtil,
                                          final boolean usePreviousSession,
                                          final int retryCounter,
                                          final RequestQueue queue) {

        JsonObjectRequest jsonObjReq =
            new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    if (parser != null)
                        parser.parseResult(response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    int counter = retryCounter - 1;
                    if (counter <= 0) {
                        errorHandling(volleyError);
                        if (parser != null) parser.onError(volleyError);
                    } else {
                        //try again without session id
                        Log.d(TAG, "Trying again without session ID. Retries left: "
                            + String.valueOf(counter));
                        makeJsonGetRequest(parser,
                            username,
                            password,
                            url,
                            sessionUtil,
                            false,
                            counter,
                            queue);
                    }
                }
            }) {

                @Override
                // HTTP basic authentication
                // Taken from: http://blog.lemberg.co.uk/volley-part-1-quickstart
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<>();
                    }

                    if (usePreviousSession)
                        sessionUtil.addSessionCookie(headers);
                    return createBasicAuthHeader(username, password, headers);
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    // since we don't know which of the two underlying network vehicles
                    // will Volley use, we have to handle and store session cookies manually
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
    public static void makeJsonGetResultRequest(@Nullable final JSONParserInterface parser,
                                                final String username,
                                                final String password,
                                                final String url,
                                                final SessionUtil sessionUtil,
                                                final boolean usePreviousSession,
                                                final int retryCounter,
                                                final RequestQueue queue) {

        JsonObjectRequest jsonObjReq =
            new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {

                    String jsonString;

                    try {
                        jsonString = response.getString(DomoticzValues.Json.Field.RESULT);
                        if (parser != null)
                            parser.parseResult(jsonString);
                    } catch (JSONException e) {
                        jsonErrorHandling(response, e, parser);
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    int counter = retryCounter - 1;
                    if (counter <= 0) {
                        errorHandling(volleyError);
                        if (parser != null) parser.onError(volleyError);
                    } else {
                        //try again without session id
                        Log.d(TAG, "Trying again without session ID. Retries left: "
                            + String.valueOf(counter));
                        makeJsonGetResultRequest(parser,
                            username,
                            password,
                            url,
                            sessionUtil,
                            false,
                            counter,
                            queue);
                    }
                }
            }) {

                @Override
                // HTTP basic authentication
                // Taken from: http://blog.lemberg.co.uk/volley-part-1-quickstart
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<>();
                    }

                    if (usePreviousSession)
                        sessionUtil.addSessionCookie(headers);
                    return createBasicAuthHeader(username, password, headers);
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    // since we don't know which of the two underlying network vehicles
                    // will Volley use, we have to handle and store session cookies manually
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
    public static void makeJsonPutRequest(@Nullable final JSONParserInterface parser,
                                          final String username,
                                          final String password,
                                          final String url,
                                          final SessionUtil sessionUtil,
                                          final boolean usePreviousSession,
                                          final int retryCounter,
                                          final RequestQueue queue) {

        JsonObjectRequest jsonObjReq =
            new JsonObjectRequest(Request.Method.PUT, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String jsonString;

                        try {
                            jsonString = response.getString(DomoticzValues.Json.Field.STATUS);
                            if (jsonString.equals(DomoticzValues.Json.Field.ERROR)) {
                                jsonErrorHandling(response, null, parser);
                            } else {
                                if (parser != null)
                                    parser.parseResult(jsonString);
                            }
                        } catch (JSONException e) {

                        }
                    }
                }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    int counter = retryCounter - 1;
                    if (counter <= 0) {
                        errorHandling(volleyError);
                        if (parser != null) parser.onError(volleyError);
                    } else {
                        //try again without session id
                        Log.d(TAG, "Trying again without session ID. Retries left: "
                            + String.valueOf(counter));
                        makeJsonPutRequest(parser,
                            username,
                            password,
                            url,
                            sessionUtil,
                            false,
                            counter,
                            queue);
                    }
                }
            }) {

                @Override
                // HTTP basic authentication
                // Taken from: http://blog.lemberg.co.uk/volley-part-1-quickstart
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<>();
                    }

                    if (usePreviousSession)
                        sessionUtil.addSessionCookie(headers);
                    return createBasicAuthHeader(username, password, headers);
                }

                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    // since we don't know which of the two underlying network vehicles
                    // will Volley use, we have to handle and store session cookies manually
                    sessionUtil.checkSessionCookie(response.headers);
                    return super.parseNetworkResponse(response);
                }
            };

        // Adding request to request queue
        addToRequestQueue(jsonObjReq, queue);
    }

    public static ImageLoader getImageLoader(final Domoticz domoticz, final SessionUtil sessionUtil, Context context) {
        if (domoticz == null)
            return null;

        ImageLoader.ImageCache imageCache = new BitmapLruCache();
        return new ImageLoader(Volley.newRequestQueue(context), imageCache) {
            @SuppressWarnings("deprecation")
            @Override
            protected com.android.volley.Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth, int maxHeight,
                                                                          ImageView.ScaleType scaleType, final String cacheKey) {
                return new ImageRequest(requestUrl, new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        onGetImageSuccess(cacheKey, response);
                    }
                }, maxWidth, maxHeight,
                    Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        onGetImageError(cacheKey, error);
                    }
                }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = super.getHeaders();

                        if (headers == null
                            || headers.equals(Collections.emptyMap())) {
                            headers = new HashMap<>();
                        }

                        String credentials = domoticz.getUserCredentials(Domoticz.Authentication.USERNAME) + ":" + domoticz.getUserCredentials(Domoticz.Authentication.PASSWORD);
                        String base64EncodedCredentials =
                            Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

                        headers.put("Authorization", "Basic " + base64EncodedCredentials);
                        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                        headers.put("Accept-Language", "en-US,en;q=0.7,nl;q=0.3");
                        headers.put("Accept-Encoding", "gzip, deflate");

                        sessionUtil.addSessionCookie(headers);
                        return headers;
                    }
                };
            }
        };
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

        String credentials = username + ":" + password;
        String base64EncodedCredentials =
            Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headerMap.put("Authorization", "Basic " + base64EncodedCredentials);

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
     * @param parser   Parser interface
     */
    private static void jsonErrorHandling(JSONObject response, Exception e, JSONParserInterface parser) {
        if (parser == null || response == null)
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

        Log.e(TAG, exNew.getMessage());
        if (parser != null)
            parser.onError(exNew);
    }

    private static int socketTimeout = 5000;
    public static <T> void addToRequestQueue(Request<T> req, RequestQueue queue) {
        req.setTag(TAG);

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        req.setRetryPolicy(retryPolicy);
        queue.add(req);
    }
}