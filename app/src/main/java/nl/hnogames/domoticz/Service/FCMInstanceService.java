package nl.hnogames.domoticz.Service;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import nl.hnogames.domoticz.Utils.GCMUtils;

public class FCMInstanceService extends FirebaseInstanceIdService {
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("GCM", "Refreshed token: " + refreshedToken);
        GCMUtils.sendRegistrationIdToBackend(this, refreshedToken);
    }
}