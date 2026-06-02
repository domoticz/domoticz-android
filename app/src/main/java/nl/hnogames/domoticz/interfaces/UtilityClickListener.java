
package nl.hnogames.domoticz.interfaces;

import android.view.View;

import nl.hnogames.domoticzapi.Containers.UtilitiesInfo;

public interface UtilityClickListener {
    void onClick(UtilitiesInfo utility);

    void OnModeChanged(UtilitiesInfo utility, int id, String mode);

    void onLogClick(UtilitiesInfo utility, String range);

    void onThermostatClick(int idx);

    void onLogButtonClick(int idx);

    void onLikeButtonClick(int idx, boolean checked);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(int position);
}