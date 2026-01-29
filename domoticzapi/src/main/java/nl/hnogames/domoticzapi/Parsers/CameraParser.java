
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.CameraInfo;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Interfaces.CameraReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

public class CameraParser implements JSONParserInterface {
    private static final String TAG = CameraParser.class.getSimpleName();
    private CameraReceiver receiver;
    private Domoticz domoticz;

    public CameraParser(CameraReceiver receiver, Domoticz domoticz) {
        this.domoticz = domoticz;
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<CameraInfo> mCameras = new ArrayList<>();
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    CameraInfo cameraInfo = new CameraInfo(row);
                    cameraInfo.setSnapShotURL(this.domoticz.getSnapshotUrl(cameraInfo));
                    if (cameraInfo.getEnabled())
                        mCameras.add(cameraInfo);
                }
            }
            receiver.OnReceiveCameras(mCameras);
        } catch (JSONException e) {
            Log.e(TAG, "CameraParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "CameraParser of JSONParserInterface exception");
        receiver.onError(error);
    }
}