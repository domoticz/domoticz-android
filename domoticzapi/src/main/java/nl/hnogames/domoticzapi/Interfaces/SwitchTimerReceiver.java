
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SwitchTimerInfo;

public interface SwitchTimerReceiver {

    void onReceiveSwitchTimers(ArrayList<SwitchTimerInfo> switchTimers);

    void onError(Exception error);
}