
package nl.hnogames.domoticzapi.Interfaces;

import nl.hnogames.domoticzapi.Containers.Language;

public interface LanguageReceiver {
    void onReceiveLanguage(Language language);

    void onError(Exception error);
}
