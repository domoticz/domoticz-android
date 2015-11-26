package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class UserVariableInfo {
    JSONObject jsonObject;

    int idx;
    String Type;
    String Name;
    String LastUpdate;
    String Value;

    public UserVariableInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Type"))
            Type = row.getString("Type");
        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");
        if (row.has("Value"))
            Value = row.getString("Value");
        idx = row.getInt("idx");
    }


    public String getName() {
        return Name;
    }
    public String getValue() {
        return Value;
    }
    public String getType() {
        return Type;
    }
    public String getLastUpdate() {
        return LastUpdate;
    }
    public int getIdx() {
        return idx;
    }


    @Override
    public String toString() {
        return "UserVariableInfo{" +
                "idx=" + idx +
                ", Name='" + Name +"', " +
                "Value='" + Value +"', " +
                "Type='" + Type +"', " +
                "LastUpdate='" + LastUpdate +
                "'}";
    }
}