
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.GraphPointInfo;

public interface GraphDataReceiver {
    void onReceive(ArrayList<GraphPointInfo> mGraphList);

    void onError(Exception error);
}
