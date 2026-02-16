
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.TemperatureInfo;

public interface TemperatureReceiver {

    void onReceiveTemperatures(ArrayList<TemperatureInfo> mTemperatureInfos);

    void onError(Exception error);
}