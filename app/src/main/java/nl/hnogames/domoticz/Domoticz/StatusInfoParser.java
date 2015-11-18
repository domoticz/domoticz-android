package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.StatusReceiver;

@SuppressWarnings("unused")
public class StatusInfoParser implements JSONParserInterface {

    private static final String TAG = StatusInfoParser.class.getSimpleName();
    private StatusReceiver statusReceiver;

    public StatusInfoParser(StatusReceiver receiver) {
        this.statusReceiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        // Change the result data here so the view class gets the ready data

        try {

            JSONArray jsonArray = new JSONArray(result);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            statusReceiver.onReceiveStatus(new ExtendedStatusInfo(jsonObject));

        } catch (JSONException error) {
            Log.d(TAG, "StatusInfoParser onError");
            statusReceiver.onError(error);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.d(TAG, "StatusInfoParser onError");
        statusReceiver.onError(error);
    }
}