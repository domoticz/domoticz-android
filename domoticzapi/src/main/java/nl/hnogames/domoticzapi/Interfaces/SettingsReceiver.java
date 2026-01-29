
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.SettingsInfo;

public interface SettingsReceiver {
    void onReceiveSettings(SettingsInfo settings);

    void onError(Exception error);
}
