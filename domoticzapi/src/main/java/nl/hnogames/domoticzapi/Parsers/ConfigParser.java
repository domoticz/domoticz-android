
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.ConfigInfo;
import nl.hnogames.domoticzapi.Interfaces.ConfigReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

public class ConfigParser implements JSONParserInterface {

    private static final String TAG = ConfigParser.class.getSimpleName();
    private ConfigReceiver receiver;

    public ConfigParser(ConfigReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            ConfigInfo ConfigInfo = new ConfigInfo(new JSONObject(result));
            receiver.onReceiveConfig(ConfigInfo);
        } catch (JSONException e) {
            Log.e(TAG, "ConfigParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "SettingsParser of JSONParserInterface exception");
        if (receiver != null)
            receiver.onError(error);
    }
}