package nl.hnogames.domoticz.cache;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {JsonCache.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract JsonCacheDao jsonCacheDao();
}