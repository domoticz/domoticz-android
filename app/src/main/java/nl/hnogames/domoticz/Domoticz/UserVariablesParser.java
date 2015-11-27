package nl.hnogames.domoticz.Domoticz;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.UserVariableInfo;
import nl.hnogames.domoticz.Interfaces.JSONParserInterface;
import nl.hnogames.domoticz.Interfaces.UserVariablesReceiver;

@SuppressWarnings("unused")
public class UserVariablesParser implements JSONParserInterface {

    private static final String TAG = UserVariablesParser.class.getSimpleName();
    private UserVariablesReceiver varsReceiver;

    public UserVariablesParser(UserVariablesReceiver varsReceiver) {
        this.varsReceiver = varsReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<UserVariableInfo> mVars = new ArrayList<>();
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mVars.add(new UserVariableInfo(row));
                }
            }

            if(mVars==null || mVars.size()<=0)
                onError(new NullPointerException(
                        "No UserVariables devined in Domoticz."));
            else
                varsReceiver.onReceiveUserVariabeles(mVars);
        } catch (JSONException e) {
            Log.e(TAG, "UserVariabeleParser JSON exception");
            e.printStackTrace();
            varsReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "UserVariabeleParser of JSONParserInterface exception");
        varsReceiver.onError(error);
    }
}