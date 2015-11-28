package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.EventInfo;
import nl.hnogames.domoticz.Containers.EventXmlInfo;

public interface EventXmlReceiver {
    void onReceiveEventXml(ArrayList<EventXmlInfo> mEventXmlInfos);
    void onError(Exception error);
}
