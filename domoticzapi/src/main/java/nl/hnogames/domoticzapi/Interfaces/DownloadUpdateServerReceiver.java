
package nl.hnogames.domoticzapi.Interfaces;

/**
 * Receiver which returns if the update of the server is a success
 */
public interface DownloadUpdateServerReceiver {
    void onDownloadStarted(boolean updateSuccess);

    void onError(Exception error);
}