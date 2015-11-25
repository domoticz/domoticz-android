package nl.hnogames.domoticz.Interfaces;

public interface UpdateReceiver {

    void onReceiveUpdate(String version);

    void onError(Exception error);

}
