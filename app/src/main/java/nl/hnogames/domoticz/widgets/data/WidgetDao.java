package nl.hnogames.domoticz.widgets.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface WidgetDao {
    @Query("SELECT * FROM widgets WHERE widgetId = :widgetId")
    WidgetEntity getWidget(int widgetId);

    @Query("SELECT * FROM widgets WHERE widgetId = :widgetId")
    LiveData<WidgetEntity> getWidgetLiveData(int widgetId);

    @Query("SELECT * FROM widgets")
    LiveData<List<WidgetEntity>> getAllWidgets();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertWidget(WidgetEntity widget);

    @Query("DELETE FROM widgets WHERE widgetId = :widgetId")
    void deleteWidget(int widgetId);

    @Query("UPDATE widgets SET lastUpdate = :timestamp WHERE widgetId = :widgetId")
    void updateTimestamp(int widgetId, long timestamp);
}
