
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.NotificationInfo;

public interface NotificationReceiver {
    void onReceiveNotifications(ArrayList<NotificationInfo> mNotificationInfos);

    void onError(Exception error);
}
