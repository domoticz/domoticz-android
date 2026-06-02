
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.UserInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.UsersReceiver;

public class UsersParser implements JSONParserInterface {

    private static final String TAG = UsersParser.class.getSimpleName();
    private UsersReceiver usersReceiver;

    public UsersParser(UsersReceiver usersReceiver) {
        this.usersReceiver = usersReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            ArrayList<UserInfo> mUserInfo = new ArrayList<>();

            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject row = jsonArray.getJSONObject(i);
                    mUserInfo.add(new UserInfo(row));
                }
            }

            if (mUserInfo == null || mUserInfo.size() <= 0)
                onError(new NullPointerException(
                        "No Users found in Domoticz."));
            else
                usersReceiver.onReceiveUsers(mUserInfo);

        } catch (JSONException e) {
            Log.e(TAG, "UsersParser JSON exception");
            e.printStackTrace();
            usersReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "UsersParser of JSONParserInterface exception");
        usersReceiver.onError(error);
    }
}