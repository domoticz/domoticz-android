
package nl.hnogames.domoticzapi.Containers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class UserVariableInfo implements Comparable, Serializable {
    String jsonObject;

    int idx;
    String Type;
    String Name;
    String LastUpdate;
    String Value;

    public UserVariableInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
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

    public String getTypeValue() {
        switch (getType()) {
            case "0":
                return "Integer";
            case "1":
                return "Float";
            case "2":
                return "String";
            case "3":
                return "Date";
            case "4":
                return "Time";
        }
        return null;
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
                ", Name='" + Name + "', " +
                "Value='" + Value + "', " +
                "Type='" + Type + "', " +
                "LastUpdate='" + LastUpdate +
                "'}";
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}