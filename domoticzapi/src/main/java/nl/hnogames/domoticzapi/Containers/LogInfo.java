
package nl.hnogames.domoticzapi.Containers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class LogInfo implements Serializable {

    private final String jsonObject;
    private int level;
    private String message;

    public LogInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();

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

    public String getJsonObject() {
        return this.jsonObject;
    }

}