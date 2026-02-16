
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.SunRiseInfo;

public interface SunRiseReceiver {
    void onReceive(SunRiseInfo mSunRiseInfo);

    void onError(Exception error);
}
