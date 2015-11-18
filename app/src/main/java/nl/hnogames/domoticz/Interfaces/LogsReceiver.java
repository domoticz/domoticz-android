package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.LogInfo;

public interface LogsReceiver {

    void onReceiveLogs(ArrayList<LogInfo> mLogInfos);

    void onError(Exception error);
}
