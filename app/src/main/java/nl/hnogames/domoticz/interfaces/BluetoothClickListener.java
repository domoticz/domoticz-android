
package nl.hnogames.domoticz.interfaces;

import nl.hnogames.domoticz.containers.BluetoothInfo;

public interface BluetoothClickListener {
    boolean onEnableClick(BluetoothInfo device, boolean checked);

    void onRemoveClick(BluetoothInfo device);
}