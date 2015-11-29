package nl.hnogames.domoticz.Interfaces;

import nl.hnogames.domoticz.Containers.UtilitiesInfo;

public interface UtilityClickListener {
    void onClick(UtilitiesInfo utility);

    void onThermostatClick(int idx, int action, long newSetPoint);
}