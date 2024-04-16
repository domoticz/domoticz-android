package nl.hnogames.domoticz.cache;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "json_cache")
public class JsonCache {
    @PrimaryKey
    @NonNull
    public String key;
    public String json;
    public long timestamp;
}