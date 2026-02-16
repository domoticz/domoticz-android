
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.ConfigInfo;

public interface ConfigReceiver {
    void onReceiveConfig(ConfigInfo settings);

    void onError(Exception error);
}
