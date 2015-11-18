package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class DevicesInfo {

    private final String UNKNOWN = "Unknown";
    private final String TAG = DevicesInfo.class.getSimpleName();
    JSONObject jsonObject;
    String SubType;
    boolean timers;
    int idx;
    String Name;
    String LastUpdate;
    long setPoint;
    String Type;
    int Favorite;
    int HardwareID;
    String HardwareName;
    String TypeImg;
    String PlanID;
    int batteryLevel;
    int maxDimLevel;
    int signalLevel;
    String status;
    int level;
    int switchTypeVal;
    String switchType;
    String Data;
    String Timers;
    boolean statusBoolean;
    boolean isProtected;

    public DevicesInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        try {
            if (row.has("LevelInt"))
                level = row.getInt("LevelInt");
        } catch (Exception e) {
            level = 0;
        }
        try {
            if (row.has("MaxDimLevel"))
                maxDimLevel = row.getInt("MaxDimLevel");
        } catch (Exception e) {
            maxDimLevel = 1;
        }

        try {
            if (row.has("Status"))
                status = row.getString("Status");
        } catch (Exception e) {
            status = "";
        }
        try {
            if (row.has("Timers"))
                Timers = row.getString("Timers");
        } catch (Exception e) {
            Timers = "False";
        }
        try {
            if (row.has("Data"))
                Data = row.getString("Data");
        } catch (Exception e) {
            status = "";
        }
        try {
            if (row.has("PlanID"))
                PlanID = row.getString("PlanID");
        } catch (Exception e) {
            PlanID = "";
        }
        try {
            if (row.has("BatteryLevel"))
                batteryLevel = row.getInt("BatteryLevel");
        } catch (Exception e) {
            batteryLevel = 0;
        }
        try {
            isProtected = row.getBoolean("Protected");
        } catch (Exception e) {
            isProtected = false;
        }
        try {
            if (row.has("SignalLevel"))
                signalLevel = row.getInt("SignalLevel");
        } catch (Exception e) {
            signalLevel = 0;
        }
        try {
            if (row.has("SwitchType"))
                switchType = row.getString("SwitchType");
        } catch (Exception e) {
            switchType = UNKNOWN;
        }
        try {
            if (row.has("SwitchTypeVal"))
                switchTypeVal = row.getInt("SwitchTypeVal");
        } catch (Exception e) {
            switchTypeVal = 999999;
        }
        if (row.has("Favorite"))
            Favorite = row.getInt("Favorite");
        if (row.has("HardwareID"))
            HardwareID = row.getInt("HardwareID");
        if (row.has("HardwareName"))
            HardwareName = row.getString("HardwareName");
        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        if (row.has("Type"))
            Type = row.getString("Type");
        if (row.has("Timers"))
            timers = row.getBoolean("Timers");
        if (row.has("SubType"))
            SubType = row.getString("SubType");

        idx = row.getInt("idx");

        try {
            signalLevel = row.getInt("SignalLevel");
        } catch (Exception ex) {
            signalLevel = 0;
        }
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = false;
        if (this.Favorite == 1) favorite = true;
        return favorite;
    }

    public String getTimers() {
        return Timers;
    }

    public String getPlanID() {
        return PlanID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getStatusBoolean() {
        try {
            boolean statusBoolean = true;

            if (status.equalsIgnoreCase("On") || status.equalsIgnoreCase("Off")) {
                if (status.equalsIgnoreCase("On")) statusBoolean = true;
                else if (status.equalsIgnoreCase("Off")) statusBoolean = false;
            } else {
                if (status.equalsIgnoreCase("Open")) statusBoolean = true;
                else if (status.equalsIgnoreCase("Closed")) statusBoolean = false;
            }

            this.statusBoolean = statusBoolean;
            return statusBoolean;
        } catch (Exception ex) {
            return false;
        }
    }

    public void setStatusBoolean(boolean status) {
        this.statusBoolean = status;
        setStatus("On");
    }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "idx=" + idx +
                ", name='" + Name + '\'' +
                ", lastUpdate='" + LastUpdate + '\'' +
                ", type='" + Type + '\'' +
                ", favorite=" + Favorite +
                ", hardwareID=" + HardwareID +
                '}';
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    ;

    public int getFavorite() {
        return Favorite;
    }

    ;

    public void setFavorite(int favorite) {
        Favorite = favorite;
    }

    ;

    public int getHardwareID() {
        return HardwareID;
    }

    public void setHardwareID(int hardwareID) {
        HardwareID = hardwareID;
    }

    public String getHardwareName() {
        return HardwareName;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getData() {
        return Data;
    }

    public String getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        LastUpdate = lastUpdate;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setIsProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public int getMaxDimLevel() {
        return maxDimLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public int getSwitchTypeVal() {
        return switchTypeVal;
    }

    public String getSwitchType() {
        return switchType;
    }
}