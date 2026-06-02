
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.UpdateVersionReceiver;

public class UpdateVersionParser implements JSONParserInterface {

    private static final String TAG = UpdateVersionParser.class.getSimpleName();
    private UpdateVersionReceiver receiver;

    public UpdateVersionParser(UpdateVersionReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            ServerUpdateInfo serverUpdateInfo = new ServerUpdateInfo(response);

            receiver.onReceiveUpdate(serverUpdateInfo);
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