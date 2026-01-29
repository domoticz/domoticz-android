
package nl.hnogames.domoticzapi.Containers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class PlanInfo implements Serializable {

    private String jsonObject;
    private int devices;
    private String name;
    private int order;
    private int idx;

    public PlanInfo() {
    }

    public PlanInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();

        devices = row.getInt("Devices");
        name = row.getString("Name");
        order = row.getInt("Order");
        idx = row.getInt("idx");
    }

    @Override
    public String toString() {
        return "PlanInfo{" +
                "idx=" + idx +
                ", order='" + order +
                "', name='" + name +
                "', devices='" + devices +
                "', json='" + jsonObject +
                "'}";
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int n) {
        idx = n;
    }

    public int getDevices() {
        return devices;
    }

    public int getOrder() {
        return order;
    }
}