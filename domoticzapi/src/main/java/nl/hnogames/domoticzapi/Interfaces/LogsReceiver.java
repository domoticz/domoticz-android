
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.LogInfo;

public interface LogsReceiver {
    void onReceiveLogs(ArrayList<LogInfo> mLogInfos);

    void onError(Exception error);
}
