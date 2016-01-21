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

import android.Manifest;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Adapters.LocationAdapter;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.LocationClickListener;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.UI.LocationDialog;
import nl.hnogames.domoticz.UI.SwitchDialog;
import nl.hnogames.domoticz.Utils.GeoUtil;
import nl.hnogames.domoticz.Utils.PermissionsUtil;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;

public class GeoSettingsActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @SuppressWarnings("FieldCanBeLocal")
    private final int PLACE_PICKER_REQUEST = 333;
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
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mApiClient;
    private List<Geofence> mGeofenceList;
    private ArrayList<LocationInfo> locations;
    private LocationAdapter adapter;
    private CoordinatorLayout coordinatorLayout;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private boolean requestInProgress;
    private boolean isGeofenceServiceStarted;
    private boolean isLocationUpdatesStarted;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PLACE_PICKER_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a place.
                Place place = PlacePicker.getPlace(data, this);
                Snackbar.make(coordinatorLayout,
                        String.format(getString(R.string.geofence_place), place.getName()),
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_settings);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.geofence);

        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(
                    GeoSettingsActivity.this,
                    R.string.google_play_services_unavailable,
                    Toast.LENGTH_SHORT).show();
            // Snackbar not possible since we're ending the activity
            finish();
            return;
        }

        domoticz = new Domoticz(this);
        mSharedPrefs = new SharedPrefUtil(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        createListView();
        initSwitches();
        createLocationRequest();
    }

    private void initSwitches() {
        Switch notSwitch = (Switch) findViewById(R.id.switch_notifications_button);
        Switch geoSwitch = (Switch) findViewById(R.id.switch_button);

        geoSwitch.setChecked(mSharedPrefs.isGeofenceEnabled());
        geoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPrefs.setGeofenceEnabled(isChecked);
                invalidateOptionsMenu();
            }
        });

        notSwitch.setChecked(mSharedPrefs.isGeofenceNotificationsEnabled());
        notSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSharedPrefs.setGeofenceNotificationsEnabled(isChecked);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mApiClient.isConnected()) mApiClient.disconnect();
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

    public void showSwitchesDialog(
            final LocationInfo selectedLocation,
            ArrayList<SwitchInfo> switches) {

        SwitchDialog infoDialog = new SwitchDialog(
                GeoSettingsActivity.this, switches,
                R.layout.dialog_switch_logs,
                domoticz);

        infoDialog.onDismissListener(new SwitchDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX, String selectedSwitchPassword) {
                selectedLocation.setSwitchidx(selectedSwitchIDX);
                selectedLocation.setSwitchPassword(selectedSwitchPassword);
                mSharedPrefs.updateLocation(selectedLocation);
                adapter.data = mSharedPrefs.getLocations();
                adapter.notifyDataSetChanged();
            }
        });

        infoDialog.show();
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

        mGeofenceList = new ArrayList<>();
        if (locations != null)
            for (LocationInfo locationInfo : locations)
                if (locationInfo.getEnabled())
                    mGeofenceList.add(locationInfo.toGeofence());

        adapter = new LocationAdapter(this, locations, new LocationClickListener() {
            @Override
            public boolean onEnableClick(LocationInfo locationInfo, boolean checked) {
                if (locationInfo.getSwitchidx() <= 0 && checked)
                    return showNoDeviceAttachedDialog(locationInfo);
                else {
                    locationInfo.setEnabled(checked);
                    mSharedPrefs.updateLocation(locationInfo);
                    return checked;
                }
            }

            @Override
            public void onRemoveClick(final LocationInfo locationInfo) {
                new MaterialDialog.Builder(GeoSettingsActivity.this)
                        .title(R.string.delete)
                        .content(R.string.are_you_sure)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                removeLocation(locationInfo);
                            }
                        })
                        .show();
            }
        });

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
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

    private void removeLocation(LocationInfo locationInfo) {
        adapter.data.remove(locationInfo);
        mSharedPrefs.removeLocation(locationInfo);
        adapter.notifyDataSetChanged();
    }

    private void getSwitchesAndShowSwitchesDialog(final LocationInfo locationInfo) {
        domoticz.getSwitches(new SwitchesReceiver() {
            @Override
            public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                showSwitchesDialog(locationInfo, switches);
            }

            @Override
            public void onError(Exception error) {
                Snackbar.make(coordinatorLayout,
                        R.string.unable_to_get_switches,
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private boolean showNoDeviceAttachedDialog(final LocationInfo locationInfo) {
        new MaterialDialog.Builder(this)
                .title("No switch selected")
                .content("For GeoFencing to have effect, a switch should be connected to an GeoFence"
                        + UsefulBits.newLine()
                        + UsefulBits.newLine()
                        + "Connect one now?")
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
                    this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
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

        //noinspection ResourceType
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mApiClient, mLocationRequest, new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        //noinspection ResourceType
                        currentLocation
                                = LocationServices.FusedLocationApi.getLastLocation(mApiClient);
                    }
                });
        isLocationUpdatesStarted = true;

    }

    public void showAddLocationDialog() {
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

                mGeofenceList = new ArrayList<>();//reset values

                if (locations != null)
                    for (LocationInfo l : locations)
                        if (l.getEnabled())
                            mGeofenceList.add(l.toGeofence());

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

    public void showEditLocationDialog(LocationInfo location) {
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

                mGeofenceList = new ArrayList<>();      //reset values

                if (locations != null)
                    for (LocationInfo l : locations)
                        if (l.getEnabled())
                            mGeofenceList.add(l.toGeofence());

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

    /**
     * Checks if Google Play services is available.
     *
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        setGeoFenceService();
        startLocationUpdates();
    }

    public void setGeoFenceService() {
        checkForLocationPermission(ACTION_SET_GEOFENCE_SERVICE);
    }

    public void startGeofenceService() {
        if (mSharedPrefs.isGeofenceEnabled()) {
            if (mGeofenceList != null && mGeofenceList.size() > 0) {
                mGeofenceRequestIntent = mSharedPrefs.getGeofenceTransitionPendingIntent();
                //noinspection ResourceType
                LocationServices.GeofencingApi
                        .addGeofences(mApiClient, mGeofenceList, mGeofenceRequestIntent);
                if (domoticz.isDebugEnabled())
                    Snackbar.make(coordinatorLayout,
                            R.string.starting_geofence_service, Snackbar.LENGTH_LONG).show();
                isGeofenceServiceStarted = true;
            }
        }
    }

    public void stopGeofenceService() {
        if (mGeofenceRequestIntent != null)
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGeofenceRequestIntent != null)
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
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