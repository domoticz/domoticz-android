package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.TemperatureInfo;

public interface TemperatureReceiver {

    void onReceiveTemperatures(ArrayList<TemperatureInfo> mTemperatureInfos);

    void onError(Exception error);
}
