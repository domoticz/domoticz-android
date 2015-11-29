package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.GraphPointInfo;
import nl.hnogames.domoticz.Interfaces.GraphDataReceiver;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;

@SuppressWarnings("unused")
public class GraphDataParser implements JSONParserInterface {

    private static final String TAG = GraphDataParser.class.getSimpleName();
    private GraphDataReceiver varsReceiver;

    public GraphDataParser(GraphDataReceiver varsReceiver) {
        this.varsReceiver = varsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<GraphPointInfo> mVars = new ArrayList<>();
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mVars.add(new GraphPointInfo(row));
                }
            }

            if (mVars == null || mVars.size() <= 0)
                onError(new NullPointerException(
                        "No Data found in Domoticz."));
            else
                varsReceiver.onReceive(mVars);

        } catch (JSONException e) {
            Log.e(TAG, "GraphDataParser JSON exception");
            e.printStackTrace();
            varsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "GraphDataParser of JSONParserInterface exception");
        varsReceiver.onError(error);
    }
}