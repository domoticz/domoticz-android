package nl.hnogames.domoticz.widgets.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class WidgetDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "widgets.db";
    private static final int DATABASE_VERSION = 2;

    public WidgetDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WidgetContract.WidgetEntry.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WidgetContract.WidgetEntry.TABLE_WIDGET);
        onCreate(db);
    }

    public void saveWidgetConfiguration(int widgetId, ContentValues values) {
        Log.d("WidgetDbHelper", "Saving configuration for widgetId: " + widgetId);
        SQLiteDatabase db = getWritableDatabase();
        try {
            // Ensure that widgetId is included in the ContentValues
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_ID, widgetId);
            db.insertWithOnConflict(WidgetContract.WidgetEntry.TABLE_WIDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        } finally {
            db.close();
        }
        Log.d("WidgetDbHelper", "Saved configuration for widgetId: " + widgetId);
    }

    public ContentValues getWidgetConfiguration(int widgetId) {
        Log.d("WidgetDbHelper", "Getting configuration for widgetId: " + widgetId);
        SQLiteDatabase db = getReadableDatabase();
        ContentValues values = new ContentValues();
        try {
            try (Cursor cursor = db.query(
                    WidgetContract.WidgetEntry.TABLE_WIDGET,
                    null,
                    WidgetContract.WidgetEntry.COLUMN_WIDGET_ID + " = ?",
                    new String[]{String.valueOf(widgetId)},
                    null,
                    null,
                    null
            )) {
                if (cursor != null && cursor.moveToFirst()) {
                    values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_TYPE, cursor.getString(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_TYPE)));
                    values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX, cursor.getInt(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX)));
                    values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE, cursor.getInt(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE)) != 0);
                    values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD, cursor.getString(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD)));
                    values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID, cursor.getInt(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID)));
                    values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_VALUE, cursor.getString(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_VALUE)));
                    // Retrieve other columns as needed
                    Log.d("WidgetDbHelper", "Retrieved configuration for widgetId: " + widgetId);
                } else {
                    Log.d("WidgetDbHelper", "No configuration found for widgetId: " + widgetId);
                }
            }
        } finally {
            db.close();
        }
        return values;
    }

    public void deleteWidgetConfiguration(int widgetId) {
        Log.d("WidgetDbHelper", "Deleting data for widgetId: " + widgetId);
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(WidgetContract.WidgetEntry.TABLE_WIDGET, WidgetContract.WidgetEntry.COLUMN_WIDGET_ID + " = ?", new String[]{String.valueOf(widgetId)});
        } finally {
            db.close();
        }
        Log.d("WidgetDbHelper", "Data deleted for widgetId: " + widgetId);
    }
}
