package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.UserVariableInfo;

public interface UserVariablesReceiver {
    void onReceiveUserVariabeles(ArrayList<UserVariableInfo> mVarInfos);

    void onError(Exception error);
}
