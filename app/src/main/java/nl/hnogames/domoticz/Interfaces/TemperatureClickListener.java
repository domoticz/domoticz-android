package nl.hnogames.domoticz.Interfaces;

import nl.hnogames.domoticz.Containers.TemperatureInfo;

public interface TemperatureClickListener {
    void onLogClick(TemperatureInfo temp, String range);
}