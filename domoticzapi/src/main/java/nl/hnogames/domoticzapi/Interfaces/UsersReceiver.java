
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.UserInfo;

public interface UsersReceiver {
    void onReceiveUsers(ArrayList<UserInfo> mUserInfo);

    void onError(Exception error);
}
