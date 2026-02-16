
package nl.hnogames.domoticzapi.Parsers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Containers.EnergyDashboardInfo;
import nl.hnogames.domoticzapi.Interfaces.EnergyDashboardReceiver;
import nl.hnogames.domoticzapi.Interfaces.JSONParserInterface;

/**
 * Parser for energy dashboard configuration data
 */
public class EnergyDashboardParser implements JSONParserInterface {

    private static final String TAG = EnergyDashboardParser.class.getSimpleName();
    private EnergyDashboardReceiver receiver;

    public EnergyDashboardParser(EnergyDashboardReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void parseResult(String result) {
        try {
            EnergyDashboardInfo energyDashboard = new EnergyDashboardInfo(new JSONObject(result));
            receiver.onReceiveEnergyDashboard(energyDashboard);
        } catch (JSONException e) {
            Log.e(TAG, "EnergyDashboardParser JSON exception");
            e.printStackTrace();
            receiver.onError(e);
        }
    }

    @Override
    public void onError(Exception error) {
        Log.e(TAG, "EnergyDashboardParser of JSONParserInterface exception");
        if (receiver != null)
            receiver.onError(error);
    }
}

