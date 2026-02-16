
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.VersionInfo;

/**
 * Receiver which returns the version of the Domoticz server
 */
public interface VersionReceiver {
    void onReceiveVersion(VersionInfo version);

    void onError(Exception error);
}