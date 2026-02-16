
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import nl.hnogames.domoticzapi.Interfaces.DownloadUpdateServerReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class DownloadUpdateParser implements JSONParserInterface {

    private static final String TAG = DownloadUpdateParser.class.getSimpleName();
    private DownloadUpdateServerReceiver receiver;

    public DownloadUpdateParser(DownloadUpdateServerReceiver receiver) {
        this.receiver = receiver;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void parseResult(String result) {
        try {
            if (!UsefulBits.isEmpty(result) && result.contains("ERR"))
                receiver.onDownloadStarted(false);
            else
                receiver.onDownloadStarted(true);
        } catch (Exception error) {
            receiver.onError(error);
            error.printStackTrace();
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "DownloadUpdateParser of Exception");
        receiver.onError(error);
    }
}