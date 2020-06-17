package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.util.Log;

import nl.hnogames.domoticz.helpers.StaticHelper;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;

/**
 * Created by heini on 08/11/2017.
 */

public class GCMUtils {
    public static void sendRegistrationIdToBackend(final Context context, final String sender_id) {
        final String UUID = DeviceUtils.getUniqueID(context);
        if (UsefulBits.isEmpty(sender_id) || UsefulBits.isEmpty(UUID))
            return;

        StaticHelper.getDomoticz(context).CleanMobileDevice(UUID, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                // Previous id cleaned
                registerMobileForGCM(context, UUID, sender_id);
            }

            @Override
            public void onError(Exception error) {
                // Nothing to clean
                registerMobileForGCM(context, UUID, sender_id);
            }
        });
    }

    private static void registerMobileForGCM(Context context, String UUID, String senderid) {
        StaticHelper.getDomoticz(context).AddMobileDevice(UUID, senderid, new MobileDeviceReceiver() {
            @Override
            public void onSuccess() {
                Log.i("GCM", "Device registered on Domoticz");
            }

            @Override
            public void onError(Exception error) {
                if (error != null)
                    Log.i("GCM", "Device not registered on Domoticz, " + error.getMessage());
            }
        });
    }
}
