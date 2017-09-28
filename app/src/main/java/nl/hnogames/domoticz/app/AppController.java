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
import shortbread.Shortbread;

import static android.text.TextUtils.isDigitsOnly;

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
        Shortbread.create(this);
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
            String message = decode(bundle.getString("message"));
            String subject = decode(bundle.getString("subject"));
            String body = decode(bundle.getString("body"));

            int prio = 0; //default
            String priority = decode(bundle.getString("priority"));
            if (!UsefulBits.isEmpty(priority) && isDigitsOnly(priority))
                prio = Integer.valueOf(priority);

            if (subject != null && !body.equals(subject)) {
                //String extradata = decode(bundle.getString("extradata"));
                String deviceid = decode(bundle.getString("deviceid"));
                if (!UsefulBits.isEmpty(deviceid) && isDigitsOnly(deviceid) && Integer.valueOf(deviceid) > 0)
                    NotificationUtil.sendSimpleNotification(subject, body, prio, this);
                else
                    NotificationUtil.sendSimpleNotification(Integer.valueOf(deviceid), subject, body, prio, this);
            } else {
                NotificationUtil.sendSimpleNotification(this.getString(R.string.app_name_domoticz), message, prio, this);
            }
        } else {
            if (bundle.containsKey("notification")) {
                Bundle notification = bundle.getBundle("notification");
                if (notification.containsKey("message")) {
                    String message = decode(notification.getString("message"));
                    String subject = decode(notification.getString("subject"));
                    String body = decode(notification.getString("body"));

                    int prio = 0; //default
                    String priority = decode(notification.getString("priority"));
                    if (!UsefulBits.isEmpty(priority) && isDigitsOnly(priority))
                        prio = Integer.valueOf(priority);

                    if (subject != null && !body.equals(subject)) {
                        //String extradata = decode(notification.getString("extradata"));
                        String deviceid = decode(notification.getString("deviceid"));
                        if (!UsefulBits.isEmpty(deviceid) && isDigitsOnly(deviceid) && Integer.valueOf(deviceid) > 0)
                            NotificationUtil.sendSimpleNotification(subject, body, prio, this);
                        else
                            NotificationUtil.sendSimpleNotification(Integer.valueOf(deviceid), subject, body, prio, this);
                    } else {
                        NotificationUtil.sendSimpleNotification(this.getString(R.string.app_name_domoticz), message, prio, this);
                    }
                }
            }
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

    private String decode(String str) {
        if (str != null) {
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (Exception e) {
                Log.i("GCM", "text not decoded: " + str);
            }
        }
        return str;
    }
}