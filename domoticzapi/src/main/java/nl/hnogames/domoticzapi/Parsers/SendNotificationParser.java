
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.SendNotificationReceiver;

public class SendNotificationParser implements JSONParserInterface {

    private static final String TAG = SendNotificationParser.class.getSimpleName();
    private SendNotificationReceiver receiver;

    public SendNotificationParser(SendNotificationReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            if (jsonResult.has("status")) {
                if (jsonResult.getString("status").equals("OK")) {
                    receiver.onSuccess();
                    return;
                }
            }
            receiver.onError(null);
        } catch (JSONException e) {
            Log.e(TAG, "SendNotificationParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "SendNotificationParser of JSONParserInterface exception");
        receiver.onError(error);
    }

}