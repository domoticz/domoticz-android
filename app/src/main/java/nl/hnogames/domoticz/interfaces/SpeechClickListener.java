
package nl.hnogames.domoticz.interfaces;


import nl.hnogames.domoticz.containers.SpeechInfo;

public interface SpeechClickListener {
    boolean onEnableClick(SpeechInfo speech, boolean checked);

    void onRemoveClick(SpeechInfo speech);
}