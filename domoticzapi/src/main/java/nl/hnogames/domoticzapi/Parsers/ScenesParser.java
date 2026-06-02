
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SceneInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.ScenesReceiver;

public class ScenesParser implements JSONParserInterface {

    private static final String TAG = ScenesParser.class.getSimpleName();
    private ScenesReceiver scenesReceiver;
    private int idx = 999999;

    public ScenesParser(ScenesReceiver scenesReceiver) {
        this.scenesReceiver = scenesReceiver;
    }

    public ScenesParser(ScenesReceiver scenesReceiver, int idx) {
        this.scenesReceiver = scenesReceiver;
        this.idx = idx;
    }

    private SceneInfo getScene(int idx, ArrayList<SceneInfo> mSceneInfo) {
        for (SceneInfo s : mSceneInfo) {
            if (s.getIdx() == idx) {
                return s;
            }
        }
        return null;
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

            if (idx == 999999)
                scenesReceiver.onReceiveScenes(mScenes);
            else {
                scenesReceiver.onReceiveScene(getScene(idx, mScenes));
            }

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