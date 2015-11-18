package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SwitchInfo;

public interface SwitchesReceiver {

    void onReceiveSwitches(ArrayList<SwitchInfo> switches);

    void onError(Exception error);
}
