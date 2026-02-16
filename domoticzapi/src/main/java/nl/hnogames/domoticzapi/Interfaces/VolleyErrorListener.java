
package nl.hnogames.domoticzapi.Interfaces;

import org.json.JSONObject;

public interface VolleyErrorListener {
    void onDone(JSONObject response);

    void onError(Exception error);
}