
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.UpdateDomoticzServerReceiver;
import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class UpdateDomoticzServerParser implements JSONParserInterface {

    private static final String TAG = UpdateDomoticzServerParser.class.getSimpleName();
    private UpdateDomoticzServerReceiver receiver;

    public UpdateDomoticzServerParser(@Nullable UpdateDomoticzServerReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        Log.d(TAG, result);
        try {
            JSONObject response = new JSONObject(result);
            Log.d(TAG, response.toString());
            boolean updateSuccess = false;
            String resultText;

            if (response.has("status")) {
                resultText = response.getString("status");
                if (!UsefulBits.isEmpty(resultText))
                    updateSuccess = true;
            } else updateSuccess = false;

            if (receiver != null) receiver.onUpdateFinish(updateSuccess);
        } catch (JSONException e) {
            if (receiver != null) receiver.onError(e);
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "VersionParser of JSONParserInterface exception");
        if (receiver != null) receiver.onError(error);
    }
}