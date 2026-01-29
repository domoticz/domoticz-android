
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.EventXmlInfo;

public interface EventXmlReceiver {
    void onReceiveEventXml(ArrayList<EventXmlInfo> mEventXmlInfos);

    void onError(Exception error);
}
