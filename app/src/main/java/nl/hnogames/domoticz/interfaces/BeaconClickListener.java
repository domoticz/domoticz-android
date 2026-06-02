
package nl.hnogames.domoticz.interfaces;

import nl.hnogames.domoticz.containers.BeaconInfo;

public interface BeaconClickListener {
    boolean onEnableClick(BeaconInfo beacon, boolean checked);

    void onRemoveClick(BeaconInfo beacon);
}