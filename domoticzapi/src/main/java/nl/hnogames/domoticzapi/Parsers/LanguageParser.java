
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.Language;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.LanguageReceiver;


public class LanguageParser implements JSONParserInterface {

    private static final String TAG = LanguageParser.class.getSimpleName();
    private LanguageReceiver receiver;

    public LanguageParser(LanguageReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            Language language = new Language(new JSONObject(result));
            receiver.onReceiveLanguage(language);
        } catch (JSONException e) {
            Log.e(TAG, "LanguageParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "LanguageParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}