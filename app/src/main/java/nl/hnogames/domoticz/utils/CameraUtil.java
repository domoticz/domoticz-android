package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

import nl.hnogames.domoticz.CameraActivity;

public class CameraUtil {
    private static HashMap<String, Drawable> cameraCache = new HashMap<>();

    public static Drawable getDrawable(String url) {
        if (cameraCache.containsKey(url))
            return cameraCache.get(url);
        return null;
    }

    public static void setDrawable(String url, Drawable drawable) {
        cameraCache.remove(url);
        cameraCache.put(url, drawable);
    }

    public static void ProcessImage(Context context, int idx, String title) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("CAMERATITLE", title);
        intent.putExtra("CAMERAIDX", idx);
        context.startActivity(intent);
    }
}
