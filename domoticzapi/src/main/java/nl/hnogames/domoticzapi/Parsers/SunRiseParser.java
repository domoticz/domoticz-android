
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.SunRiseInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.SunRiseReceiver;

public class SunRiseParser implements JSONParserInterface {
    private static final String TAG = SunRiseParser.class.getSimpleName();
    private SunRiseReceiver sunriseReceiver;

    public SunRiseParser(SunRiseReceiver sunriseReceiver) {
        this.sunriseReceiver = sunriseReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            sunriseReceiver.onReceive(new SunRiseInfo(new JSONObject(result)));
        } catch (JSONException e) {
            Log.e(TAG, "SunRiseParser JSON exception");
            e.printStackTrace();
            sunriseReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "SunRiseParser of JSONParserInterface exception");
        sunriseReceiver.onError(error);
    }
}