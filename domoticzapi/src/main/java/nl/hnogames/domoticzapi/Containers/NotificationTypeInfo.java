
package nl.hnogames.domoticzapi.Containers;

import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationTypeInfo {
    private String name;
    private String description;

    public NotificationTypeInfo(JSONObject row) throws JSONException {
        if (row.has("name"))
            name = (row.getString("name"));
        if (row.has("description"))
            name = (row.getString("description"));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new GsonBuilder()
                        .serializeSpecialFloatingPointValues()
                        .create()
                        .toJson(this) +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}