package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.TemperatureInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.TemperatureReceiver;

@SuppressWarnings("unused")
public class TemperaturesParser implements JSONParserInterface {

    private static final String TAG = TemperaturesParser.class.getSimpleName();
    private TemperatureReceiver temperatureReceiver;

    public TemperaturesParser(TemperatureReceiver temperaturereceiver) {
        this.temperatureReceiver = temperaturereceiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<TemperatureInfo> mTemperatures = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mTemperatures.add(new TemperatureInfo(row));
                }
            }

            temperatureReceiver.onReceiveTemperatures(mTemperatures);

        } catch (JSONException e) {
            Log.e(TAG, "TemperatureParser JSON exception");
            e.printStackTrace();
            temperatureReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "TemperatureParser of JSONParserInterface exception");
        temperatureReceiver.onError(error);
    }
}