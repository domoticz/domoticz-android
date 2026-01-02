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

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
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
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.EntitlementInfos;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import de.duenndns.ssl.MemorizingTrustManager;
import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.MainActivity;
import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.BeaconInfo;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.SubscriptionsListener;
import nl.hnogames.domoticz.utils.FirebaseConfigHelper;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class AppController extends MultiDexApplication implements BootstrapNotifier, BeaconConsumer {
    public static final String TAG = AppController.class.getSimpleName();
    private static final String IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String ALTBEACON_LAYOUT = "m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25";
    private static final String EDDYSTONE_LAYOUT = "x,s:0-1=feaa,m:2-2=20,d:3-3,d:4-5,d:6-7,d:8-11,d:12-15";
    private static final String EDDYSTONE2_LAYOUT = "s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19";
    private static final String EDDYSTONE3_LAYOUT = "s:0-1=feaa,m:2-2=10,p:3-3:-41,i:4-20v";

    private static final String BACKGROUND_NOTIFICATION_CHANNEL_ID = "6516581";
    public static boolean IsPremiumEnabled = false;
    public static com.revenuecat.purchases.Package premiumPackage;
    public static CustomerInfo customer;
    private static AppController mInstance;
    public BeaconManager beaconManager;
    int socketTimeout = 1000 * 5;               // 5 seconds
    private RequestQueue mRequestQueue;
    private Tracker mTracker;
    private SharedPrefUtil mSharedPrefs;
    private BackgroundPowerSaver backgroundPowerSaver;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public static void HandleRestoreSubscriptions(SubscriptionsListener listener) {
        Purchases.getSharedInstance().restorePurchases(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                HandleCustomerInfo(customerInfo);
                if (listener != null)
                    listener.OnDone(IsPremiumEnabled);
            }

            @Override
            public void onError(@NonNull PurchasesError purchasesError) {
            }
        });
    }

    private static void HandleCustomerInfo(@NonNull CustomerInfo customerInfo) {
        customer = customerInfo;
        EntitlementInfos entitlements = customerInfo.getEntitlements();

        if (!BuildConfig.NEW_VERSION)
            IsPremiumEnabled = true;
        else {
            EntitlementInfo premium = entitlements.get("premium");
            if (premium != null && premium.isActive()) {
                IsPremiumEnabled = true;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // CRITICAL: Initialize Firebase FIRST before anything else
        // This must happen before FirebaseInstanceIdService tries to get the default instance
        mInstance = this;
        mSharedPrefs = new SharedPrefUtil(this);
        initializeUserFirebase();

        registerActivityLifecycleCallbacks(new AppLifecycleTracker());

        Scoop.waffleCone()
                .addFlavor(getString(R.string.theme_default), R.style.AppThemeDefault, true)
                .addDayNightFlavor(getString(R.string.theme_daynight), R.style.AppThemeMain)
                .addFlavor(getString(R.string.theme_orange), R.style.AppThemeAlt1Main)
                .addFlavor(getString(R.string.theme_pink), R.style.AppThemeAlt2Main)
                .addFlavor(getString(R.string.theme_blue), R.style.AppThemeAlt3Main)
                .addFlavor(getString(R.string.theme_green), R.style.AppThemeAlt4Main)
                .addFlavor(getString(R.string.theme_dark_blue), R.style.AppThemeAlt5Main)
                .setSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this))
                .initialize();

        if (mSharedPrefs.isBeaconEnabled()) {
            try {
                StartBeaconScanning();
            } catch (RemoteException e) {
                if (e.getMessage() != null)
                    Log.e("BeaconManager", e.getMessage());
                e.printStackTrace();
            }
        }

        HandleSubscriptions();
    }

    public void HandleSubscriptions() {
        Purchases.setDebugLogsEnabled(BuildConfig.DEBUG);
        String key = getString(R.string.revenuecat_apikey);

        Purchases.configure(new PurchasesConfiguration.Builder(this, key).build());
        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {
            @Override
            public void onReceived(@NonNull Offerings offerings) {
                if (offerings.getCurrent() != null) {
                    List<com.revenuecat.purchases.Package> availablePackages = offerings.getCurrent().getAvailablePackages();
                    if (availablePackages.size() > 0) {
                        premiumPackage = availablePackages.get(0);
                    }
                }
            }

            @Override
            public void onError(@NonNull PurchasesError purchasesError) {
            }
        });

        Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                HandleCustomerInfo(customerInfo);
            }

            @Override
            public void onError(@NonNull PurchasesError purchasesError) {
            }
        });
    }

    public void StopBeaconScanning() {
        try {
            beaconManager.removeMonitorNotifier(this);
            List<BeaconInfo> beacons = mSharedPrefs.getBeaconList();
            for (BeaconInfo b : beacons) {
                beaconManager.stopMonitoringBeaconsInRegion(new Region(b.getName(), Identifier.parse(b.getId()), Identifier.parse(String.valueOf(b.getMajor())), Identifier.parse(String.valueOf(b.getMinor()))));
            }
            beaconManager.unbind(this);
        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.e("BeaconManager", e.getMessage());
        }
    }

    public void StartBeaconScanning() throws RemoteException {
        try {
            createNotificationChannel(
                    BACKGROUND_NOTIFICATION_CHANNEL_ID,
                    R.string.beacon_scan,
                    R.string.beacon_scan_desc
            );

            beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
            beaconManager.getBeaconParsers().clear();
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_LAYOUT));
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(ALTBEACON_LAYOUT));
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE_LAYOUT));
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE2_LAYOUT));
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(EDDYSTONE3_LAYOUT));

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, BACKGROUND_NOTIFICATION_CHANNEL_ID);
            mBuilder.setSmallIcon(R.drawable.domoticz_white);
            mBuilder.setContentTitle(this.getString(R.string.beacon_scan_desc));
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            );
            mBuilder.setContentIntent(pendingIntent);

            try {
                beaconManager.enableForegroundServiceScanning(mBuilder.build(), 997755);
            } catch (Exception ex) {
                Log.e("BeaconManager", ex.getMessage());
            }

            beaconManager.setEnableScheduledScanJobs(false);
            beaconManager.setForegroundScanPeriod(30000L);
            beaconManager.setForegroundBetweenScanPeriod(10000L);
            beaconManager.setBackgroundBetweenScanPeriod(10000L);
            beaconManager.setBackgroundScanPeriod(30000L);
            beaconManager.updateScanPeriods();
            backgroundPowerSaver = new BackgroundPowerSaver(this);

            BeaconManager.setRssiFilterImplClass(RunningAverageRssiFilter.class);
            RunningAverageRssiFilter.setSampleExpirationMilliseconds(10000L);
            beaconManager.bind(this);
            BeaconManager.setDebug(true);
        } catch (Exception ex) {
            Log.e("BeaconManager", ex.getMessage());
        }
    }

    private void createNotificationChannel(String notification_channel, int resIdName, int resIdDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(resIdName);
            String description = getString(resIdDescription);
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(notification_channel, name, importance);
            channel.setDescription(description);
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
                     GooglePlayServicesNotAvailableException |
                     GooglePlayServicesRepairableException e) {
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

    @Override
    public void onBeaconServiceConnect() {
        try {
            int counter = 0;
            beaconManager.addMonitorNotifier(this);
            List<BeaconInfo> beacons = mSharedPrefs.getBeaconList();
            if (beacons != null) {
                for (BeaconInfo b : beacons) {
                    if (b.isEnabled()) {
                        counter++;
                        beaconManager.startMonitoringBeaconsInRegion(new Region(b.getName(), Identifier.parse(b.getId()), Identifier.parse(String.valueOf(b.getMajor())), Identifier.parse(String.valueOf(b.getMinor()))));
                    }
                }
            }
            if (counter == 0)
                StopBeaconScanning();
        } catch (Exception e) {
            if (e.getMessage() != null)
                Log.e("BeaconManager", e.getMessage());
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        BeaconInfo beaconFound = mSharedPrefs.getBeacon(region.getUniqueId());
        Log.d(TAG, "Triggered entering a beacon region: " + beaconFound.getName());

        if (mSharedPrefs.isBeaconNotificationsEnabled()) {
            String notificationTitle = String.format(getString(R.string.beacon_entering), beaconFound.getName());
            String notificationDescription = getString(R.string.beacon_entering_text);
            NotificationUtil.sendSimpleNotification(new NotificationInfo(-1, notificationTitle, notificationDescription, 0, new Date()), this);
        }
        if (beaconFound.getSwitchIdx() > 0)
            handleSwitch(this, beaconFound.getSwitchIdx(), beaconFound.getSwitchPassword(), true, beaconFound.getValue(), beaconFound.isSceneOrGroup());
    }

    @Override
    public void didExitRegion(Region region) {
        BeaconInfo beaconFound = mSharedPrefs.getBeacon(region.getUniqueId());
        Log.d(TAG, "Triggered leaving a beacon region: " + beaconFound.getName());

        if (mSharedPrefs.isBeaconNotificationsEnabled()) {
            String notificationTitle = String.format(
                    getString(R.string.beacon_leaving),
                    beaconFound.getName());
            String notificationDescription = getString(R.string.beacon_leaving_text);
            NotificationUtil.sendSimpleNotification(new NotificationInfo(-1, notificationTitle, notificationDescription, 0, new Date()), this);
        }
        if (beaconFound.getSwitchIdx() > 0)
            handleSwitch(this, beaconFound.getSwitchIdx(), beaconFound.getSwitchPassword(), false, beaconFound.getValue(), beaconFound.isSceneOrGroup());
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        String s = state == 0 ? "Out of distance" : "In distance";
        Log.i("BeaconManager", s + " " + region.getUniqueId());
    }

    private void handleSwitch(final Context context, final int idx, final String password, final boolean checked, final String value, final boolean isSceneOrGroup) {
        StaticHelper.getDomoticz(context).getDevice(new DevicesReceiver() {
            @Override
            public void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo) {
            }

            @Override
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
                if (mDevicesInfo == null)
                    return;

                int jsonAction;
                int jsonUrl = DomoticzValues.Json.Url.Set.SWITCHES;
                int jsonValue = 0;

                if (!isSceneOrGroup) {
                    if (checked) {
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                        if (!UsefulBits.isEmpty(value)) {
                            jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                            jsonValue = getSelectorValue(mDevicesInfo, value);
                        }
                    } else {
                        jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        if (!UsefulBits.isEmpty(value)) {
                            jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                            jsonValue = 0;
                            if (mDevicesInfo.getStatus() != value)//before turning stuff off check if the value is still the same as the on value (else something else took over)
                                return;
                        }
                    }

                    if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE ||
                            mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.DOORLOCKINVERTED) {
                        if (checked)
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        else
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    } else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON)
                        jsonAction = DomoticzValues.Device.Switch.Action.ON;
                    else if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON)
                        jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                } else {
                    jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
                    if (!checked) {
                        jsonAction = DomoticzValues.Scene.Action.ON;
                    } else
                        jsonAction = DomoticzValues.Scene.Action.OFF;
                    if (mDevicesInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                        jsonAction = DomoticzValues.Scene.Action.ON;
                }
                StaticHelper.getDomoticz(context).setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    public void onReceiveResult(String result) {
                    }

                    @Override
                    public void onError(Exception error) {
                    }
                });
            }

            @Override
            public void onError(Exception error) {
            }

        }, idx, isSceneOrGroup);
    }

    private int getSelectorValue(DevicesInfo mDevicesInfo, String value) {
        if (mDevicesInfo == null || mDevicesInfo.getLevelNames() == null)
            return 0;

        int jsonValue = 0;
        if (!UsefulBits.isEmpty(value)) {
            ArrayList<String> levelNames = mDevicesInfo.getLevelNames();
            int counter = 0;
            for (String l : levelNames) {
                if (l.equals(value))
                    break;
                else
                    counter += 10;
            }
            jsonValue = counter;
        }
        return jsonValue;
    }

    /**
     * Initialize Firebase with user configuration if available
     * MUST be called as the first thing in onCreate() to prevent crashes
     */
    private void initializeUserFirebase() {
        if (mSharedPrefs.hasFirebaseConfig()) {
            Log.d(TAG, "Initializing Firebase with user configuration");
            FirebaseConfigHelper.initializeFirebase(this, mSharedPrefs);
        } else {
            Log.w(TAG, "No Firebase configuration found - push notifications will not work");
            Log.w(TAG, "Please configure Firebase in Settings -> Notifications -> Firebase Settings");
        }
    }

    /**
     * Restart the app
     */
    public void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public class AppLifecycleTracker implements Application.ActivityLifecycleCallbacks {
        private int numStarted = 0;
        private boolean isInBackground = true;

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (numStarted == 0) {
                if (isInBackground) {
                    if (activity instanceof MainActivity && !MainActivity.fromSettings) {
                        ((MainActivity) activity).authenticateUser();
                    }
                }
                isInBackground = false;
            }
            numStarted++;
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            numStarted--;
            if (numStarted == 0) {
                isInBackground = true;
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {

        }
    }
}