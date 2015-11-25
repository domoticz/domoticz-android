package nl.hnogames.domoticz.Interfaces;

import android.content.Context;

import java.util.List;

/**
 * Created by admin on 12/29/13.
 */
public interface IDynamicProvider {
    public int getCount();

    public <T> List<T> getItems();

    @Deprecated
    public void populate();

    public void populate(Context context);
}