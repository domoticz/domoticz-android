
package nl.hnogames.domoticzapi.Containers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SceneInfo implements Comparable, Serializable {
    private boolean isProtected = false;
    private String jsonObject;

    private int favorite;
    private String Description;
    private int hardwareID;
    private String lastUpdate;
    private String name;
    private String offAction;
    private String onAction;
    private String status;
    private Boolean timers;
    private String type;
    private int idx;

    public SceneInfo() {
    }

    public SceneInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has("Favorite"))
            favorite = row.getInt("Favorite");
        if (row.has("Description")) Description = row.getString("Description");

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

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
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
        boolean favorite = this.favorite == 1;
        return favorite;
    }

    public void setFavoriteBoolean(boolean favorite) {
        if (favorite) this.favorite = 1;
        else this.favorite = 0;
    }

    public int getHardwareID() {
        return hardwareID;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public Date getLastUpdateDateTime() {
        //2016-01-30 12:48:37
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return format.parse(lastUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
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

    public boolean hasTimers() {
        return timers;
    }

    public String getType() {
        return type;
    }

    public void setType(String n) {
        type = n;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int n) {
        idx = n;
    }

    public String getJsonObject() {
        return this.jsonObject;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((SceneInfo) another).getName());
    }
}