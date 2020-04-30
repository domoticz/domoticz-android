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
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.adapters.BeaconAdapter;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.containers.BeaconInfo;
import nl.hnogames.domoticz.interfaces.BeaconClickListener;
import nl.hnogames.domoticz.ui.AddBeaconDialog;
import nl.hnogames.domoticz.ui.SwitchDialog;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class BeaconSettingsActivity extends AppCompatPermissionsActivity implements BeaconClickListener {
    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<BeaconInfo> beaconList;
    private nl.hnogames.domoticz.adapters.BeaconAdapter adapter;
    private PermissionHelper permissionHelper;
    private BeaconAdapter mBeaconAdapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        Scoop.getInstance().apply(this);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        permissionHelper = PermissionHelper.getInstance(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beacon_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.beacon);

        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        beaconList = mSharedPrefs.getBeaconList();
        adapter = new nl.hnogames.domoticz.adapters.BeaconAdapter(this, beaconList, this);

        createListView();
    }

    private void showBeacons() {
        AddBeaconDialog dialog = new AddBeaconDialog(
                this, new AddBeaconDialog.OnDoneListener() {
            @Override
            public void onAdded(final BeaconInfo beacon) {
                new MaterialDialog.Builder(BeaconSettingsActivity.this)
                        .title(R.string.beacon_found)
                        .content(R.string.beacon_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(R.string.beacon, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (!UsefulBits.isEmpty(String.valueOf(input))) {
                                    UsefulBits.showSnackbar(BeaconSettingsActivity.this, coordinatorLayout, getString(R.string.beacon_saved) + ": " + input, Snackbar.LENGTH_SHORT);
                                    beacon.setName(String.valueOf(input));
                                    updateBeacon(beacon);
                                }
                            }
                        }).show();
            }
        });
        dialog.show();
    }

    private void createListView() {
        ListView listView = findViewById(R.id.listView);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long id) {
                getSwitchesAndShowSwitchesDialog(beaconList.get(item));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                getSwitchesAndShowSwitchesDialog(beaconList.get(position));
                return true;
            }
        });
    }

    private void getSwitchesAndShowSwitchesDialog(final BeaconInfo qrInfo) {
        domoticz.getDevices(new DevicesReceiver() {
            @Override
            @DebugLog
            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                showSwitchesDialog(qrInfo, switches);
            }

            @Override
            @DebugLog
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbarWithAction(BeaconSettingsActivity.this, coordinatorLayout, BeaconSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSwitchesAndShowSwitchesDialog(qrInfo);
                            }
                        }, BeaconSettingsActivity.this.getString(R.string.retry));
            }
        }, 0, "all");
    }

    private void showSwitchesDialog(
            final BeaconInfo BeaconInfo,
            final ArrayList<DevicesInfo> switches) {

        final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo d : switches) {
            if (DeviceUtils.isAutomatedToggableDevice(d))
                supportedSwitches.add(d);
        }

        SwitchDialog infoDialog = new SwitchDialog(
                BeaconSettingsActivity.this, supportedSwitches,
                R.layout.dialog_switch_logs,
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName, boolean isSceneOrGroup) {
                BeaconInfo.setSwitchIdx(selectedSwitchIDX);
                BeaconInfo.setSwitchPassword(selectedSwitchPassword);
                BeaconInfo.setSwitchName(selectedSwitchName);
                BeaconInfo.setSceneOrGroup(isSceneOrGroup);

                if (!isSceneOrGroup) {
                    for (DevicesInfo s : supportedSwitches) {
                        if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                            showSelectorDialog(BeaconInfo, s);
                        else
                            updateBeacon(BeaconInfo);

                    }
                } else {
                    updateBeacon(BeaconInfo);
                }
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final BeaconInfo BeaconInfo, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        BeaconInfo.setValue(String.valueOf(text));
                        updateBeacon(BeaconInfo);
                    }
                })
                .show();
    }

    public void updateBeacon(BeaconInfo BeaconInfo) {
        if (beaconList == null)
            beaconList = new ArrayList<>();

        boolean found = false;
        int i = 0;
        for (BeaconInfo l : beaconList) {
            if (l.getId().equals(BeaconInfo.getId())) {
                beaconList.set(i, BeaconInfo);
                found = true;
            }
            i++;
        }
        if (!found)//add new
            beaconList.add(BeaconInfo);

        mSharedPrefs.saveBeaconList(beaconList);
        adapter.data = beaconList;
        adapter.notifyDataSetChanged();

        try {
            AppController.getInstance().StopBeaconScanning();
            AppController.getInstance().StartBeaconScanning();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean showNoDeviceAttachedDialog(final BeaconInfo BeaconInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.noSwitchSelected_title)
                .content(getString(R.string.noSwitchSelected_explanation_beacon)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + getString(R.string.noSwitchSelected_connectOneNow))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getSwitchesAndShowSwitchesDialog(BeaconInfo);
                        result = true;
                    }
                })
                .show();
        return result;
    }

    @Override
    public boolean onEnableClick(BeaconInfo Beacon, boolean checked) {
        if (Beacon.getSwitchIdx() <= 0 && checked)
            return showNoDeviceAttachedDialog(Beacon);
        else {
            Beacon.setEnabled(checked);
            updateBeacon(Beacon);
            return checked;
        }
    }

    @Override
    public void onRemoveClick(BeaconInfo Beacon) {
        showRemoveUndoSnackbar(Beacon);
    }


    private void showRemoveUndoSnackbar(final BeaconInfo BeaconInfo) {
        // remove location from list view
        removeBeaconFromListView(BeaconInfo);

        // Show snackbar with undo option
        String text = String.format(getString(R.string.something_deleted),
                getString(R.string.beacon));

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
                        removeBeaconFromListView(BeaconInfo);
                        break;
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBeacon(BeaconInfo);//undo
            }
        }, this.getString(R.string.undo));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (mSharedPrefs.isBeaconEnabled())
                getMenuInflater().inflate(R.menu.menu_beacon, menu);
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
            case R.id.action_beacon_show:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionsUtil.canAccessBluetooth(this) && PermissionsUtil.canAccessLocation(this)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (PermissionsUtil.canAccessBackgroundLocation(this))
                                showBeacons();
                            else
                                permissionHelper.request(PermissionsUtil.BACKGROUND_LOCATION_PERMS);
                        }
                        else
                            showBeacons();
                    } else
                        permissionHelper.request(PermissionsUtil.INITIAL_BEACON_PERMS);
                } else
                    showBeacons();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeBeaconFromListView(BeaconInfo BeaconInfo) {
        beaconList.remove(BeaconInfo);
        mSharedPrefs.saveBeaconList(beaconList);
        adapter.data = beaconList;
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
        if (PermissionsUtil.canAccessBluetooth(this) && PermissionsUtil.canAccessLocation(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (PermissionsUtil.canAccessBackgroundLocation(this))
                    showBeacons();
                else
                    permissionHelper.request(PermissionsUtil.BACKGROUND_LOCATION_PERMS);
            }
            else
                showBeacons();
        }
    }
}