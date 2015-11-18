package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.CameraInfo;

public interface CameraReceiver {
    void OnReceiveCameras(ArrayList<CameraInfo> plans);

    void onError(Exception error);
}
