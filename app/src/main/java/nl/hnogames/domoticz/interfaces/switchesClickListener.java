
package nl.hnogames.domoticz.interfaces;

import android.view.View;

import nl.hnogames.domoticzapi.Containers.DevicesInfo;

public interface switchesClickListener {

    void onSwitchClick(DevicesInfo device, boolean action);

    void onBlindClick(DevicesInfo device, int action);

    void onDimmerChange(DevicesInfo device, int value, boolean selector);

    void onButtonClick(DevicesInfo device, boolean action);

    void onLogButtonClick(DevicesInfo device);

    void onLikeButtonClick(DevicesInfo device, boolean checked);

    void onColorButtonClick(DevicesInfo device);

    void onTimerButtonClick(DevicesInfo device);

    void onNotificationButtonClick(DevicesInfo device);

    void onThermostatClick(DevicesInfo device);

    void onSetTemperatureClick(DevicesInfo device);

    void onSecurityPanelButtonClick(DevicesInfo device);

    void onStateButtonClick(DevicesInfo device, int itemsRes, int[] itemIds);

    void onSelectorDimmerClick(DevicesInfo device, String[] levelNames);

    void onSelectorChange(DevicesInfo device, int l);

    void onItemClicked(View v, int position);

    boolean onItemLongClicked(DevicesInfo device);

    void onCameraFullScreenClick(DevicesInfo device, String name);

    void OnModeChanged(DevicesInfo utility, int id, String mode);
}