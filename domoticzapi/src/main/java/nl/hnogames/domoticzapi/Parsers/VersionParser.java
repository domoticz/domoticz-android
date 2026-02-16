
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.VersionInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.VersionReceiver;

public class VersionParser implements JSONParserInterface {

    private static final String TAG = VersionParser.class.getSimpleName();
    private VersionReceiver receiver;

    public VersionParser(VersionReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            VersionInfo versionInfo = new VersionInfo(response);
            receiver.onReceiveVersion(versionInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "VersionParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}