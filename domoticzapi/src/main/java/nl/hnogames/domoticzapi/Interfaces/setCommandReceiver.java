
package nl.hnogames.domoticzapi.Interfaces;

public interface setCommandReceiver {

    void onReceiveResult(String result);

    void onError(Exception error);

}