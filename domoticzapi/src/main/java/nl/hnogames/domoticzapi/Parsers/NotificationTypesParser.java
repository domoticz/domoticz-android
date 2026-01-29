
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.NotificationTypeInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.NotificationTypesReceiver;

public class NotificationTypesParser implements JSONParserInterface {
    private static final String TAG = NotificationTypesParser.class.getSimpleName();
    private NotificationTypesReceiver receiver;

    public NotificationTypesParser(NotificationTypesReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("notifiers");
            ArrayList<NotificationTypeInfo> mTypes = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    mTypes.add(new NotificationTypeInfo(jsonArray.getJSONObject(i)));
                }
            }

            receiver.onReceive(mTypes);
        } catch (JSONException e) {
            Log.e(TAG, "NotificationTypesParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "NotificationTypesParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}