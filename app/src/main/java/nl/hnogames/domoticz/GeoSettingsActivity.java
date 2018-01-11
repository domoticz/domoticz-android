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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.google.android.gms.maps.model.LatLng;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.schibstedspain.leku.LocationPickerActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Adapters.LocationAdapter;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Interfaces.LocationClickListener;
import nl.hnogames.domoticz.UI.SwitchDialog;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.GeoUtils;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class GeoSettingsActivity extends AppCompatActivity implements OnPermissionCallback {

    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private ArrayList<LocationInfo> locations;
    private LocationAdapter adapter;
    private GeoUtils oGeoUtils;
    private CoordinatorLayout coordinatorLayout;
    private int EditLocationID = 0;
    private PermissionHelper permissionHelper;
    private Switch geoSwitch;
    private Switch geoNotificationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);
        if (mSharedPrefs.darkThemeEnabled())
            setTheme(R.style.AppThemeDark);
        else
            setTheme(R.style.AppTheme);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        oGeoUtils = new GeoUtils(this, this);

        permissionHelper = PermissionHelper.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.geofence);

        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        if (mSharedPrefs.darkThemeEnabled()) {
            coordinatorLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }

        oGeoUtils.AddGeofences();
        createListView();
        initSwitches();
    }

    private void initSwitches() {
        geoSwitch = (Switch) findViewById(R.id.switch_button);
        geoSwitch.setChecked(mSharedPrefs.isGeofenceEnabled());

        geoNotificationSwitch = (Switch) findViewById(R.id.switch_notifications_button);
        geoNotificationSwitch.setChecked(mSharedPrefs.isGeofenceNotificationsEnabled());

        geoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!PermissionsUtil.canAccessLocation(GeoSettingsActivity.this)) {
                            geoSwitch.setChecked(false);
                            geoNotificationSwitch.setEnabled(false);
                            permissionHelper
                                    .request(PermissionsUtil.INITIAL_LOCATION_PERMS);
                        } else {
                            if (!PermissionsUtil.canAccessStorage(GeoSettingsActivity.this)) {
                                geoSwitch.setChecked(false);
                                geoNotificationSwitch.setEnabled(false);
                                permissionHelper
                                        .request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                            } else {
                                //all settings are correct
                                mSharedPrefs.setGeofenceEnabled(isChecked);
                                geoNotificationSwitch.setEnabled(true);
                                oGeoUtils.AddGeofences();
                                invalidateOptionsMenu();
                            }
                        }
                    } else {
                        mSharedPrefs.setGeofenceEnabled(isChecked);
                        oGeoUtils.AddGeofences();
                        invalidateOptionsMenu();
                    }
                } else {
                    mSharedPrefs.setGeofenceEnabled(false);
                    geoNotificationSwitch.setEnabled(false);
                    oGeoUtils.RemoveGeofences();
                    invalidateOptionsMenu();
                }
            }
        });

        geoNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPrefs.setGeofenceNotificationsEnabled(isChecked);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showSwitchesDialog(
            final LocationInfo selectedLocation,
            final ArrayList<DevicesInfo> switches) {

        final ArrayList<DevicesInfo> supportedSwitches = new ArrayList<>();
        for (DevicesInfo d : switches) {
            if (DeviceUtils.isAutomatedToggableDevice(d))
                supportedSwitches.add(d);
        }

        SwitchDialog infoDialog = new SwitchDialog(
                GeoSettingsActivity.this, supportedSwitches,
                R.layout.dialog_switch_logs,
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName, boolean isSceneOrGroup) {
                selectedLocation.setSwitchIdx(selectedSwitchIDX);
                selectedLocation.setSwitchPassword(selectedSwitchPassword);
                selectedLocation.setSwitchName(selectedSwitchName);
                selectedLocation.setSceneOrGroup(isSceneOrGroup);

                if (!isSceneOrGroup) {
                    for (DevicesInfo s : supportedSwitches) {
                        if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                            showSelectorDialog(selectedLocation, s);
                        else {
                            mSharedPrefs.updateLocation(selectedLocation);
                            adapter.data = mSharedPrefs.getLocations();
                            adapter.notifyDataSetChanged();
                        }
                    }
                }  
                else {   
                    mSharedPrefs.updateLocation(selectedLocation);
                    adapter.data = mSharedPrefs.getLocations();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final LocationInfo selectedLocation, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        selectedLocation.setValue(String.valueOf(text));
                        mSharedPrefs.updateLocation(selectedLocation);
                        adapter.data = mSharedPrefs.getLocations();
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    private void createListView() {
        locations = mSharedPrefs.getLocations();
        adapter = new LocationAdapter(this, locations, new LocationClickListener() {
            @Override
            public boolean onEnableClick(LocationInfo locationInfo, boolean checked) {
                if (locationInfo.getSwitchIdx() <= 0 && checked)
                    return showNoDeviceAttachedDialog(locationInfo);
                else {
                    //enable or disable geofences
                    locationInfo.setEnabled(checked);
                    mSharedPrefs.updateLocation(locationInfo);
                    GeoUtils.geofencesAlreadyRegistered = false;
                    oGeoUtils.AddGeofences();
                    return checked;
                }
            }

            @Override
            public void onRemoveClick(final LocationInfo locationInfo) {
                showRemoveUndoSnackbar(locationInfo);
            }
        });

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
                showEditLocationDialog(locations.get(item));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                getSwitchesAndShowSwitchesDialog(locations.get(position));
                return true;
            }
        });
    }

    private void showRemoveUndoSnackbar(final LocationInfo locationInfo) {
        // remove location from list view
        removeLocationFromListView(locationInfo);

        // Show snackbar with undo option
        String text = String.format(getString(R.string.something_deleted),
                getString(R.string.geofence));

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
                        removeLocationFromPreferences(locationInfo);
                        break;
                }
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addLocationToListView(locationInfo);
            }
        }, this.getString(R.string.undo));
    }

    private void removeLocationFromListView(LocationInfo locationInfo) {
        adapter.data.remove(locationInfo);
        adapter.notifyDataSetChanged();
    }

    private void removeLocationFromPreferences(LocationInfo locationInfo) {
        mSharedPrefs.removeLocation(locationInfo);
        GeoUtils.geofencesAlreadyRegistered = false;
        oGeoUtils.AddGeofences();
    }

    private void addLocationToListView(LocationInfo locationInfo) {
        adapter.data.add(locationInfo);
        adapter.notifyDataSetChanged();
    }

    private void getSwitchesAndShowSwitchesDialog(final LocationInfo locationInfo) {
        domoticz.getDevices(new DevicesReceiver() {
            @Override
            @DebugLog
            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                showSwitchesDialog(locationInfo, switches);
            }

            @Override
            @DebugLog
            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override
            @DebugLog
            public void onError(Exception error) {
                UsefulBits.showSnackbarWithAction(GeoSettingsActivity.this, coordinatorLayout, GeoSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getSwitchesAndShowSwitchesDialog(locationInfo);
                            }
                        }, GeoSettingsActivity.this.getString(R.string.retry));
            }
        }, 0, "all");
    }

    private boolean showNoDeviceAttachedDialog(final LocationInfo locationInfo) {
        new MaterialDialog.Builder(this)
                .title(R.string.noSwitchSelected_title)
                .content(getString(R.string.noSwitchSelected_explanation)
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + getString(R.string.noSwitchSelected_connectOneNow))
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getSwitchesAndShowSwitchesDialog(locationInfo);
                        result = true;
                    }
                })
                .show();
        return result;
    }

    private void showAddLocationDialog() {
        Intent i = new Intent(this, LocationPickerActivity.class);
        startActivityForResult(i, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            String name = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);
            if (nl.hnogames.domoticzapi.Utils.UsefulBits.isEmpty(name)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.title_edit_location)
                        .content(R.string.Location_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                String name = String.valueOf(input);
                                if (!nl.hnogames.domoticzapi.Utils.UsefulBits.isEmpty(name)) {
                                    final LocationInfo location = new LocationInfo(new Random().nextInt(999999), name,
                                            new LatLng(data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0), data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0)),
                                            500);
                                    new MaterialDialog.Builder(GeoSettingsActivity.this)
                                            .title(R.string.radius)
                                            .content(R.string.radius)
                                            .inputType(InputType.TYPE_CLASS_NUMBER)
                                            .input("500", "500", new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                                    try {
                                                        location.setRadius(Integer.parseInt(String.valueOf(input)));
                                                    } catch (Exception ex) {
                                                    }
                                                    mSharedPrefs.addLocation(location);
                                                    locations = mSharedPrefs.getLocations();

                                                    GeoUtils.geofencesAlreadyRegistered = false;
                                                    oGeoUtils.AddGeofences();

                                                    createListView();
                                                }
                                            }).show();
                                }
                            }
                        }).show();
            } else {
                final LocationInfo location = new LocationInfo(new Random().nextInt(999999), name,
                        new LatLng(data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0), data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0)),
                        500);
                new MaterialDialog.Builder(this)
                        .title(R.string.radius)
                        .content(R.string.radius)
                        .inputType(InputType.TYPE_CLASS_NUMBER)
                        .input("500", "500", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                try {
                                    location.setRadius(Integer.parseInt(String.valueOf(input)));
                                } catch (Exception ex) {
                                }
                                mSharedPrefs.addLocation(location);
                                locations = mSharedPrefs.getLocations();

                                GeoUtils.geofencesAlreadyRegistered = false;
                                oGeoUtils.AddGeofences();

                                createListView();
                            }
                        }).show();
            }
        } else
            permissionHelper.onActivityForResult(requestCode);
    }

    private void showEditLocationDialog(LocationInfo location) {
        EditLocationID = location.getID();
        Intent intent = new Intent(getApplicationContext(), LocationPickerActivity.class);
        intent.putExtra(LocationPickerActivity.LATITUDE, location.getLocation().latitude);
        intent.putExtra(LocationPickerActivity.LONGITUDE, location.getLocation().longitude);
        startActivityForResult(intent, 2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (mSharedPrefs.isGeofenceEnabled()) {
                getMenuInflater().inflate(R.menu.menu_geo, menu);
            }
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
            case R.id.action_add:
                showAddLocationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPermissionDeclined(@NonNull String[] permissionName) {
        Log.i("onPermissionDeclined", "Permission(s) " + Arrays.toString(permissionName) + " Declined");
        String[] neededPermission = PermissionHelper.declinedPermissions(GeoSettingsActivity.this, PermissionsUtil.INITIAL_LOCATION_PERMS);
        AlertDialog alert = PermissionsUtil.getAlertDialog(GeoSettingsActivity.this, permissionHelper, GeoSettingsActivity.this.getString(R.string.permission_title),
                GeoSettingsActivity.this.getString(R.string.permission_desc_location), neededPermission);
        if (!alert.isShowing()) {
            alert.show();
        }
    }

    @Override
    public void onPermissionPreGranted(@NonNull String permissionsName) {
        Log.i("onPermissionPreGranted", "Permission( " + permissionsName + " ) preGranted");
    }

    @Override
    public void onPermissionNeedExplanation(@NonNull String permissionName) {
        Log.i("NeedExplanation", "Permission( " + permissionName + " ) needs Explanation");
    }

    @Override
    public void onPermissionReallyDeclined(@NonNull String permissionName) {
        Log.i("ReallyDeclined", "Permission " + permissionName + " can only be granted from settingsScreen");
    }

    @Override
    public void onNoPermissionNeeded() {
        Log.i("onNoPermissionNeeded", "Permission(s) not needed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted(@NonNull String[] permissionName) {
        Log.i("onPermissionGranted", "Permission(s) " + Arrays.toString(permissionName) + " Granted");
        if (PermissionsUtil.canAccessLocation(GeoSettingsActivity.this)) {
            if (PermissionsUtil.canAccessStorage(GeoSettingsActivity.this)) {
                mSharedPrefs.setGeofenceEnabled(true);
                oGeoUtils.AddGeofences();
                invalidateOptionsMenu();
            } else {
                permissionHelper
                        .request(PermissionsUtil.INITIAL_STORAGE_PERMS);
            }
        }
    }
}
