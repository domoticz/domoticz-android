package nl.hnogames.domoticz.interfaces;

import android.nfc.Tag;

public interface OnDiscoveredTagListener {
    public void tagDiscovered(Tag t);
}