package nl.hnogames.domoticz.widgets.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Widget configuration entity using Room for proper persistence
 * Data survives app updates/upgrades
 */
@Entity(tableName = "widgets")
public class WidgetEntity {
    @PrimaryKey
    public int widgetId;

    public int entityIdx;
    public String entityName;
    public String entityType;
    public boolean isScene;
    public String layoutStyle;
    public String themeStyle;
    public long lastUpdate;
    public String password;
    public String customValue;

    @Ignore
    public WidgetEntity(int widgetId, int entityIdx, String entityName, String entityType,
                       boolean isScene, String layoutStyle, String themeStyle,
                       long lastUpdate, String password, String customValue) {
        this.widgetId = widgetId;
        this.entityIdx = entityIdx;
        this.entityName = entityName != null ? entityName : "";
        this.entityType = entityType != null ? entityType : "";
        this.isScene = isScene;
        this.layoutStyle = layoutStyle != null ? layoutStyle : "auto";
        this.themeStyle = themeStyle != null ? themeStyle : "auto";
        this.lastUpdate = lastUpdate;
        this.password = password;
        this.customValue = customValue;
    }

    public WidgetEntity() {
        this.entityName = "";
        this.entityType = "";
        this.layoutStyle = "auto";
        this.themeStyle = "auto";
        this.lastUpdate = System.currentTimeMillis();
    }
}
