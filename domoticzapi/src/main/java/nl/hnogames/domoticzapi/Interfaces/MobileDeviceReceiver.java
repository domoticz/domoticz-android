
package nl.hnogames.domoticzapi.Interfaces;

public interface MobileDeviceReceiver {
    void onSuccess();

    void onError(Exception error);
}
