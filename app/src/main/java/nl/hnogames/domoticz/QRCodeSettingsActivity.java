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
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Adapters.QRCodeAdapter;
import nl.hnogames.domoticz.Containers.QRCodeInfo;
import nl.hnogames.domoticz.Interfaces.QRCodeClickListener;
import nl.hnogames.domoticz.UI.SwitchDialog;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppCompatPermissionsActivity;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;


public class QRCodeSettingsActivity extends AppCompatPermissionsActivity implements QRCodeClickListener {

    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private CoordinatorLayout coordinatorLayout;
    private ArrayList<QRCodeInfo> qrcodeList;
    private QRCodeAdapter adapter;
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        permissionHelper = PermissionHelper.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_settings);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        if (mSharedPrefs.darkThemeEnabled()) {
            coordinatorLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.category_QRCode);

        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        qrcodeList = mSharedPrefs.getQRCodeList();
        adapter = new QRCodeAdapter(this, qrcodeList, this);

        createListView();

        UsefulBits.showSnackbar(this, coordinatorLayout, R.string.qrcode_register, Snackbar.LENGTH_SHORT);
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
                showEditDialog(qrcodeList.get(item));
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

    private void showEditDialog(final QRCodeInfo mQRCodeInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.qrcode_edit)
                .content(R.string.qrcode_name)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .negativeText(R.string.cancel)
                .input(this.getString(R.string.category_QRCode), mQRCodeInfo.getName(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (!UsefulBits.isEmpty(String.valueOf(input))) {
                            mQRCodeInfo.setName(String.valueOf(input));
                            updateQRCode(mQRCodeInfo);
                        }
                    }
                }).show();
    }

    private void getSwitchesAndShowSwitchesDialog(final QRCodeInfo qrInfo) {
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
                UsefulBits.showSnackbarWithAction(QRCodeSettingsActivity.this, coordinatorLayout, QRCodeSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSwitchesAndShowSwitchesDialog(qrInfo);
                            }
                        }, QRCodeSettingsActivity.this.getString(R.string.retry));
            }
        }, 0, "all");
    }

    private void showSwitchesDialog(
            final QRCodeInfo qrcodeInfo,
            final ArrayList<DevicesInfo> switches) {

        final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo d : switches) {
            if (DeviceUtils.isAutomatedToggableDevice(d))
                supportedSwitches.add(d);
        }

        SwitchDialog infoDialog = new SwitchDialog(
                QRCodeSettingsActivity.this, supportedSwitches,
                R.layout.dialog_switch_logs,
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName, boolean isSceneOrGroup) {
                qrcodeInfo.setSwitchIdx(selectedSwitchIDX);
                qrcodeInfo.setSwitchPassword(selectedSwitchPassword);
                qrcodeInfo.setSwitchName(selectedSwitchName);
                qrcodeInfo.setSceneOrGroup(isSceneOrGroup);

                if (!isSceneOrGroup) {
                    for (DevicesInfo s : supportedSwitches) {
                        if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                            showSelectorDialog(qrcodeInfo, s);
                        else
                            updateQRCode(qrcodeInfo);

                    }
                }
                else {
                    updateQRCode(qrcodeInfo);
                }
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final QRCodeInfo qrcodeInfo, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        qrcodeInfo.setValue(String.valueOf(text));
                        updateQRCode(qrcodeInfo);
                    }
                })
                .show();
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
                        removeQRCodeFromListView(qrcodeInfo);
                        break;
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateQRCode(qrcodeInfo);//undo
            }
        }, this.getString(R.string.undo));
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
                        permissionHelper.request(PermissionsUtil.INITIAL_CAMERA_PERMS);
                    }
                } else {
                    Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
                    startActivityForResult(iQRCodeScannerActivity, 998);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void removeQRCodeFromListView(QRCodeInfo qrcodeInfo) {
        qrcodeList.remove(qrcodeInfo);
        mSharedPrefs.saveQRCodeList(qrcodeList);
        adapter.data = qrcodeList;
        adapter.notifyDataSetChanged();
    }

    /* Called when the second activity's finishes */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            final String QR_Code_ID = data.getStringExtra("QRCODE");

            boolean newTagFound = true;
            if (qrcodeList != null && qrcodeList.size() > 0) {
                for (QRCodeInfo n : qrcodeList) {
                    if (n.getId().equals(QR_Code_ID))
                        newTagFound = false;
                }
            }

            if (newTagFound) {
                UsefulBits.showSnackbar(this, coordinatorLayout, getString(R.string.qrcode_found) + ": " + QR_Code_ID, Snackbar.LENGTH_SHORT);
                new MaterialDialog.Builder(this)
                        .title(R.string.qrcode_found)
                        .content(R.string.qrcode_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(R.string.category_QRCode, 0, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (!UsefulBits.isEmpty(String.valueOf(input))) {
                                    UsefulBits.showSnackbar(QRCodeSettingsActivity.this, coordinatorLayout, getString(R.string.qrcode_saved) + ": " + input, Snackbar.LENGTH_SHORT);
                                    QRCodeInfo qrCodeInfo = new QRCodeInfo();
                                    qrCodeInfo.setId(QR_Code_ID);
                                    qrCodeInfo.setName(String.valueOf(input));
                                    updateQRCode(qrCodeInfo);
                                }
                            }
                        }).show();
            } else {
                UsefulBits.showSnackbar(this, coordinatorLayout, R.string.qrcode_exists, Snackbar.LENGTH_SHORT);
            }
        }
        permissionHelper.onActivityForResult(requestCode);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
        if (PermissionsUtil.canAccessStorage(this)) {
            Intent iQRCodeScannerActivity = new Intent(this, QRCodeCaptureActivity.class);
            startActivityForResult(iQRCodeScannerActivity, 998);
        }
    }
}