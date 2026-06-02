
package nl.hnogames.domoticzapi.Containers;

import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationInfo {
    private int idx;
    private String ActiveSystems;
    private String Params;
    private String CustomMessage;
    private int Priority;
    private boolean SendAlways;

    public NotificationInfo(JSONObject row) throws JSONException {
        if (row.has("ActiveSystems"))
            setActiveSystems(row.getString("ActiveSystems"));
        if (row.has("Params"))
            setParams(row.getString("Params"));
        if (row.has("CustomMessage"))
            setCustomMessage(row.getString("CustomMessage"));
        if (row.has("Priority"))
            setPriority(row.getInt("Priority"));
        if (row.has("SendAlways"))
            setSendAlways(row.getBoolean("SendAlways"));
        setIdx(row.getInt("idx"));
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                new GsonBuilder()
                        .serializeSpecialFloatingPointValues()
                        .create()
                        .toJson(this) +
                '}';
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getActiveSystems() {
        return ActiveSystems;
    }

    public void setActiveSystems(String activeSystems) {
        ActiveSystems = activeSystems;
    }

    public String getParams() {
        return Params;
    }

    public void setParams(String params) {
        Params = params;
    }

    public String getCustomMessage() {
        return CustomMessage;
    }

    public void setCustomMessage(String customMessage) {
        CustomMessage = customMessage;
    }

    public int getPriority() {
        return Priority;
    }

    public void setPriority(int priority) {
        Priority = priority;
    }

    public boolean isSendAlways() {
        return SendAlways;
    }

    public void setSendAlways(boolean sendAlways) {
        SendAlways = sendAlways;
    }
}