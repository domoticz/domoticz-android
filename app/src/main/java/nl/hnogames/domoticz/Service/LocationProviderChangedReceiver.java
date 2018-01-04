package nl.hnogames.domoticz.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.compat.BuildConfig;
import android.util.Log;
import android.widget.Toast;

import nl.hnogames.domoticz.Utils.GeoUtils;
import nl.hnogames.domoticz.Utils.SharedPrefUtil;

public class LocationProviderChangedReceiver extends BroadcastReceiver {
    private final static String TAG = "LocationProviderChanged";

    boolean isGpsEnabled;
    boolean isNetworkEnabled;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
            Log.i(TAG, "Location Providers changed");

            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (BuildConfig.DEBUG)
                Toast.makeText(context, "GPS Enabled: " + isGpsEnabled + " Network Location Enabled: " + isNetworkEnabled, Toast.LENGTH_LONG).show();
            StartGeofenceService(context);
        }
    }

    private void StartGeofenceService(Context context) {
        SharedPrefUtil mSharedPrefUtil = new SharedPrefUtil(context);
        if (mSharedPrefUtil.isGeofenceEnabled()) {
            GeoUtils.geofencesAlreadyRegistered = false;
            new GeoUtils(context, null).AddGeofences();
        }
    }
}