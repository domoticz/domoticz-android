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
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ftinc.scoop.Scoop;
import com.google.android.material.snackbar.Snackbar;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import nl.hnogames.domoticz.adapters.NFCAdapter;
import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.containers.NFCInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.helpers.TagDispatcher;
import nl.hnogames.domoticz.helpers.TagDispatcherBuilder;
import nl.hnogames.domoticz.interfaces.NFCClickListener;
import nl.hnogames.domoticz.interfaces.OnDiscoveredTagListener;
import nl.hnogames.domoticz.ui.SwitchDialog;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class NFCSettingsActivity extends AppCompatAssistActivity implements NFCClickListener, OnDiscoveredTagListener {
    private final String TAG = NFCSettingsActivity.class.getSimpleName();
    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<NFCInfo> nfcList;
    private NFCAdapter adapter;
    private boolean busyWithTag = false;
    private Toolbar toolbar;
    private TagDispatcher nfcManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        Log.i(TAG, "onCreate called");

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_settings);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.category_nfc);

        nfcManager = new TagDispatcherBuilder(this, this)
                .enableBroadcomWorkaround(true)
                .enableSounds(false)
                .build();

        nfcList = mSharedPrefs.getNFCList();
        adapter = new NFCAdapter(this, nfcList, this);
        createListView();
    }

    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called");

        try {
            if (NfcAdapter.getDefaultAdapter(this) != null) {
                if (!NfcAdapter.getDefaultAdapter(this).isEnabled()) {
                    UsefulBits.showSimpleToast(this, getString(R.string.enable_nfc), Snackbar.LENGTH_SHORT);
                    Log.i(TAG, getString(R.string.enable_nfc));
                    startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                } else {
                    UsefulBits.showSnackbar(this, coordinatorLayout, R.string.nfc_register, Snackbar.LENGTH_SHORT);
                    Log.i(TAG, getString(R.string.nfc_register));
                }
            } else {
                UsefulBits.showSnackbar(this, coordinatorLayout, R.string.nfc_not_supported, Snackbar.LENGTH_SHORT);
                Log.i(TAG, getString(R.string.nfc_not_supported));
            }

            nfcManager.enableExclusiveNfc();
        } catch (Exception ex) {
            Log.e(this.getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called");
        nfcManager.disableExclusiveNfc();
    }

    private void createListView() {
        ListView listView = findViewById(R.id.listView);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener((adapterView, view, item, id) -> showEditDialog(nfcList.get(item)));
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            NFCInfo nfcDevice = nfcList.get(position);
            if (nfcDevice.getSwitchIdx() > 0) {
                nfcDevice.setSwitchIdx(0);
                nfcDevice.setSwitchName(null);
                nfcDevice.setValue(null);
                nfcDevice.setSwitchPassword(null);
                updateNFC(nfcDevice);
                UsefulBits.showSnackbar(NFCSettingsActivity.this, coordinatorLayout, R.string.switch_connection_removed, Snackbar.LENGTH_LONG);
                adapter.notifyDataSetChanged();
            } else
                getSwitchesAndShowSwitchesDialog(nfcDevice);
            return true;
        });
    }

    private void showEditDialog(final NFCInfo mNFCInfo) {
        busyWithTag = true;
        new MaterialDialog.Builder(this)
                .title(R.string.nfc_tag_edit)
                .content(R.string.nfc_tag_name)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(R.string.cancel)
                .input(this.getString(R.string.category_nfc), mNFCInfo.getName(), (dialog, input) -> {
                    if (!UsefulBits.isEmpty(String.valueOf(input))) {
                        mNFCInfo.setName(String.valueOf(input));
                        updateNFC(mNFCInfo);
                    }
                    busyWithTag = false;
                }).show();
    }

    private void getSwitchesAndShowSwitchesDialog(final NFCInfo nfcInfo) {
        StaticHelper.getDomoticz(NFCSettingsActivity.this).getDevices(new DevicesReceiver() {
            @Override

            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                showSwitchesDialog(nfcInfo, switches);
            }

            @Override

            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override

            public void onError(Exception error) {
                UsefulBits.showSnackbarWithAction(NFCSettingsActivity.this, coordinatorLayout, NFCSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, v -> getSwitchesAndShowSwitchesDialog(nfcInfo), NFCSettingsActivity.this.getString(R.string.retry));
            }
        }, 0, "all");
    }

    private void showSwitchesDialog(
            final NFCInfo nfcInfo,
            ArrayList<DevicesInfo> switches) {

        final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo d : switches) {
            if (DeviceUtils.isAutomatedToggableDevice(d))
                supportedSwitches.add(d);
        }

        SwitchDialog infoDialog = new SwitchDialog(
                NFCSettingsActivity.this, supportedSwitches,
                R.layout.dialog_switch_logs,
                StaticHelper.getDomoticz(NFCSettingsActivity.this));

        infoDialog.onDismissListener((selectedSwitchIDX, selectedSwitchPassword, selectedSwitchName, isSceneOrGroup) -> {
            nfcInfo.setSwitchIdx(selectedSwitchIDX);
            nfcInfo.setSwitchPassword(selectedSwitchPassword);
            nfcInfo.setSwitchName(selectedSwitchName);
            nfcInfo.setSceneOrGroup(isSceneOrGroup);

            if (!isSceneOrGroup) {
                for (DevicesInfo s : supportedSwitches) {
                    if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                        showSelectorDialog(nfcInfo, s);
                    else
                        updateNFC(nfcInfo);
                }
            } else {
                updateNFC(nfcInfo);
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final NFCInfo nfcInfo, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback((dialog, view, which, text) -> {
                    nfcInfo.setValue(String.valueOf(text));
                    updateNFC(nfcInfo);
                })
                .show();
    }

    public void updateNFC(NFCInfo nfcInfo) {
        if (nfcList == null)
            nfcList = new ArrayList<>();

        boolean found = false;
        int i = 0;
        for (NFCInfo l : nfcList) {
            if (l.getId().equals(nfcInfo.getId())) {
                nfcList.set(i, nfcInfo);
                found = true;
            }
            i++;
        }
        if (!found)//add new
            nfcList.add(nfcInfo);

        mSharedPrefs.saveNFCList(nfcList);
        adapter.data = nfcList;
        adapter.notifyDataSetChanged();
    }

    private boolean showNoDeviceAttachedDialog(final NFCInfo nfcInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.noSwitchSelected_title)
                .content(getString(R.string.noSwitchSelected_explanation_nfc)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + getString(R.string.noSwitchSelected_connectOneNow))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((dialog, which) -> {
                    getSwitchesAndShowSwitchesDialog(nfcInfo);
                    result = true;
                })
                .show();
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onEnableClick(NFCInfo nfc, boolean checked) {
        if (nfc.getSwitchIdx() <= 0 && checked)
            return showNoDeviceAttachedDialog(nfc);
        else {
            nfc.setEnabled(checked);
            updateNFC(nfc);
            return checked;
        }
    }

    @Override
    public void onRemoveClick(NFCInfo nfc) {
        showRemoveUndoSnackbar(nfc);
    }


    private void showRemoveUndoSnackbar(final NFCInfo nfcInfo) {
        // remove location from list view
        removeNFCFromListView(nfcInfo);

        // Show snackbar with undo option
        String text = String.format(getString(R.string.something_deleted),
                getString(R.string.nfc));

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
                        removeNFCFromListView(nfcInfo);
                        break;
                }
            }
        }, v -> updateNFC(nfcInfo), this.getString(R.string.undo));
    }

    private void removeNFCFromListView(NFCInfo nfcInfo) {
        nfcList.remove(nfcInfo);
        mSharedPrefs.saveNFCList(nfcList);

        adapter.data = nfcList;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void tagDiscovered(Tag tag) {
        Log.i(TAG, "tagDiscovered");

        if (tag != null && tag.getId() != null && !busyWithTag) {
            boolean newTagFound = true;
            busyWithTag = true;
            final String tagID = UsefulBits.ByteArrayToHexString(tag.getId());
            if (nfcList != null && nfcList.size() > 0) {
                for (NFCInfo n : nfcList) {
                    if (n.getId().equals(tagID))
                        newTagFound = false;
                }
            }

            if (newTagFound) {
                UsefulBits.showSnackbar(this, coordinatorLayout, getString(R.string.nfc_tag_found) + ": " + tagID, Snackbar.LENGTH_SHORT);
                new MaterialDialog.Builder(this)
                        .title(R.string.nfc_tag_found)
                        .content(R.string.nfc_tag_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(R.string.category_nfc, 0, (dialog, input) -> {
                            if (!UsefulBits.isEmpty(String.valueOf(input))) {
                                UsefulBits.showSnackbar(NFCSettingsActivity.this, coordinatorLayout, getString(R.string.nfc_saved) + ": " + input, Snackbar.LENGTH_SHORT);
                                NFCInfo newNFC = new NFCInfo();
                                newNFC.setId(tagID);
                                newNFC.setName(String.valueOf(input));
                                updateNFC(newNFC);
                            }
                            busyWithTag = false;
                        }).show();
            } else {
                UsefulBits.showSnackbar(NFCSettingsActivity.this, coordinatorLayout, R.string.nfc_exists, Snackbar.LENGTH_SHORT);
                busyWithTag = false;
            }
        }
    }
}
