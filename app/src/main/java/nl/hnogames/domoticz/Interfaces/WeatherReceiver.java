package nl.hnogames.domoticz.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticz.Containers.WeatherInfo;

public interface WeatherReceiver {

    void onReceiveWeather(ArrayList<WeatherInfo> mWeatherInfos);

    void onError(Exception error);
}
