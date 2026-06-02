
package nl.hnogames.domoticzapi.Containers;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticzapi.Utils.UsefulBits;

public class SwitchTimerInfo {

    JSONObject jsonObject;
    int Type = 0;
    String Date;
    String Time;
    String Active;
    int idx = 0;
    int Cmd = 0;
    int Days = 0;
    int MDay = 0;
    int Month = 0;
    int Occurence = 0;
    boolean Randomness = false;

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
        if (row.has("MDay"))
            MDay = row.getInt("MDay");
        if (row.has("Days"))
            Days = row.getInt("Days");
        if (row.has("Cmd"))
            Cmd = row.getInt("Cmd");
        if (row.has("Month"))
            Month = row.getInt("Month");
        if (row.has("Occurence"))
            Occurence = row.getInt("Occurence");
        if (row.has("Randomness"))
            Randomness = row.getBoolean("Randomness");

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

    public int getMonthDay() {
        return MDay;
    }

    public int getDays() {
        return Days;
    }

    public int getMonth() {
        return Month;
    }

    public int getOccurence() {
        return Occurence;
    }

    public char[] getDaysBinary() {
        String binary = Integer.toBinaryString(Days);
        if (!UsefulBits.isEmpty(binary))
            return binary.toCharArray();
        else return null;
    }

    public boolean getRandomness() {
        return Randomness;
    }

}