package nl.hnogames.domoticz.Containers;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class GraphPointInfo {

    JSONObject jsonObject;
    String dateTime;
    String hu;
    String ba;
    long te;

    public GraphPointInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("te"))
            te = row.getLong("te");
        if (row.has("d"))
            dateTime = row.getString("d");
        if (row.has("hu"))
            hu = row.getString("hu");
        if (row.has("ba"))
            ba = row.getString("ba");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new Gson().toJson(this) +
                '}';
    }


    public long getTemperature() {
        return te;
    }

    public String getHumidity() {
        return hu;
    }

    public String getBarometer() {
        return ba;
    }

    public String getDateTime() {
        return dateTime;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

}