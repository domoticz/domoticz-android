package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.GraphPointInfo;

public interface GraphDataReceiver {
    void onReceive(ArrayList<GraphPointInfo> mGraphList);

    void onError(Exception error);
}
