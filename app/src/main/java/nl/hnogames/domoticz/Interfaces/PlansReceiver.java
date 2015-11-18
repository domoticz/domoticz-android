package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.PlanInfo;

public interface PlansReceiver {

    void OnReceivePlans(ArrayList<PlanInfo> plans);

    void onError(Exception error);
}
