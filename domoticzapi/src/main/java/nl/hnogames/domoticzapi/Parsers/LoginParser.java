
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.LoginInfo;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.LoginReceiver;

public class LoginParser implements JSONParserInterface {

    private static final String TAG = LoginParser.class.getSimpleName();
    private LoginReceiver loginReceiver;

    public LoginParser(LoginReceiver receiver) {
        this.loginReceiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            if (result == null)
                loginReceiver.OnReceive(new LoginInfo());
            else {
                JSONObject jsonObject = new JSONObject(result);
                LoginInfo info = new LoginInfo(jsonObject);
                if (info == null)
                    onError(new NullPointerException(
                            "Not logged in Domoticz."));
                else
                    loginReceiver.OnReceive(info);
            }
        } catch (Exception e) {
            Log.e(TAG, "LoginParser JSON exception");
            e.printStackTrace();
            loginReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "LoginParser of JSONParserInterface exception");
        loginReceiver.onError(error);
    }
}