
package nl.hnogames.domoticz.interfaces;

import nl.hnogames.domoticz.containers.QRCodeInfo;

public interface QRCodeClickListener {
    boolean onEnableClick(QRCodeInfo nfc, boolean checked);

    void onRemoveClick(QRCodeInfo nfc);
}