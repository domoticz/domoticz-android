package nl.hnogames.domoticz.service.tiles;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import nl.hnogames.domoticz.utils.SharedPrefUtil;

public class GeofenceTileService extends TileService {
    private SharedPrefUtil mSharedPrefUtil;

    @Override
    public void onClick() {
        super.onClick();

        if(mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(this);

        boolean isEnabled = !mSharedPrefUtil.isGeofenceEnabled();
        mSharedPrefUtil.setGeofenceEnabled(isEnabled);

        updateTile(isEnabled);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if(mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(this);

        boolean isEnabled = mSharedPrefUtil.isGeofenceEnabled();
        updateTile(isEnabled);
    }

    private void updateTile(boolean isEnabled) {
        Tile tile = getQsTile();
        if (isEnabled) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }
        tile.updateTile();
    }
}