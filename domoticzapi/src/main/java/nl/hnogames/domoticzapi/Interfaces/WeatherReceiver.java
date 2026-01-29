
package nl.hnogames.domoticzapi.Interfaces;

import java.util.ArrayList;

import nl.hnogames.domoticzapi.Containers.WeatherInfo;

public interface WeatherReceiver {

    void onReceiveWeather(ArrayList<WeatherInfo> mWeatherInfos);

    void onError(Exception error);
}