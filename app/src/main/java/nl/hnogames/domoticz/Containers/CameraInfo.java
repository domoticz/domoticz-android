package nl.hnogames.domoticz.Containers;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class CameraInfo {
    JSONObject jsonObject;
    int devices;
    String Name;
    String Address;
    String Username;
    String Password;
    String Port;
    boolean Enabled;
    String ImageURL;
    int idx;

    public CameraInfo(JSONObject row) throws JSONException {
        this.jsonObject = row;
        if (row.has("Name"))
            Name = row.getString("Name");
        if (row.has("Enabled") &&
                row.getString("Enabled").equals("true"))
            Enabled = true;
        else
            Enabled = false;
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
                ", FullURL='" + getFullURL() +
                ", Enabled=" + Enabled +
                "'}";
    }


    public String getName() {
        return Name;
    }

    public boolean getEnabled() {
        return Enabled;
    }

    public String getAddress() {
        return Address;
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

    public int getDevices() {
        return devices;
    }


    public String getFullURL() {
        String combinedUserName = "";
        if (getUsername() != null && getUsername().length() > 0) {
            combinedUserName += getUsername();
            if (getPassword() != null && getPassword().length() > 0)
                combinedUserName += ":" + getPassword();
        }

        String combinedUrl = "";
        if (combinedUserName.length() > 0)
            combinedUrl = combinedUserName + "@";

        if (getAddress() != null && !getAddress().startsWith("http://"))
            combinedUrl = "http://" + combinedUrl;

        combinedUrl += getAddress();

        if (getPort() != null && getPort().length() > 0)
            combinedUrl += ":" + getPort();

        combinedUrl += "/" + getImageURL();
        return combinedUrl;
    }
}