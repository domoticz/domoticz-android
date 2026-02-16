
package nl.hnogames.domoticzapi.Containers;

import org.json.JSONException;
import org.json.JSONObject;

public class Language {

    private JSONObject jsonObject;

    public Language(JSONObject row) throws JSONException {
        mapFields(row);
    }

    private void mapFields(JSONObject row) throws JSONException {
        this.jsonObject = row;

    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}