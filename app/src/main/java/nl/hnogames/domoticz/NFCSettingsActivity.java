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

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Adapters.NFCAdapter;
import nl.hnogames.domoticz.Containers.NFCInfo;
import nl.hnogames.domoticz.Interfaces.NFCClickListener;
import nl.hnogames.domoticz.UI.SwitchDialog;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class NFCSettingsActivity extends AppCompatActivity implements NFCClickListener {
    // list of NFC technologies detected:
    private final String[][] techList = new String[][]{
            new String[]{
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(),
                    Ndef.class.getName()
            }
    };

    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private CoordinatorLayout coordinatorLayout;
    private NfcAdapter mNfcAdapter;
    private ArrayList<NFCInfo> nfcList;
    private NFCAdapter adapter;
    private boolean busyWithTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_settings);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        if (mSharedPrefs.darkThemeEnabled()) {
            coordinatorLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.category_nfc);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            UsefulBits.showSnackbar(this, coordinatorLayout, R.string.nfc_register, Snackbar.LENGTH_SHORT);
        } else {
            UsefulBits.showSnackbar(this, coordinatorLayout, R.string.nfc_not_supported, Snackbar.LENGTH_SHORT);
        }

        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        nfcList = mSharedPrefs.getNFCList();
        adapter = new NFCAdapter(this, nfcList, this);

        createListView();
    }

    protected void onResume() {
        super.onResume();

        try {
            // creating pending intent:
            PendingIntent mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            // creating intent receiver for NFC events:
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

            // enabling foreground dispatch for getting intent from NFC event:
            if (mNfcAdapter == null) {
                mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

                if (mNfcAdapter != null) {
                    UsefulBits.showSnackbar(this, coordinatorLayout, R.string.nfc_register, Snackbar.LENGTH_SHORT);
                } else {
                    UsefulBits.showSnackbar(this, coordinatorLayout, R.string.nfc_not_supported, Snackbar.LENGTH_SHORT);
                }
            }
            if (mNfcAdapter != null)
                mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, new IntentFilter[]{filter}, this.techList);
        } catch (Exception ex) {
            Log.e(this.getClass().getSimpleName(), ex.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // disabling foreground dispatch:
        if (mNfcAdapter == null)
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED) && !busyWithTag) {
            boolean newTagFound = true;
            busyWithTag = true;
            final String tagID = UsefulBits.ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
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
                        .input(R.string.category_nfc, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (!UsefulBits.isEmpty(String.valueOf(input))) {
                                    UsefulBits.showSnackbar(NFCSettingsActivity.this, coordinatorLayout, getString(R.string.nfc_saved) + ": " + input, Snackbar.LENGTH_SHORT);
                                    NFCInfo newNFC = new NFCInfo();
                                    newNFC.setId(tagID);
                                    newNFC.setName(String.valueOf(input));
                                    updateNFC(newNFC);
                                }
                                busyWithTag = false;
                            }
                        }).show();
            } else {
                UsefulBits.showSnackbar(NFCSettingsActivity.this, coordinatorLayout, R.string.nfc_exists, Snackbar.LENGTH_SHORT);
                busyWithTag = false;
            }
        }
    }

    private void createListView() {
        ListView listView = (ListView) findViewById(R.id.listView);
        if (mSharedPrefs.darkThemeEnabled()) {
            listView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long id) {
                showEditDialog(nfcList.get(item));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                getSwitchesAndShowSwitchesDialog(nfcList.get(position));
                return true;
            }
        });
    }

    private void showEditDialog(final NFCInfo mNFCInfo) {
        busyWithTag = true;
        new MaterialDialog.Builder(this)
                .title(R.string.nfc_tag_edit)
                .content(R.string.nfc_tag_name)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(R.string.cancel)
                .input(this.getString(R.string.category_nfc), mNFCInfo.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (!UsefulBits.isEmpty(String.valueOf(input))) {
                            mNFCInfo.setName(String.valueOf(input));
                            updateNFC(mNFCInfo);
                        }
                        busyWithTag = false;
                    }
                }).show();
    }

    private void getSwitchesAndShowSwitchesDialog(final NFCInfo nfcInfo) {
        domoticz.getDevices(new DevicesReceiver() {
            @Override
            @DebugLog
            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                showSwitchesDialog(nfcInfo, switches);
            }

            @Override
            @DebugLog
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbarWithAction(NFCSettingsActivity.this, coordinatorLayout, NFCSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSwitchesAndShowSwitchesDialog(nfcInfo);
                            }
                        }, NFCSettingsActivity.this.getString(R.string.retry));
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
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName, boolean isSceneOrGroup) {
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
                }
                else {
                    updateNFC(nfcInfo);
                }
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final NFCInfo nfcInfo, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        nfcInfo.setValue(String.valueOf(text));
                        updateNFC(nfcInfo);
                    }
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
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getSwitchesAndShowSwitchesDialog(nfcInfo);
                        result = true;
                    }
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
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateNFC(nfcInfo);
            }
        }, this.getString(R.string.undo));
    }

    private void removeNFCFromListView(NFCInfo nfcInfo) {
        nfcList.remove(nfcInfo);
        mSharedPrefs.saveNFCList(nfcList);

        adapter.data = nfcList;
        adapter.notifyDataSetChanged();
    }
}
