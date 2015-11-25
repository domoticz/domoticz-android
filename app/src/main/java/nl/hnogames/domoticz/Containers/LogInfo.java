package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class LogInfo {

    JSONObject jsonObject;
    int level;
    String message;

    public LogInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("level"))
            level = row.getInt("level");
        if (row.has("message"))
            message = row.getString("message");

    }

    @Override
    public String toString() {
        return "LogInfo{" +
                "level=" + level +
                ", message='" + message + '}';
    }

    public int getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }


    public JSONObject getJsonObject() {
        return this.jsonObject;
    }
}