
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.PlanInfo;

public interface PlansReceiver {

    void OnReceivePlans(ArrayList<PlanInfo> plans);

    void onError(Exception error);
}