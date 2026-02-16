
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.NotificationTypeInfo;

public interface NotificationTypesReceiver {
    void onReceive(ArrayList<NotificationTypeInfo> mNotificationTypes);

    void onError(Exception error);
}
