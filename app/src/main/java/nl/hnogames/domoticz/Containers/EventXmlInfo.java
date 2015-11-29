package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class EventXmlInfo {
    JSONObject jsonObject;

    int id;
    String Name;
    String Status;
    String Xmlstatement;

    public EventXmlInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
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
        if (getStatus().equals("1"))
            return true;
        else
            return false;
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
}