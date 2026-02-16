
package nl.hnogames.domoticz.interfaces;

import nl.hnogames.domoticzapi.Containers.UserVariableInfo;

@SuppressWarnings("unused")
public interface UserVariablesClickListener {
    void onUserVariableClick(UserVariableInfo clickedVar);
}