package nl.hnogames.domoticz.utils;

import android.graphics.drawable.Drawable;

import java.util.HashMap;

public class CameraUtil {
    private static HashMap<String, Drawable> cameraCache = new HashMap<>();

    public static Drawable getDrawable(String url) {
        if (cameraCache.containsKey(url))
            return cameraCache.get(url);
        return null;
    }

    public static void setDrawable(String url, Drawable drawable) {
        if (cameraCache.containsKey(url))
            cameraCache.remove(url);
        cameraCache.put(url, drawable);
    }
}
