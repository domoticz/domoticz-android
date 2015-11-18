package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class UtilitiesInfo {

    private final boolean isProtected;
    JSONObject jsonObject;

    int idx;
    String Name;
    String Data;
    String LastUpdate;
    long setPoint;
    String Type;
    int Favorite;
    int HardwareID;
    String HardwareName;
    String TypeImg;
    int signalLevel;

    public UtilitiesInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

        if (row.has("Favorite"))
            Favorite = row.getInt("Favorite");
        if (row.has("TypeImg"))
            TypeImg = row.getString("TypeImg");
        isProtected = row.getBoolean("Protected");
        if (row.has("HardwareID"))
            HardwareID = row.getInt("HardwareID");
        if (row.has("HardwareName"))
            HardwareName = row.getString("HardwareName");
        if (row.has("LastUpdate"))
            LastUpdate = row.getString("LastUpdate");
        if (row.has("SetPoint"))
            setPoint = row.getLong("SetPoint");
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Data"))
            Data = row.getString("Data");
        if (row.has("Type"))
            Type = row.getString("Type");
        idx = row.getInt("idx");

        if (row.has("SignalLevel")) {
            try {
                signalLevel = row.getInt("SignalLevel");
            } catch (Exception ex) {
                signalLevel = 0;
            }
        }
    }

    @Override
    public String toString() {
        return "UtilitiesInfo{" +
                "isProtected=" + isProtected +
                ", jsonObject=" + jsonObject +
                ", idx=" + idx +
                ", Name='" + Name + '\'' +
                ", LastUpdate='" + LastUpdate + '\'' +
                ", setPoint=" + setPoint +
                ", Type='" + Type + '\'' +
                ", Favorite=" + Favorite +
                ", HardwareID=" + HardwareID +
                ", signalLevel=" + signalLevel +
                '}';
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTypeImg() {
        return TypeImg;
    }

    public String getData() {
        return Data;
    }

    public String getHardwareName() {
        return HardwareName;
    }

    public long getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(long setPoint) {
        this.setPoint = setPoint;
    }

    ;

    public int getFavorite() {
        return Favorite;
    }

    public void setFavorite(int favorite) {
        Favorite = favorite;
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = false;
        if (this.Favorite == 1) favorite = true;
        return favorite;
    }

    public void setFavoriteBoolean(boolean favorite) {
        if (favorite) this.Favorite = 1;
        else this.Favorite = 0;
    }

    public int getHardwareID() {
        return HardwareID;
    }

    public void setHardwareID(int hardwareID) {
        HardwareID = hardwareID;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
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
}