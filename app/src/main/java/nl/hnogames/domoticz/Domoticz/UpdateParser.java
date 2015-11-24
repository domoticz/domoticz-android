package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.UpdateReceiver;
import nl.hnogames.domoticz.Interfaces.VersionReceiver;

public class UpdateParser implements JSONParserInterface {

    private static final String TAG = UpdateParser.class.getSimpleName();
    private UpdateReceiver receiver;

    public UpdateParser(UpdateReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            String version = "";
            if(response.has("revision"))
                version= response.getString("revision");
            if(response.has("HaveUpdate") && !response.getBoolean("HaveUpdate"))
                version= "";

            receiver.onReceiveUpdate(version);
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