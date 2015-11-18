package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.WeatherInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.WeatherReceiver;

@SuppressWarnings("unused")
public class WeatherParser implements JSONParserInterface {

    private static final String TAG = WeatherParser.class.getSimpleName();
    private WeatherReceiver WeatherReceiver;

    public WeatherParser(WeatherReceiver Weatherreceiver) {
        this.WeatherReceiver = Weatherreceiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<WeatherInfo> mWeathers = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mWeathers.add(new WeatherInfo(row));
                }
            }

            WeatherReceiver.onReceiveWeather(mWeathers);

        } catch (JSONException e) {
            Log.e(TAG, "WeatherParser JSON exception");
            e.printStackTrace();
            WeatherReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "WeatherParser of JSONParserInterface exception");
        WeatherReceiver.onError(error);
    }
}