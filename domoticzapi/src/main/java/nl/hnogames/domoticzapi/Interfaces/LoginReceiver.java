
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.LoginInfo;

public interface LoginReceiver {
    void OnReceive(LoginInfo mLoginInfo);

    void onError(Exception error);
}
