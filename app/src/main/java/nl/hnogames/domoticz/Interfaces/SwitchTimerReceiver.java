package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchTimerInfo;

public interface SwitchTimerReceiver {

    void onReceiveSwitchTimers(ArrayList<SwitchTimerInfo> switchTimers);

    void onError(Exception error);
}
