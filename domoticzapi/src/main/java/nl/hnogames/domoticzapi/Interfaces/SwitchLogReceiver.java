
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SwitchLogInfo;

public interface SwitchLogReceiver {

    void onReceiveSwitches(ArrayList<SwitchLogInfo> switcheLogs);

    void onError(Exception error);
}