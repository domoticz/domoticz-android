package nl.hnogames.domoticz.widgets;

import android.provider.BaseColumns;

public final class WidgetContract {

    private WidgetContract() {
    }

    public static class WidgetEntry implements BaseColumns {
        public static final String TABLE_WIDGET = "widgets";
        public static final String COLUMN_WIDGET_ID = "widget_id";
        public static final String COLUMN_WIDGET_TYPE = "widget_type";
        public static final String COLUMN_WIDGET_IDX = "widget_idx";
        public static final String COLUMN_WIDGET_IS_SCENE = "widget_is_scene";
        public static final String COLUMN_WIDGET_PASSWORD = "widget_password";
        public static final String COLUMN_WIDGET_LAYOUT_ID = "widget_layout_id";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_WIDGET + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_WIDGET_ID + " INTEGER," +
                        COLUMN_WIDGET_TYPE + " TEXT," +
                        COLUMN_WIDGET_IDX + " INTEGER," +
                        COLUMN_WIDGET_IS_SCENE + " INTEGER," +
                        COLUMN_WIDGET_PASSWORD + " TEXT," +
                        COLUMN_WIDGET_LAYOUT_ID + " INTEGER)";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_WIDGET;
    }
}
