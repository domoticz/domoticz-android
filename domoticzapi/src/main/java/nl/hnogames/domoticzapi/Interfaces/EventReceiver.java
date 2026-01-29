
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.EventInfo;

public interface EventReceiver {
    void onReceiveEvents(ArrayList<EventInfo> mEventInfos);

    void onError(Exception error);
}
