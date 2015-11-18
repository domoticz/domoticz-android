package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchLogInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.SwitchLogReceiver;

@SuppressWarnings("unused")
public class SwitchLogParser implements JSONParserInterface {

    private static final String TAG = SwitchLogParser.class.getSimpleName();
    private SwitchLogReceiver switcheLogsReceiver;

    public SwitchLogParser(SwitchLogReceiver switcheLogsReceiver) {
        this.switcheLogsReceiver = switcheLogsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<SwitchLogInfo> mSwitcheLogs = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mSwitcheLogs.add(new SwitchLogInfo(row));
                }
            }
            switcheLogsReceiver.onReceiveSwitches(mSwitcheLogs);

        } catch (JSONException e) {
            Log.e(TAG, "ScenesParser JSON exception");
            e.printStackTrace();
            switcheLogsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "ScenesParser of JSONParserInterface exception");
        switcheLogsReceiver.onError(error);
    }
}