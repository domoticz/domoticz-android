
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.UserVariableInfo;

public interface UserVariablesReceiver {

    void onReceiveUserVariables(ArrayList<UserVariableInfo> mVarInfos);

    void onError(Exception error);
}