
package nl.hnogames.domoticz.interfaces;

import nl.hnogames.domoticz.containers.LocationInfo;

public interface LocationClickListener {

    boolean onEnableClick(LocationInfo location, boolean checked);

    void onRemoveClick(LocationInfo location);
}