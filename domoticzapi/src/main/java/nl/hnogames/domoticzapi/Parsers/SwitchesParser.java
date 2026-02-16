
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SwitchInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.SwitchesReceiver;

public class SwitchesParser implements JSONParserInterface {

    private static final String TAG = SwitchesParser.class.getSimpleName();
    private SwitchesReceiver switchesReceiver;

    public SwitchesParser(SwitchesReceiver switchesReceiver) {
        this.switchesReceiver = switchesReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<SwitchInfo> mSwitches = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mSwitches.add(new SwitchInfo(row));
                }
            }

            switchesReceiver.onReceiveSwitches(mSwitches);
        } catch (JSONException e) {
            Log.e(TAG, "ScenesParser JSON exception");
            e.printStackTrace();
            switchesReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "ScenesParser of JSONParserInterface exception");
        switchesReceiver.onError(error);
    }
}