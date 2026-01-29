
package nl.hnogames.domoticz.interfaces;

import android.view.View;

import nl.hnogames.domoticzapi.Containers.WeatherInfo;

public interface WeatherClickListener {
    void onLogClick(WeatherInfo weather, String range);

    void onLikeButtonClick(int idx, boolean checked);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(int position);
}