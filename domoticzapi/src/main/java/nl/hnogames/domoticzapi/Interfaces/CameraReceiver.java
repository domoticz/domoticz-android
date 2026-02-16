
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.CameraInfo;

public interface CameraReceiver {
    void OnReceiveCameras(ArrayList<CameraInfo> plans);

    void onError(Exception error);
}
