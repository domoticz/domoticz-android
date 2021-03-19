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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.isupatches.wisefy.WiseFy;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import nl.hnogames.domoticz.adapters.WifiAdapter;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.containers.WifiInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.WifiClickListener;
import nl.hnogames.domoticz.ui.SwitchDialog;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;


public class WifiSettingsActivity extends AppCompatPermissionsActivity implements WifiClickListener {
    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<WifiInfo> WifiList;
    private WiseFy wisefy;
    private PermissionHelper permissionHelper;
    private WifiAdapter adapter;
    private Toolbar toolbar;
    private SwitchMaterial notificationSwitch;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        Scoop.getInstance().apply(this);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        permissionHelper = PermissionHelper.getInstance(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.category_wifi);

        WifiList = mSharedPrefs.getWifiList();
        adapter = new WifiAdapter(this, WifiList, this);
        wisefy = new WiseFy.Brains(this).getSmarts();

        notificationSwitch = findViewById(R.id.switch_notifications_button);
        notificationSwitch.setChecked(mSharedPrefs.isWifiNotificationsEnabled());
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mSharedPrefs.setWifiNotificationsEnabled(isChecked));

        createListView();

        try {
            if (!wisefy.isWifiEnabled()) {
                UsefulBits.showSnackbar(WifiSettingsActivity.this, coordinatorLayout, R.string.wifi_turned_off, Snackbar.LENGTH_SHORT);
                return;
            }
        } catch (Exception ignored) {
        }
    }

    private void showWifiList() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissionHelper.request(PermissionsUtil.INITIAL_LOCATION_PERMS);
        else {
            try {
                List<ScanResult> nearbyAccessPoints = wisefy.getNearbyAccessPoints(true);
                if (nearbyAccessPoints == null)
                    return;

                final List<String> wifiDevices = new ArrayList<>();
                for (ScanResult result : nearbyAccessPoints)
                    wifiDevices.add(result.SSID);

                CharSequence[] items = wifiDevices.toArray(new CharSequence[wifiDevices.size()]);
                new AlertDialog.Builder(this)
                        .setSingleChoiceItems(items, 0, null)
                        .setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                            dialog.dismiss();
                            int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            final String SSID = wifiDevices.get(position);
                            boolean newTagFound = true;
                            if (WifiList != null && WifiList.size() > 0) {
                                for (WifiInfo n : WifiList) {
                                    if (n.getSSID().equals(SSID))
                                        newTagFound = false;
                                }
                            }

                            if (newTagFound) {
                                UsefulBits.showSnackbar(WifiSettingsActivity.this, coordinatorLayout, getString(R.string.wifi_saved) + ": " + SSID, Snackbar.LENGTH_SHORT);
                                WifiInfo WifiInfo = new WifiInfo();
                                WifiInfo.setSSID(SSID);
                                WifiInfo.setName(String.valueOf(SSID));
                                updateWifi(WifiInfo);
                            } else {
                                UsefulBits.showSnackbar(WifiSettingsActivity.this, coordinatorLayout, R.string.wifi_exists, Snackbar.LENGTH_SHORT);
                            }
                        })
                        .show();
            } catch (Exception ignored) {
            }
        }
    }

    private void createListView() {
        ListView listView = findViewById(R.id.listView);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener((adapterView, view, item, id) -> getSwitchesAndShowSwitchesDialog(WifiList.get(item)));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            WifiInfo wifi = WifiList.get(position);
            if (wifi.getSwitchIdx() > 0) {
                wifi.setSwitchIdx(0);
                wifi.setSwitchName(null);
                wifi.setValue(null);
                wifi.setSwitchPassword(null);
                updateWifi(wifi);
                UsefulBits.showSnackbar(WifiSettingsActivity.this, coordinatorLayout, R.string.switch_connection_removed, Snackbar.LENGTH_LONG);
                adapter.notifyDataSetChanged();
            } else
                getSwitchesAndShowSwitchesDialog(wifi);
            return true;
        });
    }

    private void getSwitchesAndShowSwitchesDialog(final WifiInfo wifiInfo) {
        StaticHelper.getDomoticz(WifiSettingsActivity.this).getDevices(new DevicesReceiver() {
            @Override

            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                showSwitchesDialog(wifiInfo, switches);
            }

            @Override

            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override

            public void onError(Exception error) {
                UsefulBits.showSnackbarWithAction(WifiSettingsActivity.this, coordinatorLayout, WifiSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, v -> getSwitchesAndShowSwitchesDialog(wifiInfo), WifiSettingsActivity.this.getString(R.string.retry));
            }
        }, 0, "all");
    }

    private void showSwitchesDialog(
            final WifiInfo WifiInfo,
            final ArrayList<DevicesInfo> switches) {

        final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo d : switches) {
            if (DeviceUtils.isAutomatedToggableDevice(d))
                supportedSwitches.add(d);
        }

        SwitchDialog infoDialog = new SwitchDialog(
                WifiSettingsActivity.this, supportedSwitches,
                R.layout.dialog_switch_logs,
                StaticHelper.getDomoticz(WifiSettingsActivity.this));

        infoDialog.onDismissListener((selectedSwitchIDX, selectedSwitchPassword, selectedSwitchName, isSceneOrGroup) -> {
            WifiInfo.setSwitchIdx(selectedSwitchIDX);
            WifiInfo.setSwitchPassword(selectedSwitchPassword);
            WifiInfo.setSwitchName(selectedSwitchName);
            WifiInfo.setSceneOrGroup(isSceneOrGroup);

            if (!isSceneOrGroup) {
                for (DevicesInfo s : supportedSwitches) {
                    if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                        showSelectorDialog(WifiInfo, s);
                    else
                        updateWifi(WifiInfo);

                }
            } else {
                updateWifi(WifiInfo);
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final WifiInfo WifiInfo, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback((dialog, view, which, text) -> {
                    WifiInfo.setValue(String.valueOf(text));
                    updateWifi(WifiInfo);
                })
                .show();
    }

    public void updateWifi(WifiInfo WifiInfo) {
        if (WifiList == null)
            WifiList = new ArrayList<>();

        boolean found = false;
        int i = 0;
        for (WifiInfo l : WifiList) {
            if (l.getSSID().equals(WifiInfo.getSSID())) {
                WifiList.set(i, WifiInfo);
                found = true;
            }
            i++;
        }
        if (!found)//add new
            WifiList.add(WifiInfo);

        mSharedPrefs.saveWifiList(WifiList);
        adapter.data = WifiList;
        adapter.notifyDataSetChanged();
    }

    private boolean showNoDeviceAttachedDialog(final WifiInfo WifiInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.noSwitchSelected_title)
                .content(getString(R.string.noSwitchSelected_explanation_wifi)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + getString(R.string.noSwitchSelected_connectOneNow))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((dialog, which) -> {
                    getSwitchesAndShowSwitchesDialog(WifiInfo);
                    result = true;
                })
                .show();
        return result;
    }

    @Override
    public boolean onEnableClick(WifiInfo WifiInfo, boolean checked) {
        if (WifiInfo.getSwitchIdx() <= 0 && checked)
            return showNoDeviceAttachedDialog(WifiInfo);
        else {
            WifiInfo.setEnabled(checked);
            updateWifi(WifiInfo);
            return checked;
        }
    }

    @Override
    public void onRemoveClick(WifiInfo WifiInfo) {
        showRemoveUndoSnackbar(WifiInfo);
    }


    private void showRemoveUndoSnackbar(final WifiInfo WifiInfo) {
        // remove location from list view
        removeWifiFromListView(WifiInfo);

        // Show snackbar with undo option
        String text = String.format(getString(R.string.something_deleted),
                getString(R.string.bluetooth));

        UsefulBits.showSnackbarWithAction(this, coordinatorLayout, text, Snackbar.LENGTH_SHORT, new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                switch (event) {
                    case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                    case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                    case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                    case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                    case Snackbar.Callback.DISMISS_EVENT_ACTION:
                        removeWifiFromListView(WifiInfo);
                        break;
                }
            }
        }, v -> {
            updateWifi(WifiInfo);//undo
        }, this.getString(R.string.undo));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (mSharedPrefs.isBluetoothEnabled())
                getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_bluetooth_show:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionsUtil.canAccessLocation(this)) {
                        showWifiList();
                    } else {
                        permissionHelper.request(PermissionsUtil.INITIAL_LOCATION_PERMS);
                    }
                } else {
                    showWifiList();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeWifiFromListView(WifiInfo WifiInfo) {
        WifiList.remove(WifiInfo);
        mSharedPrefs.saveWifiList(WifiList);
        adapter.data = WifiList;
        adapter.notifyDataSetChanged();
    }

    /* Called when the second activity's finishes */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
        if (PermissionsUtil.canAccessLocation(this)) {
            showWifiList();
        }
    }
}