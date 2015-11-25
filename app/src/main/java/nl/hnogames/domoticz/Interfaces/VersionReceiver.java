package nl.hnogames.domoticz.Interfaces;

public interface VersionReceiver {

    void onReceiveVersion(String version);

    void onError(Exception error);

}
