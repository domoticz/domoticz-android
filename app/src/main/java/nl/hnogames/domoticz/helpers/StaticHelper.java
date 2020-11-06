package nl.hnogames.domoticz.helpers;

import android.content.Context;

import nl.hnogames.domoticz.BuildConfig;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticzapi.Domoticz;
import nl.hnogames.domoticzapi.Utils.ServerUtil;
import nl.hnogames.domoticzapi.Utils.SessionUtil;

public class StaticHelper {
    private static Domoticz domoticz;

    public static Domoticz getDomoticz(Context context) {
        if (domoticz == null)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        if (BuildConfig.PAID_OOTT)
            Domoticz.BasicAuthDetected = true;
        return domoticz;
    }

    public static Domoticz getDomoticz(Context context, boolean refresh) {
        if (refresh)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        if (BuildConfig.PAID_OOTT)
            Domoticz.BasicAuthDetected = true;
        return domoticz;
    }

    public static SessionUtil getSessionUtil(Context context) {
        if (domoticz == null)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        if (BuildConfig.PAID_OOTT)
            Domoticz.BasicAuthDetected = true;
        return domoticz.getSessionUtil();
    }

    public static ServerUtil getServerUtil(Context context) {
        if (domoticz == null)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        if (BuildConfig.PAID_OOTT)
            Domoticz.BasicAuthDetected = true;
        return domoticz.getServerUtil();
    }

    public static ServerUtil getServerUtil(Context context, boolean refresh) {
        if (refresh)
            domoticz = new Domoticz(context, AppController.getInstance().getRequestQueue());
        if (BuildConfig.PAID_OOTT)
            Domoticz.BasicAuthDetected = true;
        return domoticz.getServerUtil();
    }
}