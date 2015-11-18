package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.UtilitiesInfo;

public interface UtilitiesReceiver {

    void onReceiveUtilities(ArrayList<UtilitiesInfo> mUtilitiesInfos);

    void onError(Exception error);
}
