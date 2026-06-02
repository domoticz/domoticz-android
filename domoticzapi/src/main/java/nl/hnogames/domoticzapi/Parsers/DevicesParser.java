
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.DevicesInfo;
import nl.hnogames.domoticzapi.DomoticzValues;
import nl.hnogames.domoticzapi.Interfaces.DevicesReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

public class DevicesParser implements JSONParserInterface {

    private static final String TAG = DevicesParser.class.getSimpleName();
    private DevicesReceiver receiver;
    private int idx = 999999;
    private boolean scene_or_group = false;

    public DevicesParser(DevicesReceiver receiver) {
        this.receiver = receiver;
    }

    public DevicesParser(DevicesReceiver receiver, int idx, boolean scene_or_group) {
        this.receiver = receiver;
        this.idx = idx;
        this.scene_or_group = scene_or_group;
    }

    private DevicesInfo getDevice(int idx, ArrayList<DevicesInfo> mDevicesInfo, boolean scene_or_group) {
        for (DevicesInfo s : mDevicesInfo) {
            if (s.getIdx() == idx) {
                if (scene_or_group &&
                        (s.getType().equals(DomoticzValues.Scene.Type.GROUP) || s.getType().equals(DomoticzValues.Scene.Type.SCENE))) {
                    //looking for a scene or group
                    return s;
                } else { //not looking for a scene or group
                    if (!s.getType().equals(DomoticzValues.Scene.Type.GROUP) && !s.getType().equals(DomoticzValues.Scene.Type.SCENE)) {
                        return s;
                    }
                }
            }
        }
        return null;
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

            if (idx == 999999)
                receiver.onReceiveDevices(mDevices);
            else {
                receiver.onReceiveDevice(getDevice(idx, mDevices, scene_or_group));
            }
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