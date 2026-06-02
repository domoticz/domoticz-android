
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.SwitchTimerReceiver;

public class SwitchTimerParser implements JSONParserInterface {

    private static final String TAG = SwitchTimerParser.class.getSimpleName();
    private SwitchTimerReceiver switchLogsReceiver;

    public SwitchTimerParser(SwitchTimerReceiver switchLogsReceiver) {
        this.switchLogsReceiver = switchLogsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<SwitchTimerInfo> mSwitchTimers = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mSwitchTimers.add(new SwitchTimerInfo(row));
                }
            }
            switchLogsReceiver.onReceiveSwitchTimers(mSwitchTimers);

        } catch (JSONException e) {
            Log.e(TAG, "ScenesParser JSON exception");
            e.printStackTrace();
            switchLogsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        //no timers found
        switchLogsReceiver.onReceiveSwitchTimers(new ArrayList<>());
    }
}