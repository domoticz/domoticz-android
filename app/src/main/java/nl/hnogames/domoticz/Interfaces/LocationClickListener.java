package nl.hnogames.domoticz.Interfaces;

import nl.hnogames.domoticz.Containers.LocationInfo;

public interface LocationClickListener {

    void onEnableClick(LocationInfo location, boolean checked);

    void onRemoveClick(LocationInfo location);
}