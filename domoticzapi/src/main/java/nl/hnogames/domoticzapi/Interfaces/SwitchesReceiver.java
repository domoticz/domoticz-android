
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SwitchInfo;

public interface SwitchesReceiver {
    void onReceiveSwitches(ArrayList<SwitchInfo> switches);

    void onError(Exception error);
}