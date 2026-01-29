
package nl.hnogames.domoticzapi.Containers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class EventXmlInfo implements Comparable, Serializable {
    private final String jsonObject;
    private final int id;
    private String Name;
    private String Status;
    private String Xmlstatement;

    public EventXmlInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has("name"))
            Name = row.getString("name");
        if (row.has("eventstatus"))
            Status = row.getString("eventstatus");
        if (row.has("xmlstatement"))
            Xmlstatement = row.getString("xmlstatement");
        id = row.getInt("id");
    }

    public String getName() {
        return Name;
    }

    public String getStatus() {
        return Status;
    }

    public String getXmlstatement() {
        return Xmlstatement;
    }

    public boolean getStatusBoolean() {
        return getStatus().equals("1");
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "EventXmlInfo{" +
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