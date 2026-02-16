
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.EnergyDashboardInfo;

/**
 * Interface for receiving energy dashboard configuration data
 */
public interface EnergyDashboardReceiver {
    void onReceiveEnergyDashboard(EnergyDashboardInfo energyDashboard);

    void onError(Exception error);
}

