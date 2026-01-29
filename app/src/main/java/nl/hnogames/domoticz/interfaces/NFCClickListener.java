
package nl.hnogames.domoticz.interfaces;


import nl.hnogames.domoticz.containers.NFCInfo;

public interface NFCClickListener {
    boolean onEnableClick(NFCInfo nfc, boolean checked);

    void onRemoveClick(NFCInfo nfc);
}