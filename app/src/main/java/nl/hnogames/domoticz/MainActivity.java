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

package nl.hnogames.domoticz;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.digitus.Digitus;
import com.afollestad.digitus.DigitusCallback;
import com.afollestad.digitus.DigitusErrorType;
import com.afollestad.digitus.FingerprintDialog;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import hotchemi.android.rate.AppRate;
import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Containers.QRCodeInfo;
import nl.hnogames.domoticz.Containers.SpeechInfo;
import nl.hnogames.domoticz.Fragments.Cameras;
import nl.hnogames.domoticz.Fragments.Dashboard;
import nl.hnogames.domoticz.Fragments.Scenes;
import nl.hnogames.domoticz.Fragments.Switches;
import nl.hnogames.domoticz.UI.PasswordDialog;
import nl.hnogames.domoticz.UI.SortDialog;
import nl.hnogames.domoticz.Utils.GCMUtils;
import nl.hnogames.domoticz.Utils.GeoUtils;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SerializableManager;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.TalkBackUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.app.DomoticzDashboardFragment;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.ServerInfo;
import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.UpdateVersionReceiver;
import nl.hnogames.domoticzapi.Interfaces.VersionReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;
import shortbread.Shortcut;

@DebugLog
public class MainActivity extends AppCompatPermissionsActivity implements DigitusCallback, FingerprintDialog.Callback {
    private static TalkBackUtil oTalkBackUtil;
    private final int iQRResultCode = 775;
    private final int iWelcomeResultCode = 885;
    private final int iSettingsResultCode = 995;
    public boolean onPhone;
    private SharedPrefUtil mSharedPrefs;
    private String TAG = MainActivity.class.getSimpleName();
    private String[] fragments;
    private ServerUtil mServerUtil;
    private SearchView searchViewAction;
    private Toolbar toolbar;
    private ArrayList<String> stackFragments = new ArrayList<>();
    private Domoticz domoticz;
    private Timer cameraRefreshTimer = null;
    private Fragment latestFragment = null;
    private Drawer drawer;
    private SpeechRecognizer speechRecognizer;
    private RecognitionProgressView recognitionProgressView;
    private RecognitionListenerAdapter recognitionListener;
    private boolean listeningSpeechRecognition = false;
    private boolean fromVoiceWidget = false;
    private boolean fromQRCodeWidget = false;
    private MenuItem speechMenuItem;
    private boolean validateOnce = true;
    private PermissionHelper permissionHelper;
    private boolean fromShortcut = false;
    private AdView mAdView;

    @DebugLog
    public ServerUtil getServerUtil() {
        if (mServerUtil == null)
            mServerUtil = new ServerUtil(this);
        return mServerUtil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDarkMain);
        else
            setTheme(R.style.AppThemeMain);
        permissionHelper = PermissionHelper.getInstance(this);

        UsefulBits.checkAPK(this, mSharedPrefs);
        if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
            setContentView(R.layout.activity_newmain_free);
            mAdView = (AdView) findViewById(R.id.adView);
            MobileAds.initialize(this, this.getString(R.string.ADMOB_APP_KEY));
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("83DBECBB403C3E924CAA8B529F7E848E").build();
            mAdView.loadAd(adRequest);
        } else {
            setContentView(R.layout.activity_newmain_paid);
            mAdView = (AdView) findViewById(R.id.adView);
            mAdView.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                fromVoiceWidget = extras.getBoolean("VOICE", false);
                fromQRCodeWidget = extras.getBoolean("QRCODE", false);
            }
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean resolvableError = UsefulBits.checkPlayServicesAvailable(this);
        if (!resolvableError) this.finish();

        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            if (mSharedPrefs.isStartupSecurityEnabled()) {
                Digitus.init(this,
                        getString(R.string.app_name),
                        69,
                        this);
            } else {
                new GeoUtils(this, this).AddGeofences();
                buildScreen();
            }
        }
    }

    public void initTalkBack() {
        if (mSharedPrefs.isTalkBackEnabled()) {
            oTalkBackUtil = new TalkBackUtil();
            if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage())) {
                oTalkBackUtil.Init(this, new Locale(mSharedPrefs.getDisplayLanguage()), new TalkBackUtil.InitListener() {
                    @Override
                    public void onInit(int status) {
                    }
                });
            } else {
                oTalkBackUtil.Init(this, new TalkBackUtil.InitListener() {
                    @Override
                    public void onInit(int status) {
                    }
                });
            }
        }
    }

    public void Talk(String message) {
        if (mSharedPrefs.isTalkBackEnabled() && oTalkBackUtil != null)
            oTalkBackUtil.Talk(message);
    }

    public void Talk(int message) {
        Talk(this.getString(message));
    }

    @DebugLog
    public void buildScreen() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            applyLanguage();
            TextView usingTabletLayout = (TextView) findViewById(R.id.tabletLayout);

            if (usingTabletLayout == null)
                onPhone = true;

            appRate();
            initTalkBack();

            mServerUtil = new ServerUtil(this);
            if (mServerUtil.getActiveServer() != null && UsefulBits.isEmpty(mServerUtil.getActiveServer().getRemoteServerUrl())) {
                Toast.makeText(this, "Incorrect settings detected, please reconfigure this app.", Toast.LENGTH_LONG).show();

                //incorrect settings detected
                mSharedPrefs.setNavigationDefaults();
                Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
                startActivityForResult(welcomeWizard, iWelcomeResultCode);
                mSharedPrefs.setFirstStart(false);
            } else {
                if (domoticz == null)
                    domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());

                if (!fromVoiceWidget && !fromQRCodeWidget) {
                    setupMobileDevice();
                    checkDomoticzServerUpdate();
                    setScheduledTasks();

                    WidgetUtils.RefreshWidgets(this);
                    UsefulBits.checkDownloadedLanguage(this, mServerUtil, false, false);
                    drawNavigationMenu(null);

                    UsefulBits.getServerConfigForActiveServer(this, false, new ConfigReceiver() {
                        @Override
                        @DebugLog
                        public void onReceiveConfig(ConfigInfo settings) {
                            drawNavigationMenu(settings);
                            if (!fromShortcut) addFragment();
                        }

                        @Override
                        @DebugLog
                        public void onError(Exception error) {
                            //drawNavigationMenu(null);
                            if (!fromShortcut) addFragment();
                        }
                    }, mServerUtil.getActiveServer().getConfigInfo(this));
                } else {
                    if (!fromShortcut) addFragment();
                }
            }
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    /* Called when the second activity's finishes */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (!res.getBoolean("RESULT", false))
                        this.finish();
                    else {
                        if (mSharedPrefs.darkThemeEnabled())
                            setTheme(R.style.AppThemeDarkMain);
                        else
                            setTheme(R.style.AppThemeMain);
                        buildScreen();
                    }
                    SerializableManager.cleanAllSerializableObjects(this);
                    break;
                case iSettingsResultCode:
                    mServerUtil = new ServerUtil(this);
                    SerializableManager.cleanAllSerializableObjects(this);
                    if (mSharedPrefs.darkThemeEnabled())
                        setTheme(R.style.AppThemeDarkMain);
                    else
                        setTheme(R.style.AppThemeMain);
                    this.recreate();
                    break;
                case iQRResultCode:
                    String QR_ID = data.getStringExtra("QRCODE");
                    if (mSharedPrefs.isQRCodeEnabled()) {
                        ArrayList<QRCodeInfo> qrList = mSharedPrefs.getQRCodeList();
                        QRCodeInfo foundQRCode = null;
                        Log.i(TAG, "QR Code ID Found: " + QR_ID);

                        if (qrList != null && qrList.size() > 0) {
                            for (QRCodeInfo n : qrList) {
                                if (n.getId().equals(QR_ID))
                                    foundQRCode = n;
                            }
                        }

                        if (foundQRCode != null && foundQRCode.isEnabled()) {
                            handleSwitch(foundQRCode.getSwitchIdx(), foundQRCode.getSwitchPassword(), -1, foundQRCode.getValue(), foundQRCode.isSceneOrGroup());
                            Toast.makeText(MainActivity.this, getString(R.string.qrcode) + " " + foundQRCode.getName(), Toast.LENGTH_SHORT).show();
                        } else {
                            if (foundQRCode == null)
                                Toast.makeText(MainActivity.this, getString(R.string.qrcode_new_found), Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(MainActivity.this, getString(R.string.qr_code_disabled), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        } else if (resultCode == 789) {
            //reload settings
            startActivityForResult(new Intent(this, SettingsActivity.class), iSettingsResultCode);
            SerializableManager.cleanAllSerializableObjects(this);
        } else {
            switch (requestCode) {
                case iWelcomeResultCode:
                    this.finish();
                    break;
            }
        }
        if (fromQRCodeWidget)
            this.finish();

        permissionHelper.onActivityForResult(requestCode);
    }

    private void handleSwitch(final int idx, final String password, final int inputJSONAction, final String value, final boolean isSceneOrGroup) {
        if (domoticz == null)
            domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());

        domoticz.getDevice(new DevicesReceiver() {
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
                    if (inputJSONAction < 0) {
                        if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                                mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                            if (!mDevicesInfo.getStatusBoolean())
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                            else {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            }
                        } else {
                            if (!mDevicesInfo.getStatusBoolean()) {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            } else
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        }
                    } else {
                        if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                                mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                            if (inputJSONAction == 1)
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                            else {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            }
                        } else {
                            if (inputJSONAction == 1) {
                                jsonAction = DomoticzValues.Device.Switch.Action.ON;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = getSelectorValue(mDevicesInfo, value);
                                }
                            } else
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                        }
                    }

                    switch (mDevicesInfo.getSwitchTypeVal()) {
                        case DomoticzValues.Device.Type.Value.PUSH_ON_BUTTON:
                            jsonAction = DomoticzValues.Device.Switch.Action.ON;
                            break;
                        case DomoticzValues.Device.Type.Value.PUSH_OFF_BUTTON:
                            jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                            break;
                    }
                } else {
                    jsonUrl = DomoticzValues.Json.Url.Set.SCENES;
                    if (inputJSONAction < 0) {
                        if (!mDevicesInfo.getStatusBoolean()) {
                            jsonAction = DomoticzValues.Scene.Action.ON;
                        } else
                            jsonAction = DomoticzValues.Scene.Action.OFF;
                    } else {
                        if (inputJSONAction == 1) {
                            jsonAction = DomoticzValues.Scene.Action.ON;
                        } else
                            jsonAction = DomoticzValues.Scene.Action.OFF;
                    }

                    if (mDevicesInfo.getType().equals(DomoticzValues.Scene.Type.SCENE))
                        jsonAction = DomoticzValues.Scene.Action.ON;
                }

                domoticz.setAction(idx, jsonUrl, jsonAction, jsonValue, password, new setCommandReceiver() {
                    @Override
                    @DebugLog
                    public void onReceiveResult(String result) {
                        Log.d(TAG, result);
                        if (fromQRCodeWidget)
                            MainActivity.this.finish();
                    }

                    @Override
                    @DebugLog
                    public void onError(Exception error) {
                        if (fromQRCodeWidget)
                            MainActivity.this.finish();
                    }
                });
            }

            @Override
            public void onError(Exception error) {
                if (fromQRCodeWidget)
                    MainActivity.this.finish();
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


    @DebugLog
    public void refreshFragment() {
        Fragment f = latestFragment;
        if (f instanceof DomoticzRecyclerFragment) {
            ((DomoticzRecyclerFragment) f).refreshFragment();
        } else if (f instanceof DomoticzCardFragment)
            ((DomoticzCardFragment) f).refreshFragment();
        else if (f instanceof DomoticzDashboardFragment)
            ((DomoticzDashboardFragment) f).refreshFragment();
    }

    @DebugLog
    public void removeFragmentStack(String fragment) {
        if (stackFragments != null) {
            if (stackFragments.contains(fragment))
                stackFragments.remove(fragment);
        }
    }

    @DebugLog
    public void addFragmentStack(String fragment) {
        int screenIndex = mSharedPrefs.getStartupScreenIndex();
        if (fragment.equals(getResources().getStringArray(R.array.drawer_fragments)[screenIndex])) {
            stackFragments = new ArrayList<>();
            stackFragments.add(fragment);
        } else {
            if (stackFragments == null)
                stackFragments = new ArrayList<>();

            if (!stackFragments.contains(fragment)) {
                if (stackFragments.size() > 1)
                    stackFragments.remove(stackFragments.size() - 1);
                stackFragments.add(fragment);
            }
        }
    }

    private void setScreenAlwaysOn() {
        if (mSharedPrefs.getAlwaysOn())
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    @DebugLog
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            // Notify Digitus of the result
            Digitus.get().handleResult(requestCode, permissions, grantResults);
        } catch (Exception ex) {
        }
    }

    @Override
    @DebugLog
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Fragment f = latestFragment;
        if ((f instanceof DomoticzDashboardFragment)) {
            ((DomoticzDashboardFragment) f).setGridViewLayout();
        } else if (f instanceof DomoticzRecyclerFragment) {
            ((DomoticzRecyclerFragment) f).setGridViewLayout();
        }
    }

    @DebugLog
    public void changeFragment(String fragment) {
        //if (!isFinishing()) {
        try {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            latestFragment = Fragment.instantiate(MainActivity.this, fragment);
            tx.replace(R.id.main, latestFragment);
            tx.commitAllowingStateLoss();
            addFragmentStack(fragment);
            saveScreenToAnalytics(fragment);
        } catch (Exception e) {
            Log.e("Fragment", e.getMessage());
        }
        //}
    }

    private void addFragment() {
        if (!isFinishing()) {
            try {
                int screenIndex = mSharedPrefs.getStartupScreenIndex();
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                latestFragment = Fragment.instantiate(MainActivity.this, getResources().getStringArray(R.array.drawer_fragments)[screenIndex]);
                tx.replace(R.id.main, latestFragment);
                tx.commitAllowingStateLoss();
                addFragmentStack(getResources().getStringArray(R.array.drawer_fragments)[screenIndex]);
                saveScreenToAnalytics(getResources().getStringArray(R.array.drawer_fragments)[screenIndex]);
            } catch (Exception e) {
            }
        }
    }

    private void saveScreenToAnalytics(String screen) {
        try {
            AppController application = (AppController) getApplication();
            Tracker mTracker = application.getDefaultTracker();
            mTracker.setScreenName(screen);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        } catch (Exception ignored) {
        }
    }

    private void applyLanguage() {
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage())) {
            // User has set a language in settings
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        }
    }

    private void setupMobileDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionsUtil.canAccessDeviceState(this)) {
                permissionHelper.request(PermissionsUtil.INITIAL_DEVICE_PERMS);
            } else {
                try {
                    String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                    Log.d("Firbase id login", "Refreshed token: " + refreshedToken);
                    GCMUtils.sendRegistrationIdToBackend(this, refreshedToken);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {

            try {
                String refreshedToken = FirebaseInstanceId.getInstance().getToken();
                Log.d("Firbase id login", "Refreshed token: " + refreshedToken);
                GCMUtils.sendRegistrationIdToBackend(this, refreshedToken);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void appRate() {
        if (!BuildConfig.DEBUG) {
            AppRate.with(this)
                    .setInstallDays(0) // default 10, 0 means install day.
                    .setLaunchTimes(3) // default 10
                    .setRemindInterval(2) // default 1
                    .monitor();

            // Show a dialog if meets conditions
            AppRate.showRateDialogIfMeetsConditions(this);
        }
    }

    @DebugLog
    public void drawNavigationMenu(final ConfigInfo mConfig) {
        ConfigInfo config = mConfig;

        if (config == null)
            config = mServerUtil.getActiveServer().getConfigInfo(this);

        ProfileDrawerItem loggedinAccount = new ProfileDrawerItem().withName("Logged in").withEmail(domoticz.getUserCredentials(Domoticz.Authentication.USERNAME))
                .withIcon(R.mipmap.ic_launcher);
        if (mSharedPrefs.darkThemeEnabled()) {
            loggedinAccount.withSelectedColorRes(R.color.material_indigo_600);
        }

        // Create the AccountHeader
        final ConfigInfo finalConfig = config;
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.darkheader)
                .addProfiles(loggedinAccount)
                .withOnlyMainProfileImageVisible(true)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    @DebugLog
                    public boolean onProfileChanged(View view, final IProfile profile, boolean current) {
                        if (!current) {
                            if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
                                if (getFragmentCoordinatorLayout() != null) {
                                    Snackbar.make(getFragmentCoordinatorLayout(), getString(R.string.category_account) + " " + getString(R.string.premium_feature), Snackbar.LENGTH_LONG)
                                            .setAction(R.string.upgrade, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    UsefulBits.openPremiumAppStore(MainActivity.this);
                                                }
                                            })
                                            .setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.material_blue_600))
                                            .show();
                                }
                                return false;
                            } else {
                                PasswordDialog passwordDialog = new PasswordDialog(MainActivity.this, null);
                                passwordDialog.show();
                                passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
                                    @Override
                                    @DebugLog
                                    public void onDismiss(String password) {
                                        if (UsefulBits.isEmpty(password)) {
                                            UsefulBits.showSnackbar(MainActivity.this, getFragmentCoordinatorLayout(), R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                            Talk(R.string.security_wrong_code);
                                            drawNavigationMenu(finalConfig);
                                        } else {
                                            for (UserInfo user : finalConfig.getUsers()) {
                                                if (user.getUsername() == profile.getEmail().getText()) {
                                                    String md5Pass = UsefulBits.getMd5String(password);
                                                    if (md5Pass.equals(user.getPassword())) {
                                                        //if correct set credentials in activeserver and recreate drawer
                                                        domoticz.setUserCredentials(user.getUsername(), password);
                                                        domoticz.LogOff();
                                                        UsefulBits.getServerConfigForActiveServer(MainActivity.this, true, new ConfigReceiver() {
                                                            @Override
                                                            @DebugLog
                                                            public void onReceiveConfig(ConfigInfo settings) {
                                                                UsefulBits.showSnackbar(MainActivity.this, getFragmentCoordinatorLayout(), R.string.user_switch, Snackbar.LENGTH_SHORT);
                                                                drawNavigationMenu(finalConfig);
                                                            }

                                                            @Override
                                                            @DebugLog
                                                            public void onError(Exception error) {
                                                            }
                                                        }, finalConfig);
                                                    } else {
                                                        UsefulBits.showSnackbar(MainActivity.this, getFragmentCoordinatorLayout(), R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                                                        drawNavigationMenu(finalConfig);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancel() {
                                    }
                                });
                            }

                            drawNavigationMenu(finalConfig);
                        }
                        return false;
                    }
                })
                .build();

        if (config != null &&
                config.getUsers() != null) {
            for (UserInfo user : config.getUsers()) {

                ProfileDrawerItem profile = new ProfileDrawerItem().withName(user.getRightsValue(this)
                ).withEmail(user.getUsername())
                        .withIcon(R.drawable.users)
                        .withEnabled(user.isEnabled());

                if (mSharedPrefs.darkThemeEnabled()) {
                    profile.withSelectedColorRes(R.color.material_indigo_600);
                }

                headerResult.addProfiles(profile);
            }
        }

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .withDrawerItems(getDrawerItems())
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    @DebugLog
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            if (searchViewAction != null) {
                                searchViewAction.setQuery("", false);
                                searchViewAction.clearFocus();
                            }

                            if (drawerItem.getTag() != null && String.valueOf(drawerItem.getTag()).equals("Settings")) {
                                stopCameraTimer();
                                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), iSettingsResultCode);
                            } else if (drawerItem.getTag() != null) {
                                changeFragment(String.valueOf(drawerItem.getTag()));
                                stopCameraTimer();

                                invalidateOptionsMenu();
                                if (onPhone)
                                    drawer.closeDrawer();
                            }
                        }
                        return false;
                    }
                })
                .build();

        drawer.addStickyFooterItem(createSecondaryDrawerItem(this.getString(R.string.action_settings), null, "gmd_settings", "Settings"));
    }

    private List<IDrawerItem> getDrawerItems() {
        List<IDrawerItem> drawerItems = new ArrayList<>();
        String[] drawerActions = mSharedPrefs.getNavigationActions();
        fragments = mSharedPrefs.getNavigationFragments();
        String ICONS[] = mSharedPrefs.getNavigationIcons();

        for (int i = 0; i < drawerActions.length; i++)
            if (fragments[i].indexOf("Wizard") >= 0 || fragments[i].indexOf("Dashboard") >= 0)
                drawerItems.add(createPrimaryDrawerItem(drawerActions[i], null, ICONS[i], fragments[i]));
        drawerItems.add(new DividerDrawerItem());
        for (int i = 0; i < drawerActions.length; i++)
            if (fragments[i].indexOf("Wizard") < 0 && fragments[i].indexOf("Dashboard") < 0)
                drawerItems.add(createSecondaryDrawerItem(drawerActions[i], null, ICONS[i], fragments[i]));

        return drawerItems;
    }

    private SecondaryDrawerItem createSecondaryDrawerItem(String title, String badge, String icon, String fragmentID) {
        SecondaryDrawerItem item = new SecondaryDrawerItem();
        item.withName(title)
                .withBadge(badge)
                .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700))
                .withIcon(GoogleMaterial.Icon.valueOf(icon)).withIconColorRes(R.color.material_indigo_600)
                .withTag(fragmentID);

        if (mSharedPrefs.darkThemeEnabled()) {
            item.withIconColorRes(R.color.white);
            item.withSelectedColorRes(R.color.material_indigo_600);
        }

        return item;
    }

    private PrimaryDrawerItem createPrimaryDrawerItem(String title, String badge, String icon, String fragmentID) {
        PrimaryDrawerItem item = new PrimaryDrawerItem();
        item.withName(title).withBadge(badge).withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.md_red_700))
                .withIcon(GoogleMaterial.Icon.valueOf(icon)).withIconColorRes(R.color.material_indigo_600)
                .withTag(fragmentID);

        if (mSharedPrefs.darkThemeEnabled()) {
            item.withIconColorRes(R.color.white);
            item.withSelectedColorRes(R.color.material_indigo_600);
        }

        return item;
    }

    private void checkDomoticzServerUpdate() {
        if (mSharedPrefs.checkForUpdatesEnabled()) {
            // Get latest Domoticz version update
            domoticz.getUpdate(new UpdateVersionReceiver() {
                @Override
                @DebugLog
                public void onReceiveUpdate(ServerUpdateInfo serverUpdateInfo) {
                    boolean haveUpdate = serverUpdateInfo.isUpdateAvailable();

                    if (mServerUtil.getActiveServer() != null && haveUpdate) {
                        // Only show an update revision snackbar once per revision number!
                        if (!mSharedPrefs.getLastUpdateShown().equals(serverUpdateInfo.getUpdateRevisionNumber())) {
                            // Write update version to shared preferences
                            mServerUtil.getActiveServer().setServerUpdateInfo(MainActivity.this, serverUpdateInfo);
                            mServerUtil.saveDomoticzServers(true);
                            if (serverUpdateInfo.getSystemName().equalsIgnoreCase("linux")) {
                                // Great! We can remote/auto update Linux systems
                                getCurrentServerVersion();
                            } else {
                                // No remote/auto updating available for other systems (like Windows, Synology)
                                showSnackbar(getString(R.string.server_update_available));
                            }
                            mSharedPrefs.setLastUpdateShown(serverUpdateInfo.getUpdateRevisionNumber());
                        }
                    }
                }

                @Override
                @DebugLog
                public void onError(Exception error) {
                    String message = String.format(
                            getString(R.string.error_couldNotCheckForUpdates),
                            domoticz.getErrorMessage(error));
                    showSnackbar(message);

                    if (mServerUtil.getActiveServer().getServerUpdateInfo(MainActivity.this) != null)
                        mServerUtil.getActiveServer().getServerUpdateInfo(MainActivity.this).setCurrentServerVersion("");
                    mServerUtil.saveDomoticzServers(true);
                }
            });
        }
    }

    private void getCurrentServerVersion() {
        // Get current Domoticz server version
        domoticz.getServerVersion(new VersionReceiver() {
            @Override
            @DebugLog
            public void onReceiveVersion(String serverVersion) {
                if (!UsefulBits.isEmpty(serverVersion)) {

                    if (mServerUtil.getActiveServer() != null &&
                            mServerUtil.getActiveServer().getServerUpdateInfo(MainActivity.this) != null) {
                        mServerUtil.getActiveServer()
                                .getServerUpdateInfo(MainActivity.this)
                                .setCurrentServerVersion(serverVersion);
                    }

                    String[] version
                            = serverVersion.split("\\.");
                    // Update version is only revision number
                    String updateVersion = (mServerUtil.getActiveServer() != null &&
                            mServerUtil.getActiveServer().getServerUpdateInfo(MainActivity.this) != null) ?
                            version[0] + "."
                                    + mServerUtil.getActiveServer()
                                    .getServerUpdateInfo(MainActivity.this)
                                    .getUpdateRevisionNumber() :
                            version[0];

                    String message
                            = String.format(getString(R.string.update_available_enhanced),
                            serverVersion,
                            updateVersion);
                    showSnackBarToUpdateServer(message);
                }
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                String message = String.format(
                        getString(R.string.error_couldNotCheckForUpdates),
                        domoticz.getErrorMessage(error));
                showSnackbar(message);
            }
        });
    }

    private void showSnackBarToUpdateServer(String message) {
        CoordinatorLayout layout = getFragmentCoordinatorLayout();
        if (layout != null) {
            UsefulBits.showSnackbarWithAction(this, layout, message, Snackbar.LENGTH_SHORT, null, new View.OnClickListener() {
                @Override
                @DebugLog
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, UpdateActivity.class));
                }
            }, this.getString(R.string.update_server));
        }
    }

    @Override
    @DebugLog
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment f = latestFragment;

        if (!fromVoiceWidget && !fromQRCodeWidget) {
            if ((f instanceof Cameras)) {
                if (cameraRefreshTimer != null)
                    getMenuInflater().inflate(R.menu.menu_camera_pause, menu);
                else
                    getMenuInflater().inflate(R.menu.menu_camera, menu);
            } else if ((f instanceof DomoticzDashboardFragment) || (f instanceof DomoticzRecyclerFragment)) {
                if ((f instanceof Dashboard) || (f instanceof Scenes) || (f instanceof Switches))
                    getMenuInflater().inflate(R.menu.menu_main_sort, menu);
                else
                    getMenuInflater().inflate(R.menu.menu_main, menu);

                MenuItem searchMenuItem = menu.findItem(R.id.search);
                searchViewAction = (SearchView) MenuItemCompat
                        .getActionView(searchMenuItem);
                searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    @DebugLog
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    @DebugLog
                    public boolean onQueryTextChange(String newText) {
                        Fragment n = latestFragment;
                        if (n instanceof DomoticzDashboardFragment) {
                            ((DomoticzDashboardFragment) n).Filter(newText);
                        } else if (n instanceof DomoticzRecyclerFragment) {
                            ((DomoticzRecyclerFragment) n).Filter(newText);
                        }
                        return false;
                    }
                });
            } else {
                getMenuInflater().inflate(R.menu.menu_simple, menu);
            }

            if (mSharedPrefs.isMultiServerEnabled()) {
                //set multi server actionbar item
                MenuItem searchMenuItem = menu.findItem(R.id.action_switch_server);
                if (searchMenuItem != null && mServerUtil != null && mServerUtil.getEnabledServerList() != null && mServerUtil.getEnabledServerList().size() > 1) {
                    searchMenuItem.setVisible(true);
                } else if (searchMenuItem != null)
                    searchMenuItem.setVisible(false);
            }

            if (mSharedPrefs.isQRCodeEnabled()) {
                MenuItem searchMenuItem = menu.findItem(R.id.action_scan_qrcode);
                if (searchMenuItem != null && mSharedPrefs != null && mSharedPrefs.getQRCodeList() != null && mSharedPrefs.getQRCodeList().size() > 0) {
                    searchMenuItem.setVisible(true);
                } else if (searchMenuItem != null)
                    searchMenuItem.setVisible(false);
            }

            if (mSharedPrefs.isSpeechEnabled()) {
                speechMenuItem = menu.findItem(R.id.action_speech);
                if (speechMenuItem != null && mSharedPrefs != null && mSharedPrefs.getSpeechList() != null && mSharedPrefs.getSpeechList().size() > 0) {
                    speechMenuItem.setVisible(true);
                } else if (speechMenuItem != null)
                    speechMenuItem.setVisible(false);
            }
        } else {
            if (fromVoiceWidget) {
                getMenuInflater().inflate(R.menu.menu_speech, menu);
                if (mSharedPrefs.isSpeechEnabled()) {
                    speechMenuItem = menu.findItem(R.id.action_speech);
                    if (speechMenuItem != null && mSharedPrefs != null && mSharedPrefs.getSpeechList() != null && mSharedPrefs.getSpeechList().size() > 0) {
                        speechMenuItem.setVisible(true);
                        onOptionsItemSelected(speechMenuItem);
                    } else if (speechMenuItem != null)
                        speechMenuItem.setVisible(false);
                }
            } else if (fromQRCodeWidget) {
                getMenuInflater().inflate(R.menu.menu_qrcode, menu);
                if (mSharedPrefs.isQRCodeEnabled()) {
                    MenuItem qrcodeMenuItem = menu.findItem(R.id.action_scan_qrcode);
                    if (qrcodeMenuItem != null && mSharedPrefs != null && mSharedPrefs.getQRCodeList() != null && mSharedPrefs.getQRCodeList().size() > 0) {
                        qrcodeMenuItem.setVisible(true);
                        onOptionsItemSelected(qrcodeMenuItem);
                    } else if (qrcodeMenuItem != null)
                        qrcodeMenuItem.setVisible(false);
                }
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    @DebugLog
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_speech:
                    if (speechRecognizer == null)
                        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
                    if (recognitionProgressView == null)
                        recognitionProgressView = (RecognitionProgressView) findViewById(R.id.recognition_view);
                    if (recognitionListener == null) {
                        recognitionListener = new RecognitionListenerAdapter() {
                            @Override
                            public void onResults(Bundle results) {
                                showSpeechResults(results);
                                stopRecognition();
                            }
                        };
                    }
                    if (mSharedPrefs.darkThemeEnabled()) {
                        int color = ContextCompat.getColor(MainActivity.this, R.color.background_dark);
                        if (color != 0 && recognitionProgressView != null)
                            recognitionProgressView.setBackgroundColor(color);
                    }
                    int[] colors = {
                            ContextCompat.getColor(this, R.color.material_amber_600),
                            ContextCompat.getColor(this, R.color.material_blue_600),
                            ContextCompat.getColor(this, R.color.material_deep_purple_600),
                            ContextCompat.getColor(this, R.color.material_green_600),
                            ContextCompat.getColor(this, R.color.material_orange_600)
                    };
                    recognitionProgressView.setColors(colors);
                    recognitionProgressView.setSpeechRecognizer(speechRecognizer);
                    recognitionProgressView.setRecognitionListener(recognitionListener);
                    recognitionProgressView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startRecognition();
                        }
                    }, 50);

                    return true;
                case R.id.action_camera_play:
                    if (cameraRefreshTimer == null) {
                        cameraRefreshTimer = new Timer("camera", true);
                        cameraRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            @DebugLog
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    @DebugLog
                                    public void run() {
                                        //call refresh fragment
                                        Fragment f = latestFragment;
                                        if (f instanceof Cameras) {
                                            ((Cameras) f).refreshFragment();
                                        } else {
                                            //we're not at the camera fragment? stop timer!
                                            stopCameraTimer();
                                            invalidateOptionsMenu();
                                        }
                                    }
                                });
                            }
                        }, 0, 5000);//schedule in 5 seconds
                    }
                    invalidateOptionsMenu();//set pause button
                    return true;
                case R.id.action_scan_qrcode:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (PermissionsUtil.canAccessCamera(this)) {
                            Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                            startActivityForResult(iQRCodeScannerActivity, iQRResultCode);
                        } else {
                            permissionHelper.request(PermissionsUtil.INITIAL_CAMERA_PERMS);
                        }
                    } else {
                        Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                        startActivityForResult(iQRCodeScannerActivity, iQRResultCode);
                    }
                    return true;
                case R.id.action_camera_pause:
                    stopCameraTimer();
                    invalidateOptionsMenu();//set pause button
                    return true;
                case R.id.action_sort:
                    SortDialog infoDialog = new SortDialog(
                            this,
                            R.layout.dialog_switch_logs);
                    infoDialog.onDismissListener(new SortDialog.DismissListener() {
                        @Override
                        @DebugLog
                        public void onDismiss(String selectedSort) {
                            Log.i(TAG, "Sorting: " + selectedSort);
                            Fragment f = latestFragment;
                            if (f instanceof DomoticzRecyclerFragment) {
                                ((DomoticzRecyclerFragment) f).sortFragment(selectedSort);
                            } else if (f instanceof DomoticzDashboardFragment) {
                                ((DomoticzDashboardFragment) f).sortFragment(selectedSort);
                            }
                        }
                    });
                    infoDialog.show();
                    return true;
                case R.id.action_switch_server:
                    showServerDialog();
                    return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

    private void playRecognitionAnimation() {
        ((FrameLayout) findViewById(R.id.main)).setVisibility(View.GONE);
        recognitionProgressView.setVisibility(View.VISIBLE);
        recognitionProgressView.play();
    }

    private void stopRecognitionAnimation() {
        ((FrameLayout) findViewById(R.id.main)).setVisibility(View.VISIBLE);
        recognitionProgressView.setVisibility(View.GONE);
        recognitionProgressView.stop();
    }

    @DebugLog
    private void showSpeechResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        int jsonAction = -1;
        String actionFound = "Toggle";
        String SPEECH_ID = matches.get(0).toLowerCase().trim();
        if (mSharedPrefs.isSpeechEnabled()) {
            ArrayList<SpeechInfo> qrList = mSharedPrefs.getSpeechList();
            SpeechInfo foundSPEECH = null;

            if (qrList != null && qrList.size() > 0) {
                for (SpeechInfo n : qrList) {
                    if (n.getId().equals(SPEECH_ID))
                        foundSPEECH = n;
                }
            }

            if (foundSPEECH == null) {
                if (SPEECH_ID.endsWith(getString(R.string.button_state_off).toLowerCase())) {
                    actionFound = getString(R.string.button_state_off);
                    SPEECH_ID = SPEECH_ID.replace(getString(R.string.button_state_off).toLowerCase(), "").trim();
                    jsonAction = 0;
                } else if (SPEECH_ID.endsWith(getString(R.string.button_state_on).toLowerCase())) {
                    actionFound = getString(R.string.button_state_on);
                    SPEECH_ID = SPEECH_ID.replace(getString(R.string.button_state_on).toLowerCase(), "").trim();
                    jsonAction = 1;
                }

                if (qrList != null && qrList.size() > 0) {
                    for (SpeechInfo n : qrList) {
                        if (n.getId().equals(SPEECH_ID))
                            foundSPEECH = n;
                    }
                }
            }

            if (foundSPEECH != null && foundSPEECH.isEnabled()) {
                handleSwitch(foundSPEECH.getSwitchIdx(), foundSPEECH.getSwitchPassword(), jsonAction, foundSPEECH.getValue(), foundSPEECH.isSceneOrGroup());
                Toast.makeText(MainActivity.this, getString(R.string.Speech) + ": " + SPEECH_ID + " - " + actionFound, Toast.LENGTH_SHORT).show();
            } else {
                if (foundSPEECH == null)
                    Toast.makeText(MainActivity.this, getString(R.string.Speech_found) + ": " + SPEECH_ID, Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, getString(R.string.Speech_disabled) + ": " + SPEECH_ID, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRecognition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionsUtil.canAccessAudioState(this)) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                speechRecognizer.startListening(intent);
                listeningSpeechRecognition = true;
                playRecognitionAnimation();
            } else {
                permissionHelper.request(PermissionsUtil.INITIAL_AUDIO_PERMS);
            }
        } else {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizer.startListening(intent);

            listeningSpeechRecognition = true;
            playRecognitionAnimation();
        }
    }

    private void stopRecognition() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.cancel();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        stopRecognitionAnimation();
        listeningSpeechRecognition = false;

        if (fromVoiceWidget)
            this.finish();
    }

    @DebugLog
    public void showServerDialog() {
        String[] serverNames = new String[mServerUtil.getServerList().size()];
        int count = 0;
        int selectionId = -1;

        for (ServerInfo s : mServerUtil.getEnabledServerList()) {
            serverNames[count] = s.getServerName();
            if (mServerUtil.getActiveServer() != null &&
                    mServerUtil.getActiveServer().getServerName().equals(s.getServerName()))
                selectionId = count;
            count++;
        }

        //show dialog with servers
        new MaterialDialog.Builder(this)
                .title(R.string.choose_server)
                .items(serverNames)
                .itemsCallbackSingleChoice(selectionId, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    @DebugLog
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        ServerInfo setNew = null;
                        for (ServerInfo s : mServerUtil.getEnabledServerList()) {
                            if (s.getServerName().equals(text)) {
                                String message = String.format(
                                        getString(R.string.switch_to_server), s.getServerName());
                                showSnackbar(message);
                                setNew = s;
                            }
                        }
                        if (setNew != null) {
                            mServerUtil.setActiveServer(setNew);
                            buildScreen();
                            invalidateOptionsMenu();
                        }
                        return false;
                    }
                })
                .show();
    }

    /**
     * Starts the scheduled tasks service via GCM Network manager
     * Automatically detects if this has been done before
     */
    private void setScheduledTasks() {
        UsefulBits.setScheduledTasks(this);
    }

    private void showSnackbar(String message) {
        CoordinatorLayout layout = getFragmentCoordinatorLayout();
        if (layout != null)
            UsefulBits.showSnackbar(this, layout, message, Snackbar.LENGTH_SHORT);
        else
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @DebugLog
    public CoordinatorLayout getFragmentCoordinatorLayout() {
        CoordinatorLayout layout = null;
        try {
            Fragment f = latestFragment;
            if (f != null) {
                View v = f.getView();
                if (v != null)
                    layout = (CoordinatorLayout) v.findViewById(R.id.coordinatorLayout);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Unable to get the coordinator layout of visible fragment");
            ex.printStackTrace();
        }
        return layout;
    }

    private void stopCameraTimer() {
        if (cameraRefreshTimer != null) {
            cameraRefreshTimer.cancel();
            cameraRefreshTimer.purge();
            cameraRefreshTimer = null;
        }
    }

    @Override
    @DebugLog
    public void onResume() {
        super.onResume();
        setScreenAlwaysOn();
        if (listeningSpeechRecognition) {
            startRecognition();
        }
    }

    @Override
    @DebugLog
    public void onDestroy() {
        if (oTalkBackUtil != null) {
            oTalkBackUtil.Stop();
            oTalkBackUtil = null;
        }

        stopCameraTimer();
        Digitus.deinit();
        super.onDestroy();
    }

    @Override
    @DebugLog
    public void onPause() {
        if (listeningSpeechRecognition) {
            stopRecognition();
        }
        if (oTalkBackUtil != null) {
            oTalkBackUtil.Stop();
            oTalkBackUtil = null;
        }
        super.onPause();
    }

    @Override
    @DebugLog
    public void onBackPressed() {
        if (listeningSpeechRecognition) {
            stopRecognition();

            if (fromVoiceWidget)
                this.finish();
        } else {
            if (fromQRCodeWidget)
                this.finish();

            //handle the back press :D close the drawer first and if the drawer is closed close the activity
            if (drawer != null && drawer.isDrawerOpen()) {
                drawer.closeDrawer();
            } else {
                if (stackFragments == null || stackFragments.size() <= 1) {
                    MainActivity.super.onBackPressed();
                } else {
                    String currentFragment = stackFragments.get(stackFragments.size() - 1);
                    String previousFragment = stackFragments.get(stackFragments.size() - 2);
                    changeFragment(previousFragment);
                    stackFragments.remove(currentFragment);
                }

                stopCameraTimer();
                invalidateOptionsMenu();
            }
        }
    }

    /**
     * Opens the dialog
     *
     * @param dialogStandardFragment
     */
    private void openDialogFragment(DialogFragment dialogStandardFragment) {
        if (!isFinishing()) {
            if (mSharedPrefs != null) {
                PackageInfo pInfo = null;
                try {
                    pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;
                    String preVersion = mSharedPrefs.getPreviousVersionNumber();
                    if (!version.equals(preVersion)) {
                        if (dialogStandardFragment != null) {
                            getSupportFragmentManager().beginTransaction().add(dialogStandardFragment, "changelog_dialog").commitAllowingStateLoss();
                        }
                        mSharedPrefs.setVersionNumber(version);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDigitusReady(Digitus digitus) {
        if (validateOnce)
            FingerprintDialog.show(this, getString(R.string.app_name), 69, true);
    }

    @Override
    public void onDigitusListening(boolean newFingerprint) {
    }

    @Override
    public void onDigitusAuthenticated(Digitus digitus) {
        digitus.deinit();
        validateOnce = false;
        if (!mSharedPrefs.isGeofencingStarted()) {
            mSharedPrefs.setGeofencingStarted(true);
            new GeoUtils(this, this).AddGeofences();
        }
        buildScreen();
    }

    @Override
    public void onDigitusError(Digitus digitus, DigitusErrorType type, Exception e) {
        this.finish();
    }

    @Override
    public void onFingerprintDialogAuthenticated() {
        FingerprintDialog dialog = FingerprintDialog.getVisible(this);
        if (dialog != null)
            dialog.dismiss();
        Digitus.get().deinit();
        validateOnce = false;

        if (!mSharedPrefs.isGeofencingStarted()) {
            mSharedPrefs.setGeofencingStarted(true);
            new GeoUtils(this, this).AddGeofences();
        }

        buildScreen();
    }

    @Override
    public void onFingerprintDialogVerifyPassword(FingerprintDialog dialog, String password) {
        if (domoticz == null)
            domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        String pw = domoticz.getUserCredentials(Domoticz.Authentication.PASSWORD);
        if (pw.equals(password)) {
            if (dialog != null)
                dialog.dismiss();
            Digitus.get().deinit();
            validateOnce = false;

            if (!mSharedPrefs.isGeofencingStarted()) {
                mSharedPrefs.setGeofencingStarted(true);
                new GeoUtils(this, this).AddGeofences();
            }

            buildScreen();
        } else {
            UsefulBits.showSimpleToast(this, this.getString(R.string.security_wrong_password_fingerprint), Toast.LENGTH_LONG);
            if (dialog != null)
                dialog.dismiss();

            FingerprintDialog.show(this, getString(R.string.app_name), 69, true);
        }
    }

    @Override
    public void onFingerprintDialogStageUpdated(FingerprintDialog dialog, FingerprintDialog.Stage stage) {
    }

    @Override
    public void onFingerprintDialogCancelled() {
        this.finish();
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
        StringBuilder builder = new StringBuilder(permissionName.length);
        if (permissionName.length > 0) {
            for (String permission : permissionName) {
                builder.append(permission).append("\n");
            }
        }

        if (builder.toString().contains("android.permission.READ_PHONE_STATE")) {
            if (PermissionsUtil.canAccessDeviceState(this)){
                setupMobileDevice();
            }
        }

        if (builder.toString().contains("android.permission.CAMERA")) {
            if (PermissionsUtil.canAccessStorage(this)) {
                Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                startActivityForResult(iQRCodeScannerActivity, iQRResultCode);
            }
        }

        if (builder.toString().contains("android.permission.RECORD_AUDIO")) {
            if (PermissionsUtil.canAccessAudioState(this)) {
                startRecognition();
            }
        }
        super.onPermissionGranted(permissionName);
    }

    @Shortcut(id = "open_dashboard", icon = R.drawable.generic, shortLabelRes = R.string.title_dashboard, rank = 5, activity = MainActivity.class)
    public void OpenDashBoard() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.Fragments.Dashboard");
    }

    @Shortcut(id = "open_switches", icon = R.drawable.dimmer, shortLabelRes = R.string.title_switches, rank = 4, activity = MainActivity.class)
    public void OpenSwitch() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.Fragments.Switches");
    }

    @Shortcut(id = "open_utilities", icon = R.drawable.harddisk, shortLabelRes = R.string.title_utilities, rank = 3, activity = MainActivity.class)
    public void OpenUtilities() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.Fragments.Utilities");
    }

    @Shortcut(id = "open_temperature", icon = R.drawable.temperature, shortLabelRes = R.string.title_temperature, rank = 2, activity = MainActivity.class)
    public void OpenTemperature() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.Fragments.Temperature");
    }
}
