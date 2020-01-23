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
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.Volley;
import com.ftinc.scoop.Scoop;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;
import nl.hnogames.domoticz.R;
import shortbread.Shortbread;

public class AppController extends MultiDexApplication {
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
        mInstance = this;

        Scoop.waffleCone()
                .addFlavor("Default", R.style.AppThemeMain, true)
                .addFlavor("Light", R.style.AppThemeMain)
                .addFlavor("Dark", R.style.AppThemeDarkMain)
                //.addDayNightFlavor("DayNight", R.style.Theme_Scoop_DayNight)
                //.addFlavor("Alternate 1", R.style.Theme_Scoop_Alt1)
                //.addFlavor("Alternate 2", R.style.Theme_Scoop_Alt2)
                .setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this))
                .initialize();
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public RequestQueue getRequestQueue() {
        SSLContext sc;

        if (mRequestQueue == null) {
            Context context = getApplicationContext();
            try {
                Log.d(TAG, "Initializing SSL");
                MemorizingTrustManager mtm = new MemorizingTrustManager(context);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    sc = SSLContext.getInstance("TLS");
                    sc.init(null, new X509TrustManager[]{mtm}, new java.security.SecureRandom());
                } else {
                    ProviderInstaller.installIfNeeded(getApplicationContext());
                    sc = SSLContext.getInstance("TLSv1.2");
                    sc.init(null, null, null);
                    sc.createSSLEngine();
                }
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(
                        mtm.wrapHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()));
            } catch (KeyManagementException | NoSuchAlgorithmException |
                    GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            }
            mRequestQueue = Volley.newRequestQueue(context);
        }
        return mRequestQueue;
    }

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

}