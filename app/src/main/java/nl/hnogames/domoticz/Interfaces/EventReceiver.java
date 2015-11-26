package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Containers.UserVariableInfo;

public interface EventReceiver {
    void onReceiveEvents(ArrayList<EventInfo> mEventInfos);
    void onError(Exception error);
}
