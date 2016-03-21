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

package nl.hnogames.domoticz;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import hotchemi.android.rate.AppRate;
import nl.hnogames.domoticz.Adapters.NavigationAdapter;
import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Containers.QRCodeInfo;
import nl.hnogames.domoticz.Containers.ServerInfo;
import nl.hnogames.domoticz.Containers.ServerUpdateInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Fragments.Cameras;
import nl.hnogames.domoticz.Fragments.Dashboard;
import nl.hnogames.domoticz.Fragments.Scenes;
import nl.hnogames.domoticz.Fragments.Switches;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Interfaces.UpdateVersionReceiver;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;
import nl.hnogames.domoticz.UI.SortDialog;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.ServerUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.Utils.WidgetUtils;
import nl.hnogames.domoticz.Welcome.WelcomeViewActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.app.DomoticzCardFragment;
import nl.hnogames.domoticz.app.DomoticzDashboardFragment;
import nl.hnogames.domoticz.app.DomoticzFragment;

public class MainActivity extends AppCompatActivity {

    private final int iQRResultCode = 775;
    private final int iWelcomeResultCode = 885;
    private final int iSettingsResultCode = 995;

    private String TAG = MainActivity.class.getSimpleName();
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawer;
    private String[] fragments;
    private SharedPrefUtil mSharedPrefs;
    private ServerUtil mServerUtil;
    private NavigationAdapter mAdapter;
    private SearchView searchViewAction;

    private ArrayList<String> stackFragments = new ArrayList<>();
    private Domoticz domoticz;
    private boolean onPhone;
    private Timer cameraRefreshTimer = null;

    public ServerUtil geServerUtil() {
        if (mServerUtil == null)
            mServerUtil = new ServerUtil(this);
        return mServerUtil;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean resolvableError = UsefulBits.checkPlayServicesAvailable(this);
        if (!resolvableError) this.finish();

        if (mSharedPrefs.isFirstStart()) {
            mSharedPrefs.setNavigationDefaults();
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            mSharedPrefs.setFirstStart(false);
        } else {
            // Only start Geofences when not started
            // Geofences are already started on device boot up by the BootUpReceiver
            if (!mSharedPrefs.isGeofencingStarted()) {
                mSharedPrefs.setGeofencingStarted(true);
                mSharedPrefs.enableGeoFenceService();
            }
            buildScreen();
        }
    }

    public void buildScreen() {
        if (mSharedPrefs.isWelcomeWizardSuccess()) {
            applyLanguage();

            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            mServerUtil = new ServerUtil(this);
            domoticz = new Domoticz(this, mServerUtil);
            drawNavigationMenu();

            setupMobileDevice();
            checkDomoticzServerUpdate();
            setScheduledTasks();
            checkDownloadedLanguage();
            saveServerConfigToActiveServer();

            appRate();
            WidgetUtils.RefreshWidgets(this);

            AppController.getInstance().resendRegistrationIdToBackend();
        } else {
            Intent welcomeWizard = new Intent(this, WelcomeViewActivity.class);
            startActivityForResult(welcomeWizard, iWelcomeResultCode);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    public void drawNavigationMenu() {
        TextView usingTabletLayout = (TextView) findViewById(R.id.tabletLayout);
        if (usingTabletLayout == null)
            onPhone = true;
        else {
            if (mSharedPrefs.darkThemeEnabled()) {
                ((LinearLayout) findViewById(R.id.tabletLayoutWrapper)).setBackgroundColor(getResources().getColor(R.color.background_dark));
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        addDrawerItems();
        addFragment();
    }

    private void setScreenAlwaysOn() {
        if (mSharedPrefs.getAlwaysOn())
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void applyLanguage() {
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage())) {
            // User has set a language in settings
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        }
    }

    private void checkDownloadedLanguage() {
        UsefulBits.checkDownloadedLanguage(this, mServerUtil, false, false);
    }

    private void saveServerConfigToActiveServer() {
        UsefulBits.saveServerConfigToActiveServer(this, false);
    }

    /* Called when the second activity's finishes */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case iWelcomeResultCode:
                    Bundle res = data.getExtras();
                    if (!res.getBoolean("RESULT", false))
                        this.finish();
                    else {
                        if (mSharedPrefs.darkThemeEnabled())
                            setTheme(R.style.AppThemeDark);

                        buildScreen();
                    }
                    break;
                case iSettingsResultCode:
                    mServerUtil = new ServerUtil(this);
                    if (mSharedPrefs.darkThemeEnabled())
                        setTheme(R.style.AppThemeDark);

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
                            handleSwitch(foundQRCode.getSwitchIdx(), foundQRCode.getSwitchPassword());
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
            startActivityForResult(new Intent(this, SettingsActivity.class), this.iSettingsResultCode);
        }
    }

    private void handleSwitch(final int idx, final String password) {
        domoticz = new Domoticz(this, null);
        domoticz.getSwitches(new SwitchesReceiver() {
                                 @Override
                                 public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                                     for (SwitchInfo s : switches) {
                                         if (s.getIdx() == idx) {
                                             domoticz.getStatus(idx, new StatusReceiver() {
                                                 @Override
                                                 public void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo) {
                                                     int jsonAction;
                                                     int jsonUrl = Domoticz.Json.Url.Set.SWITCHES;
                                                     if (extendedStatusInfo.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDS ||
                                                             extendedStatusInfo.getSwitchTypeVal() == Domoticz.Device.Type.Value.BLINDPERCENTAGE) {
                                                         if (!extendedStatusInfo.getStatusBoolean())
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                         else
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                     } else {
                                                         if (!extendedStatusInfo.getStatusBoolean())
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                         else
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                     }

                                                     switch (extendedStatusInfo.getSwitchTypeVal()) {
                                                         case Domoticz.Device.Type.Value.PUSH_ON_BUTTON:
                                                             jsonAction = Domoticz.Device.Switch.Action.ON;
                                                             break;
                                                         case Domoticz.Device.Type.Value.PUSH_OFF_BUTTON:
                                                             jsonAction = Domoticz.Device.Switch.Action.OFF;
                                                             break;
                                                     }

                                                     domoticz.setAction(idx, jsonUrl, jsonAction, 0, password, new setCommandReceiver() {
                                                         @Override
                                                         public void onReceiveResult(String result) {
                                                             Log.d(TAG, result);
                                                         }

                                                         @Override
                                                         public void onError(Exception error) {
                                                         }
                                                     });
                                                 }

                                                 @Override
                                                 public void onError(Exception error) {
                                                 }
                                             });
                                         }
                                     }
                                 }

                                 @Override
                                 public void onError(Exception error) {
                                 }
                             }
        );
    }

    public void refreshFragment() {
        Fragment f = getVisibleFragment();
        if (f instanceof DomoticzFragment) {
            ((DomoticzFragment) f).refreshFragment();
        } else if (f instanceof DomoticzCardFragment)
            ((DomoticzCardFragment) f).refreshFragment();
        else if (f instanceof DomoticzDashboardFragment)
            ((DomoticzDashboardFragment) f).refreshFragment();
    }

    public void removeFragmentStack(String fragment) {
        if (stackFragments != null) {
            if (stackFragments.contains(fragment))
                stackFragments.remove(fragment);
        }
    }

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

    public void changeFragment(String fragment) {
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        // tx.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        tx.replace(R.id.main, Fragment.instantiate(MainActivity.this, fragment));
        tx.commitAllowingStateLoss();
        addFragmentStack(fragment);
        saveScreenToAnalytics(fragment);
    }

    private void addFragment() {
        int screenIndex = mSharedPrefs.getStartupScreenIndex();
        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        //tx.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        tx.replace(R.id.main, Fragment.instantiate(MainActivity.this, getResources().getStringArray(R.array.drawer_fragments)[screenIndex]));
        tx.commitAllowingStateLoss();
        addFragmentStack(getResources().getStringArray(R.array.drawer_fragments)[screenIndex]);
        saveScreenToAnalytics(getResources().getStringArray(R.array.drawer_fragments)[screenIndex]);
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

    private void updateDrawerItems() {
        String[] drawerActions = mSharedPrefs.getNavigationActions();
        fragments = mSharedPrefs.getNavigationFragments();
        int ICONS[] = mSharedPrefs.getNavigationIcons();
        mAdapter.updateData(drawerActions, ICONS);
    }

    /**
     * Adds the items to the drawer and registers a click listener on the items
     */
    private void addDrawerItems() {
        String[] drawerActions = mSharedPrefs.getNavigationActions();
        fragments = mSharedPrefs.getNavigationFragments();
        int ICONS[] = mSharedPrefs.getNavigationIcons();

        String NAME = getString(R.string.app_name_domoticz);
        String WEBSITE = getString(R.string.domoticz_url);
        int PROFILE = R.drawable.ic_launcher;

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView);
        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new NavigationAdapter(drawerActions, ICONS, NAME, WEBSITE, PROFILE, this);
        mAdapter.onClickListener(new NavigationAdapter.ClickListener() {
            @Override
            public void onClick(View child, int position) {
                if (child != null) {
                    try {
                        searchViewAction.setQuery("", false);
                        searchViewAction.clearFocus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    try {
                        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                        tx.replace(R.id.main,
                                Fragment.instantiate(MainActivity.this,
                                        fragments[position - 1]));
                        tx.commitAllowingStateLoss();
                        addFragmentStack(fragments[position - 1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    invalidateOptionsMenu();
                    if (onPhone)
                        mDrawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        setupDrawer();
    }

    /**
     * Sets the drawer with listeners for open and closed
     */
    private void setupDrawer() {
        if (onPhone) {
            mDrawerToggle = new ActionBarDrawerToggle(
                    this, mDrawer, R.string.drawer_open, R.string.drawer_close) {
                /**
                 * Called when a mDrawer has settled in a completely open state.
                 */
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);

                    try {
                        if (searchViewAction != null)
                            searchViewAction.clearFocus();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //getSupportActionBar().setTitle(R.string.drawer_navigation_title);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                /**
                 * Called when a mDrawer has settled in a completely closed state.
                 */
                public void onDrawerClosed(View view) {
                    super.onDrawerClosed(view);
                    //getSupportActionBar().setTitle(currentTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };

            mDrawerToggle.setDrawerIndicatorEnabled(true); // hamburger menu icon
            mDrawer.setDrawerListener(mDrawerToggle); // attach hamburger menu icon to drawer
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public Fragment getVisibleFragment() {
        try {
            FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
            List<Fragment> fragments = fragmentManager.getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null && fragment.isVisible())
                    return fragment;
            }

            return null;
        } catch (Exception ex) {
            return null;
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

    private void setupMobileDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionsUtil.canAccessDeviceState(this)) {
                requestPermissions(PermissionsUtil.INITIAL_DEVICE_PERMS, PermissionsUtil.INITIAL_DEVICE_REQUEST);
            } else {
                AppController.getInstance().StartEasyGCM();
            }
        } else {
            AppController.getInstance().StartEasyGCM();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_DEVICE_REQUEST:
                if (PermissionsUtil.canAccessDeviceState(this))
                    AppController.getInstance().StartEasyGCM();
                break;
            case PermissionsUtil.INITIAL_CAMERA_REQUEST:
                if (PermissionsUtil.canAccessStorage(this)) {
                    Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                    startActivityForResult(iQRCodeScannerActivity, iQRResultCode);
                }
                break;
        }
    }

    private void checkDomoticzServerUpdate() {
        if (mSharedPrefs.checkForUpdatesEnabled()) {
            // Get latest Domoticz version update
            domoticz.getUpdate(new UpdateVersionReceiver() {
                @Override
                public void onReceiveUpdate(ServerUpdateInfo serverUpdateInfo) {
                    boolean haveUpdate = serverUpdateInfo.isUpdateAvailable();

                    if (mServerUtil.getActiveServer() != null) {
                        //only show an update revision snackbar once per revisionnumber!
                        if (!mSharedPrefs.getLastUpdateShown().equals(serverUpdateInfo.getUpdateRevisionNumber())) {
                            // Write update version to shared preferences
                            mServerUtil.getActiveServer().setServerUpdateInfo(serverUpdateInfo);
                            mServerUtil.saveDomoticzServers(true);
                            if (haveUpdate) {
                                if (serverUpdateInfo.getSystemName().equalsIgnoreCase("linux")) {
                                    // Great! We can remote/auto update Linux systems
                                    getCurrentServerVersion();
                                } else {
                                    // No remote/auto updating available for other systems (like Windows, Synology)
                                    showSimpleSnackbar(getString(R.string.server_update_available));
                                }
                                mSharedPrefs.setLastUpdateShown(serverUpdateInfo.getUpdateRevisionNumber());
                            }
                        }
                    }
                }

                @Override
                public void onError(Exception error) {
                    String message = String.format(
                            getString(R.string.error_couldNotCheckForUpdates),
                            domoticz.getErrorMessage(error));
                    showSimpleSnackbar(message);

                    if (mServerUtil.getActiveServer().getServerUpdateInfo() != null)
                        mServerUtil.getActiveServer().getServerUpdateInfo().setCurrentServerVersion("");
                    mServerUtil.saveDomoticzServers(true);
                }
            });
        }
    }

    private void getCurrentServerVersion() {
        // Get current Domoticz server version
        domoticz.getServerVersion(new VersionReceiver() {
            @Override
            public void onReceiveVersion(String serverVersion) {
                if (!UsefulBits.isEmpty(serverVersion)) {

                    if (mServerUtil.getActiveServer() != null &&
                            mServerUtil.getActiveServer().getServerUpdateInfo() != null) {
                        mServerUtil.getActiveServer()
                                .getServerUpdateInfo()
                                .setCurrentServerVersion(serverVersion);
                    }

                    String[] version
                            = serverVersion.split("\\.");
                    // Update version is only revision number
                    String updateVersion = (mServerUtil.getActiveServer() != null &&
                            mServerUtil.getActiveServer().getServerUpdateInfo() != null) ?
                            version[0] + "."
                                    + mServerUtil.getActiveServer()
                                    .getServerUpdateInfo()
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
            public void onError(Exception error) {
                String message = String.format(
                        getString(R.string.error_couldNotCheckForUpdates),
                        domoticz.getErrorMessage(error));
                showSimpleSnackbar(message);
            }
        });
    }

    private void showSnackBarToUpdateServer(String message) {
        View layout = getFragmentCoordinatorLayout();
        if (layout != null) {
            Snackbar.make(layout, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.update_server, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(MainActivity.this, UpdateActivity.class));
                        }
                    })
                    .show();
        }
    }

    /**
     * Starts the scheduled tasks service via GCM Network manager
     * Automatically detects if this has been done before
     */
    private void setScheduledTasks() {
        UsefulBits.setScheduledTasks(this);
    }

    private void showSimpleSnackbar(String message) {
        View layout = getFragmentCoordinatorLayout();
        if (layout != null) Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
        else Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public View getFragmentCoordinatorLayout() {
        View layout = null;
        try {
            Fragment f = getVisibleFragment();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Fragment f = getVisibleFragment();
        if (!(f instanceof DomoticzFragment)) {
            if ((f instanceof Cameras)) {
                if (cameraRefreshTimer != null)
                    getMenuInflater().inflate(R.menu.menu_camera_pause, menu);
                else
                    getMenuInflater().inflate(R.menu.menu_camera, menu);
            } else
            if ((f instanceof DomoticzDashboardFragment)) {
                if ((f instanceof Dashboard) || (f instanceof Scenes) || (f instanceof Switches))
                    getMenuInflater().inflate(R.menu.menu_main_sort, menu);
                else
                    getMenuInflater().inflate(R.menu.menu_main, menu);

                MenuItem searchMenuItem = menu.findItem(R.id.search);
                searchViewAction = (SearchView) MenuItemCompat
                        .getActionView(searchMenuItem);
                searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        Fragment n = getVisibleFragment();
                        if (n instanceof DomoticzDashboardFragment ) {
                            ((DomoticzDashboardFragment) n).Filter(newText);
                        }
                        return false;
                    }
                });
            }
            else
                getMenuInflater().inflate(R.menu.menu_simple, menu);
        } else {
            if ((f instanceof Dashboard) || (f instanceof Scenes) || (f instanceof Switches))
                getMenuInflater().inflate(R.menu.menu_main_sort, menu);
            else
                getMenuInflater().inflate(R.menu.menu_main, menu);

            MenuItem searchMenuItem = menu.findItem(R.id.search);
            searchViewAction = (SearchView) MenuItemCompat
                    .getActionView(searchMenuItem);
            searchViewAction.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Fragment n = getVisibleFragment();
                    if (n instanceof DomoticzFragment) {
                        ((DomoticzFragment) n).Filter(newText);
                    }
                    return false;
                }
            });
        }

        if (mSharedPrefs.isMultiServerEnabled()) {
            //set multi server actionbar item
            MenuItem searchMenuItem = menu.findItem(R.id.action_switch_server);
            if (searchMenuItem != null && mServerUtil.getEnabledServerList() != null && mServerUtil.getEnabledServerList().size() > 1) {
                searchMenuItem.setVisible(true);
            } else if (searchMenuItem != null)
                searchMenuItem.setVisible(false);
        }

        if (mSharedPrefs.isQRCodeEnabled()) {
            MenuItem searchMenuItem = menu.findItem(R.id.action_scan_qrcode);
            if (searchMenuItem != null && mSharedPrefs.getQRCodeList() != null && mSharedPrefs.getQRCodeList().size() > 0) {
                searchMenuItem.setVisible(true);
            } else if (searchMenuItem != null)
                searchMenuItem.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case R.id.action_camera_play:
                    if (cameraRefreshTimer == null) {
                        cameraRefreshTimer = new Timer("camera", true);
                        cameraRefreshTimer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //call refresh fragment
                                        Fragment f = getVisibleFragment();
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
                            requestPermissions(PermissionsUtil.INITIAL_CAMERA_PERMS, PermissionsUtil.INITIAL_CAMERA_REQUEST);
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
                case R.id.action_settings:
                    stopCameraTimer();
                    startActivityForResult(new Intent(this, SettingsActivity.class), this.iSettingsResultCode);
                    return true;
                case R.id.action_sort:
                    SortDialog infoDialog = new SortDialog(
                            this,
                            R.layout.dialog_switch_logs);
                    infoDialog.onDismissListener(new SortDialog.DismissListener() {
                        @Override
                        public void onDismiss(String selectedSort) {
                            Log.i(TAG, "Sorting: " + selectedSort);
                            Fragment f = getVisibleFragment();
                            if (f instanceof DomoticzFragment) {
                                ((DomoticzFragment) f).sortFragment(selectedSort);
                            }
                            else  if (f instanceof DomoticzDashboardFragment) {
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

            // Activate the navigation drawer toggle
            if (mDrawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onOptionsItemSelected(item);
    }

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
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        ServerInfo setNew = null;
                        for (ServerInfo s : mServerUtil.getEnabledServerList()) {
                            if (s.getServerName().equals(text)) {
                                String message = String.format(
                                        getString(R.string.switch_to_server), s.getServerName());
                                showSimpleSnackbar(message);
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

    @Override
    public void onResume() {
        super.onResume();
        setScreenAlwaysOn();
        refreshFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCameraTimer();
    }

    private void stopCameraTimer() {
        if (cameraRefreshTimer != null) {
            cameraRefreshTimer.cancel();
            cameraRefreshTimer.purge();
            cameraRefreshTimer = null;
        }
    }

    @Override
    public void onBackPressed() {
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