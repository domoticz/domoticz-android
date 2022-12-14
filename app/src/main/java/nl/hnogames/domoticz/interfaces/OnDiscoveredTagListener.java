package nl.hnogames.domoticz.interfaces;

import android.nfc.Tag;

public interface OnDiscoveredTagListener {
    void tagDiscovered(Tag t);
}