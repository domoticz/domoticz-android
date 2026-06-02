
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.UpdateDownloadReadyReceiver;

public class UpdateDownloadReadyParser implements JSONParserInterface {

    private static final String TAG = UpdateDownloadReadyParser.class.getSimpleName();
    private UpdateDownloadReadyReceiver receiver;

    public UpdateDownloadReadyParser(UpdateDownloadReadyReceiver receiver) {
        this.receiver = receiver;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            boolean updateDownloadUpdateReady;

            if (response.has("downloadok"))
                updateDownloadUpdateReady = response.getBoolean("downloadok");
            else updateDownloadUpdateReady = false;

            receiver.onUpdateDownloadReady(updateDownloadUpdateReady);
        } catch (JSONException error) {
            receiver.onError(error);
            error.printStackTrace();
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "UpdateDownloadReadyParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}