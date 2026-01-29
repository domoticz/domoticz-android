
package nl.hnogames.domoticz.interfaces;

import nl.hnogames.domoticz.containers.WifiInfo;

public interface WifiClickListener {
    boolean onEnableClick(WifiInfo device, boolean checked);

    void onRemoveClick(WifiInfo device);
}