
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.ExtendedStatusInfo;

public interface StatusReceiver {

    void onReceiveStatus(ExtendedStatusInfo extendedStatusInfo);

    void onError(Exception error);
}