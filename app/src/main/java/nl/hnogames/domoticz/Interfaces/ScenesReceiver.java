package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.SceneInfo;

public interface ScenesReceiver {

    void onReceiveScenes(ArrayList<SceneInfo> scenes);

    void onError(Exception error);
}
