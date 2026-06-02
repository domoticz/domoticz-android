
package nl.hnogames.domoticz.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;

import nl.hnogames.domoticz.utils.GeoUtils;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class TaskService extends GcmTaskService {

    @Override
    public void onInitializeTasks() {
        // When Google Play Services or the client app is updated,
        // all scheduled tasks are removed.
        //
        // You can override this method to reschedule them in the case of
        // an updated package. This is not called when your application is first installed.
        //
        // This is called on your application's main thread.

        // Tasks are wiped by system, clear our own flag
        new SharedPrefUtil(this).setTaskIsScheduled(false);
        UsefulBits.setScheduledTasks(this);
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        String tag = taskParams.getTag();
        if (tag.equals(UsefulBits.TASK_TAG_PERIODIC) || tag.equals("TEST")) {
            final boolean forceUpdate = true;
            // Force update
            //noinspection ConstantConditions
            UsefulBits.getServerConfigForActiveServer(this, null, null, null);
            //noinspection ConstantConditions
            UsefulBits.checkDownloadedLanguage(this, forceUpdate, true);
            resetGeofenceService(this);
        }

        return GcmNetworkManager.RESULT_SUCCESS;
    }

    private void resetGeofenceService(Context context) {
        if (new SharedPrefUtil(context).isGeofenceEnabled()) {
            GeoUtils.geofencesAlreadyRegistered = false;
            new GeoUtils(context, null).AddGeofences();
            Log.i("TASK", "Reset Geofences received, starting geofences");
        }
    }
}