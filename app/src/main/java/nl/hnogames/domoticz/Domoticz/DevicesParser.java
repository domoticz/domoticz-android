package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.DevicesInfo;
import nl.hnogames.domoticz.Interfaces.DevicesReceiver;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;

public class DevicesParser implements JSONParserInterface {

    private static final String TAG = DevicesParser.class.getSimpleName();
    private DevicesReceiver receiver;

    public DevicesParser(DevicesReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<DevicesInfo> mDevices = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mDevices.add(new DevicesInfo(row));
                }
            }

            receiver.onReceiveDevices(mDevices);

        } catch (JSONException e) {
            Log.e(TAG, "DevicesParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "DevicesParser of JSONParserInterface exception");
        receiver.onError(error);
    }

}