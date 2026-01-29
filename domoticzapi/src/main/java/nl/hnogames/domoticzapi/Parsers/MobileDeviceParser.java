
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;
import nl.hnogames.domoticzapi.Interfaces.MobileDeviceReceiver;

public class MobileDeviceParser implements JSONParserInterface {

    private static final String TAG = MobileDeviceParser.class.getSimpleName();
    private MobileDeviceReceiver mobileReceiver;

    public MobileDeviceParser(MobileDeviceReceiver mobileReceiver) {
        this.mobileReceiver = mobileReceiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            JSONObject jsonResult = new JSONObject(result);
            if (jsonResult.has("status")) {
                if (jsonResult.getString("status").equals("OK")) {
                    mobileReceiver.onSuccess();
                    return;
                }
            }
            mobileReceiver.onError(null);
        } catch (JSONException e) {
            Log.e(TAG, "MobileDeviceParser JSON exception");
            e.printStackTrace();
            mobileReceiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "MobileDeviceParser of JSONParserInterface exception");
        mobileReceiver.onError(error);
    }

}