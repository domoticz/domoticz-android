
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SwitchLogInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.SwitchLogReceiver;

public class SwitchLogParser implements JSONParserInterface {

    private static final String TAG = SwitchLogParser.class.getSimpleName();
    private SwitchLogReceiver switchLogReceiver;

    public SwitchLogParser(SwitchLogReceiver switchLogReceiver) {
        this.switchLogReceiver = switchLogReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<SwitchLogInfo> mSwitchLogs = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mSwitchLogs.add(new SwitchLogInfo(row));
                }
            }
            switchLogReceiver.onReceiveSwitches(mSwitchLogs);

        } catch (JSONException e) {
            Log.e(TAG, "ScenesParser JSON exception");
            e.printStackTrace();
            switchLogReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "ScenesParser of JSONParserInterface exception");
        switchLogReceiver.onError(error);
    }
}