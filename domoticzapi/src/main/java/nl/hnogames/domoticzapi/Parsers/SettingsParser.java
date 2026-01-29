
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.SettingsInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.SettingsReceiver;

public class SettingsParser implements JSONParserInterface {

    private static final String TAG = SettingsParser.class.getSimpleName();
    private SettingsReceiver receiver;

    public SettingsParser(SettingsReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            SettingsInfo SettingsInfo = new SettingsInfo(jsonObject);
            receiver.onReceiveSettings(SettingsInfo);
        } catch (JSONException e) {
            Log.e(TAG, "SettingsParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "SettingsParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}