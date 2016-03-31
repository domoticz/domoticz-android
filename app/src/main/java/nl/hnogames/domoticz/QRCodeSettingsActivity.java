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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import nl.hnogames.domoticz.Adapters.QRCodeAdapter;
import nl.hnogames.domoticz.Containers.QRCodeInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.QRCodeClickListener;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.UI.SwitchDialog;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;


public class QRCodeSettingsActivity extends AppCompatActivity implements QRCodeClickListener {

    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<QRCodeInfo> qrcodeList;
    private QRCodeAdapter adapter;
    private boolean busyWithQRCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_settings);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        if (mSharedPrefs.darkThemeEnabled()) {
            coordinatorLayout.setBackgroundColor(getResources().getColor(R.color.background_dark));
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.category_QRCode);

        domoticz = new Domoticz(this, null);
        qrcodeList = mSharedPrefs.getQRCodeList();
        adapter = new QRCodeAdapter(this, qrcodeList, this);

        createListView();

        showSimpleSnackbar(getString(R.string.qrcode_register));
    }

    private void createListView() {
        ListView listView = (ListView) findViewById(R.id.listView);
        if (mSharedPrefs.darkThemeEnabled()) {
            listView.setBackgroundColor(getResources().getColor(R.color.background_dark));
        }
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int item, long id) {

            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                getSwitchesAndShowSwitchesDialog(qrcodeList.get(position));

                return true;
            }
        });
    }

    private void getSwitchesAndShowSwitchesDialog(final QRCodeInfo qrInfo) {
        domoticz.getSwitches(new SwitchesReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                showSwitchesDialog(qrInfo, switches);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(coordinatorLayout,
                        R.string.unable_to_get_switches,
                        Snackbar.LENGTH_SHORT)
                        .setAction(R.string.retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // User clicked retry button
                                getSwitchesAndShowSwitchesDialog(qrInfo);
                            }
                        })
                        .show();
            }
        });
    }

    private void showSwitchesDialog(
            final QRCodeInfo qrcodeInfo,
            ArrayList<SwitchInfo> switches) {

        SwitchDialog infoDialog = new SwitchDialog(
                QRCodeSettingsActivity.this, switches,
                R.layout.dialog_switch_logs,
                domoticz);
        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName) {
                qrcodeInfo.setSwitchIdx(selectedSwitchIDX);
                qrcodeInfo.setSwitchPassword(selectedSwitchPassword);
                qrcodeInfo.setSwitchName(selectedSwitchName);
                updateQRCode(qrcodeInfo);
            }
        });

        infoDialog.show();
    }

    public void updateQRCode(QRCodeInfo qrcodeInfo) {
        if (qrcodeList == null)
            qrcodeList = new ArrayList<>();

        boolean found = false;
        int i = 0;
        for (QRCodeInfo l : qrcodeList) {
            if (l.getId().equals(qrcodeInfo.getId())) {
                qrcodeList.set(i, qrcodeInfo);
                found = true;
            }
            i++;
        }
        if (!found)//add new
            qrcodeList.add(qrcodeInfo);

        mSharedPrefs.saveQRCodeList(qrcodeList);
        adapter.data = qrcodeList;
        adapter.notifyDataSetChanged();
    }

    private boolean showNoDeviceAttachedDialog(final QRCodeInfo qrcodeInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.noSwitchSelected_title)
                .content(getString(R.string.noSwitchSelected_explanation_qrcode)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + getString(R.string.noSwitchSelected_connectOneNow))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getSwitchesAndShowSwitchesDialog(qrcodeInfo);
                        result = true;
                    }
                })
                .show();
        return result;
    }

    private void showSimpleSnackbar(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onEnableClick(QRCodeInfo qrcode, boolean checked) {
        if (qrcode.getSwitchIdx() <= 0 && checked)
            return showNoDeviceAttachedDialog(qrcode);
        else {
            qrcode.setEnabled(checked);
            updateQRCode(qrcode);
            return checked;
        }
    }

    @Override
    public void onRemoveClick(QRCodeInfo qrcode) {
        showRemoveUndoSnackbar(qrcode);
    }


    private void showRemoveUndoSnackbar(final QRCodeInfo qrcodeInfo) {
        // remove location from list view
        removeQRCodeFromListView(qrcodeInfo);

        // Show snackbar with undo option
        String text = String.format(getString(R.string.something_deleted),
                getString(R.string.qrcode));
        Snackbar.make(coordinatorLayout,
                text,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateQRCode(qrcodeInfo);//undo
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);

                        switch (event) {
                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                            case Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE:
                            case Snackbar.Callback.DISMISS_EVENT_MANUAL:
                                // Snackbar was timed out so let's remove the data from
                                // shared preferences
                                removeQRCodeFromListView(qrcodeInfo);
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (mSharedPrefs.isQRCodeEnabled())
                getMenuInflater().inflate(R.menu.menu_qrcode, menu);
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
            case R.id.action_scan_qrcode:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (PermissionsUtil.canAccessCamera(this)) {
                        Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                        startActivityForResult(iQRCodeScannerActivity, 998);
                    } else {
                        requestPermissions(PermissionsUtil.INITIAL_CAMERA_PERMS, PermissionsUtil.INITIAL_CAMERA_REQUEST);
                    }
                } else {
                    Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                    startActivityForResult(iQRCodeScannerActivity, 998);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsUtil.INITIAL_CAMERA_REQUEST:
                if (PermissionsUtil.canAccessStorage(this)) {
                    Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                    startActivityForResult(iQRCodeScannerActivity, 998);
                }
                break;
        }
    }

    private void removeQRCodeFromListView(QRCodeInfo qrcodeInfo) {
        qrcodeList.remove(qrcodeInfo);
        mSharedPrefs.saveQRCodeList(qrcodeList);

        adapter.data = qrcodeList;
        adapter.notifyDataSetChanged();
    }

    /* Called when the second activity's finishes */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            final String QR_Code_ID = data.getStringExtra("QRCODE");

            boolean newTagFound = true;
            busyWithQRCode = true;
            if (qrcodeList != null && qrcodeList.size() > 0) {
                for (QRCodeInfo n : qrcodeList) {
                    if (n.getId().equals(QR_Code_ID))
                        newTagFound = false;
                }
            }

            if (newTagFound) {
                showSimpleSnackbar(getString(R.string.qrcode_found) + ": " + QR_Code_ID);
                new MaterialDialog.Builder(this)
                        .title(R.string.qrcode_found)
                        .content(R.string.qrcode_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(R.string.category_QRCode, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                showSimpleSnackbar(getString(R.string.nfc_saved) + ": " + input);
                                QRCodeInfo qrCodeInfo = new QRCodeInfo();
                                qrCodeInfo.setId(QR_Code_ID);
                                qrCodeInfo.setName(String.valueOf(input));
                                updateQRCode(qrCodeInfo);
                                busyWithQRCode = false;
                            }
                        }).show();
            } else {
                showSimpleSnackbar(getString(R.string.qrcode_exists));
                busyWithQRCode = false;
            }
        }
    }
}