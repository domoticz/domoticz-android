package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.LogInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.LogsReceiver;

@SuppressWarnings("unused")
public class LogsParser implements JSONParserInterface {

    private static final String TAG = LogsParser.class.getSimpleName();
    private LogsReceiver logsReceiver;

    public LogsParser(LogsReceiver logsReceiver) {
        this.logsReceiver = logsReceiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<LogInfo> mlogs = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);

                    mlogs.add(new LogInfo(row));
                }
            }

            logsReceiver.onReceiveLogs(mlogs);

        } catch (JSONException e) {
            Log.e(TAG, "logsParser JSON exception");
            e.printStackTrace();
            logsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "logsParser of JSONParserInterface exception");
        logsReceiver.onError(error);
    }

}