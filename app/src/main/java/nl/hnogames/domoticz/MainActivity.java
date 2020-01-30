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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.ftinc.scoop.Scoop;
import com.github.zagum.speechrecognitionview.RecognitionProgressView;
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import hotchemi.android.rate.AppRate;
import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.app.DomoticzDashboardFragment;
import nl.hnogames.domoticz.app.DomoticzRecyclerFragment;
import nl.hnogames.domoticz.app.RefreshFragment;
import nl.hnogames.domoticz.containers.QRCodeInfo;
import nl.hnogames.domoticz.containers.SpeechInfo;
import nl.hnogames.domoticz.fragments.Cameras;
import nl.hnogames.domoticz.fragments.Logs;
import nl.hnogames.domoticz.ui.PasswordDialog;
import nl.hnogames.domoticz.ui.SortDialog;
import nl.hnogames.domoticz.utils.GCMUtils;
import nl.hnogames.domoticz.utils.GeoUtils;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SerializableManager;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.TalkBackUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticz.utils.WidgetUtils;
import nl.hnogames.domoticz.welcome.WelcomeViewActivity;
import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Containers.ServerInfo;
import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;
import nl.hnogames.domoticzapi.Utils.ServerUtil;
import shortbread.Shortcut;

@DebugLog
public class MainActivity extends AppCompatPermissionsActivity {
    private static TalkBackUtil oTalkBackUtil;
    private final int iQRResultCode = 775;
    private final int iWelcomeResultCode = 885;
    private final int iSettingsResultCode = 995;
    public boolean onPhone;
    public Exception configException;
    private SharedPrefUtil mSharedPrefs;
    private String TAG = MainActivity.class.getSimpleName();
    private ServerUtil mServerUtil;
    private SearchView searchViewAction;
    private Toolbar toolbar;
    private ArrayList<String> stackFragments = new ArrayList<>();
    private Domoticz domoticz;
    private Timer cameraRefreshTimer = null;
    private Timer autoRefreshTimer = null;
    private Fragment latestFragment = null;
    private Drawer drawer;
    private SpeechRecognizer speechRecognizer;
    private RecognitionProgressView recognitionProgressView;
    private RecognitionListenerAdapter recognitionListener;
    private boolean listeningSpeechRecognition = false;
    private boolean fromVoiceWidget = false;
    private boolean fromQRCodeWidget = false;
    private PermissionHelper permissionHelper;
    private boolean fromShortcut = false;
    private ConfigInfo mConfigInfo;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @DebugLog
    public ServerUtil getServerUtil() {
        if (mServerUtil == null)
            mServerUtil = new ServerUtil(this);
        return mServerUtil;
    }

    @DebugLog
    public ConfigInfo getConfig() {
        return mConfigInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configException = null;
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InitBiometric();
        mSharedPrefs = new SharedPrefUtil(this);

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);
        permissionHelper = PermissionHelper.getInstance(this);

        UsefulBits.checkAPK(this, mSharedPrefs);
        if (BuildConfig.LITE_VERSION || !mSharedPrefs.isAPKValidated()) {
            setContentView(R.layout.activity_newmain_free);
            MobileAds.initialize(this, this.getString(R.string.ADMOB_APP_KEY));
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("A18F9718FC3511DC6BCB1DC5AF076AE4").build();
            ((AdView) findViewById(R.id.adView)).loadAd(adRequest);
        } else {
            setContentView(R.layout.activity_newmain_paid);
            (findViewById(R.id.adView)).setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                fromVoiceWidget = extras.getBoolean("VOICE", false);
                fromQRCodeWidget = extras.getBoolean("QRCODE", false);
            }
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!UsefulBits.checkPlayServicesAvailable(this)) 
			this.finish();

        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            new GeoUtils(this, this).AddGeofences();
            initScreen();
        }
    }

    private void InitBiometric() {
        biometricPrompt = new BiometricPrompt(this, Executors.newSingleThreadExecutor(), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == 13) {
                    runOnUiThread(new Runnable() {
                        @Override
                        @DebugLog
                        public void run() {
                            FallbackSecurity();
                        }
                    });
                } else MainActivity.this.finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.app_name_domoticz))
                .setSubtitle(getString(R.string.fingerprint_make_sure))
                .setDescription(getString(R.string.fingerprint_dialog_description))
                .setNegativeButtonText(getString(R.string.security_password_fallback))
                .build();
    }

    private void FallbackSecurity() {
        PasswordDialog passwordDialog = new PasswordDialog(MainActivity.this, null);
        passwordDialog.show();
        passwordDialog.onDismissListener(new PasswordDialog.DismissListener() {
            @Override
            @DebugLog
            public void onDismiss(String password) {
                if (UsefulBits.isEmpty(password)) {
                    UsefulBits.showSnackbar(MainActivity.this, getFragmentCoordinatorLayout(), R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                    Talk(R.string.security_wrong_code);
                } else {
                    if (password.equals(domoticz.getUserCredentials(Domoticz.Authentication.PASSWORD)))
                        return;
                    else {
                        UsefulBits.showSnackbar(MainActivity.this, getFragmentCoordinatorLayout(), R.string.security_wrong_code, Snackbar.LENGTH_SHORT);
                        Talk(R.string.security_wrong_code);
                    }
                }
                FallbackSecurity();
            }

            @Override
            public void onCancel() {
                biometricPrompt.authenticate(promptInfo);
            }
        });
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
    public void initScreen() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            applyLanguage();
            TextView usingTabletLayout = findViewById(R.id.tabletLayout);
            if (usingTabletLayout == null)
                onPhone = true;

            appRate();
            initTalkBack();
            ShowLoading();

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

                //mConfigInfo = mServerUtil.getActiveServer().getConfigInfo(this);
                if (!fromVoiceWidget && !fromQRCodeWidget) {
                    UsefulBits.getServerConfigForActiveServer(MainActivity.this, new ConfigReceiver() {
                        @Override
                        @DebugLog
                        public void onReceiveConfig(ConfigInfo settings) {
                            if (MainActivity.this.mConfigInfo == null || settings == null || !MainActivity.this.mConfigInfo.toString().equals(settings.toString())) {
                                MainActivity.this.mConfigInfo = settings;
                                SerializableManager.saveSerializable(MainActivity.this, settings, "ConfigInfo");

                                setupMobileDevice();
                                setScheduledTasks();

                                WidgetUtils.RefreshWidgets(MainActivity.this);
                                buildscreen();
                            }
                        }

                        @Override
                        @DebugLog
                        public void onError(Exception error) {
                            configException = error;
                            if (!fromShortcut)
                                addFragment(true);
                        }
                    }, mConfigInfo);
                }
                if (mSharedPrefs.isStartupSecurityEnabled()) {
                    biometricPrompt.authenticate(promptInfo);
                }
            }
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public void ShowLoading() {
        changeFragment("nl.hnogames.domoticz.fragments.Loading", false);
    }

    public void buildscreen() {
        drawNavigationMenu(mConfigInfo);
        if (!fromShortcut)
            addFragment(false);
    }

    /* Called when the second activity's finishes */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (res != null && !res.getBoolean("RESULT", false))
                        this.finish();
                    else
                        initScreen();
                    SerializableManager.cleanAllSerializableObjects(this);
                    break;
                case iSettingsResultCode:
                    this.recreate();
                    mServerUtil = new ServerUtil(this);
                    SerializableManager.cleanAllSerializableObjects(this);
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
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void hideViews() {
        toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        toolbar.setVisibility(View.GONE);
        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    public void showViews() {
        toolbar.setVisibility(View.VISIBLE);
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
                            if (!mDevicesInfo.getStatusBoolean()) {
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = 0;
                                }
                            } else {
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
                            } else {
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = 0;
                                }
                            }
                        }
                    } else {
                        if (mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDS ||
                                mDevicesInfo.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.BLINDPERCENTAGE) {
                            if (inputJSONAction == 1) {
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = 0;
                                }
                            } else {
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
                            } else {
                                jsonAction = DomoticzValues.Device.Switch.Action.OFF;
                                if (!UsefulBits.isEmpty(value)) {
                                    jsonAction = DomoticzValues.Device.Dimmer.Action.DIM_LEVEL;
                                    jsonValue = 0;
                                }
                            }
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
        else if (f instanceof RefreshFragment)
            ((RefreshFragment) f).RefreshFragment();
    }

    @DebugLog
    public void removeFragmentStack(String fragment) {
        if (stackFragments != null) {
            stackFragments.remove(fragment);
        }
    }

    @DebugLog
    public void clearFragmentStack() {
        if (stackFragments != null) {
            stackFragments.clear();
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
    public void changeFragment(String fragment, boolean keepInStack) {
        try {
            FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
            latestFragment = Fragment.instantiate(MainActivity.this, fragment);
            tx.replace(R.id.main, latestFragment);
            tx.commitAllowingStateLoss();
            if (keepInStack)
                addFragmentStack(fragment);
            saveScreenToAnalytics(fragment);
            invalidateOptionsMenu();
        } catch (Exception e) {
            Log.e("Fragment", e.getMessage());
        }
    }

    private void addFragment(boolean exception) {
        if (!exception) {
            if (!isFinishing()) {
                try {
                    changeFragment(getResources().getStringArray(R.array.drawer_fragments)[mSharedPrefs.getStartupScreenIndex()], true);
                } catch (Exception ignored) {
                    //get default screen (dashboard)
                    changeFragment(getResources().getStringArray(R.array.drawer_fragments)[1], true);
                }
            }
        } else {
            changeFragment("nl.hnogames.domoticz.fragments.Error", false);
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

    private void setupAutoRefresh() {
        if (mSharedPrefs.getAutoRefresh() && autoRefreshTimer == null) {
            autoRefreshTimer = new Timer("autorefresh", true);
            autoRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                @DebugLog
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        @DebugLog
                        public void run() {
                            refreshFragment();
                        }
                    });
                }
            }, 0, (mSharedPrefs.getAutoRefreshTimer() * 1000));
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
            if (!PermissionsUtil.canAccessDeviceState(this))
                permissionHelper.request(PermissionsUtil.INITIAL_DEVICE_PERMS);
            else
                GetFirebaseToken();
        } else {
            GetFirebaseToken();
        }
    }

    private void GetFirebaseToken() {
        try {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "getInstanceId failed", e);
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            try {
                                if (!task.isSuccessful() && task.getResult() == null) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                String refreshedToken = task.getResult().getToken();
                                Log.d("Firebase id login", "Refreshed token: " + refreshedToken);
                                GCMUtils.sendRegistrationIdToBackend(MainActivity.this, refreshedToken);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
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
        List<String> allUsers = new ArrayList<>();

        if (config == null)
            config = mServerUtil.getActiveServer().getConfigInfo(this);

        ProfileDrawerItem loggedinAccount = new ProfileDrawerItem()
                .withName("Logged in")
                .withEmail(domoticz.getUserCredentials(Domoticz.Authentication.USERNAME))
                .withIcon(R.mipmap.ic_launcher);
        allUsers.add(domoticz.getUserCredentials(Domoticz.Authentication.USERNAME));

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.graphTextColor, typedValue, true);

        // Create the AccountHeader
        final ConfigInfo finalConfig = config;
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(loggedinAccount)
                .withTextColor(typedValue.data)
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
                                            .setActionTextColor(ContextCompat.getColor(MainActivity.this, R.color.primary))
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
                                                if (user.getUsername().equals(profile.getEmail().getText())) {
                                                    String md5Pass = UsefulBits.getMd5String(password);
                                                    if (md5Pass.equals(user.getPassword())) {
                                                        domoticz.LogOff();
                                                        domoticz.setUserCredentials(user.getUsername(), password);
                                                        initScreen();
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
                if (!allUsers.contains(user.getUsername())) {
                    ProfileDrawerItem profile = new ProfileDrawerItem().withName(user.getRightsValue(this)
                    ).withEmail(user.getUsername())
                            .withIcon(R.drawable.users)
                            .withEnabled(user.isEnabled());
                    allUsers.add(user.getUsername());
                    headerResult.addProfiles(profile);
                }
            }
        }

        drawer = new DrawerBuilder()
                .withActivity(this)
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
                                OpenSettings();
                            } else if (drawerItem.getTag() != null) {
                                changeFragment(String.valueOf(drawerItem.getTag()), true);
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
        drawer.addStickyFooterItem(createSecondaryDrawerItem(this.getString(R.string.action_settings), "gmd_settings", "Settings"));
    }

    public void OpenSettings() {
        stopCameraTimer();
        startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), iSettingsResultCode);
    }

    private List<IDrawerItem> getDrawerItems() {
        List<IDrawerItem> drawerItems = new ArrayList<>();
        String[] drawerActions = mSharedPrefs.getNavigationActions();
        String[] fragments = mSharedPrefs.getNavigationFragments();
        String[] ICONS = mSharedPrefs.getNavigationIcons();

        UserInfo user = null;
        try {
            if (mConfigInfo != null && mConfigInfo.getUsers() != null) {
                for (UserInfo u : mConfigInfo.getUsers()) {
                    if (u.getUsername().equals(domoticz.getUserCredentials(Domoticz.Authentication.USERNAME)))
                        user = u;
                }
            }
        } catch (Exception ignored) {
        }

        for (int i = 0; i < drawerActions.length; i++)
            if (fragments[i].contains("fragments.Wizard"))
                drawerItems.add(createPrimaryDrawerItem(drawerActions[i], ICONS[i], fragments[i]));
        if (drawerItems.size() > 0)
            drawerItems.add(new DividerDrawerItem());

        for (int i = 0; i < drawerActions.length; i++)
            if (fragments[i].contains("fragments.Dashboard") ||
                    (fragments[i].contains("fragments.Switch") && (mConfigInfo != null && mConfigInfo.isEnableTabLights())) ||
                    (fragments[i].contains("fragments.Scene") && (mConfigInfo != null && mConfigInfo.isEnableTabScenes())))
                drawerItems.add(createPrimaryDrawerItem(drawerActions[i], ICONS[i], fragments[i]));
        drawerItems.add(new DividerDrawerItem());

        for (int i = 0; i < drawerActions.length; i++)
            if ((fragments[i].contains("fragments.Temperature") && (mConfigInfo != null && mConfigInfo.isEnableTabTemp())) ||
                    (fragments[i].contains("fragments.Weather") && (mConfigInfo != null && mConfigInfo.isEnableTabWeather())))
                drawerItems.add(createPrimaryDrawerItem(drawerActions[i], ICONS[i], fragments[i]));
        drawerItems.add(new DividerDrawerItem());

        for (int i = 0; i < drawerActions.length; i++) {
            if ((fragments[i].contains("fragments.Plans")) ||
                    (fragments[i].contains("fragments.Utilities") && (mConfigInfo != null && mConfigInfo.isEnableTabUtility())))
                drawerItems.add(createPrimaryDrawerItem(drawerActions[i], ICONS[i], fragments[i]));
        }

        try {
            if (user != null && user.getRights() >= 2) {
                for (int i = 0; i < drawerActions.length; i++) {
                    if (fragments[i].contains("fragments.Camera"))
                        drawerItems.add(createPrimaryDrawerItem(drawerActions[i], ICONS[i], fragments[i]));
                }
                drawerItems.add(new DividerDrawerItem());
                for (int i = 0; i < drawerActions.length; i++)
                    if (fragments[i].contains("fragments.Logs") || fragments[i].contains("fragments.Events") || fragments[i].contains("fragments.UserVariables"))
                        drawerItems.add(createSecondaryDrawerItem(drawerActions[i], ICONS[i], fragments[i]));
            }
        } catch (Exception ex) {
        }
        return drawerItems;
    }

    private SecondaryDrawerItem createSecondaryDrawerItem(String title, String icon, String fragmentID) {
        SecondaryDrawerItem item = new SecondaryDrawerItem();
        item.withName(title)
                .withIcon(GoogleMaterial.Icon.valueOf(icon))
                .withTag(fragmentID);
        return item;
    }

    private PrimaryDrawerItem createPrimaryDrawerItem(String title, String icon, String fragmentID) {
        PrimaryDrawerItem item = new PrimaryDrawerItem();
        item.withName(title)
                .withIcon(GoogleMaterial.Icon.valueOf(icon))
                .withTag(fragmentID);
        return item;
    }

    @Override
    @DebugLog
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment f = latestFragment;

        MenuItem speechMenuItem;
        if (!fromVoiceWidget && !fromQRCodeWidget) {
            if ((f instanceof nl.hnogames.domoticz.fragments.Error)) {
                getMenuInflater().inflate(R.menu.menu_error, menu);
            } else if ((f instanceof Cameras)) {
                if (cameraRefreshTimer != null)
                    getMenuInflater().inflate(R.menu.menu_camera_pause, menu);
                else
                    getMenuInflater().inflate(R.menu.menu_camera, menu);
            } else if ((f instanceof DomoticzDashboardFragment) || (f instanceof DomoticzRecyclerFragment) || (f instanceof RefreshFragment)) {
                getMenuInflater().inflate(R.menu.menu_main, menu);

                MenuItem searchMenuItem = menu.findItem(R.id.search);
                searchViewAction = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
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
                        } else if (n instanceof RefreshFragment) {
                            ((RefreshFragment) n).Filter(newText);
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
            } else {
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
                        recognitionProgressView = findViewById(R.id.recognition_view);
                    if (recognitionListener == null) {
                        recognitionListener = new RecognitionListenerAdapter() {
                            @Override
                            public void onResults(Bundle results) {
                                showSpeechResults(results);
                                stopRecognition();
                            }
                        };
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
                        }, 0, 10000);//schedule in 10 seconds
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
                    SortDialog infoDialog = (latestFragment instanceof Logs) ?
                            new SortDialog(
                                    this,
                                    R.layout.dialog_switch_logs,
                                    new String[]{getString(R.string.filter_all), getString(R.string.filter_normal), getString(R.string.filter_status), getString(R.string.filter_error)}) :
                            new SortDialog(
                                    this,
                                    R.layout.dialog_switch_logs, null);
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
                            } else if (f instanceof RefreshFragment) {
                                ((RefreshFragment) f).sortFragment(selectedSort);
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
        (findViewById(R.id.main)).setVisibility(View.GONE);
        recognitionProgressView.setVisibility(View.VISIBLE);
        recognitionProgressView.play();
    }

    private void stopRecognitionAnimation() {
        (findViewById(R.id.main)).setVisibility(View.VISIBLE);
        recognitionProgressView.setVisibility(View.GONE);
        recognitionProgressView.stop();
    }

    @DebugLog
    private void showSpeechResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null)
            return;
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
                        try {
                            for (ServerInfo s : mServerUtil.getEnabledServerList()) {
                                if (s.getServerName() != null && s.getServerName().contentEquals(text)) {
                                    String message = String.format(getString(R.string.switch_to_server), s.getServerName());
                                    showSnackbar(message);
                                    mServerUtil.setActiveServer(s);
                                    domoticz.getSessionUtil().clearSessionCookie();
                                    MainActivity.this.recreate();
                                }
                            }
                            return false;
                        } catch (Exception ex) {
                            return false;
                        }
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
                    layout = v.findViewById(R.id.coordinatorLayout);
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

    private void stopAutoRefreshTimer() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.cancel();
            autoRefreshTimer.purge();
            autoRefreshTimer = null;
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
        setupAutoRefresh();
    }

    @Override
    @DebugLog
    public void onDestroy() {
        if (oTalkBackUtil != null) {
            oTalkBackUtil.Stop();
            oTalkBackUtil = null;
        }

        stopCameraTimer();
        stopAutoRefreshTimer();
        super.onDestroy();
    }

    @DebugLog
    public void clearSearch() {
        if (searchViewAction != null) {
            searchViewAction.setQuery("", false);
            searchViewAction.clearFocus();
            searchViewAction.onActionViewCollapsed();
        }
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

        stopAutoRefreshTimer();
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
                    changeFragment(previousFragment, true);
                    stackFragments.remove(currentFragment);
                }

                stopCameraTimer();
                invalidateOptionsMenu();
            }
            showViews();
        }
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
            if (PermissionsUtil.canAccessDeviceState(this)) {
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
        changeFragment("nl.hnogames.domoticz.fragments.Dashboard", false);
    }

    @Shortcut(id = "open_switches", icon = R.drawable.dimmer, shortLabelRes = R.string.title_switches, rank = 4, activity = MainActivity.class)
    public void OpenSwitch() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.fragments.Switches", false);
    }

    @Shortcut(id = "open_utilities", icon = R.drawable.harddisk, shortLabelRes = R.string.title_utilities, rank = 3, activity = MainActivity.class)
    public void OpenUtilities() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.fragments.Utilities", false);
    }

    @Shortcut(id = "open_temperature", icon = R.drawable.temperature, shortLabelRes = R.string.title_temperature, rank = 2, activity = MainActivity.class)
    public void OpenTemperature() {
        fromShortcut = true;
        changeFragment("nl.hnogames.domoticz.fragments.Temperature", false);
    }
}
