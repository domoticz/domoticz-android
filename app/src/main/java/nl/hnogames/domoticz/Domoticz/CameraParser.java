package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.CameraInfo;
import nl.hnogames.domoticz.Interfaces.CameraReceiver;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;

@SuppressWarnings("unused")
public class CameraParser implements JSONParserInterface {

    private static final String TAG = CameraParser.class.getSimpleName();
    private CameraReceiver receiver;

    public CameraParser(CameraReceiver receiver) {
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