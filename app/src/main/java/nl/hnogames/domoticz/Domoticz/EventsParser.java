package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Containers.UserVariableInfo;
import nl.hnogames.domoticz.Interfaces.EventReceiver;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;

@SuppressWarnings("unused")
public class EventsParser implements JSONParserInterface {

    private static final String TAG = EventsParser.class.getSimpleName();
    private EventReceiver varsReceiver;
    public EventsParser(EventReceiver varsReceiver) {
        this.varsReceiver = varsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<EventInfo> mVars = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mVars.add(new EventInfo(row));
                }
            }
            varsReceiver.onReceiveEvents(mVars);
        } catch (JSONException e) {
            Log.e(TAG, "UserVariabeleParser JSON exception");
            e.printStackTrace();
            varsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "UserVariabeleParser of JSONParserInterface exception");
        varsReceiver.onError(error);
    }
}