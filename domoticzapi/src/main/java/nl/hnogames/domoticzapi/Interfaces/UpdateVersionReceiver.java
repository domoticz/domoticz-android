
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.ServerUpdateInfo;

/**
 * Receiver which returns the version and if there is a update available
 */
public interface UpdateVersionReceiver {

    void onReceiveUpdate(ServerUpdateInfo serverUpdateInfo);

    void onError(Exception error);

}