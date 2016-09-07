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

package nl.hnogames.domoticz.app;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;
import eu.inloop.easygcm.EasyGcm;
import eu.inloop.easygcm.GcmListener;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;

public class AppController extends MultiDexApplication implements GcmListener {

    public static final String TAG = AppController.class.getSimpleName();
    private static AppController mInstance;
    int socketTimeout = 1000 * 5;               // 5 seconds
    private RequestQueue mRequestQueue;
    private Tracker mTracker;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (PermissionsUtil.canAccessDeviceState(this))
            StartEasyGCM();

        mInstance = this;
    }

    public void StartEasyGCM() {
        EasyGcm.init(this);
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Context context = getApplicationContext();
            try {
                Log.d(TAG, "Initializing SSL");
                SSLContext sc = SSLContext.getInstance("TLS");
                MemorizingTrustManager mtm = new MemorizingTrustManager(context);
                sc.init(null, new X509TrustManager[]{mtm}, new java.security.SecureRandom());

                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(
                        mtm.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

    /*
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
    */

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        req.setRetryPolicy(retryPolicy);
        getRequestQueue().add(req);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(getString(R.string.analiticsapikey));
        }
        return mTracker;
    }

    @Override
    public void onMessage(String s, Bundle bundle) {
        if (bundle.containsKey("message")) {
            String message = bundle.getString("message");
            try {
                message = URLDecoder.decode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            NotificationUtil.sendSimpleNotification(this.getString(R.string.app_name_domoticz), message, this);
        }
    }

    public void resendRegistrationIdToBackend() {
        final String UUID = DeviceUtils.getUniqueID(this);
        String sender_id = getGCMRegistrationId();
        if (UsefulBits.isEmpty(sender_id) || UsefulBits.isEmpty(UUID))
            return;

        registerMobileForGCM(UUID, sender_id);
    }

    public String getGCMRegistrationId() {
        return EasyGcm.getRegistrationId(this);
    }

    @Override
    public void sendRegistrationIdToBackend(final String sender_id) {
        final String UUID = DeviceUtils.getUniqueID(this);
        if (UsefulBits.isEmpty(sender_id) || UsefulBits.isEmpty(UUID))
            return;

        final Domoticz mDomoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        mDomoticz.CleanMobileDevice(UUID, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                // Previous id cleaned
                registerMobileForGCM(UUID, sender_id);
            }

            @Override
            public void onError(Exception error) {
                // Nothing to clean
                registerMobileForGCM(UUID, sender_id);
            }
        });
    }

    private void registerMobileForGCM(String UUID, String senderid) {

        final Domoticz mDomoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        mDomoticz.AddMobileDevice(UUID, senderid, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                Log.i("GCM", "Device registered on Domoticz");
            }

            @Override
            public void onError(Exception error) {
                if (error != null)
                    Log.i("GCM", "Device not registered on Domoticz, " + error.getMessage());
            }
        });
    }
}