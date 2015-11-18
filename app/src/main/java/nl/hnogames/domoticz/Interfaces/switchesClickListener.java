package nl.hnogames.domoticz.Interfaces;

public interface switchesClickListener {

    void onSwitchClick(int idx, boolean action);

    void onBlindClick(int idx, int action);

    void onDimmerChange(int idx, int value);

    void onButtonClick(int idx, boolean action);

    void onLogButtonClick(int idx);

    void onTimerButtonClick(int idx);
}