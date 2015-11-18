package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class SceneInfo {

    private final boolean isProtected;
    JSONObject jsonObject;

    int favorite;
    int hardwareID;
    String lastUpdate;
    String name;
    String offAction;
    String onAction;
    String status;
    Boolean timers;
    String type;
    int idx;

    public SceneInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("Favorite"))
            favorite = row.getInt("Favorite");

        isProtected = row.getBoolean("Protected");
        if (row.has("HardwareID"))
            hardwareID = row.getInt("HardwareID");
        if (row.has("LastUpdate"))
            lastUpdate = row.getString("LastUpdate");
        if (row.has("Name"))
            name = row.getString("Name");
        if (row.has("OffAction"))
            offAction = row.getString("OffAction");
        if (row.has("OnAction"))
            onAction = row.getString("OnAction");
        if (row.has("Status"))
            status = row.getString("Status");
        if (row.has("Timers"))
            timers = row.getBoolean("Timers");
        if (row.has("Type"))
            type = row.getString("Type");

        idx = row.getInt("idx");
    }

    @Override
    public String toString() {
        return "SceneInfo{" +
                "isProtected=" + isProtected +
                ", jsonObject=" + jsonObject +
                ", favorite=" + favorite +
                ", hardwareID=" + hardwareID +
                ", lastUpdate='" + lastUpdate + '\'' +
                ", name='" + name + '\'' +
                ", offAction='" + offAction + '\'' +
                ", onAction='" + onAction + '\'' +
                ", status='" + status + '\'' +
                ", timers=" + timers +
                ", type='" + type + '\'' +
                ", idx=" + idx +
                '}';
    }

    public boolean isProtected() {
        return isProtected;
    }

    public int getFavorite() {
        return favorite;
    }

    public boolean getFavoriteBoolean() {
        boolean favorite = false;
        if (this.favorite == 1) favorite = true;
        return favorite;
    }

    public void setFavoriteBoolean(boolean favorite) {
        if (favorite) this.favorite = 1;
        else this.favorite = 0;
    }

    public int getHardwareID() {
        return hardwareID;
    }

    ;

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getName() {
        return name;
    }

    public String getOffAction() {
        return offAction;
    }

    public String getOnAction() {
        return onAction;
    }

    public boolean getStatusInBoolean() {
        return status.equalsIgnoreCase("on");
    }

    public String getStatusInString() {
        return status;
    }

    public Boolean isTimers() {
        return timers;
    }

    public String getType() {
        return type;
    }

    public int getIdx() {
        return idx;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }
}