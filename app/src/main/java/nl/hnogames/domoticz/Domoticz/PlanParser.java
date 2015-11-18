package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.PlanInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.PlansReceiver;

@SuppressWarnings("unused")
public class PlanParser implements JSONParserInterface {

    private static final String TAG = PlanParser.class.getSimpleName();
    private PlansReceiver receiver;

    public PlanParser(PlansReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {

        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<PlanInfo> mPlans = new ArrayList<>();

            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mPlans.add(new PlanInfo(row));
                }

            }
            receiver.OnReceivePlans(mPlans);

        } catch (JSONException e) {
            Log.e(TAG, "PlanParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "PlanParser of JSONParserInterface exception");
        receiver.onError(error);
    }

}