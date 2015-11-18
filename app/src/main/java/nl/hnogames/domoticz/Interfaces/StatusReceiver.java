package nl.hnogames.domoticz.Interfaces;

import nl.hnogames.domoticz.Containers.ExtendedStatusInfo;

public interface StatusReceiver {

    void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo);

    void onError(Exception error);
}