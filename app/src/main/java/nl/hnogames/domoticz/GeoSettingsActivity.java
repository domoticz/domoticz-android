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
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.ArrayList;

import hugo.weaving.DebugLog;
import nl.hnogames.domoticz.Adapters.LocationAdapter;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Interfaces.LocationClickListener;
import nl.hnogames.domoticz.UI.LocationDialog;
import nl.hnogames.domoticz.UI.SwitchDialog;
import nl.hnogames.domoticz.Utils.GeoUtil;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;

public class GeoSettingsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @SuppressWarnings("FieldCanBeLocal")
    private final int LOCATION_INTERVAL = 100000;
    @SuppressWarnings("FieldCanBeLocal")
    private final int LOCATION_FASTEST_INTERVAL = 50000;
    private final int ACTION_SET_GEOFENCE_SERVICE = 11;
    private final int ACTION_GET_LOCATION = 12;
    private final int REQUEST_GEOFENCE_SERVICE = 21;
    private final int REQUEST_GET_LOCATION = 22;
    boolean result = false;
    private String TAG = GeoSettingsActivity.class.getSimpleName();
    private SharedPrefUtil mSharedPrefs;
    private Domoticz domoticz;
    private GoogleApiClient mApiClient;
    private ArrayList<LocationInfo> locations;
    private LocationAdapter adapter;
    private CoordinatorLayout coordinatorLayout;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private boolean requestInProgress;
    private boolean isGeofenceServiceStarted;
    private boolean isLocationUpdatesStarted;

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
        setContentView(R.layout.activity_geo_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.geofence);

        domoticz = new Domoticz(this, AppController.getInstance().getRequestQueue());
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        if (mSharedPrefs.darkThemeEnabled()) {
            coordinatorLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_dark));
        }
        
        createListView();
        initSwitches();
        createLocationRequest();
    }

    private void initSwitches() {
        Switch geoSwitch = (Switch) findViewById(R.id.switch_button);

        geoSwitch.setChecked(mSharedPrefs.isGeofenceEnabled());
        geoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPrefs.setGeofenceEnabled(isChecked);
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mApiClient.isConnected()) mApiClient.disconnect();

        if ((mSharedPrefs.getEnabledGeofences() == null || mSharedPrefs.getEnabledGeofences().size() <= 0) &&
                mSharedPrefs.isGeofenceEnabled()) {
            mSharedPrefs.setGeofenceEnabled(false);
            stopGeofenceService();
            Toast.makeText(this, R.string.geofencing_disabled_no_enabled_fences,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mApiClient != null) {
            mApiClient.connect();
        } else {
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mApiClient.connect();
        }
    }

    private void showSwitchesDialog(
            final LocationInfo selectedLocation,
            final ArrayList<DevicesInfo> switches) {

        SwitchDialog infoDialog = new SwitchDialog(
                GeoSettingsActivity.this, switches,
                R.layout.dialog_switch_logs,
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword, String selectedSwitchName) {
                selectedLocation.setSwitchIdx(selectedSwitchIDX);
                selectedLocation.setSwitchPassword(selectedSwitchPassword);
                selectedLocation.setSwitchName(selectedSwitchName);
                for (DevicesInfo s : switches) {
                    if (s.getIdx() == selectedSwitchIDX && s.getSwitchTypeVal() == DomoticzValues.Device.Type.Value.SELECTOR)
                        showSelectorDialog(selectedLocation, s);
                    else {
                        mSharedPrefs.updateLocation(selectedLocation);
                        adapter.data = mSharedPrefs.getLocations();
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        infoDialog.show();
    }

    private void showSelectorDialog(final LocationInfo selectedLocation, DevicesInfo selector) {
        final String[] levelNames = selector.getLevelNames();
        new MaterialDialog.Builder(this)
                .title(R.string.selector_value)
                .items((CharSequence[]) levelNames)
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
        boolean addressChanged = false;

        if (locations != null) {
            for (LocationInfo l : locations) {
                if (l.getAddress() == null && l.getLocation() != null) {
                    //load the address
                    l.setAddress(new GeoUtil(GeoSettingsActivity.this).getAddressFromLatLng(
                            new LatLng(l.getLocation().latitude, l.getLocation().longitude)));
                    addressChanged = true;
                }
            }
            if (addressChanged) mSharedPrefs.saveLocations(locations);
        }

        adapter = new LocationAdapter(this, locations, new LocationClickListener() {
            @Override
            public boolean onEnableClick(LocationInfo locationInfo, boolean checked) {
                if (locationInfo.getSwitchIdx() <= 0 && checked)
                    return showNoDeviceAttachedDialog(locationInfo);
                else {
                    locationInfo.setEnabled(checked);
                    mSharedPrefs.updateLocation(locationInfo);
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
        }, 0, "light");
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

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        requestInProgress = false;

        switch (requestCode) {
            case PermissionsUtil.INITIAL_LOCATION_REQUEST:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                    startGeofenceService();
                }
                break;

            case REQUEST_GEOFENCE_SERVICE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGeofenceService();
                    if (!isLocationUpdatesStarted) startLocationUpdates();
                } else {
                    stopGeofenceService();
                }
                break;

            case REQUEST_GET_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                    if (!isGeofenceServiceStarted) stopGeofenceService();
                }
                break;
        }
    }

    private void checkForLocationPermission(final int actionToStart) {
        if (PermissionsUtil.canAccessLocation(this)) {
            // We have permission already!
            Log.v(TAG, "We have permission, let's go!");
            switch (actionToStart) {
                case ACTION_GET_LOCATION:
                    getLocationServices();
                    break;

                case ACTION_SET_GEOFENCE_SERVICE:
                    startGeofenceService();
                    break;
            }
        } else {
            // No permission, check if the dialog has already been shown to user
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // User has declined already somewhere, we should explain why we need this
                // permission and what's in it for the user
                Log.v(TAG, "Should show request permission rationale");
                if (!requestInProgress) {

                    // Request not yet in progress: let's start!

                    requestInProgress = true;
                    String sb;
                    sb = "Geofencing in Domoticz enables you to switch based on your location" + UsefulBits.newLine();
                    sb += "For Geofencing to work, Domoticz needs to know the location of your device" + UsefulBits.newLine();
                    sb += UsefulBits.newLine();
                    sb += "Enable location permission?";
                    AlertDialog.Builder builder = new AlertDialog.Builder(GeoSettingsActivity.this);
                    builder.setTitle("Domoticz requires your permission")
                            .setMessage(sb)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    // Let's find out which request this is
                                    switch (actionToStart) {
                                        case ACTION_GET_LOCATION:
                                            ActivityCompat.requestPermissions(
                                                    GeoSettingsActivity.this,
                                                    PermissionsUtil.INITIAL_LOCATION_PERMS,
                                                    REQUEST_GET_LOCATION);
                                            break;

                                        case ACTION_SET_GEOFENCE_SERVICE:
                                            ActivityCompat.requestPermissions(
                                                    GeoSettingsActivity.this,
                                                    PermissionsUtil.INITIAL_LOCATION_PERMS,
                                                    REQUEST_GEOFENCE_SERVICE);
                                            break;
                                    }
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(GeoSettingsActivity.this,
                                            "Without location permission Domoticz cannot use Geofencing",
                                            Toast.LENGTH_SHORT).show();
                                    stopGeofenceService();
                                    GeoSettingsActivity.this.finish();
                                }
                            })
                            .show();
                }
            } else {

                if (!requestInProgress) {

                    // Request not yet in progress: let's start!

                    requestInProgress = true;

                    // Users hasn't seen the permission dialog, let show it to them

                    Log.v(TAG, "Requesting permission");

                    int requestCode;

                    switch (actionToStart) {
                        case ACTION_GET_LOCATION:
                            requestCode = REQUEST_GET_LOCATION;
                            break;

                        case ACTION_SET_GEOFENCE_SERVICE:
                            requestCode = REQUEST_GEOFENCE_SERVICE;
                            break;
                        default:
                            requestCode = PermissionsUtil.INITIAL_LOCATION_REQUEST;
                            break;

                    }
                    ActivityCompat.requestPermissions(
                            this,
                            PermissionsUtil.INITIAL_LOCATION_PERMS,
                            requestCode);
                }
            }
        }
    }

    private void startLocationUpdates() {

        checkForLocationPermission(ACTION_GET_LOCATION);
    }

    private void getLocationServices() {
        if (mApiClient.isConnected()) {
            //noinspection ResourceType
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mApiClient, mLocationRequest, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            //noinspection ResourceType
                            currentLocation
                                    = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                        }
                    });
            isLocationUpdatesStarted = true;
        }
    }

    private void showAddLocationDialog() {
        LocationDialog locationDialog = new LocationDialog(
                this,
                R.layout.dialog_location);
        locationDialog.setCurrentLocation(currentLocation);
        locationDialog.setLocationToEdit(null);             // Set to null so its in add mode
        locationDialog.onDismissListener(new LocationDialog.DismissListener() {
            @Override
            public void onDismiss(LocationInfo location) {

                mSharedPrefs.addLocation(location);
                locations = mSharedPrefs.getLocations();

                setGeoFenceService();
                createListView();
            }

            @Override
            public void onDismissEmpty() {
                //nothing selected
            }
        });
        locationDialog.show();
    }

    private void showEditLocationDialog(LocationInfo location) {
        LocationDialog locationDialog = new LocationDialog(
                this,
                R.layout.dialog_location);

        locationDialog.setTitle(getString(R.string.title_edit_location));
        locationDialog.setLocationToEdit(location);
        locationDialog.setRadius(location.getRadius());
        locationDialog.setCurrentLocation(null);            // Set to null so its in edit mode
        locationDialog.onDismissListener(new LocationDialog.DismissListener() {
            @Override
            public void onDismiss(LocationInfo location) {

                mSharedPrefs.updateLocation(location);
                locations = mSharedPrefs.getLocations();

                setGeoFenceService();
                createListView();
            }

            @Override
            public void onDismissEmpty() {
                //nothing selected
            }
        });
        locationDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            if (mSharedPrefs.isGeofenceEnabled())
                getMenuInflater().inflate(R.menu.menu_geo, menu);
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
    public void onConnected(Bundle bundle) {
        setGeoFenceService();
        startLocationUpdates();
    }

    private void setGeoFenceService() {
        checkForLocationPermission(ACTION_SET_GEOFENCE_SERVICE);
    }

    private void startGeofenceService() {
        if (mSharedPrefs.isGeofenceEnabled()) {
            mSharedPrefs.enableGeoFenceService();
            if (mSharedPrefs.isDebugEnabled())
                UsefulBits.showSnackbar(this, coordinatorLayout, R.string.starting_geofence_service, Snackbar.LENGTH_SHORT);
            isGeofenceServiceStarted = true;
        }
    }

    private void stopGeofenceService() {
        if (mApiClient != null) mSharedPrefs.stopGeofenceService();
        isGeofenceServiceStarted = false;
    }

    @Override
    public void onConnectionSuspended(int i) {
        PendingIntent mGeofenceRequestIntent = mSharedPrefs.getGeofenceTransitionPendingIntent();

        if (mGeofenceRequestIntent != null && mApiClient != null)
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        isGeofenceServiceStarted = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 999);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
    }
}