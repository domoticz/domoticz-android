
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.SceneInfo;

public interface ScenesReceiver {

    void onReceiveScenes(ArrayList<SceneInfo> scenes);

    void onError(Exception error);

    void onReceiveScene(SceneInfo scene);
}
