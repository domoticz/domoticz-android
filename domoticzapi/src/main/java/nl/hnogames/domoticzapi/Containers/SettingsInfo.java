
package nl.hnogames.domoticzapi.Containers;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class SettingsInfo {
    private final JSONObject jsonObject;
    private String secPassword;
    private int secOnDelay;

    public SettingsInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("SecPassword"))
            secPassword = row.getString("SecPassword");
        if (row.has("SecOnDelay"))
            secOnDelay = row.getInt("SecOnDelay");
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new Gson().toJson(this) +
                '}';
    }

    public String getSecPassword() {
        return secPassword;
    }

    public int getSecOnDelay() {
        return secOnDelay;
    }

}