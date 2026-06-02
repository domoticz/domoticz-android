
package nl.hnogames.domoticz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import nl.hnogames.domoticz.utils.GeoUtils;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.WidgetUtils;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null)
            return;

        WidgetUtils.RefreshWidgets(context);
        startGeofenceService(context);
    }

    private void startGeofenceService(Context context) {
        SharedPrefUtil mSharedPrefUtil = new SharedPrefUtil(context);
        if (mSharedPrefUtil.isGeofenceEnabled()) {
            GeoUtils.geofencesAlreadyRegistered = false;
            new GeoUtils(context, null).AddGeofences();
            Log.i("BOOT", "Bootup received, starting geofences");
        }
    }
}