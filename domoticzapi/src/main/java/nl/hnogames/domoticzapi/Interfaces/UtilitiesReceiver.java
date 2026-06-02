
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;

public interface UtilitiesReceiver {

    void onReceiveUtilities(ArrayList<UtilitiesInfo> mUtilitiesInfos);

    void onError(Exception error);
}