package nl.hnogames.domoticz.Interfaces;

public interface setCommandReceiver {

    void onReceiveResult(String result);

    void onError(Exception error);

}
