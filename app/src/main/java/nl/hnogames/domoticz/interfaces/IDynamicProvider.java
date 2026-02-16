
package nl.hnogames.domoticz.interfaces;

import android.content.Context;

import java.util.List;

@SuppressWarnings("unused")
public interface IDynamicProvider {
    int getCount();

    <T> List<T> getItems();

    void populate();

    void populate(Context context);
}