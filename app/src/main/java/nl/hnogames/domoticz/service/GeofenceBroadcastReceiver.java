package nl.hnogames.domoticz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     *                Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(GeofenceBroadcastReceiver.class.getSimpleName(), "Received geofence event.");
        // Enqueues a JobIntentService passing the context and intent as parameters
        GeofenceTransitionsIntentService.enqueueWork(context, intent);
    }
}