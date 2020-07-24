package nl.hnogames.domoticz.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.HashMap;

import nl.hnogames.domoticz.CameraActivity;
import nl.hnogames.domoticz.helpers.StaticHelper;

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

    public static void ProcessImage(Context context, Bitmap savePic, String title) {
        File dir = StaticHelper.getDomoticz(context).saveSnapShot(savePic, title);
        if (dir != null) {
            Intent intent = new Intent(context, CameraActivity.class);
            intent.putExtra("IMAGETITLE", title);
            intent.putExtra("IMAGEURL", dir.getPath());
            context.startActivity(intent);
        }
    }
}
