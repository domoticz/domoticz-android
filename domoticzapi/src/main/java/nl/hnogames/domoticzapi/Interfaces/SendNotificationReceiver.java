
package nl.hnogames.domoticzapi.Interfaces;

public interface SendNotificationReceiver {
    void onSuccess();

    void onError(Exception error);
}
