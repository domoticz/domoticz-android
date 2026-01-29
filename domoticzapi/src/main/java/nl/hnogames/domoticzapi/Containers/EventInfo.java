
package nl.hnogames.domoticzapi.Containers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class EventInfo implements Comparable, Serializable {
    @SuppressWarnings({"SpellCheckingInspection", "FieldCanBeLocal"})
    private final String EVENT_STATUS = "eventstatus";
    @SuppressWarnings("FieldCanBeLocal")
    private final String NAME = "name";
    private final String jsonObject;
    private final int id;
    private String Name;
    private String Status;

    public EventInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has(NAME))
            Name = row.getString(NAME);
        if (row.has(EVENT_STATUS))
            Status = row.getString(EVENT_STATUS);
        id = row.getInt("id");
    }

    public String getName() {
        return Name;
    }

    public String getStatus() {
        return Status;
    }

    public boolean getStatusBoolean() {
        return getStatus().equals("1");
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "EventInfo{" +
                "id=" + id +
                ", Name='" + Name + "', " +
                "Status='" + Status +
                "'}";
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}