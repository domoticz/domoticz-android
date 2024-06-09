package nl.hnogames.domoticz.service.tiles;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import nl.hnogames.domoticz.R;
import nl.hnogames.domoticz.app.AppController;
import nl.hnogames.domoticz.utils.SharedPrefUtil;
import nl.hnogames.domoticz.utils.UsefulBits;

public class SpeechRecognitionTileService extends TileService {
    private SharedPrefUtil mSharedPrefUtil;

    @Override
    public void onClick() {
        super.onClick();

        if (mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(this);

        boolean isSpeechRecognitionEnabled = !mSharedPrefUtil.isSpeechEnabled();
        if (isSpeechRecognitionEnabled) {
            if (!AppController.IsPremiumEnabled || !mSharedPrefUtil.isAPKValidated()) {
                UsefulBits.showPremiumToast(this, getString(R.string.category_Speech));
                return;
            }
        }

        mSharedPrefUtil.setSpeechEnabled(isSpeechRecognitionEnabled);
        updateTile(isSpeechRecognitionEnabled);
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        if (mSharedPrefUtil == null)
            mSharedPrefUtil = new SharedPrefUtil(this);
        updateTile(mSharedPrefUtil.isSpeechEnabled());
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