package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchLogInfo;

public interface SwitchLogReceiver {

    void onReceiveSwitches(ArrayList<SwitchLogInfo> switcheLogs);

    void onError(Exception error);
}
