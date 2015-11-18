package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SceneInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.ScenesReceiver;

@SuppressWarnings("unused")
public class ScenesParser implements JSONParserInterface {

    private static final String TAG = ScenesParser.class.getSimpleName();
    private ScenesReceiver scenesReceiver;

    public ScenesParser(ScenesReceiver scenesReceiver) {
        this.scenesReceiver = scenesReceiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<SceneInfo> mScenes = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mScenes.add(new SceneInfo(row));
                }

            }
            scenesReceiver.onReceiveScenes(mScenes);

        } catch (JSONException e) {
            Log.e(TAG, "ScenesParser JSON exception");
            e.printStackTrace();
            scenesReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "ScenesParser of JSONParserInterface exception");
        scenesReceiver.onError(error);
    }

}