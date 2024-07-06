package nl.hnogames.domoticz.service;

import static android.text.TextUtils.isDigitsOnly;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.containers.NotificationInfo;
import nl.hnogames.domoticz.utils.GCMUtils;
import nl.hnogames.domoticz.utils.NotificationUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class FCMMessageInstanceService extends FirebaseMessagingService {
    private static final String TAG = "FCMMessageInstanceService";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Received onMessageReceived()");
        if (remoteMessage == null)
            return;

        Map data = remoteMessage.getData();
        Log.d(TAG, "Message Received: " + data);

        if (data.containsKey("message")) {
            String message = decode(data.containsKey("message") ? data.get("message").toString() : "");
            String subject = decode(data.containsKey("subject") ? data.get("subject").toString() : "");
            String body = decode(data.containsKey("body") ? data.get("body").toString() : "");

            int prio = 0; //default
            if (data.containsKey("priority")) {
                String priority = decode(data.get("priority").toString());
                if (!UsefulBits.isEmpty(priority) && isDigitsOnly(priority))
                    prio = Integer.valueOf(priority);
            }

            if (!UsefulBits.isEmpty(subject) && !UsefulBits.isEmpty(body) && !body.equals(subject)) {
                String deviceId = decode(data.containsKey("deviceid") ? data.get("deviceid").toString() : "");
                if (!UsefulBits.isEmpty(deviceId) && isDigitsOnly(deviceId) && Integer.valueOf(deviceId) > 0)
                    NotificationUtil.sendSimpleNotification(new NotificationInfo(Integer.valueOf(deviceId), subject, body, prio, new Date()), this);
                else
                    NotificationUtil.sendSimpleNotification(new NotificationInfo(-1, subject, body, prio, new Date()), this);
            } else
                NotificationUtil.sendSimpleNotification(new NotificationInfo(-1, this.getString(R.string.app_name_domoticz), message, prio, new Date()), this);
        }
    }

    private String decode(String str) {
        if (str != null) {
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (Exception e) {
                Log.i(TAG, "text not decoded: " + str);
            }
        }
        return str;
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        GCMUtils.sendRegistrationIdToBackend(this, token);
    }
}
