package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.DevicesInfo;

public interface DevicesReceiver {

    void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo);

    void onError(Exception error);
}
