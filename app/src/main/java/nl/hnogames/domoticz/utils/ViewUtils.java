package nl.hnogames.domoticz.utils;

import android.content.Context;
import nl.hnogames.domoticz.R;

public class ViewUtils {
    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }
}