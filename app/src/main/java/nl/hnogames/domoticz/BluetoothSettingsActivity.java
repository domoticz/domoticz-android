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

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.containers.BluetoothInfo;
import nl.hnogames.domoticz.interfaces.BluetoothClickListener;
import nl.hnogames.domoticz.ui.SwitchDialog;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;


public class BluetoothSettingsActivity extends AppCompatPermissionsActivity implements BluetoothClickListener {
    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<BluetoothInfo> BluetoothList;
    private nl.hnogames.domoticz.adapters.BluetoothAdapter adapter;
    private PermissionHelper permissionHelper;
    private BluetoothAdapter mBluetoothAdapter;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        Scoop.getInstance().apply(this);

        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        permissionHelper = PermissionHelper.getInstance(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bluetooth_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.category_bluetooth);

        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        BluetoothList = mSharedPrefs.getBluetoothList();
        adapter = new nl.hnogames.domoticz.adapters.BluetoothAdapter(this, BluetoothList, this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        createListView();
    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        final List<String> bluetoothDevices = new ArrayList<>();
        for (BluetoothDevice bt : pairedDevices)
            bluetoothDevices.add(bt.getName());
        CharSequence[] items = bluetoothDevices.toArray(new CharSequence[bluetoothDevices.size()]);
        if (items == null || items.length <= 0) {
            UsefulBits.showSnackbar(BluetoothSettingsActivity.this, coordinatorLayout, R.string.bluetooth_turned_off, Snackbar.LENGTH_SHORT);
            return;
        }

        new AlertDialog.Builder(this)
                .setSingleChoiceItems(items, 0, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        final String Bluetooth_ID = bluetoothDevices.get(position);

                        boolean newTagFound = true;
                        if (BluetoothList != null && BluetoothList.size() > 0) {
                            for (BluetoothInfo n : BluetoothList) {
                                if (n.getId().equals(Bluetooth_ID))
                                    newTagFound = false;
                            }
                        }

                        if (newTagFound) {
                            UsefulBits.showSnackbar(BluetoothSettingsActivity.this, coordinatorLayout, getString(R.string.bluetooth_saved) + ": " + Bluetooth_ID, Snackbar.LENGTH_SHORT);
                            BluetoothInfo BluetoothInfo = new BluetoothInfo();
                            BluetoothInfo.setId(Bluetooth_ID);
                            BluetoothInfo.setName(String.valueOf(Bluetooth_ID));
                            updateBluetooth(BluetoothInfo);
                        } else {
                            UsefulBits.showSnackbar(BluetoothSettingsActivity.this, coordinatorLayout, R.string.bluetooth_exists, Snackbar.LENGTH_SHORT);
                        }
                    }
                })
                .show();
    }

    private void createListView() {
        ListView listView = findViewById(R.id.listView);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long id) {
                getSwitchesAndShowSwitchesDialog(BluetoothList.get(item));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                getSwitchesAndShowSwitchesDialog(BluetoothList.get(position));
                return true;
            }
        });
    }

    private void getSwitchesAndShowSwitchesDialog(final BluetoothInfo qrInfo) {
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
                UsefulBits.showSnackbarWithAction(BluetoothSettingsActivity.this, coordinatorLayout, BluetoothSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSwitchesAndShowSwitchesDialog(qrInfo);
                            }
                        }, BluetoothSettingsActivity.this.getString(R.string.retry));
            }
        }, 0, "all");
    }

    private void showSwitchesDialog(
            final BluetoothInfo BluetoothInfo,
            final ArrayList<DevicesInfo> switches) {

        final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo d : switches) {
            if (DeviceUtils.isAutomatedToggableDevice(d))
                supportedSwitches.add(d);
        }

        SwitchDialog infoDialog = new SwitchDialog(
                BluetoothSettingsActivity.this, supportedSwitches,
                R.layout.dialog_switch_logs,
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName, boolean isSceneOrGroup) {
                BluetoothInfo.setSwitchIdx(selectedSwitchIDX);
                BluetoothInfo.setSwitchPassword(selectedSwitchPassword);
                BluetoothInfo.setSwitchName(selectedSwitchName);
                BluetoothInfo.setSceneOrGroup(isSceneOrGroup);

                if (!isSceneOrGroup) {
                    for (DevicesInfo s : supportedSwitches) {
                        if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                            showSelectorDialog(BluetoothInfo, s);
                        else
                            updateBluetooth(BluetoothInfo);

                    }
                } else {
                    updateBluetooth(BluetoothInfo);
                }
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final BluetoothInfo BluetoothInfo, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        BluetoothInfo.setValue(String.valueOf(text));
                        updateBluetooth(BluetoothInfo);
                    }
                })
                .show();
    }

    public void updateBluetooth(BluetoothInfo BluetoothInfo) {
        if (BluetoothList == null)
            BluetoothList = new ArrayList<>();

        boolean found = false;
        int i = 0;
        for (BluetoothInfo l : BluetoothList) {
            if (l.getId().equals(BluetoothInfo.getId())) {
                BluetoothList.set(i, BluetoothInfo);
                found = true;
            }
            i++;
        }
        if (!found)//add new
            BluetoothList.add(BluetoothInfo);

        mSharedPrefs.saveBluetoothList(BluetoothList);
        adapter.data = BluetoothList;
        adapter.notifyDataSetChanged();
    }

    private boolean showNoDeviceAttachedDialog(final BluetoothInfo BluetoothInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.noSwitchSelected_title)
                .content(getString(R.string.noSwitchSelected_explanation_bluetooth)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + getString(R.string.noSwitchSelected_connectOneNow))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getSwitchesAndShowSwitchesDialog(BluetoothInfo);
                        result = true;
                    }
                })
                .show();
        return result;
    }

    @Override
    public boolean onEnableClick(BluetoothInfo Bluetooth, boolean checked) {
        if (Bluetooth.getSwitchIdx() <= 0 && checked)
            return showNoDeviceAttachedDialog(Bluetooth);
        else {
            Bluetooth.setEnabled(checked);
            updateBluetooth(Bluetooth);
            return checked;
        }
    }

    @Override
    public void onRemoveClick(BluetoothInfo Bluetooth) {
        showRemoveUndoSnackbar(Bluetooth);
    }


    private void showRemoveUndoSnackbar(final BluetoothInfo BluetoothInfo) {
        // remove location from list view
        removeBluetoothFromListView(BluetoothInfo);

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
                        removeBluetoothFromListView(BluetoothInfo);
                        break;
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBluetooth(BluetoothInfo);//undo
            }
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
                    if (PermissionsUtil.canAccessBluetooth(this)) {
                        showPairedDevices();
                    } else {
                        permissionHelper.request(PermissionsUtil.INITIAL_BLUETOOTH_PERMS);
                    }
                } else {
                    showPairedDevices();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeBluetoothFromListView(BluetoothInfo BluetoothInfo) {
        BluetoothList.remove(BluetoothInfo);
        mSharedPrefs.saveBluetoothList(BluetoothList);
        adapter.data = BluetoothList;
        adapter.notifyDataSetChanged();
    }

    /* Called when the second activity's finishes */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        permissionHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
        if (PermissionsUtil.canAccessBluetooth(this)) {
            showPairedDevices();
        }
    }
}