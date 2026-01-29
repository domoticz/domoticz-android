
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.NotificationInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.NotificationReceiver;


public class NotificationsParser implements JSONParserInterface {

    private static final String TAG = NotificationsParser.class.getSimpleName();
    private NotificationReceiver notificationsReceiver;

    public NotificationsParser(NotificationReceiver notificationsReceiver) {
        this.notificationsReceiver = notificationsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<NotificationInfo> mNotificationInfo = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mNotificationInfo.add(new NotificationInfo(row));
                }
            }
            notificationsReceiver.onReceiveNotifications(mNotificationInfo);

        } catch (JSONException e) {
            Log.e(TAG, "JSON exception");
            e.printStackTrace();
            notificationsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "JSONParserInterface exception");
        notificationsReceiver.onError(error);
    }
}