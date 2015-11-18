package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class PlanInfo {

    JSONObject jsonObject;

    int devices;
    String name;
    int order;
    int idx;

    public PlanInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;

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

    public int getIdx() {
        return idx;
    }

    public int getDevices() {
        return devices;
    }

    public int getOrder() {
        return order;
    }
}