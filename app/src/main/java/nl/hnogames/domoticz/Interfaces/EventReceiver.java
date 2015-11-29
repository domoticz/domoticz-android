package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.EventInfo;

public interface EventReceiver {
    void onReceiveEvents(ArrayList<EventInfo> mEventInfos);

    void onError(Exception error);
}
