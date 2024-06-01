package nl.hnogames.domoticz.widgets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WidgetDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "widgets.db";
    private static final int DATABASE_VERSION = 1;

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
        SQLiteDatabase db = getWritableDatabase();

        // Ensure that widgetId is included in the ContentValues
        values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_ID, widgetId);

        long newRowId = db.insertWithOnConflict(WidgetContract.WidgetEntry.TABLE_WIDGET, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ContentValues getWidgetConfiguration(int widgetId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                WidgetContract.WidgetEntry.COLUMN_WIDGET_TYPE,
                WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX,
                WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE,
                WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD,
                WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID
                // Add other columns as needed
        };

        String selection = WidgetContract.WidgetEntry.COLUMN_WIDGET_ID + " = ?";
        String[] selectionArgs = {String.valueOf(widgetId)};

        Cursor cursor = db.query(
                WidgetContract.WidgetEntry.TABLE_WIDGET,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        ContentValues values = new ContentValues();

        if (cursor != null && cursor.moveToFirst()) {
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_TYPE, cursor.getString(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_TYPE)));
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX, cursor.getInt(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_IDX)));
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE, cursor.getInt(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_IS_SCENE)) != 0);
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD, cursor.getString(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_PASSWORD)));
            values.put(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID, cursor.getInt(cursor.getColumnIndexOrThrow(WidgetContract.WidgetEntry.COLUMN_WIDGET_LAYOUT_ID)));
            // Retrieve other columns as needed

            cursor.close();
        }

        return values;
    }

    public void deleteWidgetConfiguration(int widgetId) {
        SQLiteDatabase db = getWritableDatabase();

        String selection = WidgetContract.WidgetEntry.COLUMN_WIDGET_ID + " = ?";
        String[] selectionArgs = {String.valueOf(widgetId)};

        db.delete(WidgetContract.WidgetEntry.TABLE_WIDGET, selection, selectionArgs);
    }
}
