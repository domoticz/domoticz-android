package nl.hnogames.domoticz.widgets.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import nl.hnogames.domoticz.widgets.data.WidgetDao;
import nl.hnogames.domoticz.widgets.data.WidgetEntity;

/**
 * Widget database using Room
 * Ensures data persistence across app updates
 */
@Database(entities = {WidgetEntity.class}, version = 1, exportSchema = false)
public abstract class WidgetDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "widgets.db";
    private static volatile WidgetDatabase INSTANCE;

    public abstract WidgetDao widgetDao();

    public static WidgetDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (WidgetDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        WidgetDatabase.class,
                        DATABASE_NAME
                    )
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
