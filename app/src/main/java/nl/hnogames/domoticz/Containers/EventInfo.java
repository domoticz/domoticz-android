package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class EventInfo {
    JSONObject jsonObject;

    int id;
    String Name;
    String Status;

    public EventInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("name"))
            Name = row.getString("name");
        if (row.has("eventstatus"))
            Status = row.getString("eventstatus");
        id = row.getInt("id");
    }

    public String getName() {
        return Name;
    }
    public String getStatus() {
        return Status;
    }
    public int getId() {
        return id;
    }


    @Override
    public String toString() {
        return "EventInfo{" +
                "id=" + id +
                ", Name='" + Name +"', " +
                "Status='" + Status +
                "'}";
    }
}