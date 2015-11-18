package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.UtilitiesInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.UtilitiesReceiver;

@SuppressWarnings("unused")
public class UtilitiesParser implements JSONParserInterface {

    private static final String TAG = UtilitiesParser.class.getSimpleName();
    private UtilitiesReceiver utilitiesReceiver;

    public UtilitiesParser(UtilitiesReceiver utilitiesReceiver) {
        this.utilitiesReceiver = utilitiesReceiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<UtilitiesInfo> mUtilities = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);

                    mUtilities.add(new UtilitiesInfo(row));
                }
            }

            utilitiesReceiver.onReceiveUtilities(mUtilities);

        } catch (JSONException e) {
            Log.e(TAG, "UtilitiesParser JSON exception");
            e.printStackTrace();
            utilitiesReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "UtilitiesParser of JSONParserInterface exception");
        utilitiesReceiver.onError(error);
    }

}