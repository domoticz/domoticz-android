package nl.hnogames.domoticz.Containers;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import nl.hnogames.domoticz.Domoticz.Domoticz;

@SuppressWarnings("unused")
public class SwitchInfo {

    private static final String UNKNOWN = "Unknown";
    private final String TAG = SwitchInfo.class.getSimpleName();
    JSONObject jsonObject;
    String IsDimmer;
    String Name;
    String SubType;
    String type;
    String TypeImg;
    int idx;
    String Timers;

    int switchTypeVal;
    String switchType;


    public SwitchInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        try {
            IsDimmer = row.getString("IsDimmer");
        } catch (Exception e) {
            exceptionHandling(e);
            IsDimmer = "False";
        }

        try {
            if (row.has("Timers"))
                Timers = row.getString("Timers");
        } catch (Exception e) {
            Timers = "False";
        }
        try {
            Name = row.getString("Name");
        } catch (Exception e) {
            exceptionHandling(e);
            Name = UNKNOWN;
        }
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        try {
            SubType = row.getString("SubType");
        } catch (Exception e) {
            exceptionHandling(e);
            SubType = UNKNOWN;
        }
        try {
            type = row.getString("Type");
        } catch (Exception e) {
            exceptionHandling(e);
            type = UNKNOWN;
        }
        try {
            idx = row.getInt("idx");
        } catch (Exception e) {
            exceptionHandling(e);
            idx = Domoticz.DOMOTICZ_FAKE_ID;
        }

        try {
            if (row.has("SwitchType"))
                switchType = row.getString("SwitchType");
        } catch (Exception e) {
            switchType = UNKNOWN;
            exceptionHandling(e);
        }
        try {
            if (row.has("SwitchTypeVal"))
                switchTypeVal = row.getInt("SwitchTypeVal");
        } catch (Exception e) {
            switchTypeVal = 999999;
            exceptionHandling(e);
        }
    }

    @Override
    public String toString() {
        return "SwitchInfo{" +
                "IsDimmer='" + IsDimmer + '\'' +
                ", name='" + Name + '\'' +
                ", SubType='" + SubType + '\'' +
                ", type='" + type + '\'' +
                ", idx=" + idx +
                '}';
    }

    public String getIsDimmerString() {
        return IsDimmer;
    }

    public boolean getIsDimmerBoolean() {
        return IsDimmer.equalsIgnoreCase("true") ? true : false;
    }

    public void setIsDimmer(String isDimmer) {
        IsDimmer = isDimmer;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSubType() {
        return SubType;
    }

    public void setSubType(String subType) {
        SubType = subType;
    }

    public String getTimers() {
        return Timers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    private void exceptionHandling(Exception error) {
        Log.e(TAG, "Exception occurred");
        error.printStackTrace();
    }


    public int getSwitchTypeVal() {
        return switchTypeVal;
    }

    public String getSwitchType() {
        return switchType;
    }
}