package nl.hnogames.domoticz;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import nl.hnogames.domoticz.Adapters.LocationAdapter;
import nl.hnogames.domoticz.Containers.LocationInfo;
import nl.hnogames.domoticz.Containers.SwitchInfo;
import nl.hnogames.domoticz.Domoticz.Domoticz;
import nl.hnogames.domoticz.Interfaces.LocationClickListener;
import nl.hnogames.domoticz.Interfaces.SwitchesReceiver;
import nl.hnogames.domoticz.Service.GeofenceTransitionsIntentService;
import nl.hnogames.domoticz.UI.LocationDialog;
import nl.hnogames.domoticz.UI.SwitchsDialog;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;


public class GeoSettingsActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "GeoSettings";
    private final int BESTAVAILABLEPROVIDERCODE = 111;
    private final int BESTPROVIDERCODE = 222;
    private final int PLACE_PICKER_REQUEST = 333;
    private GoogleMap map;
    private LocationManager locationManager;
    private SharedPrefUtil mSharedPrefs;

    private Domoticz domoticz;
    private ListView listView;

    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mApiClient;
    private List<Geofence> mGeofenceList;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PLACE_PICKER_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a place.
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.setTitle(R.string.geofence);
        domoticz=new Domoticz(this);

        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play services unavailable.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mSharedPrefs = new SharedPrefUtil(this);
        getCurrentLocationOnMapFromProvider();

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

        createListView();
    }

    public void showSwitchesDialog(final LocationInfo selectedLocation, ArrayList<SwitchInfo> switches)
    {
        SwitchsDialog infoDialog = new SwitchsDialog(
                GeoSettingsActivity.this,switches,
                R.layout.dialog_switch_logs);
        infoDialog.onDismissListener(new SwitchsDialog.DismissListener() {
            @Override
            public void onDismiss(int selectedSwitchIDX) {
                selectedLocation.setSwitchidx(selectedSwitchIDX);
                mSharedPrefs.updateLocation(GeoSettingsActivity.this, selectedLocation);

                adapter.data = mSharedPrefs.getLocations(GeoSettingsActivity.this);
                adapter.notifyDataSetChanged();
            }
        });
        infoDialog.show();
    }

    private ArrayList<LocationInfo> locations;
    private LocationAdapter adapter;

    private void createListView() {
        locations = mSharedPrefs.getLocations(this);
        mGeofenceList=new ArrayList<>();

        if(locations!=null)
        for(LocationInfo l : locations)
            if(l.getEnabled())
                mGeofenceList.add(l.toGeofence());

        adapter = new LocationAdapter(this, locations, new LocationClickListener() {
            @Override
            public void onEnableClick(LocationInfo location, boolean checked) {
                location.setEnabled(checked);
                mSharedPrefs.updateLocation(GeoSettingsActivity.this, location);
            }

            @Override
            public void onRemoveClick(final LocationInfo location) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                adapter.data.remove(location);
                                mSharedPrefs.removeLocation(GeoSettingsActivity.this, location);
                                adapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(GeoSettingsActivity.this);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                setMarker(locations.get(position).getLocation());
                domoticz.getSwitches(new SwitchesReceiver() {
                    @Override
                    public void onReceiveSwitches(ArrayList<SwitchInfo> switches) {
                        showSwitchesDialog(locations.get(position), switches);
                    }

                    @Override
                    public void onError(Exception error) {
                    }
                });
                return false;
            }
        });
    }

    public void getCurrentLocationOnMapFromProvider() {
        Criteria criteria = new Criteria();
        criteria.setSpeedRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(true);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setAltitudeRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String bestProvider = locationManager.getBestProvider(criteria, false);
        String bestAvailableProvider = locationManager.getBestProvider(criteria, true);

        if (bestProvider == null) {
        } else if (bestAvailableProvider != null && bestAvailableProvider.equals(bestAvailableProvider)) {
            boolean enabled = locationManager.isProviderEnabled(bestAvailableProvider);
            if (!enabled) {
                Toast.makeText(this, " Please enable " + bestAvailableProvider + " to find your location", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(mainIntent, BESTAVAILABLEPROVIDERCODE);
            } else {
                getLocation(bestAvailableProvider);
            }
        } else {
            boolean enabled = locationManager.isProviderEnabled(bestProvider);
            if (!enabled) {
                Toast.makeText(this, " Please enable " + bestProvider + " to find your location", Toast.LENGTH_LONG).show();
                Intent mainIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(mainIntent, BESTPROVIDERCODE);
            } else {
                getLocation(bestProvider);
            }
        }
    }

    public void getLocation(String usedLocationService) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(usedLocationService, 0, 0, this);

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        setMarker(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    private void setMarker(LatLng currentLatLng) {
        Marker currentLocation = map.addMarker(new MarkerOptions().position(currentLatLng));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
        map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mSharedPrefs.isGeofenceEnabled())
            getMenuInflater().inflate(R.menu.menu_geo, menu);

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


    public void showAddLocationDialog()
    {
        LocationDialog infoDialog = new LocationDialog(
                this,
                R.layout.dialog_location);
        infoDialog.onDismissListener(new LocationDialog.DismissListener() {
            @Override
            public void onDismiss(LocationInfo location) {
                //save location
                Log.d(TAG, "Location Added: " + location.getName());

                Marker newLocation = map.addMarker(new MarkerOptions().position(location.getLocation()));
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(location.getLocation(), 15));
                map.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                mSharedPrefs.addLocation(GeoSettingsActivity.this, location);
                locations = mSharedPrefs.getLocations(GeoSettingsActivity.this);

                mGeofenceList=new ArrayList<>();//reset values

                if(locations!=null)
                    for(LocationInfo l : locations)
                    if(l.getEnabled())
                        mGeofenceList.add(l.toGeofence());

                setGeoFenceService();
                createListView();
            }

            @Override
            public void onDismissEmpty() {
                //nothing selected
            }
        });
        infoDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location Found", location.getLatitude() + " " + location.getLongitude());
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Disabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Enabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status: " + status);
    }


    /**
     * Checks if Google Play services is available.
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
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

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onConnected(Bundle bundle) {
        setGeoFenceService();
    }

    public void setGeoFenceService() {
        if (mGeofenceList != null && mGeofenceList.size() > 0) {
        /*LocationServices.GeofencingApi.removeGeofences(
                mApiClient,
                getGeofenceTransitionPendingIntent()
        );*/

            mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
            LocationServices.GeofencingApi.addGeofences(mApiClient, mGeofenceList, mGeofenceRequestIntent);
            //Toast.makeText(this, "Starting Geofence Service", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        999);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
    }
}