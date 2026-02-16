
package nl.hnogames.domoticzapi.Interfaces;

/**
 * Receiver which returns if the update is downloaded and ready
 */
public interface UpdateDownloadReadyReceiver {

    void onUpdateDownloadReady(boolean downloadOk);

    void onError(Exception error);

}