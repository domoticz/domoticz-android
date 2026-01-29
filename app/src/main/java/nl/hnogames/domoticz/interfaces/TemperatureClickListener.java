
package nl.hnogames.domoticz.interfaces;

import android.view.View;

import nl.hnogames.domoticzapi.Containers.TemperatureInfo;

public interface TemperatureClickListener {
    void onLogClick(TemperatureInfo temp, String range);

    void onSetClick(TemperatureInfo t);

    void onLikeButtonClick(int idx, boolean checked);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(int position);
}