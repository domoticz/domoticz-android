package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.setCommandReceiver;

@SuppressWarnings("unused")
public class setCommandParser implements JSONParserInterface {

    private static final String TAG = setCommandParser.class.getSimpleName();
    private setCommandReceiver setCommandReceiver;

    public setCommandParser(setCommandReceiver setCommandReceiver) {
        this.setCommandReceiver = setCommandReceiver;
    }

    @Override
    public void parseResult(String result) {
        setCommandReceiver.onReceiveResult(result);
    }

    @Override
    public void onError(Exception error) {
        Log.d(TAG, "setCommandParser onError");
        setCommandReceiver.onError(error);
    }
}