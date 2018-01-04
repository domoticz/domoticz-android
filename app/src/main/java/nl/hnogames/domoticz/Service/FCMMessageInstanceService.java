package nl.hnogames.domoticz.Service;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URLDecoder;
import java.util.Map;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.Utils.DeviceUtils;
import nl.hnogames.domoticz.Utils.NotificationUtil;
import nl.hnogames.domoticz.Utils.UsefulBits;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;

import static android.text.TextUtils.isDigitsOnly;

public class FCMMessageInstanceService extends FirebaseMessagingService {

    public FCMMessageInstanceService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        if(remoteMessage == null || remoteMessage.getData() == null)
            return;

        Map data = remoteMessage.getData();
        Log.d("GCM", "Message Received: " + data.toString());

        if (data.containsKey("message")) {
            String message = decode(data.containsKey("message") ? data.get("message").toString() : "");
            String subject = decode(data.containsKey("subject") ? data.get("subject").toString() : "");
            String body = decode(data.containsKey("body") ? data.get("body").toString() : "");

            int prio = 0; //default
            if(data.containsKey("priority")) {
                String priority = decode(data.get("priority").toString());
                if (!UsefulBits.isEmpty(priority) && isDigitsOnly(priority))
                    prio = Integer.valueOf(priority);
            }

            if (!UsefulBits.isEmpty(subject) && !UsefulBits.isEmpty(body) &&
                !body.equals(subject)) {

                String deviceid = decode(data.containsKey("deviceid") ? data.get("deviceid").toString() : "");
                if (!UsefulBits.isEmpty(deviceid) && isDigitsOnly(deviceid) && Integer.valueOf(deviceid) > 0)
                    NotificationUtil.sendSimpleNotification(Integer.valueOf(deviceid), subject, body, prio, this);
                else
                    NotificationUtil.sendSimpleNotification(subject, body, prio, this);
            } else {
                NotificationUtil.sendSimpleNotification(this.getString(R.string.app_name_domoticz), message, prio, this);
            }
        }
    }

    private String decode(String str) {
        if (str != null) {
            try {
                return URLDecoder.decode(str, "UTF-8");
            } catch (Exception e) {
                Log.i("GCM", "text not decoded: " + str);
            }
        }
        return str;
    }
}
