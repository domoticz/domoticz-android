
package nl.hnogames.domoticz.containers;

public class BeaconInfo {
    private boolean enabled = false;
    private String id;
    private String name;
    private int switchIdx;
    private int major;
    private int minor;
    private String switchName;
    private String switchPassword = "";
    private String value;

    private boolean isSceneOrGroup = false;

    public boolean isSceneOrGroup() {
        return isSceneOrGroup;
    }

    public void setSceneOrGroup(boolean sceneOrGroup) {
        isSceneOrGroup = sceneOrGroup;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getSwitchIdx() {
        return switchIdx;
    }

    public void setSwitchIdx(int switchIdx) {
        this.switchIdx = switchIdx;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSwitchPassword() {
        return switchPassword;
    }

    public void setSwitchPassword(String switchPassword) {
        this.switchPassword = switchPassword;
    }

    public String getSwitchName() {
        return switchName;
    }

    public void setSwitchName(String switchName) {
        this.switchName = switchName;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }
}
