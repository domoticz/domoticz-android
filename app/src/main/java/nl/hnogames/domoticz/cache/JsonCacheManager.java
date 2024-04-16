package nl.hnogames.domoticz.cache;

import android.content.Context;
import androidx.room.Room;

public class JsonCacheManager {
    private static final String DATABASE_NAME = "json_cache_database";
    private AppDatabase db;

    public JsonCacheManager(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
    }

    public void saveJson(String key, String json) {
        JsonCache cache = new JsonCache();
        cache.key = key;
        cache.json = json;
        cache.timestamp = System.currentTimeMillis();
        db.jsonCacheDao().insertCache(cache);
    }

    public String getJson(String key) {
        JsonCache cache = db.jsonCacheDao().getCache(key);
        if (cache != null) {
            return cache.json;
        } else {
            return null;
        }
    }

    public void clearAllCache() {
        db.jsonCacheDao().deleteAll();
    }

    public void removeCache(String key) {
        JsonCache cache = db.jsonCacheDao().getCache(key);
        if (cache != null) {
            db.jsonCacheDao().delete(cache);
        }
    }
}