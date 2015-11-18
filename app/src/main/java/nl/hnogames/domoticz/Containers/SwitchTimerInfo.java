package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class SwitchTimerInfo {

    JSONObject jsonObject;
    int Type = 0;
    String Date;
    String Time;
    String Active;
    int idx = 0;
    int Cmd = 0;
    int Days = 0;

    public SwitchTimerInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("Date"))
            Date = row.getString("Date");
        if (row.has("Active"))
            Active = row.getString("Active");
        if (row.has("Time"))
            Active = row.getString("Time");

        if (row.has("Type"))
            Type = row.getInt("Type");
        if (row.has("Days"))
            Days = row.getInt("Days");
        if (row.has("Cmd"))
            Cmd = row.getInt("Cmd");

        idx = row.getInt("idx");
    }


    public String getDate() {
        return Date;
    }

    public String getActive() {
        return Active;
    }

    public String getTime() {
        return Time;
    }


    public int getIdx() {
        return idx;
    }

    public int getType() {
        return Type;
    }

    public int getCmd() {
        return Cmd;
    }

    public int getDays() {
        return Days;
    }

}