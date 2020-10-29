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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.fastaccess.permission.base.PermissionHelper;
import com.fastaccess.permission.base.callback.OnPermissionCallback;
import com.ftinc.scoop.Scoop;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.schibstedspain.leku.LocationPickerActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import nl.hnogames.domoticz.adapters.LocationAdapter;
import nl.hnogames.domoticz.app.AppCompatAssistActivity;
import nl.hnogames.domoticz.containers.LocationInfo;
import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticz.interfaces.LocationClickListener;
import nl.hnogames.domoticz.ui.SwitchDialog;
import nl.hnogames.domoticz.utils.DeviceUtils;
import nl.hnogames.domoticz.utils.GeoUtils;
import nl.hnogames.domoticz.utils.PermissionsUtil;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class GeoSettingsActivity extends AppCompatAssistActivity implements OnPermissionCallback {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String LOCATION_ADDRESS = "location_address";
    boolean result = false;
    private SharedPrefUtil mSharedPrefs;
    private ArrayList<LocationInfo> locations;
    private LocationAdapter adapter;
    private GeoUtils oGeoUtils;
    private CoordinatorLayout coordinatorLayout;
    private PermissionHelper permissionHelper;
    private SwitchMaterial geoSwitch;
    private SwitchMaterial geoNotificationSwitch;
    private int editedLocationID = -1;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPrefs = new SharedPrefUtil(this);

        // Apply Scoop to the activity
        Scoop.getInstance().apply(this);
        if (!UsefulBits.isEmpty(mSharedPrefs.getDisplayLanguage()))
            UsefulBits.setDisplayLanguage(this, mSharedPrefs.getDisplayLanguage());
        oGeoUtils = new GeoUtils(this, this);

        permissionHelper = PermissionHelper.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_settings);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.geofence);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        oGeoUtils.AddGeofences();
        createListView();
        initSwitches();
    }

    private void initSwitches() {
        geoSwitch = findViewById(R.id.switch_button);
        geoSwitch.setChecked(mSharedPrefs.isGeofenceEnabled());

        geoNotificationSwitch = findViewById(R.id.switch_notifications_button);
        geoNotificationSwitch.setChecked(mSharedPrefs.isGeofenceNotificationsEnabled());

        geoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (!PermissionsUtil.canAccessLocation(GeoSettingsActivity.this)) {
                                    geoSwitch.setChecked(false);
                                    geoNotificationSwitch.setEnabled(false);
                                    permissionHelper.request(PermissionsUtil.INITIAL_LOCATION_PERMS);
                                } else {
                                    if (!PermissionsUtil.canAccessStorage(GeoSettingsActivity.this)) {
                                        geoSwitch.setChecked(false);
                                        geoNotificationSwitch.setEnabled(false);
                                        permissionHelper.request(PermissionsUtil.INITIAL_STORAGE_PERMS);
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !PermissionsUtil.canAccessBackgroundLocation(GeoSettingsActivity.this))
                                            permissionHelper.request(PermissionsUtil.BACKGROUND_LOCATION_PERMS);
                                        else {
                                            mSharedPrefs.setGeofenceEnabled(true);
                                            geoNotificationSwitch.setEnabled(true);
                                            oGeoUtils.AddGeofences();
                                            invalidateOptionsMenu();
                                        }
                                    }
                                }
                            } else {
                                mSharedPrefs.setGeofenceEnabled(true);
                                oGeoUtils.AddGeofences();
                                invalidateOptionsMenu();
                            }
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            mSharedPrefs.setGeofenceEnabled(false);
                            geoNotificationSwitch.setEnabled(false);
                            oGeoUtils.RemoveGeofences();
                            invalidateOptionsMenu();
                            geoSwitch.setChecked(false);
                            break;
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.background_location_warning)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();

            } else {
                mSharedPrefs.setGeofenceEnabled(false);
                geoNotificationSwitch.setEnabled(false);
                oGeoUtils.RemoveGeofences();
                invalidateOptionsMenu();
            }
        });

        geoNotificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mSharedPrefs.setGeofenceNotificationsEnabled(isChecked));
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
                StaticHelper.getDomoticz(GeoSettingsActivity.this));

        infoDialog.onDismissListener((selectedSwitchIDX, selectedSwitchPassword, selectedSwitchName, isSceneOrGroup) -> {
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
            } else {
                mSharedPrefs.updateLocation(selectedLocation);
                adapter.data = mSharedPrefs.getLocations();
                adapter.notifyDataSetChanged();
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final LocationInfo selectedLocation, DevicesInfo selector) {
        final ArrayList<String> levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items(levelNames)
                .itemsCallback((dialog, view, which, text) -> {
                    selectedLocation.setValue(String.valueOf(text));
                    mSharedPrefs.updateLocation(selectedLocation);
                    adapter.data = mSharedPrefs.getLocations();
                    adapter.notifyDataSetChanged();
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

        ListView listView = findViewById(R.id.listView);
        SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(listView);
        listView.setAdapter(animationAdapter);
        listView.setOnItemClickListener((adapterView, view, item, id) -> showEditLocationDialog(locations.get(item)));

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            LocationInfo location = locations.get(position);
            if (location.getSwitchIdx() > 0) {
                location.setSwitchIdx(0);
                location.setSwitchName(null);
                location.setValue(null);
                location.setSwitchPassword(null);
                mSharedPrefs.updateLocation(location);
                UsefulBits.showSnackbar(GeoSettingsActivity.this, coordinatorLayout, R.string.switch_connection_removed, Snackbar.LENGTH_LONG);
                adapter.notifyDataSetChanged();
            } else
                getSwitchesAndShowSwitchesDialog(locations.get(position));
            return true;
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
        }, v -> addLocationToListView(locationInfo), this.getString(R.string.undo));
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
        StaticHelper.getDomoticz(GeoSettingsActivity.this).getDevices(new DevicesReceiver() {
            @Override

            public void onReceiveDevices(ArrayList<DevicesInfo> switches) {
                showSwitchesDialog(locationInfo, switches);
            }

            @Override

            public void onReceiveDevice(DevicesInfo mDevicesInfo) {
            }

            @Override

            public void onError(Exception error) {
                UsefulBits.showSnackbarWithAction(GeoSettingsActivity.this, coordinatorLayout, GeoSettingsActivity.this.getString(R.string.unable_to_get_switches), Snackbar.LENGTH_SHORT,
                        null, v -> getSwitchesAndShowSwitchesDialog(locationInfo), GeoSettingsActivity.this.getString(R.string.retry));
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
                .onPositive((dialog, which) -> {
                    getSwitchesAndShowSwitchesDialog(locationInfo);
                    result = true;
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
        final int editedID = editedLocationID; //Store the edited value locally, and delete the global value
        editedLocationID = -1;
        if (resultCode == RESULT_OK) {
            final LatLng latLng = new LatLng(data.getDoubleExtra(LATITUDE, 0), data.getDoubleExtra(LONGITUDE, 0));
            String prefillEditedName = null;

            if (editedID != -1) {
                for (LocationInfo location : locations)
                    if (location.getID() == editedID) {
                        prefillEditedName = location.getName();
                        break;
                    }

                if (prefillEditedName == null) // The ID hasn't matched any LI, so we invalidate it
                    editedLocationID = -1;
            }

            String name = data.getStringExtra(LOCATION_ADDRESS);
            if (nl.hnogames.domoticzapi.Utils.UsefulBits.isEmpty(name)) {
                new MaterialDialog.Builder(this)
                        .title(R.string.title_edit_location)
                        .content(R.string.Location_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, prefillEditedName, (dialog, input) -> {
                            String name1 = String.valueOf(input);
                            if (!nl.hnogames.domoticzapi.Utils.UsefulBits.isEmpty(name1))
                                showRadiusEditor(editedID, name1, latLng);
                        }).show();
            } else
                showRadiusEditor(editedID, name, latLng);
        } else if (resultCode != RESULT_CANCELED)
            permissionHelper.onActivityForResult(requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showRadiusEditor(final int editedLocationID, String name, LatLng latlng) {
        LocationInfo location = null;
        if (editedLocationID != -1) { // We are currently editing something
            for (LocationInfo loc : locations)
                if (loc.getID() == editedLocationID) {
                    location = loc;
                    loc.setLocation(latlng);
                    loc.setName(name);
                    break;
                }

        }

        if (location == null)
            location = new LocationInfo(new Random().nextInt(999999), name, latlng, 500);

        final LocationInfo finalLocation = location;
        new MaterialDialog.Builder(GeoSettingsActivity.this)
                .title(R.string.radius)
                .content(R.string.radius)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("500", String.valueOf(location.getRadius()), (dialog, input) -> {
                    try {
                        finalLocation.setRadius(Integer.parseInt(String.valueOf(input)));
                    } catch (Exception ex) {
                    }

                    if (editedLocationID != -1 && locations != null) {
                        mSharedPrefs.updateLocation(finalLocation);
                        for (int i = locations.size() - 1; i >= 0; i--)
                            if (locations.get(i).getID() == editedLocationID) {
                                locations.set(i, finalLocation);
                                GeoUtils.geofencesAlreadyRegistered = false;
                                oGeoUtils.AddGeofences();
                                break;
                            }
                    } else {
                        mSharedPrefs.addLocation(finalLocation);
                        locations = mSharedPrefs.getLocations();
                        adapter.data = locations;
                        GeoUtils.geofencesAlreadyRegistered = false;
                        oGeoUtils.AddGeofences();
                    }
                    adapter.notifyDataSetChanged();
                }).show();
    }

    private void showEditLocationDialog(LocationInfo location) {
        Intent intent = new Intent(getApplicationContext(), LocationPickerActivity.class);
        intent.putExtra(LATITUDE, location.getLocation().latitude);
        intent.putExtra(LONGITUDE, location.getLocation().longitude);
        editedLocationID = location.getID();
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !PermissionsUtil.canAccessBackgroundLocation(GeoSettingsActivity.this))
                    permissionHelper.request(PermissionsUtil.BACKGROUND_LOCATION_PERMS);
                else {
                    mSharedPrefs.setGeofenceEnabled(true);
                    oGeoUtils.AddGeofences();
                    invalidateOptionsMenu();
                }
            } else {
                permissionHelper
                        .request(PermissionsUtil.INITIAL_STORAGE_PERMS);
            }
        }
    }
}
