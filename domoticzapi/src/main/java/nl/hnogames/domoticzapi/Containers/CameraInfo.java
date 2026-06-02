
package nl.hnogames.domoticzapi.Containers;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CameraInfo implements Comparable, Serializable {
    private String jsonObject;
    private int devices;
    private String Name;
    private String Address;
    private String Username;
    private String Password;
    private String Port;
    private boolean Enabled;
    private String ImageURL;
    private String SnapShotURL;
    private int idx;

    public CameraInfo() {
    }

    public CameraInfo(JSONObject row) throws JSONException {
        this.jsonObject = row.toString();
        if (row.has("Name"))
            Name = row.getString("Name");
        Enabled = row.has("Enabled") &&
                row.getString("Enabled").equals("true");
        if (row.has("Address"))
            Address = row.getString("Address");
        if (row.has("Password"))
            Password = row.getString("Password");
        if (row.has("Port"))
            Port = row.getString("Port");
        if (row.has("Username"))
            Username = row.getString("Username");
        if (row.has("ImageURL"))
            ImageURL = row.getString("ImageURL");
        idx = row.getInt("idx");
    }

    @Override
    public String toString() {
        return "CameraInfo{" +
                "idx=" + idx +
                ", Name='" + Name +
                "', Address='" + Address +
                "', ImageURL='" + ImageURL +
                ", Port='" + Port +
                ", Username='" + Username +
                ", Password='" + Password +
                ", Enabled=" + Enabled +
                "'}";
    }

    public String getName() {
        return Name;
    }

    public void setName(String n) {
        Name = n;
    }

    public boolean getEnabled() {
        return Enabled;
    }

    public String getSnapShotURL() {
        return SnapShotURL;
    }

    public void setSnapShotURL(String url) {
        SnapShotURL = url;
    }

    public String getAddress() {
        return Address;
    }

    public String getTotalImageURL() {
        return "http://" + Address + ":" + Port + "/" + ImageURL;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public String getUsername() {
        return Username;
    }

    public String getPassword() {
        return Password;
    }

    public String getPort() {
        return Port;
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

    @Override
    public int compareTo(@NonNull Object another) {
        return this.getName().compareTo(((DevicesInfo) another).getName());
    }
}