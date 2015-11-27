package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Containers.EventXmlInfo;
import nl.hnogames.domoticz.Interfaces.EventReceiver;
import nl.hnogames.domoticz.Interfaces.EventXmlReceiver;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;

@SuppressWarnings("unused")
public class EventsXmlParser implements JSONParserInterface {

    private static final String TAG = EventsXmlParser.class.getSimpleName();
    private EventXmlReceiver varsReceiver;
    public EventsXmlParser(EventXmlReceiver varsReceiver) {
        this.varsReceiver = varsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<EventXmlInfo> mVars = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mVars.add(new EventXmlInfo(row));
                }
            }

            if(mVars==null || mVars.size()<=0)
                onError(new NullPointerException(
                        "No Event Details found in Domoticz."));
            else
                varsReceiver.onReceiveEventXml(mVars);

        } catch (JSONException e) {
            Log.e(TAG, "EventsXmlParser JSON exception");
            e.printStackTrace();
            varsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "EventsXmlParser of JSONParserInterface exception");
        varsReceiver.onError(error);
    }
}