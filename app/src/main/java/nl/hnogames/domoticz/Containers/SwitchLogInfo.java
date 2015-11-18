package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class SwitchLogInfo {
    JSONObject jsonObject;
    int MaxDimLevel = 0;
    int Level = 0;
    String Status;
    String Date;
    String Data;
    int idx;

    public SwitchLogInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("Status"))
            Status = row.getString("Status");
        if (row.has("Date"))
            Date = row.getString("Date");
        if (row.has("Data"))
            Data = row.getString("Data");
        if (row.has("MaxDimLevel"))
            MaxDimLevel = row.getInt("MaxDimLevel");
        if (row.has("Level"))
            Level = row.getInt("Level");

        idx = row.getInt("idx");
    }

    public String getData() {
        return Data;
    }

    public String getStatus() {
        return Status;
    }

    public String getDate() {
        return Date;
    }

    public int getIdx() {
        return idx;
    }

    public int getLevel() {
        return Level;
    }

    public int getMaxDimLevel() {
        return MaxDimLevel;
    }

}