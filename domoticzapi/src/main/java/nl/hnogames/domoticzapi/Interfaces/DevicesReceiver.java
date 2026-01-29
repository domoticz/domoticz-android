
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.DevicesInfo;

public interface DevicesReceiver {
    void onReceiveDevices(ArrayList<DevicesInfo> mDevicesInfo);

    void onReceiveDevice(DevicesInfo mDevicesInfo);

    void onError(Exception error);
}
