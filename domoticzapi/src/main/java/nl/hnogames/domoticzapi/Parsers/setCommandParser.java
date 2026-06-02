
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.setCommandReceiver;

public class setCommandParser implements JSONParserInterface {

    private static final String TAG = setCommandParser.class.getSimpleName();
    private setCommandReceiver setCommandReceiver;

    public setCommandParser(setCommandReceiver setCommandReceiver) {
        this.setCommandReceiver = setCommandReceiver;
    }

    @Override
    public void parseResult(String result) {
        if (setCommandReceiver != null)
            setCommandReceiver.onReceiveResult(result);
    }

    @Override
    public void onError(Exception error) {
        Log.d(TAG, "setCommandParser onError");
        if (setCommandReceiver != null)
            setCommandReceiver.onError(error);
    }
}