package nl.hnogames.domoticz.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface JsonCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCache(JsonCache jsonCache);

    @Query("SELECT * FROM json_cache WHERE key = :key")
    JsonCache getCache(String key);

    @Query("DELETE FROM json_cache")
    void deleteAll();

    @Delete
    void delete(JsonCache jsonCache);
}