package nl.hnogames.domoticz.Utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

public class BitmapLruCache
        extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {

    public BitmapLruCache() {
        this(getDefaultLruCacheSize());
    }

    public BitmapLruCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    public static int getDefaultLruCacheSize() {
        //final int maxMemory =
        //        (int) (Runtime.getRuntime().maxMemory() / 1024);
        //final int cacheSize = maxMemory / 8;

        return 1;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }
}